package jess;

/** **********************************************************************
 * A test that always passes, and makes calls with calltype RIGHT.
 * Used to build nested NOTs
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */

class Node1LTR extends Node {
    void callNodeLeft(Token t, Context context) throws JessException {
        broadcastEvent(JessEvent.RETE_TOKEN + LEFT, t);
        passAlong(t, context);
    }

    void passAlong(Token t, Context context) throws JessException {
        Node[] sa = m_succ;
        for (int j = 0; j < m_nSucc; j++) {
            Node s = sa[j];
            // System.out.println(this + " " +  t);
            s.callNodeRight(t, context);
        }
    }

    public boolean equals(Object o) {
        return (o instanceof Node1LTR);
    }

    public String getCompilationTraceToken() {
        return "a";
    }

    public String toString() {
        return "[Right input adapter]";
    }

}

