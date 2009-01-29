package modelo.matrices;

public class MatrizSolucionProblema {

	private static MatrizSolucionProblema _instancia=null;
	private static int[][] _solucion=new int[216][216];
	
	public static MatrizSolucionProblema getInstancia(){
		if(_instancia==null){
			_instancia=new MatrizSolucionProblema();
		}
		return _instancia;
	}
	
	public void conecta(int planeta1, int planeta2,int valor){
		_solucion[planeta1][planeta2]=valor;
	}
	
	public int algoritmo(int planeta1, int planeta2){
		return _solucion[planeta1][planeta2];
	}
}
