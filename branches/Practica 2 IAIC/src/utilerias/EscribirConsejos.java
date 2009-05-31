/**
 * Clase utilizada para escribir en un fichero.
 */

package utilerias;

import java.io.PrintWriter;

/**
 * AUTORES:
 * @author Victor Adaíl Ferrer
 * @author José Miguel Guerrero Hernández
 */
public class EscribirConsejos {
    private static PrintWriter _ficheroSalida=null;
    private static String _ruta="";

    /**
     * Metodo que asigna la ruta del fichero donde vamos a escribir.
     *
     * @param ruta String correspondiente a la ruta del fichero.
     */
    public static void asignaRuta(String ruta){
        _ruta=ruta;
    }

    /**
     * Metodo que escribe en el fichero el dato deseado
     *
     * @param dato String correspondiente al texto a escribir en el fichero.
     */
    public static void escribe(String dato){
        try {
			_ficheroSalida = new PrintWriter (_ruta);
            _ficheroSalida.println(dato);
            _ficheroSalida.close();
		} catch (Exception e) {}
    }
}
