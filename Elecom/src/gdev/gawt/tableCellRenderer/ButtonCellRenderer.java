package gdev.gawt.tableCellRenderer;

import gdev.gawt.GTable;
import gdev.gawt.GTableModel;
import gdev.gbalancer.GViewBalancer;
import gdev.gen.GConst;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import dynagent.common.utils.RowItem;

public class ButtonCellRenderer extends JButton implements TableCellRenderer{

	Icon m_iconAdd;
	Icon m_iconDel;
	GTable gTable;
	
	public ButtonCellRenderer(GTable gTable,Icon iconAdd, Icon iconDel) {
		m_iconAdd=iconAdd;
		m_iconDel=iconDel;
		this.gTable=gTable;
		
		setBorderPainted(false);
		setContentAreaFilled(false);
		setMargin(new Insets(0,0,0,0));
	}

	public Component getTableCellRendererComponent(	JTable table,
			Object value,
			boolean isSelected,
			boolean hasFocus,
			int row,
			int column){
		/*return new JButton(((TableForm)table).iconoFlechaExtrae);*/
			
		GTableModel tfm = gTable.getModel();
		if(tfm.getRowData().size()>row){
			RowItem rowItem = (RowItem) tfm.getRowData().get(row);
			String toolTipText=null;
			if(rowItem.isPermanent()){
				//System.err.println("row:"+row+" column:"+column+" permanent");
				setIcon(m_iconDel);
				toolTipText="Quitar";
				setOpaque(true);
			}else{
				//System.err.println("row:"+row+" column:"+column);
				setIcon(m_iconAdd);
				toolTipText="Fijar";
				setOpaque(false);
			}
			
			//button.setFocusPainted(false);
			setToolTipText(toolTipText);
			return this;
		}
		
		return null;
	}

}
