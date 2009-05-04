package jess;

import java.io.Serializable;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * A defmodule
 * <P>
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */


class Defmodule implements Serializable {
    public static final String MAIN = "MAIN";
    private HeapPriorityQueue m_queue;
    private TreeMap m_deftemplates;
    private String m_name;
    private String m_comment;

    Defmodule(String name, String comment, Strategy s) {
        m_name = name;
        m_comment = comment;
        m_queue = new HeapPriorityQueue(s);
        m_deftemplates = new TreeMap();
    }

    HeapPriorityQueue getQueue() {
        return m_queue;
    }

    String getName() {
        return m_name;
    }

    String getDocstring() {
        return m_comment;
    }

    void reset() {
        m_queue.clear();
    }

    Iterator listDeftemplates() {
        return m_deftemplates.values().iterator();
    }
    
    Deftemplate getDeftemplate(String name) {
        int index = name.indexOf("::");
        if (index != -1) {
            name = name.substring(index+2);
        }
        return (Deftemplate) m_deftemplates.get(name);
    }

    Deftemplate addDeftemplate(Deftemplate dt, Rete engine)
        throws JessException {
        synchronized (m_deftemplates) {
            if (!dt.getModule().equals(m_name))
                throw new JessException("Defmodule.addDeftemplate",
                                        "Wrong module name",
                                        dt.getModule());
            String name = dt.getBaseName();
            Deftemplate existing = (Deftemplate) m_deftemplates.get(name);
            if (existing == null || existing.equals(dt)) {
                engine.broadcastEvent(JessEvent.DEFTEMPLATE, dt);
                m_deftemplates.put(name, dt);
            } else {
                throw new JessException("Defmodule.addDeftemplate",
                                        "Cannot redefine deftemplate",
                                        dt.getName());
            }
            return dt;
        }
    }

}
