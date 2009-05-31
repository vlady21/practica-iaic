/*
 * AsesorLaboral.java
 */

package main;

import controlador.ControladorGUI;
import modelo.ModeloFormularios;
import vista.*;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
/**
 * AUTORES:
 * @author Victor Adaíl Ferrer
 * @author José Miguel Guerrero Hernández
 */
public class AsesorLaboral extends SingleFrameApplication {
    
    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {

        ModeloFormularios modelo = new ModeloFormularios();

        ControladorGUI controlador = new ControladorGUI();
        controlador.setModelo(modelo);

        Principal.lanzar(modelo, controlador);
    }

    @Override
    protected void startup() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}


