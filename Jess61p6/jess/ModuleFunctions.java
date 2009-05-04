package jess;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

/** **********************************************************************
 * Some functions associated with defmodules
 * <P>
 * See the Jess manual for the list of functions in this package.
 * <P>
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 ********************************************************************** */

class ModuleFunctions implements IntrinsicPackage, Serializable {

    private void addFunction(Userfunction uf, HashMap ht) {
        ht.put(uf.getName(), uf);
    }

    public void add(HashMap table) {

        addFunction(new SetFocus(), table);
        addFunction(new GetFocus(), table);
        addFunction(new SetCurrentModule(), table);
        addFunction(new GetCurrentModule(), table);
        addFunction(new ListFocusStack(), table);
        addFunction(new GetFocusStack(), table);
        addFunction(new ClearFocusStack(), table);
        addFunction(new PopFocus(), table);
    }
}

class SetFocus implements Userfunction, Serializable {
    public String getName() {
        return "focus";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Rete engine = context.getEngine();
        String oldFocus = engine.getFocus();
        for (int i = (vv.size() - 1); i > 0; --i) {
            String module = vv.get(i).stringValue(context);
            engine.setFocus(module);
        }
        return new Value(oldFocus, RU.ATOM);
    }
}

class GetFocus implements Userfunction, Serializable {
    public String getName() {
        return "get-focus";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        String module = context.getEngine().getFocus();
        return new Value(module, RU.ATOM);
    }
}

class PopFocus implements Userfunction, Serializable {
    public String getName() {
        return "pop-focus";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        String module = context.getEngine().popFocus(null);
        return new Value(module, RU.ATOM);
    }
}

class SetCurrentModule implements Userfunction, Serializable {
    public String getName() {
        return "set-current-module";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        String module = vv.get(1).stringValue(context);
        String oldModule = context.getEngine().setCurrentModule(module);
        return new Value(oldModule, RU.ATOM);
    }
}

class GetCurrentModule implements Userfunction, Serializable {
    public String getName() {
        return "get-current-module";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        String module = context.getEngine().getCurrentModule();
        return new Value(module, RU.ATOM);
    }
}


class ClearFocusStack implements Userfunction, Serializable {
    public String getName() {
        return "clear-focus-stack";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        context.getEngine().clearFocusStack();
        return Funcall.NIL;
    }
}

/**
 * Has to "flip" the values as returned by listFocusStack().
 */

class ListFocusStack implements Userfunction, Serializable {
    public String getName() {
        return "list-focus-stack";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Rete r = context.getEngine();
        PrintWriter outStream = r.getOutStream();
        Stack s = new Stack();
        for (Iterator it = r.listFocusStack(); it.hasNext();)
            s.push(it.next());
        while (!s.isEmpty())
            outStream.println(s.pop());
        outStream.flush();
        return Funcall.NIL;
    }
}

class GetFocusStack implements Userfunction, Serializable {
    public String getName() {
        return "get-focus-stack";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Rete r = context.getEngine();
        ValueVector rv = new ValueVector();
        Stack s = new Stack();
        for (Iterator it = r.listFocusStack(); it.hasNext();)
            s.push(it.next());
        while (!s.isEmpty())
            rv.add(new Value((String) s.pop(), RU.ATOM));
        return new Value(rv, RU.LIST);
    }
}



