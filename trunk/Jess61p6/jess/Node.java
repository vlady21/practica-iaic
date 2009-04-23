/** **********************************************************************
 * Parent class of all nodes of the pattern network
 *
 * $Id: Node.java,v 1.3 2003/01/16 15:57:50 ejfried Exp $
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 ********************************************************************** */

package jess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */
public abstract class Node implements Serializable {

    public final static int LEFT = 0;
    public final static int RIGHT = 1;

    /**
     How many rules use me?
     */

    public int m_usecount = 0;


    Node[] m_succ;
    int m_nSucc;

    /**
     * Constructor
     */

    public Enumeration getSuccessors() {
        return new NodeEnumeration();
    }

    Node resolve(Node n) {
        for (int i = 0; i < m_nSucc; i++) {
            if (m_succ[i].equals(n))
                return m_succ[i];
        }
        return n;
    }

    void addSuccessor(Node n, NodeSink r)
            throws JessException {
        if (m_succ == null || m_nSucc == m_succ.length) {
            Node[] temp = m_succ;
            m_succ = new Node[m_nSucc + 5];
            if (temp != null)
                System.arraycopy(temp, 0, m_succ, 0, m_nSucc);
        }
        m_succ[m_nSucc++] = n;
        r.addNode(n);
    }

    Node mergeSuccessor(Node n, NodeSink r)
            throws JessException {
        for (int i = 0; i < m_nSucc; i++) {
            Node test = m_succ[i];
            if (n.equals(test)) {
                r.addNode(test);
                return test;
            }
        }
        // No match found
        addSuccessor(n, r);
        return n;
    }


    /**
     * Some Nodes compare equal using equals(), but aren't the same
     * physical node. We only want to remove the ones that are
     * physically equal, not just conceptually.  */

    void removeSuccessor(Node s) {
        for (int i = 0; i < m_nSucc; i++)
            if (s == m_succ[i]) {
                System.arraycopy(m_succ, i + 1, m_succ, i, (--m_nSucc) - i);
                return;
            }
    }

    /**
     * Do the business of this node.
     */

    void callNodeLeft(Token token, Context context) throws JessException {
        throw new JessException("callNodeLeft", "Undefined in class", getClass().getName());
    }

    void callNodeRight(Token t, Context context) throws JessException {
        throw new JessException("callNodeRight", "Undefined in class", getClass().getName());
    }

    private transient Hashtable m_listeners;

    public void addJessListener(JessListener jel) {
        if (m_listeners == null)
            m_listeners = new Hashtable();

        m_listeners.put(jel, jel);
    }

    /**
     * @param jel
     */
    public void removeJessListener(JessListener jel) {
        if (m_listeners == null)
            return;

        m_listeners.remove(jel);
        if (m_listeners.size() == 0)
            m_listeners = null;
    }

    void broadcastEvent(int type, Object data) throws JessException {
        if (m_listeners != null && m_listeners.size() != 0) {
            Enumeration e = m_listeners.elements();
            JessEvent event = new JessEvent(this, type, data);
            while (e.hasMoreElements()) {
                ((JessListener) e.nextElement()).eventHappened(event);
            }
        }
    }

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }

    private class NodeEnumeration implements Enumeration {
        private int m_index = 0;

        public boolean hasMoreElements() {
            return m_index < m_nSucc;
        }

        public Object nextElement() {
            if (!hasMoreElements())
                throw new RuntimeException("No more elements!");
            return m_succ[m_index++];
        }
    }

    abstract String getCompilationTraceToken();
}






