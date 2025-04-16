package pub.lab.trading.model.pricing;

import org.agrona.AsciiSequenceView;
import org.agrona.DirectBuffer;
import play.lab.model.sbe.QuoteDecoder;

import java.util.function.DoubleConsumer;

public class QuoteView {
    private final QuoteDecoder decoder = new QuoteDecoder();

    private final AsciiSequenceView ccyPairView = new AsciiSequenceView();
    private final AsciiSequenceView tenorView = new AsciiSequenceView();
    private final AsciiSequenceView valueDateView = new AsciiSequenceView();
    private final AsciiSequenceView clientTierView = new AsciiSequenceView();

    private DirectBuffer buffer;
    private int bidsOffset;
    private int asksOffset;
    private int bidCount;
    private int askCount;

    public QuoteView wrap(DirectBuffer buffer, int offset) {
        this.buffer = buffer;

        // Decode the outer composite
        decoder.wrap(buffer, offset, buffer.capacity() - offset, 1);


        return this;
    }

    public void forEachBid(DoubleConsumer consumer) {
        for (int i = 0; i < bidCount; i++) {
            double bid = buffer.getDouble(bidsOffset + i * 8);
            consumer.accept(bid);
        }
    }

    public void forEachAsk(DoubleConsumer consumer) {
        for (int i = 0; i < askCount; i++) {
            double ask = buffer.getDouble(asksOffset + i * 8);
            consumer.accept(ask);
        }
    }

    public long priceCreationTimestamp() {
        return decoder.priceCreationTimestamp();
    }

}

