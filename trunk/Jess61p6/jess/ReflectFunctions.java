package jess;

import java.beans.*;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

/** **********************************************************************
 * Java Reflection for Jess.
 * <P>
 * This stuff is suprisingly powerful! Right now we don't handle
 * multi-dimensional arrays, but I think we don't miss anything else.
 * <P>
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 * @author E.J. Friedman-Hill
 ********************************************************************** */

class ReflectFunctions implements IntrinsicPackage, Serializable {

    private void addFunction(Userfunction uf, HashMap ht) {
        ht.put(uf.getName(), uf);
    }

    public void add(HashMap table) {
        addFunction(new Engine(), table);
        addFunction(new FetchContext(), table);
        addFunction(new JessImport(), table);
        addFunction(new JessNew(), table);
        addFunction(new Call(), table);
        addFunction(new JessField("set-member"), table);
        addFunction(new JessField("get-member"), table);
        addFunction(new Set(), table);
        addFunction(new Get(), table);
        addFunction(new Defclass(), table);
        addFunction(new UnDefinstance(), table);
        addFunction(new Definstance(), table);
        addFunction(new InstanceOf(), table);
    }

    /**
     * ******************************
     * Return a Java argument derived from the Value which matches the
     * Class object as closely as possible. Throws an exception if no match.
     * ******************************
     */

    static Object valueToObject(Class clazz, Value value, Context context)
            throws IllegalArgumentException, JessException {
        return valueToObject(clazz, value, context, true);
    }

    static Object valueToObject(Class clazz, Value value, Context context, boolean strict)
            throws IllegalArgumentException, JessException {
        value = value.resolveValue(context);
        switch (value.type()) {

            case RU.EXTERNAL_ADDRESS:
            case RU.FACT:
                {
                    if (clazz.isInstance(value.externalAddressValue(context)))
                        return value.externalAddressValue(context);
                    else
                        throw new IllegalArgumentException();
                }

            case RU.ATOM:
            case RU.STRING:
                {
                    String s = value.stringValue(context);

                    if (!clazz.isPrimitive() && s.equals(Funcall.NIL.stringValue(context)))
                        return null;

                    else if (clazz.isAssignableFrom(String.class))
                        return s;

                    else if (clazz == Character.TYPE) {
                        if (s.length() == 1)
                            return new Character(s.charAt(0));
                        else
                            throw new IllegalArgumentException();
                    } else if (clazz == Boolean.TYPE) {
                        if (s.equals(Funcall.TRUE.stringValue(context)))
                            return Boolean.TRUE;
                        if (s.equals(Funcall.FALSE.stringValue(context)))
                            return Boolean.FALSE;
                        else
                            throw new IllegalArgumentException();
                    } else
                        throw new IllegalArgumentException();
                }

            case RU.INTEGER:
                {

                    if (clazz == Long.TYPE || clazz == Long.class)
                        return new Long(value.longValue(context));

                    int i = value.intValue(context);

                    if (clazz == Integer.TYPE ||
                        clazz == Integer.class ||
                        clazz == Object.class)
                        return new Integer(i);

                    else if (clazz == Short.TYPE || clazz == Short.class)
                        return new Short((short) i);

                    else if (clazz == Character.TYPE || clazz == Character.class)
                        return new Character((char) i);

                    else if (clazz == Byte.TYPE || clazz == Byte.class)
                        return new Byte((byte) i);

                    else if (!strict && clazz == String.class)
                        return String.valueOf(i);

                    else
                        throw new IllegalArgumentException();

                }
            case RU.LONG:
                {

                    if (clazz == Long.TYPE ||
                        clazz == Long.class ||
                        clazz == Object.class)
                        return new Long(value.longValue(context));

                    int i = value.intValue(context);

                    if (clazz == Integer.TYPE ||
                        clazz == Integer.class)
                        return new Integer(i);

                    else if (clazz == Short.TYPE || clazz == Short.class)
                        return new Short((short) i);

                    else if (clazz == Character.TYPE || clazz == Character.class)
                        return new Character((char) i);

                    else if (clazz == Byte.TYPE || clazz == Byte.class)
                        return new Byte((byte) i);

                    else if (!strict && clazz == String.class)
                        return String.valueOf(i);

                    else
                        throw new IllegalArgumentException();

                }

            case RU.FLOAT:
                {
                    double d = value.floatValue(context);

                    if (clazz == Double.TYPE ||
                        clazz == Double.class ||
                        clazz == Object.class)
                        return new Double(d);

                    else if (clazz == Float.TYPE || clazz == Float.class)
                        return new Float((float) d);

                    else if (!strict && clazz == String.class)
                        return String.valueOf(d);

                    else
                        throw new IllegalArgumentException();

                }

                // Turn lists into arrays.
            case RU.LIST:
                {
                    if (clazz.isArray()) {
                        Class elemType = clazz.getComponentType();
                        ValueVector vv = value.listValue(context);
                        Object array = Array.newInstance(elemType, vv.size());
                        for (int i = 0; i < vv.size(); i++)
                            Array.set(array, i,
                                    valueToObject(elemType, vv.get(i), context, false));
                        return array;
                    } else
                        throw new IllegalArgumentException();
                }
            default:
                throw new IllegalArgumentException();
        }

    }

    /**
     * ******************************
     * Create a Jess Value object out of a Java Object. Primitive types get
     * special treatment.
     * ******************************
     */

    static Value objectToValue(Class c, Object obj) throws JessException {
        Class r = (obj == null) ? Void.TYPE : obj.getClass();

        if (obj == null && !c.isArray())
            return Funcall.NIL;

        if (c == Void.class)
            return Funcall.NIL;

        if (obj instanceof Value)
            return (Value) obj;

        if (c == String.class || r == String.class)
            return new Value(obj.toString(), RU.STRING);

        if (c.isArray()) {
            int length = 0;
            if (obj != null)
                length = Array.getLength(obj);
            ValueVector vv = new ValueVector(length);

            for (int i = 0; i < length; i++)
                vv.add(objectToValue(c.getComponentType(), Array.get(obj, i)));

            return new Value(vv, RU.LIST);
        }

        if (c == Boolean.TYPE || r == Boolean.TYPE ||
            c == Boolean.class || r == Boolean.class)
            return ((Boolean) obj).booleanValue() ? Funcall.TRUE : Funcall.FALSE;

        if (c == Byte.TYPE || c == Short.TYPE || c == Integer.TYPE ||
            r == Byte.TYPE || r == Short.TYPE || r == Integer.TYPE ||
            c == Byte.class || c == Short.class || c == Integer.class ||
            r == Byte.class || r == Short.class || r == Integer.class)

            return new Value(((Number) obj).intValue(), RU.INTEGER);

        if (c == Long.TYPE || r == Long.TYPE ||
            c == Long.class || r == Long.class)
            return new LongValue(((Long) obj).longValue());

        if (c == Double.TYPE || c == Float.TYPE ||
            r == Double.TYPE || r == Float.TYPE ||
            c == Double.class || c == Float.class ||
            r == Double.class || r == Float.class)
            return new Value(((Number) obj).doubleValue(), RU.FLOAT);

        if (c == Character.TYPE || r == Character.TYPE ||
            c == Character.class || r == Character.class)
            return new Value(obj.toString(), RU.ATOM);

        return new Value(obj);
    }

    private static Hashtable s_descriptors = new Hashtable();

    static PropertyDescriptor[] getPropertyDescriptors(Class c)
            throws JessException, IntrospectionException {

        PropertyDescriptor[] pds;
        if ((pds = (PropertyDescriptor[]) s_descriptors.get(c)) != null)
            return pds;

        BeanInfo bi = Introspector.getBeanInfo(c);
        if (bi.getBeanDescriptor().getBeanClass() != c)
            throw new JessException("ReflectFunctions.getPropertyDescriptors",
                    "Introspector returned bogus BeanInfo object for class ",
                    bi.getBeanDescriptor().getBeanClass().getName());

        pds = bi.getPropertyDescriptors();
        s_descriptors.put(c, pds);
        return pds;
    }
}

class Engine implements Userfunction, Serializable {
    public String getName() {
        return "engine";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        return new Value(context.getEngine());
    }
}

class FetchContext implements Userfunction, Serializable {
    public String getName() {
        return "context";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        return new Value(context);
    }
}

/**
 * **********************************************************************
 * Call a Java method from Jess. First argument is EITHER an external-address
 * object, or the name of a class. The latter works only for Static methods, of
 * course. Later arguments are the contructor arguments. We pick methods based
 * on a first-fit algorithm, not necessarily a best-fit. If you want to be super
 * selective, you can disambiguate by wrapping basic types in object wrappers.
 * If it absolutely won't work, well, you can always write a Java Userfunction
 * as a wrapper!
 * **********************************************************************
 * @author Ernest J. Friedman-Hill
 */

class Call implements Userfunction, Serializable {

    String m_name = "call";

    public String getName() {
        return m_name;
    }

    private static Hashtable s_methods = new Hashtable();

    static Method[] getMethods(Class c) {
        if (s_methods.get(c) != null)
            return (Method[]) s_methods.get(c);
        else {
            Method[] m = c.getMethods();
            s_methods.put(c, m);
            return m;
        }
    }


    public Value call(ValueVector vv, Context context) throws JessException {

        ValueVector resolved = new ValueVector(vv.size());
        for (int i=0; i<vv.size(); ++i)
            resolved.add(vv.get(i).resolveValue(context));

        String method = resolved.get(2).stringValue(context);

        Class c = null;
        try {
            Object target = null;

            Value v = resolved.get(1);
            if (v.type() == RU.STRING || v.type() == RU.ATOM) {

                if (v.equals(Funcall.NIL))
                    throw new JessException("call",
                            "Can't call method on nil reference:",
                            method);

                try {
                    c = context.getEngine().findClass(v.stringValue(context));
                } catch (Exception cnfe) {
                    // Maybe we're supposed to call the method
                    // on the string object itself...
                }
            }

            if (c == null) {
                target = v.externalAddressValue(context);
                c = target.getClass();
            }

            /*
             * Build argument list
             */

            int nargs = resolved.size() - 3;
            Object args[] = new Object[nargs];

            Method[] methods = Call.getMethods(c);
            Object rv;
            int i;
            for (i = 0; i < methods.length; i++) {
                try {
                    Method m = methods[i];
                    Class[] argTypes = m.getParameterTypes();
                    if (!m.getName().equals(method) || nargs != argTypes.length)
                        continue;

                    // OK, found a method. Problem is, it might be a public
                    // method of a private class. We'll check for this, and
                    // if so, we have to find a more appropriate method
                    // descriptor. Can't believe we have to do this.

                    if (!Modifier.isPublic(c.getModifiers())) {
                        m = null;
                        escape:
                          while (c != null) {
                              Class[] interfaces = c.getInterfaces();
                              for (int ii = 0; ii < interfaces.length; ii++) {
                                  try {
                                      m = interfaces[ii].getMethod(method, argTypes);
                                      break escape;
                                  } catch (NoSuchMethodException nsme) {
                                  }
                              }
                              c = c.getSuperclass();
                              if (c != null && Modifier.isPublic(c.getModifiers())) {
                                  try {
                                      m = c.getMethod(method, argTypes);
                                      break escape;
                                  } catch (NoSuchMethodException nsme) {
                                  }
                              }
                          }
                        if (m == null)
                            throw new JessException("call",
                                    "Method not accessible",
                                    method);
                    }

                    // Now give it a try!

                    for (int j = 0; j < nargs; j++) {
                        args[j]
                                = ReflectFunctions.valueToObject(argTypes[j],
                                        resolved.get(j + 3),
                                        context);

                    }


                    rv = m.invoke(target, args);

                    return ReflectFunctions.objectToValue(m.getReturnType(), rv);

                } catch (IllegalArgumentException iae) {
                    // Try the next one!
                }
            }

            throw new NoSuchMethodException(method);

        } catch (NoSuchMethodException nsm) {
            if (!hasMethodOfName(c, method))
                throw new JessException("call", "No method named '" + method + "' found",
                        "in class " + c.getName());

            else
                throw new JessException("call", "No overloading of method '" + method + "'",
                        "in class " + c.getName() +
                        " I can call with these arguments: " +
                        resolved.toStringWithParens());

        } catch (InvocationTargetException ite) {
            throw new JessException("call", "Called method threw an exception",
                    ite.getTargetException());
        } catch (IllegalAccessException iae) {
            throw new JessException("call", "Method is not accessible", iae);
        } catch (IllegalArgumentException iae) {
            throw new JessException("call", "Invalid argument to " + method, iae);
        }
    }

    private boolean hasMethodOfName(Class c, String name) {
        try {
            Method[] m = Call.getMethods(c);
            for (int i = 0; i < m.length; i++)
                if (m[i].getName().equals(name))
                    return true;
            return false;
        } catch (Exception e) {
            return false;
        }
    }

}

class Set extends Call {
    Set() {
        m_name = "set";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        try {
            // Pass this along to 'call'
            // We can't self-modify since we're not copied
            Funcall f = new Funcall("call", context.getEngine());
            for (int i = 1; i < vv.size(); i++)
                f.arg(vv.get(i).resolveValue(context));

            String propName = f.get(2).stringValue(context);

            PropertyDescriptor[] pd =
                    ReflectFunctions.getPropertyDescriptors(f.get(1).externalAddressValue(context).getClass());

            for (int i = 0; i < pd.length; i++) {
                Method m;
                if (pd[i].getName().equals(propName) &&
                        (m = pd[i].getWriteMethod()) != null) {
                    f.set(new Value(m.getName(), RU.STRING), 2);
                    return super.call(f, context);
                }
            }
            throw new JessException("set", "No such property:", propName);
        } catch (IntrospectionException ie) {
            throw new JessException("set", "Introspection Error:", ie);
        }
    }
}

class Get extends Call {
    Get() {
        m_name = "get";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        try {
            // Pass this along to 'call'
            // We can't self-modify since we're not copied
            Funcall f = new Funcall("call", context.getEngine());
            for (int i = 1; i < vv.size(); i++)
                f.arg(vv.get(i).resolveValue(context));

            String propName = vv.get(2).stringValue(context);
            // note that these are cached, so all the introspection
            // only gets done once.
            PropertyDescriptor[] pd =
                    ReflectFunctions.getPropertyDescriptors(f.get(1).externalAddressValue(context).getClass());
            for (int i = 0; i < pd.length; i++) {
                Method m;
                if (pd[i].getName().equals(propName) &&
                        (m = pd[i].getReadMethod()) != null) {
                    f.set(new Value(m.getName(), RU.STRING), 2);
                    return super.call(f, context);
                }
            }
            throw new JessException("get", "No such property:", propName);
        } catch (IntrospectionException ie) {
            throw new JessException("get", "Introspection Error", ie);
        }
    }
}

class JessImport implements Userfunction, Serializable {

    public String getName() {
        return "import";
    }

    public Value call(ValueVector vv, Context c) throws JessException {
        // ###
        String arg = vv.get(1).atomValue(c);
        if (arg.indexOf("*") != -1)
            c.getEngine().importPackage(arg.substring(0, arg.indexOf("*")));

        else
            c.getEngine().importClass(arg);
        return Funcall.TRUE;
    }
}

/**
 * **********************************************************************
 * Create a Java object from Jess
 * The first argument is the full-qualified typename; later arguments are
 * the contructor arguments.  We pick methods based on a first-fit algorithm,
 * not necessarily a best-fit. If you want to be super selective, you can
 * disambiguate by wrapping basic types in object wrappers.
 * **********************************************************************
 * @author Ernest J. Friedman-Hill
 */

class JessNew implements Userfunction, Serializable {
    public String getName() {
        return "new";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        try {
            /*
              Find target class
            */

            String clazz = vv.get(1).stringValue(context);
            Class c = context.getEngine().findClass(clazz);


            ValueVector resolved = new ValueVector();
            for (int i = 2; i < vv.size(); i++)
                resolved.add(vv.get(i).resolveValue(context));


            /*
             * Build argument list
             */

            int nargs = vv.size() - 2;
            Object args[] = new Object[nargs];

            Constructor[] cons = c.getConstructors();
            Object rv;
            int i;
            for (i = 0; i < cons.length; i++) {
                try {
                    Constructor constructor = cons[i];
                    Class[] argTypes = constructor.getParameterTypes();
                    if (nargs != argTypes.length)
                        continue;

                    // Otherwise give it a try!
                    for (int j = 0; j < nargs; j++) {
                        args[j]
                                = ReflectFunctions.valueToObject(argTypes[j],
                                        resolved.get(j),
                                        context);
                    }

                    rv = constructor.newInstance(args);
                    return new Value(rv);

                } catch (IllegalArgumentException iae) {
                    // Try the next one!
                }
            }

            throw new NoSuchMethodException(c.getName());

        } catch (InvocationTargetException ite) {
            throw new JessException("new", "Constructor threw an exception",
                    ite.getTargetException());
        } catch (NoSuchMethodException nsm) {
            throw new JessException("new", "Constructor not found: " +
                    vv.toStringWithParens(),
                    nsm);
        } catch (ClassNotFoundException cnfe) {
            throw new JessException("new", "Class not found", cnfe);
        } catch (IllegalAccessException iae) {
            throw new JessException("new",
                    "Class or constructor is not accessible",
                    iae);
        } catch (InstantiationException ie) {
            throw new JessException("new", "Class cannot be instantiated", ie);
        }
    }
}

/**
 * **********************************************************************
 * Set or get a data member of a Java object from Jess
 * **********************************************************************
 * @author Ernest J. Friedman-Hill
 */

class JessField implements Userfunction, Serializable {

    private String m_name;

    public String getName() {
        return m_name;
    }

    JessField(String functionName) {
        // name should be get-member or set-member
        m_name = functionName;
    }


    public Value call(ValueVector vv, Context context) throws JessException {
        String field = vv.get(2).stringValue(context);

        boolean doSet = false;

        if (vv.get(0).stringValue(context).equals("set-member"))
            doSet = true;

        Class c = null;
        Object target = null;

        Value v = vv.get(1).resolveValue(context);

        if (v.type() == RU.STRING || v.type() == RU.ATOM) {
            try {
                c = context.getEngine().findClass(v.stringValue(context));
            } catch (ClassNotFoundException ex) {
                throw new JessException(vv.get(0).stringValue(context),
                        "No such class",
                        v.stringValue(context));
            }
        }
        if (c == null) {
            target = v.externalAddressValue(context);
            c = target.getClass();
        }

        try {
            Field f = c.getField(field);
            Class argType = f.getType();
            if (doSet) {
                Value v2 = vv.get(3).resolveValue(context);
                f.set(target,
                        ReflectFunctions.valueToObject(argType, v2, context));
                return v2;
            } else {
                Object o = f.get(target);
                return ReflectFunctions.objectToValue(argType, o);
            }
        } catch (NoSuchFieldException nsfe) {
            throw new JessException(vv.get(0).stringValue(context),
                    "No such field " + field +
                    " in class ", c.getName());
        } catch (IllegalAccessException iae) {
            throw new JessException(vv.get(0).stringValue(context),
                    "Field is not accessible",
                    field);
        } catch (IllegalArgumentException iae) {
            throw new JessException(vv.get(0).stringValue(context),
                    "Invalid argument",
                    vv.get(1).toString());
        }
    }
}

/************************************************************************
 * Tell Jess to match on properties of a specific Java object
 *
 */

class Definstance
        implements Userfunction, Serializable {

    /**
     * Return the name of this command
     * @return The command name
     */
    public String getName() {
        return "definstance";
    }

    /**
     * SYNTAX: (definstance <jess-classname> <external-address>
     * [static | dynamic])
     */
    public Value call(ValueVector vv, Context context) throws JessException {
        // ###
        Value v = vv.get(2).resolveValue(context);
        if (v.equals(Funcall.NIL))
            throw new JessException("definstance",
                    "Argument is nil:", v.toString());

        String jessTypename = vv.get(1).stringValue(context);
        Object object = v.externalAddressValue(context);
        boolean dynamic = (vv.size() < 4 || vv.get(3).equals("dynamic"));

        Rete engine = context.getEngine();

        return engine.definstance(jessTypename, object, dynamic, context);
    }
}

/**
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */
class UnDefinstance implements Userfunction, Serializable {

    public String getName() {
        return "undefinstance";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Rete engine = context.getEngine();
        Value v = vv.get(1).resolveValue(context);
        if (v.type() == RU.EXTERNAL_ADDRESS) {
            Fact f = engine.undefinstance(v.externalAddressValue(context));
            if (f == null)
                return Funcall.NIL;
            else
                return new FactIDValue(f);
        } else if (v.equals("*")) {
            for (Iterator e = engine.listDefinstances(); e.hasNext();)
                engine.undefinstance(e.next());
            return Funcall.TRUE;
        } else
            throw new JessException("undefinstance",
                    "Invalid argument", v.toString());
    }
}

/** **********************************************************************
 * Tell Jess to prepare to match on properties of a Java class
 * Generates a deftemplate from the class
 * @author Ernest J. Friedman-Hill
 *
 ********************************************************************** */

class Defclass implements Userfunction, Serializable {

    public String getName() {
        return "defclass";
    }

    /**
     * SYNTAX: (defclass <jess-classname> <Java-classname> [extends <parent>])
     */
    public Value call(ValueVector vv, Context context) throws JessException {
        // ###
        String jessName = vv.get(1).stringValue(context);
        String clazz = vv.get(2).stringValue(context);
        String parent = vv.size() > 4 ? vv.get(4).stringValue(context) : null;

        if (parent != null && !vv.get(3).equals("extends"))
            throw new JessException("defclass",
                    "expected 'extends <classname>'",
                    vv.get(3).toString());

        return context.getEngine().defclass(jessName, clazz, parent);
    }
}


class InstanceOf implements Userfunction, Serializable {
    public String getName() {
        return "instanceof";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Object o = vv.get(1).externalAddressValue(context);
        String className = vv.get(2).stringValue(context);
        try {
            Class clazz = context.getEngine().findClass(className);
            return clazz.isInstance(o) ? Funcall.TRUE : Funcall.FALSE;
        } catch (ClassNotFoundException cnfe) {
            throw new JessException("instanceof",
                    "Class not found: " + className,
                    cnfe);
        }

    }
}
