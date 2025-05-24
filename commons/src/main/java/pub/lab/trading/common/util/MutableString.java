package pub.lab.trading.common.util;

import java.io.IOException;

@SuppressWarnings("NullableProblems")
public class MutableString implements CharSequence, Appendable {
    private final StringBuilder builder;

    public MutableString() {
        builder = new StringBuilder();
    }

    public MutableString(final CharSequence toInitWith) {
        builder = new StringBuilder(toInitWith);
    }

    public MutableString init() {
        builder.setLength(0);
        return this;
    }

    public MutableString init(final CharSequence toInitWith) {
        builder.setLength(0);
        builder.append(toInitWith);
        return this;
    }

    public MutableString append(final CharSequence toAppend) {
        builder.append(toAppend);
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        return builder.append(csq, start, end);
    }

    @Override
    public Appendable append(char c) throws IOException {
        return builder.append(c);
    }

    public MutableString reset() {
        builder.setLength(0);
        return this;
    }

    @Override
    public String toString() {
        return this.builder.toString();
    }

    @Override
    public int length() {
        return builder.length();
    }

    @Override
    public char charAt(int index) {
        return builder.charAt(builder.charAt(index));
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return builder.subSequence(start, end);
    }
}
