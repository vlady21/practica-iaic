package vista;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

@SuppressWarnings({"serial","deprecation"})
public class VentanaComparativaGraficas extends JFrame{
	private BufferedImage _grafica=null;
	private ArrayList<Integer> _costeReal=null;
	private ArrayList<Integer> _costeHeuristico=null;
	
	public VentanaComparativaGraficas(ArrayList<Integer> costeReal, ArrayList<Integer> costeHeuristico){
		_costeReal=costeReal;
		_costeHeuristico=costeHeuristico;
		initComponents();
	}
	
	public void initComponents(){
        setIconImage(new ImageIcon("imagenes/icono.png").getImage());
		setSize(710,610);
		setTitle("Grafica comparativa");
		setResizable(false);
		show();
        repaint();
        setAlwaysOnTop(true);
	}
	
	public BufferedImage creaGrafica(){
		
		XYSeries real = new XYSeries("Coste real");
		for (int i=0;i<_costeReal.size();i++){
			real.add(i, _costeReal.get(i));
		}
		
		
		XYSeries heuristico = new XYSeries("Coste heuristico");
		for (int i=0;i<_costeHeuristico.size();i++){
			heuristico.add(i, _costeHeuristico.get(i));
		}
		
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(real);
		dataset.addSeries(heuristico);
		
		JFreeChart chart = ChartFactory.createXYLineChart(
				"Comparativa", // nombre grafica  
		        "Planetas", // nombre horizontal
		        "Coste", // nombre vertical
		        dataset,PlotOrientation.VERTICAL,
		        true, // mostrar leyenda
		        true, 
		        false 
		        );

		BufferedImage image = chart.createBufferedImage(700,600);   
	    return image;
	}
	
	public void paint(Graphics g){
		   super.paint(g);		   
		   if(_grafica == null){
			   _grafica = creaGrafica();
		   }
		   g.drawImage(_grafica,0,0,null);
	}
}