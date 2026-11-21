package br.edu.ufersa.thingsfall.common;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class LinkedList<T> implements Iterable<T> {
    public class Node {
        T data;
        Node next;

        public Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node head;
    private Node tail;
    private int size;

    public LinkedList() {
        head = null;
        tail = null;
        size = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedIterator(this.head);
    }

    private class LinkedIterator implements Iterator<T> {
        private Node next;
        private int nextIndex;

        LinkedIterator(Node head) {
            next = head;
            nextIndex = 0;
        }

        public boolean hasNext() {
            return nextIndex < size;
        }

        public T next() {
            if (!hasNext())
                throw new NoSuchElementException();

            Node lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.data;
        }
    }

    public void addFirst(T data) {
        Node item = new Node(data);

        if (head == null) {
            head = item;
            tail = item;
        } else {
            item.next = head;
            head = item;
        }
        size++;
    }

    public void addLast(T data) {
        Node item = new Node(data);

        if (head == null) {
            head = item;
        } else {
            tail.next = item;
        }
        tail = item;

        size++;
    }

    public void addAll(Iterable<? extends T> list) {
        for (T el : list) {
            this.addLast(el);
        }
    }

    public T getFirst() {
        if (head == null) {
            return null;
        } else {
            return head.data;
        }
    }

    public T getLast() {
        if (tail == null) {
            return null;
        } else {
            return tail.data;
        }
    }

    private Node searchNode(Predicate<? super T> condition) {
        Node p = head;

        while (p != null) {
            if (condition.test(p.data)) {
                return p;
            }
            p = p.next;
        }

        return null;
    }

    private Node searchNode(T item) {
        Node p = head;
        while (p != null) {
            if (p.data.equals(item)) {
                return p;
            }
            p = p.next;
        }
        return null;
    }

    public T search(Predicate<? super T> condition) {
        Node p = searchNode(condition);
        if (p == null) {
            return null;
        } else {
            return p.data;
        }
    }

    public T removeFirst() {
        Node p = head;
        T data = null;

        if (head == null) {
            return null;
        } else {
            data = head.data;

            if (head == tail) {
                head = null;
                tail = null;
            } else {
                head = head.next;
            }
            p.next = null;
            size--;
        }

        return data;
    }

    public T removeLast() {
        T data = null;

        if (tail == null) {
            return null;
        } else {
            data = tail.data;
            if (head == tail) {
                head = null;
                tail = null;
            } else {
                Node p = head;
                while (p.next != tail) {
                    p = p.next;
                }

                tail = p;
                tail.next = null;
            }
            size--;
        }
        return data;
    }

    public boolean remove(T item) {
        return this.removeFirstMatching((T x) -> x.equals(item)) != null;
    }

    private Node searchBefore(Predicate<? super T> condition) {
        Node p = head;
        Node before = null;

        while (p != null) {
            before = p;
            p = p.next;

            if (p != null && condition.test(p.data)) {
                return before;
            }
        }

        return null;
    }

    private T removeFirstMatching(Predicate<? super T> condition) {
        Node before = null;
        Node removed = null;

        if (head == null) {
            return null;
        }
        if (condition.test(head.data)) {
            return removeFirst();
        }
        before = searchBefore(condition);
        if (before == null) {
            return null;
        } else {
            removed = before.next;
            if (removed == tail) {
                return removeLast();
            } else {
                before.next = removed.next;
                removed.next = null;
                size--;
                return removed.data;
            }
        }
    }

    public boolean removeIf(Predicate<? super T> condition) {
        boolean removed = false;
        Node previous = null;
        Node current = head;

        while (current != null) {
            Node next = current.next;
            if (condition.test(current.data)) {
                if (previous == null) {
                    head = next;
                } else {
                    previous.next = next;
                }
                if (current == tail) {
                    tail = previous;
                }
                current.next = null;
                size--;
                removed = true;
            } else {
                previous = current;
            }
            current = next;
        }
        return removed;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return size;
    }

}
