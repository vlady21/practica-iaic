/*
 * AsesorLaboral.java
 */

package main;

import conocimiento.LanzadorJess;
import conocimiento.Reglas_1;
import java.util.ArrayList;
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

        /* PRUEBA LANZAMIENTO JESS OTRAS REGLAS DISTINTAS
         *
         try {
            Reglas_1 reg = new Reglas_1(rellenoPrueba(), "log_grupoB09.txt", "reglasB09.clp");
        } catch (JessException ex) {
            Logger.getLogger(AsesorLaboral.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    @Override
    protected void startup() {
        show(new PrincipalView(this));
    }
/*
    public static ArrayList<String> rellenoPrueba(){
        ArrayList<String> _listaSlot=new ArrayList<String>();
        _listaSlot.add("universitarios");
        _listaSlot.add("2");
        _listaSlot.add("4");
        _listaSlot.add("21");
        _listaSlot.add("masculino");
        _listaSlot.add("si");
        _listaSlot.add("ingles");
        _listaSlot.add("frances");
        _listaSlot.add("informatica");
        _listaSlot.add("si");
        _listaSlot.add("si");
        _listaSlot.add("1000");
        _listaSlot.add("12");
        _listaSlot.add("programador");
        _listaSlot.add("13");
        _listaSlot.add("1");
        _listaSlot.add("si");
        _listaSlot.add("2");
        _listaSlot.add("rechazado");
        return _listaSlot;
    }
 */
}


