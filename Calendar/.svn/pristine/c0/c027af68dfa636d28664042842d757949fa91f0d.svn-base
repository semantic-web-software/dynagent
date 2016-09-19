package miniCalendar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class JDayChooser extends JPanel implements ActionListener, KeyListener, FocusListener {
    
	private static final long serialVersionUID = 1L;
	protected JButton[] days;    
    protected JPanel dayPanel;
    protected int day;
    protected Color oldDayBackgroundColor;
    protected Color sundayForeground;
    protected Color selectedColor;
    protected Color weekdayForeground;
    protected Color decorationBackgroundColor;
    protected String[] dayNames = {"","D","L","M","X","J","V","S"};
    protected Calendar calendar;
    protected Calendar today;
    Integer selected=null;
    int nRows;
    boolean initialSelected;	
    
    /**
     * JDayChooser constructor.
     *
     * @param weekOfYearVisible true, if the weeks of a year shall be shown
     */
    public JDayChooser(Calendar init, int nRows, boolean initialSelected) {
    	//setBackground(Color.blue);
        calendar = init;
        this.nRows=nRows;
        this.initialSelected=initialSelected;        
        today = (Calendar)init.clone();       

        setLayout(new BorderLayout());
        
        dayPanel = new JPanel();
        dayPanel.setLayout(new GridLayout(nRows, 7));
        days = new JButton[nRows*7];        
        
        sundayForeground = new Color(164, 0, 0);
        weekdayForeground = new Color(0, 90, 164);
        
        decorationBackgroundColor = new Color(210, 228, 238);

        for (int y = 0; y < nRows; y++) {
            for (int x = 0; x < 7; x++) {
                int index = x + (7 * y);

                if (y == 0) {
                    // Create a button that doesn't react on clicks or focus changes.
                    days[index] = new JButton() {
						private static final long serialVersionUID = 1L;
						public void addMouseListener(MouseListener l){}						
						public boolean isFocusable() {
							return false;
						}
						
					};                    
                    days[index].setContentAreaFilled(false);
                    days[index].setBorderPainted(true);
                    days[index].setBorder(BorderFactory.createEmptyBorder());
                    //days[index].setBackground(decorationBackgroundColor);
                    days[index].setFont(new Font("Dialog", Font.PLAIN, 11));
                    //dayPanel.add(days[index]);
                } else {
                    days[index] = new JButton("x"){                    	
						private static final long serialVersionUID = 1L;
						public Border getBorder() {							
							return BorderFactory.createRaisedBevelBorder();
						}
                    };
                    //days[index].setBorder(BorderFactory.createLineBorder(new JButton().getBackground(), 1));
                    days[index].addActionListener(this);
                    days[index].addKeyListener(this);
                    days[index].addFocusListener(this);
                }

                days[index].setMargin(new Insets(0, 0, 0, 0));
                days[index].setFocusPainted(false);
                dayPanel.add(days[index]);
            }
        }
        init();
        //this.day = calendar.get(Calendar.DAY_OF_MONTH);
        //setDay(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        add(dayPanel, BorderLayout.CENTER);         
    }

    /**
     * Initilizes the locale specific names for the days of the week.
     */
    protected void init() {
    	selectedColor = new Color(160, 160, 160);
        JButton testButton = new JButton();
        oldDayBackgroundColor = testButton.getBackground();
        
        Date date = calendar.getTime();
        calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTime(date);

        drawDayNames();
		String dateString=Integer.toString(today.get(Calendar.DATE));
        for (int i = 7; i < nRows*7; i++) {
        	if (days[i].getText().equals(dateString))
        			selected=i;
        }
        drawDays();
    }

    /**
     * Draws the day names of the day columnes.
     */
    private void drawDayNames() {
        int firstDayOfWeek = calendar.getFirstDayOfWeek();
        //DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
        //dayNames = dateFormatSymbols.getShortWeekdays();

        int day = firstDayOfWeek;

        for (int i = 0; i < 7; i++) {
            days[i].setText(dayNames[day]);

            if (day == 1) 
                days[i].setForeground(sundayForeground);
            else
                days[i].setForeground(weekdayForeground);
           

            if (day < 7)
                day++;
            else
                day -= 6;         
        }
    }

    /**
     * Hides and shows the day buttons.
     */
    protected void drawDays() {
        Calendar tmpCalendar = (Calendar) calendar.clone();
        int firstDayOfWeek = tmpCalendar.getFirstDayOfWeek();
        tmpCalendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDay = tmpCalendar.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek;

        if (firstDay < 0)
            firstDay += 7;
        
        int i;
        for (i = 0; i < firstDay; i++) {
            days[i + 7].setVisible(false);
            days[i + 7].setText("");
            //dayPanel.add(days[i + 7]);
        }

        tmpCalendar.add(Calendar.MONTH, 1);

        Date firstDayInNextMonth = tmpCalendar.getTime();
        tmpCalendar.add(Calendar.MONTH, -1);

        Date day = tmpCalendar.getTime();
        int n = 0;
        Color foregroundColor = getForeground();

        while (day.before(firstDayInNextMonth)) {
        	if(i+n+7<nRows*7){
        		days[i + n + 7].setText(Integer.toString(n + 1));
        		days[i + n + 7].setVisible(true);
        		//dayPanel.add(days[i + n + 7]);
        		//days[i + n + 7].setFont(new Font("Dialog", Font.PLAIN, 10));                

        		if ((tmpCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) && (tmpCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR))){
        			if ((tmpCalendar.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) && (tmpCalendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)))
        				days[i + n + 7].setForeground(sundayForeground); 
        			if(initialSelected){
        				
        				days[i + n + 7].setEnabled(false); 
        				selected = i + n + 7;
        				initialSelected=false;
        			}
        			//days[i + n + 7].setBackground(selectedColor); 
        		}else
        			days[i + n + 7].setForeground(foregroundColor);        		
        	}
            n++;
            tmpCalendar.add(Calendar.DATE, 1);
            day = tmpCalendar.getTime();
        }
        
        for (int k = n + i + 7; k < nRows*7; k++) {
            days[k].setVisible(false);
            days[k].setText("");
        }
    }

    /**
     * Sets the day. This is a bound property.
     */
    public void setDay(int d) {
    	       
        if (d < 1) 
            d = 1;

        Calendar tmpCalendar = (Calendar) calendar.clone();
        tmpCalendar.set(Calendar.DAY_OF_MONTH, 1);
        tmpCalendar.add(Calendar.MONTH, 1);
        tmpCalendar.add(Calendar.DATE, -1);

        int maxDaysInMonth = tmpCalendar.get(Calendar.DATE);

        if (d > maxDaysInMonth)
            d = maxDaysInMonth;  
        
        int oldDay = day;
        day = d;
        
        for (int i = 7; i < nRows*7; i++) {        	
        	days[i].setEnabled(true);
			if (days[i].getText().equals(Integer.toString(day))) {				
				selected=i;
				days[selected].setEnabled(false);			
			}
        }        
        firePropertyChange("day", oldDay, day);
    }

    /**
     * Returns the selected day.
     */
    public int getDay() {
        return day;
    }

    /**
     * Sets a specific month. This is needed for correct graphical
     * representation of the days.
     *
     * @param month the new month
     */
    public void setMonth(int month) {
        calendar.set(Calendar.MONTH, month);
        drawDays();        
    }

    /**
     * Sets a specific year. This is needed for correct graphical
     * representation of the days.
     *
     * @param year the new year
     */
    public void setYear(int year) {
        calendar.set(Calendar.YEAR, year);
        drawDays();        
    }

    /**
     * Sets a specific calendar. This is needed for correct graphical
     * representation of the days.
     *
     * @param calendar the new calendar
     */
    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
        drawDays();
    }

    /**
     * Sets the font property.
     *
     * @param font the new font
     */
    public void setFont(Font font) {
    	if (days != null)
            for (int i = 0; i < 49; i++)
                days[i].setFont(font);           
    }

    /**
     * Sets the foregroundColor color.
     *
     * @param foreground the new foregroundColor
     */
    public void setForeground(Color foreground) {
        super.setForeground(foreground);

        if (days != null) {
            for (int i = 7; i < 49; i++)
                days[i].setForeground(foreground);            

            drawDays();
        }
    }

    /**
     * JDayChooser is the ActionListener for all day buttons.
     *
     * @param e the ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
    	JButton button = (JButton) e.getSource();
        String buttonText = button.getText();
        int day = new Integer(buttonText).intValue();
        setDay(day);
    }

    /**
     * JDayChooser is the FocusListener for all day buttons. 
     */
    public void focusGained(FocusEvent e) {
    }

    /**
     * Does nothing.
     */
    public void focusLost(FocusEvent e) {
    }

    /**
     * JDayChooser is the KeyListener for all day buttons. (Added by Thomas
     * Schaefer and modified by Austin Moore)
     *
     * @param e the KeyEvent
     */
    public void keyPressed(KeyEvent e) {
        int offset = (e.getKeyCode() == KeyEvent.VK_UP) ? (-7)
                                                        : ((e.getKeyCode() == KeyEvent.VK_DOWN)
            ? (+7)
            : ((e.getKeyCode() == KeyEvent.VK_LEFT) ? (-1)
                                                    : ((e.getKeyCode() == KeyEvent.VK_RIGHT)
            ? (+1) : 0)));

        int newDay = getDay() + offset;

        if ((newDay >= 1) &&
                (newDay <= calendar.getMaximum(Calendar.DAY_OF_MONTH))) {
            setDay(newDay);
        }
    }

    /**
     * Does nothing.
     */
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Does nothing.
     */
    public void keyReleased(KeyEvent e) {
    }

    /**
     * Enable or disable the JDayChooser.
     *
     * @param enabled The new enabled value
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        for (short i = 0; i < days.length; i++) {
            if (days[i] != null) {
                days[i].setEnabled(enabled);
            }
        }        
    }
    
    public void notSelect(){
    	days[selected].setEnabled(true);
    }
    
    public void setSelect(){
    	days[selected].setEnabled(true);
    }

	public void setInitialSelected(boolean initialSelected) {
		this.initialSelected = initialSelected;
	}
}
