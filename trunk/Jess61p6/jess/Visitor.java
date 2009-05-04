package jess;

/**
 * Visit some Jess classes. Lets you, for example, print out compelx
 * structures ithout putting the printing code in the structures
 * themselves.
 * $Id: Visitor.java,v 1.1.1.1 2003/01/08 04:47:21 ejfried Exp $
 * (C) 2001 E.J. Friedman-Hill and Sandia National Laboratories
 */

public interface Visitor {
    
    public Object visitDeffacts(Deffacts d);
    public Object visitDeftemplate(Deftemplate d);
    public Object visitDeffunction(Deffunction d);
    public Object visitDefglobal(Defglobal d);
    public Object visitDefrule(Defrule d);
    public Object visitDefquery(Defquery d);
    public Object visitPattern(Pattern p);
    public Object visitGroup(Group p);
    public Object visitTest1(Test1 t);
}
