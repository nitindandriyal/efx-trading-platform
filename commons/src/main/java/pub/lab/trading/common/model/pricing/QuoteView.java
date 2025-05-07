package pub.lab.trading.common.model.pricing;

import org.agrona.AsciiSequenceView;
import org.agrona.DirectBuffer;
import play.lab.model.sbe.QuoteDecoder;

import java.util.function.DoubleConsumer;

public class QuoteView {
    private final QuoteDecoder decoder = new QuoteDecoder();

    public QuoteView wrap(DirectBuffer buffer, int offset) {
        decoder.wrap(buffer, offset, buffer.capacity() - offset, 1);
        return this;
    }

    public QuoteDecoder.RungDecoder getRung() {
        return decoder.rung();
    }

    public long priceCreationTimestamp() {
        return decoder.priceCreationTimestamp();
    }

}

