package br.edu.ufersa.thingsfall.common;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class DynamicArray<T> implements Iterable<T> {
    private static final int DEFAULT_CAPACITY = 8;

    private Object[] elements;
    private int size;

    public DynamicArray() {
        elements = new Object[DEFAULT_CAPACITY];
    }

    @SafeVarargs
    public DynamicArray(T... values) {
        elements = new Object[Math.max(DEFAULT_CAPACITY, values.length)];
        for (T value : values) {
            add(value);
        }
    }

    @SafeVarargs
    public static <T> DynamicArray<T> of(T... values) {
        return new DynamicArray<>(values);
    }

    public static <T> DynamicArray<T> copyOf(Iterable<? extends T> values) {
        DynamicArray<T> copy = new DynamicArray<>();
        copy.addAll(values);
        return copy;
    }

    public void add(T value) {
        ensureCapacity(size + 1);
        elements[size++] = value;
    }

    public void addAll(Iterable<? extends T> values) {
        for (T value : values) {
            add(value);
        }
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        checkIndex(index);
        return (T) elements[index];
    }

    public void set(int index, T value) {
        checkIndex(index);
        elements[index] = value;
    }

    public T getFirst() {
        return isEmpty() ? null : get(0);
    }

    public T getLast() {
        return isEmpty() ? null : get(size - 1);
    }

    public int indexOf(T value) {
        for (int i = 0; i < size; i++) {
            if (equal(elements[i], value)) {
                return i;
            }
        }
        return -1;
    }

    public boolean contains(T value) {
        return indexOf(value) >= 0;
    }

    public T remove(int index) {
        checkIndex(index);
        T removed = get(index);
        int elementsToMove = size - index - 1;
        if (elementsToMove > 0) {
            System.arraycopy(elements, index + 1, elements, index, elementsToMove);
        }
        elements[--size] = null;
        return removed;
    }

    public boolean remove(T value) {
        int index = indexOf(value);
        if (index < 0) {
            return false;
        }
        remove(index);
        return true;
    }

    public int removeIf(Predicate<? super T> condition) {
        int removedCount = 0;
        for (int i = 0; i < size;) {
            if (condition.test(get(i))) {
                remove(i);
                removedCount++;
            } else {
                i++;
            }
        }
        return removedCount;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }
        size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    public T[] toArray(T[] destination) {
        if (destination.length < size) {
            throw new IllegalArgumentException("Destination array is smaller than the dynamic array");
        }
        for (int i = 0; i < size; i++) {
            destination[i] = (T) elements[i];
        }
        if (destination.length > size) {
            destination[size] = null;
        }
        return destination;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int index;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return get(index++);
            }
        };
    }

    private void ensureCapacity(int requiredCapacity) {
        if (requiredCapacity <= elements.length) {
            return;
        }
        int newCapacity = Math.max(requiredCapacity, elements.length * 2);
        Object[] expanded = new Object[newCapacity];
        System.arraycopy(elements, 0, expanded, 0, size);
        elements = expanded;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }

    private boolean equal(Object first, Object second) {
        return first == second || (first != null && first.equals(second));
    }
}
