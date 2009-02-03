/*
 * Principal.java
 */

package vista;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

            SplashTest splash = new SplashTest();
            splash.iniciar();

            PrincipalView vista = new PrincipalView(this,micromundo);
            show(vista);

            vista.getFrame().setAlwaysOnTop(true);
			micromundo.setObserver(vista);
			Estadisticas.dameInstancia().setObserver(vista);

            vista.getFrame().setAlwaysOnTop(false);


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

    public class SplashTest {
        void renderSplashFrame(Graphics2D g, int frame) {
            final String[] comps = {"Modulo de Inteligencia Artificial", "Micromundo del Sistema Planetario", "Algoritmos de busqueda", "Interfaz Grafica"};
            g.setComposite(AlphaComposite.Clear);
            // Se obtienen las dimensiones en pixels de la pantalla.
            Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();

            int x=(pantalla.width) / 2;
            int y=(pantalla.height) / 2;

            if(pantalla.width<1024){
                x=pantalla.width-200;
                y=pantalla.height-200;
            }

            g.fillRect(x-10,y-20,280,40);
            g.setPaintMode();
            g.setColor(Color.BLACK);

            g.drawString("Cargando "+comps[(frame/5)%4]+"...", x, y);
            g.fillRect(x,y+20,(frame*18)%280,20);
        }
        public SplashTest() {
            
            final SplashScreen splash = SplashScreen.getSplashScreen();
            if (splash == null) {
                System.out.println("SplashScreen.getSplashScreen() returned null");
                return;
            }
            Graphics2D g = (Graphics2D)splash.createGraphics();
            if (g == null) {
                System.out.println("g is null");
                return;
            }
            for(int i=0; i<20; i++) {
                renderSplashFrame(g, i);
                splash.update();
                try {
                    Thread.sleep(200);
                }
                catch(InterruptedException e) {
                }
            }
            splash.close();
            
        }
        public void actionPerformed(ActionEvent ae) {
            System.exit(0);
        }

        public void iniciar() {
            SplashTest test = new SplashTest();
        }
    }
}


