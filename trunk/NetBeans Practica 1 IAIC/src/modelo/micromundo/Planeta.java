package modelo.micromundo;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import modelo.juegos.GestorJuegos;
import modelo.matrices.MatrizConexiones;
import modelo.matrices.MatrizProblemas;
import modelo.matrices.MatrizSolucionProblema;

import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;
import aima.search.framework.StepCostFunction;

public class Planeta extends InterfazPlaneta{	

	private int _numeroPlaneta=0;
	private int _valorHeuristico=0;
	/*
	 * valores que van de 0 a 100, indican el porcentaje de agua que tiene un planeta, 
	 * inicial 0-0, final 200-200 y los planetas intermedios tendran un valor mayor
	 * cuanto mas cerca de un estado final esten y proporcional a la distancia.
	 */
	private int _cantidadOxigeno=0;
	private int _cantidadAgua=0;
	private ArrayList<Integer> _planetasVecinos=null;
	private ArrayList<Integer> _costeplanetasVecinos=new ArrayList<Integer>();
	
	public Planeta(){
		_nodosExpandidos=0;
		_numeroPlaneta=0;
		_pasoApaso=false;
		_siguiente=false;
		_resuelto=false;
		_listaPlanetas=new ArrayList<Planeta>();
		_listaPlanetasExpandidos=new ArrayList<Integer>();
	}

	public Planeta(ArrayList<Planeta> planetas){
		// planeta inicial
		_nodosExpandidos=0;
		_numeroPlaneta=0;
		_pasoApaso=false;
		_siguiente=false;
		_resuelto=false;
		_listaPlanetasExpandidos=new ArrayList<Integer>();
		_listaPlanetas=planetas;
		_planetasVecinos=MatrizConexiones.getInstancia().damePlanetasContiguos(_numeroPlaneta);
		_cantidadAgua=0;
		_cantidadOxigeno=0;
		for(int i=0;i<_planetasVecinos.size();i++){
			_costeplanetasVecinos.add(MatrizProblemas.getInstancia().ping(_numeroPlaneta, _planetasVecinos.get(i)));
		}
		generaHeuristica();
	}
	
	public Planeta(int numeroPlaneta, ArrayList<Planeta> listaPlanetas){
		_numeroPlaneta=numeroPlaneta;
		_listaPlanetas=listaPlanetas;
		if(_numeroPlaneta==0){//si es inicial
			_cantidadOxigeno=0;
			_cantidadAgua=0;
		}else if(_numeroPlaneta>211){//si es final
			_cantidadOxigeno=100;
			_cantidadAgua=100;
		}
		_planetasVecinos=MatrizConexiones.getInstancia().damePlanetasContiguos(_numeroPlaneta);
		for(int i=0;i<_planetasVecinos.size();i++){
			_costeplanetasVecinos.add(MatrizProblemas.getInstancia().ping(_numeroPlaneta, _planetasVecinos.get(i)));
		}
		generaHeuristica();
	}
	
	public void setAguaOxigeno(int agua,int oxigeno){
		//asignamos los valores si son mejores que los que tenemos
		if(agua>_cantidadAgua){
			_cantidadAgua=agua;
		}
		if(oxigeno>_cantidadOxigeno){
			_cantidadOxigeno=oxigeno;
		}
	}
	
	public int getAgua(){
		return _cantidadAgua;
	}
	
	public int getOxigeno(){
		return _cantidadOxigeno;
	}
	
	public void asignaLista(ArrayList<Planeta> planetas){
		_listaPlanetas=planetas;
	}
	
	
	public void generaHeuristica(){
		int agua=_cantidadAgua;
		int oxigeno=_cantidadOxigeno;
		int valorMax=200; //100 agua + 100 oxigeno en estado final
		_valorHeuristico=(valorMax-agua-oxigeno)/5;//cuanto mas se asemeje a 0 mas cerca del final estamos
    }
	
	public int dameValorHeuristico(){
        generaHeuristica();
        if(_numeroPlaneta>211)
            return 0;
		return _valorHeuristico;
	}
	
	public boolean resolverProblema(int planeta1, int planeta2){
		//obtenemos el valor de la distancia que sera el numero de problema a resolver
		int problema=MatrizProblemas.getInstancia().ping(planeta1, planeta2);
		//obtenemos el valor del algoritmo para resolver el problema
		int solucion=MatrizSolucionProblema.getInstancia().algoritmo(planeta1, planeta2);
		
		GestorJuegos.dameInstancia().asignarProblema(problema);
		return GestorJuegos.dameInstancia().solucionar(solucion);
	}
	
	public Problem getProblema(ArrayList<Planeta> planetas) {
		Problem problem = new Problem(new Planeta(planetas), new Sucesores(), new EsFinal(),new ValorReal(), new ValorHeuristico());
        return problem;
	}
	
	public boolean resuelto() {
		return _resuelto;
	}
	
	public boolean valido(){
		return (_nodosExpandidos<5000);
	}
	
	public void pasoApaso(){
		_pasoApaso=true;
	}
	
	public void siguiente(){
		_siguiente=true;
	}
	
	public void continuo(){
		_siguiente=false;
		_pasoApaso=false;
	}

    public int numeroPlaneta(){
		return _numeroPlaneta;
	}

	//------------------------------------------------- CLASES FUNCIONES

	@SuppressWarnings({"unchecked"})
	public class Sucesores implements SuccessorFunction{
		public List<Successor> getSuccessors(Object state) {
			Planeta pla=(Planeta)state;
			List sucesores=new ArrayList();
			ArrayList<Integer> vecinos=pla._planetasVecinos;
			int heuristica=pla.dameValorHeuristico();
			int valorPlaneta=pla._numeroPlaneta;
			_nodosExpandidos++;
			
            //colocamos la nave
            posicionarNave(valorPlaneta);
            
			//comprobamos si ya ha sido explorado el planeta y si no lo esta lo agregmos y expandimos
			/*boolean expandir=false;
			if(_listaPlanetasExpandidos.indexOf(valorPlaneta)==-1){
				_listaPlanetasExpandidos.add(valorPlaneta);
				expandir=true;
			}*/

            Log.dameInstancia().agregar("\n-- EXPLORO EL PLANETA "+(valorPlaneta+1));
			_observer.escribeLog("\n-- EXPLORO EL PLANETA "+(valorPlaneta+1));

			String movimiento="";
			int coste=0;
			
			//si hay que expandir comprobamos si podemos viajar a sus hijos
			for(int siguiente=0;(siguiente<vecinos.size()/* && expandir*/);siguiente++){
				_observer.informacionStatus("Explorando Planeta "+(valorPlaneta+1));
                if(_pasoApaso){
                    while(!_siguiente){}
                    _siguiente=false;
                }

                int planetaVecino=vecinos.get(siguiente);
				movimiento="Paso del planeta "+(valorPlaneta+1)+" al planeta vecino "+(planetaVecino+1);
				_observer.informacionStatus("Explorando Planeta "+(valorPlaneta+1)+": resolviendo problema para intentar viajar al Planeta "+(planetaVecino+1));
                boolean pasar=resolverProblema(valorPlaneta,planetaVecino);
				//como hemos cargado el juego, obtenemos su valor
				coste=GestorJuegos.dameCosteProblema();
				if(!pasar){
					//obtenemos el valor de la distancia que sera el numero de problema a resolver
					int problema=MatrizProblemas.getInstancia().ping(valorPlaneta, planetaVecino);
					//obtenemos el valor del algoritmo para resolver el problema
					int solucion=MatrizSolucionProblema.getInstancia().algoritmo(valorPlaneta, planetaVecino);
					
					conectaPlanetas(valorPlaneta, planetaVecino, 0);
                    Log.dameInstancia().agregar("No puedo resolver el problema \""+GestorJuegos.dameInstancia().dameEnunciadoProblema()+"\" con el algoritmo \""+GestorJuegos.dameInstancia().dameNombreAlgoritmo()+"\" para viajar al planeta "+(planetaVecino+1));
                    _observer.escribeLog("No puedo resolver el problema \""+GestorJuegos.dameInstancia().dameEnunciadoProblema()+"\" con el algoritmo \""+GestorJuegos.dameInstancia().dameNombreAlgoritmo()+"\" para viajar al planeta "+(planetaVecino+1));

				}else{
                    //puede pasar
                    conectaPlanetas(valorPlaneta, planetaVecino, 1);
                    int heuristicaVecino=pla._listaPlanetas.get(planetaVecino).dameValorHeuristico();
                    Log.dameInstancia().agregar("* Puedo resolver el problema \""+GestorJuegos.dameInstancia().dameEnunciadoProblema()+"\" con el algoritmo \""+GestorJuegos.dameInstancia().dameNombreAlgoritmo()+"\" para viajar al planeta "+(planetaVecino+1)+" con coste real "+coste+" y heuristica "+heuristicaVecino);
                    _observer.escribeLog("* Puedo resolver el problema \""+GestorJuegos.dameInstancia().dameEnunciadoProblema()+"\" con el algoritmo \""+GestorJuegos.dameInstancia().dameNombreAlgoritmo()+"\" para viajar al planeta "+(planetaVecino+1)+" con coste real "+coste+" y heuristica "+heuristicaVecino);
                }

				Planeta nuevoEstado=_listaPlanetas.get(planetaVecino);
				
				if (pasar && nuevoEstado.valido()){
					Estadisticas.dameInstancia().insertaValorReal(coste);
					Estadisticas.dameInstancia().insertaValorHeuristico(heuristica);
					String datos=movimiento+" ,COSTE:"+coste;
					sucesores.add(new Successor(datos, nuevoEstado));
				}

			}
			return sucesores;
		}
	}

	public class EsFinal implements GoalTest{
		public boolean isGoalState(Object state) {
			Planeta pla=(Planeta)state;
			if(pla._numeroPlaneta>211){
				_resuelto=true;
			}
			return _resuelto;
		}
	}
	
	public class ValorHeuristico implements HeuristicFunction{
		public int getHeuristicValue(Object state) {
			Planeta pla=(Planeta)state;
			return pla.dameValorHeuristico();
		}
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
