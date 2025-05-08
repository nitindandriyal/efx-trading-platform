package pub.lab.trading.common.lifecycle;

import java.util.Arrays;
import java.util.function.Supplier;

public class ArrayObjectPool<E> {

    private final String name;
    private final Supplier<E> factory;
    private final float growthFactor;
    private Object[] array;
    private int pointer = 0;

    public ArrayObjectPool(String name, Supplier<E> factory) {
        this(name, 64, factory, 1.5f);
    }

    public ArrayObjectPool(String name, int initialCapacity, Supplier<E> factory, float growthFactor) {
        this.name = name;
        this.factory = factory;
        this.growthFactor = growthFactor;
        check(growthFactor);
        this.array = new Object[initialCapacity];
        for (int i = 0; i < array.length; i++) {
            this.array[i] = factory.get();
        }
    }

    private void check(float growthFactor) {
        if (growthFactor <= 1) {
            throw new IllegalArgumentException("Illegal growthFactor value=(" + growthFactor + "), should be bigger than one");
        }
    }

    int getArrayLength() {
        return this.array.length;
    }

    @SuppressWarnings("unchecked")
    private E arrayItem(int index) {
        return (E) array[index];
    }

    E getArrayItem(int index) {
        return arrayItem(index);
    }

    private int grow(boolean growRight) {

        int newLength = (int) (growthFactor * array.length); // casting faster than rounding

        if (newLength == array.length) newLength++;

        Object[] newArray = new Object[newLength];

        int offset = this.array.length;

        if (!growRight) {
            offset = newArray.length - this.array.length;
            System.arraycopy(this.array, 0, newArray, offset, this.array.length);
            Arrays.fill(this.array, null);
        }

        this.array = newArray;

        return offset;
    }

    public final E get() {
        if (pointer == array.length) {
            grow(true);
        }

        @SuppressWarnings("unchecked")
        E toReturn = (E) array[pointer];
        if (toReturn == null) {
            toReturn = factory.get();
        } else {
            this.array[pointer] = null;
        }
        pointer++;
        return toReturn;
    }

    public final void release(E object) {

        ensureNotNull(object);

        if (pointer == 0) {
            pointer = grow(false);
        }
        this.array[--pointer] = object;
    }

    private void ensureNotNull(E object) {
        if (object == null) throw new IllegalArgumentException("Cannot release null!");
    }

    public String getName() {
        return name;
    }
}
