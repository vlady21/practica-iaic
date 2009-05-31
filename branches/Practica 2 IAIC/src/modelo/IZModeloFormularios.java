/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package modelo;

/**
 * AUTORES:
 * @author Victor Adaíl Ferrer
 * @author José Miguel Guerrero Hernández
 */
public interface IZModeloFormularios {

    public void attach (IZObservadorFormularios o);

    public void detach (IZObservadorFormularios o);

    public void notifyObservers ();
}
