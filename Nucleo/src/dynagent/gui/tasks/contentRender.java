package dynagent.gui.tasks;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;


public class contentRender extends JTextArea implements TableCellRenderer {

	private static final long serialVersionUID = 1L;

	public contentRender(){
		super();
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if(value!=null)
			this.setText(String.valueOf(value));		
		else
			this.setText("");
		return this;
	}

}