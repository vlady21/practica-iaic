package jess;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A heap-based priority queue.
 *
 * See Sedgewick's "Algorithms in C++", Third Ed., page 386.  We don't
 * use element 0 in the array because that makes the implementation
 * cleaner.
 * <P>
 * (C) E.J. Friedman-Hill and 1997 Sandia National Laboratories
 * @version $Id: HeapPriorityQueue.java,v 1.2 2003/01/11 01:45:21 ejfried Exp $
 */

public class HeapPriorityQueue implements Serializable {
    private Activation [] m_queue;
    private Strategy m_strategy;
    private int m_size;

    public HeapPriorityQueue(Strategy s) {
        m_queue = new Activation[10];
        m_strategy = s;
    }

    private HeapPriorityQueue(HeapPriorityQueue hpq) {
        m_queue = (Activation[]) hpq.m_queue.clone();
        m_size = hpq.m_size;
        m_strategy = hpq.m_strategy;
    }

    public Strategy setStrategy(Strategy s) {
        Strategy temp = m_strategy;
        m_strategy = s;
        return temp;
    }

    public Strategy getStrategy() {
        return m_strategy;
    }

    public boolean isEmpty() {
        return m_size == 0;
    }

    public synchronized void remove(Activation c) {
        for (int i=1; i<=m_size; i++)
            if (m_queue[i].equals(c)) {
                m_queue[i] = m_queue[m_size];
                m_queue[m_size] = null;
                --m_size;
                fixDown(i);
                return;
            }
    }

    public synchronized void push(Activation c) {
        if (m_size == m_queue.length - 1) {
            Activation[] temp = new Activation[m_size*2];
            System.arraycopy(m_queue, 1, temp, 1, m_size);
            m_queue = temp;
        }
        m_queue[++m_size] = c;
        fixUp(m_size);
        notify();
    }

    public synchronized Activation pop() {
        if (isEmpty())
            return null;
        Activation c = m_queue[1];
        remove(c);
        return c;
    }

    public void clear() {
        m_queue = new Activation[10];
        m_size = 0;
    }

    public Iterator iterator() {
        HeapPriorityQueue hpq = new HeapPriorityQueue(this);
        ArrayList v = new ArrayList();
        while (!hpq.isEmpty())
            v.add(hpq.pop());
        return v.iterator();
    }


    // Push an element down to reform the heap.
    private void fixDown(int k) {
        Activation[] queue = m_queue;
        while (2*k <= m_size) {
            int j = 2*k;
            if (j < m_size && m_strategy.compare(queue[j], queue[j+1]) > 0)
                j++;
            if (! (m_strategy.compare(queue[k], queue[j]) > 0))
                break;
            exch(k, j);
            k = j;
        }
    }

    // Push an element up to reform the heap.
    private void fixUp(int k) {
        Activation[] queue = m_queue;
        while (k > 1 && m_strategy.compare(queue[k/2], queue[k]) > 0) {
            int j = k/2;
            exch(k, j);
            k = j;
        }
    }

    private void exch(int i, int j) {
        Activation[] queue = m_queue;
        Activation c = queue[i];
        queue[i] = queue[j];
        queue[j] = c;
    }

    /**
     * Don't write out inactive records -- they might contain
     * non-serializable objects.
     */

    private void writeObject(ObjectOutputStream stream) throws IOException {
        HeapPriorityQueue hpq = new HeapPriorityQueue(m_strategy);
        while (!isEmpty()) {
            Activation a = pop();
            if (!a.isInactive())
                hpq.push(a);
        }
        m_queue = hpq.m_queue;
        m_size = hpq.m_size;

        stream.defaultWriteObject();
    }
}

