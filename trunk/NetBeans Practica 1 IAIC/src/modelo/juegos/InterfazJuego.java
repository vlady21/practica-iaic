package modelo.juegos;

import aima.search.framework.Problem;

public abstract class InterfazJuego {
	protected static int _nodosExpandidos=0;
	protected static boolean _resuelto=false;
	public abstract boolean resuelto();
	public abstract Problem getProblema();
	public abstract int dificultad();
}
