package modelo.matrices;

import java.util.ArrayList;

//matriz que muestra que planeta se conecta con otros
public class MatrizConexiones {

	private static MatrizConexiones _instancia=null;
	private static int[][] _conexiones=new int[216][216];
	
	public static MatrizConexiones getInstancia(){
		if(_instancia==null){
			_instancia=new MatrizConexiones();
		}
		return _instancia;
	}
	
	public void conecta(int planeta1, int planeta2){
		_conexiones[planeta1][planeta2]=1;
	}
	
	public boolean conectados(int planeta1, int planeta2){
		if(_conexiones[planeta1][planeta2]==1){
			return true;
		}
		return false;
	}
	
	public ArrayList<Integer> damePlanetasContiguos(int planeta){
		ArrayList<Integer> planetasContiguos=new ArrayList<Integer>();
		for(int i=0;i<216;i++){
			if(_conexiones[planeta][i]==1){
				planetasContiguos.add(i);
			}
		}
		return planetasContiguos;
	}
}
