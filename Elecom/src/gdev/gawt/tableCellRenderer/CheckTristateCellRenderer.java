package gdev.gawt.tableCellRenderer;

import gdev.gawt.GTable;
import gdev.gawt.GTableModel;
import gdev.gawt.utils.TristateCheckBox;
import gdev.gbalancer.GViewBalancer;
import gdev.gen.GConfigView;
import gdev.gfld.GTableColumn;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import dynagent.common.communication.docServer;
import dynagent.common.utils.RowItem;


public class CheckTristateCellRenderer extends TristateCheckBox implements TableCellRenderer{

	private static final long serialVersionUID = 1L;
	private GTableColumn column;
	private GTable gTable;
	private boolean modeFilter;
	
	public CheckTristateCellRenderer(GTableColumn column, GTable gTable, boolean filter) {
		super(gTable.getServer(),column.getWidth()-(int)Math.round(column.getWidth()*GConfigView.reductionSizeCheck),column.getHeight()-(int)Math.round(column.getHeight()*GConfigView.reductionSizeCheck));
		this.column=column;
		this.gTable=gTable;
		this.modeFilter=filter;
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(0, GConfigView.horizontalMarginCell, 0, GConfigView.horizontalMarginCell));
	}
	
	public Component getTableCellRendererComponent(JTable table, Object val, boolean isSelected, boolean hasFocus, int row, int col) {

		super.setHorizontalAlignment(SwingConstants.CENTER);
		super.setMargin(new Insets(0,0,0,0));
		super.setIconTextGap(0);
		
		GTableModel tfm = (GTableModel) table.getModel();
				
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
		Boolean bolVal=null;
		if(val==null){
			if(!modeFilter){
				super.setText("<"+column.getLabel()+">");
				super.setFont(new Font(getFont().getName(), Font.ITALIC, getFont().getSize()));
				super.setHorizontalTextPosition(TristateCheckBox.CENTER);
			    super.setVerticalTextPosition(TristateCheckBox.CENTER);
			    //if(table.isValid() && gTable.getModel().getColumnModel().getColumn(col).getWidth()<GViewBalancer.getDimString(getText(), false, getFont(), ((Graphics2D)table.getGraphics()).getFontRenderContext(),false).width && gTable.isTopLabel())
				//	setToolTipText(column.getLabel());
			    setForeground(Color.gray);
			}
		}
		else if(val instanceof Boolean){
			setText(null);
			bolVal=(Boolean)val;
		}else if(val instanceof String){
			setText(null);
			String[] buf=((String)val).split(":");
			bolVal=(buf[0].equals("null")?null:new Boolean(buf[0]));
		}
		setToolTipText(this.column.getLabel());
		super.setSelected(bolVal);
		
		return this;
	}
}
