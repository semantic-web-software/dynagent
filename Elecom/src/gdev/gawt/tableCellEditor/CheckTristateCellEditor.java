package gdev.gawt.tableCellEditor;

import gdev.gawt.GTable;
import gdev.gawt.utils.TristateCheckBox;
import gdev.gen.GConfigView;
import gdev.gfld.GTableColumn;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import dynagent.common.utils.RowItem;

public class CheckTristateCellEditor extends CheckCellEditor{
	
	private static final long serialVersionUID = 1L;
	
	public CheckTristateCellEditor(GTableColumn column,GTable tf, final FocusListener listener) {
		super(column,tf,listener);
	}
	
	@Override
	protected void create() {
		int height=column.getHeight()-(int)Math.round(column.getHeight()*GConfigView.reductionSizeCheck);
		int width=column.getWidth()-(int)Math.round(column.getWidth()*GConfigView.reductionSizeCheck);
		editorComponent = new TristateCheckBox(gTable.getServer(),width,height);
		value=null;
	}

	public Component getTableCellEditorComponent(JTable table, Object val, boolean isSelected, int row, int column) {
		mustStop=true;
		Boolean bolVal=null;
		if( val instanceof Boolean ){
			bolVal=(Boolean)val;
		}
		else if(val instanceof String){
			String[] buf=((String)val).split(":");
			bolVal=(buf[0].equals("null")?null:new Boolean(buf[0]));
			//TODO Hay que pensar que hacer cuando es un boolean con comentario
			//if(buf.length>1)
			//	commentVal=(buf[1].equals("null")?null:buf[1]);
		}
		
		oldValue=value;
		editorComponent.setSelected( bolVal );
		value=bolVal;
		
		if(!gTable.getModel().isNullable(row,column))
			editorComponent.setBackground(GConfigView.colorBackgroundRequired);
		else editorComponent.setBackground(colorDefaultComponent);
		
		return editorComponent;
	}
	
	@Override
	protected void nextState() {
		if (value == null) {
			value=true;
		} else if (value == true) {
			value=false;
		} else if (value==false) {
			value=null;
		}
	}
	
}