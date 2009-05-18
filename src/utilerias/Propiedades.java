/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilerias;

import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author Victor
 */
public class Propiedades {

    /**
     * Recupera el valor del fichero properties.
     * @param String Fichero properties al que se desea acceder
     * @return Properties
     */
    public static Properties getPropiedades(String ficheroProperties)
    {
      Properties propiedades = new Properties();

      try {
          FileInputStream fichero = new FileInputStream(ficheroProperties);
          propiedades.load(fichero);
          fichero.close();
      } catch (Exception e) {
          System.out.println("Error : " + e.toString());
      }
      return propiedades;
    } // Fin getPropiedades()
}
