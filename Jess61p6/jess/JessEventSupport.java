package jess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

/**
 * JessEvent listener broadcaster helper functions.
 */

class JessEventSupport implements Serializable {

    private java.util.List m_listeners = Collections.synchronizedList(new ArrayList());
    private int m_eventMask = 0;

    JessEventSupport(Rete source) {
        addJessListener(source);
    }

    public void addJessListener(JessListener jel) {
        m_listeners.add(jel);
    }

    public void removeJessListener(JessListener jel) {
        m_listeners.remove(jel);
    }


    public Iterator listJessListeners() {
        synchronized (m_listeners) {
            return new ArrayList(m_listeners).iterator();
        }
    }

    public synchronized int getEventMask() {
        return m_eventMask;
    }

    public synchronized void setEventMask(int i) {
        m_eventMask = i;
    }

    final void broadcastEvent(Object source, int type, Object data) throws JessException {

        // only broadcast active events
        if ((type & getEventMask()) == 0)
            return;

        // We lock this because it's cheaper than going in and out of
        // the vector methods over and over. We clone the vector so
        // that handlers added by an event handler won't be invoked
        // until the next time around.
        ArrayList snapshot;
        int size;

        synchronized (m_listeners) {
            if ((size = m_listeners.size()) == 0)
                return;

            snapshot = new ArrayList(m_listeners);
        }

            for (int i=0; i<size; i++) {
                try {
                    JessEvent theEvent = new JessEvent(source, type, data);
                    ((JessListener) snapshot.get(i)).eventHappened(theEvent);
                } catch (JessException je) {
                    throw je;
                } catch (Exception e) {
                    throw new JessException("JessEventSupport.broadcastEvent",
                                            "Event handler threw an exception",
                                            e);
                }
            }
    }
}
