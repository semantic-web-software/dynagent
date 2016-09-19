package dynagent.framework.gestores;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import java.awt.FlowLayout;
import javax.swing.ImageIcon;

import dynagent.framework.ConstantesGraficas;

/**
 * Clase que se encarga de la gestion de la zona en la que se ubican los modulos de la aplicacion.
 * La gestion la realiza a partir de las interfaces GestorContenedorItems(para los modulos) y
 * GestorContenedorPaneles(para el monitor)
 * @author Francisco Javier Martinez Navarro
 *
 */
public class GestorZonaEstado extends GestorContenedor{

	public  GestorZonaEstado() {        
        super();
        panel.setLayout(new BorderLayout(0,0));
        //panel.setPreferredSize(ConstantesGraficas.dimStatus);
        //panel.setMinimumSize(ConstantesGraficas.dimStatus);
    	panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));
    	restriccionConjuntoPaneles=BorderLayout.CENTER;
    }     
    
    public boolean addItem(String identificador,String texto,String textoUso,ImageIcon icono,String identificadorPadre,String identificadorConjunto){
       	return false;
    }
 
    public boolean addPanel(String identificador,JComponent panel,int posicion,Dimension sizeMinimo,String identificadorConjunto){
    	Object conjunto=mapeadoConjuntosPaneles.get(identificadorConjunto);
    	if(conjunto==null){
    		// New JPanel simula un componente que contendria todos los paneles que se añaden
    		// a un mismo conjunto. Como en esta zona solo vamos a tener un panel, no va a haber mas paneles de un
    		// mismo conjunto pero haciendolo asi evitamos tener que reescribir todos los metodos de GestorContenedor
    		JPanel panelAux=new JPanel(new BorderLayout(0,0));
    		panelAux.add(panel,restriccionConjuntoPaneles);
    		mapeadoConjuntosPaneles.put(identificadorConjunto,panelAux);
    	}
    	panel.setName(identificador);
    	if(sizeMinimo!=null)
    		panel.setMinimumSize(sizeMinimo);    	
    	mapeadoPaneles.put(identificador,panel);
    	return true;
    }    
 }
