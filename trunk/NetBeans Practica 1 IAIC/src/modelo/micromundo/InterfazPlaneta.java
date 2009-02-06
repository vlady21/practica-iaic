package modelo.micromundo;

import java.util.ArrayList;

import aima.search.framework.Problem;
import observador.Observador;

/*
 * Clase de la que debe heredar el planeta para asi tener todos los
 * planetas las mismas variables con las que poder interactuar y que
 * van a ser las mismas y que deben compartir todos los planetas.
 * @author Jose Miguel Guerrero Hernandez 53466473Y
 */
public abstract class InterfazPlaneta {
    //Variables
	protected static ArrayList<Planeta> _listaPlanetas;
	protected static ArrayList<Integer> _listaPlanetasExpandidos;
    //Interfaz a la que pintar las conexiones y la nave
    protected static Observador _observer;

    //Cantidad de nodos expandidos durante toda la ejecucion
	protected static int _nodosExpandidos=0;
    //Indica si se ha resuelto el problema, si se ha llegado a un planeta objetivo
	protected static boolean _resuelto=false;
    //Indica si se debe hacer paso a paso y parar en cada creacion de un hijo
	protected static boolean _pasoApaso=false;
    //Si se ejecuta paso a paso, esto da lugar a la siguiente transicion
	protected static boolean _siguiente=false;

    //Metodos
	public abstract void pasoApaso();
	public abstract void siguiente();
	public abstract void continuo();
	public abstract boolean resuelto();
	public abstract Problem getProblema(ArrayList<Planeta> planetas);
    public abstract int numeroPlaneta();

    //Asigna la interfaz de la aplicacion
    public void setObserver(Observador observer){
        _observer=observer;
    }

    //Indica a la vista donde debe poner la nave
    public void posicionarNave(int numeroPlaneta){
        _observer.posicionarNave(numeroPlaneta);
    }

    /*
     * Indica a la vista que pinte la conexion entre dos planeta segun el tipo:
     * 0-verde(podemos viajar), 1-rojo(no podemos viajar), 2-azul(solucion)
     */
    public static void conectaPlanetas(int planeta1, int planeta2, int tipo){
        _observer.conectaPlanetas(planeta1, planeta2, tipo);
    }
}
