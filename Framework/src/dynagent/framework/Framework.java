package dynagent.framework;


/*
 * Framework.java
 *
 * Created on 12 de diciembre de 2006, 12:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author Francisco Javier Martinez Navarro
 */
/*public class Framework extends javax.swing.JApplet {
    
    /** Creates a new instance of Framework */
/*    public Framework() {
        //JApplet a=new JApplet();  
    }
    
    public void init
    
}
*/
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import dynagent.framework.gestores.GestorContenedorItems;
import dynagent.framework.gestores.GestorContenedorPaneles;
import dynagent.framework.gestores.GestorInterfaz;
import dynagent.framework.utilidades.SkinComponente;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Dimension;


/**
 * @author jag
 * @author mem
 * @author kwalrath
 * @author ir71389
 */

/*
 * TumbleItem.java requires these files:
 *   all the images in the images/tumble directory
 *     (or, if specified in the applet tag, another directory [dir]
 *     with images named T1.gif ... Tx.gif, where x is the total
 *     number of images [nimgs])
 *   the appropriate code to specify that the applet be executed,
 *     such as the HTML code in TumbleItem.html or TumbleItem.atag,
 *     or the JNLP code in TumbleItem.jnlp
 *
 */
public class Framework extends JApplet{
                        //implements ActionListener {
    /*int loopslot = -1;  //the current frame number

    String dir;         //the directory relative to the codebase
                        //from which the images are loaded

    Timer timer;
                        //the timer animating the images

    int pause;          //the length of the pause between revs

    int offset;         //how much to offset between loops
    int off;            //the current offset
    int speed;          //animation speed
    int nimgs;          //number of images to animate
    int width;          //width of the applet's content pane
    //Animator animator;  //the applet's content pane

    ImageIcon imgs[];   //the images
    int maxWidth;       //width of widest image
    JLabel statusLabel;*/

	private static final long serialVersionUID = 1L;
	GestorInterfaz gestorInterfaz;
    
    //Called by init.
    protected void loadAppletParameters() {
        //Get the applet parameters.
        /*String at = getParameter("img");
        dir = (at != null) ? at : "images/tumble";
        at = getParameter("pause");
        pause = (at != null) ? Integer.valueOf(at).intValue() : 1900;
        at = getParameter("offset");
        offset = (at != null) ? Integer.valueOf(at).intValue() : 0;
        at = getParameter("speed");
        speed = (at != null) ? (1000 / Integer.valueOf(at).intValue()) : 100;
        at = getParameter("nimgs");
        nimgs = (at != null) ? Integer.valueOf(at).intValue() : 16;
        at = getParameter("maxwidth");
        maxWidth = (at != null) ? Integer.valueOf(at).intValue() : 0;*/
    }

    /**
     * Create the GUI. For thread safety, this method should
     * be invoked from the event-dispatching thread.
     */
    private void createGUI() {
        //Animate from right to left if offset is negative.
        /*width = getSize().width;
        if (offset < 0) {
            off = width - maxWidth;
        }*/

        //Custom component to draw the current image
        //at a particular offset.
        /*animator = new Animator();
        animator.setOpaque(true);
        animator.setBackground(Color.white);
        setContentPane(animator);
                
        //Put a "Loading Images..." label in the middle of
        //the content pane.  To center the label's text in
        //the applet, put it in the center part of a
        //BorderLayout-controlled container, and center-align
        //the label's text.
        statusLabel = new JLabel("Loading Images...",
                                 JLabel.CENTER);
        animator.add(statusLabel, BorderLayout.CENTER);*/
        
        gestorInterfaz=new GestorInterfaz(null);
        this.setContentPane(gestorInterfaz.getComponente());
        SkinComponente skinItemsModulos=new SkinComponente();
/*        skinItemsModulos.putColorFondo(Skin.COMPONENTE, java.awt.Color.PINK.darker());
        //skin.putColorFondo(Skin.COMPONENTE, new java.awt.Color(184,207,229));
        skinItemsModulos.putColorTexto(Skin.COMPONENTE, java.awt.Color.WHITE);
        skinItemsModulos.putColorFondo(Skin.COMPONENTE_PADRE, java.awt.Color.WHITE);
        skinItemsModulos.putColorFondo(Skin.COMPONENTE_ABUELO, java.awt.Color.PINK.darker());
    	//skin.putColorFondo(Skin.COMPONENTE_ABUELO, java.awt.Color.WHITE);
        skinItemsModulos.putFuente(Skin.COMPONENTE, new java.awt.Font("Verdana",java.awt.Font.BOLD,12));
*/    	/*skin.putColor(Skin.COLOR_FONDO, Skin.COMPONENTE_PADRE, java.awt.Color.YELLOW);
    	skin.putColor(Skin.COLOR_FONDO, Skin.COMPONENTE_PADRE, java.awt.Color.YELLOW);*/
    	
        /*if(gestorInterfaz.crearComponente())
            this.setContentPane(gestorInterfaz.getComponente());*/
        GestorContenedorItems gestorItemsModulos=(GestorContenedorItems)gestorInterfaz.getZona(GestorInterfaz.ZONA_TRABAJO);
        GestorContenedorPaneles gestorPanelesModulos=(GestorContenedorPaneles)gestorInterfaz.getZona(GestorInterfaz.ZONA_TRABAJO);
        
        String identificador="A001";
        String nombre=null;
        String textoUso="Mostrar herramientas clientes";
        String rutaImagen="imagenes/Icono24.gif";
        //Dimension sizeImagen=skinsZonaModulos.getSizeIconoBoton();
        Dimension sizeImagen=null;
        gestorItemsModulos.addItem(identificador,nombre,textoUso,dynagent.framework.utilidades.CreadorIconos.crearIcono(rutaImagen,sizeImagen),null,null);
        gestorItemsModulos.setEventoItem(identificador,"3",new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                clicEnModulo(e); }
        });
        
        identificador="A002";
        nombre=null;
        textoUso="Mostrar herramientas pedidos";
        rutaImagen="imagenes/Icono23.gif";
        //sizeImagen=skinsZonaModulos.getSizeIconoBoton();
        sizeImagen=null;
        gestorItemsModulos.addItem(identificador,nombre,textoUso,dynagent.framework.utilidades.CreadorIconos.crearIcono(rutaImagen,sizeImagen),null,null);
        gestorItemsModulos.setEventoItem(identificador,"2",new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                clicEnModulo(e); }
        });
        gestorItemsModulos.setVisibleItems(null,true);
        
        gestorItemsModulos.setSkinItems(null,skinItemsModulos);
        identificador="monitor";
        JPanel panelMonitor=new JPanel();
        panelMonitor.setBackground(java.awt.Color.GREEN.darker());
        int posicion=-1;
        Dimension sizeMinimo=new Dimension(32,32);
        Dimension sizePreferido=new Dimension(45,45);
        panelMonitor.setPreferredSize(sizePreferido);
        gestorPanelesModulos.addPanel(identificador,panelMonitor,posicion,sizeMinimo,null);
        gestorPanelesModulos.setVisiblePanels(null,true);
        
        /*gestorInterfaz.addZonaModulos(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                clicEnModulo(e); }
        });*/
        
        SkinComponente skinItemsMenu=new SkinComponente();
  /*      skinItemsMenu.putColorFondo(Skin.COMPONENTE, java.awt.Color.PINK.darker());
        //skin.putColorFondo(Skin.COMPONENTE, new java.awt.Color(184,207,229));
        skinItemsMenu.putColorTexto(Skin.COMPONENTE, java.awt.Color.WHITE);
        skinItemsMenu.putColorFondo(Skin.COMPONENTE_PADRE, java.awt.Color.WHITE);
        skinItemsMenu.putColorFondo(Skin.COMPONENTE_ABUELO, java.awt.Color.PINK.darker());
    	//skin.putColorFondo(Skin.COMPONENTE_ABUELO, java.awt.Color.WHITE);
        skinItemsMenu.putFuente(Skin.COMPONENTE, new java.awt.Font("Verdana",java.awt.Font.BOLD,12));
 */   	/*skin.putColor(Skin.COLOR_FONDO, Skin.COMPONENTE_PADRE, java.awt.Color.YELLOW);
    	skin.putColor(Skin.COLOR_FONDO, Skin.COMPONENTE_PADRE, java.awt.Color.YELLOW);*/
        
        String identificadorConjuntoMenu="01";
        GestorContenedorItems gestorItemsMenu=(GestorContenedorItems)gestorInterfaz.getZona(GestorInterfaz.ZONA_MENU);
        identificador="A001";
        nombre="Operaciones";
        textoUso="Mostrar herramientas";
        rutaImagen="imagenes/Icono24.gif";
        //sizeImagen=skinsZonaMenu.getSizeIconoBoton();
        gestorItemsMenu.addItem(identificador,nombre,textoUso,null,null,identificadorConjuntoMenu);
        //addItem(identificador,nombre,textoUso,dynagent.utilidades.CreadorIconos.crearIcono(rutaImagen,sizeImagen),null);

        String identificadorPadre=new String(identificador);
        identificador="B001";
        nombre="Clientesasasasasas";
        textoUso="Mostrar herramientas clientes";
        rutaImagen="imagenes/Icono24.gif";
        //sizeImagen=skinsZonaMenu.getSizeIconoBoton();
        gestorItemsMenu.addItem(identificador,nombre,textoUso,dynagent.framework.utilidades.CreadorIconos.crearIcono(rutaImagen,sizeImagen),identificadorPadre,identificadorConjuntoMenu);
                
        identificador="B002";
        nombre="Pedidos";
        textoUso="Mostrar herramientas pedidos";
        rutaImagen="imagenes/Icono23.gif";
        //sizeImagen=skinsZonaMenu.getSizeIconoBoton();
        gestorItemsMenu.addItem(identificador,nombre,textoUso,dynagent.framework.utilidades.CreadorIconos.crearIcono(rutaImagen,sizeImagen),identificadorPadre,identificadorConjuntoMenu);
        
        identificador="A002";
        nombre="Novedades";
        textoUso="Mostrar otras herramientas";
        rutaImagen="imagenes/Icono24.gif";
        //sizeImagen=skinsZonaMenu.getSizeIconoBoton();
        gestorItemsMenu.addItem(identificador,nombre,textoUso,null,null,identificadorConjuntoMenu);
        
        identificadorPadre=new String(identificador);
        identificador="B003";
        nombre="Informes";
        textoUso="Mostrar informes";
        rutaImagen="imagenes/Icono24.gif";
        //sizeImagen=skinsZonaMenu.getSizeIconoBoton();
        gestorItemsMenu.addItem(identificador,nombre,textoUso,dynagent.framework.utilidades.CreadorIconos.crearIcono(rutaImagen,sizeImagen),identificadorPadre,identificadorConjuntoMenu);
        gestorItemsMenu.setEventoItems(identificadorConjuntoMenu,"1",new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                clicEnItems(e); }
        });
        //gestorItemsMenu.setSkinItem("A001",skin);
        gestorItemsMenu.setSkinItems("01",skinItemsMenu);
        gestorItemsMenu.setVisibleItems("01",true);
        //gestorItemsMenu.setSkinItems("01",null);
        
        GestorContenedorPaneles gestorPanelesTrabajo=(GestorContenedorPaneles)gestorInterfaz.getZona(GestorInterfaz.ZONA_TRABAJO);
        JPanel panel1=new JPanel();
        JPanel panel2=new JPanel();
        JPanel panel3=new JPanel();
        panel1.setBackground(java.awt.Color.PINK);
        panel2.setBackground(java.awt.Color.PINK);
        panel3.setBackground(java.awt.Color.PINK);
        String conjunto="A";
        //panel1.setPreferredSize(new Dimension(250,250));
        //panel2.setPreferredSize(new Dimension(200,200));
        SkinComponente skinPanelesMenu=new SkinComponente();
        gestorPanelesTrabajo.addPanel("P001",panel1,0,new Dimension(100,150),conjunto);
        gestorPanelesTrabajo.addPanel("P002",panel2,1,new Dimension(100,100),conjunto);
        gestorPanelesTrabajo.addPanel("P003",panel3,2,null,conjunto);
        
        gestorPanelesTrabajo.setSkinPanels(conjunto,skinPanelesMenu);
        gestorPanelesTrabajo.setVisiblePanels(conjunto,true);
        
        //gestorItemsMenu.addItem(identificador,nombre,textoUso,dynagent.utilidades.CreadorIconos.crearIcono(rutaImagen,sizeImagen),identificadorPadre,identificadorConjuntoMenu);
        
        gestorItemsMenu.setVisibleItems("01",true);
        /*gestorInterfaz.addZonaMenu(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                clicEnItems(e); }
        });*/
        
        /*gestorInterfaz.addZonaTrabajo((java.awt.event.ActionListener)null);
        */
        
        
        //gestorItemsMenu.removeItem("B003");
        //gestorItemsModulos.removeItem("A001");
        
        
        //gestorInterfaz.addZonaMenu();
        //gestorInterfaz.addZonaTrabajo();
        
    }
boolean primero=true;
boolean despues=false;
    public void clicEnItems(ActionEvent e) {
        if(primero){
            GestorContenedorPaneles gestorPanelesTrabajo=(GestorContenedorPaneles)gestorInterfaz.getZona(GestorInterfaz.ZONA_TRABAJO);
            if(!despues){
                JPanel panel1=new JPanel();
                JPanel panel2=new JPanel();
                JPanel panel3=new JPanel();
                panel1.setBackground(java.awt.Color.RED);
                panel2.setBackground(java.awt.Color.GREEN);
                panel3.setBackground(java.awt.Color.BLUE);
                String conjunto="B";
                gestorPanelesTrabajo.addPanel("P001",panel1,0,new Dimension(100,150),conjunto);
                gestorPanelesTrabajo.addPanel("P002",panel2,1,new Dimension(100,100),conjunto);
                gestorPanelesTrabajo.addPanel("P003",panel3,2,null,conjunto);
                gestorPanelesTrabajo.setVisiblePanels("B",true);
                //gestorInterfaz.addZonaTrabajo((java.awt.event.ActionListener)null);
                despues=true;
            }else{
                gestorPanelesTrabajo.setVisiblePanels("B",true);
                //gestorInterfaz.addZonaTrabajo((java.awt.event.ActionListener)null);
            }
            //despues=!despues;
        }else{
            GestorContenedorPaneles gestorPanelesTrabajo=(GestorContenedorPaneles)gestorInterfaz.getZona(GestorInterfaz.ZONA_TRABAJO);
            gestorPanelesTrabajo.setVisiblePanels("A",true);
            //gestorInterfaz.addZonaTrabajo((java.awt.event.ActionListener)null);
        }
        primero=!primero;
    }
    
    boolean primero1=true;
boolean despues1=false;
    public void clicEnModulo(ActionEvent e) {
        GestorContenedorItems gestorItemsMenu=(GestorContenedorItems)gestorInterfaz.getZona(GestorInterfaz.ZONA_MENU);
        if(primero1){
            if(!despues1){
                //gestorItemsMenu.removeItems("01");
                String identificadorConjuntoMenu="02";
                String identificador="D001";
                String nombre="Operaciones";
                String textoUso="Mostrar herramientas";
                String rutaImagen="imagenes/Icono24.gif";
                Dimension sizeImagen=null;
                gestorItemsMenu.addItem(identificador,nombre,textoUso,null,null,identificadorConjuntoMenu);
                //addItem(identificador,nombre,textoUso,dynagent.utilidades.CreadorIconos.crearIcono(rutaImagen,sizeImagen),null);

                String identificadorPadre=new String(identificador);
                identificador="E001";
                nombre="";
                textoUso="Mostrar herramientas clientes";
                rutaImagen="imagenes/Icono24.gif";
                //sizeImagen=skinsZonaMenu.getSizeIconoBoton();
                gestorItemsMenu.addItem(identificador,nombre,textoUso,dynagent.framework.utilidades.CreadorIconos.crearIcono(rutaImagen,sizeImagen),identificadorPadre,identificadorConjuntoMenu);
                gestorItemsMenu.setEventoItems(identificadorConjuntoMenu,"4",new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        clicEnItems(e); }
                });
                gestorItemsMenu.setVisibleItems("02",true);
                
                /*gestorInterfaz.addZonaMenu(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        clicEnItems(e); }
                });*/
                despues1=true;
            }else{
                gestorItemsMenu.setVisibleItems("02",true);
                /*gestorInterfaz.addZonaMenu(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        clicEnItems(e); }
                });*/
            }
            //despues1=!despues1;
        }else{
            gestorItemsMenu.setVisibleItems("01",true);
            /*gestorInterfaz.addZonaMenu(new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                        clicEnItems(e); }
                });*/
        }
        primero1=!primero1;
    }
    //Background task for loading images.
    /*SwingWorker worker = new SwingWorker<ImageIcon[], Void>() {
        @Override
        public ImageIcon[] doInBackground() {
            final ImageIcon[] innerImgs = new ImageIcon[nimgs];
            for (int i = 0; i < nimgs; i++) {
                innerImgs[i] = loadImage(i + 1);
            }
            return innerImgs;
        }

        @Override
        public void done() {
            //Remove the "Loading images" label.
            animator.removeAll();
            loopslot = -1;
            try {
                imgs = get();
            } catch (InterruptedException ignore) {}
            catch (java.util.concurrent.ExecutionException e) {
                String why = null;
                Throwable cause = e.getCause();
                if (cause != null) {
                    why = cause.getMessage();
                } else {
                    why = e.getMessage();
                }
                System.err.println("Error retrieving file: " + why);
            }
        }
    };
*/
    //Called when this applet is loaded into the browser.
    public void init() {
        loadAppletParameters();

        //Execute a job on the event-dispatching thread:
        //creating this applet's GUI.
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createGUI();
                }
            });
        } catch (Exception e) { 
            e.printStackTrace();
            System.err.println("createGUI didn't successfully complete");
        }
    }

    //The component that actually presents the GUI.
    /*public class Animator extends JPanel {
        public Animator() {
            super(new BorderLayout());
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (worker.isDone() &&
                (loopslot > -1) && (loopslot < nimgs)) {
                if (imgs != null && imgs[loopslot] != null) {
                    imgs[loopslot].paintIcon(this, g, off, 0);
                }
            }
        }
    }*/

    //Handle timer event. Update the loopslot (frame number) and the
    //offset.  If it's the last frame, restart the timer to get a long
    //pause between loops.
   /* public void actionPerformed(ActionEvent e) {
        //If still loading, can't animate.
        if (!worker.isDone()) {
            return;
        }

        loopslot++;

        if (loopslot >= nimgs) {
            loopslot = 0;
            off += offset;

            if (off < 0) {
                off = width - maxWidth;
            } else if (off + maxWidth > width) {
                off = 0;
            }
        }

        animator.repaint();

        if (loopslot == nimgs - 1) {
            timer.restart();
        }
    }
*/
    public void start() {
        /*if (worker.isDone() && (nimgs > 1)) {
            timer.restart();
        }*/
    }

    public void stop() {
        //timer.stop();
    }

    /**
     * Load the image for the specified frame of animation. Since
     * this runs as an applet, we use getResourceAsStream for 
     * efficiency and so it'll work in older versions of Java Plug-in.
     */
   /* protected ImageIcon loadImage(int imageNum) {
        String path = dir + "/T" + imageNum + ".gif";
        int MAX_IMAGE_SIZE = 2400;  //Change this to the size of
                                     //your biggest image, in bytes.
        int count = 0;
        BufferedInputStream imgStream = new BufferedInputStream(
           this.getClass().getResourceAsStream(path));
        if (imgStream != null) {
            byte buf[] = new byte[MAX_IMAGE_SIZE];
            try {
                count = imgStream.read(buf);
                imgStream.close();
            } catch (java.io.IOException ioe) {
                System.err.println("Couldn't read stream from file: " + path);
                return null;
            }
            if (count <= 0) {
                System.err.println("Empty file: " + path);
                return null;
            }
            return new ImageIcon(Toolkit.getDefaultToolkit().createImage(buf));
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }*/
}
