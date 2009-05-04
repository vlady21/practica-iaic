package jess;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Label;
import java.io.Serializable;

/**
 *
 * A simple Applet which uses ConsolePanel. It could
 * serve as the basis for any number of 'interview' style Expert System
 * GUIs.
 * <P>
 * Applet Parameters:
 * <UL>
 * <LI> INPUT: if present, the applet will find the named document relative
 *    to the applet's document base and interpret the file in batch mode,
 *    then fall into a parse loop when the file completes.
 * </UL>
 * <P>
 * (C) 2003 E.J. Friedman-Hill and the Sandia Corporation<BR>
 * $Id: ConsoleApplet.java,v 1.3 2003/01/21 03:38:38 ejfried Exp $
 */

public class ConsoleApplet extends Applet implements Runnable, Serializable {
    // The display panel
    private ConsolePanel m_panel;
    // The inference engine
    private Rete m_rete;
    // Thread in which the parse loop runs
    private Thread m_thread;
    // Main object used to drive Rete
    private Main m_main;


    /**
     * Set up the applet's window and process the parameters. Reads any
     * input file and prepares to parse it.
     */
    public void init() {
        setLayout(new BorderLayout());
        m_rete = new Rete(this);
        m_panel = new ConsolePanel(m_rete);
        add("Center", m_panel);
        add("South", new Label());

        // ###
        String[] argv = new String[]{};
        // Process Applet Parameters
        String appParam = getParameter("INPUT");
        if (appParam != null)
            argv = new String[]{appParam};

        m_main = new Main();
        m_main.initialize(argv, m_rete);
    }

    /**
     * Called by this applet to execute Jess in another Thread.
     */
    public synchronized void run() {
        do {
            try {
                m_panel.setFocus();
                while (true)
                    m_main.execute(true);
            } catch (Throwable t) {
                m_thread = null;
            }
        } while (m_thread != null);
    }

    /**
     * Starts the engine running in another thread.
     */
    public void start() {
        if (m_thread == null) {
            // ###
            m_thread = new Thread(this);
            m_thread.start();
        }
    }

    /**
     * Terminates Jess.
     */
    public void stop() {
        m_thread = null;
    }
}

