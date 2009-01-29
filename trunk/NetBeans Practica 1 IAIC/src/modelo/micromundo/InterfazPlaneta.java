package modelo.micromundo;

import java.util.ArrayList;

import aima.search.framework.Problem;

public abstract class InterfazPlaneta {
	protected static ArrayList<Planeta> _listaPlanetas;
	protected static ArrayList<Integer> _listaPlanetasExpandidos;
	protected static int _nodosExpandidos=0;
	protected static boolean _resuelto=false;
	protected static boolean _pasoApaso=false;
	protected static boolean _siguiente=false;
	
	public abstract void pasoApaso();
	public abstract void siguiente();
	public abstract void continuo();
	public abstract boolean resuelto();
	public abstract Problem getProblema(ArrayList<Planeta> planetas);
}
