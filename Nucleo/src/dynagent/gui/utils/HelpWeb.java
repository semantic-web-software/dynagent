package dynagent.gui.utils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;

import dynagent.framework.ConstantesGraficas;
import dynagent.gui.Singleton;
import dynagent.gui.WindowComponent;

public class HelpWeb extends JPanel{
	private static final long serialVersionUID = 1L;
	private WindowComponent parent;
	private WindowComponent dialog;
	private JEditorPane html=null;
	private int ancho=500, alto=380;
	private int dimBoton = ConstantesGraficas.intToolY-3;

	public HelpWeb(WindowComponent dialog_parent){
		super();
		parent=dialog_parent;

		setLayout(null);
		setBackground(UIManager.getColor("ToolBar.background"));
		setBorder(new EmptyBorder(0,0,0,0));
		setPreferredSize(new Dimension(/*anchoPanel*/ConstantesGraficas.intToolY,ConstantesGraficas.intToolY));
		JButton b = new JButton(Singleton.getInstance().getComm().getIcon(null,"help_general",30,30));
		b.setToolTipText("Ayuda general");
		b.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent action) {
				Singleton.getInstance().getComm().showHelpPage();
			}			
		});
		b.setBounds(/*anchoPanel-dimBoton*/1, /*(ConstantesGraficas.intToolY-3)/2-dimBoton/2*/1, dimBoton, dimBoton);
		add(b);
	}

//	public JPanel getContent(){
//		JPanel panel = new JPanel();
//		panel.setLayout(null);
//		panel.setBackground(UIManager.getColor("ToolBar.background"));
//
//		try {
//			html = new JEditorPane(Singleton.getInstance().getComm().getURL("aboutUs.html"));
//			html.setBackground(UIManager.getColor("ToolBar.background"));
//		} catch (IOException e) {
//			Singleton.getInstance().getComm().logError(e,"Error al abrir un archivo");
//		}	
//		html.setContentType("text/html");
//		html.setEditable(false);
//		html.addHyperlinkListener(this);
//		html.setBorder(BorderFactory.createEmptyBorder());
//		JScrollPane scrollPane = new JScrollPane();
//		scrollPane.getVerticalScrollBar().setUnitIncrement(ConstantesGraficas.IncrementScrollVertical);
//		scrollPane.getViewport().add(html);
//		scrollPane.setBorder(BorderFactory.createEmptyBorder());
//
////		JButton accept = new JButton("Aceptar");
////		accept.addActionListener(new ActionListener(){
////			public void actionPerformed(ActionEvent e) {
////				dialog.dispose();
////			}			
////		});
//		/*JButton image = new JButton(Singleton.getInstance().getComm().getIcon("logoAboutUs"));
//		image.setBorderPainted(false);
//		image.setBorder(BorderFactory.createEmptyBorder());
//		image.setSelected(false);*/
//		JLabel image = new JLabel();
//		image.setIcon(Singleton.getInstance().getComm().getIcon("logoAboutUs"));
//		
//		int imageY=138;
//		int htmlY=160;
//		image.setBounds(0, 0, ancho, imageY);
//		scrollPane.setBounds(10, imageY, ancho-10, htmlY);
//		
////		accept.setBounds(ancho/2-40, alto-65, 80, 22);
//
//		panel.add(scrollPane);
////		panel.add(accept);
//		panel.add(image);
//
//		return panel;
//	}
//
//	public void hyperlinkUpdate(HyperlinkEvent e){
//		
//		try{
//			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
//				//String cmd = "rundll32 url.dll,FileProtocolHandler " + e.getURL();;
//				//Runtime.getRuntime().exec(cmd);
//				Singleton.getInstance().getApplet().getAppletContext().showDocument(e.getURL(), "_blank");
//			}
//		}catch(Exception ex){
//			ex.printStackTrace();
//		}
//	}
}
