package gdev.gawt.tableHeaderRenderer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class MultiLineHeaderRenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = 1L;

	public MultiLineHeaderRenderer() {
		setOpaque(true);
		//setForeground(UIManager.getColor("TableHeader.foreground"));
		//setBackground(UIManager.getColor("TableHeader.background"));
		//setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		setHorizontalAlignment(JLabel.CENTER);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		setFont(table.getFont());
		String str = (value == null) ? "" : value.toString();
		int posToken = str.indexOf(":");
		if (posToken != -1)
			str = "<HTML><TABLE cellpadding=0 vspace=0 cellspacing=0><TR><TC>"
				+ str.substring(0, posToken) + "</TC></TR>" + "<TR><TC>"
				+ str.substring(posToken + 1) + "</TC></TR>"
				+ "</TABLE></HTML>";
		setText(str);
		return this;
	}
}

