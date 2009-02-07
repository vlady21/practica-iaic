package modelo.juegos;

import aima.search.framework.Problem;
import aima.search.framework.StepCostFunction;

/*
 * Todo juego debe extender de esta clase e inicializar las
 * variables en su constructor
 *
 * @author Jose Miguel Guerrero Hernandez 53466473Y
 */
public abstract class InterfazJuego {
    //Variables
	protected static int _nodosExpandidos=0;
    protected static String _enunciadoProblema="";
	protected static boolean _resuelto=false;

    //Metodos
	public abstract int dificultad();

	public boolean resuelto(){
		return _resuelto;
	}
    public String dameEnunciadoProblema(){
        return _enunciadoProblema;
    }

    //Instancia del problema para que AIMA lo utilice
	public abstract Problem getProblema();

    /*
     * Obtenemos el coste de la accion sucedida entre la transicion, toda transicion
     * va a tener la palabra COSTE: por lo que podemos extraer de ahi el valor real.
     * En cada accion de generar un sucesor se debe incorporar al final de la accion,
     * COSTE:X - donde x es el valor del coste.
     */
    public class ValorReal implements StepCostFunction {
        public Double calculateStepCost(Object fromState, Object toState, String action) {
            String coste=action.substring(action.lastIndexOf("COSTE:")+6);
			int cont=0;
			String numero="";
			while(cont<coste.length() && Character.isDigit(coste.charAt(cont))){
				numero+=coste.charAt(cont);
				cont++;
			}
            return Double.parseDouble(""+numero);
        }
    }
}
