package gdev.gawt.tableCellRenderer;

import gdev.gawt.GTable;
import gdev.gawt.GTableModel;
import gdev.gawt.utils.TextVerifier;
import gdev.gbalancer.GViewBalancer;
import gdev.gen.GConfigView;
import gdev.gfld.GTableColumn;

import javax.swing.table.TableCellRenderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;

import dynagent.common.utils.RowItem;

public class DateCellRenderer extends JLabel implements TableCellRenderer{
	
	private static final long serialVersionUID = 1L;
	private TextVerifier textVerifier;
	private boolean modeFilter;
	private Font fuente;
	private GTableColumn column;
	private GTable gTable;
	
	public DateCellRenderer(GTable gTable, GTableColumn columna, boolean filter){
		super();
		textVerifier=new TextVerifier(columna.getMask(), columna.getType(), filter);
		this.gTable=gTable;
		this.modeFilter=filter;
		column=columna;
		fuente = getFont();
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(0, GConfigView.horizontalMarginCell, 0, GConfigView.horizontalMarginCell));
	}	

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		GTableModel tfm = gTable.getModel();
		
		if(tfm.getRowData().size()>row){
			RowItem rowItem = (RowItem) tfm.getRowData().get(row);
			
			if(rowItem.isPermanent()){
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
			
			if(rowItem.isNullRow())
				super.setForeground(Color.gray);
		}
		
		String toolTipText=null;
		if(value!=null){
			setFont(fuente);
			setHorizontalAlignment(JLabel.LEFT);
			setForeground(Color.black);
			setText(textVerifier.format(value));
			if(table.isValid()){//Comprobamos antes si es valido porque si no el objeto Graphic de la tabla es null
				if(gTable.isTopLabel() && gTable.getModel().getColumnModel().getColumn(column).getWidth()<GViewBalancer.getDimString(getText(), false, getFont(), ((Graphics2D)table.getGraphics()).getFontRenderContext(),false).width)
					toolTipText=getText();
				else if(!gTable.isTopLabel() && gTable.getModel().getColumnModel().getColumn(column).getWidth()<GViewBalancer.getDimString(getText(), false, getFont(), ((Graphics2D)table.getGraphics()).getFontRenderContext(),false).width)
					toolTipText=this.column.getLabel()+": "+getText();
			}else if(!gTable.isTopLabel())
				toolTipText=this.column.getLabel();
		}else{
			if(!modeFilter){
				setText("<"+this.column.getLabel()+">");
				setFont(new Font(fuente.getName(), Font.ITALIC, fuente.getSize()));
				if(table.isValid() && gTable.getModel().getColumnModel().getColumn(column).getWidth()<GViewBalancer.getDimString(getText(), false, getFont(), ((Graphics2D)table.getGraphics()).getFontRenderContext(),false).width)
					toolTipText=this.column.getLabel();
				else if(!gTable.isTopLabel())
					toolTipText=this.column.getLabel();
				setHorizontalAlignment(JLabel.CENTER);
				setForeground(Color.gray);
			}else{
				setText("");
			}
		}
		setToolTipText(toolTipText);
		
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
