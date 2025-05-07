package pub.lab.trading.common.model.pricing;

import org.agrona.MutableDirectBuffer;
import play.lab.model.sbe.QuoteEncoder;

public class QuoteWriter {
    private final QuoteEncoder encoder = new QuoteEncoder();

    public QuoteWriter wrap(MutableDirectBuffer buffer, int offset) {
        encoder.wrap(buffer, offset);
        return this;
    }

    public QuoteWriter setCcyPair(CharSequence value) {

        return this;
    }

    public QuoteWriter setTenor(int value) {
        encoder.tenor(value);
        return this;
    }

    public QuoteWriter setValueDate(long value) {
        encoder.valueDate(value);
        return this;
    }

    public QuoteWriter setClientTier(int value) {
        encoder.clientTier(value);
        return this;
    }

    public QuoteWriter setPriceCreationTimestamp(long timestamp) {
        encoder.priceCreationTimestamp(timestamp);
        return this;
    }

    public QuoteWriter setBids(double[] prices, int count) {

        return this;
    }

    public QuoteWriter setAsks(double[] prices, int count) {

        return this;
    }

    public int encodedLength() {
        return encoder.encodedLength();
    }
}
