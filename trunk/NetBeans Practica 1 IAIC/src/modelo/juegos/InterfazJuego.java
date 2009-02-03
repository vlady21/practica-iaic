package modelo.juegos;

import aima.search.framework.Problem;
import aima.search.framework.StepCostFunction;

public abstract class InterfazJuego {
	protected static int _nodosExpandidos=0;
    protected static String _enunciadoProblema="";
	protected static boolean _resuelto=false;
	public abstract boolean resuelto();
	public abstract Problem getProblema();
	public abstract int dificultad();

    public String dameEnunciadoProblema(){
        return _enunciadoProblema;
    }

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
