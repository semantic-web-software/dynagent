package calendar;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

public class JScrollPaneRenderer extends JScrollPane implements TableCellRenderer{	
	private static final long serialVersionUID = 1L;

	public Component getTableCellRendererComponent(JTable table, Object	value, boolean isSelected, boolean hasFocus, int row, int column) {		
		if(value==null)
    		return null;
    	else
    		return (JComponent)value;
	}	
}