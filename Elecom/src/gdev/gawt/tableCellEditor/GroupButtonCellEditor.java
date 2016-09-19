package gdev.gawt.tableCellEditor;

import gdev.gawt.GTable;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;


public class GroupButtonCellEditor extends CellEditor implements ItemListener{ 

	private static final long serialVersionUID = 1L;
	/** The Swing component being edited. */
	protected JTextField textEditorComponent;
	protected JToggleButton buttonEditorComponent;
	protected String par2, m_label;
	/**
	 * An integer specifying the number of clicks needed to start editing.
	 * Even if <code>clickCountToStart</code> is defined as zero, it
	 * will not initiate until a click occurs.
	 */
	protected int clickCountToStart = 0;
	/*TableForm m_tf;*/
	boolean buttonLook=false;
	int currentRow=0;
	//

	/* public GroupButtonCellEditor(TableForm tf, String label, boolean buttonLook) {*/
	public GroupButtonCellEditor(GTable tf, String label, boolean buttonLook) {
		super(tf,null);
		textEditorComponent =new JTextField();
		buttonEditorComponent= new JToggleButton("+");
		buttonEditorComponent.setMargin(new Insets(0,0,0,0));
		buttonEditorComponent.setMaximumSize( new Dimension(10,10) );
		textEditorComponent.setMaximumSize( new Dimension(10,10) );
		this.clickCountToStart = 1;
		m_label= label;
		this.buttonLook=buttonLook;
		buttonEditorComponent.addItemListener( this );
	}

	public void itemStateChanged( ItemEvent e){
		try{
			
			boolean expand=false;
			switch( e.getStateChange() ){
			case ItemEvent.DESELECTED:
				expand=false;
				break;
			case ItemEvent.SELECTED:
				expand=true;
				break;
			default:
				System.out.println("UNKONW EVENT "+e.getStateChange());
			return;
			} 		
			//System.outprintln("CAMBIO ROW " + currentRow + " A:"+expand);
			Boolean value=new Boolean( expand );
			setValue(value, false);
	
			//gTable.getModel().setValueAt( value,currentRow, 0 );
	
			stopCellEditing();
		}catch(Exception ex){
			ex.printStackTrace();
			gTable.getServer().logError(SwingUtilities.getWindowAncestor(gTable),ex,"Error al asignar valor");
		}
	}

	public void setValue(Object value, boolean updateGUI) {
		if( value instanceof Boolean ){		
			buttonLook=true;
			Boolean bol=(Boolean)value;
			buttonEditorComponent.setText( (bol.booleanValue() ? "—":"+" ));
			//System.outprintln("NUEVO TEXTO:"+bol.booleanValue()+","+(bol.booleanValue() ? "—":"+" ));
			if(updateGUI) buttonEditorComponent.setSelected( bol.booleanValue() );
		}else{
			textEditorComponent.setText((value != null) ? value.toString() : "");
			buttonLook=false;
		}
	}
	public String getLabel(){
		return m_label;
	}

	public void reset(){
	}

	public int getExternalCode(){
		return 0;
	}

	public void commitValorInicial(){
	}

	public void resetRestriction(){
	}

	public Object getCellEditorValue() {
		if(buttonLook) return new Boolean(buttonEditorComponent.isSelected());
		else return null;
	}

	public Component getComponent() {
		if(buttonLook)
			return buttonEditorComponent;
		else return textEditorComponent;
	}

	public void setClickCountToStart(int count) {
		clickCountToStart = count;
	}

	public int getClickCountToStart() {
		return clickCountToStart;
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected,
			int row, int column) {
		currentRow= row;
		setValue(value,true);
		/*TableFormModel tfm= (TableFormModel)m_tf.getModel();*/
		//GTableModel tfm= (GTableModel)m_tf.getModel();
		return getComponent();
	}

	public boolean isCellEditable(EventObject anEvent) {
		return buttonLook;
	}

	public boolean shouldSelectCell(EventObject anEvent) { 
		return buttonLook; 
	}

	public boolean startCellEditing(EventObject anEvent) {
		return buttonLook;
	}

	public boolean stopCellEditing() { 
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		stopCellEditing();
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