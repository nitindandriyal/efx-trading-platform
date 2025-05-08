package pub.lab.trading.common.model.pricing;

import org.agrona.DirectBuffer;
import play.lab.model.sbe.QuoteMessageDecoder;

public class QuoteView {
    private final QuoteMessageDecoder decoder = new QuoteMessageDecoder();

    public QuoteView wrap(DirectBuffer buffer, int offset) {
        decoder.wrap(buffer, offset, buffer.capacity() - offset, 1);
        return this;
    }

    public QuoteMessageDecoder.RungDecoder getRung() {
        return decoder.rung();
    }

    public long priceCreationTimestamp() {
        return decoder.priceCreationTimestamp();
    }

}

