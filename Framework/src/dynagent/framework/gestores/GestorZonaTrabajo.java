package dynagent.framework.gestores;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.BorderLayout;

/**
 * Clase que se encarga de la gestion de la zona de trabajo(filtro,formulario y resultados) de la aplicacion.
 * La gestion la realiza a partir de la interface GestorContenedorPaneles(para cada una de las zonas)
 * @author Francisco Javier Martinez Navarro
 *
 */
public class GestorZonaTrabajo extends GestorContenedor{
 
    public  GestorZonaTrabajo() {
    	super();
        panel.setLayout(new BorderLayout(0,0));
        panel.setBorder(BorderFactory.createEmptyBorder());
        restriccionConjuntoPaneles=BorderLayout.CENTER;
    } 
    
    public boolean addItem(String identificador, String texto, String textoUso, ImageIcon icono, String identificadorPadre, String identificadorConjunto){
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
    		
    		panel.setName(identificador);
        	mapeadoPaneles.put(identificador,panel);
        }else{
        	((JPanel)conjunto).add(panel);
        	panel.setName(identificador);
        	mapeadoPaneles.put(identificador,panel);
        }
    	
    	return true;
    }
 }
