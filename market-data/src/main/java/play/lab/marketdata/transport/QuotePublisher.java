package play.lab.marketdata.transport;

import io.aeron.Aeron;
import io.aeron.Publication;
import org.agrona.concurrent.UnsafeBuffer;
import play.lab.marketdata.model.MarketDataTick;
import pub.lab.trading.common.config.AeronConfigs;
import pub.lab.trading.common.lifecycle.ArrayObjectPool;
import pub.lab.trading.common.model.pricing.QuoteWriter;
import pub.lab.trading.common.util.MutableString;

import java.nio.ByteBuffer;

public class QuotePublisher {
    private final QuoteWriter quoteWriter;
    private final UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(2048));
    private final Publication quotePub;
    private final ArrayObjectPool<MutableString> currencyPairObjectPool = new ArrayObjectPool<>("currencyPairObjectPool", MutableString::new);

    public QuotePublisher() {
        String aeronDir = System.getProperty("aeron.base.path") + AeronConfigs.LIVE_DIR;
        Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(aeronDir));
        this.quotePub = aeron.addExclusivePublication("aeron:ipc", 10);
        this.quoteWriter = new QuoteWriter();
    }

    public void publish(final MarketDataTick marketDataTick) {
        double[] bid = new double[1];
        bid[0] = marketDataTick.getBid();
        double[] ask = new double[1];
        ask[0] = marketDataTick.getAsk();


        long result = quotePub.offer(buffer, 0, quoteWriter.encodedLength());
        if (result < 0) {
            System.err.println("❌ Failed to publish quote for " + marketDataTick.getPair() + " — code " + result);
        }
    }
}
