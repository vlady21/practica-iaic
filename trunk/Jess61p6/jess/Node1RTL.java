package jess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** **********************************************************************
 * A test that always passes, but makes calls with calltype LEFT instead of RIGHT.
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */

class Node1RTL extends Node1 implements LogicalNode {

    /**
     * Tokens that give logical support to facts
     */

    private HashMap m_logicalDepends;
    private int m_tokenSize;

    void callNodeRight(Token t, Context context) throws JessException {
        if (m_logicalDepends != null && t.m_tag != RU.UPDATE)
            removeLogicalSupportFrom(t, context);

        passAlong(t, context);
        return;
    }

    void passAlong(Token t, Context context) throws JessException {
        m_tokenSize = t.size();
        Node[] sa = m_succ;
        for (int j = 0; j < m_nSucc; j++) {
            Node s = sa[j];
            // System.out.println(this + " " +  t);
            s.callNodeLeft(t, context);
        }
    }

    public boolean equals(Object o) {
        return (o instanceof Node1RTL);
    }

    public String toString() {
        return "[Left input adapter]";
    }

    public void dependsOn(Fact f, Token t) {
        if (m_logicalDepends == null)
            m_logicalDepends = new HashMap();

        ArrayList list = (ArrayList) m_logicalDepends.get(t);
        if (list == null) {
            list = new ArrayList();
            m_logicalDepends.put(t, list);
        }

        synchronized (list) {
            list.add(f);
        }
    }

    // For testing only
    public Map getLogicalDependencies() {
        return m_logicalDepends;
    }

    public int getTokenSize() {
        return m_tokenSize;
    }

    protected void removeLogicalSupportFrom(Token token, Context context) {
        ArrayList list = (ArrayList) m_logicalDepends.remove(token);
        if (list != null) {
            Rete engine = context.getEngine();
            for (int i = 0; i < list.size(); ++i) {
                Fact f = (Fact) list.get(i);
                engine.removeLogicalSupportFrom(token, f);
            }
        }
    }
}

