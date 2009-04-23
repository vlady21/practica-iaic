package jess;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

/** **********************************************************************
 * Agenda management.
 * <P>
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 */

class Agenda implements Serializable {

    private Object m_activationSemaphore = new String("ACTIVATION LOCK");
    private boolean m_halt = false;
    private int m_evalSalience = Rete.INSTALL;
    private HashMap m_modules = new HashMap();
    private String m_thisModule;
    private Strategy m_strategy = new depth();
    private Stack m_focusStack = new Stack();
    private Activation m_thisActivation;

    Agenda() {
        try {
            addDefmodule(Defmodule.MAIN);
        } catch (JessException je) {
            throw new RuntimeException("Can't define module MAIN");
        }
    }

    void setEvalSalience(int method) throws JessException {
        if (method < Rete.INSTALL || method > Rete.EVERY_TIME)
            throw new JessException("Agenda.setEvalSalience",
                                    "Invalid value", method);
        m_evalSalience = method;
    }

    int getEvalSalience() {
        return m_evalSalience;
    }

    void reset(Rete engine) throws JessException {
        for (Iterator modules = m_modules.values().iterator();
             modules.hasNext();) {
            Defmodule module = (Defmodule) modules.next();
            module.reset();
        }
        m_focusStack.clear();
        m_focusStack.push(Defmodule.MAIN);
        engine.broadcastEvent(JessEvent.FOCUS, Defmodule.MAIN);
    }

    void clear() throws JessException {
        m_modules.clear();
        m_strategy = new depth();
        addDefmodule(Defmodule.MAIN);
        m_focusStack.clear();
    }

    Object getActivationSemaphore() {
        return m_activationSemaphore;
    }

    HeapPriorityQueue getQueue() {
        return ((Defmodule) m_modules.get(m_thisModule)).getQueue();
    }

    HeapPriorityQueue getQueue(Object module) throws JessException {
        verifyModule(module);
        return ((Defmodule) m_modules.get(module)).getQueue();
    }

    Activation getNextActivation(Rete engine) throws JessException {
        synchronized (m_activationSemaphore) {
            if (m_focusStack.empty())
                return getQueue(Defmodule.MAIN).pop();
            
            while (!m_focusStack.empty()) {
                HeapPriorityQueue q = getQueue(m_focusStack.peek());
                Activation a = q.pop();
                if (a != null)
                    return a;
                else {
                    Object oldFocus = m_focusStack.pop();
                    engine.broadcastEvent(JessEvent.FOCUS | JessEvent.REMOVED,
                                          oldFocus);
                    if (!getFocus().equals(oldFocus))
                        engine.broadcastEvent(JessEvent.FOCUS, getFocus());
                }
            }
            return getQueue(Defmodule.MAIN).pop();
        }
    }

    Iterator listActivations() {
        return getQueue().iterator();
    }

    Iterator listActivations(String moduleName) throws JessException {
        return getQueue(moduleName).iterator();
    }

    Iterator listModules() {
        return m_modules.keySet().iterator();
    }

    void addActivation(Activation a, Rete engine) throws JessException {

        synchronized (m_activationSemaphore) {
            if (m_evalSalience != Rete.INSTALL)
                a.evalSalience(engine);
            
            getQueue(a.getModule()).push(a);
            
            if (a.getAutoFocus()) {
                setFocus(a.getModule(), engine);
            }
            
            m_activationSemaphore.notifyAll();
        }
    }

    void waitForActivations() {
        try {
            synchronized (m_activationSemaphore) {
                if (getQueue().isEmpty())
                    m_activationSemaphore.wait();
            }
        }
        catch (InterruptedException ie) { /* FALL THROUGH */ }
    }

    Strategy getStrategy() {
        return m_strategy;
    }

    String setStrategy(Strategy s, Rete r) throws JessException {
        String rv = null;
        for (Iterator modules = m_modules.values().iterator();
             modules.hasNext();) {

            HeapPriorityQueue queue = ((Defmodule) modules.next()).getQueue();
            synchronized (queue) {
                Iterator e = queue.iterator();
                queue.clear();
                rv = queue.setStrategy(s).getName();
                while (e.hasNext()) {
                    Activation a = (Activation) e.next();
                    if (!a.isInactive())
                        r.addActivation(a);
                }
            }
        }
        m_strategy = s;
        return rv;
    }

    void halt() {
        m_halt = true;
        synchronized (m_activationSemaphore) {
            m_activationSemaphore.notifyAll();
        }
    }

    int runUntilHalt(Rete r) throws JessException {
        int count = 0;
        do {
            count += run(r);
            if (m_halt)
                break;
            waitForActivations();
        } while (!m_halt);

        return count;
    }

    synchronized int run(Rete r) throws JessException {
        int i=0, j;
        // ###
        do {
            j = run(Integer.MAX_VALUE, r);
            i += j;
        } while (j > 0 && !m_halt);
        return i;
    }

    synchronized int run(int max, Rete r) throws JessException {
        int n = 0;
        m_halt = false;
        Activation a;
        // ###

        while (!m_halt && n < max && (a = getNextActivation(r)) != null) {

            if (!a.isInactive()) {
                a.setSequenceNumber(++n);
                r.broadcastEvent(JessEvent.DEFRULE_FIRED, a);
                try {
                    r.aboutToFire(a);
                    m_thisActivation = a;
                    a.fire(r);
                } finally {
                    m_thisActivation = null;
                    r.justFired(a);
                }
            }

            if (m_evalSalience == Rete.EVERY_TIME) {
                setStrategy(getQueue().getStrategy(), r);
            }
        }
        return n;
    }

    String getCurrentModule() {
        return m_thisModule;
    }

    Defmodule getModule(String name) throws JessException {
        verifyModule(name);
        return (Defmodule) m_modules.get(name);
    }

    Iterator listFocusStack() {
        return m_focusStack.iterator();
    }

    void clearFocusStack() {
        m_focusStack.clear();
    }

    String getFocus() {
        if (m_focusStack.empty())
            return Defmodule.MAIN;
        else
            return (String) m_focusStack.peek();
    }

    String popFocus(Rete engine, String expect) throws JessException {
        if (m_focusStack.empty())
            return Defmodule.MAIN;
        else if (expect != null && !expect.equals(getFocus()))
            return expect;
        else {
            String oldFocus = (String) m_focusStack.pop();

            engine.broadcastEvent(JessEvent.FOCUS | JessEvent.REMOVED,
                                  oldFocus);
            engine.broadcastEvent(JessEvent.FOCUS, getFocus());
            return oldFocus;
        }
    }

    void setFocus(String name, Rete engine) throws JessException {
        if (getFocus().equals(name))
            return;
        verifyModule(name);
        engine.broadcastEvent(JessEvent.FOCUS | JessEvent.REMOVED, getFocus());
        engine.broadcastEvent(JessEvent.FOCUS, name);
        m_focusStack.push(name);
    }

    String  setCurrentModule(String moduleName) throws JessException {
        String old = m_thisModule;
        verifyModule(moduleName);
        m_thisModule = moduleName;
        return old;
    }

    void addDefmodule(String moduleName) throws JessException {
        addDefmodule(moduleName, null);
    }

    void addDefmodule(String moduleName, String comment) throws JessException {
        if (m_modules.get(moduleName) != null)
            throw new JessException("Agenda.addDefmodule",
                                    "Attempt to redefine defmodule",
                                    moduleName);

        m_thisModule = moduleName;
        m_modules.put(moduleName,
                      new Defmodule(moduleName, comment, m_strategy));
    }

    void verifyModule(Object moduleName) throws JessException {
        if (m_modules.get(moduleName) == null)
            throw new JessException("Agenda.verifyModule",
                                    "Undefined module",
                                    moduleName.toString());
    }

    String resolveName(String name) {
        if (name.indexOf("::") == -1)
            name = RU.scopeName(getCurrentModule(), name);
        return name;
    }

    Activation getThisActivation() {
        return m_thisActivation;
    }


}
