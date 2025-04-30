package play.lab;

import io.aeron.Aeron;
import io.aeron.Publication;

import org.agrona.concurrent.UnsafeBuffer;
import play.lab.model.sbe.MessageHeaderEncoder;
import pub.lab.trading.model.pricing.QuoteWriter;
import pub.lab.trading.model.ClientTierLevel;
import pub.lab.trading.model.Tenor;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.List;

public class QuotePublisherMain {
    public static void main(String[] args) throws Exception {
        // 1. Start Aeron embedded
        Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName("aeron-trading"));

        // 2. Setup IPC publication on Quote stream (10)
        Publication quotePub = aeron.addExclusivePublication("aeron:ipc", 10);

        // 3. Prepare reusable components
        FxPriceGenerator generator = new FxPriceGenerator();
        QuoteWriter quoteWriter = new QuoteWriter();
        MessageHeaderEncoder header = new MessageHeaderEncoder();
        UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(2048)); // Enough for full book

        // 4. Start loop

        System.out.println("📡 Streaming quotes to Aeron IPC [stream=10] — Ctrl+C to stop");

        long lastSend = System.currentTimeMillis();
        while (true) {
            long now = System.currentTimeMillis();
            double dt = (now - lastSend) / 1000.0;
            lastSend = now;

            List<FxPriceGenerator.FxTick> ticks = generator.generateAll(now, dt);

            for (FxPriceGenerator.FxTick tick : ticks) {
                // Make 2 bid/ask levels for the pair
                double[] bids = new double[]{tick.bid, tick.bid - 0.00005};
                double[] asks = new double[]{tick.ask, tick.ask + 0.00005};

                // Wrap + encode
                quoteWriter.wrap(buffer, 0)
                        .setCcyPair(tick.pair)
                        .setTenor(Tenor.SPOT.getCode())
                        .setValueDate(LocalDate.now().toEpochDay())
                        .setClientTier(ClientTierLevel.GOLD.getId())
                        .setPriceCreationTimestamp(tick.timestamp)
                        .setBids(bids, bids.length)
                        .setAsks(asks, asks.length);

                long result = quotePub.offer(buffer, 0, quoteWriter.encodedLength());
                if (result < 0) {
                    System.err.println("❌ Failed to publish quote for " + tick.pair + " — code " + result);
                }
                Thread.sleep(5000);
            }

            Thread.sleep(1000); // simulate tick interval
        }
    }
}
