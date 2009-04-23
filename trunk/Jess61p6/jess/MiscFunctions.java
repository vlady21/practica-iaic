package jess;

import jess.factory.Factory;

import java.io.*;
import java.net.Socket;
import java.util.*;

/** **********************************************************************
 * Some miscellaneous functions.
 * <P>
 * See the Jess manual for the list of functions in this package.
 * <P>
 * (C) 2003 E.J. Friedman-Hill and the Sandia Corporation<BR>
 * $Id: MiscFunctions.java,v 1.5.2.2 2003/09/02 14:33:20 ejfried Exp $
 ********************************************************************** */

class MiscFunctions implements IntrinsicPackage, Serializable {

    private void addFunction(Userfunction uf, HashMap ht) {
        ht.put(uf.getName(), uf);
    }

    public void add(HashMap table) {

        addFunction(new GetStrategy(), table);
        addFunction(new SetStrategy(), table);
        addFunction(new JessSocket(), table);
        addFunction(new JessFormat(), table);
        addFunction(new JessSystem(), table);
        addFunction(new LoadPkg(), table);
        addFunction(new LoadFn(), table);
        addFunction(new Time(), table);

        // These two are the same for now!
        addFunction(new Build("build"), table);
        addFunction(new Build("eval"), table);

        addFunction(new ListFunctions(), table);
        addFunction(new RunQuery(RunQuery.RUN), table);
        addFunction(new RunQuery(RunQuery.COUNT), table);
        addFunction(new ShowAgenda(), table);
        addFunction(new ListRules(), table);
        addFunction(new ListFacts(), table);
        addFunction(new ListDeftemplates(), table);

        addFunction(new Bits(Bits.AND), table);
        addFunction(new Bits(Bits.OR), table);
        addFunction(new Bits(Bits.NOT), table);

        // setgen added by Win Carus (9.19.97)
        addFunction(new Setgen(), table);

        addFunction(new ResetGlobals(ResetGlobals.SET), table);
        addFunction(new ResetGlobals(ResetGlobals.GET), table);

        addFunction(new EvalSalience(EvalSalience.SET), table);
        addFunction(new EvalSalience(EvalSalience.GET), table);

        addFunction(new SetNodeIndexing(), table);
        addFunction(new SetFactory(), table);

        addFunction(new JessLong(), table);

        addFunction(new CallOnEngine(), table);
        addFunction(new MakeFactID(), table);
        addFunction(new Asc(), table);
        addFunction(new Synchronized(), table);

        addFunction(new Dependents(), table);
        addFunction(new Dependencies(), table);

    }
}

class SetStrategy implements Userfunction, Serializable {
    public String getName() {
        return "set-strategy";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Strategy s;
        String name = vv.get(1).stringValue(context);
        Rete engine = context.getEngine();
        try {
            s = (Strategy) engine.findClass("jess." + name).newInstance();
        } catch (Throwable t) {
            try {
                s = (Strategy) engine.findClass(name).newInstance();
            } catch (Throwable tt) {
                throw new JessException("set-strategy",
                        "Strategy class not found:", name);
            }
        }

        // return name of old strategy
        String rv = engine.setStrategy(s);
        return new Value(rv, RU.ATOM);
    }
}

class GetStrategy implements Userfunction, Serializable {
    public String getName() {
        return "get-strategy";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        return new Value(context.getEngine().getStrategy().getName(), RU.ATOM);
    }
}

class Build implements Userfunction, Serializable {
    private String m_name;

    Build(String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        String argument = vv.get(1).stringValue(context);
        return context.getEngine().executeCommand(argument, context);
    }
}

class Bits implements Userfunction, Serializable {
    private String m_name;
    final static String AND = "bit-and", OR = "bit-or", NOT = "bit-not";

    Bits(String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        int rv = vv.get(1).intValue(context);

        if (m_name.equals(AND)) {
            for (int i = 2; i < vv.size(); i++)
                rv &= vv.get(i).intValue(context);
        } else if (m_name.equals(OR)) {
            for (int i = 2; i < vv.size(); i++)
                rv |= vv.get(i).intValue(context);
        } else // not
        {
            rv = ~rv;
        }
        return new Value(rv, RU.INTEGER);
    }
}


class ListFunctions implements Userfunction, Serializable {
    public String getName() {
        return "list-function$";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        ValueVector rv = new ValueVector(100);
        HashSet ht = new HashSet();

        Iterator e = context.getEngine().listFunctions();
        while (e.hasNext()) {
            String name = ((Userfunction) e.next()).getName();
            ht.add(name);
        }

        e = Funcall.listIntrinsics();
        while (e.hasNext()) {
            String name = ((Userfunction) e.next()).getName();
            ht.add(name);
        }

        Object[] o = ht.toArray();
        Arrays.sort(o);
        for (int i = 0; i < o.length; ++i)
            rv.add(new Value((String) o[i], RU.ATOM));

        return new Value(rv, RU.LIST);
    }
}

abstract class ModuleOperator implements Serializable {
    String getModule(ValueVector vv, Context context) throws JessException {
        return
                (vv.size() == 1) ?
                context.getEngine().getCurrentModule() :
                vv.get(1).stringValue(context);
    }

    static interface Filter {
        boolean accept(Modular m);
    }

    static interface Displayer {
        void display(Modular o, PrintWriter pw);
    }

    class NullFilter implements Filter {
        public boolean accept(Modular m) {
            return true;
        }
    }

    class NameFilter implements Filter {
        private String m_name;

        NameFilter(String name) {
            m_name = name;
        }

        public boolean accept(Modular m) {
            return m_name.equals(m.getModule());
        }
    }

    Filter chooseFilter(String module, Rete r) throws JessException {
        if (module.equals("*"))
            return new NullFilter();

        else {
            r.verifyModule(module);
            return new NameFilter(module);
        }
    }

    Value displayAll(Iterator it, ValueVector vv, Context context,
                     String name, Displayer d)
            throws JessException {

        String module = getModule(vv, context);
        Rete r = context.getEngine();
        Filter f = chooseFilter(module, r);
        PrintWriter pw = r.getOutStream();
        int count = 0;
        while (it.hasNext()) {
            Modular m = (Modular) it.next();
            if (f.accept(m)) {
                d.display(m, pw);
                ++count;
            }
        }

        pw.print("For a total of ");
        pw.print(count);
        pw.print(" ");
        pw.print(name);
        pw.println(".");
        pw.flush();
        return Funcall.NIL;

    }
}

class ShowAgenda extends ModuleOperator implements Userfunction {
    public String getName() {
        return "agenda";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Rete r = context.getEngine();
        PrintWriter outStream = r.getOutStream();
        int count = 0;
        String agenda = getModule(vv, context);

        if (agenda.equals("*")) {
            for (Iterator modules = r.listModules(); modules.hasNext();) {
                String moduleName = (String) modules.next();
                outStream.print(moduleName);
                outStream.println(":");
                count +=
                        showOneAgenda(r.listActivations(moduleName), outStream);
            }
        } else {
            count = showOneAgenda(r.listActivations(agenda), outStream);
        }

        outStream.print("For a total of ");
        outStream.print(count);
        outStream.println(" activations.");
        outStream.flush();
        return Funcall.NIL;
    }

    private int showOneAgenda(Iterator e, PrintWriter outStream) {
        int count = 0;
        while (e.hasNext()) {
            Activation a = (Activation) e.next();
            if (!a.isInactive()) {
                outStream.println(a);
                ++count;
            }
        }
        return count;
    }

}

class ListRules extends ModuleOperator implements Userfunction {
    public String getName() {
        return "rules";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Rete r = context.getEngine();
        return displayAll(r.listDefrules(), vv, context, "rules",
                new Displayer() {
                    public void display(Modular m, PrintWriter pw) {
                        pw.println(m.getName());
                    }
                });
    }
}

class ListFacts extends ModuleOperator implements Userfunction {
    public String getName() {
        return "facts";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Rete r = context.getEngine();
        return displayAll(r.listFacts(), vv, context, "facts",
                new Displayer() {
                    public void display(Modular m, PrintWriter pw) {
                        Fact f = (Fact) m;
                        pw.print("f-");
                        pw.print(f.getFactId());
                        pw.print("   ");
                        pw.println(f);
                    }
                });
    }
}

class ListDeftemplates extends ModuleOperator implements Userfunction {
    public String getName() {
        return "list-deftemplates";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Rete r = context.getEngine();
        return displayAll(r.listDeftemplates(), vv, context, "deftemplates",
                new Displayer() {
                    public void display(Modular m, PrintWriter pw) {
                        pw.println(m.getName());
                    }
                });
    }
}

class JessSystem implements Userfunction, Serializable {
    public String getName() {
        return "system";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        try {
            boolean async = false;
            int size = vv.size();
            if (vv.get(size - 1).stringValue(context).equals("&")) {
                async = true;
                --size;
            }

            String[] words = new String[size - 1];
            for (int i = 1; i < size; i++)
                words[i - 1] = vv.get(i).stringValue(context);
            Process p = Runtime.getRuntime().exec(words);
            Thread t1 = new ReaderThread(p.getInputStream(),
                    context.getEngine().getOutStream());
            t1.start();
            Thread t2 = new ReaderThread(p.getErrorStream(),
                    context.getEngine().getErrStream());
            t2.start();

            if (!async) {
                try {
                    p.waitFor();
                    t1.join();
                    t2.join();
                } catch (InterruptedException ie) {
                    /* Nothing */
                }
            }
            return new Value(p);
        } catch (IOException ioe) {
            throw new JessException("system", vv.toStringWithParens(), ioe);
        } catch (SecurityException se) {
            throw new JessException("system", vv.toStringWithParens(), se);
        }
    }

    // Read outputs of subprocess, send to terminal
    private class ReaderThread extends Thread {
        InputStream m_is;
        Writer m_os;

        ReaderThread(InputStream is, Writer os) {
            m_is = is;
            m_os = os;
            setDaemon(true);
        }

        public void run() {
            try {
                int i;
                while (true) {
                    if ((i = m_is.read()) != -1)
                        m_os.write((char) i);
                    else
                        break;
                }
            } catch (Exception e) { /* quietly exit */
            }
        }
    }
}

class LoadPkg implements Userfunction, Serializable {
    public String getName() {
        return "load-package";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        String clazz = vv.get(1).stringValue(context);
        try {
            Rete engine = context.getEngine();
            Userpackage up = (Userpackage) engine.findClass(clazz).newInstance();
            engine.addUserpackage(up);
        } catch (ClassNotFoundException cnfe) {
            throw new JessException("load-package", "Class not found", clazz);
        } catch (IllegalAccessException iae) {
            throw new JessException("load-package", "Class is not accessible",
                    clazz);
        } catch (InstantiationException ie) {
            throw new JessException("load-package", "Class cannot be instantiated",
                    clazz);
        } catch (ClassCastException cnfe) {
            throw new JessException("load-package",
                    "Class must inherit from UserPackage", clazz);
        }
        return Funcall.TRUE;
    }
}

class LoadFn implements Userfunction, Serializable {
    public String getName() {
        return "load-function";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        String clazz = vv.get(1).stringValue(context);
        try {
            Rete engine = context.getEngine();
            Userfunction uf = (Userfunction) engine.findClass(clazz).newInstance();
            engine.addUserfunction(uf);
        } catch (ClassNotFoundException cnfe) {
            throw new JessException("load-function", "Class not found", clazz);
        } catch (IllegalAccessException iae) {
            throw new JessException("load-function", "Class is not accessible",
                    clazz);
        } catch (InstantiationException ie) {
            throw new JessException("load-function",
                    "Class cannot be instantiated",
                    clazz);
        } catch (ClassCastException cnfe) {
            throw new JessException("load-function",
                    "Class must inherit from UserFunction", clazz);
        }

        return Funcall.TRUE;
    }
}

class Time implements Userfunction, Serializable {
    public String getName() {
        return "time";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        return new Value(System.currentTimeMillis() / 1000, RU.FLOAT);
    }
}

class JessSocket implements Userfunction, Serializable {
    public String getName() {
        return "socket";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        String host = vv.get(1).stringValue(context);
        int port = vv.get(2).intValue(context);
        String router = vv.get(3).stringValue(context);

        try {
            Socket sock = new Socket(host, port);
            Rete engine = context.getEngine();
            engine.addInputRouter(router,
                    new InputStreamReader(sock.getInputStream()),
                    false);
            engine.addOutputRouter(router,
                    new PrintWriter(sock.getOutputStream()));
            return vv.get(3);
        } catch (IOException ioe) {
            throw new JessException("socket", "I/O Exception", ioe);
        }
    }
}

class Setgen implements Userfunction, Serializable {
    public String getName() {
        return "setgen";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        int i = vv.get(1).intValue(context);
        RU.s_gensymIdx = (i > RU.s_gensymIdx) ? i : (RU.s_gensymIdx + 1);

        return Funcall.TRUE;
    }
}

class ResetGlobals implements Userfunction, Serializable {
    public static final int SET = 0, GET = 1;
    private int m_cmd;

    public ResetGlobals(int cmd) {
        m_cmd = cmd;
    }

    public String getName() {
        return m_cmd == SET ? "set-reset-globals" : "get-reset-globals";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        switch (m_cmd) {
            case SET:
                {
                    Value v;
                    Value v1 = vv.get(1);
                    if (v1.equals(Funcall.NIL) || v1.equals(Funcall.FALSE)) {
                        context.getEngine().setResetGlobals(false);
                        v = Funcall.FALSE;
                    } else {
                        context.getEngine().setResetGlobals(true);
                        v = Funcall.TRUE;
                    }
                    return v;
                }

            default:
                return context.getEngine().getResetGlobals() ?
                        Funcall.TRUE : Funcall.FALSE;
        }
    }
}

class EvalSalience implements Userfunction, Serializable {
    public static final int SET = 0, GET = 1;
    private static final String[] s_values = {"when-defined", "when-activated",
                                              "every-cycle"};
    private int m_cmd;

    public EvalSalience(int cmd) {
        m_cmd = cmd;
    }

    public String getName() {
        return m_cmd == SET ? "set-salience-evaluation"
                : "get-salience-evaluation";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        switch (m_cmd) {
            case SET:
                {
                    int old = context.getEngine().getEvalSalience();
                    String s = vv.get(1).stringValue(context);
                    for (int i = 0; i < s_values.length; i++)
                        if (s.equals(s_values[i])) {
                            context.getEngine().setEvalSalience(i);
                            return new Value(s_values[old], RU.ATOM);
                        }
                    throw new JessException("set-eval-salience", "Invalid value: " + s,
                            "(valid values are when-defined, " +
                            "when-activated, every-cycle)");
                }
            case GET:
            default:
                {
                    return new Value(s_values[context.getEngine().getEvalSalience()],
                            RU.ATOM);
                }
        }
    }
}

class SetNodeIndexing implements Userfunction, Serializable {
    public String getName() {
        return "set-node-index-hash";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        int hash = vv.get(1).intValue(context);

        context.getEngine().getCompiler().setHashKey(hash);
        return Funcall.TRUE;
    }
}

class SetFactory implements Userfunction, Serializable {
    public String getName() {
        return "set-factory";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Factory newF =
                (jess.factory.Factory) vv.get(1).externalAddressValue(context);

        Factory oldF = context.getEngine().getFactory();
        context.getEngine().setFactory(newF);

        return new Value(oldF);
    }
}

class RunQuery implements Userfunction, Serializable {
    public static final int RUN = 0, COUNT = 1;
    private int m_cmd;

    RunQuery(int cmd) {
        m_cmd = cmd;
    }

    public String getName() {
        return m_cmd == RUN ? "run-query" : "count-query-results";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        String queryName = vv.get(1).atomValue(context);
        HasLHS lhs = context.getEngine().findDefrule(queryName);
        if (lhs == null || !(lhs instanceof Defquery))
            throw new JessException("run-query", "No such query:", queryName);

        Defquery dq = (Defquery) lhs;
        Rete engine = context.getEngine();

        // Create the query-trigger fact
        Fact f = new Fact(dq.getQueryTriggerName(), engine);
        ValueVector qv = new ValueVector();
        if ((vv.size() - 2) != dq.getNVariables())
            throw new JessException("run-query", "Wrong number of variables for query", queryName);

        for (int i = 2; i < vv.size(); i++)
            qv.add(vv.get(i).resolveValue(context));

        f.setSlotValue(RU.DEFAULT_SLOT_NAME, new Value(qv, RU.LIST));

        // Assert the fact, blocking access to other queries; then return the
        // results, which clears the query
        synchronized (engine.getCompiler()) {
            synchronized (dq) {
                dq.clearResults();
                engine.assertFact(f, context);
                // Allow backwards chaining to occur
                if (dq.getMaxBackgroundRules() > 0)
                    engine.run(dq.getMaxBackgroundRules());
                Value v;
                if (m_cmd == RUN) {
                    Iterator e = dq.getResults();
                    v = new Value(e);
                } else
                    v = new Value(dq.countResults(), RU.INTEGER);

                dq.clearResults();
                engine.retract(f);
                return v;
            }
        }
    }
}

class JessFormat implements Userfunction, Serializable {
    public String getName() {
        return "format";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Value router = vv.get(1).resolveValue(context);
        String fmt = vv.get(2).stringValue(context);
        Object[] args = new Object[vv.size() - 3];
        for (int i = 0, j = 3; i < args.length; ++i, ++j) {
            Value rawArg = vv.get(j).resolveValue(context);
            Object arg;
            switch (rawArg.type()) {
                case RU.ATOM:
                case RU.STRING:
                    arg = rawArg.stringValue(context);
                    break;
                case RU.INTEGER:
                    arg = new Integer(rawArg.intValue(context));
                    break;
                case RU.FLOAT:
                    arg = new Double(rawArg.floatValue(context));
                    break;
                case RU.LONG:
                    arg = new Long(rawArg.longValue(context));
                    break;
                case RU.EXTERNAL_ADDRESS:
                    arg = rawArg.externalAddressValue(context);
                    break;
                default:
                    arg = rawArg.toString();
                    break;
            }
            args[i] = arg;
        }
        try {
            String result = new PrintfFormat(fmt).sprintf(args);

            if (!router.equals(Funcall.NIL)) {
                String routerName = router.stringValue(context);
                Writer os = context.getEngine().getOutputRouter(routerName);
                if (os == null)
                    throw new JessException("format",
                            "Bad router", routerName);
                try {
                    os.write(result);
                    os.flush();
                } catch (IOException ioe) {
                    throw new JessException("format", "I/O Exception", ioe);
                }
            }

            return new Value(result, RU.STRING);
        } catch (IllegalArgumentException iae) {
            throw new JessException("format", "Bad format string " + fmt, iae);

        }
    }
}

class JessLong implements Userfunction, Serializable {
    public String getName() {
        return "long";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        long l;
        Value v = vv.get(1).resolveValue(context);
        switch (v.type()) {
            case RU.STRING:
            case RU.ATOM:
                try {
                    l = new Long(v.stringValue(context)).longValue();
                    break;
                } catch (NumberFormatException nfe) {
                    throw new JessException("long", "Invalid number format", v.toString());
                }
            case RU.INTEGER:
            case RU.FLOAT:
            case RU.FACT:
                l = (long) v.numericValue(context);
                break;
            default:
                throw new JessException("long", "Illegal argument", v.toString());
        }

        return new LongValue(l);
    }
}

/**
 * Calls a function on another engine from Jess code.
 * Note that the current variable context is used, so (for instance)
 * all defglobal values will be from the calling engine, not the target.
 */

class CallOnEngine implements Userfunction, Serializable {
    public String getName() {
        return "call-on-engine";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Rete r = (Rete) vv.get(1).externalAddressValue(context);
        Value v = Funcall.NIL;
        Context hybrid = new Context(context, r);
        for (int i = 2; i < vv.size(); i++) {
            Funcall f = vv.get(i).funcallValue(hybrid);
            v = f.execute(hybrid);
        }

        return v.resolveValue(hybrid);
    }

}

/**
 * Returns the FactIDValue corresponding to a given numeric fact-id.
 */

class MakeFactID implements Userfunction, Serializable {
    public String getName() {
        return "fact-id";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        int index = (int) vv.get(1).numericValue(context);
        Fact f = context.getEngine().findFactByID(index);
        if (f == null)
            throw new JessException("factid", "No such fact-id:", index);
        else
            return new FactIDValue(f);
    }
}

/**
 * Returns the ASCII value of the first character of the String argument
 */

class Asc implements Userfunction, Serializable {
    public String getName() {
        return "asc";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        int index = (int) vv.get(1).stringValue(context).charAt(0);
        return new Value(index, RU.INTEGER);
    }
}

/**
 * Lock the first argument, and evaluate all the others, returning the
 * value of the last one.
 */


class Synchronized implements Userfunction, Serializable {
    public String getName() {
        return "synchronized";
    }

    public Value call(ValueVector vv, Context c) throws JessException {
        Object lock = vv.get(1).externalAddressValue(c);
        Value rv = null;
        synchronized (lock) {
            for (int i = 2; i < vv.size(); ++i) {
                rv = vv.get(i).resolveValue(c);
                if (c.returning()) {
                    rv = c.getReturnValue();
                    break;
                }
            }
        }
        return rv;
    }
}

class Dependents implements Userfunction, Serializable {
    public String getName() {
        return "dependents";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Rete engine = context.getEngine();
        Value arg = vv.get(1).resolveValue(context);
        Fact fact;
        if (arg.type() == RU.INTEGER)
            fact = engine.findFactByID(arg.intValue(context));
        else
            fact = arg.factValue(context);

        ValueVector vector = new ValueVector();
        ArrayList list = engine.getSupportedFacts(fact);
        for (int i = 0; i < list.size(); ++i) {
            Fact dependent = (Fact) list.get(i);
            vector.add(new FactIDValue(dependent));
        }
        return new Value(vector, RU.LIST);
    }
}

class Dependencies implements Userfunction, Serializable {
    public String getName() {
        return "dependencies";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Rete engine = context.getEngine();
        Value arg = vv.get(1).resolveValue(context);
        Fact fact;
        if (arg.type() == RU.INTEGER)
            fact = engine.findFactByID(arg.intValue(context));
        else
            fact = arg.factValue(context);

        ValueVector vector = new ValueVector();
        ArrayList list = engine.getSupportingTokens(fact);
        if (list != null)
            for (int i = 0; i < list.size(); ++i) {
                Object token = list.get(i);
                vector.add(new Value(token));
            }
        return new Value(vector, RU.LIST);
    }
}
