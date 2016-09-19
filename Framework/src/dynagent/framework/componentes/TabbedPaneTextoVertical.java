package dynagent.framework.componentes;

import java.awt.Dimension;
import java.awt.Insets;


import javax.swing.BorderFactory;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import java.awt.Component;

import dynagent.framework.ConstantesGraficas;
import dynagent.framework.utilidades.*;

/**
 * Clase que hereda de JTabbedPane y permite que las pesta�as muestren en forma
 * vertical el texto de su nombre
 * @author Francisco Javier Martinez Navarro
 */
public class TabbedPaneTextoVertical extends javax.swing.JTabbedPane {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
     * Indica el tipo de orientacion del texto de la pesta�a
     */
    private int orientacionTextoTab;
    /**
     * Indica el tama�o del icono de la pesta�a
     */
    private Dimension sizeIconoTab;
    /**
     * Indica la posicion del icono de la pesta�a en relacion con el texto
     */
    private int posicionIconoTab;
    
    /**
     * Almacena los componentes hijos una vez replegada
     */
    private Component[] componentesHijo;
    
    /**
     * Indica si el componente esta o no replegado
     */
    private boolean replegada;
/**
     * Constructor por defecto en el que se establece la orientacion VTextIcon.LEFT
     * como predefinida
     */
    public  TabbedPaneTextoVertical() {        
        this(VTextIcon.ROTATE_LEFT);
    }

    /**
     * Constructor en el que se establece la orientacion que se le pasa por parametro
     * como predefinida
     * @param orientacionTextoTab Orientacion que tendra el texto de la pesta�a.
     * Sus valores pueden ser:
     * 		VTextIcon.ROTATE_DEFAULT
     * 		VTextIcon.ROTATE_NONE
     * 		VTextIcon.ROTATE_LEFT
     * 		VTextIcon.ROTATE_RIGHT
     */
    public  TabbedPaneTextoVertical(int orientacionTextoTab) {        
        this(orientacionTextoTab,null,SwingConstants.TOP);
    }
    
    /**
     * Constructor en el que se establece la orientacion, dimension del icono y posicion
     * de este con respecto al texto
     * @param orientacionTextoTab Orientacion que tendra el texto de la pesta�a.
     * Sus valores pueden ser:
     * 		VTextIcon.ROTATE_DEFAULT
     * 		VTextIcon.ROTATE_NONE
     * 		VTextIcon.ROTATE_LEFT
     * 		VTextIcon.ROTATE_RIGHT
     * @param sizeIconoTab Dimension del icono de la pesta�a
     * @param posicionIconoTab Posicion relativa del icono de la pesta�a respecto al texto.
     * Sus valores pueden ser:
     * 		SwingConstants.LEFT
     *    	SwingConstants.RIGHT
     *    	SwingConstants.TOP
     *    	SwingConstants.BOTTOM
     */
    public  TabbedPaneTextoVertical(int orientacionTextoTab,Dimension sizeIconoTab,int posicionIconoTab) {        
        this.orientacionTextoTab=orientacionTextoTab;
        this.sizeIconoTab=sizeIconoTab;
        this.posicionIconoTab=posicionIconoTab;
        componentesHijo=null;
        replegada=false;
    } 
    
    /**
     * Obtener la orientacion definida para las pesta�as que se a�adan al componente
     * @return Atributo orientacionTextoTab
     */
    public int getOrientacionTextoTab(){
        return this.orientacionTextoTab;
    }
    
    /**
     * Establecer la orientacion para las pesta�as que se a�adan al componente
     * @param orientacionTextoTab Orientacion que tendra el texto de la pesta�a.
     * Sus valores pueden ser:
     * 	VTextIcon.ROTATE_DEFAULT
     * 	VTextIcon.ROTATE_NONE
     * 	VTextIcon.ROTATE_LEFT
     * 	VTextIcon.ROTATE_RIGHT
     */
    public void setOrientacionTextoTab(int orientacionTextoTab){
        this.orientacionTextoTab=orientacionTextoTab;
    }
    
    /**
     * Obtener la dimension del icono definida para las pesta�as que se a�adan al componente
     * @return Atributo sizeIconoTab
     */
    public Dimension getSizeIconoTab(){
        return this.sizeIconoTab;
    }
    
    /**
     * Establecer el tama�o de los iconos de las pesta�as que se a�adan al componente
     * @param sizeIconoTab Dimension de los iconos
     */
    public void setSizeIconoTab(Dimension sizeIconoTab){
        this.sizeIconoTab=sizeIconoTab;
    }

    /**
     * Obtener la posicion relativa del icono de la pesta�a respecto al texto
     * @return Atributo posicionIconoTab
     */
    public int getPosicionIconoTab(){
        return this.posicionIconoTab;
    }
    
    /**
     * Establecer la posicion relativa del icono de la pesta�a respecto al texto
     * @param posicionIconoTab Posicion relativa del icono de la pesta�a respecto al texto.
     * Sus valores pueden ser:
     *    SwingConstants.LEFT
     *    SwingConstants.RIGHT
     *    SwingConstants.TOP
     *    SwingConstants.BOTTOM
     */
    public void setPosicionIconoTab(int posicionIconoTab){
        this.posicionIconoTab=posicionIconoTab;
    }
/**
     * A�adir un componente indicando la ruta de la imagen del icono
     * @param componente Componente que se le a�ade
     * @param nombreTab Nombre de la pesta�a
     * @param rutaIconoTab Ruta en la que se encuentra la imagen del icono que aparecera en la pesta�a, null para no poner icono
     * @param textoUso Texto de descripcion del componente, aparece al situar el raton encima de su pesta�a
     */
    public void addTabTextoVertical(java.awt.Component componente, String nombreTab, String rutaIconoTab, String textoUso) {        
        VTextIcon iconoTexto = new VTextIcon(this, nombreTab, this.orientacionTextoTab);
	if(rutaIconoTab!=null){
            CompositeIcon iconoCompuesto = new CompositeIcon(CreadorIconos.crearIcono(rutaIconoTab,this.sizeIconoTab), iconoTexto, this.posicionIconoTab);
            this.addTab(null, iconoCompuesto, componente, textoUso);
        }else this.addTab(null, iconoTexto, componente, textoUso);
    } 
    
    /**
     * A�adir un componente pasandole el icono
     * @param componente Componente que se le a�ade
     * @param nombreTab Nombre de la pesta�a
     * @param iconoTab Icono que aparece en la pesta�a, null para no poner icono
     * @param textoUso Texto de descripcion del componente, aparece al situar el raton encima de su pesta�a
     */
    public void addTabTextoVertical(java.awt.Component componente, String nombreTab, ImageIcon iconoTab, String textoUso) {        
        VTextIcon iconoTexto = new VTextIcon(this, nombreTab, this.orientacionTextoTab);
        if(iconoTab!=null){
            CompositeIcon iconoCompuesto = new CompositeIcon(iconoTab, iconoTexto, this.posicionIconoTab);
            this.addTab(null, iconoCompuesto, componente, textoUso);
        }else this.addTab(null, iconoTexto, componente, textoUso);
    }
    
    /**
     * Saber si el componente esta o no replegado
     * @return
     */
    public boolean isReplegada(){
    	return replegada;
    }
    
    /**
     * Replegar/Desplegar el componente
     * @param replegar True replegar, False desplegar
     */
    public void setTabReplegada(boolean replegar){
        if(replegar){
            componentesHijo=this.getComponents();

            int numeroHijos=this.getComponentCount();
            //int numeroHijos=this.getTabCount();
            for(int i=0;i<numeroHijos;i++){
            	/*javax.swing.JPanel panelAux=new javax.swing.JPanel();
            	panelAux.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
            	panelAux.setPreferredSize(new Dimension(0,0));//Esto se necesita cuando solo hay una pesta�a
            	panelAux.setMinimumSize(new Dimension(0,0));//Esto se necesita cuando solo hay una pesta�a
            	panelAux.setMaximumSize(new Dimension(0,0));//Esto se necesita cuando solo hay una pesta�a
            	panelAux.setSize(0, 0);*/
            	BarraHerramientas barraAux=new BarraHerramientas();
            	barraAux.setOrientation(JToolBar.VERTICAL);
            	barraAux.setBorder(BorderFactory.createEmptyBorder());
            	barraAux.setMargin(new Insets(0,0,0,0));
            	this.setComponentAt(i,/*panelAux*/barraAux);
            }
            setPreferredSize(null);
            replegada=true;
        }else{
            int numeroHijos=this.getComponentCount();
            //int numeroHijos=this.getTabCount();
            if(componentesHijo!=null){
	            for(int i=0;i<numeroHijos;i++){
	                this.setComponentAt(i,componentesHijo[i]);
	            }
	            replegada=false;
	            setPreferredSize(new Dimension(ConstantesGraficas.intMenuX,getPreferredSize().height));
            }
        }
    }
 }
