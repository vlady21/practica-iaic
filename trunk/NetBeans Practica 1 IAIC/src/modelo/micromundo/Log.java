package modelo.micromundo;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
/*
 * Clase que guarda el log de la aplicacion en el fichero log.txt
 * @author Jose Miguel Guerrero Hernandez 53466473Y
 */
public class Log {
	private String _ruta="log.txt";
	private PrintWriter _writer;
	private static Log _instancia=null;
	
	public Log(){
		abrirLog();
	}

    //Devuelve la instancia de la clase y si no esta creada la inicializa
	public static Log dameInstancia(){
		if(_instancia==null){
			_instancia=new Log();
		}
		return _instancia;
	}

    //Genera el fichero que va a contener el log y lo abre
	public void abrirLog(){
		try {
			_writer = new PrintWriter(_ruta);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	//Agrega el texto deseado al fichero
	public void agregar(String texto){
		_writer.append(texto+"\n");
	}

    //Cierra el fichero
	public void cerrarLog(){
    	_writer.close();	
	}
}
