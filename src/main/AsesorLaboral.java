/*
 * Clase principal de la aplicacion
 */

package main;

import controlador.ControladorGUI;
import modelo.ModeloFormularios;
import vista.*;
import org.jdesktop.application.SingleFrameApplication;


/**
 * AUTORES:
 * @author Victor Adaíl Ferrer
 * @author José Miguel Guerrero Hernández
 */
public class AsesorLaboral extends SingleFrameApplication {
    private static ThreadSplash hiloSplash;
    
    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {

        hiloSplash = new ThreadSplash();
        hiloSplash.run();

    }

    @Override
    protected void startup() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}


