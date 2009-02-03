package modelo.matrices;

public class MatrizProblemas {
	
	private static MatrizProblemas _instancia=null;
	private static int[][] _distancias=new int[216][216];
	
	public static MatrizProblemas getInstancia(){
		if(_instancia==null){
			_instancia=new MatrizProblemas();
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
