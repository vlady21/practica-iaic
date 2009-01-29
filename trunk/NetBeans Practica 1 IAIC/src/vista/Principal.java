/*
 * Principal.java
 */

package vista;

import modelo.micromundo.Estadisticas;
import modelo.micromundo.Micromundo;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class Principal extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        try{

			Micromundo micromundo=new Micromundo();
            PrincipalView vista = new PrincipalView(this,micromundo);
			show(vista);
			micromundo.setObserver(vista);
			Estadisticas.dameInstancia().setObserver(vista);

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
        launch(Principal.class, args);
    }
}
