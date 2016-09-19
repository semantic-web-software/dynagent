package dynagent.framework.componentes;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import dynagent.framework.ConstantesGraficas;
import dynagent.framework.utilidades.CreadorIconos;

public class BotonScroll extends JPanel implements MouseWheelListener{
	
	private static final long serialVersionUID = 1L;
	
	private double scrollHeight;
	private double alto;
	
	private JComponent menu;
	private JButton down, up;
	private JScrollPane scroll;
	
	private final int heightValue = 100;
	private int value = 0;
		
	public BotonScroll(JComponent menu, double alto){
		super();
		this.menu=menu;
		this.alto=alto;
		
		ImageIcon iconUp = CreadorIconos.crearIcono("imagenes/arrow_up_20_20.gif"/*Up16.gif"*/,new Dimension(16, 16));
		up = new JButton(iconUp);
		up.setBorderPainted(false);
		up.setContentAreaFilled(false);
		up.setMargin(new Insets(0,0,0,0));
		up.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				upScroll();
			}			
		});
		
		ImageIcon iconDown = CreadorIconos.crearIcono("imagenes/arrow_down_20_20.gif"/*Down16.gif"*/,new Dimension(16, 16));
		down = new JButton(iconDown);
		down.setBorderPainted(false);
		down.setContentAreaFilled(false);
		down.setMargin(new Insets(0,0,0,0));
		down.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				downScroll();
			}			
		});
		
		scroll = new JScrollPane(menu, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setUnitIncrement(ConstantesGraficas.IncrementScrollVertical);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		
		setBorder(BorderFactory.createLineBorder(Color.gray));
		setBackground(menu.getBackground());
		setLayout(new BorderLayout());
		add(up, BorderLayout.NORTH);
		add(scroll, BorderLayout.CENTER);
		add(down, BorderLayout.SOUTH);
		//addMouseWheelListener(this);			
		menu.addMouseWheelListener(this);		
		
		double botonHeight = down.getPreferredSize().getHeight();
		scrollHeight = alto-(2*botonHeight);
		up.setEnabled(false);
		
	}
	
	private void upScroll(){

		if(value - heightValue>=0){
			value -= heightValue;
			Point p = new Point(0,value);
			scroll.getViewport().setViewPosition(p);
			scroll.validate();
			scroll.repaint();
			down.setEnabled(true);
			if(value==0)
				up.setEnabled(false);
		}
	}
	
	private void downScroll(){

		if((value + scrollHeight)<=menu.getPreferredSize().getHeight()){
			value += heightValue;
			Point p = new Point(0,value);
			scroll.getViewport().setViewPosition(p);
			scroll.validate();
			scroll.repaint();
			up.setEnabled(true);
			if((value + scrollHeight)>menu.getPreferredSize().getHeight())
				down.setEnabled(false);
		}
	}	

	public void paint(Graphics g) {		
		if(alto>=menu.getPreferredSize().getHeight()){
			down.setVisible(false);
			up.setVisible(false);
		}
		super.paint(g);
	}

	public void mouseWheelMoved(MouseWheelEvent e) {		
		int notches = e.getWheelRotation();
		if (notches < 0) {
			upScroll();
		} else {
			downScroll();
		}		
	}
	
	/*public static void main(String args[]){
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BotonScroll bs = new BotonScroll(getPanel(), 720);
		bs.setPreferredSize(new Dimension(720, 100));
		bs.setSize(new Dimension(720, 100));
		bs.setMaximumSize(new Dimension(720, 100));
		f.setContentPane(bs);
		f.setSize(1024, 768);
		f.setSize(new Dimension(1024, 768));
		f.setPreferredSize(new Dimension(1024, 768));
		f.pack();		
		f.setVisible(true);		
	}
	
	public static JPanel getPanel(){
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setSize(new Dimension(100, 368*4));
		panel.setPreferredSize(new Dimension(100, 368*4));
		
		JButton b1 = new JButton("b1");
		JButton b2 = new JButton("b2");
		JButton b3 = new JButton("b3");
		JButton b4 = new JButton("b4");
		
		b1.setBounds(0, 0, 100, 368);
		b2.setBounds(0, 1*368, 100, 368);
		b3.setBounds(0, 2*368, 100, 368);
		b4.setBounds(0, 3*368, 100, 368);
		
		panel.add(b1);
		panel.add(b2);
		panel.add(b3);
		panel.add(b4);
		
		return panel;
	}*/
}
