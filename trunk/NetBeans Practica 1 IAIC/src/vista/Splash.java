/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vista;

import java.awt.*;
import java.awt.event.*;

public class Splash extends Frame implements ActionListener {
    static void renderSplashFrame(Graphics2D g, int frame) {
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
    public Splash() {
        super("SplashScreen demo");
        setSize(500, 300);
        setLayout(new BorderLayout());
        Menu m1 = new Menu("File");
        MenuItem mi1 = new MenuItem("Exit");
        m1.add(mi1);
        mi1.addActionListener(this);

        MenuBar mb = new MenuBar();
        setMenuBar(mb);
        mb.add(m1);
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
        setVisible(true);
        toFront();
    }
    public void actionPerformed(ActionEvent ae) {
        System.exit(0);
    }
    
}
