/*
 * Copyright 2015-2024 (c) CoralBlocks LLC - http://www.coralblocks.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package pub.lab.trading.common.lifecycle;

import java.util.Arrays;

public class ArrayObjectPool<E> {

    private Object[] array;
    private int pointer = 0;
    private final ObjectFactory<E> factory;
    private final float growthFactor;

    public ArrayObjectPool(int initialCapacity, ObjectFactory<E> factory) {
        this(initialCapacity, factory, 1.5f);
    }

    public ArrayObjectPool(int initialCapacity, ObjectFactory<E> factory, float growthFactor) {
        this.factory = factory;
        this.growthFactor = growthFactor;
        check(growthFactor);
        this.array = new Object[initialCapacity];
        for (int i = 0; i < array.length; i++) {
            this.array[i] = factory.create();
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
            toReturn = factory.create();
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
}
