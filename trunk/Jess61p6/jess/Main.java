package jess;

import java.io.*;
import java.net.URL;

/** **********************************************************************
 * A command-line interface for Jess; also displayed in a window by the
 * Console classes.
 * <P>
 * (C) 1998 E.J. Friedman-Hill and the Sandia Corporation
 ********************************************************************** */

public class Main {

    private Rete m_rete;
    private Reader m_fis;
    private Jesp m_j;
    private boolean m_readStdin = true;
    private boolean m_exitOnError = false;

    public static void main(String[] argv) {
        Main m = new Main();
        m.initialize(argv, new Rete());
        m.execute(true);
    }


    /**
     * Display the Jess startup banner on the Rete object's standard
     * output. It will look something like
     *
     * <PRE>
     * Copyright (C) 2001 E.J. Friedman Hill and the Sandia Corporation
     * Jess Version 6.1 4/15/03
     * </PRE>
     * */

    public void showLogo()
    {
        if (m_rete != null && m_rete.getOutStream() != null) {
            m_rete.getOutStream().println("\nJess, the Java Expert System Shell");
            m_rete.getOutStream().println("Copyright (C) 2001 E.J. Friedman Hill"
                                          + " and the Sandia Corporation");
            try {
                m_rete.executeCommand("(printout t (jess-version-string) crlf crlf)");
                // &&&
            } catch(JessException re) {}
        }
    }

    /**
     * Set a Main object up for later execution.
     * @param argv Command-line arguments
     * @param r An initialized Rete object, with routers set up
     */
    public Main initialize(String [] argv, Rete r) {
        m_rete = r;

        // **********************************************************************
        // ###
        // Process any command-line switches
        int argIdx = 0;
        boolean doLogo = true;
        if (argv.length > 0) {
            while(argIdx < argv.length && argv[argIdx].startsWith("-")) {
                if (argv[argIdx].equals("-nologo"))
                    doLogo = false;
                else if (argv[argIdx].equals("-exit"))
                    m_exitOnError = true;
                argIdx++;
            }
        }

        // **********************************************************************
        // Print banner
        if (doLogo)
            showLogo();

        // **********************************************************************
        // ###
        // Open a file if requested
        m_fis = m_rete.getInputRouter("t");
        String name = argv.length <= argIdx ? null : argv[argIdx];

        try {
            if (name != null) {
                if (m_rete.getApplet() == null)
                    m_fis = new BufferedReader(new FileReader(name));

                else {
                    URL url
                        = new URL(m_rete.getApplet().getDocumentBase(),
                                  name);
                    m_fis = new BufferedReader(new InputStreamReader(url.openStream()));
                }
                m_readStdin = false;
            }

        } catch (IOException ioe) {
            m_rete.getErrStream().println("File not found or cannot open file:" +
                                          ioe.getMessage());
            m_rete.getErrStream().flush();
            System.exit(0);
        }

        return this ;
    }

    /**
     * Repeatedly parse and excute commands, from location determined
     * during initialize().
     * @param doPrompt True if a prompt should be printed, false otherwise.
     * Prompts will never be printed during a (batch) command.
     */

    public void execute(boolean doPrompt) {
        // **********************************************************************
        // ###
        // Process input from file or keyboard

        if (m_fis != null) {
            m_j = new Jesp(m_fis, m_rete);
            do {
                try {
                    // Argument is 'true' for prompting, false otherwise
                    m_j.parse(doPrompt && m_readStdin);

                } catch (JessException re) {
                    PrintWriter err = m_rete.getErrStream();
                    if (re.getCause() != null) {
                        err.write(re.toString());
                        err.write("\nNested exception is:\n");
                        err.println(re.getCause().getMessage());
                        re.getCause().printStackTrace(err);

                    } else
                        re.printStackTrace(err);

                    if (m_exitOnError) {
                        m_rete.getErrStream().flush();
                        m_rete.getOutStream().flush();
                        System.exit(-1);
                    }

                } catch (Exception e) {
                    m_rete.getErrStream().println("Unexpected exception:");
                    e.printStackTrace(m_rete.getErrStream());

                    if (m_exitOnError) {
                        m_rete.getErrStream().flush();
                        m_rete.getOutStream().flush();
                        System.exit(-1);
                    }

                } finally {
                    m_rete.getErrStream().flush();
                    m_rete.getOutStream().flush();
                }
            }
            // Loop if we're using the command line
            while (m_readStdin);
        }
        // If called again, read stdin, not batch file
        // ###
        m_readStdin = true;
        m_fis = m_rete.getInputRouter("t");
    }

}




