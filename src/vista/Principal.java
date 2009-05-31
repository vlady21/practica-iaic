/*
 * Principal.java
 */

package vista;

import controlador.ControladorGUI;
import main.ThreadSplash;
import modelo.ModeloFormularios;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * AUTORES:
 * @author Victor Adaíl Ferrer
 * @author José Miguel Guerrero Hernández
 */
public class Principal extends SingleFrameApplication {

    private static ThreadSplash hiloSplash;
    private static ModeloFormularios modelo;
    private static ControladorGUI controlador;

    public static void lanzar(ModeloFormularios modelo, ControladorGUI controlador) {
        hiloSplash = new ThreadSplash();
        hiloSplash.run();

        Principal.modelo = modelo;
        Principal.controlador = controlador;

        launch(Principal.class,null);
    }

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        show(new PrincipalView(this, modelo, controlador));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of Principal
     */
    public static Principal getApplication() {
        return Application.getInstance(Principal.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {

        hiloSplash = new ThreadSplash();
        hiloSplash.run();

        launch(Principal.class, args);
    }

    public static void lanzar(String[] args) {

        hiloSplash = new ThreadSplash();
        hiloSplash.run();

        launch(Principal.class, args);
    }

    public class ThreadSplashClose extends Thread {
        public ThreadSplashClose() {

        }
        public void run() {

            try {
                 Thread.sleep(3000);
            }
            catch(InterruptedException e) {
            }
        }
    }
}
