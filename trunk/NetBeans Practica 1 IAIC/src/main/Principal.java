/*
 * Principal.java
 */

package main;

import vista.*;
import modelo.micromundo.Estadisticas;
import modelo.micromundo.Micromundo;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class Principal extends SingleFrameApplication {
    private static ThreadSplash hiloInterfaz1;
    private PrincipalView vista;
    private Splash sp;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        try{

			Micromundo micromundo=new Micromundo();

            vista = new PrincipalView(this,micromundo);
            
            micromundo.setObserver(vista);
			Estadisticas.dameInstancia().setObserver(vista);

            show(vista);
            //vista.getFrame().setVisible(false);
            /*try {
                 Thread.sleep(1);
            }
            catch(InterruptedException e) {
            }*/
            //vista.getFrame().setVisible(true);


            /*ThreadSplash hiloInterfaz = new ThreadSplash();
            hiloInterfaz.run();

            ThreadSplashClose hiloInterfaza = new ThreadSplashClose();
            hiloInterfaza.run();

            
            //sp.setVisible(true);
            //sp.setAlwaysOnTop(true);

            //sp.setVisible(false);
            //vista.getFrame().setVisible(true);
            
            
            //vista.getFrame().setAlwaysOnTop(true);
*/

		}catch(Throwable e){e.printStackTrace();}
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

        hiloInterfaz1 = new ThreadSplash();
        hiloInterfaz1.run();

        launch(Principal.class, args);
    }

    /*public class ThreadSplash extends Thread {
        public ThreadSplash() {

        }
        public void run() {

            vista.getFrame().setVisible(false);
            vista.getFrame().setAlwaysOnTop(false);
            sp = new Splash();
            sp.setVisible(true);
            /*
            try {
                 ThreadSplash.sleep(3000);
            }
            catch(InterruptedException e) {
            }

            //vista.getFrame().setVisible(true);
            //vista.getFrame().setAlwaysOnTop(true);
            sp.setVisible(false);



            //sp.setVisible(false);*//*
        }
    }
*/
    public class ThreadSplashClose extends Thread {
        public ThreadSplashClose() {

        }
        public void run() {

            try {
                 Thread.sleep(3000);
            }
            catch(InterruptedException e) {
            }

            //vista.getFrame().setVisible(true);
            //vista.getFrame().setAlwaysOnTop(true);
            //sp.setVisible(false);

            //sp.setVisible(false);*/
        }
    }
}


