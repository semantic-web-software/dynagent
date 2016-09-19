package gdev.gawt.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import dynagent.common.Constants;
import dynagent.common.communication.docServer;
import dynagent.common.utils.IdOperationForm;

public class PopupOptions{
	
	private static final long serialVersionUID = 1L;
	private ArrayList<JMenuItem> buttons;
	private Component comp;
	//private int ANCHO = 90;
	private int ALTO = 21;
	private int margin = 0;
	private JPopupMenu menu;
	private docServer server;
	
	public PopupOptions(ArrayList<JMenuItem> buttons, Component comp, docServer server, ActionListener actionListenerHelp){
		this.buttons=buttons;
		this.comp=comp;
		this.server=server;
		initialize(actionListenerHelp);
	}
	
	public void initialize(ActionListener actionListenerHelp){
		menu = new JPopupMenu();
		Color color=UIManager.getColor("List.background");
		//Creamos un nuevo Color en vez de usarlo porque si no no funciona ya que realmente es un ColorUIResource
		menu.setBackground(new Color(color.getRed(),color.getGreen(),color.getBlue()));
		GridBagLayout gblayout=new GridBagLayout();
		menu.setLayout(gblayout);
		Collections.sort(buttons,new Comparator<JMenuItem>(){
			public int compare(JMenuItem o1, JMenuItem o2) {
				return Constants.languageCollator.compare(o1.getText(),o2.getText());
			}
		});
		for(int i=0;i<buttons.size();i++){
			final JMenuItem b = buttons.get(i);
			//b.setPreferredSize(new Dimension(ancho, ALTO));
			//b.setMaximumSize(new Dimension(ancho, ALTO));
			//b.setBounds(0, ALTO*i, ANCHO, ALTO);
			/*b.addMouseListener(new MouseListener(){
				public void mouseClicked(MouseEvent e){
					System.err.println("Buttonnnn");
					menu.setVisible(false);	
					b.removeMouseListener(this);
					menu.removeAll();
					//menu=null;					
				}
				public void mouseEntered(MouseEvent e){}
				public void mouseExited(MouseEvent e){}
				public void mousePressed(MouseEvent e){}
				public void mouseReleased(MouseEvent e){}				
			});*/
			
			GridBagConstraints gbc=new GridBagConstraints();
						
			gbc.gridy=i;
			gbc.gridx=0;
			
			gblayout.setConstraints(b, gbc);
			
			menu.add(b);
			
			
//			final JButton button=new JButton();
//			
//			int sizeIcon=(int)(b.getPreferredSize().getHeight()*0.85);
//			final ImageIcon icon=server.getIcon(button,"help",sizeIcon,sizeIcon);
//			button.setPreferredSize(new Dimension((int)b.getPreferredSize().getHeight(),(int)b.getPreferredSize().getHeight()));
//			button.setBorderPainted(false);
//			button.setContentAreaFilled(false);
//			button.setFocusPainted(false);
//			button.setBorder(BorderFactory.createEmptyBorder());
//			IdOperationForm operationForm=new IdOperationForm(b.getActionCommand());
//			operationForm.setButtonType(botoneraAccion.HELP);
//			button.setActionCommand(operationForm.getIdString());
//			
//			MouseListener mouseListener=new MouseListener(){
//
//				public void mouseClicked(MouseEvent ev) {}
//				public void mouseEntered(MouseEvent arg0) {
//					button.setIcon(icon);
//					button.repaint();
//				}
//
//				public void mouseExited(MouseEvent arg0) {
//					button.setIcon(null);
//					button.repaint();
//				}
//				public void mousePressed(MouseEvent arg0) {}
//				public void mouseReleased(MouseEvent arg0) {}
//				
//			};
//			b.addMouseListener(mouseListener);
//			button.addMouseListener(mouseListener);
//			
//			if(actionListenerHelp!=null){
//				button.addActionListener(actionListenerHelp);
//			}
//
//			gbc.gridx=1;
//			
//			gblayout.setConstraints(button, gbc);
//			
//			menu.add(button);
		}
		//this.setUndecorated(true);
		//this.setContentPane(panel);
		menu.pack();
		//this.setResizable(false);
	}
	
	public void show(boolean orientationDown){
		menu.show(comp, comp.getPreferredSize().width/2, orientationDown?comp.getPreferredSize().height/2:(comp.getPreferredSize().height/2)-(int)menu.getPreferredSize().getHeight());
		//Seleccionamos el primer button. Lo hacemos en un invokeLater porque hay que esperar a que el menu este visible
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MenuSelectionManager.defaultManager().setSelectedPath(new MenuElement[]{menu, buttons.get(0)}); 
			}
		});
	}
	
	
	/*private Point getPointOnScreen(Component f, Component start){
		Point locationStart=start.getLocationOnScreen();
		Point locationFrame=f.getLocationOnScreen();
		Point p = new Point(locationStart.x+start.getWidth()/2, locationStart.y+start.getHeight()/2);

		//Sobresale por la derecha
		if(p.x+this.getWidth()>locationFrame.x+f.getWidth()){
			p.x=p.x-this.getWidth();
			if(p.x<locationFrame.x)
				p.x=p.x+this.getWidth()/2;
		}
		//Sobresale por la izquierda
		if(p.x<locationFrame.x){
			p.x=p.x+this.getWidth();
			if(p.x+this.getWidth()>locationFrame.x+f.getWidth())
				p.x=p.x-this.getWidth()/2;
		}
		//Sobresale por abajo
		if(p.y+this.getHeight()>locationFrame.y+f.getHeight()){
			p.y=p.y-this.getHeight();
			if(p.y<locationFrame.y)
				p.y=p.y+this.getHeight()/2;
		}
		//Sobresale por arriba
		if(p.y<locationFrame.y){
			p.y=p.y+this.getHeight();
			if(p.y+this.getHeight()>locationFrame.y+f.getHeight())
				p.y=p.y-this.getHeight()/2;
		}

		return p;
	}*/
	
	public JPopupMenu getPopUp(){
		return menu;
	}
	
	public static void main(String args[]){
		JFrame f = new JFrame();		
		f.setResizable(false);
		
		JPanel p = new JPanel();
		final JButton b = new JButton("Report");
		b.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				ArrayList<JMenuItem> labels = new ArrayList<JMenuItem>();
				labels.add(new JMenuItem("Report 1"));
				labels.add(new JMenuItem("Report 2 report"));
				labels.add(new JMenuItem("Report 3"));
				PopupOptions p=new PopupOptions(labels, b, null, null);
				p.show(true);
			}			
		});
		p.add(b);
		
		f.setContentPane(p);		        
        f.setSize(new Dimension(1024,768));
        f.setPreferredSize(new Dimension(1024,768));
        f.setTitle("DynaAps®");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
        f.pack(); 
	}
}
