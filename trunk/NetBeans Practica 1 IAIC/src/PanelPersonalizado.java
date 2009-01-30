// Una clase JPanel personalizada.
  import java.awt.*;
  import javax.swing.*;

  public class PanelPersonalizado extends JPanel {
     public final static int CIRCULO = 1, CUADRADO = 2;
     private int figura;

     // usar figura para dibujar un óvalo o rectángulo
      public void paintComponent( Graphics g )
      {
          
         super.paintComponent( g );

         if ( figura == CIRCULO )
            g.fillOval( 50, 10, 60, 60 );
         else if ( figura == CUADRADO )
            g.fillRect( 50, 10, 60, 60 );

      }

      // establecer valor de figura y repintar PanelPersonalizado
      public void dibujar( int figuraADibujar )
      {
         figura = figuraADibujar;
         repaint();
      }

   } // fin de la clase PanelPersonalizado
