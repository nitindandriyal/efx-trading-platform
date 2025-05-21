package play.lab.marketdata.generator;

import io.aeron.Aeron;
import io.aeron.Publication;
import org.agrona.concurrent.UnsafeBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.lab.marketdata.model.MarketDataTick;
import pub.lab.trading.common.config.AeronConfigs;
import pub.lab.trading.common.config.StreamId;
import pub.lab.trading.common.lifecycle.ArrayObjectPool;
import pub.lab.trading.common.model.ClientTierLevel;
import pub.lab.trading.common.model.Tenor;
import pub.lab.trading.common.model.pricing.QuoteMessageWriter;
import pub.lab.trading.common.util.MutableString;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class QuotePublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuotePublisher.class);

    private final QuoteMessageWriter quoteMessageWriter;
    private final UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(2048));
    private final Publication quotePub;
    private final ArrayObjectPool<MutableString> currencyPairObjectPool = new ArrayObjectPool<>("currencyPairObjectPool", MutableString::new);

    QuotePublisher() {
        Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(AeronConfigs.AERON_LIVE_DIR));
        this.quotePub = aeron.addPublication(AeronConfigs.LIVE_CHANNEL, StreamId.RAW_QUOTE.getCode());
        this.quoteMessageWriter = new QuoteMessageWriter();
        LOGGER.info("Connected Aeron Dir : {} {} {}", aeron.context().aeronDirectory(), quotePub.channel(), quotePub.streamId());

        // Wait for the subscriber to connect
        while (!quotePub.isConnected()) {
            LOGGER.warn("⏳ Waiting for subscriber...");
            try {
                TimeUnit.MILLISECONDS.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    void publish(final MarketDataTick marketDataTick) {
        try {
            quoteMessageWriter.beginQuote(
                            marketDataTick.getPair(),
                            marketDataTick.getValueDateEpoch(),
                            marketDataTick.getValueDateEpoch(),
                            Tenor.SPOT.getCode(),
                            ClientTierLevel.GOLD.getId(),
                            1).
                    addRung(
                            marketDataTick.getBid(),
                            marketDataTick.getAsk(),
                            1_000_000
                    );
            UnsafeBuffer buffer = quoteMessageWriter.buffer();
            int encodedLength = quoteMessageWriter.encodedLength();

            long result = quotePub.offer(buffer, 0, encodedLength);
            if (result < 0) {
                LOGGER.error("❌ Failed to publish quote for {} — code {}, channel: {}, streamId: {}, status: {}",
                        marketDataTick.getPair(), result, quotePub.channel(), quotePub.streamId(), quotePub.channelStatus());
            } else {
                LOGGER.info("✅ Published quote for {}", marketDataTick.getPair());
                try {
                    TimeUnit.MILLISECONDS.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error publishing quote for {}: {}", marketDataTick.getPair(), e.getMessage(), e);
            throw e;
        }
    }

    boolean isReady() {
        return quotePub.isConnected();
    }
}
