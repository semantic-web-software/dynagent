package calendar;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;
  
class RowHeaderRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 1L;

	public RowHeaderRenderer(JTable table) {
		JTableHeader header = table.getTableHeader();
		setOpaque(true);
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0.30f, 0.45f, 0.7f)));
		//setBorder(BorderFactory.createEmptyBorder());//UIManager.getBorder("TableHeader.cellBorder"));
		setHorizontalAlignment(CENTER);
		setForeground(header.getForeground());
		setBackground(header.getBackground());
		setFont(header.getFont());
	}

	public Component getListCellRendererComponent( JList list, 
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		setText((value == null) ? "" : value.toString());
		return this;
	}
}
