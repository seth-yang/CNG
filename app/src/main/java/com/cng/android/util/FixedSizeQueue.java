package com.cng.android.util;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by game on 2016/2/21
 */
public class FixedSizeQueue<E> extends ConcurrentLinkedQueue<E> {
    private int capacity = 16;

    public FixedSizeQueue () {
    }

    public FixedSizeQueue (int capacity) {
        this.capacity = capacity;
    }

    @Override
    public synchronized boolean add (E o) {
        while (size () >= capacity) {
            poll ();
        }
        return super.add (o);
    }

    @Override
    public boolean offer (E o) {
        return add (o);
    }

    @Override
    public synchronized boolean addAll (Collection<? extends E> c) {
        if (c == null || c.isEmpty ())
            return false;

        for (E e : c)
            add (e);
        return true;
/*
        int new_size = c.size ();
        if (new_size > capacity) {
            int delta = new_size - capacity;
//            Collection<E> tmp = new HashSet<> ();
            Iterator<? extends E> iterator = c.iterator ();
            for (int i = 0; i < delta; i ++)
                iterator.next ();

            while (iterator.hasNext ())
                add (iterator.next ());
            return true;
        } else if (new_size == capacity) {
            clear ();
            return super.addAll (c);
        } else if (new_size + size () > capacity) {
            while (new_size + size () >= capacity)
                poll ();
            return super.addAll (c);
        }
        return super.addAll (c);
*/
    }
}