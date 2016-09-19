package dynagent.gui;

import gdev.gawt.utils.botoneraAccion;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

import dynagent.framework.gestores.GestorContenedor;
import dynagent.framework.gestores.GestorInterfaz;

public class NavigationControl implements ActionListener{

	private JPanel panel;
	private static String PREVIOUS_BUTTON="previous";
	private static String NEXT_BUTTON="next";
	private JButton previousButton;
	private JButton nextButton;
	private ArrayList<String> pages;
	private int actualPage;
	
	public NavigationControl() {
		panel=new JPanel();
		pages=new ArrayList<String>();
		actualPage=-1;
		build();
	}
	
	private void build(){
		previousButton=botoneraAccion.subBuildBoton(panel, "Anterior", "Back24", PREVIOUS_BUTTON, "Anterior", this, false, Singleton.getInstance().getComm());
		previousButton.setEnabled(false);
		nextButton=botoneraAccion.subBuildBoton(panel, "Siguiente", "Forward24", NEXT_BUTTON, "Siguiente", this, false, Singleton.getInstance().getComm());
		nextButton.setEnabled(false);
	}
	
	public JPanel getComponent(){
		return panel;
	}

	public void actionPerformed(ActionEvent ae) {
		try{
			String actionCommand=ae.getActionCommand();
			if(actionCommand.equals(PREVIOUS_BUTTON)){
				GestorContenedor gestor=(GestorContenedor)Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_MENU);
				actualPage--;
				String id=pages.get(actualPage);
				if(!gestor.setVisibleItem(id, true)){
					Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_TRABAJO).setVisiblePanels(id, true);
					GestorContenedor gestorMenu=(GestorContenedor)Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_MENU);
					gestorMenu.setVisibleItems(null, false);
				}
				if(!nextButton.isEnabled())
					nextButton.setEnabled(true);
				if(actualPage==0)
					previousButton.setEnabled(false);
			}
			else if(actionCommand.equals(NEXT_BUTTON)){
				GestorContenedor gestor=(GestorContenedor)Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_MENU);
				actualPage++;
				String id=pages.get(actualPage);
				if(!gestor.setVisibleItem(id, true)){
					Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_TRABAJO).setVisiblePanels(id, true);
					GestorContenedor gestorMenu=(GestorContenedor)Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_MENU);
					gestorMenu.setVisibleItems(null, false);
				}
				if(!previousButton.isEnabled())
					previousButton.setEnabled(true);
				if(actualPage==pages.size()-1)
					nextButton.setEnabled(false);
			}
		}catch(Exception e){
			Singleton.getInstance().getComm().logError(null,e,"Error al crear el anterior/siguiente formulario");
			e.printStackTrace();
		}
	}
	
	public void setActualPage(String id){
		if(actualPage==-1 || !pages.get(actualPage).equals(id)){
			if(!previousButton.isEnabled() && actualPage!=-1){
				previousButton.setEnabled(true);
			}
			
			if(actualPage<pages.size()-1){
				int size=pages.size();
				for(int i=actualPage+1;i<size;i++)
					pages.remove(pages.size()-1);
				if(nextButton.isEnabled())
					nextButton.setEnabled(false);
			}
			pages.add(id);
			actualPage=pages.size()-1;
			
			//if(!nextButton.isEnabled())
			//	nextButton.setEnabled(true);
		}
	}

}
