package modelo.matrices;

public class MatrizCosteDistancias {
	
	private static MatrizCosteDistancias _instancia=null;
	private static int[][] _distancias=new int[216][216];
	
	public static MatrizCosteDistancias getInstancia(){
		if(_instancia==null){
			_instancia=new MatrizCosteDistancias();
		}
		return _instancia;
	}
	
	public void conecta(int planeta1, int planeta2,int valor){
		_distancias[planeta1][planeta2]=valor;
	}
	
	public int ping(int planeta1, int planeta2){
		return _distancias[planeta1][planeta2];
	}
}
