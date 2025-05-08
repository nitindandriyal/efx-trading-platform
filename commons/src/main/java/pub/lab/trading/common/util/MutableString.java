package pub.lab.trading.common.util;

@SuppressWarnings("NullableProblems")
public class MutableString implements CharSequence {
    private final StringBuilder builder;

    public MutableString() {
        builder = new StringBuilder();
    }

    public MutableString(final CharSequence toInitWith) {
        builder = new StringBuilder(toInitWith);
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

    public MutableString reset() {
        builder.setLength(0);
        return this;
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
