package jess;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Parser functionality for Jess. Not serializable, as it holds a
 * reference to an input stream.
 * <P>
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */

public class Jesp {
    private final static String JAVACALL = "call";

    /**
       Stream where input comes from
    */
    private JessTokenStream m_jts;
    private Rete m_rete;

    /**
     * Construct a Jesp object.
     * The reader will be looked up in the Rete object's router tables,
     * and any wrapper found there will be used.
     * @param reader The Reader from which this Jesp should get its input
     * @param engine The engine that the parsed commands go to */

    public Jesp(Reader reader, Rete engine)  {
        // ###
        m_rete = engine;
        Tokenizer t = engine.getInputWrapper(reader);

        if (t == null) {
            t = new Tokenizer(reader);
        }

        m_jts = new JessTokenStream(t);
    }

    /**
     * Parses an input file.
     * Argument is true if a prompt should be printed (to the
     * ReteObject's standard output), false for no prompt.
     * @param prompt True if a prompt should be printed.
     * @exception JessException If anything goes wrong.
     * @return The result of the last parsed entity (often TRUE or FALSE).
     */

    public Value parse(boolean prompt) throws JessException {
        return parse(prompt, m_rete.getGlobalContext());
    }

    public synchronized Value parse(boolean prompt, Context context)
            throws JessException {

        Value val = Funcall.TRUE, oldval = val;

        if (prompt) {
            m_rete.getOutStream().print("Jess> ");
            m_rete.getOutStream().flush();
        }

        while (!val.equals(Funcall.EOF)) {
            oldval = val;
            val = parseSexp(context);

            if (prompt) {
                if (!val.equals(Funcall.NIL))
                {
                    if (val.type() == RU.LIST)
                    // Add parens to list
                        m_rete.getOutStream().print('(');

                    m_rete.getOutStream().print(val);

                    if (val.type() == RU.LIST)
                        m_rete.getOutStream().print(')');

                    m_rete.getOutStream().println();
                }
                m_rete.getOutStream().print("Jess> ");
                m_rete.getOutStream().flush();
            }
        }
        return oldval;
    }

    /**
     * Flush any partially-parsed information, probably to the next
     * ')'. Useful in error recovery.  */

    public void clear() {
        m_jts.clear();
    }

    /**
     * Parses an input file containing only facts, asserts each one.
     * @exception JessException If an error occurs
     * @return The symbol TRUE
     */
    public Value loadFacts(Context c) throws JessException {
        JessToken jt = m_jts.nextToken();

        while (jt.m_ttype != RU.NONE)
            {
                m_jts.pushBack(jt);
                Fact f = parseFact();
                m_rete.assertFact(f, c);
                jt = m_jts.nextToken();
            }

        return Funcall.TRUE;
    }

    /**
     * parseSexp
     *
     * Syntax:
     * ( -Something- )
     * @exception JessException
     * @return
     */
    private Value parseSexp(Context context) throws JessException {
        try {
            JessToken jt = m_jts.nextToken();
            switch (jt.m_ttype) {
            case RU.ATOM: case RU.STRING: case RU.INTEGER: case RU.FLOAT:
            case RU.VARIABLE: case RU.MULTIVARIABLE:
                return jt.tokenToValue(context);
            case '(':
                m_jts.pushBack(jt);
                break;
            case RU.NONE:
                if ("EOF".equals(jt.m_sval))
                    return Funcall.EOF;
            default:
                throw new JessException("Jesp.parseSexp",
                                        "Expected a '(', constant, or global variable",
                                        jt.toString());
            }

            String head = m_jts.head();

            if (head.equals("defrule"))
                return parseDefrule(context);

            else if (head.equals("defquery"))
                return parseDefquery(context);

            else if (head.equals("deffacts"))
                return parseDeffacts();

            else if (head.equals("deftemplate"))
                return parseDeftemplate(context);

            else if (head.equals("deffunction"))
                return parseDeffunction();

            else if (head.equals("defglobal"))
                return parseDefglobal();

            else if (head.equals("defmodule"))
                return parseDefmodule();

            else if (head.equals("EOF"))
                return Funcall.EOF;

            else
                return parseAndExecuteFuncall(null, context);
        }
        catch (JessException re) {
            if (re instanceof ParseException)
                throw re;
            else {
                re.setLineNumber(m_jts.lineno());
                re.setProgramText(m_jts.toString());
                m_jts.clear();
                throw re;
            }
        }
    }

    /**
     * parseDefmodule
     *
     * Syntax:
     * (defmodule modulename "Comment")
     * @exception JessException
     * @return
     */
    private Value parseDefmodule() throws JessException  {
        /* ****************************************
           '(defmodule'
           **************************************** */

        if (  (m_jts.nextToken().m_ttype != '(') ||
              ! (m_jts.nextToken().m_sval.equals("defmodule")) )
            parseError("parseDefmodule", "Expected (defmodule...");

        JessToken name = m_jts.nextToken();
        if (name.m_ttype != RU.ATOM)
            parseError("parseDefmodule", "Expected module name");

        JessToken next = m_jts.nextToken();

        if (next.m_ttype == RU.STRING) {
            m_rete.addDefmodule(name.m_sval, next.m_sval);

        } else if (next.m_ttype == ')') {
            m_rete.addDefmodule(name.m_sval);

        } else
            parseError("parseDefmodule", "Expected ')'");


        return Funcall.TRUE;
    }

    /**
     * parseDefglobal
     *
     * Syntax:
     * (defglobal ?x = 3 ?y = 4 ... )
     * @exception JessException
     * @return
     */
    private Value parseDefglobal() throws JessException
    {
        /* ****************************************
           '(defglobal'
           **************************************** */

        if (  (m_jts.nextToken().m_ttype != '(') ||
              ! (m_jts.nextToken().m_sval.equals("defglobal")) )
            parseError("parseDefglobal", "Expected (defglobal...");


        /* ****************************************
           varname = value sets
           **************************************** */

        JessToken name, value;
        while ((name = m_jts.nextToken()).m_ttype != ')')
            {

                if (name.m_ttype != RU.VARIABLE)
                    parseError("parseDefglobal", "Expected a variable name");

                // Defglobal names must start and end with an asterisk!
                if (name.m_sval.charAt(0) != '*' ||
                    name.m_sval.charAt(name.m_sval.length() -1) != '*')
                    parseError("parseDefglobal", "Defglobal names must start and " +
                               "end with an asterisk!");

                if (m_jts.nextToken().m_ttype != '=')
                    parseError("parseDefglobal", "Expected =");

                value = m_jts.nextToken();

                switch (value.m_ttype)
                    {

                    case RU.ATOM:
                    case RU.STRING:
                    case RU.VARIABLE:
                    case RU.MULTIVARIABLE:
                    case RU.FLOAT:
                    case RU.INTEGER:
                    case '(':
                        m_rete.addDefglobal(new Defglobal(name.m_sval,
                                                          tokenToValue(value)));
                        break;

                    default:
                        parseError("parseDefglobal", "Bad value");
                    }
            }

        return Funcall.TRUE;

    }

    /**
     * parseFuncall
     *
     * Syntax:
     * (functor field2 (nested funcall) (double (nested funcall)))
     *
     * Trick: If the functor is a variable, we insert the functor 'call'
     * and assume we're going to make an outcall to Java on the object in
     * the variable!
     * @exception JessException
     * @return
     */
    private Funcall parseFuncall() throws JessException
    {
        JessToken tok;
        String name;
        Funcall fc = null;

        if (m_jts.nextToken().m_ttype != '(')
            parseError("parseFuncall", "Expected '('");

        /* ****************************************
           functor
           **************************************** */
        tok = m_jts.nextToken();
        switch (tok.m_ttype)
            {

            case RU.ATOM:
                fc = new Funcall(tok.m_sval, m_rete);
                break;

            case '=':
                // special functors
                fc = new Funcall("=".intern(), m_rete);
                break;

            case RU.VARIABLE:
                // insert implied functor
                fc = new Funcall(JAVACALL, m_rete);
                fc.add(new Variable(tok.m_sval, RU.VARIABLE));
                break;

            case '(':
                // insert implied functor
                fc = new Funcall(JAVACALL, m_rete);
                m_jts.pushBack(tok);
                Funcall fc2 = parseFuncall();
                fc.add(new FuncallValue(fc2));
                break;

            default:
                parseError("parseFuncall", "Bad functor");
            }

        name = fc.get(0).stringValue(null);

        /* ****************************************
           arguments
           **************************************** */
        tok = m_jts.nextToken();
        while (tok.m_ttype != ')')
            {

                switch (tok.m_ttype)
                    {
                        // simple arguments
                    case RU.ATOM: case RU.STRING:
                        if ((name.equals("run-query") ||
                                name.equals("count-query-results")) && fc.size() == 1) {
                            tok.m_sval = m_rete.resolveName(tok.m_sval);
                        }
                        // FALL THROUGH
                    case RU.VARIABLE: case RU.MULTIVARIABLE:
                    case RU.FLOAT: case RU.INTEGER:
                        fc.add(tokenToValue(tok));
                        break;

                        // nested funcalls
                    case '(':
                        m_jts.pushBack(tok);
                        if (name.equals("assert")) {
                            Fact fact = parseFact();
                            fc.add(new FactIDValue(fact));
                            break;

                        } else if ((name.equals("modify") ||
                                name.equals("duplicate"))
                                && fc.size() > 1)  {
                            ValueVector pair = parseValuePair();
                            fc.add(new Value(pair, RU.LIST));
                            break;

                        } else {
                            Funcall fc2 = parseFuncall();
                            fc.add(new FuncallValue(fc2));
                            break;
                        }

                    case RU.NONE:
                        // EOF during eval
                        parseError("parseFuncall", "Unexpected EOF");
                        break;

                    default:
                        fc.add(new Value(String.valueOf((char) tok.m_ttype), RU.STRING));
                        break;

                    } // switch tok.m_ttype
                tok = m_jts.nextToken();
            } // while tok.m_ttype != ')'

        return fc;
    }

    /**
     * parseValuePair
     * These are used in (modify) funcalls and salience declarations
     *
     * Syntax:
     * (ATOM VALUE)
     * @exception JessException
     * @return
     */
    private ValueVector parseValuePair() throws JessException
    {
        ValueVector pair = new ValueVector(2);
        JessToken tok = null;

        /* ****************************************
           '(atom'
           **************************************** */

        if (m_jts.nextToken().m_ttype != '(' ||
            (tok = m_jts.nextToken()).m_ttype != RU.ATOM)
            {
                parseError("parseValuePair", "Expected '( <atom>'");
            }

        pair.add(new Value(tok.m_sval, RU.ATOM));

        /* ****************************************
           value
           **************************************** */
        do
            {
                switch ((tok = m_jts.nextToken()).m_ttype)
                    {
                    case RU.ATOM:
                    case RU.STRING:
                    case RU.VARIABLE:
                    case RU.MULTIVARIABLE:
                    case RU.FLOAT:
                    case RU.INTEGER:
                    case '(':
                        pair.add(tokenToValue(tok));
                        break;

                    case ')':
                        break;

                    default:
                        parseError("parseValuePair", "Bad argument");
                    }
            }
        while (tok.m_ttype != ')');

        return pair;
    }


    /**
     * parseDeffacts
     *
     * Syntax:
     * (deffacts <name> ["comment"] (fact) [(fact)...])
     * @exception JessException
     * @return
     */
    private Value parseDeffacts() throws JessException
    {
        Deffacts df;
        JessToken tok;

        /* ****************************************
           '(deffacts'
           **************************************** */

        if (m_jts.nextToken().m_ttype != '(' ||
            (tok = m_jts.nextToken()).m_ttype != RU.ATOM ||
            !tok.m_sval.equals("deffacts"))
            {
                parseError("parseDeffacts", "Expected '( deffacts'");
            }

        /* ****************************************
           deffacts name
           **************************************** */

        if ((tok = m_jts.nextToken()).m_ttype != RU.ATOM)
            parseError("parseDeffacts", "Expected deffacts name");
        String name = tok.m_sval;

        tok = m_jts.nextToken();

        /* ****************************************
           optional comment
           **************************************** */

        String docstring = "";
        if (tok.m_ttype == RU.STRING)
            {
                docstring = tok.m_sval;
                tok = m_jts.nextToken();
            }

        df = new Deffacts(name, docstring, m_rete);
        m_rete.setCurrentModule(df.getModule());

        /* ****************************************
           list of facts
           **************************************** */

        while (tok.m_ttype == '(')
            {
                m_jts.pushBack(tok);
                Fact f = parseFact();
                df.addFact(f);
                tok = m_jts.nextToken();
            }

        /* ****************************************
           closing paren
           **************************************** */

        if (tok.m_ttype != ')')
            parseError("parseDeffacts", "Expected ')'");

        m_rete.addDeffacts(df);
        return Funcall.TRUE;

    }

    /**
     * parseFact
     *
     * This is called from the parse routine for Deffacts and from the
     * Funcall parser for 'assert'; because of this latter, it can have
     * variables that need expanding.
     *
     * Syntax:
     * ordered facts: (atom field1 2 "field3")
     * NOTE: We now turn these into unordered facts with a single slot "__data"
     * unordered facts: (atom (slotname value) (slotname value2))
     * @exception JessException
     * @return
     */
    Fact parseFact() throws JessException
    {
        String name, slot=RU.DEFAULT_SLOT_NAME;
        int slot_type;
        Fact f;
        JessToken tok = null;

        /* ****************************************
           '( atom'
           **************************************** */

        if (m_jts.nextToken().m_ttype != '(' ||
            (tok = m_jts.nextToken()).m_ttype != RU.ATOM)
            parseError("parseFact", "Expected '( <atom>'");

        name = tok.m_sval;

        /* ****************************************
           slot data
           What we do next depends on whether we're parsing an
           ordered or unordered fact. We can determine this very easily:
           If there is a deftemplate, use it; if the first slot is named
           "__data", this is unordered, else ordered. If there is no
           deftemplate, assume ordered.
           **************************************** */

        // get a deftemplate if one already exists.
        boolean ordered = false;

        Deftemplate deft = m_rete.createDeftemplate(name);
        if (deft.getSlotIndex(RU.DEFAULT_SLOT_NAME) == 0)
            ordered = true;

        /* ****************************************
           SLOT DATA
           **************************************** */
        f = new Fact(name, m_rete);
        tok = m_jts.nextToken();

        while (tok.m_ttype != ')')
            {

                if (!ordered)
                    {
                        // Opening parenthesis
                        if (tok.m_ttype != '(')
                            parseError("parseFact", "Expected '('");

                        // Slot name
                        if  ((tok = m_jts.nextToken()).m_ttype != RU.ATOM)
                            parseError("parseFact", "Bad slot name");
                        slot = tok.m_sval;
                        tok = m_jts.nextToken();
                    }

                // Is this a slot or a multislot?
                int idx = deft.getSlotIndex(slot);
                if (idx == -1)
                    throw new JessException("Jesp.parseFact",
                                            "No such slot " + slot +
                                            " in template",
                                            deft.getName());

                slot_type = deft.getSlotType(idx);

                switch (slot_type)
                    {

                        // Data in normal slot
                    case RU.SLOT:
                        switch (tok.m_ttype)
                            {

                            case RU.ATOM:
                            case RU.STRING:
                            case RU.VARIABLE:
                            case RU.MULTIVARIABLE:
                            case RU.FLOAT:
                            case RU.INTEGER:
                                f.setSlotValue(slot, tokenToValue(tok)); break;

                            case '=':
                                tok = m_jts.nextToken();
                                if (tok.m_ttype != '(')
                                    throw new JessException("Jesp.parseFact",
                                                            "'=' cannot appear as an " +
                                                            "atom within a fact", "");
                                // FALLTHROUGH
                            case '(':
                                {
                                    m_jts.pushBack(tok);
                                    Funcall fc = parseFuncall();
                                    f.setSlotValue(slot, new FuncallValue(fc)); break;
                                }

                            default:
                                parseError("parseFact", "Bad slot value");
                            }

                        if  ((tok = m_jts.nextToken()).m_ttype != ')')
                            parseError("parseFact", "Expected ')'");
                        break;

                    case RU.MULTISLOT:
                        // Data in multislot. Code is very similar, but bits of
                        // data are added to a multifield
                        ValueVector slot_vv = new ValueVector();

                        while (tok.m_ttype != ')')
                            {
                                switch (tok.m_ttype)
                                    {

                                    case RU.ATOM:
                                    case RU.STRING:
                                    case RU.VARIABLE:
                                    case RU.MULTIVARIABLE:
                                    case RU.FLOAT:
                                    case RU.INTEGER:
                                        slot_vv.add(tokenToValue(tok)); break;

                                    case '=':
                                        tok = m_jts.nextToken();
                                        if (tok.m_ttype != '(')
                                            throw new JessException("Jesp.parseFact",
                                                                    "'=' cannot appear as an " +
                                                                    "atom within a fact", "");
                                        // FALLTHROUGH
                                    case '(':
                                        {
                                            m_jts.pushBack(tok);
                                            Funcall fc = parseFuncall();
                                            slot_vv.add(new FuncallValue(fc)); break;
                                        }

                                    default:
                                        parseError("parseFact", "Bad slot value");
                                    }

                                tok = m_jts.nextToken();

                            }
                        f.setSlotValue(slot, new Value(slot_vv, RU.LIST));
                        break;

                    default:
                        parseError("parseFact", "No such slot in deftemplate");
                    }

                if (!ordered)
                    {
                        // hopefully advance to next ')'
                        tok = m_jts.nextToken();
                    }
                else
                    break;
            }

        if (tok.m_ttype != ')')
            parseError("parseFact", "Expected ')'");

        return f;

    }

    /**
     * parseDeftemplate
     *
     * Syntax:
     * (deftemplate (slot foo (default <value>)) (multislot bar))
     * @exception JessException
     * @return
     */
    private Value parseDeftemplate(Context context) throws JessException
    {
        Deftemplate dt;
        int slot_type;
        Value default_value;
        String default_type;
        JessToken tok;

        /* ****************************************
           '(deftemplate'
           **************************************** */

        if (  (m_jts.nextToken().m_ttype != '(') ||
              ! (m_jts.nextToken().m_sval.equals("deftemplate")) )
            parseError("parseDeftemplate", "Expected (deftemplate...");

        /* ****************************************
           deftemplate name, optional extends clause
           **************************************** */

        if ((tok = m_jts.nextToken()).m_ttype != RU.ATOM)
            parseError("parseDeftemplate", "Expected deftemplate name");

        String name = tok.m_sval;
        String docstring = "";
        String parent = null;

        if ((tok = m_jts.nextToken()).m_ttype == RU.ATOM)
            {
                if (tok.m_sval.equals("extends"))
                    {
                        tok = m_jts.nextToken();
                        if (tok.m_ttype == RU.ATOM)
                            parent = tok.m_sval;
                        else
                            parseError("parseDeftemplate", "Expected deftemplate name to extend");
                    }
                else
                    parseError("parseDeftemplate", "Expected '(' or 'extends'");
                tok = m_jts.nextToken();
            }

        /* ****************************************
           optional comment
           **************************************** */

        if (tok.m_ttype == RU.STRING)
            {
                docstring = tok.m_sval;
                tok = m_jts.nextToken();
            }

        if (parent == null)
            dt = new Deftemplate(name, docstring, m_rete);
        else
            dt = new Deftemplate(name, docstring,
                                 m_rete.findDeftemplate(parent), m_rete);

        /* ****************************************
           individual slot descriptions
           **************************************** */

        // ( <slot type>

        while (tok.m_ttype == '(')
            { // 'slot'
                if ((tok = m_jts.nextToken()).m_ttype != RU.ATOM ||
                    !(tok.m_sval.equals("slot") || tok.m_sval.equals("multislot")))
                    parseError("parseDeftemplate", "Bad slot type");

                slot_type = tok.m_sval.equals("slot") ? RU.SLOT : RU.MULTISLOT;

                // <slot name>
                if ((tok = m_jts.nextToken()).m_ttype != RU.ATOM)
                    parseError("parseDeftemplate", "Bad slot name");
                name = tok.m_sval;

                // optional slot qualifiers

                default_value = (slot_type == RU.SLOT) ?
                    Funcall.NIL : Funcall.NILLIST;

                default_type = "ANY";

                tok = m_jts.nextToken();
                while (tok.m_ttype == '(')
                    { // slot qualifier
                        if ((tok = m_jts.nextToken()).m_ttype != RU.ATOM)
                            parseError("parseDeftemplate", "Slot qualifier must be atom");

                        // default value qualifier

                        String option = tok.m_sval;

                        if (option.equalsIgnoreCase("default") ||
                            option.equalsIgnoreCase("default-dynamic"))
                            {
                                tok = m_jts.nextToken();
                                switch (tok.m_ttype)
                                    {

                                    case RU.ATOM:
                                    case RU.STRING:
                                    case RU.FLOAT:
                                    case RU.INTEGER:
                                        default_value = tokenToValue(tok); break;

                                    case '(':
                                        if (option.equalsIgnoreCase("default-dynamic"))
                                            {
                                                m_jts.pushBack(tok);
                                                Funcall fc = parseFuncall();
                                                default_value = new FuncallValue(fc);
                                            }
                                        else
                                            default_value =
                                                parseAndExecuteFuncall(tok, context);
                                        break;

                                    default:
                                        parseError("parseDeftemplate",
                                                   "Illegal default slot value");
                                    }
                            }
                        else if (option.equalsIgnoreCase("type"))
                            {
                                if (slot_type == RU.MULTISLOT)
                                    parseError("parseDeftemplate",
                                               "'type' not allowed for multislots");

                                // type is allowed; we save the value, but otherwise ignore it.
                                tok = m_jts.nextToken();
                                default_type = tok.m_sval;
                            }
                        else
                            parseError("parseDeftemplate", "Unimplemented slot qualifier");

                        if ((m_jts.nextToken()).m_ttype != ')')
                            parseError("parseDeftemplate", "Expected ')'");

                        tok = m_jts.nextToken();
                    }
                if (tok.m_ttype != ')')
                    parseError("parseDeftemplate", "Expected ')'");

                if (slot_type == RU.SLOT)
                    dt.addSlot(name, default_value, default_type);
                else
                    {
                        if (default_value.type() != RU.LIST)
                            parseError("parseDeftemplate", "Default value for multislot " +
                                       name + " is not a multifield: " + default_value);
                        dt.addMultiSlot(name, default_value);
                    }

                tok = m_jts.nextToken();
            }
        if (tok.m_ttype != ')')
            parseError("parseDeftemplate", "Expected ')'");

        m_rete.addDeftemplate(dt);
        return Funcall.TRUE;
    }


    /**
     * parseDefrule
     * Wrapper around doParseDefrule
     * We're going to split defrules into multiple rules if we see an (or) CE
     *
     * @exception JessException
     * @return
     */
    private Value parseDefrule(Context context) throws JessException
    {
        Value v;
        v = doParseDefrule(context);
        return v;
    }

    /**
     * doParseDefrule
     *
     * Syntax:
     * (defrule name
     * [ "docstring...." ]
     * [ (declare [(salience 1)] [(node-index-hash 57)]) ]
     * (pattern 1)
     * ?foo <- (pattern 2)
     * (pattern 3)
     * =>
     * (action 1)
     * (action ?foo)
     * )
     * @exception JessException
     * @return
     */

    private synchronized Value doParseDefrule(Context context)
        throws JessException {
        JessToken tok;

        String nameAndDoc[] = parseNameAndDocstring("defrule");

        /* ****************************************
         * check for salience declaration
         **************************************** */

        Hashtable declarations = new Hashtable();
        parseDeclarations(declarations);

        /* ****************************************
         * Parse all the LHS patterns
         **************************************** */
        String module = RU.getModuleFromName(nameAndDoc[0], m_rete);
        m_rete.setCurrentModule(module);
        LHSComponent patterns = parseLHS(module);

        /* ****************************************
         * should be looking at "=>"
         **************************************** */
        tok = m_jts.nextToken();
        if (tok.m_ttype != '=' ||
            (tok = m_jts.nextToken()).m_ttype != RU.ATOM ||
            !tok.m_sval.equals(">")) {
            parseError("parseDefrule", "Expected '=>'");
        }

        /* ****************************************
         * Parse RHS actions
         **************************************** */
        ArrayList actions = parseActions();

        /* ****************************************
         * Should be looking at the closing paren
         **************************************** */
        expect(')', ")");

        /* ****************************************
         * All parsed. Now build the rule(s)
         **************************************** */
        Defrule previous = null;
        LHSComponent g = patterns.canonicalize();

        if (!g.getName().equals(Group.OR))
            throw new JessException("Jesp.parseDefrule", "Bogus assumption", "");

        for (int i=0; i<g.getGroupSize(); ++i) {
            LHSComponent newPatterns = g.getLHSComponent(i);
            Group flat = new Group(Group.AND);
            newPatterns.addToGroup(flat);
            previous = addARule(flat, nameAndDoc, i, declarations,
                                actions, previous, context);
        }

        return Funcall.TRUE;
    }

    private Defrule addARule(Group newPatterns, String[] nameAndDoc,
                             int i, Hashtable declarations, ArrayList actions,
                             Defrule previous, Context context)
        throws JessException {

        String name = nameAndDoc[0];
        if (i > 0)
            name += "&" + i;
        Defrule dr = new Defrule(name, nameAndDoc[1], m_rete);

        // install declarations
        for (Enumeration e = declarations.keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            ValueVector vv = (ValueVector) declarations.get(key);
            if (key.equals("salience"))
                dr.setSalience(vv.get(1), m_rete);

            else if (key.equals("node-index-hash"))
                dr.setNodeIndexHash(vv.get(1).intValue(context));

            else if (key.equals("auto-focus")) {
                String val = vv.get(1).atomValue(context);
                dr.setAutoFocus(!val.equals(Funcall.FALSE));
            }
            else
                parseError("parseDefrule", "Invalid declarand: " + key);
        }

        newPatterns.addToLHS(dr, m_rete);

        // Install actions
        for (int j=0; j<actions.size(); j++)
            dr.addAction((Funcall) actions.get(j));

        // Add the rule to the engine
        if (previous != null)
            previous.setNext(dr);
        m_rete.addDefrule(dr);
        return dr;
    }

    private String[] parseNameAndDocstring(String construct)
        throws JessException {
        JessToken tok;

        /* ****************************************
           '(defrule'
           **************************************** */

        if (  (m_jts.nextToken().m_ttype != '(') ||
              ! (m_jts.nextToken().m_sval.equals(construct)) )
            parseError("parseNameAndDocstring", "Expected " + construct);

        /* ****************************************
           defrule name, optional comment
           **************************************** */

        if ((tok = m_jts.nextToken()).m_ttype != RU.ATOM)
            parseError("parseNameAndDocstring", "Expected defrule name");
        String name = tok.m_sval;

        String docstring = "";
        if ((tok = m_jts.nextToken()).m_ttype == RU.STRING)
            docstring = tok.m_sval;
        else
            m_jts.pushBack(tok);

        return new String[] { name, docstring };
    }

    private Hashtable parseDeclarations(Hashtable declarations)
        throws JessException {

        JessToken tok, tok2;
        // Consume the paren
        tok = m_jts.nextToken();
        tok2 = m_jts.nextToken();

        if (tok2.m_ttype == RU.ATOM && tok2.m_sval.equals("declare")) {
            while ((tok2 = m_jts.nextToken()).m_ttype != ')') {
                m_jts.pushBack(tok2);
                ValueVector vv = parseValuePair();

                String head = vv.get(0).atomValue(null);
                declarations.put(head, vv);
            }

        } else { // head wasn't 'declare'
            m_jts.pushBack(tok2);
            m_jts.pushBack(tok);
        }
        return declarations;
    }


    /**
     * parseActions
     * Parses the RHSs of rules
     */

    private ArrayList parseActions() throws JessException {
        JessToken tok = m_jts.nextToken();
        ArrayList actions = new ArrayList();
        while (tok.m_ttype == '(') {
            m_jts.pushBack(tok);
            Funcall f = parseFuncall();
            actions.add(f);
            tok = m_jts.nextToken();
        }
        m_jts.pushBack(tok);
        return actions;
    }

    /**
     * parseLHS
     * Parses the patterns of a defrule or defquery.
     */

    private LHSComponent parseLHS(String module) throws JessException {
        // **************************************************
        // We need to keep track of the type of each variable, since
        // CLIPS code lets you omit the second and later '$' before multivars.
        // This only matters when a multivar is actualy matched against, since
        // if the '$' is omitted, a TMF node won't get generated. We'll
        // therefore 'put the $'s back in' as needed. This table is shared
        // across all patterns in a rule.
        // **************************************************

        Hashtable varnames = new Hashtable();
        Group patterns = new Group(Group.AND);

        // now we're looking for just patterns
        JessToken tok = m_jts.nextToken();
        while (tok.m_ttype == '(' || tok.m_ttype == RU.VARIABLE) {
            m_jts.pushBack(tok);

            LHSComponent p = parsePattern(varnames, module);
            patterns.add(p);
            tok = m_jts.nextToken();
        }
        m_jts.pushBack(tok);
        return patterns;
    }

    private JessToken expect(int type, String value) throws JessException {
        JessToken tok = m_jts.nextToken();
        if (tok.m_ttype != type ||
            !tok.m_sval.equals(value))
            parseError("parseLHS", "Expected '" + value + "'");
        return tok;
    }


    /**
     * parsePattern
     *
     * parse a Pattern object in a Rule LHS context
     *
     * Syntax:
     * Like that of a fact, except that values can have complex forms like
     *
     * ~value       (test for not a value)
     * ?X&~red      (store the value in X; fail match if not red)
     * ?X&:(> ?X 3) (store the value in X; fail match if not greater than 3)
     * @param varnames
     * @exception JessException
     * @return
     */
    LHSComponent parsePattern(Hashtable varnames, String module)
        throws JessException {
        String name, slot = RU.DEFAULT_SLOT_NAME;
        String patternBinding = null;
        JessToken tok = m_jts.nextToken();

        if (tok.m_ttype == RU.VARIABLE) {

                // pattern bound to a variable
                // These look like this:
                // ?name <- (pattern 1 2 3)

                patternBinding = tok.m_sval;
                expect(RU.ATOM, "<-");
                tok = m_jts.nextToken();
            }


        /* ****************************************
           ' ( <atom> '
           **************************************** */

        if (  (tok.m_ttype != '(') ||
              ! ((tok = m_jts.nextToken()).m_ttype == RU.ATOM))
            parseError("parsePattern", "Expected '( <atom>'");

        name = tok.m_sval;

        /* ****************************************
           Special handling for grouping CEs
           **************************************** */

        if (name.equals(Group.EXISTS)) {
            Group inner = new Group(Group.NOT);
            while ((tok = m_jts.nextToken()).m_ttype != ')') {
                m_jts.pushBack(tok);
                inner.add(parsePattern(varnames, module));
            }
            Group outer = new Group(Group.NOT);
            outer.add(inner);
            if (patternBinding != null)
                outer.setBoundName(patternBinding);
            return outer;
        }

        if (name.equals(Group.UNIQUE)) {
            LHSComponent pattern = parsePattern(varnames, module);
            if (m_jts.nextToken().m_ttype != ')')
                parseError("parsePattern", "Expected ')'");
            if (patternBinding != null)
                pattern.setBoundName(patternBinding);
            return pattern;
        }

        if (Group.isGroupName(name)) {
            Group g = new Group(name);
            while ((tok = m_jts.nextToken()).m_ttype != ')') {
                m_jts.pushBack(tok);
                g.add(parsePattern(varnames, module));
            }

            if (patternBinding != null)
                g.setBoundName(patternBinding);
            return g;
        }

        /* ****************************************
           Special handling for TEST CEs
           Note that these can be nested inside of NOTs.
           **************************************** */

        else if (name.equals(Group.TEST)) {
            // this is a 'test' pattern. We trick up a fake one-slotted
            // pattern which will get treated specially by the compiler.
            if (patternBinding != null)
                parseError("parsePattern",
                           "Can't bind a 'test' CE to a variable");

            Pattern p = new Pattern(name, m_rete);

            Funcall f = parseFuncall();

            p.addTest(RU.DEFAULT_SLOT_NAME,
                      new Test1(TestBase.EQ, -1, new FuncallValue(f)));

            if (m_jts.nextToken().m_ttype != ')')
                parseError("parsePattern", "Expected ')'");

            return p;
        }

        /* ****************************************
           What we do next depends on whether we're parsing
           an ordered or unordered fact.
           **************************************** */
        Pattern p;
        boolean ordered = false;

        Deftemplate deft = m_rete.createDeftemplate(name);
        name = deft.getName();
        if (deft.getSlotIndex(RU.DEFAULT_SLOT_NAME) == 0)
            ordered = true;

        /* ****************************************
           Actual pattern slot data
           **************************************** */
        p = new Pattern(name, m_rete);
        tok = m_jts.nextToken();
        while (ordered || tok.m_ttype == '(') {

            if (!ordered) {
                if ((tok = m_jts.nextToken()).m_ttype != RU.ATOM)
                    parseError("parsePattern", "Bad slot name");
                slot = tok.m_sval;
                tok = m_jts.nextToken();
            }

            int index = deft.getSlotIndex(slot);
            if (index == -1)
                throw new JessException("Jesp.parsePattern",
                                        "No such slot " + slot +
                                        " in template",
                                        deft.getName());

            boolean multislot = (deft.getSlotType(index) == RU.MULTISLOT);

            int subidx = (multislot ? 0 : -1);
            int nextConjunction = RU.AND;
            Test1 aTest = null;
            while (tok.m_ttype != ')')
                {

                    // if this is a '~'  pattern, keep track
                    boolean not_slot = false;
                    if (tok.m_ttype == '~') {
                        not_slot = true;
                        tok = m_jts.nextToken();
                    }

                    switch (tok.m_ttype) {
                    case RU.VARIABLE: case RU.MULTIVARIABLE:
                        // Fix type if necessary - lets you omit the '$' on
                        // second and later occurrences of multivars.
                        Integer type = (Integer) varnames.get(tok.m_sval);
                        if (type == null)
                            varnames.put(tok.m_sval, new Integer(tok.m_ttype));
                        else
                            tok.m_ttype = type.intValue();

                        aTest = new Test1(not_slot ? TestBase.NEQ : TestBase.EQ, subidx,
                                          new Variable(tok.m_sval, tok.m_ttype));
                        break;

                    case RU.ATOM:

                        if (tok.m_sval.equals(":")) {
                            Funcall f = parseFuncall();
                            aTest = new Test1(not_slot ? TestBase.NEQ : TestBase.EQ, subidx,
                                              new FuncallValue(f));
                            break;
                        }
                        // FALL THROUGH

                    case RU.STRING:
                    case RU.FLOAT:
                    case RU.INTEGER:
                        aTest = new Test1(not_slot ? TestBase.NEQ : TestBase.EQ, subidx,
                                          tokenToValue(tok));
                        break;

                        // We're going to handle these by transforming them into
                        // predicate constraints.

                    case '=': {
                        Funcall inner = parseFuncall();

                        // We're building (eq* <this-slot> <inner>)
                        Funcall outer = new Funcall("eq*", m_rete);

                        // We need the variable that refers to this slot
                        Value var = null;
                        int idx = p.getDeftemplate().getSlotIndex(slot);

                        if (idx == -1)
                            throw new JessException("Jesp.parsePattern",
                                                    "No such slot " + slot +
                                                    " in template",
                                                    deft.getName());

                        if (p.getNTests(idx) > 0) {
                            Test1 t1 = p.getTest(idx, 0);
                            if (t1.getTest() == TestBase.EQ) {
                                Value var2 = t1.getValue();
                                if (var2.type() == RU.VARIABLE && t1.m_subIdx == subidx)
                                    var = var2;
                            }

                        }

                        if (var == null) {
                            var = new Variable(RU.gensym("__jesp"), RU.VARIABLE);
                            p.addTest(slot, new Test1(TestBase.EQ, subidx, var));
                        }

                        // Finish up the Funcall
                        outer.add(var);
                        outer.add(new FuncallValue(inner));

                        aTest = new Test1(not_slot ? TestBase.NEQ : TestBase.EQ, subidx,
                                          new FuncallValue(outer));
                    }
                    break;

                    default:
                        parseError("parsePattern", "Bad slot value");
                    }

                    tok = m_jts.nextToken();

                    aTest.m_conjunction = nextConjunction;

                    if (tok.m_ttype == '&')
                        tok = m_jts.nextToken();


                    else if (tok.m_ttype == '|') {
                        nextConjunction = RU.OR;
                        tok = m_jts.nextToken();
                    }

                    else
                        if (!multislot && tok.m_ttype != ')')
                            parseError("parsePattern", slot + " is not a multislot");
                        else {
                            ++subidx;
                            nextConjunction = RU.AND;
                        }

                    p.addTest(slot, aTest);
                }

            if (multislot)
                p.setSlotLength(slot, subidx);

            if (!ordered)
                tok = m_jts.nextToken();
            else
                break;

        }

        if (patternBinding != null)
            p.setBoundName(patternBinding);

        return p;
    }

    /**
     * parseDefquery
     *
     * Syntax:
     * (defquery name
     * [ "docstring...." ]
     * [(declare (variables ?var1 ?var2 ...))]
     * (pattern))
     * @exception JessException
     * @return
     */

    private synchronized Value parseDefquery(Context context)
        throws JessException {

        JessToken tok;

        String nameAndDoc[] = parseNameAndDocstring("defquery");

        // Parse variable, node-index-hash declarations
        Hashtable declarations = new Hashtable();
        parseDeclarations(declarations);

        //Parse all the LHS patterns
        String module = RU.getModuleFromName(nameAndDoc[0], m_rete);
        m_rete.setCurrentModule(module);
        LHSComponent patterns = parseLHS(module);

        // Should be looking at closing paren
        tok = m_jts.nextToken();
        if (tok.m_ttype != ')')
            parseError("parseDefquery", "Expected ')', got " + tok.toString());

        Defquery previous = null;
        LHSComponent g = patterns.canonicalize();

        if (!g.getName().equals(Group.OR))
            throw new JessException("Jesp.parseDefquery", "Bogus assumption", "");

        for (int i=0; i<g.getGroupSize(); ++i) {
            LHSComponent newPatterns = g.getLHSComponent(i);
            Group flat = new Group(Group.AND);
            newPatterns.addToGroup(flat);
            previous = addAQuery(flat, nameAndDoc, i, declarations, previous, context);
        }
        return Funcall.TRUE;
    }

    private Defquery addAQuery(Group newPatterns, String[] nameAndDoc,
                              int i, Hashtable declarations,
                              Defquery previous, Context context)
        throws JessException {
        String name = nameAndDoc[0];
        if (i > 0)
            name += "&" + i;

        Defquery query = new Defquery(name, nameAndDoc[1], m_rete);

        for (Enumeration e = declarations.keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            ValueVector vv = (ValueVector) declarations.get(key);
            if (key.equals("variables")) {
                for (int j=1; j< vv.size(); j++)
                    query.addQueryVariable((Variable) vv.get(j));
            }

            else if (key.equals("node-index-hash"))
                query.setNodeIndexHash(vv.get(1).intValue(context));

            else if (key.equals("max-background-rules"))
                query.setMaxBackgroundRules(vv.get(1).intValue(context));

            else
                parseError("parseDefquery", "Invalid declarand: " + key);
        }

        newPatterns.addToLHS(query, m_rete);

        if (previous != null)
            previous.setNext(query);
        m_rete.addDefrule(query);
        return query;
    }


    /**
     * parseDeffunction
     *
     * Syntax:
     * (deffunction name ["doc-comment"] (<arg1><arg2...) ["doc-comment"]
     * (action)
     * value
     * (action))
     * @exception JessException
     * @return
     */
    private Value parseDeffunction() throws JessException {
        Deffunction df;
        JessToken tok;

        /* ****************************************
           '(deffunction'
           **************************************** */

        if (  (m_jts.nextToken().m_ttype != '(') ||
              ! (m_jts.nextToken().m_sval.equals("deffunction")) )
            parseError("parseDeffunction", "Expected (deffunction...");


        /* ****************************************
           deffunction name
           **************************************** */

        if ((tok = m_jts.nextToken()).m_ttype != RU.ATOM)
            parseError("parseDeffunction", "Expected deffunction name");
        String name = tok.m_sval;

        /* ****************************************
           optional comment
           **************************************** */

        String docstring = "";
        if ((tok = m_jts.nextToken()).m_ttype == RU.STRING) {
            docstring = tok.m_sval;
            tok = m_jts.nextToken();
        }

        df = new Deffunction(name, docstring);

        /* ****************************************
           Argument list
           **************************************** */

        if (tok.m_ttype != '(')
            parseError("parseDeffunction", "Expected '('");

        while ((tok = m_jts.nextToken()).m_ttype == RU.VARIABLE ||
               tok.m_ttype == RU.MULTIVARIABLE)
            df.addArgument(tok.m_sval, tok.m_ttype);

        if (tok.m_ttype != ')')
            parseError("parseDeffunction", "Expected ')'");


        /* ****************************************
           optional comment
           **************************************** */

        if ((tok = m_jts.nextToken()).m_ttype == RU.STRING) {
            df.setDocstring(tok.m_sval);
            tok = m_jts.nextToken();
        }

        /* ****************************************
           function calls and values
           **************************************** */

        while (tok.m_ttype != ')') {
            if (tok.m_ttype == '(') {
                m_jts.pushBack(tok);
                Funcall f = parseFuncall();
                df.addAction(f);

            } else {
                switch (tok.m_ttype) {

                case RU.ATOM:
                case RU.STRING:
                case RU.VARIABLE:
                case RU.MULTIVARIABLE:
                case RU.FLOAT:
                case RU.INTEGER:
                    df.addValue(tokenToValue(tok)); break;

                default:
                    parseError("parseDeffunction", "Unexpected character");
                }
            }
            tok = m_jts.nextToken();
        }

        m_rete.addUserfunction(df);
        return Funcall.TRUE;
    }

    Value parseAndExecuteFuncall(JessToken tok, Context c) throws JessException {
        if (tok != null)
            m_jts.pushBack(tok);
        Funcall fc = parseFuncall();
        m_jts.eatWhitespace();
        return fc.execute(c);

    }

    private Value tokenToValue(JessToken value) throws JessException {
        switch (value.m_ttype) {
        case RU.ATOM: case RU.STRING:
            return new Value(value.m_sval, value.m_ttype);

        case RU.VARIABLE:
        case RU.MULTIVARIABLE:
            return new Variable(value.m_sval, value.m_ttype);

        case RU.FLOAT:
        case RU.INTEGER:
            return new Value(value.m_nval, value.m_ttype);

        case '(':
            m_jts.pushBack(value);
            Funcall fc = parseFuncall();
            return new FuncallValue(fc);

        default:
            return null;
        }
    }

    /**
     * Make error reporting a little more compact.
     */
    private void parseError(String routine, String msg) throws JessException {
        try {
            ParseException p =  new ParseException("Jesp." + routine, msg);
            p.setLineNumber(m_jts.lineno());
            p.setProgramText(m_jts.toString());
            throw p;
        } finally {
            m_jts.clear();
        }
    }
}


/**
 * (C) 1997 Ernest J. Friedman-Hill and Sandia National Laboratories
 * @author Ernest J. Friedman-Hill
 */
class ParseException extends JessException {

    ParseException(String s1, String s2) { super(s1, s2, ""); }
}


