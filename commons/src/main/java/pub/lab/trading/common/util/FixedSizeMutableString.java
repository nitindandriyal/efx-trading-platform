package pub.lab.trading.common.util;

import java.util.stream.IntStream;

@SuppressWarnings("NullableProblems")
public class FixedSizeMutableString implements CharSequence {
    private final char[] fixed;
    private int length = 0;
    private FixedSizeMutableString subSequenceView;

    public FixedSizeMutableString(final CharSequence toInitWith) {
        fixed = new char[toInitWith.length()];
        copy(toInitWith);
    }

    public FixedSizeMutableString(final char[] toInitWith) {
        fixed = new char[toInitWith.length];
        copy(toInitWith);
    }

    public FixedSizeMutableString(int size) {
        fixed = new char[size];
    }

    private void copy(CharSequence toInitWith) {
        for (length = 0; length < toInitWith.length(); length++) {
            fixed[length] = toInitWith.charAt(length);
        }
    }

    private void copy(char[] toInitWith) {
        System.arraycopy(toInitWith, 0, fixed, 0, fixed.length);
    }

    public FixedSizeMutableString reset(final CharSequence toInitWith) {
        length = 0;
        return this;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        if (index < 0 || index > length) {
            throw new IllegalArgumentException("Fixed String Size {}" + fixed.length);
        }
        return fixed[index];
    }

    @Override
    public boolean isEmpty() {
        return length == 0;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (subSequenceView == null) {
            subSequenceView = new FixedSizeMutableString(fixed.length);
        }
        System.arraycopy(fixed, start, subSequenceView.fixed, 0, end - start);
        return subSequenceView;
    }

    @Override
    public IntStream chars() {
        return CharSequence.super.chars();
    }

    @Override
    public IntStream codePoints() {
        return CharSequence.super.codePoints();
    }
}
