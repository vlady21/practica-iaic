package observador;

import java.util.ArrayList;

public interface Observador {

	public void posicionarNave(int numeroPlaneta);
	//conexion 0 incorrecta-rojo, 1 correcta-verde, 2 final-morado 
	public void conectaPlanetas(int planeta1, int planeta2, int tipo);
	public void escribeLog(String datos);
	public void estadisticas(ArrayList<Integer> real, ArrayList<Integer> heuristica);
	public void reiniciar();
}
