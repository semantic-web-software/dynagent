package calendar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel; 

public class Dialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final int margin = 7;

	public Dialog(int ancho, int alto, JButton[] a, JButton point){

		JPanel p=new JPanel();		
		p.setLayout(null);
		
		for(int i=0;i<a.length;i++){
			a[i].setBounds(0, i*a[i].getPreferredSize().height, ancho, a[i].getPreferredSize().height);
			p.add(a[i]);			
		}	
		        
        p.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        p.setSize(new Dimension(ancho, alto));
        p.setPreferredSize(new Dimension(ancho, alto));
		        
        addWindowListener(new WindowListener(){
			public void windowDeactivated(WindowEvent arg0){
				dispose();
			}
			public void windowActivated(WindowEvent arg0){}
			public void windowDeiconified(WindowEvent arg0){}
			public void windowIconified(WindowEvent arg0){}
			public void windowOpened(WindowEvent arg0){}
			public void windowClosed(WindowEvent arg0){}
			public void windowClosing(WindowEvent arg0){}
		});

        JPanel pBorder = new JPanel();
		pBorder.setLayout(null);
		pBorder.setSize(new Dimension(ancho+margin*2, alto+margin*2));
		pBorder.setPreferredSize(new Dimension(ancho+margin*2, alto+margin*2));
		p.setBounds(margin, margin, ancho, alto);
		pBorder.add(p);
		//pBorder.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		
		
		setUndecorated(true);		
		setContentPane(pBorder);
		pack();	
		//setLocation((int)point.getX(), (int)point.getY()-alto);
		setLocation(getPointOnScreen(Constants.frame, point));
		setResizable(false);
		setVisible(true);

	}
	
	private Point getPointOnScreen(Component f, Component start){
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
	 }
}
