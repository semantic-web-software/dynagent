package gdev.gawt.utils;

import gdev.gen.GConfigView;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSeparatorUI;

/**
 * This class implements a scrollable Popup Menu
 * @author balajihe
 *
 */
public class FinderPopUp extends JPopupMenu implements MouseListener {
	private static final long	serialVersionUID	= 1;
	private JPanel				panelMenus			= new JPanel();
	private ArrayList<ButtonPopup>	buttons			= new ArrayList<ButtonPopup>();
	private JScrollPane			scroll				= null;
	private JComponent			parent				= null;
	//public static final Icon EMPTY_IMAGE_ICON = new ImageIcon("menu_spacer.gif");
	private int selected = -1;
	public static final int numberOfResultsVisible = 5;
	private int /*ALTO = 16,*/ ANCHO;
	private boolean allowedNotSelection;

	public FinderPopUp(JComponent parent) {
		super();
		allowedNotSelection=false;
		this.parent = parent;
		this.setLayout(new BorderLayout(0,0));
		panelMenus.setBorder(BorderFactory.createEmptyBorder());
		panelMenus.setLayout(new GridLayout(0, 1, 0, 0));
		panelMenus.setBackground(UIManager.getColor("PopupMenu.background"));
		//		panelMenus.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		init(parent);

	}

	private void init(JComponent parent) {
		super.removeAll();
		scroll = new JScrollPane();
		scroll.setViewportView(panelMenus);
		scroll.getVerticalScrollBar().setUnitIncrement(GConfigView.IncrementScrollVertical);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setWheelScrollingEnabled(true);

		/*scroll.setMaximumSize(new Dimension(scroll.getMaximumSize().width, this.getToolkit().getScreenSize().height
				- this.getToolkit().getScreenInsets(parent.getGraphicsConfiguration()).top
				- this.getToolkit().getScreenInsets(parent.getGraphicsConfiguration()).bottom - 4));*/
		
		/*add(scroll);
		pack();
		
		Point p = new Point(comp.getLocationOnScreen().x, comp.getLocationOnScreen().y+comp.getPreferredSize().height);
		setLocation(p);			
		
		setVisible(true);*/
		super.add(scroll, BorderLayout.CENTER);
		//		super.add(scroll);
	}

	public void show(Component invoker, int x, int y, boolean selected) {
		init(parent);
		//        this.pack();
		panelMenus.validate();
		int maxsize = scroll.getMaximumSize().height;
		int realsize = panelMenus.getPreferredSize().height;

		panelMenus.setPreferredSize(new Dimension(ANCHO, panelMenus.getPreferredSize().height));
		panelMenus.setMinimumSize(new Dimension(ANCHO, panelMenus.getPreferredSize().height));	
		
		int ancho=ANCHO;
		int alto=(int)panelMenus.getPreferredSize().getHeight();
		if(buttons.size()>numberOfResultsVisible){
			ancho+=scroll.getVerticalScrollBar().getPreferredSize().width;
			alto=(alto/buttons.size())*numberOfResultsVisible;
		}
		if(ancho<invoker.getWidth())
			ancho=invoker.getWidth();
		
		/*int sizescroll = 0;

		if (maxsize < realsize) {
			sizescroll = scroll.getVerticalScrollBar().getPreferredSize().width;
		}*/
		/*scroll.setPreferredSize(new Dimension(scroll.getPreferredSize().width + sizescroll + 20, scroll
				.getPreferredSize().height));*/
		
		scroll.setPreferredSize(new Dimension(ancho, alto));
		//this.setPopupSize(new Dimension(scroll.getPreferredSize().width + 8, scroll.getPreferredSize().height));
		
		this.pack();
		//this.setInvoker(invoker);
//		if (sizescroll != 0) {
//			//Set popup size only if scrollbar is visible
//			this.setPopupSize(new Dimension(scroll.getPreferredSize().width + 20, scroll.getMaximumSize().height - 20));
//		}
		//        this.setMaximumSize(scroll.getMaximumSize());
		Point invokerOrigin = invoker.getLocationOnScreen();
		//this.setLocation((int) invokerOrigin.getX() + x, (int) invokerOrigin.getY() + y);
		if(selected)
			setSelectedButton(0);
		allowedNotSelection=!selected;
		//this.setVisible(true);
		show(invoker, x, y);
	}

	public void hidemenu() {
		if (this.isVisible()) {
			this.setVisible(false);
			this.repaint();
		}
	}

	public void add(ButtonPopup menuItem) {
		//		menuItem.setMargin(new Insets(0, 20, 0 , 0));
		if (menuItem == null) {
			return;
		}
		panelMenus.add(menuItem);
		buttons.add(menuItem);
		//menuItem.removeActionListener(this);
		//menuItem.addActionListener(this);
		menuItem.addMouseListener(this);
		/*if (menuItem.getIcon() == null) {
			menuItem.setIcon(EMPTY_IMAGE_ICON);
		}*/
		
		if(menuItem.getPreferredSize().width>ANCHO)
			ANCHO = menuItem.getPreferredSize().width;
		/*if (!(menuItem instanceof XCheckedButton)) {
			System.out.println(menuItem.getName());
		}*/
	}
	
	public void setSelectedButton(int index){
		if(index!=selected){
			if(selected!=-1)
				buttons.get(selected).setSelected(false);
			
			if(index!=-1)
				buttons.get(index).setSelected(true);
			selected=index;
		}
	}
	
	public void setSelectedNextButton(){
		
		if(selected+1<buttons.size()){
			setVisibleButton(buttons.get(selected+1));
			setSelectedButton(selected+1);
		}else{
			setVisibleButton(buttons.get(0));
			if(allowedNotSelection && selected!=-1)
				setSelectedButton(-1);
			else setSelectedButton(0);
		}
	}
	
	public void setSelectedPreviousButton(){
		if(selected-1>=0){
			setVisibleButton(buttons.get(selected-1));
			setSelectedButton(selected-1);
		}else{
			setVisibleButton(buttons.get(buttons.size()-1));
			if(allowedNotSelection && selected!=-1)
				setSelectedButton(-1);
			else setSelectedButton(buttons.size()-1);
		}
	}

	public Integer getSelected() {
		return selected;
	}
	
	public ButtonPopup getSelectedButton() {
		if(selected==-1)
			return null;
		return buttons.get(selected);
	}
	
	private void setVisibleButton(ButtonPopup button){
		//System.err.println("ViewPort "+scroll.getViewport().getViewRect()+" "+button.getLocation());
		if(!scroll.getViewport().getViewRect().contains(button.getLocation())){
			scroll.getViewport().setViewPosition(button.getLocation());	
		}
	}

	public void addSeparator() {
		panelMenus.add(new XSeperator());
	}

	public Component[] getComponents() {
		return panelMenus.getComponents();
	}

	public void mouseClicked(MouseEvent arg0) {
		this.hidemenu();
	}

	public void mouseEntered(MouseEvent ev) {
		setSelectedButton(buttons.indexOf((ButtonPopup)ev.getSource()));
	}

	public void mouseExited(MouseEvent ev) {}

	public void mousePressed(MouseEvent arg0) {}

	public void mouseReleased(MouseEvent arg0) {}
	
	
	public static void main(String args[]){
		
		JFrame f = new JFrame();		
		
		try {
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			//UIManager.setLookAndFeel( new com.nilo.plaf.nimrod.NimRODLookAndFeel());
			//UIManager.setLookAndFeel("com.pagosoft.plaf.PgsLookAndFeel");
			//UIManager.setLookAndFeel("org.jvnet.substance.SubstanceLookAndFeel");
			UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel"); 
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		f.getRootPane().putClientProperty("defeatSystemEventQueueCheck",
				Boolean.TRUE);
		
		f.setResizable(false);
		
		JPanel p = new JPanel();
		final JButton b = new JButton("Report");
		b.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				final FinderPopUp finder=new FinderPopUp(b);
				ArrayList<ButtonPopup> labels = new ArrayList<ButtonPopup>();
				finder.add(new ButtonPopup("Report 1"));
				finder.add(new ButtonPopup("Report 2 report"));
				finder.add(new ButtonPopup("Report 3"));
				finder.add(new ButtonPopup("Report 4"));
				finder.add(new ButtonPopup("Report 5"));
				finder.add(new ButtonPopup("Report 6"));
				finder.add(new ButtonPopup("Report 7"));
				finder.show(b,0,0);
				//new FinderPopUp(labels, b);
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
	
	private static class XSeperator extends JSeparator {
		XSeperator() {
			ComponentUI ui = XBasicSeparatorUI.createUI(this);
			XSeperator.this.setUI(ui);
		}

		private static class XBasicSeparatorUI extends BasicSeparatorUI {

			public static ComponentUI createUI(JComponent c) {
				return new XBasicSeparatorUI();
			}

			public void paint(Graphics g, JComponent c) {
				Dimension s = c.getSize();

				if (((JSeparator) c).getOrientation() == JSeparator.VERTICAL) {
					g.setColor(c.getForeground());
					g.drawLine(0, 0, 0, s.height);

					g.setColor(c.getBackground());
					g.drawLine(1, 0, 1, s.height);
				} else // HORIZONTAL
				{
					g.setColor(c.getForeground());
					g.drawLine(0, 7, s.width, 7);

					g.setColor(c.getBackground());
					g.drawLine(0, 8, s.width, 8);
				}
			}
		}
	}

}
