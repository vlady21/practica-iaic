/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * VisualizadorView.java
 *
 * Created on 12-may-2009, 20:27:05
 */

package vista;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;
import utilerias.EscribirConsejos;

/**
 * AUTORES:
 * @author Victor Adaíl Ferrer
 * @author José Miguel Guerrero Hernández
 */
public class VisualizadorView extends javax.swing.JFrame {
    private String texto;
    private String info;
    static TrayIcon trayIcon;

    /** Creates new form VisualizadorView */
    public VisualizadorView() {
        initComponents();
    }

    VisualizadorView(String texto, TrayIcon trayIcon) {

        this.info = texto;

        EscribirConsejos.escribe(texto);
        //texto = formatearInforme(texto);

        this.texto = texto;
        this.trayIcon = trayIcon;

        initComponents();

        this.setMaximumSize(new Dimension(640,480));

        this.setSize(new Dimension(640,480));

        this.setPreferredSize(new Dimension(640,480));

        this.setLocationRelativeTo(null);

        this.setMaximumSize(new Dimension(640,480));

        this.setSize(new Dimension(640,480));

        this.setPreferredSize(new Dimension(640,480));

    }

    private String formatear(String texto) {

        int lineas = 120;
        int resto;
        String aux,informe = "";

        while(texto.indexOf("  ")!=-1)
            texto = texto.replaceAll("  ", " ");

        texto = texto.replaceAll("\t", "    ");

        StringTokenizer st = new StringTokenizer(texto,"\n");
        int i;
        boolean fin;

        while(st.hasMoreTokens()){

            aux = "";
            fin=false;

            do{
                if(st.hasMoreTokens()){
                    aux= aux + st.nextToken();

                    if(aux.length()==1){

                        aux = aux + "                                                                                  ";

                        fin=true;
                    }
                    
                    if(aux.trim().endsWith(".")){

                        fin=true;
                        aux=aux+"                                                         ";
                    }
                    if(aux.trim().endsWith("*")){

                        fin=true;
                        aux=aux+"                                                         ";
                    }

                    if(aux.trim().endsWith(":")){

                        fin=true;
                        aux=aux+"                                                         ";
                    }
                       
                }else{
                    fin=true;
                }
            }while(!fin);

            resto=aux.length()%lineas;

            if(aux.length()==1){

                aux = aux + "                                                                                  ";
                 
            }

            while(lineas-resto>0){
                aux = aux + " ";
                resto++;
            }

            informe = informe + aux;
        }

        return informe;
    }

    private String formatear2(String texto) {
        int lineas = 120;
        int resto;
        String aux,informe = "";

        StringTokenizer st = new StringTokenizer(texto,"\n");
        int i;
        boolean fin;

        while(st.hasMoreTokens()){

            aux = st.nextToken();

            resto=aux.length()%lineas;

            /*if(aux.length()==1){

                aux = aux + "                                                                                  ";

            }*/

            while(lineas-resto>0){
                aux = aux + " ";
                resto++;
            }

            informe = informe + aux;

        }

        return informe;

    }

    private String formatearInforme(String texto) {

        int lineas = 120;
        int resto;
        String aux,informe = "";

        //texto = texto.replaceAll("\t", "    ");

        while(texto.indexOf("  ")!=-1)
            texto = texto.replaceAll("  ", " ");

        StringTokenizer st = new StringTokenizer(texto,"\n");
        int i;
        boolean fin;

        while(st.hasMoreTokens()){

            aux = "";
            fin=false;

            do{
                if(st.hasMoreTokens()){
                    aux= aux + st.nextToken();

                    if(aux.length()==1){

                        fin=true;
                    }

                    texto = texto.replaceAll(".", ".\n");
                    texto = texto.replaceAll(":", ":\n");

                    aux=aux.trim();

                    if(aux.trim().endsWith(".")){

                        fin=true;
                    }
                    if(aux.trim().endsWith("*")){

                        fin=true;
                    }
                    if(aux.trim().endsWith(":")){

                        fin=true;
                    }

                }else{
                    fin=true;
                }
            }while(!fin);
/*
            resto=aux.length()%lineas;

            if(aux.length()==1){

                aux = aux + "                                                                                  ";

            }

            while(lineas-resto>0){
                aux = aux + " ";
                resto++;
            }*/

            informe = informe + "\n" + aux;
        }

        return informe;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        botonGuardar = new javax.swing.JButton();
        botonCopiar = new javax.swing.JButton();
        botonImprimir = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(vista.Principal.class).getContext().getResourceMap(VisualizadorView.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setIconImage(getIconImage());
        setName("Form"); // NOI18N

        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        botonGuardar.setIcon(resourceMap.getIcon("botonGuardar.icon")); // NOI18N
        botonGuardar.setText(resourceMap.getString("botonGuardar.text")); // NOI18N
        botonGuardar.setToolTipText(resourceMap.getString("botonGuardar.toolTipText")); // NOI18N
        botonGuardar.setFocusable(false);
        botonGuardar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonGuardar.setMaximumSize(new java.awt.Dimension(57, 57));
        botonGuardar.setMinimumSize(new java.awt.Dimension(57, 57));
        botonGuardar.setName("botonGuardar"); // NOI18N
        botonGuardar.setPreferredSize(new java.awt.Dimension(57, 57));
        botonGuardar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonGuardar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                guardar(evt);
            }
        });
        jToolBar1.add(botonGuardar);

        botonCopiar.setIcon(resourceMap.getIcon("botonCopiar.icon")); // NOI18N
        botonCopiar.setText(resourceMap.getString("botonCopiar.text")); // NOI18N
        botonCopiar.setToolTipText(resourceMap.getString("botonCopiar.toolTipText")); // NOI18N
        botonCopiar.setFocusable(false);
        botonCopiar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonCopiar.setMaximumSize(new java.awt.Dimension(57, 57));
        botonCopiar.setMinimumSize(new java.awt.Dimension(57, 57));
        botonCopiar.setName("botonCopiar"); // NOI18N
        botonCopiar.setPreferredSize(new java.awt.Dimension(57, 57));
        botonCopiar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonCopiar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                copiar(evt);
            }
        });
        jToolBar1.add(botonCopiar);

        botonImprimir.setIcon(resourceMap.getIcon("botonImprimir.icon")); // NOI18N
        botonImprimir.setText(resourceMap.getString("botonImprimir.text")); // NOI18N
        botonImprimir.setToolTipText(resourceMap.getString("botonImprimir.toolTipText")); // NOI18N
        botonImprimir.setFocusable(false);
        botonImprimir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonImprimir.setMaximumSize(new java.awt.Dimension(57, 57));
        botonImprimir.setMinimumSize(new java.awt.Dimension(57, 57));
        botonImprimir.setName("botonImprimir"); // NOI18N
        botonImprimir.setPreferredSize(new java.awt.Dimension(57, 57));
        botonImprimir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonImprimir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                imprimir(evt);
            }
        });
        jToolBar1.add(botonImprimir);

        getContentPane().add(jToolBar1, java.awt.BorderLayout.NORTH);

        jScrollPane1.setMaximumSize(new java.awt.Dimension(800, 600));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextPane1.setEditable(false);
        jTextPane1.setMaximumSize(new java.awt.Dimension(800, 600));
        jTextPane1.setName("jTextPane1"); // NOI18N
        jTextPane1.setText(texto);
        jScrollPane1.setViewportView(jTextPane1);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void guardar(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_guardar

        File archivo=new File("");
        String arch = null;
        JFileChooser chooser = new JFileChooser(archivo.getAbsolutePath());
        chooser.setCurrentDirectory(archivo);

        //muestra la ventana de dialogo y asigna el valor del boton pulsado.
        int returnVal = chooser.showSaveDialog(chooser);

        //si se pulsa el boton guardar.
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            archivo=chooser.getSelectedFile();
            if(!archivo.getName().endsWith(".txt"))
             arch = archivo.getAbsolutePath() + ".txt";
            else
             arch = archivo.getAbsolutePath();

            FileWriter fichero = null;
            PrintWriter pw = null;
            try
            {
                fichero = new FileWriter(arch);
                pw = new PrintWriter(fichero);

                pw.println(texto.replaceAll("\n", System.getProperty("line.separator")));

                trayIcon.displayMessage("Informe guardado", "Informe guardado correctamente en " + arch, TrayIcon.MessageType.INFO);

            } catch (Exception e) {

                trayIcon.displayMessage("Informe no guardado", "Error al guardar el informe en " + arch, TrayIcon.MessageType.ERROR);

            } finally {
               // Nuevamente aprovechamos el finally para
               // asegurarnos que se cierra el fichero.
               if (null != fichero)

               try {
                    fichero.close();
               } catch (Exception e2) {

                    trayIcon.displayMessage("Error fichero", "Error al cerrar el archivo" + arch, TrayIcon.MessageType.ERROR);
               }
            }
        }
        
    }//GEN-LAST:event_guardar

    private void copiar(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_copiar

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        StringSelection stringSelection = new StringSelection(texto);

        clipboard.setContents(stringSelection, null);

        trayIcon.displayMessage("Informe copiado", "Informe copiado al portapapeles", TrayIcon.MessageType.INFO);


    }//GEN-LAST:event_copiar

    private void imprimir(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imprimir
        try {
            jTextPane1.print(new MessageFormat("Consejos del Asesor Laboral"), new MessageFormat("Página - {0}"));
        } catch (Exception ex) {
            trayIcon.displayMessage("Error impresion", "Error al imprimir el informe " + ex.getMessage(), TrayIcon.MessageType.ERROR);
        }

    }//GEN-LAST:event_imprimir

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VisualizadorView().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonCopiar;
    private javax.swing.JButton botonGuardar;
    private javax.swing.JButton botonImprimir;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables

}
