package gdev.gawt.tableCellRenderer;

import gdev.gawt.GTable;
import gdev.gawt.GTableModel;
import gdev.gen.GConfigView;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ComboBoxRendererLabel extends JLabel implements TableCellRenderer {
    
	private static final long serialVersionUID = 1L;
	private HashMap<Integer, String> valueMap= new HashMap<Integer, String>();
	private GTable gTable;
	
	public ComboBoxRendererLabel(GTable gTable){
		super();
		this.gTable=gTable;
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(0, GConfigView.horizontalMarginCell, 0, GConfigView.horizontalMarginCell));
	}

	public void addLine(Integer id, String label){		
		valueMap.put(id,label);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {  
		GTableModel tfm = gTable.getModel();
		if(tfm.getRowData().size()>row){
			if(tfm.getRowItemFromIndex(row).isPermanent()){
				if (isSelected) {
					super.setForeground(table.getSelectionForeground());
					super.setBackground(table.getSelectionBackground());
				} else {
					super.setForeground(table.getSelectionForeground());
					super.setBackground(GConfigView.colorBackgroundPermanent);
				}
			}else{
				if (isSelected) {
					super.setForeground(table.getSelectionForeground());
					super.setBackground(table.getSelectionBackground());
				} else {
					super.setForeground(table.getForeground());
					super.setBackground(table.getBackground());
				}
			}
		}
		if(value!=null)
			super.setText(valueMap.get(Integer.parseInt(String.valueOf(value))));
		return this;

	}

    public boolean isOpaque() { 
		Color back = getBackground();
		Component p = getParent(); 
		if (p != null)
			p = p.getParent(); 
		
		boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) &&  p.isOpaque();
		return !colorMatch && super.isOpaque(); 
	}
}