package modelo.juegos;

import java.util.ArrayList;
import java.util.List;

import aima.search.framework.GoalTest;
import aima.search.framework.HeuristicFunction;
import aima.search.framework.Problem;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

/*
     El juego del lobo, la oveja y la col se representa mediante cuatro variables.
     booleanas que indican la posicion de cada uno de los personajes del problema.
     Para todos ellos 'True' representa que el personaje esta situado a la izquierda
     del rio y 'False' a la derecha.

     En el estado inicial del problema todos los personajes estan situados a la
     izquierda del rio.

     El objetivo del problema es hacer que todos los personajes crucen el rio.
     Para ello se debera tener en cuenta las siguientes restricciones:

        El lobo se come a la oveja si se encuentran solos.
        La oveja se come a la col si se encuentran solos.
        El granjero solo puede llevar a un animal a la vez.

     @author Victor Adail Ferrer 02662811-D
 */
public class LoboOvejaYCol extends InterfazJuego{

	/**
     * Indica si el lobo esta a la izquierda del rio (true) o 
     * a la derecha (false) 
     */
	private boolean _lobo=true;
	
	/**
     * Indica si la oveja esta a la izquierda del rio (true) o 
     * a la derecha (false) 
     */
	private boolean _oveja=true;
	
	/**
     * Indica si la col esta a la izquierda del rio (true) o 
     * a la derecha (false) 
     */
	private boolean _col=true;
	
	/**
     * Indica si la barca esta a la izquierda del rio (true) o 
     * a la derecha (false) 
     */
	private boolean _barca=true;
	
	private int _dificultad=6;
	
	public LoboOvejaYCol() {
        _enunciadoProblema="En una orilla hay un Granjero, una Cabra, un Lobo y una Col, y hay que cruzar a la otra orilla sabiendo que no se pueden quedar solos (sin el granjero) la Cabra con la Col o el Lobo con la Cabra.";
		_nodosExpandidos=0;
		_resuelto=false;
		_lobo = true;
		_oveja = true;
		_col = true;
		_barca=true;
	} 
	
	/**
	 * Crea una instancia del estado actual del problema de el lobo la oveja y la col.
	 * @param posicion del lobo
	 * @param posicion de la oveja
	 * @param posicion de la col
	 * @param posicion de la barca
     */
	public LoboOvejaYCol(boolean lobo, boolean oveja, boolean col, boolean barca) {
		_lobo = lobo;
		_oveja = oveja;
		_col = col;
		_barca= barca;
	} 
	
	public Problem getProblema() {
		Problem problem = new Problem(new LoboOvejaYCol(), new Sucesores(), new EsFinal(),new ValorReal() , new ValorHeuristico());
		return problem;	
	}

	public boolean valido(){
		boolean aux=true;
		if(_nodosExpandidos>5000){
			aux=false;
		}else if(_lobo == _oveja && _barca!=_lobo){
			aux=false;
		}else if(_oveja == _col && _barca!=_oveja){
			aux=false;
		}
		return aux;
	}
	
	public int dificultad(){
		return _dificultad;
	}
	
	//------------------------------------------------- CLASES FUNCIONES

	@SuppressWarnings({"unchecked"})
	public class Sucesores implements SuccessorFunction{
		public List<Successor> getSuccessors(Object state) {
			LoboOvejaYCol granja=(LoboOvejaYCol)state;
			List sucesores=new ArrayList();
			String movimiento="";
			int coste=0;
			boolean generado=false;
			_nodosExpandidos++;
		    boolean newlobo, newoveja, newcol, newbarca;
		    
            for (int operador= 0; operador<4; operador++){
            	newlobo=granja._lobo;
            	newoveja=granja._oveja;
            	newcol=granja._col;
            	newbarca=granja._barca;
            	generado=false;
                switch(operador){
					case 0://cruza lobo
						if(granja._lobo == granja._barca){
							
							if(granja._barca){
								newlobo = false;
								newbarca = false;
							}else{
								newlobo = true;
								newbarca = true;
							}
							generado=true;
							coste=1;
							movimiento="Cruza el lobo";
						}
						break;
					case 1://cruza oveja
						if(granja._oveja == granja._barca){
							
							if(granja._barca){
								newoveja = false;
								newbarca = false;
							}else{
								newoveja = true;
								newbarca = true;
							}	
							generado=true;
							coste=1;
							movimiento="Cruza la oveja";
						}
						break;
					case 2://cruza col
						if(granja._col == granja._barca){
							
							if(granja._barca){
								newcol = false;
								newbarca = false;
								
							}else{
								newcol = true;
								newbarca = true;
							}
							generado=true;
							coste=1;
							movimiento="Cruza la col";
						}
						break;
					case 3://cruza barca
						if(granja._barca){
		                	newbarca = false;
		                }else{
							newbarca = true;
						}	
						
						generado=true;
						coste=1;
						movimiento="Cruza el granjero";
						
						break;
				}
                                
                LoboOvejaYCol nuevoEstado = new LoboOvejaYCol(newlobo, newoveja, newcol, newbarca);
				
                if (generado && nuevoEstado.valido()){
					String orillaIzq="\nIZQUIERDA( ";
					String orillaDer="\nDERECHA( ";
					String orillas  = "";
					
					if(newlobo)
						orillaIzq = orillaIzq + "LOBO,";
					else
						orillaDer = orillaDer + "LOBO,";
					
					if(newoveja)
						orillaIzq = orillaIzq + "OVEJA,";
					else
						orillaDer = orillaDer + "OVEJA,";
					
					if(newcol)
						orillaIzq = orillaIzq + "COL,";
					else
						orillaDer = orillaDer + "COL,";
					
					if(newbarca)
						orillaIzq = orillaIzq + "BARCA,";
					else
						orillaDer = orillaDer + "BARCA,";
					
					orillaIzq = orillaIzq.substring(0, orillaIzq.length()-1) + ")";
					orillaDer = orillaDer.substring(0, orillaDer.length()-1) + ")";
					
					orillas = orillaIzq + orillaDer;
					
					String datos=movimiento+" ,COSTE:"+coste+" "+orillas;
					sucesores.add(new Successor(datos, nuevoEstado));
				}
			}
			return sucesores;
		}
	}

	// COMPROBACION DEL ESTADO FINAL

    // Comprobamos si han cruzado el rio todos los personajes
	public class EsFinal implements GoalTest{
		public boolean isGoalState(Object state) {
			_resuelto=false;
			LoboOvejaYCol granja=(LoboOvejaYCol)state;
			if(!granja._lobo && !granja._oveja && !granja._col && !granja._barca){
				_resuelto=true;
			}
			
			return _resuelto;
		}
	}

    /**
      * Genera la heuristica para este problema, cuenta los personajes que no
      * han cruzado el rio.
      */
	public class ValorHeuristico implements HeuristicFunction{
		public int getHeuristicValue(Object state) {
			int heuristica = 0;
			LoboOvejaYCol granja =(LoboOvejaYCol)state;
			if(granja._lobo)
				heuristica++;
			if(granja._oveja)
				heuristica++;
			if(granja._col)
				heuristica++;
			if(granja._barca)
				heuristica++;
			if(granja._barca!=granja._lobo && granja._oveja==granja._lobo && granja._col==granja._lobo)
				heuristica++;
			return heuristica;
		}
	}
}
