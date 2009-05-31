/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package controlador;

import conocimiento.ControladorConocimiento;
import modelo.ModeloFormularios;

/**
 * AUTORES:
 * @author Victor Adaíl Ferrer
 * @author José Miguel Guerrero Hernández
 */
public class ControladorGUI {

    private ModeloFormularios modelo;

    //Asesoramiento tecnico
	public final static int TECNICO = 0;

    //Asesoramiento tecnico
	public final static int JURIDICO = 1;

    //Asesoramiento tecnico
	public final static int AFECTIVO = 2;

    public String asesorar(int asesoramiento) throws Exception {

        String consejo = "";

        ControladorConocimiento conocimiento = new ControladorConocimiento(modelo);

        switch(asesoramiento){
            case TECNICO:
                consejo = conocimiento.AsesoramientoTecnico();
                break;
            case JURIDICO:
                consejo = conocimiento.AsesoramientoJuridico();
                break;
            case AFECTIVO:
                consejo = conocimiento.AsesoramientoAfectivo();
                break;
        }

        return consejo;
    }

    public void setModelo(ModeloFormularios modelo) {
        this.modelo = modelo;
    }

}
