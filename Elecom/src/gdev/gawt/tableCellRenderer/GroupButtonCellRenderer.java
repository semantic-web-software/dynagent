package gdev.gawt.tableCellRenderer;

import gdev.gawt.GTable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.lang.Boolean;
import javax.swing.table.*;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JTable;

public class GroupButtonCellRenderer extends JToggleButton implements TableCellRenderer{ 

	private static final long serialVersionUID = 1L;
	boolean buttonLook=false;
	/*TableForm m_tf;*/
	GTable m_tf;
	JLabel textComponent= new JLabel();


	/*public GroupButtonCellRenderer(TableForm tf, boolean buttonLook) {*/
	public GroupButtonCellRenderer(GTable tf, boolean buttonLook) {
		m_tf=tf;
		this.buttonLook=buttonLook;
		setMaximumSize( new Dimension(10,10) );
		setMargin(new Insets(0,0,0,0));
	}

	public Component getTableCellRendererComponent(
			JTable table, Object color,
			boolean isSelected, boolean hasFocus,
			int row, int column) {
		Object val=m_tf.getModel().getValueAt( row, column );
		if( val instanceof Boolean ){
			boolean expandido=((Boolean)val).booleanValue();
			setText( (expandido ? "—":"+" ));
			return this;
		}else{
			textComponent.setText( val.toString() );
			return textComponent;
		}
	}

} 