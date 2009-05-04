package jess;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * User-defined functions for manipulating 'bags' of properties.
 * <P>
 * (C) 2003 Ernest J. Friedman-Hill and Sandia National Laboratories<BR>
 * $Id: BagFunctions.java,v 1.3 2003/01/21 03:38:38 ejfried Exp $
 */

class BagFunctions implements Userpackage,
        IntrinsicPackage, Serializable {

    public void add(Rete engine) {
        engine.addUserfunction(new Bag());
    }

    public void add(HashMap table) {
        table.put("bag", new Bag());
    }
}

class Bag implements Userfunction, Serializable {
    private static Hashtable m_bags = new Hashtable();

    public String getName() {
        return "bag";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        String command = vv.get(1).stringValue(context);

        // Create, destroy and find bags.
        Hashtable bags = (Hashtable) m_bags.get(context.getEngine());
        if (bags == null)
            m_bags.put(context.getEngine(), bags = new Hashtable());

        if (command.equals("create")) {
            String name = vv.get(2).stringValue(context);
            Hashtable bag = (Hashtable) bags.get(name);
            if (bag == null) {
                bag = new Hashtable();
                bags.put(name, bag);
            }
            return new Value(bag);

        } else if (command.equals("delete")) {
            String name = vv.get(2).stringValue(context);
            bags.remove(name);
            if (bags.size() == 0)
                m_bags.remove(context.getEngine());
            return Funcall.TRUE;

        } else if (command.equals("find")) {
            String name = vv.get(2).stringValue(context);
            Hashtable bag = (Hashtable) bags.get(name);
            if (bag != null)
                return new Value(bag);
            else
                return Funcall.NIL;

        } else if (command.equals("list")) {
            ValueVector rv = new ValueVector();
            Enumeration e = bags.keys();
            while (e.hasMoreElements())
                rv.add(new Value((String) e.nextElement(), RU.STRING));
            return new Value(rv, RU.LIST);
        }

        // Set, check and read properties of bags
        else if (command.equals("set")) {
            Hashtable bag = (Hashtable) vv.get(2).externalAddressValue(context);
            String name = vv.get(3).stringValue(context);
            Value val = vv.get(4).resolveValue(context);
            bag.put(name, val);
            return val;

        } else if (command.equals("get")) {
            Hashtable bag = (Hashtable) vv.get(2).externalAddressValue(context);
            String name = vv.get(3).stringValue(context);
            Value v = (Value) bag.get(name);
            if (v != null)
                return v;
            else
                return Funcall.NIL;

        } else if (command.equals("props")) {
            Hashtable bag = (Hashtable) vv.get(2).externalAddressValue(context);
            ValueVector rv = new ValueVector();
            Enumeration e = bag.keys();
            while (e.hasMoreElements())
                rv.add(new Value((String) e.nextElement(), RU.STRING));
            return new Value(rv, RU.LIST);
        } else
            throw new JessException("bag", "Unknown command", command);
    }
}
