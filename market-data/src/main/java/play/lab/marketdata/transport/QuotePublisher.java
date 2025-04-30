package play.lab.marketdata.transport;

import io.aeron.Aeron;
import io.aeron.Publication;
import org.agrona.concurrent.UnsafeBuffer;
import play.lab.marketdata.model.MarketDataTick;
import play.lab.model.sbe.MessageHeaderEncoder;
import pub.lab.trading.AeronConfigs;
import pub.lab.trading.model.ClientTierLevel;
import pub.lab.trading.model.Tenor;
import pub.lab.trading.model.pricing.QuoteWriter;

import java.nio.ByteBuffer;
import java.time.LocalDate;

public class QuotePublisher {
    private final QuoteWriter quoteWriter;
    private final UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(2048)); // Enough for full book
    private final Publication quotePub;

    public QuotePublisher() {
        String aeronDir = System.getProperty("aeron.base.path") + AeronConfigs.LIVE_DIR;
        // 1. Start Aeron
        Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(aeronDir));

        // 2. Setup IPC publication on Quote stream (10)
        this.quotePub = aeron.addExclusivePublication("aeron:ipc", 10);

        // 3. Prepare reusable components
        this.quoteWriter = new QuoteWriter();
        MessageHeaderEncoder header = new MessageHeaderEncoder();
    }

    public void publish(final MarketDataTick marketDataTick) {
        double[] bid = new double[1];
        bid[0] = marketDataTick.getBid();
        double[] ask = new double[1];
        ask[0] = marketDataTick.getAsk();
        quoteWriter.wrap(buffer, 0)
                .setCcyPair(marketDataTick.getPair())
                .setTenor(Tenor.SPOT.getCode())
                .setValueDate(LocalDate.now().toEpochDay())
                .setClientTier(ClientTierLevel.GOLD.getId())
                .setPriceCreationTimestamp(marketDataTick.getTimestamp())
                .setBids(bid, 1)
                .setAsks(ask, 1);

        long result = quotePub.offer(buffer, 0, quoteWriter.encodedLength());
        if (result < 0) {
            System.err.println("❌ Failed to publish quote for " + marketDataTick.getPair() + " — code " + result);
        }
    }
}
