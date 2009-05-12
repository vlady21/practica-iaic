/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilerias;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jose Miguel
 */
public class LectorConsejos {
    private FileReader fr = null;
    private BufferedReader bf = null;
    private String _fichero="";
    private String _texto="";

    public void leerConsejos(String fichero){
        try {
            _fichero = fichero;
            fr = new FileReader(_fichero);
            bf = new BufferedReader(fr);
            String sCadena="";
            while ((sCadena = bf.readLine()) != null) {
                _texto += sCadena;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LectorConsejos.class.getName()).log(Level.SEVERE, null, ex);
        }catch (IOException ex) {
                Logger.getLogger(LectorConsejos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String dameConsejos(){
        return _texto;
    }
}