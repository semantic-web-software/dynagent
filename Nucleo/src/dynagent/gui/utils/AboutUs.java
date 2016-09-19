package dynagent.gui.utils;

import gdev.gbalancer.GViewBalancer;
import gdev.gen.AssignValueException;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;

import javax.naming.NamingException;
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
import javax.swing.event.HyperlinkListener;

import org.jdom.JDOMException;

import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.EngineException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.framework.ConstantesGraficas;
import dynagent.gui.WindowComponent;
import dynagent.gui.Singleton;

public class AboutUs extends JPanel implements HyperlinkListener {

	private static final long serialVersionUID = 1L;
	private WindowComponent parent;
	private WindowComponent dialog;
	private JEditorPane html=null;
	private int ancho=700, alto=500;
	private int dimBoton = ConstantesGraficas.intToolY-3;

	public AboutUs(WindowComponent dialog_parent){
		super();
		parent=dialog_parent;

		setLayout(null);
		setBackground(UIManager.getColor("ToolBar.background"));
		setBorder(new EmptyBorder(0,0,0,0));
		setPreferredSize(new Dimension(/*anchoPanel*/ConstantesGraficas.intToolY,ConstantesGraficas.intToolY));
		JButton b = new JButton(Singleton.getInstance().getComm().getIcon("logo"));
		b.setToolTipText("Acerca de...");
		b.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent action) {
				JDialog d=new JDialog(parent.getComponent());
				d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				d.setModal(true);				
				d.setSize(new Dimension(ancho, alto));
				d.setPreferredSize(new Dimension(ancho, alto));
				d.setResizable(false);
				d.setContentPane(getContent());
				d.pack();
				
				dialog = new WindowComponent(d,parent,parent.getKnowledgeBase());
				dialog.setTitle("Acerca Dynagent Software");
				dialog.setMainDialog(parent.getMainDialog());
				dialog.setLocationRelativeTo(parent.getComponent());
				dialog.getComponent().setVisible(true);
			}			
		});
		b.setBounds(/*anchoPanel-dimBoton*/1, /*(ConstantesGraficas.intToolY-3)/2-dimBoton/2*/1, dimBoton, dimBoton);
		add(b);
	}

	public JPanel getContent(){
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBackground(UIManager.getColor("ToolBar.background"));

		try {
			html = new JEditorPane(Singleton.getInstance().getComm().getURL("aboutUs.html"));
			html.setBackground(UIManager.getColor("ToolBar.background"));
		} catch (IOException e) {
			Singleton.getInstance().getComm().logError(parent.getComponent(),e,"Error al abrir un archivo");
		}	
		html.setContentType("text/html");
		html.setEditable(false);
		html.addHyperlinkListener(this);
		html.setBorder(BorderFactory.createEmptyBorder());
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(ConstantesGraficas.IncrementScrollVertical);
		scrollPane.getViewport().add(html);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());

//		JButton accept = new JButton("Aceptar");
//		accept.addActionListener(new ActionListener(){
//			public void actionPerformed(ActionEvent e) {
//				dialog.dispose();
//			}			
//		});
		/*JButton image = new JButton(Singleton.getInstance().getComm().getIcon("logoAboutUs"));
		image.setBorderPainted(false);
		image.setBorder(BorderFactory.createEmptyBorder());
		image.setSelected(false);*/
		JLabel image = new JLabel();
		image.setIcon(Singleton.getInstance().getComm().getIcon("logoAboutUs"));
		
		int imageY=138;
		int htmlY=320;
		JPanel panelImage=new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
		panelImage.setBackground(Color.WHITE);
		panelImage.add(image);
		panelImage.setBounds(0, 0, ancho, imageY);
		scrollPane.setBounds(10, imageY, ancho-15, htmlY);
		
//		accept.setBounds(ancho/2-40, alto-65, 80, 22);

		panel.add(scrollPane);
//		panel.add(accept);
		panel.add(panelImage);

		return panel;
	}

	public void hyperlinkUpdate(HyperlinkEvent e){
		
		try{
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				//String cmd = "rundll32 url.dll,FileProtocolHandler " + e.getURL();;
				//Runtime.getRuntime().exec(cmd);
				//Singleton.getInstance().getApplet().getAppletContext().showDocument(e.getURL(), "_blank");
				Desktop.getDesktop().browse(e.getURL().toURI());
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
