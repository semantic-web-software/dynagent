package calendar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import tasks.ITaskCenter;

import miniCalendar.IDateListener;
import miniCalendar.JCalendar;

public class PanelCalendar extends JPanel {
	
	private static final long serialVersionUID = 1L;
	final int heightMinCal = 190;
	final int widthMinCal = 160;
	private int gap;
	
	private JTabbedPane tabbed;
	private int ancho;
	private int alto;
	private JCalendar jcal;
	private Calendar calendar;
	private Eventos event;
	
	private PanelMonth panelMonth;
	private PanelWeek panelWeek;
	private PanelDay panelDay;

	public PanelCalendar(ITaskCenter taskCenter, Dimension dimension){
		super();
		Constants.itaskCenter=taskCenter;
		Constants.frame=this;
		UIManager.put("Table.background", Color.white);
		//UIManager.put("TabbedPane.contentBorderInsets", new Insets(0,0,0,0));
		
		gap = dimension.width*2/100;
		this.ancho=dimension.width-2*gap;
		this.alto=dimension.height-2*gap;
		calendar=Calendar.getInstance();
		event = new Eventos();
		
		setLayout(new FlowLayout(FlowLayout.CENTER, gap, gap));
		setBorder(BorderFactory.createEmptyBorder());
		setPreferredSize(dimension);
		add(getPrincipal());
		
		//System.err.println("Dim "+dimension+" New "+ancho+" , "+alto);		
	}
	
	private JPanel getPrincipal(){
				
		JPanel jsc = createSmallCalendar();		
		JTabbedPane jbc = createBigCalendar();		
		jsc.setPreferredSize(new Dimension(widthMinCal, heightMinCal));
		jbc.setPreferredSize(new Dimension(ancho-widthMinCal-gap, alto));
		
		JPanel p = new JPanel(new BorderLayout(gap/2, 0));
		p.setBorder(BorderFactory.createEmptyBorder());
		p.add(jsc, BorderLayout.WEST);
		p.add(jbc, BorderLayout.EAST);
				
		return p;
	}
	
	private JPanel createSmallCalendar(){
		
		JPanel cal = new JPanel(new BorderLayout());
		cal.setBorder(BorderFactory.createEmptyBorder());
		
		jcal = new JCalendar(null, null, null, new IDateListener(){
			public void setDateSelectioned(Calendar c,JCalendar calendarComponent) {
				calendar=c;
				actualizaMP();
				actualizaWP();
				actualizaDP();
			}
			public void setAllDate(Calendar c){
				calendar=c;
				actualizaMP();
				actualizaWP();
				actualizaDP();
			}
		},false);
		jcal.setSelectedCalendar(Calendar.getInstance());
		cal.add(jcal, BorderLayout.NORTH);
		
		return cal;
	}
	
	public JTabbedPane createBigCalendar(){	
		
		JPanel cal = new JPanel(new BorderLayout());		
		new DatesBigCalendar(this);
		
		panelMonth = new PanelMonth(ancho-widthMinCal-gap, alto-2*gap, calendar);
		panelWeek = new PanelWeek(ancho-widthMinCal-gap, alto-2*gap, calendar);
		panelDay = new PanelDay(ancho-widthMinCal-gap, alto-2*gap, calendar);
		tabbed = new JTabbedPane();
		tabbed.addTab("  Mes   ", panelMonth);	
		tabbed.addTab(" Semana ", panelWeek);	
		tabbed.addTab("  Dia   ", panelDay);	
		tabbed.setSelectedIndex(0);
		
		cal.add(tabbed, BorderLayout.CENTER);			
		
		return tabbed;
	}
	
	public void actualizaMP(){		
		panelMonth.removeAll();
		PanelMonth mp = new PanelMonth(ancho-widthMinCal-gap, alto-2*gap, calendar);
		panelMonth = mp;
		tabbed.setComponentAt(0, mp);
		tabbed.validate();
		tabbed.repaint();		
	}
	
	public void actualizaWP(){		
		panelWeek.removeAll();
		PanelWeek wp = new PanelWeek(ancho-widthMinCal-gap, alto-2*gap, calendar);
		tabbed.setComponentAt(1, wp);
		tabbed.validate();
		tabbed.repaint();		
	}
	
	public void actualizaDP(){		
		panelDay.removeAll();
		PanelDay dp = new PanelDay(ancho-widthMinCal-gap, alto-2*gap, calendar);
		tabbed.setComponentAt(2, dp);
		tabbed.validate();
		tabbed.repaint();		
	}
	
	public JTabbedPane getTabbed(){
		return tabbed;
	}

	public Eventos getEventos() {
		return event;
	}
	
	public static void main(String args[]){
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel"); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		frame.getContentPane().add(new PanelCalendar(null, new Dimension(1024, 768)));
		frame.setVisible(true);
		frame.setResizable(true);		
		frame.pack();
	}
}