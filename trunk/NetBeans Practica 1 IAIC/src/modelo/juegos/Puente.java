package modelo.juegos;

import java.util.ArrayList;
import java.util.List;

import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

/*
 * Cuatro amigos deben cruzar un fragil puente de madera. Es de noche y es indispensable usar una linterna para
 * cruzarlo.
 * El puente solo puede aguantar el peso de dos personas como m�ximo y solo tienen una linterna.
 * Tienen que cruzarlo antes de 15 minutos.
 * Mercedes tarda 8 minutos en cruzarlo, Javier 4 minutos,Carlos tarda 2 y Daniel 1.
 *
 * @author Mercedes Bernal Perez 71031656C
 */

public class Puente extends InterfazJuego{

	/**
	 * Posici�n de la linterna (Lado del puente: 1-izquierda, 0-derecha).
	 */
	private int _posLinterna;
	
	/**
	 * Posici�n de Mercedes (Lado del puente: 1-izquierda, 0-derecha).
	 */
	private int _posMercedes;
	
	/**
	 * Posici�n de Javier (Lado del puente: 1-izquierda, 0-derecha).
	 */
	private int _posJavier;
	
	/**
	 * Posici�n de Carlos (Lado del puente: 1-izquierda, 0-derecha).
	 */
	private int _posCarlos;
	
	/**
	 * Posici�n de Daniel (Lado del puente: 1-izquierda, 0-derecha).
	 */
	private int _posDaniel;	
	
	/**
	 * Tiempo que queda disponible para cruzar el puente.
	 */
	private int _tiempo;
	
	private int _dificultad=4;
	
	public Puente() {
        _enunciadoProblema="Hay 4 amigos que desean pasar un puente en el menor tiempo posible ya que solo hay una linterna, indispensable para cruzar.";
		_nodosExpandidos=0;
		_resuelto=false;
		_posLinterna = 1;		
		_posMercedes = 1;		
		_posJavier = 1;	
		_posCarlos = 1;		
		_posDaniel = 1;
		_tiempo = 15;
	} 
	
	/**
	 * Crea una instancia del estado actual del problema de el lobo la oveja y la col.
	 * @param posicion del lobo
	 * @param posicion de la oveja
	 * @param posicion de la col
	 * @param posicion de la barca
     */
	
	public Puente(int linterna, int mercedes, int javier, int carlos, int daniel, int tiempo) {
		_posLinterna = linterna;
		_posMercedes = mercedes;
		_posJavier = javier;
		_posCarlos = carlos;
		_posDaniel = daniel;
		_tiempo = tiempo;
	}

	public Problem getProblema() {
		Problem problem = new Problem(new Puente(), new Sucesores(), new EsFinal(),new ValorReal(), new ValorHeuristico());
		return problem;	
	}

	public boolean valido(){
		if(_nodosExpandidos>5000){
			return false;
		}
		//Es v�lido si queda tiempo.
		return (_tiempo>=0);
	}
	
	public int dificultad(){
		return _dificultad;
	}
	
	//------------------------------------------------- CLASES FUNCIONES

    //GENERACION DE SUCESORES

	@SuppressWarnings({"unchecked"})
	public class Sucesores implements SuccessorFunction{
		public List<Successor> getSuccessors(Object state) {
			Puente puente=(Puente)state;
			List sucesores=new ArrayList();
			String movimiento="";
			int coste=0;
			boolean generado=false;
			_nodosExpandidos++;
		    int newlinterna = 1, newmercedes = 1, newjavier = 1, newcarlos  = 1, newdaniel = 1, newtiempo = 0;
    
		 	for (int operador = 0; operador<10;operador++){
		 		newlinterna = puente._posLinterna;
		 		newmercedes = puente._posMercedes;
		 		newjavier = puente._posJavier;
		 		newcarlos = puente._posCarlos;
		 		newdaniel = puente._posDaniel;
		 		newtiempo = puente._tiempo;
		 		generado=false;
		 		
		 		switch(operador){
		 		case 0:	// Cruza Mercedes sola (con la linterna).
		 			// Para poder cruzar Mercedes la linterna tiene que estar con ella.
		 			if(puente._posLinterna == puente._posMercedes){
		 				generado = true;
		 				movimiento = "cruzaMercedes";
		 				//Cruza la linterna.
		 				newlinterna = 1 - puente._posLinterna;
		 				//Cruza Mercedes.
		 				newmercedes = 1 - puente._posMercedes;
		 				// Los dem�s se quedan donde est�n.
		 				newjavier = puente._posJavier;
		 				newcarlos = puente._posCarlos;
		 				newdaniel = puente._posDaniel;
		 				//Actualizar el coste y el tiempo.
		 				coste = 8; // Tiempo que tarda Mercedes en cruzar.
		 				newtiempo = (int) (puente._tiempo - coste);
		 			}
		 			break;
		 		case 1:	// Cruza Javier solo (con la linterna).
		 			// Para poder cruzar Javier la linterna tiene que estar con el.
		 			if(puente._posLinterna == puente._posJavier){
		 				generado = true;
		 				movimiento = "cruzaJavier";
		 				//Cruza la linterna.
		 				newlinterna = 1 - puente._posLinterna;
		 				//Cruza Javier
		 				newjavier = 1 - puente._posJavier;
		 				// Los dem�s se quedan donde est�n.
		 				newmercedes = puente._posMercedes;
		 				newcarlos = puente._posCarlos;
		 				newdaniel = puente._posDaniel;
		 				//Actualizar el coste y el tiempo.
		 				coste = 4; // Tiempo que tarda Javier en cruzar.
		 				newtiempo = (int) (puente._tiempo - coste);
		 			}
		 			break;
		 		case 2:	// Cruza Carlos solo (con la linterna).
		 			// Para poder cruzar Carlos la linterna tiene que estar con el.
		 			if(puente._posLinterna == puente._posCarlos){
		 				generado = true;
		 				movimiento = "cruzaCarlos";
		 				//Cruza la linterna.
		 				newlinterna = 1 - puente._posLinterna;
		 				//Cruza Carlos.
		 				newcarlos = 1 - puente._posCarlos;
		 				// Los dem�s se quedan donde est�n.
		 				newmercedes = puente._posMercedes;
		 				newjavier = puente._posJavier;
		 				newdaniel = puente._posDaniel;
		 				//Actualizar el coste y el tiempo.
		 				coste = 2; // Tiempo que tarda Carlos en cruzar.
		 				newtiempo = (int) (puente._tiempo - coste);
		 			}
		 			break;
		 		case 3:	// Cruza Daniel solo (con la linterna).
		 			// Para poder cruzar Daniel la linterna tiene que estar con el.
		 			if(puente._posLinterna == puente._posDaniel){
		 				generado = true;
		 				movimiento = "cruzaDaniel";
		 				//Cruza la linterna.
		 				newlinterna = 1 - puente._posLinterna;
		 				//Cruza Daniel.
		 				newdaniel = 1 - puente._posDaniel;
		 				// Los dem�s se quedan donde est�n.
		 				newmercedes = puente._posMercedes;
		 				newjavier = puente._posJavier;
		 				newcarlos = puente._posCarlos;
		 				//Actualizar el coste y el tiempo.
		 				coste = 1; // Tiempo que tarda Daniel en cruzar.
		 				newtiempo = (int) (puente._tiempo - coste);
		 			}
		 			break;
		 		case 4:	// Cruza Mercedes con Javier (con la linterna).
		 			// Para poder cruzar Mercedes y Javier la linterna tiene que estar con ellos.
		 			if((puente._posLinterna == puente._posMercedes)&&(puente._posLinterna == puente._posJavier)){
		 				generado = true;
		 				movimiento = "cruzanMercedesJavier";
		 				//Cruza la linterna.
		 				newlinterna = 1 - puente._posLinterna;
		 				//Cruza Mercedes.
		 				newmercedes = 1 - puente._posMercedes;
		 				//Cruza Javier.
		 				newjavier = 1 - puente._posJavier;
		 				// Los dem�s se quedan donde est�n.
		 				newcarlos = puente._posCarlos;
		 				newdaniel = puente._posDaniel;
		 				//Actualizar el coste y el tiempo.
		 				coste = 8; // Tiempo del que m�s tarda en cruzar que es Mercedes.
		 				newtiempo = (int) (puente._tiempo - coste);
		 			}
		 			break;
		 		case 5:	// Cruza Mercedes con Carlos (con la linterna).
		 			// Para poder cruzar Mercedes y Carlos la linterna tiene que estar con ellos.
		 			if((puente._posLinterna == puente._posMercedes)&&(puente._posLinterna == puente._posCarlos)){
		 				generado = true;
		 				movimiento = "cruzanMercedesCarlos";
		 				//Cruza la linterna.
		 				newlinterna = 1 - puente._posLinterna;
		 				//Cruza Mercedes.
		 				newmercedes = 1 - puente._posMercedes;
		 				//Cruza Carlos.
		 				newcarlos = 1 - puente._posCarlos;
		 				// Los demas se quedan donde estan.
		 				newjavier = puente._posJavier;
		 				newdaniel = puente._posDaniel;
		 				//Actualizar el coste y el tiempo.
		 				coste = 8; // Tiempo del que mas tarda en cruzar que es Mercedes.
		 				newtiempo = (int) (puente._tiempo - coste);
		 			}
		 			break;
		 		case 6:	// Cruza Mercedes con Daniel (con la linterna).
		 			// Para poder cruzar Mercedes y Daniel la linterna tiene que estar con ellos.
		 			if((puente._posLinterna == puente._posMercedes)&&(puente._posLinterna == puente._posDaniel)){
		 				generado = true;
		 				movimiento = "cruzanMercedesDaniel";
		 				//Cruza la linterna.
		 				newlinterna = 1 - puente._posLinterna;
		 				//Cruza Mercedes.
		 				newmercedes = 1 - puente._posMercedes;
		 				//Cruza Daniel.
		 				newdaniel = 1 - puente._posDaniel;
		 				// Los dem�s se quedan donde est�n.
		 				newjavier = puente._posJavier;
		 				newcarlos = puente._posCarlos;
		 				//Actualizar el coste y el tiempo.
		 				coste = 8; // Tiempo del que m�s tarda en cruzar que es Mercedes.
		 				newtiempo = (int) (puente._tiempo - coste);
		 			}
		 			break;
		 		case 7:	// Cruza Javier con Carlos (con la linterna).
		 			// Para poder cruzar Javier y Carlos la linterna tiene que estar con ellos.
		 			if((puente._posLinterna == puente._posJavier)&&(puente._posLinterna == puente._posCarlos)){
		 				generado = true;
		 				movimiento = "cruzanJavierCarlos";
		 				//Cruza la linterna.
		 				newlinterna = 1 - puente._posLinterna;
		 				//Cruza Javier.
		 				newjavier = 1 - puente._posJavier;
		 				//Cruza Carlos.
		 				newcarlos = 1 - puente._posCarlos;
		 				// Los dem�s se quedan donde est�n.
		 				newmercedes = puente._posMercedes;
		 				newdaniel = puente._posDaniel;
		 				//Actualizar el coste y el tiempo.
		 				coste = 4; // Tiempo del que m�s tarda en cruzar que es Javier.
		 				newtiempo = (int) (puente._tiempo - coste);
		 			}
		 			break;
		 		case 8:	// Cruza Javier con Daniel (con la linterna).
		 			// Para poder cruzar Javier y Daniel la linterna tiene que estar con ellos.
		 			if((puente._posLinterna == puente._posJavier)&&(puente._posLinterna == puente._posDaniel)){
		 				generado = true;
		 				movimiento = "cruzanJavierDaniel";
		 				//Cruza la linterna.
		 				newlinterna = 1 - puente._posLinterna;
		 				//Cruza Javier.
		 				newjavier = 1 - puente._posJavier;
		 				//Cruza Daniel.
		 				newdaniel = 1 - puente._posDaniel;
		 				// Los dem�s se quedan donde est�n.
		 				newmercedes = puente._posMercedes;
		 				newcarlos = puente._posCarlos;
		 				//Actualizar el coste y el tiempo.
		 				coste = 4; // Tiempo del que m�s tarda en cruzar que es Javier.
		 				newtiempo = (int) (puente._tiempo - coste);
		 			}
		 			break;
		 		case 9: // Cruza Carlos con Daniel (con la linterna).
		 			// Para poder cruzar Carlos y Daniel la linterna tiene que estar con ellos.
		 			if((puente._posLinterna == puente._posCarlos)&&(puente._posLinterna == puente._posDaniel)){
		 				generado = true;
		 				movimiento = "cruzanCarlosDaniel";
		 				//Cruza la linterna.
		 				newlinterna = 1 - puente._posLinterna;
		 				//Cruza Carlos.
		 				newcarlos = 1 - puente._posCarlos;
		 				//Cruza Daniel.
		 				newdaniel = 1 - puente._posDaniel;
		 				// Los dem�s se quedan donde est�n.
		 				newmercedes = puente._posMercedes;
		 				newjavier = puente._posJavier;
		 				//Actualizar el coste y el tiempo.
		 				coste = 2; // Tiempo del que m�s tarda en cruzar que es Carlos.
		 				newtiempo = (int) (puente._tiempo - coste);
		 			}
		 			break;
		 		}

		 		Puente nuevoEstado = new Puente(newlinterna,newmercedes,newjavier,newcarlos,newdaniel,newtiempo);
		 		if (generado && nuevoEstado.valido()){
					String orillas  = "("+newlinterna+","+newmercedes+","+newjavier+","+newcarlos+","+newdaniel+")";
					orillas += "tiempo: "+newtiempo;
					
		 			String datos=movimiento+" ,COSTE:"+coste+orillas;
					sucesores.add(new Successor(datos, nuevoEstado));
		 		}
		 	}
		 	return sucesores;
		}
	}

    // COMPROBACION DEL ESTADO FINAL

	// Solucion si todos los componentes estan a la derecha.
	public class EsFinal implements GoalTest{
		public boolean isGoalState(Object state) {
			_resuelto=false;
			Puente puente=(Puente)state;
			if((puente._posLinterna == 0)&&(puente._posMercedes == 0)&&(puente._posJavier == 0)&&
				(puente._posCarlos == 0)&&(puente._posDaniel == 0)){
				_resuelto=true;
			}			
			return _resuelto;
		}
	}

    //VALOR HEURISTICO

	// Heuristica: Mejor cuanto menor sea heuristica, es decir, cuanto menos componentes
	// estan en el lado izquierdo teniendo en cuenta lo que tardan en cruzar.
	public class ValorHeuristico implements HeuristicFunction{
		public int getHeuristicValue(Object state) {
			int heuristica = 0;
			Puente puente =(Puente)state;
			heuristica = puente._posLinterna + puente._posMercedes*8 + puente._posJavier*4 + puente._posCarlos*2 + puente._posDaniel;
			return heuristica;
		}
	}
}
