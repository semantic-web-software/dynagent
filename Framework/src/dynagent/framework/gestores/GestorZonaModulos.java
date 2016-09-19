package dynagent.framework.gestores;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

import java.awt.FlowLayout;
import javax.swing.ImageIcon;

import dynagent.framework.ConstantesGraficas;
import dynagent.framework.componentes.BarraHerramientas;

/**
 * Clase que se encarga de la gestion de la zona en la que se ubican los modulos de la aplicacion.
 * La gestion la realiza a partir de las interfaces GestorContenedorItems(para los modulos) y
 * GestorContenedorPaneles(para el monitor)
 * @author Francisco Javier Martinez Navarro
 *
 */
public class GestorZonaModulos extends GestorContenedor{

	//private Dimension sizeIconos;
	//private Dimension sizeButtons;
	
	public  GestorZonaModulos() {        
        super();
        panel.setLayout(new BorderLayout(0,0));
    	panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
    	panel.setBackground(UIManager.getColor("ToolBar.background"));
    	//panel.setPreferredSize(ConstantesGraficas.dimTool);
    	restriccionConjuntoPaneles=BorderLayout.CENTER;
    	//panel.setBorder(BorderFactory.createEtchedBorder());
        //sizeIconos=calcularSizeIconos();
        calcularSizeButtonsAndIcons();
    } 

    private void calcularSizeButtonsAndIcons(){
    	//BotonRollover boton=new BotonRollover("","","",null);
        JToggleButton boton=new JToggleButton("");
        boton.setMargin(new Insets(0,0,0,0));
        //boton.setVerticalTextPosition(SwingConstants.CENTER);
        //boton.setHorizontalTextPosition(SwingConstants.CENTER);
        BarraHerramientas barra=new BarraHerramientas();
        barra.setBorder(BorderFactory.createEmptyBorder());
        //barraHerramientas.setOrientation(JToolBar.HORIZONTAL);
        JPanel panel=new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
        
        panel.add(barra);
        //panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black));
        //sizeButtons=new Dimension(-1,ConstantesGraficas.altoBarraHerramientas-(int)panel.getPreferredSize().getHeight()-1/*Es 6+6*/);
        barra.addBoton(boton);
        //sizeIconos=new Dimension(-1,sizeButtons.height-boton.getInsets().top-boton.getInsets().bottom/*Es 6+6*/);
        //System.err.println("sizeButtons modulos:"+sizeButtons);
        //System.err.println("sizeIconos modulos:"+sizeIconos);
    }
    
    public void removeSeleccion(){
    	BarraHerramientas barraHerramientas=(BarraHerramientas)mapeadoConjuntosItems.get(identificadorConjuntoItemsActual);
    	barraHerramientas.getGrupoBotones().clearSelection();
    }
     
    
    public boolean addItem(String identificador,String texto,String textoUso,ImageIcon icono,String identificadorPadre,String identificadorConjunto){
        if(identificadorPadre!=null){
            //No hacemos nada sobre esto ya que no tenemos padres
            return false;
        }else{
            /*BotonRollover boton=new BotonRollover(texto,textoUso,icono,this.sizeIconos);
            boton.setIconoEstado(BotonRollover.IMAGEN_SELECCIONADO,BotonRollover.ICONO_NORMAL);
            //boton.setIconoEstado(BotonRollover.IMAGEN_ENCIMA,BotonRollover.ICONO_NORMAL);
            boton.setIconoEstado(BotonRollover.IMAGEN_FUERA,BotonRollover.ICONO_NORMAL);
            boton.setMargin(new Insets(0,0,0,0));
            if(icono==null){
            	int width;
            	if(texto==null)
            		width=sizeButtons.height;
            	else width=(int)boton.getPreferredSize().getWidth();
            	boton.setPreferredSize(new Dimension(width,sizeButtons.height));
                boton.setMaximumSize(new Dimension(width,sizeButtons.height));
            }
            //System.err.println("Size:"+boton.getPreferredSize());
            boton.setCursor(getPredefinedCursor(Cursor.HAND_CURSOR));
            //boton.setVerticalTextPosition(SwingConstants.CENTER);
            //boton.setHorizontalTextPosition(SwingConstants.CENTER);
            
            //skinsZonaModulos.setSkinBoton(boton);//Esto lo hacemos en setSkinItem
                  	
            BarraHerramientas barraHerramientas=(BarraHerramientas)mapeadoConjuntosItems.get(identificadorConjunto);
            if(barraHerramientas==null){
            	barraHerramientas=new BarraHerramientas();
            	barraHerramientas.setBorder(BorderFactory.createEmptyBorder());
            	//barraHerramientas.addSeparator();
            	//barraHerramientas.setOrientation(JToolBar.HORIZONTAL);
            	
            	//skinsZonaModulos.setSkinBarra(barraHerramientas);
            	mapeadoConjuntosItems.put(identificadorConjunto,barraHerramientas);
            }
            barraHerramientas.addBoton(boton);
            
            //System.err.println("sizeBarraModulos:"+barraHerramientas.getPreferredSize());
            
            mapeadoItems.put(identificador,boton);*/
            return true;
        }
    }
 
    public boolean addPanel(String identificador,JComponent panel,int posicion,Dimension sizeMinimo,String identificadorConjunto){
    	Object conjunto=mapeadoConjuntosPaneles.get(identificadorConjunto);
    	if(conjunto==null){
    		// New JPanel simula un componente que contendria todos los paneles que se añaden
    		// a un mismo conjunto. Como en esta zona solo vamos a tener un panel, no va a haber mas paneles de un
    		// mismo conjunto pero haciendolo asi evitamos tener que reescribir todos los metodos de GestorContenedor
    		JPanel panelAux=new JPanel();
    		//panelAux.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
    		panelAux.setLayout(new BorderLayout(0,0));
    		panelAux.add(panel,restriccionConjuntoPaneles);
    		mapeadoConjuntosPaneles.put(identificadorConjunto,panelAux);
    		
    		BarraHerramientas barraHerramientas=(BarraHerramientas)mapeadoConjuntosItems.get(identificadorConjunto);
            if(barraHerramientas==null){
            	barraHerramientas=new BarraHerramientas();
            	barraHerramientas.setBorder(BorderFactory.createEmptyBorder());
            	//barraHerramientas.addSeparator();
            	//barraHerramientas.setOrientation(JToolBar.HORIZONTAL);
            	
            	//skinsZonaModulos.setSkinBarra(barraHerramientas);
            	mapeadoConjuntosItems.put(identificadorConjunto,barraHerramientas);
            }
            barraHerramientas.add(panelAux);
    	}else ((JPanel)conjunto).add(panel);
    	panel.setName(identificador);
    	if(sizeMinimo!=null)
    		panel.setMinimumSize(sizeMinimo);
    	mapeadoPaneles.put(identificador,panel);
    	return true;
    }
    
 }
