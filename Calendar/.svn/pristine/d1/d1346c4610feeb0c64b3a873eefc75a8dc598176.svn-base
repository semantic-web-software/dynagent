package miniCalendar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class JCalendar extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	protected Calendar calendar;
	protected JDayChooser dayChooser;
	protected JMonthChooser monthChooser;
	protected JYearChooser yearChooser;    

	private JPanel monthYearPanel;       
	protected IDateListener listener;

	public static JDialog jc;

	private int ANCHO=180;//160
	private int ALTO=210;//200;
	private JDialog dialog;
	private Component frame, button;
	private int nRows;

	//private Calendar valueCalendar;
	private Calendar init;
	
	protected JPanel closePanel;

	public static final int JCalendar = 1;
	public static final int JCalendarDateTime = 2;
	
	public boolean hasCloseButton;

	public JCalendar(Component frame, Component button, Calendar init, IDateListener listener, boolean hasCloseButton) {


		this.listener = listener;
		this.frame = frame;
		this.button = button;
		this.init=init;

		dayChooser = null;
		monthChooser = null;
		yearChooser = null;

		this.hasCloseButton=hasCloseButton;
		
		if(init==null)
			calendar = Calendar.getInstance();
		else
			calendar=init;

		setLayout(new BorderLayout());

		monthYearPanel = new JPanel();
		monthYearPanel.setLayout(new BorderLayout());

		monthChooser = new JMonthChooser(true);
		yearChooser = new JYearChooser();
		monthChooser.setYearChooser(yearChooser);

		monthYearPanel.add(monthChooser, BorderLayout.WEST);
		monthYearPanel.add(yearChooser, BorderLayout.CENTER);
		monthYearPanel.setBorder(BorderFactory.createEmptyBorder());

		nRows = getNumberOfRows();
		if(init==null)
			dayChooser = new JDayChooser(Calendar.getInstance(Locale.getDefault()), nRows, /*false*/true);
		else
			dayChooser = new JDayChooser(init, nRows, true);

		//dayChooser.addPropertyChangeListener(this);
		monthChooser.setDayChooser(dayChooser);
		//monthChooser.addPropertyChangeListener(this);		
		yearChooser.setDayChooser(dayChooser);
		//yearChooser.addPropertyChangeListener(this);

		add(monthYearPanel, BorderLayout.NORTH);
		add(dayChooser, BorderLayout.CENTER);
		
		if(hasCloseButton){
			JButton closeButton=new JButton("Cerrar");
			closeButton.setMargin(new Insets(0,0,0,0));
			//closeButton.setPre
			closeButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					closeDialog();
				}
			});
			
			closePanel=new JPanel();
			closePanel.add(closeButton);
			add(closePanel,BorderLayout.SOUTH);
		}
		
		setPreferredSize(new Dimension(ANCHO, ALTO));
		setMaximumSize(new Dimension(ANCHO, ALTO));             
		setCalendar(calendar);	
		
		dayChooser.addPropertyChangeListener(this);
		monthChooser.addPropertyChangeListener(this);
		yearChooser.addPropertyChangeListener(this);
		//valueCalendar=init;
	}

	/**
	 * JCalendar is a PropertyChangeListener, for its day, month and year
	 * chooser.
	 *
	 * @param evt the property change event
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (calendar != null) {
			Calendar c = calendar;//(Calendar) calendar.clone();
			//System.err.println("PropertyChange:"+evt.getPropertyName());
			if (evt.getPropertyName().equals("day")) {
				c.set(Calendar.DAY_OF_MONTH, ((Integer) evt.getNewValue()).intValue());
				setCalendar(c, true);
				
				//updateDayChooser(c, true);
				/*if(listener!=null)
					listener.setDateSelectioned(c,this);*/
				if(listener!=null)
					listener.setAllDate(c);    

			}else if (evt.getPropertyName().equals("month")) {

				c.set(Calendar.MONTH, ((Integer) evt.getNewValue()).intValue());	
				
				//Si el dia del mes que ya estaba seleccionado ha hecho que el mes avance al siguiente, volvemos al mes que realmente queriamos
				//Por ejemplo estando seleccionado 31 de Marzo, si ponemos Abril se estaba yendo a 1 de Mayo ya que Abril no tiene 31. Lo que hacemos en ese caso es poner 1 de Abril.
				if(c.get(Calendar.MONTH)!=((Integer) evt.getNewValue()).intValue()){
					c.set(Calendar.MONTH, ((Integer) evt.getNewValue()).intValue());
					c.set(Calendar.DAY_OF_MONTH, 1);
				}
				
				setCalendar(c, true);
				monthChooser.setMonth(c.get(Calendar.MONTH));

				/*if(init!=null && c.get(Calendar.MONTH)==init.get(Calendar.MONTH) && c.get(Calendar.YEAR)==init.get(Calendar.YEAR))		 
					updateDayChooser(init, true);
				else
					updateDayChooser(c, false);*/
				updateDayChooser(c, true);

				if(listener!=null)
					listener.setAllDate(c);

			}else if (evt.getPropertyName().equals("year")) {
				c.set(Calendar.YEAR, ((Integer) evt.getNewValue()).intValue());
				setCalendar(c, true);	

				/*if(init!=null && c.get(Calendar.MONTH)==init.get(Calendar.MONTH) && c.get(Calendar.YEAR)==init.get(Calendar.YEAR))		 
					updateDayChooser(init, true);
				else
					updateDayChooser(c, false);*/
				updateDayChooser(c, true);

				//updateDayChooser(c);
				if(listener!=null)
					listener.setAllDate(c);                
			}else if(evt.getPropertyName().equals("dateTime")){
				//System.err.println("POR FIN Entraaa"+evt.getNewValue());
			}
		}
	}

	protected void updateDayChooser(Calendar c, boolean initial){

		dayChooser.removeAll();
		nRows = getNumberOfRows();
		dayChooser = new JDayChooser(c, nRows, initial);
		dayChooser.addPropertyChangeListener(this);

		monthChooser.setDayChooser(dayChooser);
		monthChooser.setMonth(c.get(Calendar.MONTH));
		yearChooser.setDayChooser(dayChooser);
		yearChooser.setYear(c.get(Calendar.YEAR));
		
		removeAll();
		add(monthYearPanel, BorderLayout.NORTH);
		add(dayChooser, BorderLayout.CENTER);
		if(closePanel!=null){
			add(closePanel,BorderLayout.SOUTH);
		}
		dayChooser.repaint();
		dayChooser.validate();
		repaint();
		validate();
	}

	/**
	 * Sets the background color.
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);

		if (dayChooser != null) {
			dayChooser.setBackground(bg);
		}
	}

	/**
	 * Sets the calendar property. This is a bound property.
	 */
	public void setCalendar(Calendar c) {
		setCalendar(c, true);
	}

	/**
	 * Sets the calendar attribute of the JCalendar object
	 *
	 * @param c the new calendar value
	 * @param update the new calendar value
	 */
	private void setCalendar(Calendar c, boolean update) {
		Calendar oldCalendar = calendar;
		calendar = c;

		if (update) {
			yearChooser.setYear(c.get(Calendar.YEAR));
			monthChooser.setMonth(c.get(Calendar.MONTH));
			dayChooser.setDay(c.get(Calendar.DATE));            
		}
		//valueCalendar=c;
		firePropertyChange("calendar", oldCalendar, calendar);
	}

	public Calendar getCalendar(){
		//return valueCalendar;
		return calendar;
	}

	/**
	 * Enable or disable the JCalendar.
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		if (dayChooser != null) {
			dayChooser.setEnabled(enabled);
			monthChooser.setEnabled(enabled);
			yearChooser.setEnabled(enabled);
		}
	}

	/**
	 * Sets the font property.
	 */
	public void setFont(Font font) {
		super.setFont(font);        

		if (dayChooser != null) {
			dayChooser.setFont(font);
			monthChooser.setFont(font);
			yearChooser.setFont(font);
		}
	}

	/**
	 * Sets the foreground color.
	 */
	public void setForeground(Color fg) {
		super.setForeground(fg);

		if (dayChooser != null) {
			dayChooser.setForeground(fg);
			monthChooser.setForeground(fg);
			yearChooser.setForeground(fg);
		}
	}  

	public int getNumberOfRows() {
		Calendar tmpCalendar = (Calendar) calendar.clone();
		int firstDayOfWeek = tmpCalendar.getFirstDayOfWeek();
		tmpCalendar.set(Calendar.DAY_OF_MONTH, 1);

		int firstDay = tmpCalendar.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek;

		if (firstDay < 0)
			firstDay += 7;

		int i;
		for (i = 0; i < firstDay; i++) {}

		tmpCalendar.add(Calendar.MONTH, 1);

		Date firstDayInNextMonth = tmpCalendar.getTime();
		tmpCalendar.add(Calendar.MONTH, -1);
		Date day = tmpCalendar.getTime();
		int n = 0;

		while (day.before(firstDayInNextMonth)) {
			n++;
			tmpCalendar.add(Calendar.DATE, 1);
			day = tmpCalendar.getTime();
		}
		if(n+i+7<=42)
			return 6;
		else
			return 7;
	}

	// f: panel principal
	// start: componente sobre el que se va a colocar el calendario, generalmente el boton
	private Point getPointOnScreen(Component f, Component start){
		Point locationStart=start.getLocationOnScreen();
		Point locationFrame=f.getLocationOnScreen();
		Point p = new Point(locationStart.x+start.getWidth()/2, locationStart.y+start.getHeight()/2);

		//Sobresale por la derecha
		if(p.x+dialog.getWidth()>locationFrame.x+f.getWidth()){
			p.x=p.x-dialog.getWidth();
			if(p.x<locationFrame.x)
				p.x=p.x+dialog.getWidth()/2;
		}
		//Sobresale por la izquierda
		if(p.x<locationFrame.x){
			p.x=p.x+dialog.getWidth();
			if(p.x+dialog.getWidth()>locationFrame.x+f.getWidth())
				p.x=p.x-dialog.getWidth()/2;
		}
		//Sobresale por abajo
		if(p.y+dialog.getHeight()>locationFrame.y+f.getHeight()){
			p.y=p.y-dialog.getHeight();
			if(p.y<locationFrame.y)
				p.y=p.y+dialog.getHeight()/2;
		}
		//Sobresale por arriba
		if(p.y<locationFrame.y){
			p.y=p.y+dialog.getHeight();
			if(p.y+dialog.getHeight()>locationFrame.y+f.getHeight())
				p.y=p.y-dialog.getHeight()/2;
		}

		return p;
	}

	public JPanel getPanel() {
		return this;
	}

	public void notSelect(){
		dayChooser.notSelect();
		//valueCalendar=null;
	}

	public JDialog buildDialog() {
		dialog = new JDialog();
		dialog.addWindowListener(new WindowListener(){
			public void windowDeactivated(WindowEvent arg0){
				closeDialog();
			}
			public void windowActivated(WindowEvent arg0){}
			public void windowDeiconified(WindowEvent arg0){}
			public void windowIconified(WindowEvent arg0){}
			public void windowOpened(WindowEvent arg0){}
			public void windowClosed(WindowEvent arg0){}
			public void windowClosing(WindowEvent arg0){}
		});
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(ANCHO+10, ALTO+10));
		p.setLayout(null);
		this.setBounds(5, 5, this.getPreferredSize().width, this.getPreferredSize().height);
		p.add(this);
		p.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		
		dialog.setUndecorated(true);
		dialog.setContentPane(p);
		dialog.pack();	
		dialog.setLocation(getPointOnScreen(frame, button));
		dialog.setResizable(false);
		//dialog.setVisible(true);    	
		return dialog;
	}

	public void setSelectedCalendar(Calendar c){
		init = c;
		
		dayChooser.removeAll();
		nRows = getNumberOfRows();
		dayChooser = new JDayChooser(c, nRows, true);
		dayChooser.addPropertyChangeListener(this);

		monthChooser.setDayChooser(dayChooser);
		monthChooser.setMonth(c.get(Calendar.MONTH));
		yearChooser.setDayChooser(dayChooser);
		yearChooser.setYear(c.get(Calendar.YEAR));
		
		dayChooser.setFocusable(false);
		monthChooser.setFocusable(false);
		yearChooser.setFocusable(false);

		removeAll();
		add(monthYearPanel, BorderLayout.NORTH);
		add(dayChooser, BorderLayout.CENTER);
		dayChooser.repaint();
		dayChooser.validate();
	}

	public IDateListener getListener() {
		return listener;
	}

	public void setListener(IDateListener listener) {
		this.listener = listener;
	}

	public JDialog getDialog(){
		return dialog;
	}
	
	public JCalendar getThis(){
		return this;
	}
	
	public void closeDialog(){
		if(listener!=null){
			//if(calendarDateTime!=null){
				//setDate(calendar);						
				listener.setDateSelectioned(calendar,getThis());						
			/*}*//*else{
				listener.setDateSelectioned(calendar,getThis());
				System.err.println("2 "+calendar.getTime());
			}*/
		}
		dialog.dispose();
	}
}
