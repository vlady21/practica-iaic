package modelo.matrices;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;


public class GeneraMatrices {

	private static GeneraMatrices _instancia=null;
	
	public static GeneraMatrices dameInstancia(){
		if(_instancia==null){
			_instancia=new GeneraMatrices();
		}
		return _instancia;
	}
	
	public GeneraMatrices(){
		leerConexiones();
	}
	
	public int dameSemilla(){
		//leemos el fichero de conexiones
		Properties propiedades=new Properties();
		try{
			FileInputStream entrada=new FileInputStream("config/semilla.properties");
			propiedades.load(entrada);
			entrada.close();
		}
		catch (Exception e){}
		//obtenemos la conexion para el planeta que miramos
		String semilla=propiedades.getProperty("semilla");
		return Integer.parseInt(semilla);
	}
	
	public void leerConexiones(){
		for(int i=0;i<216;i++){
			//leemos el fichero de conexiones
			Properties propiedades=new Properties();
			try{
				FileInputStream entrada=new FileInputStream("config/configuracion.properties");
				propiedades.load(entrada);
				entrada.close();
			}
			catch (Exception e){}
			//obtenemos la conexion para el planeta que miramos
			String planetas=propiedades.getProperty(""+i);
			StringTokenizer subCadsCampos=new StringTokenizer(planetas,",");
			//obtenemos la lista de los planetas
			ArrayList<String> listaPlanetas=new ArrayList<String>();
			while(subCadsCampos.hasMoreTokens()){
				listaPlanetas.add(subCadsCampos.nextToken());
			}
			//conectamos el planeta con sus vecinos
			for(int j=0;j<listaPlanetas.size();j++){
				int dosPuntos1=listaPlanetas.get(j).indexOf(":");
				int dosPuntos2=listaPlanetas.get(j).lastIndexOf(":");
				String planeta=listaPlanetas.get(j).substring(0,dosPuntos1);
				//System.out.println("PLANETA:"+i);
				String problema=listaPlanetas.get(j).substring(dosPuntos1+1,dosPuntos2);
				//System.out.println(problema);
				String solucion=listaPlanetas.get(j).substring(dosPuntos2+1);
				//System.out.println(solucion);
				MatrizConexiones.getInstancia().conecta(i, Integer.parseInt(planeta));
				MatrizProblemas.getInstancia().conecta(i, Integer.parseInt(planeta), Integer.parseInt(problema));
				MatrizSolucionProblema.getInstancia().conecta(i, Integer.parseInt(planeta), Integer.parseInt(solucion));
			}
		}
	}
}
