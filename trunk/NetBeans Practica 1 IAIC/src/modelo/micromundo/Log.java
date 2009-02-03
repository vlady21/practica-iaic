package modelo.micromundo;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Log {
	private String _ruta="log.txt";
	private PrintWriter _writer;
	private static Log _instancia=null;
	
	public Log(){
		abrirLog();
	}
	
	public void abrirLog(){
		try {
			_writer = new PrintWriter(_ruta);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static Log dameInstancia(){
		if(_instancia==null){
			_instancia=new Log();
		}
		return _instancia;
	}
	
	public void agregar(String texto){
		_writer.append(texto+"\n");
	}
	
	public void cerrarLog(){
    	_writer.close();	
	}
}
