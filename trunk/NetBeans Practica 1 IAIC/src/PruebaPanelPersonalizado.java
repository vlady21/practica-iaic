/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Victor
 */
// Uso de un objeto JPanel personalizado.
  import java.awt.*;
  import java.awt.event.*;
  import javax.swing.*;
  public class PruebaPanelPersonalizado extends JFrame
  {
	private JPanel panelBotones;
	private universo miPanel;
	private JButton botonCirculo, botonCuadrado;

	// configurar GUI
	public PruebaPanelPersonalizado()
	{
		super( "Prueba de universo" );
		// crear área personalizada de dibujo
		miPanel = new universo();
		miPanel.setBackground( Color.GREEN );

		// establecer botonCuadrado
		botonCuadrado = new JButton( "Cuadrado" );
		botonCuadrado.addActionListener(
			new ActionListener()
			{
				 // clase interna anónima
				 // dibujar un cuadrado
				 public void actionPerformed( ActionEvent evento )
				 {
				 	miPanel.dibujar( universo.CUADRADO );
				 }
			} // fin de la clase interna anónima
		); // fin de la llamada a addActionListener

		botonCirculo = new JButton( "Círculo" );
		botonCirculo.addActionListener(
			new ActionListener()
			{
				// clase interna anónima
				// dibujar un círculo
				public void actionPerformed( ActionEvent evento )
				{
					//miPanel.dibujar( universo.CIRCULO );
                    miPanel.paintComponent(miPanel.getGraphics());
				}
			} // fin de la clase interna anónima
		); // fin de la llamada a addActionListener
		// establecer panel con botones
		panelBotones = new JPanel();
		panelBotones.setLayout( new GridLayout( 1, 2 ) );
		panelBotones.add( botonCirculo );
		panelBotones.add( botonCuadrado );

		// adjuntar panel de botones y área personalizada de dibujo al panel de contenido
		Container contenedor = getContentPane();
		contenedor.add( miPanel, BorderLayout.CENTER );
		contenedor.add( panelBotones, BorderLayout.SOUTH );
		setSize(300,150);
		setVisible(true);
	} // fin del constructor Pruebauniverso
	public static void main( String args[] )
	{
		PruebaPanelPersonalizado aplicacion = new PruebaPanelPersonalizado();
		aplicacion.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	}
} // fin de la clase Pruebauniverso