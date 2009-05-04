package jess;

import java.io.*;
import java.util.Hashtable;

/**
 * Manage input and output routers
 * <P>
 * Routers are kept in two hashtables: input ones and output ones.
 * Names that are read-write are kept in both tables as separate entries.
 * This means we don't need a special 'Router' class.
 * <P>
 * Every input router is wrapped in a BufferedReader so we get reliable
 * treatment of end-of-line. We need to keep track of the association, so
 * we keep the original stream paired with the wrapper in m_inWrappers.
 * <P>
 * Console-like streams act differently than file-like streams under
 * read and readline , so when you create a router, you need to specify
 * how it should act.
 * <P>
 * (C) 1997 E.J. Friedman-Hill and Sandia National Laboratories
 * $Id: Routers.java,v 1.1.1.1 2003/01/08 04:47:21 ejfried Exp $
 */

class Routers {
    
    private Hashtable m_outRouters = new Hashtable(13);
    private Hashtable m_inRouters = new Hashtable(13);
    private Hashtable m_inWrappers = new Hashtable(13);
    private Hashtable m_inModes = new Hashtable(13);

    Routers() {
        addInputRouter("t", new InputStreamReader(System.in), true);
        addOutputRouter("t", new PrintWriter(System.out, false));
        addInputRouter("WSTDIN", getInputRouter("t"), true);
        addOutputRouter("WSTDOUT", getOutputRouter("t"));
        addOutputRouter("WSTDERR", getOutputRouter("t"));
    }

    synchronized void addInputRouter(String s, Reader is,
                                     boolean consoleLike) { 
        Tokenizer t = (Tokenizer) m_inWrappers.get(is);
        if (t == null)
            t = new Tokenizer(is);
        
        m_inRouters.put(s, is);
        m_inWrappers.put(is, t);
        m_inModes.put(s, new Boolean(consoleLike));
    }

    synchronized void removeInputRouter(String s) {
        m_inRouters.remove(s);
    }

    Reader getInputRouter(String s) {
        return (Reader) m_inRouters.get(s);
    }

    Tokenizer getInputWrapper(Reader is) {
        return (Tokenizer) m_inWrappers.get(is);
    }

   boolean getInputMode(String s) {
       return ((Boolean) m_inModes.get(s)).booleanValue();
    }

    synchronized void addOutputRouter(String s, Writer os) {
        m_outRouters.put(s, os);
    }

    synchronized void removeOutputRouter(String s) {
        m_outRouters.remove(s);
    }

    Writer getOutputRouter(String s) {
        return (Writer) m_outRouters.get(s);
    }

    synchronized PrintWriter getErrStream() {
        // Coerce to PrintWriter;
        PrintWriter ps;
        Writer os = getOutputRouter("WSTDERR");
        if (os instanceof PrintWriter)
            ps = (PrintWriter) os;
        else {
            ps = new PrintWriter(os);
            addOutputRouter("WSTDERR", ps);
        }
        return ps;
    }
    
    synchronized PrintWriter getOutStream() {
        // Coerce to PrintWriter;
        PrintWriter ps;
        Writer os = getOutputRouter("WSTDOUT");
        if (os instanceof PrintWriter)
            ps = (PrintWriter) os;
        else {
            ps = new PrintWriter(os);
            addOutputRouter("WSTDOUT", ps);
        }
        return ps;
    }
    

}
 
