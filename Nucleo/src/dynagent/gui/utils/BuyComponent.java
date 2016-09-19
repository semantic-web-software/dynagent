package dynagent.gui.utils;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import dynagent.framework.ConstantesGraficas;
import dynagent.gui.Singleton;
import dynagent.gui.WindowComponent;

public class BuyComponent extends JPanel{
	private static final long serialVersionUID = 1L;
	private WindowComponent parent;
	private WindowComponent dialog;
	private JEditorPane html=null;
	private int ancho=500, alto=380;
	private int dimBoton = ConstantesGraficas.intToolY-3;

	public BuyComponent(WindowComponent dialog_parent,final String subscription,final String email){
		super();
		parent=dialog_parent;

		setLayout(null);
		setBackground(UIManager.getColor("ToolBar.background"));
		setBorder(new EmptyBorder(0,0,0,0));
		setPreferredSize(new Dimension(/*anchoPanel*/ConstantesGraficas.intToolY,ConstantesGraficas.intToolY));
		JButton b = new JButton(Singleton.getInstance().getComm().getIcon("buyDynagent"));
		b.setToolTipText("Comprar producto");
		b.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent action) {
				try {
					parent.disabledEvents();
					
					Singleton.getInstance().getComm().showBuyPage(subscription,email);
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
