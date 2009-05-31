/**
 * Clase utilizada para leer de un fichero.
 */

package utilerias;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * AUTORES:
 * @author Victor Adaíl Ferrer
 * @author José Miguel Guerrero Hernández
 */
public class LectorConsejos {
    private FileReader fr = null;
    private BufferedReader bf = null;
    private String _fichero="";
    private static String _texto="";

    /**
     * Metodo que lee el texto de un determinado fichero
     *
     * @param fichero String correspondiente al fichero a leer.
     */
    public void leerConsejos(String fichero){
        try {
            _texto="";
            _fichero = fichero;
            fr = new FileReader(_fichero);
            bf = new BufferedReader(fr);
            String sCadena="";
            while ((sCadena = bf.readLine()) != null) {
                _texto += sCadena;
            }
            bf.close();
            fr.close();
        } catch (Exception ex) {
            
        }
    }

    /**
     * Metodo que elimina el fichero de consejos para limpiar los consejos anteriores.
     * @param rutaficheroconsejos String correspondiente al fichero eliminar.
     */
    public void limpiarConsejos(String rutaficheroconsejos){
        File fichero = new File(rutaficheroconsejos);
        fichero.delete();
    }

    /**
     * Metodo que devuelve los consejos leidos.
     * 
     * @return String correspondiente a los consejos.
     */
    public static String dameConsejos(){
        return _texto;
    }
}
