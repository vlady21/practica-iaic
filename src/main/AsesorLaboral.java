/*
 * AsesorLaboral.java
 */

package main;

import vista.*;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class AsesorLaboral extends SingleFrameApplication {
    
    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {

        Principal.lanzar(args);
    }

    @Override
    protected void startup() {
        show(new PrincipalView(this));
    }
}


