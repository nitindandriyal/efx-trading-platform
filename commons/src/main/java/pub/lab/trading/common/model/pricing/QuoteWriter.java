package pub.lab.trading.common.model.pricing;

import org.agrona.concurrent.UnsafeBuffer;
import play.lab.model.sbe.MessageHeaderEncoder;
import play.lab.model.sbe.QuoteMessageEncoder;

public class QuoteWriter {
    private static final int MAX_LEVELS = 10;
    private static final int BUFFER_CAPACITY = 256;

    private final UnsafeBuffer buffer;
    private final QuoteMessageEncoder quoteMessageEncoder;
    private final MessageHeaderEncoder headerEncoder;
    private QuoteMessageEncoder.RungEncoder rungEncoder;
    private int rungCounter = 0;

    public QuoteWriter() {
        this.buffer = new UnsafeBuffer(new byte[BUFFER_CAPACITY]);
        this.quoteMessageEncoder = new QuoteMessageEncoder();
        this.headerEncoder = new MessageHeaderEncoder();
    }

    public QuoteWriter beginQuote(String symbol, long valueDate, long timestamp, int totalRungCount) {
        // Clear buffer
        buffer.putBytes(0, new byte[BUFFER_CAPACITY]);

        // Initialize Quote
        quoteMessageEncoder.wrapAndApplyHeader(buffer, 0, headerEncoder);
        quoteMessageEncoder.symbol(symbol);
        quoteMessageEncoder.valueDate(valueDate);
        quoteMessageEncoder.priceCreationTimestamp(timestamp);

        rungEncoder = quoteMessageEncoder.rungCount(totalRungCount);
        rungCounter = 0;
        return this;
    }

    public QuoteWriter addRung(double bid, double ask, double volume) {
        if (rungCounter >= MAX_LEVELS) {
            throw new IllegalStateException(
                    "Rungs count (" + (rungCounter + 1) + ") exceeds maximum (" + MAX_LEVELS + ")"
            );
        }
        QuoteMessageEncoder.RungEncoder currentRung = rungEncoder.next();
        currentRung.bid(bid).ask(ask).volume(volume);
        rungCounter++;
        return this;
    }

    public int encodedLength() {
        return quoteMessageEncoder.encodedLength();
    }

    public void setPrices(double[] bids, double[] asks, double[] volumes, int levels) {
        QuoteMessageEncoder.RungEncoder levelsEncoder = quoteMessageEncoder.rungCount(levels).next();
        for (int i = 0; i < levels; i++) {
            levelsEncoder.bid(bids[i]).ask(asks[i]).volume(volumes[i]);
            if (i < levels - 1) {
                levelsEncoder.next();
            }
        }
    }
}
