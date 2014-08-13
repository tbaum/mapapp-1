package mapapp;

import javax.swing.*;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author tbaum
 * @since 05.08.2014
 */
public class SortedListModel<E> extends AbstractListModel<E> {
    private final Comparator<E> comp;
    private final List<E> delegate = new LinkedList<>();

    public SortedListModel(Comparator<E> comp) {
        this.comp = comp;
    }

    public int getSize() {
        return delegate.size();
    }

    public E getElementAt(int index) {
        return delegate.get(index);
    }

    public void addElement(E element) {
        int index = 0;
        while (index < delegate.size() && comp.compare(delegate.get(index), element) < 0) {
            index++;
        }
        delegate.add(index, element);
        fireIntervalAdded(this, index, delegate.size());
    }

    public E set(int index, E element) {
        E rv = delegate.set(index, element);
        fireContentsChanged(this, index, index);
        return rv;
    }

    public void add(int index, E element) {
        delegate.add(index, element);
        fireIntervalAdded(this, index, index);
    }

    public E remove(int index) {
        E rv = delegate.get(index);
        delegate.remove(index);
        fireIntervalRemoved(this, index, index);
        return rv;
    }

    public void clear() {
        int index1 = delegate.size() - 1;
        delegate.clear();
        if (index1 >= 0) {
            fireIntervalRemoved(this, 0, index1);
        }
    }
}
