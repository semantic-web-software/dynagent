package gdev.gawt.tableCellEditor;

import gdev.gawt.GTable;
import gdev.gawt.GTableModel;
import gdev.gawt.utils.botoneraAccion;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTable;

import dynagent.common.utils.GIdRow;
import dynagent.common.utils.RowItem;



public class ButtonCellEditor extends CellEditor{

	private static final long serialVersionUID = 1L;
	/*TableForm m_tf;*/
	GTable gTable;
	Icon m_iconAdd;
	Icon m_iconDel;
	JButton button;

	/*public ButtonCellEditor(ActionListener list,TableForm tf, Icon icono, int accion) {*/
	public ButtonCellEditor(ActionListener list,GTable tf, Icon iconAdd, Icon iconDel) {
		super(tf,null);
		button=new JButton();
		if( list!=null && list instanceof ActionListener )
			button.addActionListener( list );
		this.m_iconAdd=iconAdd;
		this.m_iconDel=iconDel;
		//setIcon( icono );
		gTable=tf;
		
		button.setBorderPainted(false);
		//button.setFocusPainted(false);
		button.setMargin(new Insets(0,0,0,0));
	}


	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected,
			int row, int column) {
		//System.err.println("ButtonCellEditor::"+row+","+column+","+value);
		/*TableFormModel tfm= (TableFormModel)m_tf.getModel();*/
		//GTableModel tfm= (GTableModel)m_tf.getModel();
		//System.out.println("SETTINGCOMMAND:"+id);
		GTableModel tfm = gTable.getModel();
		//if(tfm.getRowData().size()>row){
			RowItem rowItem = (RowItem) tfm.getRowData().get(row);
			int action=0;
			if(rowItem.isPermanent()){
				button.setIcon(m_iconDel);
				action=botoneraAccion.ROWDEL;
				button.setOpaque(true);
			}else{
				button.setIcon(m_iconAdd);
				action=botoneraAccion.ROWADD;
				button.setOpaque(false);
			}
			GIdRow idRow=gTable.getModel().getDataFromIndex( row );
			button.setActionCommand("ACTION:0:"+idRow.getIdo()+":"+action+":"+idRow.getIdto()+":"+row);
			
		//}
		return button;
	}

	public Object getCellEditorValue(){
		return null;
	}


	@Override
	public void cancelChangeValue() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setValue(Object value) {
		// TODO Auto-generated method stub
		
	}

}
