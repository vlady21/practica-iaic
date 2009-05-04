package jess;

import jess.awt.TextReader;
import jess.factory.Factory;
import jess.factory.FactoryImpl;

import java.applet.Applet;
import java.io.*;
import java.util.*;
import java.net.URL;

/** **********************************************************************
 * The reasoning engine and the central class in the Jess library.
 * Executes the built Rete network, and coordinates many
 * other activities. Rete is basically a facade for all the other classes
 * in the Jess.
 * <P>
 * (C) 2003 Ernest J. Friedman-Hill and Sandia National Laboratories <BR>
 * $Id: Rete.java,v 1.23.2.3 2003/09/14 12:29:21 ejfried Exp $
 */

public class Rete implements Serializable, JessListener {

    private transient Object m_appObject;
    private Context m_globalContext = new Context(this);
    private transient Routers m_routers = new Routers();
    private transient TextReader m_tis = new TextReader(true);
    private transient Jesp m_jesp = new Jesp(m_tis, this);
    private transient JessEventSupport m_jes = new JessEventSupport(this);
    private boolean m_resetGlobals = true;
    private java.util.List m_deffacts = Collections.synchronizedList(new ArrayList());
    private java.util.List m_defglobals = Collections.synchronizedList(new ArrayList());
    private Map m_functions = Collections.synchronizedMap(new HashMap(101));
    private FactList m_factList = new FactList();
    private DefinstanceList m_definstanceList = new DefinstanceList(this);
    private Map m_rules = Collections.synchronizedMap(new TreeMap());
    private ReteCompiler m_compiler = new ReteCompiler();
    private Map m_storage = Collections.synchronizedMap(new HashMap());
    private static Factory m_factory = new FactoryImpl();
    private Agenda m_agenda = new Agenda();
    private Hashtable m_classImports = new Hashtable();
    private ArrayList m_packageImports = new ArrayList();
    private boolean[] m_watchInfo = new boolean[5];
    public static final int INSTALL = 0, ACTIVATE = 1, EVERY_TIME = 2;
    private static final String LIBRARY_NAME = "scriptlib.clp";

    // **********************************************************************
    // Constructors and pseudoconstructors
    // **********************************************************************

    public Rete() {
        this(null);
    }

    /**
     * @param applet If this Rete object is being created inside
     * an applet, pass it as an argument.
     */
    public Rete(Applet applet) {
        this((Object) applet);
    }


    /*
     * Construct a Rete object and supply an example of an application
     * class.  Jess will try to find things (like files passed as
     * arguments to "batch") "near" where this class was loaded; i.e.,
     * it will find them using the classloader used to load the argument.
     * @param appObject Any class loaded by the application-level class loader
     */

    public Rete(Object appObject) {
        // ###
        m_appObject = appObject;

        try {
            Deftemplate.addStandardTemplates(this);
        } catch (JessException je) {
            throw new RuntimeException(je.getMessage());
        }

        setEventMask(0);

        loadScriptlib();

        // initialize import tables
        m_packageImports.add("java.lang.");
    }

    // **********************************************************************
    // I/O Router functions
    // **********************************************************************

    /**
     * @param s The router name
     * @param is A Reader where the router's data comes from
     * @param consoleLike See the Jess manual
     */
    public void addInputRouter(String s, Reader is, boolean consoleLike) {
        m_routers.addInputRouter(s, is, consoleLike);
    }

    /**
     * @param s The name of the router
     */
    public void removeInputRouter(String s) {
        m_routers.removeInputRouter(s);
    }

    /**
     * @param s The router name
     * @return The router, or null if none
     */
    public Reader getInputRouter(String s) {
        return m_routers.getInputRouter(s);
    }

    Tokenizer getInputWrapper(Reader is) {
        return m_routers.getInputWrapper(is);
    }

    /**
     * @param s  The router name
     * @param os Where the data should go
     */
    public void addOutputRouter(String s, Writer os) {
        m_routers.addOutputRouter(s, os);
    }

    /**
     * @param s The name of the router
     */
    public void removeOutputRouter(String s) {
        m_routers.removeOutputRouter(s);
    }

    /**
     * @param s The router name
     * @return The console-like property for that router
     */
    public boolean getInputMode(String s) {
        return m_routers.getInputMode(s);
    }

    /**
     * @param s The router name
     * @return The router, or null if none
     */
    public Writer getOutputRouter(String s) {
        return m_routers.getOutputRouter(s);
    }

    /**
     * @return The WSTDERR router
     */
    public PrintWriter getErrStream() {
        return m_routers.getErrStream();
    }

    /**
     * @return The WSTDOUT router
     */
    public PrintWriter getOutStream() {
        return m_routers.getOutStream();
    }

    // **********************************************************************
    // Fact list stuff
    // **********************************************************************


    public int doPreAssertionProcessing(Fact f) throws JessException {
        return m_factList.doPreAssertionProcessing(f);
    }

    /**
     * Reinitialize engine
     * Thanks to Karl Mueller for idea
     * @exception JessException
     */
    public synchronized void clear() throws JessException {

        clearStorage();
        m_globalContext.clear();

        m_factList.clear();
        m_rules.clear();
        m_agenda.clear();
        m_definstanceList.clear();

        keepJavaUserFunctionsOnly();
        Deftemplate.addStandardTemplates(this);

        m_compiler = new ReteCompiler();

        m_deffacts.clear();
        m_defglobals.clear();

        broadcastEvent(JessEvent.CLEAR, this);
        setEventMask(0);

        unwatchAll();

        loadScriptlib();

        System.gc();
    }

    private void keepJavaUserFunctionsOnly() {
        ArrayList al = new ArrayList();
        for (Iterator it = m_functions.keySet().iterator(); it.hasNext();) {
            Userfunction uf = findUserfunction((String) it.next());
            if (!(uf instanceof Deffunction))
                al.add(uf);
        }

        m_functions = Collections.synchronizedMap(new HashMap());
        for (Iterator it = al.iterator(); it.hasNext();)
            addUserfunction((Userfunction) it.next());
    }


    public void setPendingFact(Fact fact, boolean assrt) {
        m_factList.setPendingFact(fact, assrt);
    }

    void removeAllFacts() throws JessException {
        processToken(RU.CLEAR, Fact.getClearFact());
        m_factList.clear();
    }

    /**
     * Reset the Rete engine. Remove all facts, activations,
     * etc. Clear all non-globals from the global scope. Assert
     * (initial-fact). Broadcasts a JessEvent of type RESET.
     *
     * @exception JessException If anything goes wrong.  */

    public void reset() throws JessException {

        synchronized (m_compiler) {
            removeAllFacts();
            m_globalContext.removeNonGlobals();
            m_agenda.reset(this);
            assertFact(Fact.getInitialFact(), getGlobalContext());
            resetDefglobals();
            resetDeffacts();
        }
        m_definstanceList.reset();
        broadcastEvent(JessEvent.RESET, this);
        // EJFH TODO Make this switchable!
        // System.gc();
    }

    private void resetDeffacts() throws JessException {
        for (Iterator it = m_deffacts.iterator(); it.hasNext();)
            ((Deffacts) it.next()).reset(this);
    }

    private void resetDefglobals() throws JessException {
        if (getResetGlobals()) {
            for (Iterator e = m_defglobals.iterator(); e.hasNext();)
                ((Defglobal) e.next()).reset(this);
        }
    }


    /**
     * Assert a fact, as a String
     * @param s A String representing a fact
     * @param c An execution context for resolving variables
     * @exception JessException If something goes wrong
     * @return The fact that was asserted
     */

    public Fact assertString(String s, Context c) throws JessException {
        synchronized (m_tis) {
            m_tis.clear();
            m_jesp.clear();
            m_tis.appendText("(assert " + s + ")");
            Value v = m_jesp.parseAndExecuteFuncall(null, c);
            if (v.type() == RU.FACT)
                return v.factValue(c);
            else
                return null;
        }
    }

    /**
     * Assert a fact, as a String, using the global execution context
     * @param s A String representing a fact
     * @exception JessException If something goes wrong
     * @return The fact that was asserted
     */

    public Fact assertString(String s) throws JessException {
        return assertString(s, getGlobalContext());
    }

    /**
     * @deprecated Use assertFact instead.
     */
    public Fact assert(Fact f) throws JessException {
        return assertFact(f);
    }


    /**
     * Assert a fact
     * @param f A Fact object. This fact becomes the property of Jess
     * after calling assertFact() -- don't change any of its fields until
     * the fact is retracted!
     *
     * @exception JessException If anything goes wrong
     * @return The fact ID on success, or -1.
     */

    public Fact assertFact(Fact f) throws JessException {
        return assertFact(f, getGlobalContext());
    }

    /**
     * @deprecated Use assertFact instead
     */
    public Fact assert(Fact f, Context c) throws JessException {
        return assertFact(f, c);
    }

    /**
     * Assert a fact
     * @param f A Fact object. This fact becomes the property of Jess
     * after calling assertFact() -- don't change any of its fields until
     * the fact is retracted!
     *
     * @exception JessException If anything goes wrong
     * @return The fact ID on success, or -1.
     */

    public Fact assertFact(Fact f, Context c) throws JessException {
        synchronized (m_compiler) {
            return m_factList.assertFact(f, this, c);
        }
    }

    Fact assertKeepID(Fact f, Context c) throws JessException {
        synchronized (m_compiler) {
            return m_factList.assertKeepID(f, this, c);
        }
    }

    /**
     * Karl Mueller NASA/GSFC Code 522.2
     * (Karl.R.Mueller@gsfc.nasa.gov)
     * 27.January.1998
     *
     * Retract a fact as a string
     * @param s
     * @exception JessException
     */
    public Fact retractString(String s) throws JessException {
        try {
            synchronized (m_tis) {
                m_tis.clear();
                m_jesp.clear();
                m_tis.appendText(s);
                Fact f = m_jesp.parseFact();
                return retract(f);
            }
        } catch (Exception t) {
            throw new JessException("Rete.retractString", s, t);
        }
    }

    /**
     * Retract a fact.
     * @param f A Fact object. Doesn't need to be the actual object
     * that appears on the fact-list; can just be a Fact that could
     * compare equal to one.
     * @exception JessException If anything goes wrong.
     */

    public Fact retract(Fact f) throws JessException {
        if (f.isShadow()) {
            Object ov = f.getSlotValue("OBJECT").
                    externalAddressValue(null);
            return undefinstance(ov);
        } else {
            synchronized (m_compiler) {
                return m_factList.retract(f, this);
            }
        }
    }

    Fact retractNoUndefinstance(Fact f) throws JessException {
        synchronized (m_compiler) {
            return m_factList.retract(f, this);
        }
    }

    /**
     * Modify a single slot in a fact. This function works for both
     * plain and shadow facts.
     * @param fact A fact that's currently in working memory.
     * @param slot The name of a slot in the fact
     * @param value A new value for the slot
     * @return The fact argument
     * @throws JessException If the slot name is bad or any other error occurs.
     */
    public Fact modify(Fact fact, String slot, Value value) throws JessException {
        return m_factList.modify(fact, slot, value, this, getGlobalContext());
    }

    /**
     * Modify a single slot in a fact. This function works for both
     * plain and shadow facts.
     * @param fact A fact that's currently in working memory.
     * @param slot The name of a slot in the fact
     * @param value A new value for the slot
     * @param context The execution context
     * @return The fact argument
     * @throws JessException If the slot name is bad or any other error occurs.
     */
    public Fact modify(Fact fact, String slot, Value value, Context context) throws JessException {
        return m_factList.modify(fact, slot, value, this, context);
    }

    /**
     * Do this here so we can optimize out some searches.
     */

    Fact _modify(ValueVector vv, Context context) throws JessException {
        // Deliberately not synchonized here, or can deadlock for
        // shadow facts.
        return m_factList._modify(vv, context, this);
    }

    /**
     * Do this here so we can optimize out some searches.
     * Throws if called for shadow facts
     */

    Fact _duplicate(ValueVector vv, Context context) throws JessException {
        return m_factList._duplicate(vv, context, this);
    }

    /**
     * This "find" is very slow; don't use it unless you have to.
     * Consider the returned Fact to be READ-ONLY!
     * @param id The fact-id
     * @exception JessException If something goes wrong
     * @return The fact, or null if none
     */

    public Fact findFactByID(int id) throws JessException {
        return m_factList.findFactByID(id);
    }

    /**
     * This find is fast, and can be used to find out quickly if a
     * given fact is on the fact-list and if so, obtain a reference to
     * it. The argument doesn't have to be a fact on the fact list --
     * only a Fact object identical to one that is.
     */

    public Fact findFactByFact(Fact f) throws JessException {
        return m_factList.findFactByFact(f);
    }


    void removeLogicalSupportFrom(Token token, Fact fact) {
        m_factList.removeLogicalSupportFrom(token, fact);
    }


    // **********************************************************************
    // Storing, finding, listing
    // **********************************************************************

    /**
     * Write the pretty print forms of the facts with the given head
     * to the writer */

    public void ppFacts(String head, Writer output) throws IOException {
        m_factList.ppFacts(resolveName(head), output);
    }

    /**
     * Write the pretty print forms of all facts to the writer
     * @exception IOException
     */
    public void ppFacts(Writer output) throws IOException {
        m_factList.ppFacts(output);
    }

    /**
     * Return an Iterator over all the deffacts in this engine.
     */
    public Iterator listDeffacts() {
        synchronized (m_deffacts) {
            return new ArrayList(m_deffacts).iterator();
        }
    }

    /**
     * Return the named deffacts object
     */
    public Deffacts findDeffacts(String name) {
        name = resolveName(name);
        for (Iterator it = listDeffacts(); it.hasNext();) {
            Deffacts df = (Deffacts) it.next();
            if (df.getName().equals(name))
                return df;
        }
        return null;
    }

    /**
     * Return an Iterator over all the deftemplates in this engine,
     * both explicit and implied.
     */
    public Iterator listDeftemplates() {
        ArrayList al = new ArrayList();
        for (Iterator modules = m_agenda.listModules(); modules.hasNext();) {
            try {
                Defmodule module =
                        m_agenda.getModule((String) modules.next());
                for (Iterator dts = module.listDeftemplates();
                     dts.hasNext();)
                    al.add(dts.next());
            } catch (JessException je) {
                continue;
            }
        }
        return al.iterator();
    }

    /**
     * Return an Iterator over all the defrules in this engine.
     */
    public Iterator listDefrules() {
        synchronized (m_rules) {
            return new ArrayList(m_rules.values()).iterator();
        }
    }

    /**
     * Return an Iterator over all the facts currently on the fact-list
     */
    public Iterator listFacts() {
        return m_factList.listFacts();
    }


    /**
     * Return an Iterator over all the definstanced objects
     */
    public Iterator listDefinstances() {
        return m_definstanceList.listDefinstances();
    }

    /**
     * Return an Iterator over all the names of all defclasses. You
     * can use each name to look up the corresponding template using
     * findDeftemplate, or the corresponding Java class using
     * javaClassForDefclass.
     * @see #findDeftemplate
     * @see #javaClassForDefclass
     */
    public Iterator listDefclasses() {
        return m_definstanceList.listDefclasses();
    }


    /**
     * Return an Iterator over all the defglobals in this engine.
     */
    public Iterator listDefglobals() {
        synchronized (m_defglobals) {
            return new ArrayList(m_defglobals).iterator();
        }
    }

    /**
     * Return an Iterator over all the functions in this engine:
     * built-in, user, and deffunctions.
     */
    public Iterator listFunctions() {

        // Strip advice and FunctionHolders here.
        ArrayList v = new ArrayList();
        synchronized (m_functions) {
            for (Iterator e = m_functions.keySet().iterator(); e.hasNext();)
            v.add(findUserfunction((String) e.next()));
        }

        return v.iterator();
    }

    /**
     * Find a defrule or defquery object with a certain name.
     * @param name The name
     * @return The found rule or query, or null.
     */
    public final HasLHS findDefrule(String name) {
        return (HasLHS) m_rules.get(resolveName(name));
    }

    /**
     * Return the Java Class corresponding to a given Defclass name,
     * or null if the name was not found.
     */

    public Class javaClassForDefclass(String name) {
        try {
            String clazz = m_definstanceList.jessNameToJavaName(name);
            if (clazz == null)
                return null;
            else
                return findClass(clazz);
        } catch (ClassNotFoundException cnfe) {
            return null;
        }
    }

    /**
     * Find a deftemplate object with a certain name
     * @param name The name
     * @return The found object, or null.
     */
    public Deftemplate findDeftemplate(String name) throws JessException {
        String fullName = resolveName(name);
        String moduleName = fullName.substring(0, fullName.indexOf("::"));
        Deftemplate deft = m_agenda.getModule(moduleName).getDeftemplate(name);

        if (deft == null && !fullName.equals(name)) {
            // It may actually be defined in MAIN
            deft = m_agenda.getModule(Defmodule.MAIN).getDeftemplate(name);
        }

        return deft;
    }

    /**
     * find the deftemplate, if there is one, or create implied dt.
     * @param name
     * @exception JessException
     * @return
     */
    Deftemplate createDeftemplate(String name)
            throws JessException {

        Deftemplate deft = findDeftemplate(name);

        if (deft == null) {
            // this is OK. Create an implied deftemplate
            deft = addDeftemplate(new Deftemplate(name, "(Implied)", this));
            deft.addMultiSlot(RU.DEFAULT_SLOT_NAME, Funcall.NILLIST);

        }

        return deft;
    }

    /**
     * Creates a new deftemplate in this object.
     * Ensure that every deftemplate has a unique class name.
     * @param dt
     * @exception JessException If the deftemplate is already defined
     * @return The argument.
     */
    public Deftemplate addDeftemplate(Deftemplate dt) throws JessException {
        Defmodule module = m_agenda.getModule(dt.getModule());
        // setCurrentModule(module.getName());
        return module.addDeftemplate(dt, this);
    }

    /**
     * Creates a new deffacts in this object
     * @param df A new Deffacts object
     * @exception JessException If an error occurs during event broadcasting
     * @return The argument
     */
    public Deffacts addDeffacts(Deffacts df) throws JessException {
        broadcastEvent(JessEvent.DEFFACTS, df);
        m_deffacts.add(df);
        return df;
    }

    /**
     * Creates a new Defglobal in this object. Trick it into resetting
     * right now, regardless of the setting of resetGlobals.
     * @param dg A new Defglobal object
     * @exception JessException If an error occurs
     * @return The argument
     */
    public Defglobal addDefglobal(Defglobal dg) throws JessException {
        broadcastEvent(JessEvent.DEFGLOBAL, dg);

        dg.reset(this);
        m_defglobals.add(dg);
        return dg;
    }

    /**
     * Look up a defglobal by name.
     * @param name The name of the defglobal
     * @return The Defglobal, if found, or null.
     */
    public Defglobal findDefglobal(String name) {
        for (Iterator e = listDefglobals(); e.hasNext();) {
            Defglobal dg = (Defglobal) e.next();
            if (dg.getName().equals(name))
                return dg;
        }
        return null;
    }

    /**
     * Creates a new function in this object
     * Will happily destroy an old one.
     * @param uf A new USerfunction
     * @return The parameter, or null if call rejected by event handler
     */
    public Userfunction addUserfunction(Userfunction uf) {
        try {
            broadcastEvent(JessEvent.USERFUNCTION, uf);
        } catch (JessException je) {
            return null;
        }

        FunctionHolder fh;
        if ((fh = (FunctionHolder) m_functions.get(uf.getName())) != null)
            fh.setFunction(uf);
        else
            fh = new FunctionHolder(uf);
        m_functions.put(uf.getName(), fh);
        return uf;
    }

    /**
     * Add  a Userpackage  to this engine.  A package  generally calls
     * addUserfunction lots of times.
     * @param up The package object
     * @return The  package object, or null if  call rejected by event
     * handler
     */
    public Userpackage addUserpackage(Userpackage up) {
        try {
            broadcastEvent(JessEvent.USERPACKAGE, up);
        } catch (JessException je) {
            return null;
        }

        up.add(this);
        return up;
    }

    /**
     * Find a userfunction, if there is one.
     * @param name The name of the function
     * @return The Userfunction object, if there is one.
     */
    public final Userfunction findUserfunction(String name) {
        FunctionHolder fh = (FunctionHolder) m_functions.get(name);
        if (fh != null) {
            Userfunction f = fh.getFunction();
            return f;
        } else
            return Funcall.getIntrinsic(name);
    }

    /**
     * Find a userfunction, if there is one.
     * @param name The name of the function
     * @return The Userfunction object, if there is one.
     */
    final FunctionHolder findFunctionHolder(String name) {
        FunctionHolder fh = (FunctionHolder) m_functions.get(name);
        if (fh == null) {
            Userfunction uf = Funcall.getIntrinsic(name);
            if (uf != null)
                addUserfunction(uf);
            fh = (FunctionHolder) m_functions.get(name);
        }
        return fh;
    }

    /**
     * Creates a new defrule or defquery in this object
     * @param dr A Defrule or Defquery
     * @exception JessException If anything goes wrong.
     * @return The added object
     */
    public final HasLHS addDefrule(HasLHS dr) throws JessException {
        synchronized (m_compiler) {
            JessException exception = null;
            unDefrule(dr.getName());
            try {
                m_compiler.addRule(dr, this);

            } catch (RuleCompilerException rce) {
                dr.remove(m_compiler.getRoot());
                throw rce;

            } catch (JessException je) {
                exception = je;
            }
            m_rules.put(dr.getName(), dr);
            // setCurrentModule(dr.getModule());
            broadcastEvent(JessEvent.DEFRULE, dr);

            if (exception != null)
                throw exception;
            else
                return dr;
        }
    }

    /**
     * Remove a rule or query from this Rete object. Removes all
     * subrules of the names rule as well.
     * @param name The name of the rule or query
     * @exception JessException If anything goes wrong
     * @return The symbol TRUE
     */
    public final Value unDefrule(String name) throws JessException {
        synchronized (m_compiler) {
            HasLHS odr = findDefrule(name);
            if (odr != null) {
                odr.remove(m_compiler.getRoot());
                m_rules.remove(resolveName(name));
                if (odr instanceof Defrule)
                    for (Iterator e = m_agenda.listActivations(); e.hasNext();) {
                        Activation a = (Activation) e.next();
                        if (a.getRule() == odr) {
                            removeActivation(a);
                        }
                    }
                // Chains of rules from ORs.
                if (odr.getNext() != null)
                    unDefrule(odr.getNext().getName());

                broadcastEvent(JessEvent.DEFRULE | JessEvent.REMOVED, odr);
                return Funcall.TRUE;
            }

        }

        return Funcall.FALSE;
    }
    // **********************************************************************
    // Modules
    // **********************************************************************

    /**
     *  Define a new module, which becomes current.
     */
    public void addDefmodule(String moduleName) throws JessException {
        m_agenda.addDefmodule(moduleName);
    }

    /**
     *  Define a new module, which becomes current.
     */
    public void addDefmodule(String moduleName, String doccomment)
            throws JessException {
        m_agenda.addDefmodule(moduleName, doccomment);
    }

    /**
     * Return the name of the current module.
     */
    public String getCurrentModule() {
        return m_agenda.getCurrentModule();
    }

    /**
     * Change the current module.
     */
    public String setCurrentModule(String name) throws JessException {
        return m_agenda.setCurrentModule(name);
    }

    /**
     * List all modules
     */
    public Iterator listModules() throws JessException {
        return m_agenda.listModules();
    }

    /**
     * Query the focus module.
     */
    public String getFocus() {
        return m_agenda.getFocus();
    }

    /**
     * Change the focus module.
     */
    public void setFocus(String name) throws JessException {
        m_agenda.setFocus(name, this);
    }

    /**
     * Iterate over the focus stack, from bottom to top. The current
     * focus module is returned last.
     */
    public Iterator listFocusStack() throws JessException {
        return m_agenda.listFocusStack();
    }

    /**
     * Empty the focus stack.
     */
    public void clearFocusStack() {
        m_agenda.clearFocusStack();
    }

    /**
     * Remove the top module from the focus stack, and return it. If
     * expected in non-null, then this is a no-op unless expected
     * names the top module on the stack.
     */
    public String popFocus(String expected) throws JessException {
        return m_agenda.popFocus(this, expected);
    }

    /**
     * Throw an exception if the argument isn't the name of a module.
     */

    public void verifyModule(String moduleName) throws JessException {
        m_agenda.verifyModule(moduleName);
    }

    /**
     * Decorate the name with the current module name, if it doesn't
     * already contain a module name.
     */

    public String resolveName(String name) {
        return m_agenda.resolveName(name);
    }


    // **********************************************************************
    // Dealing with the agenda: running, stopping, salience, etc.
    // **********************************************************************

    Token processToken(int tag, Fact fact) throws JessException {
        synchronized (m_compiler) {
            m_factList.assignTime(fact);
            Token t = Rete.getFactory().newToken(fact, tag);
            m_compiler.getRoot().callNodeRight(t, getGlobalContext().push());
            return t;
        }
    }


    /**
     * Present all the facts on the agenda to a single Node.
     */
    void updateNodes(Hashtable n) throws JessException {
        JessException exception = null;
        for (Iterator e = m_factList.listFacts(); e.hasNext();) {
            Fact fact = (Fact) e.next();
            Token t = Rete.getFactory().newToken(fact, RU.UPDATE);
            for (Enumeration nodes = n.elements(); nodes.hasMoreElements();)
                synchronized (m_compiler) {
                    Node node = (Node) nodes.nextElement();
                    try {
                        node.callNodeRight(t, getGlobalContext().push());
                    } catch (JessException je) {
                        exception = je;
                    }
                }
        }
        m_factList.processPendingFacts(this);
        if (exception != null)
            throw exception;
    }

    /**
     * Info about a rule to fire.
     */
    void addActivation(Activation a) throws JessException {
        broadcastEvent(JessEvent.ACTIVATION, a);
        m_agenda.addActivation(a, this);
    }

    /**
     * An activation has been cancelled or fired; forget it
     */

    void removeActivation(Activation a) throws JessException {
        broadcastEvent(JessEvent.ACTIVATION | JessEvent.REMOVED, a);
        a.setInactive();
    }

    /**
     * Return an Iterator over all the activiations for the current
     * module. Note that some of the activations may be cancelled or
     * already fired; check the return value of "isInactive()"for each
     * one to be sure.
     */
    public Iterator listActivations() {
        return m_agenda.listActivations();
    }

    /**
     * Return an Iterator over all the activiations for the named
     * module. Asterisk ("*") is not accepted. Note that some of the
     * activations may be cancelled or already fired; check the return
     * value of "isInactive()" for each one to be sure.
     * @see jess.Activation#isInactive
     * @return An iterator over the agenda for the named module
     */
    public Iterator listActivations(String moduleName) throws JessException {
        return m_agenda.listActivations(moduleName);
    }

    /**
     * The monitor of the object returned from this method will be signalled
     * whenever an activation appears. Thus a run-loop could wait on
     * this monitor when idle.
     * @see #waitForActivations
     * @return The activation lock
     */

    public Object getActivationSemaphore() {
        return m_agenda.getActivationSemaphore();
    }

    /**
     * Waits on the activation lock until a rule is activated. Can be called
     * in a run-loop to wait for more rules to fire.
     * @see #getActivationSemaphore
     */

    public void waitForActivations() {
        m_agenda.waitForActivations();
    }

    /**
     * Tell this engine to use the given Strategy object to
     * order the rules on the agenda.
     * @param s The new conflict resolution strategy
     * @exception JessException
     * @return The name of the previous conflict resolution strategy
     */
    public String setStrategy(Strategy s) throws JessException {
        return m_agenda.setStrategy(s, this);
    }


    /**
     * Retrieve the Strategy object this engine is using to order activations
     * on the agenda.
     * @return The current conflict resolution strategy
     */
    public Strategy getStrategy() {
        return m_agenda.getStrategy();
    }

    /**
     * Set the salience evaluation behaviour. The behaviour can be one
     * of INSTALL, ACTIVATE, or EVERY_TIME; the default is INSTALL. When
     * the behaviour is INSTALL, a rule's salience is evulated once when
     * the rule is compiled. If it is ACTIVATE, it is computed each time
     * the rule is activated. If it is EVERY_TIME, salience evaluations
     * are done for all rules each time the next rule on the agenda is
     * to be chosen.
     * @param method One of the acceptable values
     * @exception JessException If something goes wrong
     */

    final public void setEvalSalience(int method) throws JessException {
        m_agenda.setEvalSalience(method);
    }

    /**
     * Fetch the salience evaluation behaviour
     * @see #setEvalSalience
     * @return The salience evaluation behaviour
     */
    final public int getEvalSalience() {
        return m_agenda.getEvalSalience();
    }

    /**
     * Run the actual engine.
     * @exception JessException If anything goes wrong.
     * @return The actual number of rules fired
     */
    public int run() throws JessException {
        broadcastEvent(JessEvent.RUN, this);
        return m_agenda.run(this);
    }

    protected void aboutToFire(Activation a) {
    }

    protected void justFired(Activation a) {
    }

    /**
     * Run the rule engine.
     *
     * @param max The maximum number of rules to fire
     * @exception JessException If anything goes wrong.
     * @return The number of rules that fired
     */
    public int run(int max) throws JessException {
        broadcastEvent(JessEvent.RUN, this);
        return m_agenda.run(max, this);
    }

    /**
     * Run until halt() is called. When no rules are active, the
     * calling Thread will be waiting on the activation semaphore.
     * @return The number of rules that fired.
     */

    public int runUntilHalt() throws JessException {
        broadcastEvent(JessEvent.RUN, this);
        return m_agenda.runUntilHalt(this);
    }

    /**
     * Stop the engine from firing rules.
     */

    public void halt() throws JessException {
        broadcastEvent(JessEvent.HALT, this);
        m_agenda.halt();
    }

    /**
     * Find out the name of the currently firing rule.
     * @see #getThisActivation
     * @return The name of the rule that is currently firing, if this is
     * called while a rule is firing; otherwise, returns null.
     */
    public String getThisRuleName() {
        Activation a = getThisActivation();
        if (a != null)
            return a.getRule().getName();
        else
            return null;
    }
    /**
     * Get the activation record for the currently firing rule. An activation record
     * contains a Defrule and the list of facts that made the rule active.
     * @see #getThisRuleName
     * @return The activation record for the rule that is currently firing, if this is
     * called while a rule is firing; otherwise, returns null.
     */
    public Activation getThisActivation() {
        return m_agenda.getThisActivation();
    }


    // **********************************************************************
    // Events and event listener support
    // **********************************************************************

    public void addJessListener(JessListener jel) {
        m_jes.addJessListener(jel);
    }

    public void removeJessListener(JessListener jel) {
        m_jes.removeJessListener(jel);
    }

    public Iterator listJessListeners() {
        return m_jes.listJessListeners();
    }

    public int getEventMask() {
        return m_jes.getEventMask();
    }

    public void setEventMask(int i) {
        m_jes.setEventMask(i);
    }

    final void broadcastEvent(int type, Object data) throws JessException {
        m_jes.broadcastEvent(this, type, data);
    }

    // **********************************************************************
    // Bloading and serialization
    // **********************************************************************

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        m_routers = new Routers();
        m_tis = new TextReader(true);
        m_jesp = new Jesp(m_tis, this);
        m_jes = new JessEventSupport(this);
        m_definstanceList.setEngine(this);
        m_globalContext.setEngine(this);
    }

    /**
     * Read this object's state from the given stream.
     */

    public void bload(InputStream is)
            throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(is);

        m_globalContext = (Context) ois.readObject();
        m_globalContext.setEngine(this);
        m_resetGlobals = ois.readBoolean();
        m_deffacts = (java.util.List) ois.readObject();
        m_defglobals = (java.util.List) ois.readObject();
        m_functions = (Map) ois.readObject();
        m_factList = (FactList) ois.readObject();
        m_definstanceList = (DefinstanceList) ois.readObject();
        m_definstanceList.setEngine(this);
        m_rules = (Map) ois.readObject();
        m_compiler = (ReteCompiler) ois.readObject();
        m_storage = (Map) ois.readObject();
        m_agenda = (Agenda) ois.readObject();
        m_classImports = (Hashtable) ois.readObject();
        m_packageImports = (ArrayList) ois.readObject();
        m_watchInfo = (boolean[]) ois.readObject();
    }

    /**
     * Save this object's state out to the given stream.
     */

    public void bsave(OutputStream os) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(os);

        oos.writeObject(m_globalContext);
        oos.writeBoolean(m_resetGlobals);
        oos.writeObject(m_deffacts);
        oos.writeObject(m_defglobals);
        oos.writeObject(m_functions);
        oos.writeObject(m_factList);
        oos.writeObject(m_definstanceList);
        oos.writeObject(m_rules);
        oos.writeObject(m_compiler);
        oos.writeObject(m_storage);
        oos.writeObject(m_agenda);
        oos.writeObject(m_classImports);
        oos.writeObject(m_packageImports);
        oos.writeObject(m_watchInfo);
        oos.flush();
    }

    // **********************************************************************
    // Defclass and definstance support
    // **********************************************************************

    /**
     * Tell this engine to pattern match on the given object.
     *
     * @param jessTypename The name of a defclass
     * @param object An object of the defclass's type
     * @param dynamic true if PropertyChangeListeners should be used
     * @return A FactIdValue containing the shadow fact for the object
     */
    public Value definstance(String jessTypename, Object object,
                             boolean dynamic) throws JessException {
        return definstance(jessTypename, object, dynamic,
                getGlobalContext());
    }

    public Value definstance(String jessTypename, Object object,
                             boolean dynamic, Context context)
            throws JessException {
        broadcastEvent(JessEvent.DEFINSTANCE, object);
        return m_definstanceList.definstance(jessTypename, object,
                dynamic, context);
    }

    /**
     * Tell this engine to stop pattern matching on the given object
     *
     * @param object An object of the defclass's type
     */

    public Fact undefinstance(Object object) throws JessException {

        Fact f = m_definstanceList.undefinstance(object);
        broadcastEvent(JessEvent.DEFINSTANCE | JessEvent.REMOVED, object);
        return f;
    }

    void undefinstanceNoRetract(Object object) throws JessException {

        m_definstanceList.undefinstanceNoRetract(object);
        broadcastEvent(JessEvent.DEFINSTANCE | JessEvent.REMOVED, object);
    }



    /**
     * Bring a shadow fact up to date. If the properties of the given object,
     * assumed to be a definstanced object, have changed, its corresponding shadow
     * fact will be updated.
     * @param object A previously definstanced object
     * @return The shadow fact, as a FactIDValue
     * @throws JessException If object isn't a definstanced object, or on error.
     */
    public Value updateObject(Object object) throws JessException {
        return m_definstanceList.updateObject(object);
    }

    /**
     * Add a defclass definition to this engine
     *
     * @param jessName The name Jess should use for this defclass
     * @param clazz The name of the Java class
     * @param parent If non-null, a parent deftemplate or defclass name
     */

    public Value defclass(String jessName, String clazz, String parent)
            throws JessException {
        broadcastEvent(JessEvent.DEFCLASS, jessName);
        return m_definstanceList.defclass(jessName, clazz, parent);
    }

    FactList getFactList() {
        return m_factList;
    }

    // **********************************************************************
    // Miscellaneous
    // **********************************************************************

    /**
     * Returns the applet this Rete is installed in. Returns null if none.
     * @return The applet
     */

    public Applet getApplet() {
        if (m_appObject instanceof Applet)
            return (Applet) m_appObject;
        else
            return null;
    }

    /**
     * Returns the "application object" for this Rete instance
     * @see jess.Rete#Rete
     */

    public Class getAppObjectClass() {
        if (m_appObject != null)
            return m_appObject.getClass();
        else
            return Rete.class;
    }

    /**
     * Associates this Rete with an applet so that, for instance, the
     * (batch) commands will look for scripts using the applet's
     * document base URL.
     * @param applet The applet
     */
    public void setApplet(Applet applet) {
        m_appObject = applet;
    }

    /**
     * Associates this Rete with an object so that, for instance, the
     * (batch) commands will look for scripts using the object's
     * class loader.
     * @param appObject The app object
     */
    public void setAppObject(Object appObject) {
        m_appObject = appObject;
    }

    /**
     * Fetch the global execution context.
     * @return The global execution context.
     */
    public final Context getGlobalContext() {
        return m_globalContext;
    }

    /**
     * Evaluate a Jess expression in this engine's global context.
     * @param cmd A string containing a Jess expression
     * @exception JessException If anything goes wrong
     * @return The result of evaluating the expression
     */
    public Value executeCommand(String cmd) throws JessException {
        return executeCommand(cmd, m_globalContext);
    }

    /**
     * Evaluate a Jess expression in the given context.
     * @param cmd A string containing a Jess expression
     * @param context The evaluation context
     * @exception JessException If anything goes wrong
     * @return The result of evaluating the expression
     */
    public Value executeCommand(String cmd, Context context)
            throws JessException {
        synchronized (m_tis) {
            m_tis.clear();
            m_jesp.clear();
            m_tis.appendText(cmd);
            return m_jesp.parse(false, context);
        }
    }

    /**
     * When resetGlobals is true, the initializers of global variables
     * are evaluated when (reset) is executed.
     * @param reset The value of this property
     */
    final public void setResetGlobals(boolean reset) {
        m_resetGlobals = reset;
    }

    /**
     * When resetGlobals is true, the initializers of global variables
     * are evaluated when (reset) is executed.
     * @return The value of this property
     */
    final public boolean getResetGlobals() {
        return m_resetGlobals;
    }

    /**
     * Fetch the ReteCompiler object used by the engine. You
     * probabably shouldn't use this for anything!
     *
     * @return the Compiler object
     */

    final ReteCompiler getCompiler() {
        return m_compiler;
    }

    /**
     * Store a value in the engine under a given name for later
     * retrieval by fetch.
     *
     * @see Rete#fetch
     * @param name A key under which to file the value
     * @param val The value to store
     * @return Any old value stored under this name, or null.  */

    public Value store(String name, Value val) {
        if (val == null)
            return (Value) m_storage.remove(name);
        else
            return (Value) m_storage.put(name, val);
    }

    /**
     * Store a value in the engine under a given name for later
     * retrieval by fetch. The Object is first wrapped in a new
     * jess.Value object.
     * @see Rete#fetch
     * @param name A key under which to file the value
     * @param val The value to store
     * @return Any old value stored under this name, or null.
     */

    public Value store(String name, Object val) {
        if (val == null)
            return (Value) m_storage.remove(name);
        else
            return (Value) m_storage.put(name, new Value(val));
    }

    /**
     * Retrieve an object previously stored with store().
     * @see Rete#store
     * @param name The key under which to find an object
     * @return The object, or null if not found.
     */

    public Value fetch(String name) {
        return (Value) m_storage.get(name);
    }

    /**
     * Clear the storage used by store() and fetch().
     */

    public void clearStorage() {
        m_storage.clear();
    }


    int getTime() {
        return m_factList.getTime();
    }

    /*
     * Factory stuff
     */
    public static Factory getFactory() {
        return m_factory;
    }

    public static void setFactory(Factory f) {
        m_factory = f;
    }

    private void loadScriptlib() {
        try {
            new Batch().batch(LIBRARY_NAME, this);
        } catch (JessException je) {
            je.printStackTrace();
        }
    }

    /**
     * Try to load a class by name. First try the appObject loader, if
     * there is one. If there isn't, and Rete was loaded by a
     * different loader, try that one.
     */

    private Class classForName(String name) throws ClassNotFoundException {

        ClassLoader appLoader = getAppObjectClass().getClassLoader();
        if (appLoader != null) {
            try {
                return Class.forName(name, true, appLoader);
            } catch (ClassNotFoundException silentlyIgnore) {}
        }

        try {
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            if (contextLoader != null) {
                try {
                    return Class.forName(name, true, contextLoader);
                } catch (ClassNotFoundException silentlyIgnore) {}
            }
        } catch (SecurityException silentlyIgnore) {}

        return Class.forName(name);
    }

    /*
     * Import tables */

    public Class findClass(String clazz) throws ClassNotFoundException {
        if (clazz.indexOf(".") == -1) {
            String s = (String) m_classImports.get(clazz);
            if (s != null)
                clazz = s;

            else {
                for (Iterator e = m_packageImports.iterator(); e.hasNext();) {
                    s = ((String) e.next()) + clazz;
                    try {
                        Class c = classForName(s);
                        m_classImports.put(clazz, s);
                        return c;
                    } catch (ClassNotFoundException ex) {
                        /* Just try again */
                    }
                }
            }
        }
        return classForName(clazz);
    }

    public URL getResource(String name) {

        if (m_appObject != null) {
            URL u = m_appObject.getClass().getResource(name);
            if (u != null)
                return u;
        }

        try {
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            if (contextLoader != null) {
                URL u = contextLoader.getResource(name);
                if (u != null)
                    return u;
            }
        } catch (SecurityException silentlyIgnore) {}

        return Rete.class.getResource(name);
    }


    public void importPackage(String pack) {
        m_packageImports.add(pack);
    }

    public void importClass(String clazz) {
        m_classImports.put(clazz.substring(clazz.lastIndexOf(".") + 1,
                clazz.length()),
                clazz);
    }

    /**
     * Quick way to run a defquery
     */

    public Iterator runQuery(String name, ValueVector params)
            throws JessException {
        Funcall fc = new Funcall("run-query", this);
        fc.add(new Value(name, RU.STRING));
        fc.addAll(params);

        Context c = getGlobalContext();
        Value v = fc.execute(c);
        return (Iterator) v.externalAddressValue(c);
    }

    // **********************************************************************
    // The watch facility
    // **********************************************************************

    /**
     * Produce some debugging information. The argument specifies which
     * kind of event will be reported. The output goes to the "WSTDOUT" router.
     * @param which One of the constants in WatchConstants
     * @see jess.WatchConstants
     * @throws JessException If the argument is invalid
     */

    public void watch(int which) throws JessException {
        m_watchInfo[which] = true;
        int mask = 0;
        switch (which) {
            case WatchConstants.RULES:
                mask = JessEvent.DEFRULE_FIRED;
                break;

            case WatchConstants.FACTS:
                mask = JessEvent.FACT;
                break;

            case WatchConstants.ACTIVATIONS:
                mask = JessEvent.ACTIVATION;
                break;

            case WatchConstants.COMPILATIONS:
                mask = JessEvent.DEFRULE;
                break;

            case WatchConstants.FOCUS:
                mask = JessEvent.FOCUS;
                break;

            default:
                throw new JessException("watch", "Bad argument ", which);
        }
        m_watchInfo[which] = true;
        mask = getEventMask() | mask | JessEvent.CLEAR;
        setEventMask(mask);
    }

    /**
     * Cancel some debugging information. The argument specifies which
     * kind of event will no longer be reported.
     * @param which One of the constants in WatchConstants
     * @see jess.WatchConstants
     * @throws JessException If the argument is invalid
     */
    public void unwatch(int which) throws JessException {
        int mask = 0;
        switch (which) {
            case WatchConstants.RULES:
                mask = JessEvent.DEFRULE_FIRED;
                break;

            case WatchConstants.FACTS:
                mask = JessEvent.FACT;
                break;

            case WatchConstants.ACTIVATIONS:
                mask = JessEvent.ACTIVATION;
                break;

            case WatchConstants.COMPILATIONS:
                mask = JessEvent.DEFRULE;
                break;

            case WatchConstants.FOCUS:
                mask = JessEvent.FOCUS;
                break;

            default:
                throw new JessException("unwatch", "Bad argument ", which);
        }
        m_watchInfo[which] = false;
        mask = getEventMask() & ~mask;
        setEventMask(mask);
    }

    /**
     * Produce all possible debugging info. Equivalent to calling watch()
     * multiple times using each legal argument in succession.
     */
    public void watchAll() {
        for (int i = 0; i < m_watchInfo.length; ++i)
            m_watchInfo[i] = true;

        int mask = JessEvent.DEFRULE |
                JessEvent.DEFRULE_FIRED |
                JessEvent.FACT |
                JessEvent.FOCUS |
                JessEvent.ACTIVATION;
        mask = getEventMask() | JessEvent.CLEAR | mask;
        setEventMask(mask);
    }
    /**
     * Cancel all debugging info. Equivalent to calling unwatch()
     * using each legal argument in succession.
     */
    public void unwatchAll() {
        for (int i = 0; i < m_watchInfo.length; ++i)
            m_watchInfo[i] = false;

        int mask = JessEvent.DEFRULE |
                JessEvent.DEFRULE_FIRED |
                JessEvent.FACT |
                JessEvent.FOCUS |
                JessEvent.ACTIVATION;
        mask = getEventMask() & ~mask;
        setEventMask(mask);
    }

    boolean watchingAny() {
        for (int i = 0; i < m_watchInfo.length; ++i)
            if (m_watchInfo[i])
                return true;
        return false;
    }

    boolean watching(int which) {
        return m_watchInfo[which];
    }

    /**
     * This method is just an implementation detail. Part of the "watch"
     * facility. Each Rete object is registered with itself as a listener.
     * @param je An event object
     */
    public void eventHappened(JessEvent je) {
        if (!watchingAny())
            return;
        int type = je.getType();
        boolean remove = (type & JessEvent.REMOVED) != 0;
        boolean modified = (type & JessEvent.MODIFIED) != 0;

        PrintWriter pw = getOutStream();

        switch (type & ~JessEvent.REMOVED & ~JessEvent.MODIFIED) {

            case JessEvent.FACT:
                {
                    if (watching(WatchConstants.FACTS)) {
                        Fact f = (Fact) je.getObject();
                        pw.print(remove ? " <== " : modified ? " <=> " : " ==> ");
                        pw.print("f-");
                        pw.print(f.getFactId());
                        pw.print(" ");
                        pw.println(f);
                        pw.flush();
                    }
                    break;
                }

            case JessEvent.FOCUS:
                {
                    if (watching(WatchConstants.FOCUS)) {
                        pw.print(remove ? " <== " : " ==> ");
                        pw.print("Focus ");
                        pw.println(je.getObject());
                        pw.flush();
                    }
                    break;
                }

            case JessEvent.DEFRULE_FIRED:
                {
                    if (watching(WatchConstants.RULES))
                        ((Activation) je.getObject()).debugPrint(getOutStream());
                    break;
                }

            case JessEvent.ACTIVATION:
                {
                    if (watching(WatchConstants.ACTIVATIONS)) {
                        Activation a = (Activation) je.getObject();
                        pw.print(remove ? "<== " : "==> ");
                        pw.print("Activation: ");
                        pw.print(a.getRule().getDisplayName());
                        pw.print(" : ");
                        pw.println(a.getToken().factList());
                        pw.flush();
                    }
                    break;
                }

            case JessEvent.DEFRULE:
                {
                    if (watching(WatchConstants.COMPILATIONS) && !remove) {
                        pw.println(((HasLHS) je.getObject()).getCompilationTrace());
                        pw.flush();
                    }
                    break;
                }

            default:
                break;
        }
    }

    public String toString() {
        return "[Rete]";
    }

    // ***********************************************************************************
    // Logical support facility
    // ***********************************************************************************

    /**
     * Returns a list of one or more jess.Token objects that provide logical support
     * for this fact.  This method returns null if there is no
     * specific logical support. You can use the Token.size() method to check
     * how many supporting Facts are in a Token, and the Token.fact() method
     * to check each supporting Fact in turn. This is a fast operation, taking
     * O(ln N) time, where N is the number of facts that have logical support.
     * @see jess.Token
     * @see jess.Token#fact
     * @see jess.Token#size
     * @param fact A fact of interest.
     * @return A list of supporting Token objects, or null if there is unconditional support.
     */

    public ArrayList getSupportingTokens(Fact fact) {
        return m_factList.getSupportingTokens(fact);
    }

    /**
     * Returns a list of Fact objects that receive logical support from the argument.
     * This method is potentially expensive, as it takes time proportional to O(N),
     * where N is the number of facts currently receiving logical support of any kind.
     * @param supporter A fact of interest.
     * @return A list of zero or more Fact objects.
     */

    public ArrayList getSupportedFacts(Fact supporter) {
        return m_factList.getSupportedFacts(supporter);
    }

}



