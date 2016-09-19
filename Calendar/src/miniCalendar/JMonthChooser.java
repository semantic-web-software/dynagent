package miniCalendar;


import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * JMonthChooser is a bean for choosing a month.
 * 
 * @author Kai Toedter
 * @version 1.2
 */
public class JMonthChooser extends JPanel implements ItemListener, ChangeListener {
	
	private static final long serialVersionUID = 1L;
	protected boolean hasSpinner;
	private int month;
	private int oldSpinnerValue = 0;

	private JDayChooser dayChooser;
	private JYearChooser yearChooser;
	private JComboBox comboBox;
	private JSpinner spinner;
	private boolean initialized;
	private boolean localInitialize;

	/**
	 * JMonthChooser constructor with month spinner parameter.
	 * 
	 * @param hasSpinner
	 *            true, if the month chooser should have a spinner component
	 */
	public JMonthChooser(boolean hasSpinner) {
		super();

		this.hasSpinner = hasSpinner;

		setLayout(new BorderLayout());

		comboBox = new JComboBox();
		comboBox.addItemListener(this);

		// comboBox.addPopupMenuListener(this);
		initNames();

		if (hasSpinner) {
			spinner = new JSpinner();
			spinner.addChangeListener(this);
			comboBox.setBorder(new EmptyBorder(0, 0, 0, 0));
			spinner.setEditor(comboBox);
			spinner.setBorder(BorderFactory.createEmptyBorder());
			add(spinner, BorderLayout.WEST);
		} else {
			add(comboBox, BorderLayout.WEST);
		}

		initialized = true;
		setMonth(Calendar.getInstance().get(Calendar.MONTH));
	}

	/**
	 * Initializes the locale specific month names.
	 */
	public void initNames() {
		localInitialize = true;

		DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(Locale.getDefault());
		String[] monthNames = dateFormatSymbols.getMonths();

		if (comboBox.getItemCount() == 12) {
			comboBox.removeAllItems();
		}

		for (int i = 0; i < 12; i++) {
			comboBox.addItem(monthNames[i]);
		}

		localInitialize = false;
		comboBox.setSelectedIndex(month);
	}

	/**
	 * Is invoked if the state of the spnner changes.
	 * 
	 * @param e
	 *            the change event.
	 */
	public void stateChanged(ChangeEvent e) {
		SpinnerNumberModel model = (SpinnerNumberModel) ((JSpinner) e.getSource()).getModel();
		int value = model.getNumber().intValue();
		boolean increase = (value > oldSpinnerValue) ? true : false;
		oldSpinnerValue = value;

		int month = getMonth();

		if (increase) {
			month += 1;

			if (month == 12) {
				month = 0;

				if (yearChooser != null) {
					int year = yearChooser.getYear();
					year += 1;
					yearChooser.setYear(year);
				}
			}
		} else {
			month -= 1;

			if (month == -1) {
				month = 11;

				if (yearChooser != null) {
					int year = yearChooser.getYear();
					year -= 1;
					yearChooser.setYear(year);
				}
			}
		}

		setMonth(month);
	}

	/**
	 * The ItemListener for the months.
	 * 
	 * @param e
	 *            the item event
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			int index = comboBox.getSelectedIndex();

			if ((index >= 0) && (index != month)) {
				setMonth(index, false);			
			}
		}
	}

	/**
	 * Sets the month attribute of the JMonthChooser object. Fires a property change "month".
	 * 
	 * @param newMonth
	 *            the new month value
	 * @param select
	 *            true, if the month should be selcted in the combo box.
	 */
	private void setMonth(int newMonth, boolean select) {
		if (!initialized || localInitialize) {
			return;
		}

		int oldMonth = month;
		month = newMonth;

		if (select) {
			comboBox.setSelectedIndex(month);
		}

		if (dayChooser != null) {
			dayChooser.setMonth(month);
		}

		firePropertyChange("month", oldMonth, month);
	}

	/**
	 * Sets the month. This is a bound property.
	 * 
	 * @param newMonth
	 *            the new month value
	 * 
	 * @see #getMonth
	 */
	public void setMonth(int newMonth) {
	    if(newMonth < 0 || newMonth> 11)
	        return;
		setMonth(newMonth, true);
	}

	/**
	 * Returns the month.
	 * 
	 * @return the month value
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * Convenience method set a day chooser.
	 * 
	 * @param dayChooser
	 *            the day chooser
	 */
	public void setDayChooser(JDayChooser dayChooser) {
		this.dayChooser = dayChooser;
	}

	/**
	 * Convenience method set a year chooser. If set, the spin for the month buttons will spin
	 * the year as well
	 * 
	 * @param yearChooser
	 *            the new yearChooser value
	 */
	public void setYearChooser(JYearChooser yearChooser) {
		this.yearChooser = yearChooser;
	}

	/**
	 * Enable or disable the JMonthChooser.
	 * 
	 * @param enabled
	 *            the new enabled value
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		comboBox.setEnabled(enabled);

		if (spinner != null) {
			spinner.setEnabled(enabled);
		}
	}
}