/*
 * PrincipalView.java
 */

package vista;

import conocimiento.LanzadorJess;
import java.awt.Component;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import jess.JessException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * The application's main frame.
 */
public class PrincipalView extends FrameView {

    static Image imagenTray;
    static TrayIcon trayIcon;
    SystemTray tray;
    PrincipalView interfaz;
    private boolean noFinalizar;
    private Vector respuestasTablaTecnica;
    //private Vector respuestasTablaJuridica;
    //private Vector respuestasTablaAfectivo;
    private Vector clavesTablaTecnica;
    //private Vector clavesTablaJuridica;
    //private Vector clavesTablaAfectivo;
    private Properties propTecnico;
    //private Properties propJuridico;
    //private Properties propAfectivo;
    private LanzadorJess lanzadorJess;

    public PrincipalView(SingleFrameApplication app) {
        super(app);

        cargarPropiedades();

        cargarFormularios();

        cargarJess();

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();

        imagenTray = resourceMap.getImageIcon("Aplicacion.icon").getImage();
        trayIcon = new TrayIcon(imagenTray, "Asesor Laboral");

        if (SystemTray.isSupported()) {
            tray = SystemTray.getSystemTray();

            setPropiedadesTrayIcon(imagenTray, "Asesor Laboral");

        }

        getFrame().setIconImage(resourceMap.getImageIcon("Aplicacion.icon").getImage());

        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        interfaz = this;

        mensaje("Bienvenido","Bienvenido al asesor laboral.");

        status("Bienvenido al asesor laboral.");

    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = Principal.getApplication().getMainFrame();
            aboutBox = new PrincipalAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        Principal.getApplication().show(aboutBox);
    }

    private void cargarJess() {
        try {
            lanzadorJess = new LanzadorJess("log_grupoB09.txt", "reglasB09.clp");
            lanzadorJess.arrancarJess();
        } catch (JessException ex) {
            menError("Error Jess", "Error al lanzar el modulo de JESS");
        }

    }

    private void cargarPropiedades() {

        propTecnico = getPropiedades("config/formulario.properties");
        //propJuridico = getPropiedades("config/formularioJuridico.properties");
        //propAfectivo = getPropiedades("config/formularioAfectivo.properties");
    }

    private void cargarFormularios() {

        String [] respuestas = null;

        StringTokenizer st;

        respuestasTablaTecnica = new Vector();
        //respuestasTablaJuridica = new Vector();
        //respuestasTablaAfectivo = new Vector();

        clavesTablaTecnica = new Vector();
        //clavesTablaJuridica = new Vector();
        //clavesTablaAfectivo = new Vector();

        int j;
        int tam = propTecnico.size()/3;

        for(int i = 1; i<=tam ; i++){

            st = new StringTokenizer(propTecnico.getProperty("respuesta"+i),",");

            respuestas = new String[st.countTokens()];

            j = 0;

            while(st.hasMoreTokens()){

                respuestas[j] = st.nextToken();

                j++;
            }

            respuestasTablaTecnica.add(respuestas);
    
            clavesTablaTecnica.add(propTecnico.getProperty("hecho"+i));
        }
/*
        tam = propJuridico.size()/3;

        for(int i = 1; i<=tam ; i++){

            st = new StringTokenizer(propJuridico.getProperty("respuesta"+i),",");

            respuestas = new String[st.countTokens()];

            j = 0;

            while(st.hasMoreTokens()){

                respuestas[j] = st.nextToken();

                j++;
            }

            respuestasTablaJuridica.add(respuestas);

            clavesTablaJuridica.add(propJuridico.getProperty("hecho"+i));
        }

        tam = propAfectivo.size()/3;

        for(int i = 1; i<=tam ; i++){

            st = new StringTokenizer(propAfectivo.getProperty("respuesta"+i),",");

            respuestas = new String[st.countTokens()];

            j = 0;

            while(st.hasMoreTokens()){

                respuestas[j] = st.nextToken();

                j++;
            }

            respuestasTablaAfectivo.add(respuestas);

            clavesTablaAfectivo.add(propAfectivo.getProperty("hecho"+i));
        }*/
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        formularioTecnico = new javax.swing.JPanel();
        scrollPanelTenico = new javax.swing.JScrollPane();
        tablaTecnica = new javax.swing.JTable();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        toolBar = new javax.swing.JToolBar();
        botonAsesorar = new javax.swing.JButton();
        botonReiniciar = new javax.swing.JButton();
        botonSalir = new javax.swing.JButton();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new javax.swing.BoxLayout(mainPanel, javax.swing.BoxLayout.LINE_AXIS));

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(vista.Principal.class).getContext().getResourceMap(PrincipalView.class);
        formularioTecnico.setToolTipText(resourceMap.getString("formularioTecnico.toolTipText")); // NOI18N
        formularioTecnico.setName("formularioTecnico"); // NOI18N

        scrollPanelTenico.setName("scrollPanelTenico"); // NOI18N

        tablaTecnica.setModel(getModeloTabla("Técnico", propTecnico));
        tablaTecnica.setName("tablaTecnica"); // NOI18N
        tablaTecnica.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                tablaTecnicaMouseMoved(evt);
            }
        });
        scrollPanelTenico.setViewportView(tablaTecnica);

        javax.swing.GroupLayout formularioTecnicoLayout = new javax.swing.GroupLayout(formularioTecnico);
        formularioTecnico.setLayout(formularioTecnicoLayout);
        formularioTecnicoLayout.setHorizontalGroup(
            formularioTecnicoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formularioTecnicoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPanelTenico, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                .addContainerGap())
        );
        formularioTecnicoLayout.setVerticalGroup(
            formularioTecnicoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formularioTecnicoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPanelTenico, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                .addGap(5, 5, 5))
        );

        mainPanel.add(formularioTecnico);

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(vista.Principal.class).getContext().getActionMap(PrincipalView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
        exitMenuItem.setToolTipText(resourceMap.getString("exitMenuItem.toolTipText")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setToolTipText(resourceMap.getString("aboutMenuItem.toolTipText")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 334, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        toolBar.setRollover(true);
        toolBar.setName("toolBar"); // NOI18N

        botonAsesorar.setIcon(resourceMap.getIcon("botonAsesorar.icon")); // NOI18N
        botonAsesorar.setText(resourceMap.getString("botonAsesorar.text")); // NOI18N
        botonAsesorar.setToolTipText(resourceMap.getString("botonAsesorar.toolTipText")); // NOI18N
        botonAsesorar.setFocusable(false);
        botonAsesorar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonAsesorar.setMargin(new java.awt.Insets(0, 0, 0, 0));
        botonAsesorar.setMaximumSize(new java.awt.Dimension(60, 60));
        botonAsesorar.setMinimumSize(new java.awt.Dimension(60, 60));
        botonAsesorar.setName("botonAsesorar"); // NOI18N
        botonAsesorar.setPreferredSize(new java.awt.Dimension(60, 60));
        botonAsesorar.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        botonAsesorar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonAsesorar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                botonAsesorarPresionado(evt);
            }
        });
        toolBar.add(botonAsesorar);

        botonReiniciar.setIcon(resourceMap.getIcon("botonReiniciar.icon")); // NOI18N
        botonReiniciar.setText(resourceMap.getString("botonReiniciar.text")); // NOI18N
        botonReiniciar.setToolTipText(resourceMap.getString("botonReiniciar.toolTipText")); // NOI18N
        botonReiniciar.setFocusable(false);
        botonReiniciar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonReiniciar.setMargin(new java.awt.Insets(0, 0, 0, 0));
        botonReiniciar.setMaximumSize(new java.awt.Dimension(60, 60));
        botonReiniciar.setMinimumSize(new java.awt.Dimension(60, 60));
        botonReiniciar.setName("botonReiniciar"); // NOI18N
        botonReiniciar.setPreferredSize(new java.awt.Dimension(60, 60));
        botonReiniciar.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        botonReiniciar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        botonReiniciar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                botonReiniciarMousePressed(evt);
            }
        });
        toolBar.add(botonReiniciar);

        botonSalir.setAction(actionMap.get("quit")); // NOI18N
        botonSalir.setIcon(resourceMap.getIcon("botonSalir.icon")); // NOI18N
        botonSalir.setText(resourceMap.getString("botonSalir.text")); // NOI18N
        botonSalir.setToolTipText(resourceMap.getString("botonSalir.toolTipText")); // NOI18N
        botonSalir.setFocusable(false);
        botonSalir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonSalir.setMargin(new java.awt.Insets(0, 0, 0, 0));
        botonSalir.setMaximumSize(new java.awt.Dimension(60, 60));
        botonSalir.setMinimumSize(new java.awt.Dimension(60, 60));
        botonSalir.setName("botonSalir"); // NOI18N
        botonSalir.setPreferredSize(new java.awt.Dimension(60, 60));
        botonSalir.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        botonSalir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(botonSalir);

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
        setToolBar(toolBar);
    }// </editor-fold>//GEN-END:initComponents

    private void botonAsesorarPresionado(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonAsesorarPresionado

        asesorar();
    }//GEN-LAST:event_botonAsesorarPresionado

    private void tablaTecnicaMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaTecnicaMouseMoved

        int i = tablaTecnica.rowAtPoint(evt.getPoint());

        String[] values = (String[]) respuestasTablaTecnica.get(i);
        // These are the combobox values
        //String[] values = new String[]{"item1", "item2", "item3"};

        int vColIndex = 1;
        TableColumn col = tablaTecnica.getColumnModel().getColumn(vColIndex);
        col.setCellEditor(new MyComboBoxEditor(values));

    }//GEN-LAST:event_tablaTecnicaMouseMoved

private void botonReiniciarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonReiniciarMousePressed

    reiniciar();
    
}//GEN-LAST:event_botonReiniciarMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAsesorar;
    private javax.swing.JButton botonReiniciar;
    private javax.swing.JButton botonSalir;
    private javax.swing.JPanel formularioTecnico;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JScrollPane scrollPanelTenico;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTable tablaTecnica;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;

    public void reiniciar() {
        
        int tam = tablaTecnica.getRowCount();
        
        System.out.println(tam);
        
        for(int i = 0; i<tam; i++){

            tablaTecnica.setValueAt(null, i, 1);
        }    
    }
    
    public void mensaje(String titulo, String mensaje) {
        trayIcon.displayMessage(titulo, mensaje, TrayIcon.MessageType.NONE);

    }

    public void menInfo(String titulo, String mensaje) {
        trayIcon.displayMessage(titulo, mensaje, TrayIcon.MessageType.INFO);

    }

    public void menAdvertencia(String titulo, String mensaje) {
        trayIcon.displayMessage(titulo, mensaje, TrayIcon.MessageType.WARNING);

    }

    public void menError(String titulo, String mensaje) {
        trayIcon.displayMessage(titulo, mensaje, TrayIcon.MessageType.ERROR);

    }

    public void salir() {
        System.exit(0);
    }

    private void setPropiedadesTrayIcon(Image imagen, String texto) {

        PopupMenu menu = new PopupMenu();

        MenuItem reiniciarItem = new MenuItem("Reiniciar");
        reiniciarItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            reiniciar();
          }
        });
        menu.add(reiniciarItem);

        MenuItem asesorarItem = new MenuItem("Obtener asesoramiento");
        asesorarItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            asesorar();
          }
        });
        menu.add(asesorarItem);

        MenuItem salirItem = new MenuItem("Salir");
        salirItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            salir();
          }
        });
        menu.add(salirItem);

        trayIcon = new TrayIcon(imagen, texto, menu);

        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(interfaz.getFrame().isVisible()){
                    interfaz.getFrame().setVisible(false);
                }
                else{
                    interfaz.getFrame().setVisible(true);
                }
            }
        }
        );

        try {
            tray.add(trayIcon);

        } catch (Exception e) {
            System.err.println("TrayIcon could not be added.");
        }
    }
    
    public void status(String string) {
        statusMessageLabel.setText(string);
    }

    public void animeStatus(String string) {
        statusAnimationLabel.setText(string);
    }

    private void cargarFormulario(JTable tabla,Vector claves) throws Exception {

        String clave,valor;
        
        for(int i = 0; i<claves.size(); i++){

            clave = (String) claves.get(i);
            valor = (String) tabla.getValueAt(i, 1);

            if(valor!=null){
                
                lanzadorJess.insertaSlotValue(clave, valor);
            }

            System.out.println("clave: " + clave);

            System.out.println("valor: " + valor);

        }
        
    }

    private boolean comprobarFormulario(JTable tabla) throws Exception {

        boolean result = true;
        String valor;
        
        int tam = tabla.getRowCount();
        
        System.out.println(tam);
        
        for(int i = 0; i<tam&&result; i++){

            valor = (String) tabla.getValueAt(i, 1);

            if(valor==null){
                result = false;
            }
        }        
        
        return result;
    }
    
    public void asesorar() {
        
        animeStatus("Analizando informacion del asesorado");
        mensaje("Analizando informacion","Analizando informacion del asesorado.");

        statusAnimationLabel.setIcon(busyIcons[0]);
        busyIconIndex = 0;
        busyIconTimer.start();
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        noFinalizar = true;

        BarraStatus hiloStatus = new BarraStatus();
        hiloStatus.start();

        Asesor hiloAsesor = new Asesor();
        hiloAsesor.start();

    }

    public class Asesor extends Thread
    {

        @Override
       public void run()
       {

            try{
                
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(comprobarFormulario(tablaTecnica))
                {
                    cargarFormulario(tablaTecnica,clavesTablaTecnica);
                    
                    status("Informacion del asesorado analizada.");
                    mensaje("Informacion analizada","Informacion del asesorado analizada.");

                    InformeView informe = new InformeView(lanzadorJess);
                    informe.setVisible(true);
                    
                }else{
                    
                    status("Se han detectado preguntas sin respuesta.");
                    menError("Respuestas incompletas","Se han detectado preguntas sin respuesta.");

                }
                //cargarFormulario(tablaJuridica,clavesTablaJuridica);
                //cargarFormulario(tablaAfectiva,clavesTablaAfectivo);

                
            }catch(Exception ex){
                menError("Error respuestas", "Error al analizar respuestas: " + ex.getMessage());
                ex.printStackTrace();
            }

            noFinalizar = false;
       }
    }

    public class BarraStatus extends Thread
    {

       public void run()
       {
           noFinalizar = true;

           while(noFinalizar){

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            busyIconTimer.stop();
            statusAnimationLabel.setIcon(idleIcon);
            statusAnimationLabel.setText("");
            progressBar.setVisible(false);
            progressBar.setValue(0);

       }
    }

    public class MyComboBoxEditor extends DefaultCellEditor {
        public MyComboBoxEditor(String[] items) {
            super(new JComboBox(items));
        }
    }

    /**
     * Recupera el valor del fichero properties.
     * @param String Fichero properties al que se desea acceder
     * @return Properties
     */
    public static Properties getPropiedades(String ficheroProperties)
    {
      Properties propiedades = new Properties();

      try {
          FileInputStream fichero = new FileInputStream(ficheroProperties);
          propiedades.load(fichero);
          fichero.close();
      } catch (Exception e) {
          System.out.println("Error : " + e.toString());
      }
      return propiedades;
    } // Fin getPropiedades()

    public DefaultTableModel getModeloTabla(String tabla, Properties prop){

        Vector data = new Vector();
        Vector titulos = new Vector();
        Vector aux;

        int tam = prop.size()/3;

        for(int i = 1; i<=tam; i++){

            aux = new Vector();
            aux.add(prop.getProperty("pregunta" + i));
            aux.add(null);
            data.add(aux);
        }

        titulos.add("Preguntas al asesorado");
        titulos.add("Respuestas");

        return new DefaultTableModel(data,titulos) {
            Class[] types = new Class [] {
                java.lang.String.class, javax.swing.JComboBox.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        };
    }
}
