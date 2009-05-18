/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modelo;

import java.util.ArrayList;
import utilerias.Constantes;
import utilerias.Propiedades;

/**
 *
 * @author Victor
 */
public class ModeloFormularios implements IZModeloFormularios {

    public ArrayList<IZObservadorFormularios> mObserver;

    private TablaFormulario tblTecnico;
    private TablaFormulario tblJuridico;
    private TablaFormulario tblAfectivo;

    private boolean modificadoTecnico;
    private boolean modificadoJuridico;
    private boolean modificadoAfectivo;

    public ModeloFormularios(){

        mObserver = new ArrayList<IZObservadorFormularios>();
        modificadoTecnico = false;
        modificadoJuridico = false;
        modificadoAfectivo = false;

        tblTecnico = new TablaFormulario(Propiedades.getPropiedades(Constantes.FORMULARIO_TECNICO));
        tblJuridico = new TablaFormulario(Propiedades.getPropiedades(Constantes.FORMULARIO_JURIDICO));
        tblAfectivo = new TablaFormulario(Propiedades.getPropiedades(Constantes.FORMULARIO_AFECTIVO));
    }

    public TablaFormulario getTecnico () {
        return tblTecnico;
    }

    public TablaFormulario getJuridico () {
        return tblJuridico;
    }

    public TablaFormulario getAfectivo () {
        return tblAfectivo;
    }

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

    public boolean actualizadoTecnico(){

        return modificadoTecnico;
    }

    public boolean actualizadoJuridico(){

        return modificadoJuridico;
    }
    public boolean actualizadoAfectivo(){

        return modificadoAfectivo;
    }

    public void attach(IZObservadorFormularios o) {
        mObserver.add(o);
    }

    public void detach(IZObservadorFormularios o) {
        mObserver.remove(o);
    }

    public void notifyObservers() {

        for(int i = 0; i<mObserver.size(); i++){
            mObserver.get(i).update();
        }

        modificadoTecnico = false;
    }
}
