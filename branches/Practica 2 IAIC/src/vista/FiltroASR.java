/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vista;

import java.io.*;
import javax.swing.filechooser.FileFilter;


/**
 *
 * @author Victor
 */
class FiltroASR  extends FileFilter {

    /**
     * Metodo que devuelve true si el archivo tiene
     * la extension .asr y por lo tanto es valido para nosotros.
     *
     * @param fichero Fichero que se utiliza para hacer el filtro.
     * @return Boolean True si es un archivo de tipo .rvs
     */
     public boolean accept(File fichero) {
            return fichero.getName().toLowerCase().endsWith(".asr")   || fichero.isDirectory();

     }

    /**
     * Metodo que devuelve la cadena que servira de
     * informacion a la hora de buscar los archivos.
     *
     * @return String Cadena con la descripci�n del filtro.
     */
     public String getDescription() {
           return "Fichero de asesor laboral";
     }
}
