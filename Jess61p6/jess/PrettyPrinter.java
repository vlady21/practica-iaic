package jess;

import java.util.Iterator;

/** **********************************************************************
 * The "ppdefrule" command and such.
 * <P>
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 * @author Ernest J. Friedman-Hill
 ********************************************************************** */

public class PrettyPrinter implements Visitor {

    private Visitable m_visitable;
    private boolean m_inTestCE = false;

    public PrettyPrinter(Visitable v) {
        m_visitable = v;
    }

    public Object visitDeffacts(Deffacts facts) {
        List l = new List("deffacts", facts.getName());

        if (facts.getDocstring() != null &&
            facts.getDocstring().length() > 0)
            l.addQuoted(facts.getDocstring());


        for (int i=0; i<facts.getNFacts(); ++i) {
            l.newLine();
            l.add(facts.getFact(i));
        }

        return l.toString();
    }

    public Object visitDeftemplate(Deftemplate template) {
        List l = new List("deftemplate", template.getName());
        if (template.getParent() != null &&
            template.getParent() != template) {
            l.add("extends");
            l.add(template.getParent().getName());
        }

        if (template.getDocstring() != null &&
            template.getDocstring().length() > 0)
            l.addQuoted(template.getDocstring());

        for (int i=0; i<template.m_data.size(); i+=RU.DT_SLOT_SIZE) {
            try {
                Value val = template.m_data.get(i + RU.DT_SLOT_NAME);
                List slot = new List(val.type() == RU.SLOT ?
                                     "slot" : "multislot", val);
                val = template.m_data.get(i + RU.DT_DFLT_DATA);
                if (!val.equals(Funcall.NIL) && !val.equals(Funcall.NILLIST)) {
                    String kind =
                        val.type() == RU.FUNCALL ?
                        "default-dynamic" :
                        "default";
                    slot.add(new List(kind, val));
                }
                val = template.m_data.get(i + RU.DT_DATA_TYPE);
                if (val.intValue(null) != -1)
                    slot.add(new List("type", val));
                l.newLine();
                l.add(slot);

            } catch (JessException re) {
                l.add(re.toString());
                break;
            }
        }
        return l.toString();
    }

    public Object visitDefglobal(Defglobal global) {
        List l = new List("defglobal");
        l.add("?" + global.getName());
        l.add("=");
        l.add(global.getInitializationValue());
        return l.toString();
    }

    public Object visitDeffunction(Deffunction func) {
        List l = new List("deffunction", func.getName());
        List args = new List();
        for (Iterator it = func.getArguments(); it.hasNext();) {
            Deffunction.Argument a = (Deffunction.Argument) it.next();
            String prefix = a.m_type == RU.VARIABLE ? "?" : "$?";
            args.add(prefix + a.m_name);
        }
        l.add(args);

        if (func.getDocstring() != null && func.getDocstring().length() > 0)
            l.addQuoted(func.getDocstring());

        for (Iterator e = func.getActions(); e.hasNext();) {
            l.newLine();
            l.add(e.next());
        }
        return l.toString();
    }

    public Object visitDefrule(Defrule rule) {
        List list = new List("defrule");
        list.add(rule.getName());
        list.indent("  ");

        if (rule.m_docstring != null && rule.m_docstring.length() > 0) {
            list.newLine();
            list.addQuoted(rule.m_docstring);
        }
        boolean declsAdded = false;
        List declarations = new List("declare");

        try {
            if (! (rule.m_salienceVal.type() == RU.INTEGER &&
                   rule.m_salienceVal.intValue(null) == 0)) {

                List salience = new List("salience");
                salience.add(rule.m_salienceVal);
                declarations.add(salience);
                declsAdded = true;
            }

        } catch (JessException je) {
            // Can't happen
        }

        if (rule.getAutoFocus()) {

            if (declsAdded) {
                declarations.indent("           ");
                declarations.newLine();
            }

            List autoFocus = new List("auto-focus");
            autoFocus.add("TRUE");
            declarations.add(autoFocus);
            declsAdded = true;
        }

        if (declsAdded) {
            list.newLine();
            list.add(declarations);
        }

        for (int i=0; i<rule.getGroupSize(); ++i) {
            LHSComponent lhsc = rule.getLHSComponent(i);
            list.newLine();
            list.add(((Visitable) lhsc).accept(this));
        }
        list.newLine();
        list.add("=>");
        for (int i = 0; i < rule.getNActions(); ++i) {
            list.newLine();
            list.add(rule.getAction(i).toString());
        }

        return list.toString();
    }

    public Object visitGroup(Group g) {
        List list = new List(g.getName());
        for (int i=0; i<g.getGroupSize(); ++i) {
            LHSComponent lhsc = g.getLHSComponent(i);
            list.add(((Visitable) lhsc).accept(this));
        }
        if (g.getBoundName() != null)
            return '?' + g.getBoundName() + " <- " + list.toString();
        else
            return list.toString();
    }

    public Object visitPattern(Pattern p) {
        List list = new List(p.getName());
        Deftemplate dt = p.getDeftemplate();
        m_inTestCE = p.getName().equals("test");
        try {
            for (int i=0; i<p.getNSlots(); ++i) {
                if (p.getNTests(i) != 0) {
                    List slot;
                    if (dt.getSlotName(i).equals(RU.DEFAULT_SLOT_NAME))
                        slot = list;
                    else
                        slot = new List(dt.getSlotName(i));

                    for (int k=-1; k<=p.getSlotLength(i); ++k) {
                        StringBuffer sb = new StringBuffer();
                        for (int j=0; j<p.getNTests(i); ++j){

                            Test1 t = p.getTest(i, j);
                            if (t.m_subIdx == k) {
                                if (sb.length() > 0)
                                    sb.append("&");
                                sb.append(t.accept(this));
                            }
                        }
                        if (sb.length() > 0)
                            slot.add(sb);
                    }
                    if (!dt.getSlotName(i).equals(RU.DEFAULT_SLOT_NAME))
                        list.add(slot);
                }
            }
        } catch (JessException je) {
            list.add(je.getMessage());
        }

        if (p.getBoundName() != null)
            return '?' + p.getBoundName() + " <- " + list.toString();
        else
            return list.toString();
    }

    public Object visitTest1(Test1 t) {
        StringBuffer sb = new StringBuffer();
        if (t.m_test == Test1.NEQ)
            sb.append("~");
        if (t.m_slotValue.type() == RU.FUNCALL && !m_inTestCE)
            sb.append(":");

        sb.append(t.m_slotValue);
        return sb.toString();
    }

    public Object visitDefquery(Defquery query) {
        List list = new List("defquery");
        list.add(query.getName());
        list.indent("  ");

        if (query.m_docstring != null && query.m_docstring.length() > 0) {
            list.newLine();
            list.addQuoted(query.m_docstring);
        }

        if (query.getNVariables() > 0 || query.getMaxBackgroundRules() > 0) {
            list.newLine();
            List declarations = new List("declare");
            if (query.getNVariables() > 0) {
                List variables = new List("variables");
                for (int i=0; i<query.getNVariables(); ++i)
                    variables.add(query.getQueryVariable(i));
                declarations.add(variables);
            }
            if (query.getMaxBackgroundRules() > 0) {
                List background = new List("max-background-rules");
                background.add(String.valueOf(query.getMaxBackgroundRules()));
                declarations.add(background);
            }
            list.add(declarations);
        }

        for (int i=0; i<query.getGroupSize(); ++i) {
            LHSComponent lhsc = query.getLHSComponent(i);
            if (lhsc.getName().indexOf(Defquery.QUERY_TRIGGER) != -1)
                continue;

            list.newLine();
            list.add(((Visitable) lhsc).accept(this));
        }

        return list.toString();
    }

    public String toString() {
        return (String) m_visitable.accept(this);
    }
}





