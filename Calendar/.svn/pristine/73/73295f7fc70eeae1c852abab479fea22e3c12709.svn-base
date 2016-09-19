package miniCalendar;


import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JFrame;


/**
 * JYearChooser is a bean for choosing a year.
 *
 * @author Kai Toedter
 * @version 1.2
 */
public class JYearChooser extends JSpinField {
    
	private static final long serialVersionUID = 1L;
	protected JDayChooser dayChooser;

    /**
     * Default JCalendar constructor.
     */
    public JYearChooser() {
        Calendar calendar = Calendar.getInstance();
        dayChooser = null;
        setMinimum(calendar.getMinimum(Calendar.YEAR));
        setMaximum(calendar.getMaximum(Calendar.YEAR));
        setValue(calendar.get(Calendar.YEAR));
        setBorder(BorderFactory.createEmptyBorder());
    }

    /**
     * Sets the year. This is a bound property.
     */
    public void setYear(int y) {
        int oldYear = getValue();
        super.setValue(y, true, false);

        if (dayChooser != null)
            dayChooser.setYear(value);        

        spinner.setValue(new Integer(value));
        spinner.setBorder(BorderFactory.createEmptyBorder());
        firePropertyChange("year", oldYear, value);
    }

    /**
     * Sets the year value.
     */
    public void setValue(int value) {
        setYear(value);
    }

    /**
     * Returns the year.
     */
    public int getYear() {
        return super.getValue();
    }

    /**
     * Convenience method set a day chooser that might be updated directly.
     */
    public void setDayChooser(JDayChooser dayChooser) {
        this.dayChooser = dayChooser;
    }

    /**
     * Creates a JFrame with a JYearChooser inside and can be used for testing.
     *
     * @param s command line arguments
     */
    static public void main(String[] s) {
        JFrame frame = new JFrame("JYearChooser");
        frame.getContentPane().add(new JYearChooser());
        frame.pack();
        frame.setVisible(true);
    }
}
