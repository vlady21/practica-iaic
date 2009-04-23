package jess;

import java.beans.*;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/** **********************************************************************
 * The current list of definstances.
 * <P>
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */

class DefinstanceList implements Serializable, PropertyChangeListener {

    // Keys are Jess defclass names, elements are Java class names
    private HashMap m_javaClasses = new HashMap(101);
    // Keys are objects to match, elements are the facts that represent them.
    private HashMap m_definstances = new HashMap(101);
    // Keys are objects to match, elements are the Jess class names
    private HashMap m_jessClasses = new HashMap(101);

    private transient Rete m_rete;

    DefinstanceList(Rete engine) {
        // ###
        setEngine(engine);
    }

    void setEngine(Rete engine) {
        m_rete = engine;
    }

    synchronized void clear() {
        for (Iterator it = m_definstances.keySet().iterator(); it.hasNext();)
            removePropertyChangeListener(it.next());

        m_javaClasses.clear();
        m_definstances.clear();
        m_jessClasses.clear();
    }

    synchronized void reset() throws JessException {
        for (Iterator e = m_definstances.keySet().iterator(); e.hasNext();)
            updateMultipleSlots(e.next(), true, m_rete.getGlobalContext());
    }

    synchronized Iterator listDefinstances() {
        // We build an ArrayList in case the client does something
        // destructive with this iterator (like call undefinstance)
        return new ArrayList(m_definstances.keySet()).iterator();
    }

    synchronized Iterator listDefclasses() {
        return new ArrayList(m_javaClasses.keySet()).iterator();
    }

    String jessNameToJavaName(String s) {
        return (String) m_javaClasses.get(s);
    }

    void mapDefclassName(String javaName, String className) {
        m_javaClasses.put(javaName, className);
    }

    private Fact updateShadowFact(Object o,
                                  String changedName,
                                  Object newValue,
                                  boolean reset,
                                  Context context) throws JessException {
        if (changedName == null || newValue == null)
            return updateMultipleSlots(o, reset, context);
        else
            return updateSingleSlot(o, changedName, newValue, context);
    }


    private synchronized Fact updateMultipleSlots(Object o,
                                                  boolean resetFactId,
                                                  Context context)
            throws JessException {

        Fact fact = (Fact) m_definstances.get(o);
        if (fact == null)
            throw new JessException("DefinstanceList.updateShadowFact",
                    "Object not a definstance: ", o.toString());

        Rete engine = context.getEngine();
        FactList wm = engine.getFactList();
        synchronized (engine.getCompiler()) {
            synchronized (engine.getActivationSemaphore()) {
                try {
                    wm.prepareToModify(fact, engine);

                    Deftemplate deft = fact.getDeftemplate();
                    Object[] args = new Object[]{};

                    for (int i = 0; i < deft.getNSlots(); i++) {
                        if (deft.getSlotName(i).equals("OBJECT"))
                            continue;
                        SerializablePD pd = (SerializablePD)
                            deft.getSlotDefault(i).externalAddressValue(context);
                        String name = pd.getName();

                        Method m = pd.getReadMethod(engine);
                        Class rt = m.getReturnType();

                        Object newPropertyValue = m.invoke(o, args);

                        Value newSlotValue =
                            ReflectFunctions.objectToValue(rt, newPropertyValue);
                        Value oldSlotValue = fact.getSlotValue(name);
                        if (!newSlotValue.equals(oldSlotValue))
                            fact.setSlotValue(name, newSlotValue);
                    }

                } catch (InvocationTargetException ite) {
                    throw new JessException("DefinstanceList.updateShadowFact",
                                            "Called method threw an exception",
                                            ite.getTargetException());

                } catch (IllegalAccessException iae) {
                    throw new JessException("DefinstanceList.updateShadowFact",
                                            "Method is not accessible",
                                            iae);
                } catch (IllegalArgumentException iae) {
                    throw new JessException("DefinstanceList.updateShadowFact",
                                            "Invalid argument", iae);
                } finally {
                    if (!resetFactId)
                        fact = wm.finishModify(fact, engine, context);
                    else
                        fact = m_rete.assertFact(fact, context);
                }
            }
        }
        return fact;
    }

    private synchronized Fact updateSingleSlot(Object o,
                                               String slotName,
                                               Object newValue,
                                               Context context)
            throws JessException {

        Fact fact = (Fact) m_definstances.get(o);
        if (fact == null)
            throw new JessException("DefinstanceList.updateShadowFact",
                    "Object not a definstance: ", o.toString());

        Rete engine = context.getEngine();
        FactList wm = engine.getFactList();
        synchronized (engine.getCompiler()) {
            synchronized (engine.getActivationSemaphore()) {
                try {
                    wm.prepareToModify(fact, engine);

                    Deftemplate deft = fact.getDeftemplate();
                    int index = deft.getSlotIndex(slotName);
                    if (index == -1)
                        throw new JessException("DeftemplateList.updateSingleSlot",
                                "No such slot " + slotName + " in template",
                                deft.getName());
                    SerializablePD pd = (SerializablePD)
                        deft.getSlotDefault(index).externalAddressValue(context);
                    Method m = pd.getReadMethod(engine);
                    Class rt = m.getReturnType();

                    Value newV = ReflectFunctions.objectToValue(rt, newValue);
                    fact.setSlotValue(slotName, newV);

                } catch (IllegalArgumentException iae) {
                    throw new JessException("DefinstanceList.updateShadowFact",
                                            "Invalid argument", iae);
                } finally {
                    fact = wm.finishModify(fact, engine, context);
                }
            }
        }
        return fact;
    }

    private synchronized Fact createNewShadowFact(Object o,
                                                  Context context,
                                                  int shadowMode)
            throws JessException {

        Fact fact = null;
        Rete engine = context.getEngine();
        try {

            fact = new Fact((String) m_jessClasses.get(o), m_rete);
            fact.setSlotValue("OBJECT", new Value(o));
            fact.setShadowMode(shadowMode);
            m_definstances.put(o, fact);

            Deftemplate deft = fact.getDeftemplate();
            Object[] args = new Object[]{};

            for (int i = 0; i < deft.getNSlots(); i++) {
                if (deft.getSlotName(i).equals("OBJECT"))
                    continue;

                SerializablePD pd = (SerializablePD)
                        deft.getSlotDefault(i).externalAddressValue(context);
                String name = pd.getName();

                Method m = pd.getReadMethod(engine);
                Class rt = m.getReturnType();

                Object newValue = m.invoke(o, args);

                Value newV = ReflectFunctions.objectToValue(rt, newValue);
                fact.setSlotValue(name, newV);
            }

        } catch (InvocationTargetException ite) {
            throw new JessException("DefinstanceList.createNewShadowFact",
                    "Called method threw an exception",
                    ite.getTargetException());

        } catch (IllegalAccessException iae) {
            throw new JessException("DefinstanceList.createNewShadowFact",
                    "Method is not accessible",
                    iae);
        } catch (IllegalArgumentException iae) {
            throw new JessException("DefinstanceList.createNewShadowFact",
                    "Invalid argument", iae);
        }
        return m_rete.assertFact(fact, context);
    }

    synchronized Value updateObject(Object object) throws JessException {
        Fact fact = updateMultipleSlots(object, false, m_rete.getGlobalContext());
        return new FactIDValue(fact);
    }

    synchronized Value definstance(String jessTypename, Object object,
                                   boolean dynamic, Context context)
            throws JessException {
        try {
            String javaTypename = jessNameToJavaName(jessTypename);

            if (javaTypename == null)
                throw new JessException("DefinstanceList.definstance",
                        "Unknown object class",
                        jessTypename);

            // Make sure we're not already matching on this object
            if (m_definstances.get(object) != null)
                return new FactIDValue(null);


            if (!context.getEngine().findClass(javaTypename).
                    isAssignableFrom(object.getClass()))
                throw new JessException("DefinstanceList.definstance",
                        "Object is not instance of",
                        javaTypename);

            if (dynamic) {
                // Add ourselves to the object as a PropertyChangeListener

                Class pcl = context.getEngine().findClass("java.beans.PropertyChangeListener");
                Method apcl =
                        object.getClass().getMethod("addPropertyChangeListener",
                                new Class[]{pcl});
                apcl.invoke(object, new Object[]{this});
            }

            m_jessClasses.put(object, jessTypename);
            int shadowMode = dynamic ? Fact.DYNAMIC : Fact.STATIC;
            Fact fact = createNewShadowFact(object, context, shadowMode);

            return new FactIDValue(fact);

        } catch (InvocationTargetException ite) {
            throw new JessException("DefinstanceList.definstance",
                    "Cannot add PropertyChangeListener",
                    ite.getTargetException());

        } catch (NoSuchMethodException nsm) {
            throw new JessException("DefinstanceList.definstance",
                    "Obj doesn't accept " +
                    "PropertyChangeListeners",
                    nsm);
        } catch (ClassNotFoundException cnfe) {
            throw new JessException("DefinstanceList.definstance",
                    "Class not found", cnfe);
        } catch (IllegalAccessException iae) {
            throw new JessException("DefinstanceList.definstance",
                    "Class or method is not accessible",
                    iae);
        }
    }

    synchronized void undefinstanceNoRetract(Object o) throws JessException {
        m_definstances.remove(o);
        removePropertyChangeListener(o);
        m_jessClasses.remove(o);
    }

    synchronized Fact undefinstance(Object o) throws JessException {
        Fact f = (Fact) m_definstances.get(o);
        if (f != null)
            f = m_rete.retractNoUndefinstance(f);
        undefinstanceNoRetract(o);
        return f;
    }

    private void removePropertyChangeListener(Object o) {
        try {
            Method apcl =
                    o.getClass().getMethod("removePropertyChangeListener",
                            new Class[]{
                                PropertyChangeListener.class
                            });
            apcl.invoke(o, new Object[]{this});

        } catch (Exception e) { /* whatever */
        }
    }

    public synchronized void propertyChange(PropertyChangeEvent pce) {
        Object o = pce.getSource();

        try {
            String s = (String) m_jessClasses.get(o);
            // The facts are stored in the "definstances" map. Since they're
            // there already, the value of the last argument doesn't matter.
            if (s != null)
                updateShadowFact(o, pce.getPropertyName(),
                        pce.getNewValue(), false,
                        m_rete.getGlobalContext()
                );

        } catch (JessException re) {
            System.out.println("Async Error: " + re);
            if (re.getCause() != null)
                re.getCause().printStackTrace();
        }
    }

    synchronized Value defclass(String jessName, String clazz, String parent)
            throws JessException {

        try {
            Deftemplate dt;
            if (parent != null) {
                dt = m_rete.findDeftemplate(m_rete.resolveName(parent));
                if (dt == null)
                    throw new JessException("defclass",
                            "No such parent template: ",
                            parent);
                dt = new Deftemplate(jessName, "$JAVA-OBJECT$ " + clazz,
                        dt, m_rete);
            } else
                dt = new Deftemplate(jessName, "$JAVA-OBJECT$ " + clazz,
                        m_rete);

            Class c = m_rete.findClass(clazz);
            mapDefclassName(jessName, c.getName());

            // Make all the readable 'bean properties' into slots
            PropertyDescriptor[] props =
                    ReflectFunctions.getPropertyDescriptors(c);

            // Sort them first
            for (int i = 0; i < props.length - 1; i++)
                for (int j = i + 1; j < props.length; j++)
                    if (props[i].getName().compareTo(props[j].getName()) > 0) {
                        PropertyDescriptor temp = props[i];
                        props[i] = props[j];
                        props[j] = temp;
                    }


            // TODO: should set proper slot types
            for (int i = 0; i < props.length; i++) {
                Method m = props[i].getReadMethod();
                if (m == null)
                    continue;
                String name = props[i].getName();
                Class rt = m.getReturnType();
                Value slot = new Value(new SerializablePD(c, props[i]));
                if (rt.isArray())
                    dt.addMultiSlot(name, slot);
                else
                    dt.addSlot(name, slot, "ANY");

            }

            // Last slot is special - it holds the active instance
            dt.addSlot("OBJECT", Funcall.NIL, "OBJECT");

            // Install our synthetic deftemplate
            m_rete.addDeftemplate(dt);

            return new Value(c.getName(), RU.ATOM);
        } catch (ClassNotFoundException cnfe) {
            throw new JessException("defclass", "Class not found:", cnfe);
        } catch (IntrospectionException ie) {
            throw new JessException("defclass", "Introspection error:", ie);
        }
    }
}

