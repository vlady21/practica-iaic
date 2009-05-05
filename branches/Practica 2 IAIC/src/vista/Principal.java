/*
 * Principal.java
 */

package vista;

import main.ThreadSplash;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class Principal extends SingleFrameApplication {

    private static ThreadSplash hiloSplash;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        show(new PrincipalView(this));
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
