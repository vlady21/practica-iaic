/*
 * AsesorLaboral.java
 */

package main;

import conocimiento.LanzadorJess;
import java.util.logging.Level;
import java.util.logging.Logger;
import jess.JessException;
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

        /* PRUEBA LANZAMIENTO JESS
         * 
         try {
            LanzadorJess l = new LanzadorJess("log_grupoB09.txt", "reglasB09.clp");
            l.arrancarJess();
            l.insertaSlotValue("estado_actual", "busqueda_empleo");
            l.insertaSlotValue("rango_edad", "joven");
            l.insertaSlotValue("rechazado", "si");
            l.insertaSlotValue("situacion_laboral", "trabajando_jornadacompleta");
            l.insertaSlotValue("estudio", "no");
            l.ejecutarJess();
        } catch (JessException ex) {
            Logger.getLogger(AsesorLaboral.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    @Override
    protected void startup() {
        show(new PrincipalView(this));
    }
}


