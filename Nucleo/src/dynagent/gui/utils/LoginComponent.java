package dynagent.gui.utils;

import gdev.gbalancer.GProcessedForm;
import gdev.gbalancer.GViewBalancer;
import gdev.gen.AssignValueException;
import gdev.gen.IComponentListener;
import gdev.gfld.GValue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import dynagent.common.communication.communicator;
import dynagent.common.utils.Utils;
import dynagent.framework.ConstantesGraficas;
import dynagent.gui.Singleton;
import dynagent.gui.StatusBar;
import dynagent.gui.WindowComponent;
import dynagent.gui.forms.builders.FormManager;
import dynagent.gui.forms.builders.formFactory;

public class LoginComponent extends JPanel{
	
	JProgressBar barraProgreso;
	boolean finishBarraProgreso;
	JLabel licenseDaysLabel;
	
	public class LinkButton extends JButton{
		LinkButton(String label, final URI uri){					                
        setText(label);
        setHorizontalAlignment(SwingConstants.LEFT);
        setBorderPainted(false);
        setOpaque(false);
        setBackground(Color.lightGray);
        addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
            	if (Desktop.isDesktopSupported()) {
                	Desktop desktop = Desktop.getDesktop();
                    	try {
                        	desktop.browse(uri);
                        } catch (Exception ex) {}
            	} 
        	}
         });
        }
	}

	public LoginComponent(JApplet parent,String valueLogin,String valuePassword,Integer valueMode,ArrayList<GValue> modes,String imageName,IComponentListener componentListener,ActionListener actionListener,KeyListener keyListener) throws ParseException, AssignValueException, MalformedURLException{
		super();
		
		JLabel label=new JLabel(communicator.getIconNotCache(Singleton.getInstance().getCodeBaseJar(),null, imageName, 3*parent.getSize().width/4, parent.getSize().height/3));
//	    JPanel panel=new JPanel(new FlowLayout(FlowLayout.CENTER,2,2));
//	    //JPanel panel=new JPanel(new BoxLayout(this, BoxLayout.X_AXIS ) );
//	    panel.add(label);
//	    panel.setBackground(Color.WHITE);
		
		JPanel panelLogo=new JPanel(/*new FlowLayout(FlowLayout.CENTER,2,2)*/);
	    //JPanel panel=new JPanel(new BoxLayout(this, BoxLayout.X_AXIS ) );
		panelLogo.add(label);

		//panelLogo.setBackground(Color.WHITE);
	  	GProcessedForm viewForm=formFactory.buildFormularioLogin(1,valueLogin,2,valuePassword,3,modes,valueMode);

		ArrayList<GProcessedForm> listaViewForm=new ArrayList<GProcessedForm>();
		listaViewForm.add(viewForm);

		FormManager loginForm= new FormManager( /*null,*/
			
				componentListener,
				null,
				actionListener,
				0,					
				"INSERTE LOGIN",					
				listaViewForm,
				true,
				false,
				false,
				true,
				true,
				keyListener, ConstantesGraficas.dimInit, null,/*window*/null, null, null, null);

//		m_loginDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//		m_loginDialog.getContentPane().add(loginForm.getComponent(), BorderLayout.CENTER);
//		m_loginDialog.setTitle(Utils.normalizeLabel("Identificación de usuario"));
//		m_loginDialog.pack();
//		/*m_loginDialog.setLocation(getGUICenter().x-m_loginDialog.getWidth()/2,
//				  getGUICenter().y-m_loginDialog.getHeight()/2);*/
//		m_loginDialog.setLocationRelativeTo(null);
//		m_loginDialog.setVisible(true);
		JPanel panelAux=new JPanel();
		panelAux.setLayout(new BoxLayout(panelAux, BoxLayout.Y_AXIS));
		
		JPanel licenseDaysPanel=new JPanel();
		licenseDaysLabel=new JLabel("");
		licenseDaysPanel.add(licenseDaysLabel);
		panelAux.add(licenseDaysPanel);
		
		JPanel login=loginForm.getComponent();
		login.setBorder(BorderFactory.createEtchedBorder());
		JPanel loginPanelAux=new JPanel();
		loginPanelAux.add(login);
		panelAux.add(loginPanelAux);
		
		JPanel tips=new JPanel();
		tips.setLayout(new BoxLayout(tips, BoxLayout.Y_AXIS ));
		panelAux.add(tips);
		JPanel panelLogin=new JPanel();
		
		panelLogin.add(panelAux);
		//panel.setPreferredSize(new Dimension(parent.getSize().width,login.getPreferredSize().height+login.getInsets().bottom+login.getInsets().top));
		
		JPanel panelStatus=new JPanel();
		barraProgreso=new JProgressBar();
		barraProgreso.setString(Utils.normalizeLabel("Identificación de usuario"));
		barraProgreso.setStringPainted(true);
		//barraProgreso.setBackground(Color.WHITE);
		barraProgreso.setPreferredSize(new Dimension(parent.getSize().width/2,(int)GViewBalancer.getRowHeightS(Singleton.getInstance().getGraphics())));
		panelStatus.add(barraProgreso);
		//panelStatus.setBackground(Color.WHITE);
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS ));
		
		panelLogo.setPreferredSize(new Dimension(this.getSize().width, this.getSize().height/3));
		panelLogin.setPreferredSize(new Dimension(this.getSize().width, this.getSize().height/3));
		panelStatus.setPreferredSize(new Dimension(this.getSize().width, this.getSize().height/3));
		
		
		try {
			String stilo="style=background-color:#F5F6CE;font-family:verdana;font-size:medium;padding:5px;";
			URI adaptar = new URI("www.dynagent.es/adaptar_campos_a_medida_ERP.html");
			LinkButton tipLabel=new LinkButton("<html><p/></p><p "+stilo+">- Cómo <a href=\"www.dynagent.es\">quitar o añadir</a> campos <b>lote, talla, color</b>, proyectos, etc. fácilmente</p></html>",adaptar);
			tips.add(tipLabel);
			URI report = new URI("www.dynagent.es/adaptar_impresion_a_medida_ERP.html");
			LinkButton tipLabel2=new LinkButton("<html><p "+stilo+">- Cómo forzar que los documentos <a href=\"www.dynagent.es\">se impriman automáticamente</a> (o no). Por ejemplo albaranes, facturas, etc.</p></html>",report);
			tips.add(tipLabel2,BorderLayout.SOUTH);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//tipLabel.setPreferredSize(login.getPreferredSize());
		//tipLabel.setFont(new Font(login.getFont().getName(), Font.PLAIN, login.getFont().getSize()));
		
		this.add(panelLogo/*,BorderLayout.NORTH*/);
		this.add(panelLogin/*,BorderLayout.CENTER*/);
		this.add(panelStatus/*,BorderLayout.SOUTH*/);
	}
	
	public void startProgressBar(String message){
		barraProgreso.setString(Utils.normalizeLabel(message));
		barraProgreso.setIndeterminate(true);
		
		this.validate();
		this.repaint();
	}
	
	public void stopProgressBar(){
		barraProgreso.setString(Utils.normalizeLabel("Identificación de usuario"));
		barraProgreso.setIndeterminate(false);
		
		this.validate();
		this.repaint();
	}
	
	public void changeMessageProgressBar(String message){
		barraProgreso.setString(Utils.normalizeLabel(message));
	}
	
	public void setLicenseDays(int days){
		String message=null;
		if(days>1){
			message=days+" días de licencia";
		}else{
			message="¡Último día de licencia!";
		}
		if(days<=30){
			licenseDaysLabel.setForeground(Color.RED);
		}else{
			licenseDaysLabel.setForeground(Color.BLACK);
		}
		licenseDaysLabel.setText(message);
	}
	
}
