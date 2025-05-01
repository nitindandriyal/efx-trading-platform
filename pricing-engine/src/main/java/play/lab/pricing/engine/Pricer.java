package play.lab.pricing.engine;

import io.aeron.Aeron;
import io.aeron.Publication;
import io.aeron.Subscription;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.concurrent.YieldingIdleStrategy;
import pub.lab.trading.AeronConfigs;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Pricer {
    private final Publication fxQuotes;
    private final Publication bondQuotes;
    private static final String A_CHANNEL = "aeron:ipc?alias=fx"; // Single IPC channel, 32MB buffer
    private static final String B_CHANNEL = "aeron:ipc?alias=bond"; // Single IPC channel, 32MB buffer
    private static final int FX_STREAM_ID = 1001; // Stream for FX quotes
    private static final int BOND_STREAM_ID = 1002; // Stream for bond quotes
    private final Subscription fxSubscription;
    private final Subscription bondSubscription;
    private final ExecutorService executor;
    private volatile boolean running = true;

    public Pricer(Aeron aeron) {
        // Two Publications on the same channel, different stream IDs
        fxQuotes = aeron.addPublication(A_CHANNEL, FX_STREAM_ID);
        bondQuotes = aeron.addPublication(B_CHANNEL, BOND_STREAM_ID);
        fxSubscription = aeron.addSubscription(Pricer.A_CHANNEL, Pricer.FX_STREAM_ID);
        bondSubscription = aeron.addSubscription(Pricer.B_CHANNEL, Pricer.BOND_STREAM_ID);
        executor = Executors.newFixedThreadPool(2);
        startPolling();
    }
    private void startPolling() {
        // Poll FX stream
        executor.submit(() -> {
            YieldingIdleStrategy idleStrategy = new YieldingIdleStrategy();
            AtomicInteger counter = new AtomicInteger();
            while (running) {
                int fragments = fxSubscription.poll((buffer, offset, length, header) -> {
                    if(Quote.isQuote(buffer, offset)) {
                        Quote quote = Quote.decode(buffer, offset);
                        System.out.println("Received " + counter.incrementAndGet() + " FX quote: " + quote.getInstrument() +
                                ", bid=" + quote.getBid() + ", ask=" + quote.getAsk() +
                                ", timestamp=" + quote.getTimestamp());
                    }
                }, 10);
                idleStrategy.idle(fragments);

            }
        });

        // Poll bond stream
        executor.submit(() -> {
            YieldingIdleStrategy idleStrategy = new YieldingIdleStrategy();
            AtomicInteger counter = new AtomicInteger();
            while (running) {
                int fragments = bondSubscription.poll((buffer, offset, length, header) -> {
                    if(Quote.isQuote(buffer, offset)) {
                        Quote quote = Quote.decode(buffer, offset);
                        System.out.println("Received " + counter.incrementAndGet() + " Bond quote: " + quote.getInstrument() +
                                ", bid=" + quote.getBid() + ", ask=" + quote.getAsk() +
                                ", timestamp=" + quote.getTimestamp());
                    }
                }, 10);
                idleStrategy.idle(fragments);
            }
        });
    }

    public void publishQuote(Quote quote, boolean isFx) {
        UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(8192));
        quote.encode(buffer);
        Publication pub = isFx ? fxQuotes : bondQuotes;
        long result = pub.offer(buffer);
        if (result < 0) {
            // Backpressure: shared log buffer full
            System.out.println("Backpressure on " + (isFx ? "FX" : "Bond") + " stream: " + result);
        } else {
            System.out.println("Published " + (isFx ? "FX" : "Bond") + " quote: " + quote.getInstrument());
        }
    }

    public void close() {
        fxQuotes.close();
        bondQuotes.close();
        fxSubscription.close();
        bondSubscription.close();
        executor.close();
    }

    public static void main(String[] args) {

        String aeronDir = System.getProperty("aeron.base.path") + AeronConfigs.LIVE_DIR;
        try (Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(aeronDir))) {

            Pricer pricer = new Pricer(aeron);
            Quote fxQuote = new Quote("EUR/USD", 1.10, 1.11, System.currentTimeMillis());
            Quote bondQuote = new Quote("US-Treasury", 99.50, 99.60, System.currentTimeMillis());
            // Simulate contention
            for (int i = 0; i < 10000; i++) {
                pricer.publishQuote(fxQuote, true); // Flood FX stream
                pricer.publishQuote(bondQuote, false); // Check for bond delay
                System.out.println("index " + i);
            }

            aeron.countersReader().forEach((id, typeId, keyBuffer, label) -> {

                // inspect or log values
                System.out.println(label);

            });

            pricer.close();
            pricer.running = false;
        }
    }
}


class Quote {
    private static final int QUOTE_TEMPLATE_ID = 0x807;
    private final String instrument;
    private final double bid;
    private final double ask;
    private final long timestamp;

    public static boolean isQuote(DirectBuffer buffer, int offset) {
        return QUOTE_TEMPLATE_ID == buffer.getInt(offset);
    }

    public double getBid() {
        return bid;
    }

    public double getAsk() {
        return ask;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Quote(String instrument, double bid, double ask, long timestamp) {
        this.instrument = instrument;
        this.bid = bid;
        this.ask = ask;
        this.timestamp = timestamp;
    }

    public String getInstrument() {
        return instrument;
    }

    public void encode(UnsafeBuffer buffer) {
        buffer.putInt(0, QUOTE_TEMPLATE_ID);
        buffer.putStringAscii(4, instrument);
        buffer.putDouble(104, bid);
        buffer.putDouble(112, ask);
        buffer.putLong(120, timestamp);
    }

    public static Quote decode(DirectBuffer buffer, int offset) {
        String instrument = buffer.getStringAscii(offset + 4);
        double bid = buffer.getDouble(offset + 104);
        double ask = buffer.getDouble(offset + 112);
        long timestamp = buffer.getLong(offset + 120);
        return new Quote(instrument, bid, ask, timestamp);
    }
}
