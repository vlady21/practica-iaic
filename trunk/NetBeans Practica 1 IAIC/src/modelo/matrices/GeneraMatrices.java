package modelo.matrices;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import observador.Observador;

/*
 * Se encarga de rellenar las matrices con los datos leidos del fichero de configuracion
 * @author Jose Miguel Guerrero Hernandez 53466473Y
 */
public class GeneraMatrices {
	private static GeneraMatrices _instancia=null;
    private Observador _observer;

    //Devuelve la instancia de la clase, si no esta creada la inicializa
	public static GeneraMatrices dameInstancia(){
		if(_instancia==null){
			_instancia=new GeneraMatrices();
		}
		return _instancia;
	}

    //Constructor que lee las conexiones
	public GeneraMatrices(){
		leerConexiones();
	}

    //asignamos el observador por si hay algun error
    public void setObservador(Observador obs){
        _observer=obs;
    }

    //Lee la semilla para los valores "aleatorios" del fichero asignado para ello
	public int dameSemilla(){
		//leemos el fichero de la semilla
		Properties propiedades=new Properties();
		try{
			FileInputStream entrada=new FileInputStream("config/semilla.properties");
			propiedades.load(entrada);
			entrada.close();
		}
		catch (Exception e){
            JOptionPane.showMessageDialog(new JPanel(),
					"Error al abrir el fichero config/semilla.properties, \nes posible que no se encuentre en el sitio correspondiente.",
					"Error",JOptionPane.INFORMATION_MESSAGE);

        }
		//obtenemos el valor de la semilla
		String semilla=propiedades.getProperty("semilla");
		return Integer.parseInt(semilla);
	}

    /*
     * Se encarga de leer el fichero de configuracion, para ello divide
     * lo asignado al planeta dividiendolo a traves de las "," y luego
     * obtiene los datos de 2:2:2 donde obtenemos con que planeta se conecta,
     * que problema tiene asignado y el metodo de resolucion del problema.
     */
	public void leerConexiones(){
		for(int i=0;i<216;i++){
			//leemos el fichero de conexiones
			Properties propiedades=new Properties();
			try{
				FileInputStream entrada=new FileInputStream("config/configuracion.properties");
				propiedades.load(entrada);
				entrada.close();
			}
			catch (Exception e){
                JOptionPane.showMessageDialog(new JPanel(),
					"Error al abrir el fichero config/configuracion.properties, \nes posible que no se encuentre en el sitio correspondiente.",
					"Error",JOptionPane.INFORMATION_MESSAGE);
            }
			//obtenemos la conexion para el planeta que miramos
			String planetas=propiedades.getProperty(""+i);
			StringTokenizer subCadsCampos=new StringTokenizer(planetas,",");
			//obtenemos la lista de los planetas que se conectan con el que estamos mirando
			ArrayList<String> listaPlanetas=new ArrayList<String>();
			while(subCadsCampos.hasMoreTokens()){
				listaPlanetas.add(subCadsCampos.nextToken());
			}
			//conectamos el planeta con sus vecinos
			for(int j=0;j<listaPlanetas.size();j++){
                //obtenemos las partes de cada conexion
				int dosPuntos1=listaPlanetas.get(j).indexOf(":");
				int dosPuntos2=listaPlanetas.get(j).lastIndexOf(":");
                //obtenemos el planeta con el que se conecta
				String planeta=listaPlanetas.get(j).substring(0,dosPuntos1);
                //obtenemos el problema que tiene asignada dicha conexion
				String problema=listaPlanetas.get(j).substring(dosPuntos1+1,dosPuntos2);
                //obtenemos la forma de resolver el problema asignado
				String solucion=listaPlanetas.get(j).substring(dosPuntos2+1);
                //insertamos los valores en la matriz correspondiente
				MatrizConexiones.getInstancia().conecta(i, Integer.parseInt(planeta));
				MatrizProblemas.getInstancia().conecta(i, Integer.parseInt(planeta), Integer.parseInt(problema));
				MatrizSolucionProblema.getInstancia().conecta(i, Integer.parseInt(planeta), Integer.parseInt(solucion));
			}
		}
	}
}
