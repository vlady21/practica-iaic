/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilerias;

import java.io.PrintWriter;

/**
 *
 * @author Jose Miguel
 */
public class EscribirConsejos {
    private static PrintWriter _ficheroSalida=null;
    private static String _ruta="";


    public static void asignaRuta(String ruta){
        _ruta=ruta;
    }

    public static void escribe(String dato){
        try {
			_ficheroSalida = new PrintWriter (_ruta);
            _ficheroSalida.println(dato);
            _ficheroSalida.close();
		} catch (Exception e) {}
    }
}
