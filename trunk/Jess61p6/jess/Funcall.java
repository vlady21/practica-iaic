package jess;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;

/** **********************************************************************
 * A class for parsing, assembling, and interpreting function calls.
 * <P>
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */

public class Funcall extends ValueVector implements Serializable {

    /**
     * Formats a Funcall as a String
     * @return The pretty-print form of this Funcall
     */
    public String toString() {
        try {
            if (get(0).equals("assert")) {
                List l = new List("assert");
                for (int i = 1; i < size(); i++)
                    l.add(get(i).factValue(null));
                return l.toString();
            } else if (get(0).equals("modify") ||
                    get(0).equals("duplicate")) {
                List l = new List(get(0).atomValue(null));
                l.add(get(1));
                for (int i = 2; i < size(); i++) {
                    ValueVector vv = get(i).listValue(null);
                    List ll = new List(vv.get(0).atomValue(null));
                    for (int j = 1; j < vv.size(); j++)
                        ll.add(vv.get(j));
                    l.add(ll);
                }

                return l.toString();
            }

            return new List(super.toString()).toString();

        } catch (JessException re) {
            return re.toString();
        }
    }

    public String toStringWithParens() {
        return toString();
    }

    /** The object representing the value TRUE */
    public static Value TRUE;
    /** The object representing the value FALSE */
    public static Value FALSE;
    /** The object representing the value NIL */
    public static Value NIL;
    /** An object representing an empty list. */
    public static Value NILLIST;
    /** The object representing end-of-file */
    public static Value EOF;

    static Value s_else;
    static Value s_then;
    static Value s_do;

    private static HashMap m_intrinsics = new HashMap();

    static {
        try {
            TRUE = new Value("TRUE", RU.ATOM);
            FALSE = new Value("FALSE", RU.ATOM);
            NIL = new Value("nil", RU.ATOM);
            NILLIST = new Value(new ValueVector(), RU.LIST);
            EOF = new Value("EOF", RU.ATOM);
            s_else = new Value("else", RU.ATOM);
            s_then = new Value("then", RU.ATOM);
            s_do = new Value("do", RU.ATOM);

            loadIntrinsics();

        } catch (JessException re) {
            System.out.println("*** FATAL ***: Can't initialize Jess");
            re.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Load in all the intrinsic functions
     */

    static Userfunction getIntrinsic(String name) {
        return (Userfunction) m_intrinsics.get(name);
    }

    static Iterator listIntrinsics() {
        return m_intrinsics.values().iterator();
    }

    private static void addIntrinsic(Userfunction uf) {
        m_intrinsics.put(uf.getName(), uf);
    }

    private static void addPackage(IntrinsicPackage ip) {
        ip.add(m_intrinsics);
    }

    private static void loadIntrinsics() throws JessException {

        try {
            addIntrinsic(new Assert());
            addIntrinsic(new Retract());
            addIntrinsic(new Update());
            addIntrinsic(new RetractString());
            addIntrinsic(new DoBackwardChaining());
            addIntrinsic(new Open());
            addIntrinsic(new Close());
            addIntrinsic(new Duplicate());
            addIntrinsic(new Foreach());
            addIntrinsic(new Read());
            addIntrinsic(new Readline());
            addIntrinsic(new GensymStar());
            addIntrinsic(new While());
            addIntrinsic(new If());
            addIntrinsic(new Bind());
            addIntrinsic(new Modify());
            addIntrinsic(new And());
            addIntrinsic(new Or());
            addIntrinsic(new Not());
            addIntrinsic(new Eq());
            addIntrinsic(new EqStar());
            addIntrinsic(new Equals());
            addIntrinsic(new NotEquals());
            addIntrinsic(new Gt());
            addIntrinsic(new Lt());
            addIntrinsic(new GtOrEq());
            addIntrinsic(new LtOrEq());
            addIntrinsic(new Neq());
            addIntrinsic(new Mod());
            addIntrinsic(new Plus());
            addIntrinsic(new Times());
            addIntrinsic(new Minus());
            addIntrinsic(new Divide());
            addIntrinsic(new SymCat());
            addIntrinsic(new LoadFacts());
            addIntrinsic(new SaveFacts());
            addIntrinsic(new AssertString());
            addIntrinsic(new UnDefrule());
            addIntrinsic(new Batch());
            addIntrinsic(new Watch());
            addIntrinsic(new Unwatch());
            addIntrinsic(new JessVersion(JessVersion.NUMBER));
            addIntrinsic(new JessVersion(JessVersion.STRING));

            addIntrinsic(new HaltEtc(HaltEtc.HALT));
            addIntrinsic(new HaltEtc(HaltEtc.EXIT));
            addIntrinsic(new HaltEtc(HaltEtc.CLEAR));
            addIntrinsic(new HaltEtc(HaltEtc.RUN));
            addIntrinsic(new HaltEtc(HaltEtc.RESET));
            addIntrinsic(new HaltEtc(HaltEtc.RETURN));

            addIntrinsic(new StoreFetch(StoreFetch.STORE));
            addIntrinsic(new StoreFetch(StoreFetch.FETCH));
            addIntrinsic(new StoreFetch(StoreFetch.CLEAR_STORAGE));

            addIntrinsic(new Defadvice(Defadvice.ADVICE));
            addIntrinsic(new Defadvice(Defadvice.UNADVICE));

            addIntrinsic(new TryCatchThrow(TryCatchThrow.TRY));
            addIntrinsic(new TryCatchThrow(TryCatchThrow.THROW));

            Printout p = new Printout(Printout.PRINTOUT);
            addIntrinsic(p);
            addIntrinsic(new Printout(Printout.SETMULTI, p));
            addIntrinsic(new Printout(Printout.GETMULTI, p));

            addPackage(new ReflectFunctions());
            addPackage(new StringFunctions());
            addPackage(new PredFunctions());
            addPackage(new MultiFunctions());
            addPackage(new MiscFunctions());
            addPackage(new ModuleFunctions());
            addPackage(new MathFunctions());
            addPackage(new LispFunctions());
            addPackage(new DumpFunctions());
            addPackage(new ReflectFunctions());
            addPackage(new ViewFunctions());
            addPackage(new BagFunctions());

        } catch (Throwable t) {
            t.printStackTrace();
            throw new JessException("Funcall.loadIntrisics",
                    "Missing non-optional function class",
                    t);
        }
    }

    FunctionHolder m_funcall;

    /**
     * Create a Funcall given the name. The Funcall's arguments must
     * then be added using methods inherited from ValueVector.
     *
     * @param name The name of the function
     * @param engine The Rete engine where the function is defined
     * @exception JessException If something goes wrong.
     */

    public Funcall(String name, Rete engine) throws JessException {
        add(new Value(name, RU.ATOM));
        m_funcall = engine.findFunctionHolder(name);
    }

    Funcall(int size) {
        super(size);
    }

    /**
     * Copies a Funcall
     * @return A copy of the Funcall
     */

    public Object clone() {
        return cloneInto(new Funcall(size()));
    }

    /**
     * Makes the argument into a copy of this Funcall.
     * @param vv The FUncall into which the copy should be made
     * @return The argument
     */
    public Funcall cloneInto(Funcall vv) {
        super.cloneInto(vv);
        vv.m_funcall = m_funcall;
        return vv;
    }


    /**
     * Execute this funcall in a particular context.
     *
     * @param context An execution context for the function
     * @exception JessException If something goes wrong
     * @return The result of the function call
     */

    public final Value execute(Context context) throws JessException {
        try {
            if (m_funcall == null) {
                String name = get(0).stringValue(context);

                if ((m_funcall = context.getEngine().findFunctionHolder(name))
                        == null)
                    throw new JessException("Funcall.execute",
                            "Unimplemented function",
                            name);
            }
            context.getEngine().
                    broadcastEvent(JessEvent.USERFUNCTION_CALLED,
                            m_funcall.getFunction());

            return m_funcall.call(this, context);

        } catch (JessException re) {
            re.addContext(toStringWithParens());
            throw re;

        } catch (Exception e) {
            JessException re = new JessException("Funcall.execute",
                    "Error during execution",
                    e);

            re.addContext(toStringWithParens());
            throw re;

        }
    }

    /**
     * Calls add(v), then returns this object
     * @param v An argument to add to this Funcall
     * @return This Funcall
     * @see jess.ValueVector#add
     */
    public Funcall arg(Value v) {
        add(v);
        return this;
    }
}

/**
 * *** assert  ***
 * @author Ernest J. Friedman-Hill
 */
class Assert implements Userfunction, Serializable {
    public String getName() {
        return "assert";
    }

    public Value call(ValueVector vvec, Context context)
            throws JessException {
        Fact result = null;
        Rete engine = context.getEngine();
        for (int i = 1; i < vvec.size(); i++) {
            Fact fact = vvec.get(i).factValue(context).expand(context);
            result = engine.assertFact(fact, context);
        }
        if (result != null)
            return new FactIDValue(result);
        else
            return Funcall.FALSE;
    }
}

/**
 * *** update  ***
 * @author Ernest J. Friedman-Hill
 */
class Update implements Userfunction, Serializable {
    public String getName() {
        return "update";
    }

    public Value call(ValueVector vvec, Context context)
            throws JessException {
        Value result = null;
        Rete engine = context.getEngine();
        for (int i = 1; i < vvec.size(); i++) {
            Object o = vvec.get(i).externalAddressValue(context);
            result = engine.updateObject(o);
        }
        if (result != null)
            return result;
        else
            return Funcall.FALSE;
    }
}

/**
 * *** retract ***
 * @author Ernest J. Friedman-Hill
 */

class Retract implements Userfunction, Serializable {
    public String getName() {
        return "retract";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Value v = vv.get(1);
        if (v.type() == RU.ATOM && v.stringValue(context).equals("*")) {
            context.getEngine().removeAllFacts();

        } else {
            Rete engine = context.getEngine();
            for (int i = 1; i < vv.size(); i++) {
                Value fv = vv.get(i).resolveValue(context);
                Fact fact;
                if (fv.type() == RU.INTEGER)
                    fact = engine.findFactByID(fv.intValue(context));
                else
                    fact = (Fact) fv.externalAddressValue(context);
                if (fact != null)
                    engine.retract(fact);
                else
                    return Funcall.FALSE;
            }
        }
        return Funcall.TRUE;
    }
}

/**
 * *** printout ***
 * @author Ernest J. Friedman-Hill
 */


class PrintThread extends Thread {
    private static PrintThread s_printThread;

    static {
        s_printThread = new PrintThread();
        s_printThread.setDaemon(true);
        s_printThread.start();
    }

    static PrintThread getPrintThread() {
        return s_printThread;
    }

    private Writer m_os;

    synchronized void assignWork(Writer os) {
        m_os = os;
        notify();
    }

    public synchronized void run() {
        while (true) {
            try {
                while (m_os == null)
                    wait();
                try {
                    m_os.flush();
                } catch (IOException ioe) {
                } finally {
                    m_os = null;
                }
            } catch (InterruptedException ie) {
                break;
            }
            notifyAll();
        }
    }

    // Must return a value so it is not inlined and optimized away!
    synchronized int waitForCompletion() {
        return 1;
    }
}


class Printout implements Userfunction, Serializable {
    private boolean m_multithreadedIO = false;
    private int m_name;
    private Printout m_printout;
    private static String NEWLINE = System.getProperty("line.separator");

    static final int PRINTOUT = 0, SETMULTI = 1, GETMULTI = 2;
    private static final String[] s_names =
            new String[]{"printout", "set-multithreaded-io", "get-multithreaded-io"};

    Printout(int name) {
        m_name = name;
    }

    Printout(int name, Printout p) {
        m_name = name;
        m_printout = p;
    }

    public String getName() {
        return s_names[m_name];
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        switch (m_name) {
            case SETMULTI:
                boolean tmp = m_printout.m_multithreadedIO;
                m_printout.m_multithreadedIO =
                        !(vv.get(1).equals(Funcall.FALSE));
                return tmp ? Funcall.TRUE : Funcall.FALSE;

            case GETMULTI:
                return m_printout.m_multithreadedIO ?
                        Funcall.TRUE : Funcall.FALSE;

            case PRINTOUT:
            default:

                String routerName = vv.get(1).stringValue(context);
                Writer os = context.getEngine().getOutputRouter(routerName);
                if (os == null)
                    throw new JessException("printout",
                            "printout: bad router",
                            routerName);

                StringBuffer sb = new StringBuffer(100);
                for (int i = 2; i < vv.size(); i++) {
                    Value v = vv.get(i).resolveValue(context);
                    switch (v.type()) {
                        case RU.ATOM:
                            if (v.equals("crlf")) {
                                sb.append(NEWLINE);
                                break;
                            }

                            // FALL THROUGH
                        case RU.STRING:
                            sb.append(v.stringValue(context));
                            break;
                        case RU.INTEGER:
                            sb.append(v.intValue(context));
                            break;
                        case RU.FLOAT:
                            sb.append(v.numericValue(context));
                            break;
                        case RU.FACT:
                            sb.append(v);
                            break;
                        case RU.LIST:
                            sb.append(v.listValue(context).
                                    toStringWithParens());
                            break;
                        case RU.EXTERNAL_ADDRESS:
                            sb.append(v.toString());
                            break;
                        default:
                            sb.append(v.toString());
                    }

                }
                try {
                    os.write(sb.toString());
                    if (m_multithreadedIO)
                        PrintThread.getPrintThread().assignWork(os);
                    else
                        os.flush();
                } catch (IOException ioe) {
                    throw new JessException("printout", "I/O Exception", ioe);
                }

                return Funcall.NIL;
        }
    }
}

/**
 * *** open ***
 * @author Ernest J. Friedman-Hill
 */
class Open implements Userfunction, Serializable {
    public String getName() {
        return "open";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Rete engine = context.getEngine();

        // Obtain parameters
        String filename = vv.get(1).stringValue(context);
        String router = vv.get(2).stringValue(context);
        String access = "r";
        if (vv.size() > 3)
            access = vv.get(3).stringValue(context);

        try {
            if (access.equals("r")) {
                engine.addInputRouter(router,
                        new BufferedReader(new FileReader(filename)),
                        false);

            } else if (access.equals("w")) {
                engine.addOutputRouter(router,
                        new BufferedWriter(new FileWriter(filename)));

            } else if (access.equals("a")) {
                RandomAccessFile raf = new RandomAccessFile(filename, "rw");
                raf.seek(raf.length());
                FileWriter fos = new FileWriter(raf.getFD());
                engine.addOutputRouter(router, new BufferedWriter(fos));
            } else
                throw new JessException("open", "Unsupported access mode",
                        access);

        } catch (IOException ioe) {
            throw new JessException("open", "I/O Exception", ioe);
        }
        return new Value(router, RU.ATOM);
    }
}

/**
 * *** close ***
 * @author Ernest J. Friedman-Hill
 */
class Close implements Userfunction, Serializable {
    public String getName() {
        return "close";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Rete engine = context.getEngine();
        if (vv.size() > 1)
            for (int i = 1; i < vv.size(); i++) {
                Writer os;
                Reader is;
                String router = vv.get(i).stringValue(context);
                try {
                    if ((os = engine.getOutputRouter(router)) != null) {
                        os.close();
                        engine.removeOutputRouter(router);
                    }
                } catch (IOException ioe) {
                }
                try {
                    if ((is = engine.getInputRouter(router)) != null) {
                        is.close();
                        engine.removeInputRouter(router);
                    }
                } catch (IOException ioe) {
                }
            }
        else
            throw new JessException("close", "Must close files by name", "");

        return Funcall.TRUE;
    }
}

/**
 * *** read ***
 * @author Ernest J. Friedman-Hill
 */
class Read implements Userfunction, Serializable {

    public String getName() {
        return "read";
    }

    public Value call(ValueVector vv, Context context) throws JessException {

        // Find input source
        String routerName = "t";

        if (vv.size() > 1)
            routerName = vv.get(1).stringValue(context);

        Rete engine = context.getEngine();
        Tokenizer t = engine.getInputWrapper(engine.getInputRouter(routerName));

        if (t == null)
            throw new JessException("read", "bad router", routerName);
        JessToken jt = t.nextToken();

        // Console-like streams read a token, then throw away to newline.
        if (engine.getInputMode(routerName))
            t.discardToEOL();

        return jt.tokenToValue(null);
    }

}

/**
 * *** readline  ***
 * @author Ernest J. Friedman-Hill
 */

class Readline implements Userfunction, Serializable {
    public String getName() {
        return "readline";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        String routerName = "t";

        if (vv.size() > 1)
            routerName = vv.get(1).stringValue(context);

        Rete engine = context.getEngine();
        Tokenizer t = engine.getInputWrapper(engine.getInputRouter(routerName));

        String line = t.readLine();
        if (line == null)
            return Funcall.EOF;
        else
            return new Value(line, RU.STRING);
    }
}

/**
 * *** gensym*  ***
 * @author Ernest J. Friedman-Hill
 */

class GensymStar implements Userfunction, Serializable {
    public String getName() {
        return "gensym*";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        return new Value(RU.gensym("gen"), RU.ATOM);
    }
}

/**
 * *** while ***
 * @author Ernest J. Friedman-Hill
 */

class While implements Userfunction, Serializable {

    public String getName() {
        return "while";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        // This accepts a superset of the correct syntax...
        Value result = vv.get(1).resolveValue(context);

        // Skip optional do
        int sawDo = 0;
        if (vv.get(2).equals(Funcall.s_do))
            ++sawDo;

        outer_loop:
            while (!result.equals(Funcall.FALSE)) {
                for (int i = 2 + sawDo; i < vv.size(); i++) {
                    result = vv.get(i).resolveValue(context);
                    if (context.returning()) {
                        result = context.getReturnValue();
                        break outer_loop;
                    }

                }

                result = vv.get(1).resolveValue(context);

            }
        return result;
    }
}

/**
 * *** if ***
 * @author Ernest J. Friedman-Hill
 */

class If implements Userfunction, Serializable {
    public String getName() {
        return "if";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        // This accepts a superset of the correct syntax...

        if (!vv.get(2).equals(Funcall.s_then))
            throw new JessException("if", "Expected 'then':", vv.get(2).toString());

        // check condition
        Value result = vv.get(1).resolveValue(context);

        if (!(result.equals(Funcall.FALSE))) {
            // do 'then' part
            result = Funcall.FALSE;
            for (int i = 3; i < vv.size(); i++) {
                Value val = vv.get(i).resolveValue(context);

                if (val.equals(Funcall.s_else))
                    break;

                if (context.returning()) {
                    result = context.getReturnValue();
                    break;
                }

                result = val;
            }
            return result;
        } else {
            // first find the 'else'
            result = Funcall.FALSE;
            boolean seen_else = false;
            for (int i = 3; i < vv.size(); i++) {
                if (!seen_else) {
                    if (vv.get(i).equals(Funcall.s_else))
                        seen_else = true;

                    continue;
                }

                Value val = vv.get(i).resolveValue(context);

                if (context.returning()) {
                    result = context.getReturnValue();
                    break;
                }

                result = val;
            }

            return result;
        }
    }
}

/**
 * *** bind ***
 * @author Ernest J. Friedman-Hill
 */

class Bind implements Userfunction, Serializable {
    public String getName() {
        return "bind";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Value rv = vv.get(2).resolveValue(context);
        context.setVariable(vv.get(1).variableValue(context), rv);
        return rv;
    }
}

/**
 * *** foreach ***
 * @author Ernest J. Friedman-Hill
 */

class Foreach implements Userfunction, Serializable {

    public String getName() {
        return "foreach";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        String variable = vv.get(1).variableValue(context);
        ValueVector items = vv.get(2).listValue(context);
        Value v = Funcall.NIL;

        for (int i = 0; i < items.size(); i++) {
            context.setVariable(variable, items.get(i).resolveValue(context));
            for (int j = 3; j < vv.size(); j++) {
                v = vv.get(j).resolveValue(context);
                if (context.returning()) {
                    v = context.getReturnValue();
                    return v;
                }
            }
        }
        return v;
    }
}

/**
 * *** try, catch, throw ***
 * @author Ernest J. Friedman-Hill
 * @author Thomas Barnekow
 */

class TryCatchThrow implements Userfunction, Serializable {
    public static final String TRY = "try", THROW = "throw";
    private String m_name;

    TryCatchThrow(String s) {
        m_name = s;
    }

    public String getName() {
        return m_name;
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        if (m_name.equals(THROW)) {
            Throwable t =
                    (Throwable) vv.get(1).externalAddressValue(context);
            t.fillInStackTrace();

            if (t instanceof JessException)
                throw (JessException) t;
            else {
                throw new JessException("throw",
                        "Exception thrown from Jess language code",
                        t);
            }
        }

        // Else name is "Try." First find catch and/or finally
        int catchKeyword = -1;      // index of catch keyword
        int finallyKeyword = -1;    // index of finally keyword
        int endOfTryBlock = -1;
        int endOfCatchBlock = -1;
        int endOfFinallyBlock = -1;

        for (int j = 1; j < vv.size(); j++) {

            if (vv.get(j).type() == RU.ATOM &&
                    vv.get(j).equals("catch") &&
                    catchKeyword < 0) {

                // try/catch + possibly finally
                catchKeyword = j;
                endOfTryBlock = catchKeyword;
                // Set default
                endOfCatchBlock = vv.size();
            }

            if (vv.get(j).type() == RU.ATOM &&
                    vv.get(j).equals("finally")) {

                finallyKeyword = j;
                endOfFinallyBlock = vv.size();

                if (catchKeyword > 0) {

                    // try/catch/finally
                    endOfCatchBlock = finallyKeyword;
                } else {
                    // try/finally
                    endOfTryBlock = finallyKeyword;
                }

                break;
            }
        }

        if (catchKeyword == -1 && finallyKeyword == -1)
            throw new JessException("try",
                    "Neither catch nor finally block in try expression", "");

        Value v = Funcall.NIL;

        try {
            for (int j = 1; j < endOfTryBlock; j++) {
                v = vv.get(j).resolveValue(context);
                if (context.returning()) {
                    v = context.getReturnValue();
                    break;
                }
            }
        } catch (Throwable t) {
            // Rethrow if there is no catch block
            if (catchKeyword == -1) {
                if (t instanceof JessException) {
                    throw (JessException) t;
                } else {
                    throw new JessException("TryCatchThrow.call",
                            "Unexpected exception in try-block",
                            t);
                }
            }

            v = Funcall.FALSE; // so we can have empty handlers
            context.setVariable("ERROR", new Value(t));

            for (int j = ++catchKeyword; j < endOfCatchBlock; j++) {
                v = vv.get(j).resolveValue(context);

                if (context.returning()) {
                    v = context.getReturnValue();
                    break;
                }
            }
        } finally {
            Value fv;
            boolean wasReturning = context.returning();
            context.clearReturnValue();
            for (int j = ++finallyKeyword; j < endOfFinallyBlock; j++) {
                fv = vv.get(j).resolveValue(context);

                if (context.returning()) {
                    fv = context.getReturnValue();
                    return fv;
                }
            }
            if (wasReturning)
                context.setReturnValue(v);
        }

        // Return a value if there is no uncaught exception
        return v;
    }
}

/**
 * *** modify  ***
 * @author Ernest J. Friedman-Hill
 */

class Modify implements Userfunction, Serializable {

    public String getName() {
        return "modify";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Fact f = context.getEngine()._modify(vv, context);
        return new FactIDValue(f);
    }
}

/**
 * *** duplicate ***
 * @author Ernest J. Friedman-Hill
 */

class Duplicate implements Userfunction, Serializable {

    public String getName() {
        return "duplicate";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Fact f = context.getEngine()._duplicate(vv, context);
        if (f == null)
            return Funcall.FALSE;
        else
            return new FactIDValue(f);
    }
}

/**
 * *** and ***
 * @author Ernest J. Friedman-Hill
 */

class And implements Userfunction, Serializable {

    public String getName() {
        return "and";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        for (int i = 1; i < vv.size(); i++) {
            Value v = vv.get(i).resolveValue(context);

            if (v.equals(Funcall.FALSE))
                return Funcall.FALSE;
        }

        return Funcall.TRUE;
    }
}

/**
 * *** or ***
 * @author Ernest J. Friedman-Hill
 */
class Or implements Userfunction, Serializable {

    public String getName() {
        return "or";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        for (int i = 1; i < vv.size(); i++) {
            Value v = vv.get(i).resolveValue(context);

            if (!v.equals(Funcall.FALSE))
                return Funcall.TRUE;
        }
        return Funcall.FALSE;
    }
}

/**
 * *** not ***
 * @author Ernest J. Friedman-Hill
 */

class Not implements Userfunction, Serializable {

    public String getName() {
        return "not";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        if (vv.get(1).resolveValue(context).equals(Funcall.FALSE))
            return Funcall.TRUE;
        else
            return Funcall.FALSE;
    }
}

/**
 * *** eq ***
 * @author Ernest J. Friedman-Hill
 */

class Eq implements Userfunction, Serializable {

    public String getName() {
        return "eq";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Value first = vv.get(1).resolveValue(context);
        for (int i = 2; i < vv.size(); i++) {
            if (!vv.get(i).resolveValue(context).equals(first))
                return Funcall.FALSE;
        }
        return Funcall.TRUE;
    }
}

/**
 * *** eq* ***
 * @author Ernest J. Friedman-Hill
 */

class EqStar implements Userfunction, Serializable {

    public String getName() {
        return "eq*";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Value first = vv.get(1).resolveValue(context);
        for (int i = 2; i < vv.size(); i++) {
            if (!vv.get(i).resolveValue(context).equalsStar(first))
                return Funcall.FALSE;
        }
        return Funcall.TRUE;
    }
}

/**
 * *** = ***
 * @author Ernest J. Friedman-Hill
 */


class Equals implements Userfunction, Serializable {

    public String getName() {
        return "=";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        for (int i = 2; i < vv.size(); i++) {
            if (!(vv.get(i).numericValue(context) == vv.get(1).numericValue(context)))
                return Funcall.FALSE;
        }
        return Funcall.TRUE;
    }
}

/**
 * *** <> ***
 * @author Ernest J. Friedman-Hill
 */

class NotEquals implements Userfunction, Serializable {

    public String getName() {
        return "<>";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        for (int i = 2; i < vv.size(); i++) {
            if (vv.get(i).numericValue(context) == vv.get(1).numericValue(context))
                return Funcall.FALSE;
        }
        return Funcall.TRUE;
    }
}

/**
 * *** > ***
 * @author Ernest J. Friedman-Hill
 */

class Gt implements Userfunction, Serializable {

    public String getName() {
        return ">";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        for (int i = 1; i < vv.size() - 1; i++) {
            double value1 = vv.get(i).numericValue(context);
            double value2 = vv.get(i + 1).numericValue(context);

            if (!(value1 > value2))
                return Funcall.FALSE;
        }
        return Funcall.TRUE;
    }
}

/**
 * *** < ***
 * @author Ernest J. Friedman-Hill
 */

class Lt implements Userfunction, Serializable {

    public String getName() {
        return "<";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        for (int i = 1; i < vv.size() - 1; i++) {
            double value1 = vv.get(i).numericValue(context);
            double value2 = vv.get(i + 1).numericValue(context);

            if (!(value1 < value2))
                return Funcall.FALSE;
        }
        return Funcall.TRUE;
    }
}

/**
 * *** >= ***
 * @author Ernest J. Friedman-Hill
 */

class GtOrEq implements Userfunction, Serializable {

    public String getName() {
        return ">=";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        for (int i = 1; i < vv.size() - 1; i++) {
            double value1 = vv.get(i).numericValue(context);
            double value2 = vv.get(i + 1).numericValue(context);

            if (!(value1 >= value2))
                return Funcall.FALSE;
        }
        return Funcall.TRUE;
    }
}

/**
 * *** <= ***
 * @author Ernest J. Friedman-Hill
 */
class LtOrEq implements Userfunction, Serializable {

    public String getName() {
        return "<=";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        for (int i = 1; i < vv.size() - 1; i++) {
            double value1 = vv.get(i).numericValue(context);
            double value2 = vv.get(i + 1).numericValue(context);

            if (!(value1 <= value2))
                return Funcall.FALSE;
        }
        return Funcall.TRUE;
    }
}

/**
 * *** neq ***
 * @author Ernest J. Friedman-Hill
 */

class Neq implements Userfunction, Serializable {

    public String getName() {
        return "neq";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Value first = vv.get(1).resolveValue(context);
        for (int i = 2; i < vv.size(); i++) {
            if (vv.get(i).resolveValue(context).equals(first))
                return Funcall.FALSE;
        }
        return Funcall.TRUE;
    }
}

/**
 * *** mod ***
 * @author Ernest J. Friedman-Hill
 */

class Mod implements Userfunction, Serializable {

    public String getName() {
        return "mod";
    }

    public Value call(ValueVector vv, Context context)
            throws JessException {
        int d1 = (int) vv.get(1).numericValue(context);
        int d2 = (int) vv.get(2).numericValue(context);

        return new Value(d1 % d2, RU.INTEGER);

    }
}

/**
 * *** + ***
 * @author Ernest J. Friedman-Hill
 */
class Plus implements Userfunction, Serializable {

    public String getName() {
        return "+";
    }

    public Value call(ValueVector vv, Context context)
            throws JessException {
        double sum = 0;
        int type = RU.INTEGER;
        int size = vv.size();
        for (int i = 1; i < size; i++) {
            Value arg = vv.get(i).resolveValue(context);
            sum += arg.numericValue(context);
            if (arg.type() == RU.FLOAT)
                type = RU.FLOAT;

        }

        return new Value(sum, type);
    }
}

/**
 * *** * ***
 * @author Ernest J. Friedman-Hill
 */

class Times implements Userfunction, Serializable {

    public String getName() {
        return "*";
    }

    public Value call(ValueVector vv, Context context)
            throws JessException {
        double product = 1;
        int type = RU.INTEGER;
        int size = vv.size();
        for (int i = 1; i < size; i++) {
            Value arg = vv.get(i).resolveValue(context);
            product *= arg.numericValue(context);
            if (arg.type() == RU.FLOAT)
                type = RU.FLOAT;
        }

        return new Value(product, type);

    }
}

/**
 * *** - ***
 * @author Ernest J. Friedman-Hill
 */
class Minus implements Userfunction, Serializable {
    public String getName() {
        return "-";
    }

    public Value call(ValueVector vv, Context context)
            throws JessException {
        Value arg = vv.get(1).resolveValue(context);
        int type = (arg.type() == RU.FLOAT) ? RU.FLOAT : RU.INTEGER;
        double diff = arg.numericValue(context);
        int size = vv.size();
        for (int i = 2; i < size; i++) {
            arg = vv.get(i).resolveValue(context);
            diff -= arg.numericValue(context);
            if (arg.type() == RU.FLOAT)
                type = RU.FLOAT;
        }
        return new Value(diff, type);
    }
}

/**
 * *** / ***
 * @author Ernest J. Friedman-Hill
 */
class Divide implements Userfunction, Serializable {

    public String getName() {
        return "/";
    }

    public Value call(ValueVector vv, Context context)
            throws JessException {
        double quotient = vv.get(1).numericValue(context);
        int size = vv.size();
        for (int i = 2; i < size; i++) {
            quotient /= vv.get(i).numericValue(context);
        }
        return new Value(quotient, RU.FLOAT);


    }
}

/**
 * *** sym-cat ***
 * @author Ernest J. Friedman-Hill
 */
class SymCat implements Userfunction, Serializable {

    public String getName() {
        return "sym-cat";
    }

    public Value call(ValueVector vv, Context context)
            throws JessException {

        StringBuffer buf = new StringBuffer("");
        for (int i = 1; i < vv.size(); i++) {
            Value val = vv.get(i).resolveValue(context);
            if (val.type() == RU.STRING)
                buf.append(val.stringValue(context));
            else if (val.type() == RU.EXTERNAL_ADDRESS)
                buf.append(val.externalAddressValue(context).toString());
            else
                buf.append(val.toString());
        }

        return new Value(buf.toString(), RU.ATOM);
    }
}

/**
 * *** store, fetch **
 * @author Ernest J. Friedman-Hill
 */

class StoreFetch implements Userfunction, Serializable {
    static final int STORE = 0, FETCH = 1, CLEAR_STORAGE = 2;
    static final String[] s_names = {"store", "fetch", "clear-storage"};
    private int m_name;

    StoreFetch(int name) {
        m_name = name;
    }

    public String getName() {
        return s_names[m_name];
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Value v;
        switch (m_name) {
            case STORE:
                Value val = vv.get(2).resolveValue(context);
                if (val.equals(Funcall.NIL))
                    val = null;
                v = context.getEngine().store(vv.get(1).stringValue(context), val);

                if (v != null)
                    return v;
                else
                    return Funcall.NIL;

            case CLEAR_STORAGE:
                context.getEngine().clearStorage();
                return Funcall.TRUE;

            case FETCH:
            default:
                v = context.getEngine().fetch(vv.get(1).stringValue(context));
                if (v != null)
                    return v.resolveValue(context);
                else
                    return Funcall.NIL;
        }
    }
}

/**
 * *** HaltEtc ***
 * @author Ernest J. Friedman-Hill
 */
class HaltEtc implements Userfunction, Serializable {
    static final int HALT = 0, EXIT = 1, CLEAR = 2, RUN = 3, RESET = 4, RETURN = 5;
    static final String[] s_names = {"halt", "exit", "clear", "run",
                                     "reset", "return"};
    private int m_name;

    HaltEtc(int name) {
        m_name = name;
    }

    public String getName() {
        return s_names[m_name];
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Rete engine = context.getEngine();
        switch (m_name) {
            case HALT:
                engine.halt();
                break;
            case EXIT:
                PrintThread.getPrintThread().waitForCompletion();
                System.exit(0);
                break;
            case CLEAR:
                engine.clear();
                break;
            case RUN:
                if (vv.size() == 1)
                    return new Value(engine.run(), RU.INTEGER);
                else
                    return new Value(engine.run(vv.get(1).intValue(context)),
                            RU.INTEGER);
            case RETURN:
                {
                    if (vv.size() > 1)
                        return context.setReturnValue(vv.get(1).
                                resolveValue(context));
                    else
                        return context.setReturnValue(Funcall.NIL);
                }
            case RESET:
                engine.reset();
                break;
        }
        return Funcall.TRUE;
    }
}

/**
 * *** unwatch ***
 * @author Ernest J. Friedman-Hill
 */

class Unwatch implements Userfunction, Serializable, WatchConstants {

    public String getName() {
        return "unwatch";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        String what = vv.get(1).stringValue(context);
        Rete engine = context.getEngine();

        if (what.equals("rules"))
            engine.unwatch(RULES);

        else if (what.equals("facts"))
            engine.unwatch(FACTS);

        else if (what.equals("activations"))
            engine.unwatch(ACTIVATIONS);

        else if (what.equals("compilations"))
            engine.unwatch(COMPILATIONS);

        else if (what.equals("focus"))
            engine.unwatch(FOCUS);

        else if (what.equals("all"))
            engine.unwatchAll();
        else
            throw new JessException("unwatch",
                    "unwatch: can't unwatch", what);

        return Funcall.TRUE;
    }
}

/**
 * *** watch ***
 * @author Ernest J. Friedman-Hill
 */

class Watch implements Userfunction, Serializable, WatchConstants {

    public String getName() {
        return "watch";
    }

    // Note that the ordering of things (when installListener, THEN
    // set flag, but unset, then remove) is carefully orchestrated. Be
    // careful when modifying.

    public Value call(ValueVector vv, Context context) throws JessException {
        String what = vv.get(1).stringValue(context);
        Rete engine = context.getEngine();

        if (what.equals("rules"))
            engine.watch(RULES);

        else if (what.equals("facts"))
            engine.watch(FACTS);

        else if (what.equals("activations"))
            engine.watch(ACTIVATIONS);

        else if (what.equals("compilations"))
            engine.watch(COMPILATIONS);

        else if (what.equals("focus"))
            engine.watch(FOCUS);

        else if (what.equals("all"))
            engine.watchAll();

        else
            throw new JessException("watch",
                    "watch: can't watch/unwatch", what);

        return Funcall.TRUE;
    }



    public String toString() {
        return "[The watch command]";
    }
}

/**
 * *** jess versions  ***
 */


class JessVersion implements Userfunction, Serializable {
    static final int NUMBER = 0, STRING = 1;
    static final String[] s_names = {"jess-version-number",
                                     "jess-version-string"};
    private int m_name;

    JessVersion(int name) {
        m_name = name;
    }

    public String getName() {
        return s_names[m_name];
    }

    public Value call(ValueVector vv, Context context)
            throws JessException {
        switch (m_name) {
            case NUMBER:
                return new Value(6.1, RU.FLOAT);
            default:
                return new Value("Jess Version 6.1p6 11/21/2003", RU.STRING);
        }
    }
}

/**
 * *** load-facts ***
 * @author Ernest J. Friedman-Hill
 */

class LoadFacts implements Userfunction, Serializable {
    public String getName() {
        return "load-facts";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Reader fis;
        Rete engine = context.getEngine();
        String filename = vv.get(1).stringValue(context);
        // ###
        try {
            if (engine.getApplet() == null)
                fis = new FileReader(filename);
            else {
                URL url = new URL(engine.getApplet().getDocumentBase(),
                        filename);
                fis = new InputStreamReader(url.openStream());
            }
        } catch (Exception e) {
            try {
                // Try to find a resource file, too.
                URL u = engine.getResource(filename);
                if (u == null)
                    throw new JessException("load-facts",
                            "Cannot open file", e);
                InputStream is = u.openStream();
                fis = new InputStreamReader(is);

            } catch (IOException ioe) {
                throw new JessException("load-facts",
                        "Network error", ioe);
            }
        }

        Jesp jesp = new Jesp(fis, context.getEngine());
        return jesp.loadFacts(context);
    }
}


class SaveFacts implements Userfunction, Serializable {
    public String getName() {
        return "save-facts";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        Writer f;
        Rete engine = context.getEngine();
        if (engine.getApplet() == null) {
            try {
                f = new FileWriter(vv.get(1).stringValue(context));
            } catch (IOException t) {
                throw new JessException("save-facts", "I/O Exception", t);
            }

        } else {
            try {
                URL url =
                        new URL(engine.getApplet().getDocumentBase(),
                                vv.get(1).stringValue(context));
                URLConnection urlc = url.openConnection();
                urlc.setDoOutput(true);
                f = new OutputStreamWriter(urlc.getOutputStream());

            } catch (Exception t) {
                throw new JessException("save-facts", "Network error", t);
            }
        }

        try {
            try {
                if (vv.size() > 2) {
                    for (int i = 2; i < vv.size(); i++)
                        engine.ppFacts(vv.get(i).stringValue(context), f);

                } else {
                    engine.ppFacts(f);
                }
            } finally {
                f.close();
            }

        } catch (IOException ioe) {
            throw new JessException("save-facts", "I/O Exception", ioe);
        }
        return Funcall.TRUE;
    }
}

class AssertString implements Userfunction, Serializable {
    public String getName() {
        return "assert-string";
    }

    public Value call(ValueVector vv, Context context)
            throws JessException {
        String factString = vv.get(1).stringValue(context);
        Fact fact = context.getEngine().assertString(factString, context);
        if (fact != null)
            return new FactIDValue(fact);
        else
            return Funcall.FALSE;
    }
}

/**
 * Karl Mueller NASA/GSFC Code 522.2
 * (Karl.R.Mueller@gsfc.nasa.gov)
 * 26.January.1998
 *
 * *** retract-string ***
 * Added function to retract fact as a string
 *  * @author Ernest J. Friedman-Hill
 */

class RetractString implements Userfunction, Serializable {
    public String getName() {
        return "retract-string";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        for (int i = 1; i < vv.size(); i++) {
            context.getEngine().retractString(vv.get(i).stringValue(context));
        }
        return Funcall.TRUE;
    }
}

class UnDefrule implements Userfunction, Serializable {
    public String getName() {
        return "undefrule";
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        String rulename = vv.get(1).stringValue(context);
        return context.getEngine().unDefrule(rulename);

    }

}

/**
 *
 * Do backward-chaining (goal-seeking) for a particular deftemplate.
 *
 * @author Ernest J. Friedman-Hill
 */

class DoBackwardChaining implements Userfunction, Serializable {
    public String getName() {
        return "do-backward-chaining";
    }

    public Value call(ValueVector vv, Context context)
            throws JessException {
        String name = vv.get(1).stringValue(context);
        if (name.equals("test") || Group.isGroupName(name))
            throw new JessException("do-backward-chaining",
                    "Can't backchain on special CEs", name);
        Deftemplate dt = context.getEngine().findDeftemplate(name);
        if (dt == null)
            dt = context.getEngine().createDeftemplate(name);

        dt.doBackwardChaining();
        Deftemplate newDt = dt.getBackchainingTemplate(context.getEngine());
        newDt.forgetParent();
        context.getEngine().addDeftemplate(newDt);
        return Funcall.TRUE;
    }

}


