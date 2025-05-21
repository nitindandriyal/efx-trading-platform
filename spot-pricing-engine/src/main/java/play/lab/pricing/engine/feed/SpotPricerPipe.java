package play.lab.pricing.engine.feed;

import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import org.agrona.DirectBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pub.lab.trading.common.config.AeronConfigs;
import pub.lab.trading.common.config.StreamId;
import pub.lab.trading.common.config.caches.ClientTierConfig;
import pub.lab.trading.common.config.caches.ClientTierConfigCache;
import pub.lab.trading.common.config.caches.ConfigAgent;
import pub.lab.trading.common.lifecycle.Worker;
import pub.lab.trading.common.model.ClientTierLevel;
import pub.lab.trading.common.model.pricing.QuoteMessageWriter;
import pub.lab.trading.common.model.pricing.QuoteView;

import java.util.EnumMap;

public class SpotPricerPipe implements Worker {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotPricerPipe.class);

    private final Subscription quoteSub;
    private final EnumMap<ClientTierLevel, Publication> marketQuotePublications = new EnumMap<>(ClientTierLevel.class);
    private final ClientTierConfigCache clientTierConfigCache;
    private final EnumMap<ClientTierLevel, QuoteMessageWriter> clientTierQuoteWriterEnumMap = new EnumMap<>(ClientTierLevel.class);
    private final QuoteView quoteView = new QuoteView();
    private final FragmentHandler fragmentHandler;

    public SpotPricerPipe(final Aeron aeron, final ConfigAgent configAgent) {
        this.clientTierConfigCache = configAgent.getClientTierConfigCache();
        this.fragmentHandler = (buf, offset, len, hdr) -> consumeQuotes(buf, offset);
        this.quoteSub = aeron.addSubscription(AeronConfigs.LIVE_CHANNEL,
                StreamId.RAW_QUOTE.getCode(),
                image -> LOGGER.info("Image available: sessionId={}, channel={}, streamId={}",
                        image.sessionId(), image.sourceIdentity(), image.subscription().streamId()),
                image -> LOGGER.warn("Image unavailable: sessionId={}, channel={}, streamId={}",
                        image.sessionId(), image.sourceIdentity(), image.subscription().streamId())
        );
        for (ClientTierLevel clientTierLevel : ClientTierLevel.values()) {
            marketQuotePublications.put(clientTierLevel, aeron.addExclusivePublication(AeronConfigs.LIVE_CHANNEL,
                    StreamId.MARKET_QUOTE.getCode() + clientTierLevel.getId())
            );
        }
        LOGGER.info("Connected Aeron Dir : {} {} {}", aeron.context().aeronDirectory(), quoteSub.channel(), quoteSub.streamId());
    }

    private void consumeQuotes(DirectBuffer buf, int offset) {
        quoteView.wrap(buf, offset + 8);

        String symbol = quoteView.getSymbol();
        long timestamp = quoteView.priceCreationTimestamp();
        long tenor = quoteView.getTenor();
        long valueDate = quoteView.getValueDate();
        long clientTier = quoteView.getClientTier();

        for (QuoteView.Rung rung : quoteView.getRungs()) {
            double mid = (rung.getBid() + rung.getAsk()) * 0.5;
            double volFactor = Math.log10(rung.getVolume() / 1_000_000.0 + 1.0);
            double spreadAdjust = (1 + 0.05 * volFactor);
            double markupAdjust =  (1 + 0.1 * volFactor);
            double adjustment = (markupAdjust + volFactor);
            double bid = mid - (spreadAdjust * 0.5) - adjustment;
            double ask = mid + (spreadAdjust * 0.5) + adjustment;

            LOGGER.info("symbol={}, timestamp={}, tenor={}, valueDate={}, clientTier={}, bid={}, ask={}, volume={}",
                    symbol, timestamp, tenor, valueDate, clientTier, bid, ask, rung.getVolume());
        }
    }

    @Override
    public int doWork() {
        if (quoteSub.isConnected()) {
            return quoteSub.poll(fragmentHandler, 10);
        }
        return 0;
    }

    @Override
    public void onClose() {
        Worker.super.onClose();
    }

    @Override
    public String roleName() {
        return "MarketDataConsumer";
    }
}
