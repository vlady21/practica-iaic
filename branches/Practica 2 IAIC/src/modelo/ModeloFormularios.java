/*
 * Clase que implementa el modelo de la aplicacion
 */

package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;
import utilerias.Constantes;
import utilerias.Propiedades;

/**
 * AUTORES:
 * @author Victor Adaíl Ferrer
 * @author José Miguel Guerrero Hernández
 */
public class ModeloFormularios implements IZModeloFormularios, Serializable {

    //Vector de observadores
    public Vector<IZObservadorFormularios> mObserver;

    //Tablas del modelo de la aplicacion
    private TablaFormulario tblTecnico;
    private TablaFormulario tblJuridico;
    private TablaFormulario tblAfectivo;

    //Indicadores de estado modificado de las tablas
    private boolean modificadoTecnico;
    private boolean modificadoJuridico;
    private boolean modificadoAfectivo;

    /**
     * Constructor del modelo
     */
    public ModeloFormularios(){

        mObserver = new Vector<IZObservadorFormularios>();
        modificadoTecnico = false;
        modificadoJuridico = false;
        modificadoAfectivo = false;

        tblTecnico = new TablaFormulario(Propiedades.getPropiedades(Constantes.FORMULARIO_TECNICO));
        tblJuridico = new TablaFormulario(Propiedades.getPropiedades(Constantes.FORMULARIO_JURIDICO));
        tblAfectivo = new TablaFormulario(Propiedades.getPropiedades(Constantes.FORMULARIO_AFECTIVO));
    }

    /**
     * Activa indicador de cambio en todas las tablas
     */
    public void setCambio(){
        modificadoTecnico = true;
        modificadoJuridico = true;
        modificadoAfectivo = true;
    }

    /**
     * Establece la tabla de tecnico
     * @param tabla: tabla a insertar en el modelo de la aplicacion
     */
    public void setTecnico(TablaFormulario tabla){
        tblTecnico = tabla;
    }

    /**
     * Establece la tabla de juridico
     * @param tabla: tabla a insertar en el modelo de la aplicacion
     */
    public void setJuridico(TablaFormulario tabla){
        tblJuridico = tabla;
    }

    /**
     * Establece la tabla de afectivo
     * @param tabla: tabla a insertar en el modelo de la aplicacion
     */
    public void setAfectivo(TablaFormulario tabla){
        tblAfectivo = tabla;
    }

    /**
     * Obtiene la tabla de tecnico
     * @return TablaFormulario: tabla a obtener del modelo de la aplicacion
     */
    public TablaFormulario getTecnico () {
        return tblTecnico;
    }

    /**
     * Obtiene la tabla de juridico
     * @return TablaFormulario: tabla a obtener del modelo de la aplicacion
     */
    public TablaFormulario getJuridico () {
        return tblJuridico;
    }

    /**
     * Obtiene la tabla de afectivo
     * @return TablaFormulario: tabla a obtener del modelo de la aplicacion
     */
    public TablaFormulario getAfectivo () {
        return tblAfectivo;
    }

    /**
     * Introduce un campo en el modelo
     * @param clave: clave del campo del modelo
     * @param valor: valor del campo del modelo
     */
    public void setState (String clave, String valor) {

        if(tblTecnico.contiene(clave)){
            tblTecnico.actualiza(clave,valor);
            modificadoTecnico = true;
        }
        if(tblJuridico.contiene(clave)){
            tblJuridico.actualiza(clave,valor);
            modificadoJuridico = true;
        }
        if(tblAfectivo.contiene(clave)){
            tblAfectivo.actualiza(clave,valor);
            modificadoAfectivo = true;
        }

        notifyObservers();
    }

    /**
     * Metodo que indica si se ha actualizado la tabla Tecnico del modelo
     */
    public boolean actualizadoTecnico(){

        return modificadoTecnico;
    }

    /**
     * Metodo que indica si se ha actualizado la tabla Juridica del modelo
     */
    public boolean actualizadoJuridico(){

        return modificadoJuridico;
    }

    /**
     * Metodo que indica si se ha actualizado la tabla Afectiva del modelo
     */
    public boolean actualizadoAfectivo(){

        return modificadoAfectivo;
    }

    /**
     * Metodo para registrar un observador
     */
    public void attach(IZObservadorFormularios o) {
        mObserver.add(o);
    }

    /**
     * Metodo para borrar un observador
     */
    public void detach(IZObservadorFormularios o) {
        mObserver.remove(o);
    }

    /**
     * Metodo para notificar a los observadores
     */
    public void notifyObservers() {

        for(int i = 0; i<mObserver.size(); i++){
            mObserver.get(i).update();
        }

        modificadoTecnico = false;
        modificadoJuridico = false;
        modificadoAfectivo = false;
    }
}
