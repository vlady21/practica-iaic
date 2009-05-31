/*
 * Clase encargada de comprobar los archivos del sistema y de cargar los
 * modulos de la aplicacion. Si no existen los archivos del sistema se los
 * descarga automaticamente.
 */

package main;


import vista.Splash;
import conocimiento.LanzadorJess;
import conocimiento.Reglas_1;
import controlador.ControladorGUI;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jess.JessException;
import modelo.ModeloFormularios;
import vista.*;
import org.jdesktop.application.SingleFrameApplication;
import utilerias.Constantes;
import utilerias.Propiedades;

/**
 * AUTORES:
 * @author Victor Adaíl Ferrer
 * @author José Miguel Guerrero Hernández
 */
public class ThreadSplash extends Thread {
        public ThreadSplash() {

        }
        public void run() {

            Splash sp = new Splash();
            sp.setVisible(true);

            if(comprobarFicheros(sp)){
                for(int i = 0;i<12;i++){

                    try {
                         ThreadSplash.sleep(250);
                    }
                    catch(InterruptedException e) {
                    }

                    sp.avanza();
                }
            }

            ModeloFormularios modelo = new ModeloFormularios();

            ControladorGUI controlador = new ControladorGUI();
            controlador.setModelo(modelo);

            Principal.lanzar(modelo, controlador);

            sp.setVisible(false);


        }

        /**
         * Metodo encargado de comprobar los archivos del sistema Si no existen
         * los archivos del sistema se los descarga automaticamente.
         * @param sp: Splash dinamico de la aplicacion
         * @return boolean: booleano que indica si se ha descargado los archivos
         * o no
         */
        private static boolean comprobarFicheros(Splash sp) {
        Properties configuracion;

          try {
              FileInputStream fichero = new FileInputStream(Constantes.CONFIGURACION);
              fichero.close();

              return true;
          } catch (Exception e) {
          try{
                File f;
                String code="http://practica-iaic.googlecode.com/svn/trunk/Aplicacion/";
                URL url;

                f = new File("config");
                f.mkdir();
                f = new File("reglas");
                f.mkdir();

                sp.avanza();

                url = new URL(code + Constantes.CONFIGURACION);
                copia(url.openStream(),Constantes.CONFIGURACION);

                sp.avanza();

                url = new URL(code + Constantes.FORMULARIO_AFECTIVO);
                copia(url.openStream(),Constantes.FORMULARIO_AFECTIVO);

                sp.avanza();

                url = new URL(code + Constantes.FORMULARIO_JURIDICO);
                copia(url.openStream(),Constantes.FORMULARIO_JURIDICO);

                sp.avanza();

                url = new URL(code + Constantes.FORMULARIO_TECNICO);
                copia(url.openStream(),Constantes.FORMULARIO_TECNICO);

                sp.avanza();

                url = new URL(code + Constantes.FORMULARIO_TECNICO);
                copia(url.openStream(),Constantes.FORMULARIO_TECNICO);

                sp.avanza();

                configuracion = Propiedades.getPropiedades(Constantes.CONFIGURACION);

                sp.avanza();

                url = new URL(code + configuracion.getProperty("REGLAS_TECNICO"));
                copia(url.openStream(),configuracion.getProperty("REGLAS_TECNICO"));

                sp.avanza();

                url = new URL(code + configuracion.getProperty("REGLAS_JURIDICO"));

                sp.avanza();

                copia(url.openStream(),configuracion.getProperty("REGLAS_JURIDICO"));

                sp.avanza();

                url = new URL(code + configuracion.getProperty("REGLAS_AFECTIVO"));

                sp.avanza();

                copia(url.openStream(),configuracion.getProperty("REGLAS_AFECTIVO"));

                sp.avanza();

          }catch(Exception ex){}

          return false;
      }
    }


    /**
     * Metodo encargado de copiar al disco los archivos descargados
     * @param in: inputStream del archivo a descargar
     * @param destino: nombre del path del fichero destino
     */
    public static void copia (InputStream in, String destino) throws IOException {

        OutputStream out= new FileOutputStream(destino);
        byte[] buffer= new byte[256];
        while (true) {
          int n= in.read(buffer);
          if (n < 0)
            break;
          out.write(buffer, 0, n);
        }
        in.close();
        out.close();
  }
    }