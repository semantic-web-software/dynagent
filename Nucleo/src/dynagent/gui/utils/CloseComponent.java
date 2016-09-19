package dynagent.gui.utils;

import gdev.gawt.utils.GFocusTraversalPolicy;
import gdev.gen.EditionTableException;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;

import dynagent.common.utils.GIdRow;
import dynagent.framework.ConstantesGraficas;
import dynagent.gui.Singleton;
import dynagent.gui.WindowComponent;

public class CloseComponent extends JPanel{

	private static final long serialVersionUID = 1L;
	private WindowComponent parent;
	private WindowComponent dialog;
	private JEditorPane html=null;
	private int ancho=500, alto=380;
	private int dimBoton = ConstantesGraficas.intToolY-3;

	public CloseComponent(WindowComponent dialog_parent){
		super();
		parent=dialog_parent;

		setLayout(null);
		setBackground(UIManager.getColor("ToolBar.background"));
		setBorder(new EmptyBorder(0,0,0,0));
		setPreferredSize(new Dimension(/*anchoPanel*/ConstantesGraficas.intToolY,ConstantesGraficas.intToolY));
		JButton b = new JButton(Singleton.getInstance().getComm().getIcon("close"));
		b.setToolTipText("Cerrar sesión");
		b.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent action) {
				try {
					parent.disabledEvents();
					
					int i=0;
					if(Window.getWindows().length>1){
						for(Window wind:Window.getWindows()){
							if(wind.isDisplayable()){
								//System.err.println(wind.toString());
								i++;
							}
						}
					}
					
					/*if(i>1){
						Singleton.getInstance().getMessagesControl().showMessage("Debe finalizar el trabajo en el resto de ventanas de la aplicación para poder cerrar la sesión",parent.getComponent());
					}else{*/
						
						Object[] options = {"Aceptar", "Cancelar"};
						String message="¿Está seguro que desea cerrar la sesión?";
						if(i>1){
							message="Asegúrese de haber cerrado el resto de ventanas abiertas en la aplicación, si las hubiera, para evitar posibles inconvenientes en el servidor.\n"+message;
						}
						int res = Singleton.getInstance().getMessagesControl().showOptionMessage(
								message,
								"Cerrar sesión",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE,
								null,
								options,
								options[0],parent.getComponent());
	
						if(res == JOptionPane.YES_OPTION){
							Singleton.getInstance().getApplet().getAppletManager().closeSession();
						}
					/*}*/
				}catch(Exception ex){
					ex.printStackTrace();
				} finally{
					parent.enabledEvents();
				}
			}
		});
		b.setBounds(/*anchoPanel-dimBoton*/1, /*(ConstantesGraficas.intToolY-3)/2-dimBoton/2*/1, dimBoton, dimBoton);
		add(b);
	}

}
