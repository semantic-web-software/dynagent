package dynagent.framework.gestores;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;

import javax.swing.JToolBar;
import javax.swing.JToggleButton;
import java.awt.Component;

import dynagent.common.communication.docServer;
import dynagent.framework.*;
import dynagent.framework.componentes.*;


/**
 * Clase que se encarga de la gestion de la zona en la que se ubica el menu de los modulos de la aplicacion.
 * La gestion la realiza a partir de la interface GestorContenedorItems(para las opciones del menu)
 * @author Francisco Javier Martinez Navarro
 *
 */
public class GestorZonaMenu extends GestorContenedor{

	//private BotonScroll botonScroll;

	/**
	 * Almacena los componentes ButtonGroup creados para cada conjunto de items asignandoles un identificador
	 */
	//private Dimension sizeIcons;
	//private Dimension sizeButtons;

	private docServer server;
	private JSplitPane split;

	public  GestorZonaMenu(docServer server, JSplitPane splitMenuTrabajo) {        
		super();
		panel=new JPanel(){

			@Override
			public boolean requestFocusInWindow() {
				//System.err.println("ENTRA EN SHORTCUT DE MENU");
				if(!mapeadoConjuntosItems.isEmpty()){
					CollapsableMenu menu=(CollapsableMenu)mapeadoConjuntosItems.get(mapeadoConjuntosItems.keySet().iterator().next());
					return menu.requestFocusInWindow();
				}else return false;
			}
			
		};
		this.server=server;
		split=splitMenuTrabajo;
		panel.setLayout(new BorderLayout(0,0));
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.setBackground(UIManager.getColor("ToolBar.background"));

		restriccionConjuntoItems=BorderLayout.CENTER;

		/*botonOcultar=new BotonOcultar("Plegar/Desplegar el menu",BotonOcultar.LEFT_TO_RIGHT,null);
		botonOcultar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clickEnBotonOcultar(e); }
		});
		botonOcultar.setMargin(new Insets(0,0,0,0));
		botonOcultar.setVisible(false);

		panel.add(botonOcultar,BorderLayout.NORTH);*/

		//sizeIcons=calcularSizeIcons(sizeButtons);
		//sizeButtons=calcularSizeButtons();
//		calcularSizeButtonsAndIcons();

		//System.err.println("sizeButtons:"+sizeButtons);
		//System.err.println("sizeIcons:"+sizeIcons);
	} 

	private void clickEnBotonOcultar(ActionEvent e) {
		try{
			if(e.getActionCommand().compareTo("EstadoOriginal")==0){
				TabbedPaneTextoVertical tabbedPane=(TabbedPaneTextoVertical)mapeadoConjuntosItems.get(identificadorConjuntoItemsActual);
				if(tabbedPane!=null)
					tabbedPane.setTabReplegada(true);
			}else{
				TabbedPaneTextoVertical tabbedPane=(TabbedPaneTextoVertical)mapeadoConjuntosItems.get(identificadorConjuntoItemsActual);
				if(tabbedPane!=null)
					tabbedPane.setTabReplegada(false);

			}
			//botonOcultar.cambiarEstado();
			panel.revalidate();
		}catch(Exception ex){
			//server.logError(ex,"Error al ocultar/mostrar menu");
			ex.printStackTrace();
		}
	}

//	private void calcularSizeButtonsAndIcons(){
//		BotonOcultar botonOcultar=new BotonOcultar("Plegar/Desplegar el menu",BotonOcultar.LEFT_TO_RIGHT,null);
//		botonOcultar.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				clickEnBotonOcultar(e); 
//				
//			}
//		});
//		botonOcultar.setMargin(new Insets(0,0,0,0));
//		JToggleButton boton1=new JToggleButton();
//		boton1.setText("ABCDE");
//		JToggleButton boton2=new JToggleButton();
//		boton2.setText("ABCDE");
//		BarraHerramientas barra1=new BarraHerramientas();
//		barra1.setOrientation(JToolBar.VERTICAL);
//		barra1.setBorder(BorderFactory.createEmptyBorder());
//		BarraHerramientas barra2=new BarraHerramientas();
//		barra2.setOrientation(JToolBar.VERTICAL);
//		barra2.setBorder(BorderFactory.createEmptyBorder());
//		TabbedPaneTextoVertical tabbedPane=new TabbedPaneTextoVertical(dynagent.framework.utilidades.VTextIcon.ROTATE_LEFT);
//		tabbedPane.setTabPlacement(JTabbedPane.LEFT);
//		tabbedPane.setBorder(BorderFactory.createEmptyBorder());
//		UIManager.put("TabbedPane.textIconGap",new Integer(0));
//		tabbedPane.updateUI();
//		JPanel panel=new JPanel();
//		panel.setLayout(new BorderLayout(0,0));
//		barra1.setMargin(new Insets(0,0,0,0));
//		barra2.setMargin(new Insets(0,0,0,0));
//
//		tabbedPane.addTabTextoVertical(barra1,"Configuraciones",(String)null,"Configuracion de clientes");
//		//tabbedPane.addTabTextoVertical(barra2,"Operaciones",(String)null,"Operaciones de clientes");
//		panel.add(botonOcultar,BorderLayout.NORTH);
//		panel.add(tabbedPane,BorderLayout.CENTER);
//
//		JPanel panel1=new JPanel();
//		panel1.add(panel);
//		barra1.addBoton(boton1);
//		barra1.addBoton(boton2);
//
//		//sizeButtons=new Dimension(88,-1);
//		//sizeIcons=new Dimension(20,-1);
//	}

	public boolean addItem(String identificador,String texto,String textoUso,ImageIcon icon,String identificadorPadre,String identificadorConjunto){

		if(identificadorPadre!=null){
			BranchPanel branch=(BranchPanel)mapeadoItems.get(identificadorPadre);
			if(branch!=null){
				CollapsableMenu menu=(CollapsableMenu)mapeadoConjuntosItems.get(identificadorConjunto);
				if(menu!=null){

					JButton button = new JButton(texto);
					button.setName(texto+"@"+branch.getName());
					//button.setBorder(BorderFactory.createEmptyBorder());
					button.setMargin(new Insets(0, 6, 0, 0));
					button.setToolTipText(textoUso);
					//if(icon!=null)
					//	button.setIcon(CreadorIconos.redimensionarIcono(icon, new Dimension(17,17)));
					button.setContentAreaFilled(false);
					button.setBorderPainted(false);
					//button.setRequestFocusEnabled(false);//Esto provoca una perdida de foco sin situarse en ningun componente. Por lo que es un problema.
					button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));					
					button.setPreferredSize(new Dimension(ConstantesGraficas.intMenuX-4, button.getPreferredSize().height));
					branch.addBranchItem(identificador,button);
					mapeadoItems.put(identificador,button);
					return true;
				}else return false;
			}else return false;
		}else{

			CollapsableMenu menu=(CollapsableMenu)mapeadoConjuntosItems.get(identificadorConjunto);
			if(menu==null){
				menu=new CollapsableMenu(server);			
				menu.setBorder(BorderFactory.createEmptyBorder());
				menu.updateUI();
				menu.setPreferredSize(ConstantesGraficas.dimMenu);
				menu.setMaximumSize(ConstantesGraficas.dimMenu);
				mapeadoConjuntosItems.put(identificadorConjunto,menu);

			}
			BranchPanel branch = new BranchPanel(texto, icon, menu);
			branch.setToolTipText(textoUso);
			menu.addBranchPanel(texto, branch);
			//botonScroll=new BotonScroll(barraHerramientas,tabbedPane.getPreferredSize().getHeight());            
			mapeadoItems.put(identificador,branch);
			return true;
		}
	}
	
	public boolean setVisibleItems(String identificadorConjunto,boolean mostrar){
		panel.remove((Component)mapeadoConjuntosItems.get(identificadorConjunto));
		if(super.setVisibleItems(identificadorConjunto,mostrar)){
			CollapsableMenu menu=(CollapsableMenu)mapeadoConjuntosItems.get(identificadorConjunto);
			menu.setVisible(true);
			if(mostrar){
				menu.init();
				panel.revalidate();
				panel.repaint();
			}else{
				split.setDividerLocation(0);
				if(menu.getActualBranchPanel()!=null && menu.getActualButton()!=null)
					menu.hideActual();
			}
			return true;
		}
		return false;
	}	

	public boolean setVisibleItem(String identificador, boolean mostrar) {
		Component item=(Component)mapeadoItems.get(identificador);
		if(item!=null){
			((JButton)item).doClick();
			CollapsableMenu menu=(CollapsableMenu)mapeadoConjuntosItems.values().toArray()[0];
			if(!(item instanceof BranchPanel))
				menu.showBranch((JButton)item);
			item.setVisible(mostrar);
			return true;
		}
		return false;
	}

	public boolean addPanel(String identificador,JComponent panel,int posicion,Dimension sizeMinimo,String identificadorConjunto){
		return false;
	}

	public boolean setHighlightedItem(String identificador,boolean highlighted,String identificadorPadre){
		if(identificadorPadre!=null){
			BranchPanel branch=(BranchPanel)mapeadoItems.get(identificadorPadre);
			if(branch!=null){
				JButton button=(JButton)mapeadoItems.get(identificador);
				if(button!=null)
					branch.setHighlightedItem(identificador, highlighted);
				else return false;
			}else return false;
		}else{
			BranchPanel branch=(BranchPanel)mapeadoItems.get(identificador);
			if(branch!=null){
				branch.setHighlighted(highlighted);
			}else return false;
		}
		return true;
	}
	
	public boolean setHighlightedItemColor(String identificador,Color color,String identificadorPadre){
		if(identificadorPadre!=null){
			BranchPanel branch=(BranchPanel)mapeadoItems.get(identificadorPadre);
			if(branch!=null){
				JButton button=(JButton)mapeadoItems.get(identificador);
				if(button!=null)
					branch.setHighlightedItemColor(identificador,color);
				else return false;
			}else return false;
		}else{
			BranchPanel branch=(BranchPanel)mapeadoItems.get(identificador);
			if(branch!=null){
				branch.setHighlightedColor(color);
			}else return false;
		}
		return true;
	}
}
