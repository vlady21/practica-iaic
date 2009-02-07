package observador;

import java.util.ArrayList;

/*
 * Interfaz que debe implementar la vistas para poner en contacto
 * el modelo con la vista y asi reflejar los datos
 *
 * @author Jose Miguel Guerrero Hernandez 53466473Y
 */
public interface Observador {
    //posiciona la nave en un planeta
	public void posicionarNave(int numeroPlaneta);
	//conexion 0 incorrecta-rojo, 1 correcta-verde, 2 final-azul
	public void conectaPlanetas(int planeta1, int planeta2, int tipo);
    //escribe en el log de la interfaz
	public void escribeLog(String datos);
    //enviamos las listas para pintar las estadisticas
	public void estadisticas(ArrayList<Integer> real, ArrayList<Integer> heuristica);
	//reiniciamos la interfaz - eliminamos conexiones y posicionamos la nave en el planeta 1
    public void reiniciar();
    //limpia las conexiones
    public void limpiarRecorrido();
    //establece una informacion en la barra de estado de la interfaz
    public void informacionStatus(String info);
}
