package net.arnx.altocss.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class TreeWalker<T extends List<T>> implements Iterable<T> {
    private T root;

    public TreeWalker(T root) {
        this.root = Objects.requireNonNull(root);
    }

    @Override
    public Iterator<T> iterator() {
        return new TreeWalkerIterator();
    }

    private class TreeWalkerIterator implements Iterator<T> {
        private List<Iterator<T>> stack = new ArrayList<>();
        private Iterator<T> last;

        public TreeWalkerIterator() {
            stack.add(root.iterator());
        }

        @Override
        public boolean hasNext() {
            ListIterator<Iterator<T>> i = stack.listIterator(stack.size());
            while (i.hasPrevious()) {
                Iterator<T> current = i.previous();
                if (current.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public T next() {
            ListIterator<Iterator<T>> i = stack.listIterator(stack.size());
            while (i.hasPrevious()) {
                Iterator<T> current = i.previous();
                if (current.hasNext()) {
                    T next = current.next();
                    stack.add(next.iterator());
                    last = current;
                    return next;
                } else {
                    i.remove();
                }
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            if (last == null) {
                throw new IllegalArgumentException();
            }
            last.remove();
            last = null;
        }
    }
}
