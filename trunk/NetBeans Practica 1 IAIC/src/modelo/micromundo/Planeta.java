package modelo.micromundo;

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

/*
 * Representacion de un planeta del micromundo, los finales son aquellos mayor de 211,
 * es decir, los 4 ultimos planetas siendo indiferente las conexiones que tengan.
 *
 * @author Jose Miguel Guerrero Hernandez 53466473Y
 */
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
	
    //Planeta inicial
	public Planeta(){
		_nodosExpandidos=0;
		_numeroPlaneta=0;
		_pasoApaso=false;
		_siguiente=false;
		_resuelto=false;
		_listaPlanetas=new ArrayList<Planeta>();
		_listaPlanetasExpandidos=new ArrayList<Integer>();
	}

    //Genera el planeta inicial pero con la lista de planetas del micromundo
	public Planeta(ArrayList<Planeta> planetas){
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
		generaHeuristica();
	}

    //Creamos el planeta con el numero deseado y con la lista de los planetas del micromundo
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
		generaHeuristica();
	}

    //Asigna los valores de agua y oxigeno si son mayores que los que ya tenemos
	public void setAguaOxigeno(int agua,int oxigeno){
		if(agua>_cantidadAgua){
			_cantidadAgua=agua;
		}
		if(oxigeno>_cantidadOxigeno){
			_cantidadOxigeno=oxigeno;
		}
	}

    //Devuelve la cantidad de agua del planeta
	public int getAgua(){
		return _cantidadAgua;
	}

    //Devuelve la cantidad de oxigeno del planeta
	public int getOxigeno(){
		return _cantidadOxigeno;
	}

	/*
     * Metodo que genera la heuristica, 200 sera el valor maximo
     * del planeta objetivo al tener 100 de agua y 100 de oxigeno
     * por lo que queremos hacerle 0, de ahi que sea 200 menos el
     * valor de agua y oxigeno del planeta, dividiendolo entre 5
     * para reducir el coste y hacerla mas admisible
     */
	public void generaHeuristica(){
		int agua=_cantidadAgua;
		int oxigeno=_cantidadOxigeno;
		int valorMax=200; //100 agua + 100 oxigeno en estado final
		_valorHeuristico=(valorMax-agua-oxigeno)/5;//cuanto mas se asemeje a 0 mas cerca del final estamos
    }

    //Devuelve el valor heuristico del planeta
	public int dameValorHeuristico(){
        generaHeuristica();
        if(_numeroPlaneta>211)
            return 0;
		return _valorHeuristico;
	}

    /*
     * Metodo que devuelve si se peude viajar entre dos planetas,
     * para ello se obtiene de las matrices el problema y el
     * algoritmo a utilizar, y se lo pasamos al gestor de juegos
     * para que nos diga si tiene solucion o no.
     */
	public boolean resolverProblema(int planeta1, int planeta2){
		//obtenemos el valor de la distancia que sera el numero de problema a resolver
		int problema=MatrizProblemas.getInstancia().ping(planeta1, planeta2);
		//obtenemos el valor del algoritmo para resolver el problema
		int solucion=MatrizSolucionProblema.getInstancia().algoritmo(planeta1, planeta2);
		//asignamos el problema al gestor y nos devuelve si se puede solucionar con el algoritmo deseado
		GestorJuegos.dameInstancia().asignarProblema(problema);
		return GestorJuegos.dameInstancia().solucionar(solucion);
	}

    //Instancia del problema para que AIMA lo utilice
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

    //GENERACION DE SUCESORES

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

            //escribimos en el log de la interfaz y en el fichero
            Log.dameInstancia().agregar("\n-- EXPLORO EL PLANETA "+(valorPlaneta+1));
			_observer.escribeLog("\n-- EXPLORO EL PLANETA "+(valorPlaneta+1));

			//comprobamos si ya ha sido explorado el planeta y si no lo esta lo agregamos y expandimos
			boolean expandir=false;
			if(_listaPlanetasExpandidos.indexOf(valorPlaneta)==-1){
				_listaPlanetasExpandidos.add(valorPlaneta);
				expandir=true;
			}else{
                Log.dameInstancia().agregar("\n** Planeta ya expandido. ");
                _observer.escribeLog("\n** Planeta ya expandido. ");
            }

			String movimiento="";
			int coste=0;
			
			//si hay que expandir comprobamos si podemos viajar a sus hijos
			for(int siguiente=0;(siguiente<vecinos.size() && expandir);siguiente++){
				_observer.informacionStatus("Explorando Planeta "+(valorPlaneta+1));
               
                //si se ejecuta paso a paso esperamos a que podamos avanzar
                if(_pasoApaso){
                    while(!_siguiente){}
                    _siguiente=false;
                }
                //obtenemos un vecino
                int planetaVecino=vecinos.get(siguiente);
				movimiento="Paso del planeta "+(valorPlaneta+1)+" al planeta vecino "+(planetaVecino+1);
				_observer.informacionStatus("Explorando Planeta "+(valorPlaneta+1)+": resolviendo problema para intentar viajar al Planeta "+(planetaVecino+1));
                
                //vemos si podemos viajar al planeta vecino
                boolean pasar=resolverProblema(valorPlaneta,planetaVecino);
				//como hemos cargado el juego, obtenemos su valor
				coste=GestorJuegos.dameCosteProblema();
                //si no podemos viajar
				if(!pasar){
					//obtenemos el valor de la distancia que sera el numero de problema a resolver
					int problema=MatrizProblemas.getInstancia().ping(valorPlaneta, planetaVecino);
					//obtenemos el valor del algoritmo para resolver el problema
					int solucion=MatrizSolucionProblema.getInstancia().algoritmo(valorPlaneta, planetaVecino);

                    //conectamos el planeta con una linea roja
					conectaPlanetas(valorPlaneta, planetaVecino, 0);
                    Log.dameInstancia().agregar("No puedo resolver el problema \""+GestorJuegos.dameInstancia().dameEnunciadoProblema()+"\" con el algoritmo \""+GestorJuegos.dameInstancia().dameNombreAlgoritmo()+"\" para viajar al planeta "+(planetaVecino+1));
                    _observer.escribeLog("No puedo resolver el problema \""+GestorJuegos.dameInstancia().dameEnunciadoProblema()+"\" con el algoritmo \""+GestorJuegos.dameInstancia().dameNombreAlgoritmo()+"\" para viajar al planeta "+(planetaVecino+1));
				}else{
                    //si puede viajar
                    //conectamos el planeta con una linea verde
                    conectaPlanetas(valorPlaneta, planetaVecino, 1);
                    int heuristicaVecino=pla._listaPlanetas.get(planetaVecino).dameValorHeuristico();
                    Log.dameInstancia().agregar("* Puedo resolver el problema \""+GestorJuegos.dameInstancia().dameEnunciadoProblema()+"\" con el algoritmo \""+GestorJuegos.dameInstancia().dameNombreAlgoritmo()+"\" para viajar al planeta "+(planetaVecino+1)+" con coste real "+coste+" y heuristica "+heuristicaVecino);
                    _observer.escribeLog("* Puedo resolver el problema \""+GestorJuegos.dameInstancia().dameEnunciadoProblema()+"\" con el algoritmo \""+GestorJuegos.dameInstancia().dameNombreAlgoritmo()+"\" para viajar al planeta "+(planetaVecino+1)+" con coste real "+coste+" y heuristica "+heuristicaVecino);
                }

				Planeta nuevoEstado=_listaPlanetas.get(planetaVecino);
				//si podemos viajar y el estado nuevo es valido
				if (pasar && nuevoEstado.valido()){
                    //agregamos a la accion el coste del paso de un planeta a otro.
					String datos=movimiento+" ,COSTE:"+coste;
					sucesores.add(new Successor(datos, nuevoEstado));
				}
			}
			return sucesores;
		}
	}

    // COMPROBACION DEL ESTADO FINAL

    //Si estamos en un planeta con mayor numero de 211 sera un estado final
	public class EsFinal implements GoalTest{
		public boolean isGoalState(Object state) {
			Planeta pla=(Planeta)state;
			if(pla._numeroPlaneta>211){
				_resuelto=true;
			}
			return _resuelto;
		}
	}

    //VALOR HEURISTICO

    //devuelve el valor heuristico del planeta
	public class ValorHeuristico implements HeuristicFunction{
		public int getHeuristicValue(Object state) {
			Planeta pla=(Planeta)state;
			return pla.dameValorHeuristico();
		}
	}

    //VALOR REAL

    /*
     * Obtenemos el coste de la accion sucedida entre la transicion, toda transicion
     * va a tener la palabra COSTE: por lo que podemos extraer de ahi el valor real.
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
