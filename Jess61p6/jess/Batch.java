package jess;

import java.io.*;
import java.net.URL;

/** **********************************************************************
 * The "batch" command.
 * <P>
 * (C) 2003 E.J. Friedman-Hill and the Sandia Corporation<BR>
 * $Id: Batch.java,v 1.3.2.1 2003/09/02 14:33:19 ejfried Exp $
 ********************************************************************** */

class Batch implements Userfunction, Serializable {
    public String getName() {
        return "batch";
    }

    public Value batch(String filename, Rete engine)
        throws JessException {

        Value v = Funcall.FALSE;
        Reader fis = null;
        // ###
        try {
            try {
                if (engine.getApplet() == null)
                    fis = new FileReader(filename);
                else {
                    URL url = new URL(engine.getApplet().getDocumentBase(),
                                      filename);
                    fis = new InputStreamReader(url.openStream());
                }
            } catch (Exception e) {
                // Try to find a resource file, too.
                URL u = engine.getResource(filename);
                if (u == null)
                    throw new JessException("batch", "Cannot open file", e);

                InputStream is = u.openStream();
                fis = new InputStreamReader(is);
            }
            Jesp j = new Jesp(fis, engine);
            v = j.parse(false);

        } catch (IOException ex) {
            throw new JessException("batch", "I/O Exception", ex);

        } finally {
            if (fis != null) try { fis.close(); } catch (IOException ioe) {}
        }
        return v;
    }

    public Value call(ValueVector vv, Context context) throws JessException {
        String filename = vv.get(1).stringValue(context);

        return batch(filename, context.getEngine());
    }
}


