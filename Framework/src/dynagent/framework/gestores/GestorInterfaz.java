
package dynagent.framework.gestores;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import dynagent.common.communication.docServer;
import dynagent.framework.ConstantesGraficas;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Clase que se encarga de gestionar la ubicacion de las zonas de la interfaz grafica.
 * Permitiendo obtener las clases que gestionan cada una de las zonas.
 * @author Francisco Javier Martinez Navarro
 */

public class GestorInterfaz{

    /**
     * Container en el que se almacenan las distintas zonas
     */
    private javax.swing.JPanel panel;
    
    /**
     * Container en el que se almacenan las zonas trabajo y menu
     */
    private JSplitPane splitMenuTrabajo;

    /**
     * Instancia de la clase que se encarga de gestionar la zona donde aparece el menu de los modulos de la aplicacion
     */
    private GestorZonaMenu gestorZonaMenu;

    /**
     * Instancia de la clase que se encarga de gestionar la zona donde aparecen los modulos de la aplicacion
     */
    private GestorZonaModulos gestorZonaModulos;
    
    /**
     * Instancia de la clase que se encarga de gestionar la zona de trabajo de la aplicacion
     */
    private GestorZonaTrabajo gestorZonaTrabajo;

    /**
     * Instancia de la clase que se encarga de gestionar la zona de estado de la aplicacion
     */
    private GestorZonaEstado gestorZonaEstado;
    

    
    /**
     * Constantes para gestionar las zonas mediante los metodos addZona,getZona y removeZona
     */
    public final static int ZONA_MODULOS=1;
    public final static int ZONA_MENU=2;
    public final static int ZONA_TRABAJO=3;
    public final static int ZONA_ESTADO=4;
    
    /**
     * Constructor de la clase. Inicializa los atributos y ubica el componente de cada una de las zonas
     * en el container principal(atributo panel).
     */
    public  GestorInterfaz(docServer server) {
     
    	
    	panel=new JPanel();
    	
        panel.setLayout(new BorderLayout(0,0));
        splitMenuTrabajo = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitMenuTrabajo.setOneTouchExpandable(true);
        splitMenuTrabajo.setResizeWeight(0.0);
        splitMenuTrabajo.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent e) {
				if(e.getPropertyName().equals("dividerLocation")){
					if((Integer)e.getNewValue()>=ConstantesGraficas.intMenuX){
						splitMenuTrabajo.setDividerLocation(ConstantesGraficas.intMenuX);
						splitMenuTrabajo.validate();
						splitMenuTrabajo.repaint();
					}					
				}
			}			
		}); 
        panel.setBorder(BorderFactory.createEmptyBorder());
        splitMenuTrabajo.setBorder(BorderFactory.createEmptyBorder());
        
        gestorZonaModulos=new GestorZonaModulos();
        gestorZonaMenu=new GestorZonaMenu(server, splitMenuTrabajo);
        gestorZonaTrabajo=new GestorZonaTrabajo();
        gestorZonaEstado=new GestorZonaEstado();
        
        addZona(ZONA_MENU);
        addZona(ZONA_TRABAJO);
        addZona(ZONA_MODULOS);
        addZona(ZONA_ESTADO);              
    } 

    /**
     * Ubicar el componente de una determinada zona en el container panel
     */
    public void addZona(int zona) {
    	JPanel panelZona=null;
    	switch(zona){
    		case ZONA_MODULOS:
    			panelZona=gestorZonaModulos.getComponente();
    	    	//panelZona.setBackground(colorFondo);
    	        panel.add(panelZona, BorderLayout.NORTH);
    	        addComponents();
    	        break;
    		case ZONA_MENU:
    			panelZona=gestorZonaMenu.getComponente();
    	    
    	        splitMenuTrabajo.setLeftComponent(panelZona); 
    	        addComponents();
    			break;
    		case ZONA_TRABAJO:
    			panelZona=gestorZonaTrabajo.getComponente();
    	
    	    	splitMenuTrabajo.setRightComponent(panelZona);
    	    	addComponents();
    	    	break;
    		case ZONA_ESTADO:
    			panelZona=gestorZonaEstado.getComponente();
    	    	/*panelZona.setBackground(colorFondo);*/
    	        panel.add(panelZona,BorderLayout.SOUTH);
    	        addComponents();
    			break;
    	}
    	
    } 
    
    public void addComponents(){
    	if(splitMenuTrabajo.getLeftComponent()!=null && splitMenuTrabajo.getRightComponent()!=null && 
    			getZona(ZONA_MODULOS).getComponente()!=null && getZona(ZONA_ESTADO).getComponente()!=null){
        	panel.remove(splitMenuTrabajo);
        	panel.add(splitMenuTrabajo, BorderLayout.CENTER);   
        }
    }
    
    /**
     * Obtener el container principal
     * @return el atributo panel
     */
    public javax.swing.JPanel getComponente() {        
        return panel;
    } 
    
    /**
     * Obtener la instancia de una determinada zona
     */
    public GestorContenedor getZona(int zona) {
    	GestorContenedor gestor=null;
    	switch(zona){
    		case ZONA_MODULOS:
    			gestor=gestorZonaModulos;
    			break;
    		case ZONA_MENU:
    			gestor=gestorZonaMenu;
    			break;
    		case ZONA_TRABAJO:
    			gestor=gestorZonaTrabajo;
    			break;
    		case ZONA_ESTADO:
    			gestor=gestorZonaEstado;
    			break;
    	}
    	return gestor;
    } 
    
    /**
     * Quitar el componente de una determinada zona del container panel
     */
    public void removeZona(int zona) {
    	switch(zona){
    		case ZONA_MODULOS:
    			panel.remove(gestorZonaModulos.getComponente());
    			break;
    		case ZONA_MENU:
    			splitMenuTrabajo.remove(gestorZonaMenu.getComponente());
    	        //gestorZonaModulos.removeSeleccion();
    			break;
    		case ZONA_TRABAJO:
    			splitMenuTrabajo.remove(gestorZonaTrabajo.getComponente());      			
    			break;
    		case ZONA_ESTADO:
    			panel.remove(gestorZonaEstado.getComponente());
    	    	break;
    	}
    }    
 }
