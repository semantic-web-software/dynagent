package gdev.gawt.tableCellEditor;

import gdev.gawt.GTable;
import gdev.gawt.utils.TextVerifier;
import gdev.gen.GConfigView;
import gdev.gen.GConst;
import gdev.gfld.GTableColumn;

import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import dynagent.common.utils.RowItem;

public class NumberCellEditor extends CellEditor {
	
	private static final long serialVersionUID = 1L;
	private JTextField editorComponent;
	private Color colorDefaultComponent;
	private TextVerifierEditor textVerifier;
	private int clickCountToStart;	
	private boolean mustStop = true;
	private boolean showMessage = true;
	private String m_label;
	private String oldValue;
	
	public NumberCellEditor(GTableColumn column, GTable tf, boolean filter, final FocusListener listener){
		super(tf,column);
		this.m_label=column.getLabel();
		clickCountToStart = 2;
		textVerifier=new TextVerifierEditor(column.getMask(), column.getType(), filter);
		editorComponent =new JTextField();	
		editorComponent.setInputVerifier(textVerifier);
		//editorComponent.setBorder(BorderFactory.createEmptyBorder());
		editorComponent.setBorder(GConfigView.borderSelected);
		editorComponent.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent ev){
				mustStop=true;
				try{
					if(!gTable.getTable().isEditing())
						gTable.getTable().editCellAt(gTable.getTable().getSelectedRow(), gTable.getTable().getSelectedColumn());
					if(editorComponent.getText()!=null && !editorComponent.getText().equals("")){
						editorComponent.selectAll();
					}
				}catch(Exception ex){
					ex.printStackTrace();
					gTable.getServer().logError(SwingUtilities.getWindowAncestor(gTable),ex,"Error al seleccionar valor");
				}
			}
			public void focusLost(FocusEvent ev){
				try{
					if(mustStop && !ev.isTemporary()){					
						if(gTable.getTable().isEditing())
							stopCellEditing();
						listener.focusLost(ev);
					}
				}catch(Exception ex){
					ex.printStackTrace();
					gTable.getServer().logError(SwingUtilities.getWindowAncestor(gTable),ex,"Error al finalizar edición");
				}
			}
		});	
		editorComponent.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent ev) {
				showMessage=true;
			}
			public void keyReleased(KeyEvent ev) {}
			public void keyTyped(KeyEvent ev) {}
		});
		
		colorDefaultComponent=editorComponent.getBackground();
		
		editorComponent.setHorizontalAlignment(JTextField.RIGHT);
	}	
	
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		mustStop=true;
		/*GTableModel tfm = (GTableModel) table.getModel();
		RowItem rt = (RowItem) tfm.getRowData().get(row);*/

		if(value==null){
			oldValue=null;
			editorComponent.setText(null);				
		}else{			
			oldValue = textVerifier.format(value);
			editorComponent.setText(oldValue);
		}
		
		if(!gTable.getModel().isNullable(row,column))
			editorComponent.setBackground(GConfigView.colorBackgroundRequired);
		else editorComponent.setBackground(colorDefaultComponent);
		
		//System.err.println("editor "+editorComponent.getText());
		return editorComponent;
	}
    
    public Object getCellEditorValue(){
    	showMessage=true;
		Object res = null;
		if(editorComponent.getText().equals("")){
			res = null;			
		}else{
			String number = textVerifier.format(editorComponent.getText());
			if(number!=null)
				res=textVerifier.parseNumber(number);
		}
		//System.err.println("VALUE "+res);
		return res;
    }
     
    public boolean stopCellEditing() {
    	//System.err.println("StopCellEditing number");
    	InputVerifier verifier = editorComponent.getInputVerifier();
        if(verifier!=null && !verifier.shouldYieldFocus(editorComponent))
        		return false;
        if(getCellEditorValue()!=null){
			RowItem rowItem = gTable.getModel().getRowData().get(gTable.getTable().getSelectedRow());
			if(rowItem.getColumnIdo(column.getColumn())==null){
				if(rowItem.getIdRow().getIdto()!=rowItem.getColumnIdto(column.getColumn())){
					rowItem.setColumnOldIdo(column.getColumn(),rowItem.getColumnIdo(column.getColumn()));
					rowItem.setColumnOldIdto(column.getColumn(),rowItem.getColumnIdto(column.getColumn()));
					int action=rowItem.getState();
					if(rowItem.isNullRow())
						action=RowItem.CREATION_STATE+RowItem.SUBCREATION_STATE;
					else action=RowItem.SUBCREATION_STATE;
					rowItem.setState(action);
				}
			}
        }
        mustStop=false;
        return super.stopCellEditing();        
    }     
    
    public class TextVerifierEditor extends TextVerifier{

    	private int sintax;

    	public TextVerifierEditor(String mask, int sintax, boolean modoFilter) {
    		super(mask, sintax, modoFilter);
    		this.sintax=sintax;
    	}

    	public boolean verify(JComponent input) {
    		JTextField text= (JTextField) input;    		
    		boolean exito= super.verify(input);
    		if(exito){
    			if(sintax==GConst.TM_INTEGER || sintax==GConst.TM_REAL )
    				editorComponent.setText( text.getText() );
    		}
    		return exito;
    	}

    	public boolean shouldYieldFocus(JComponent input){

    		if(!verify(input)){
    			if(showMessage){
    				showMessage=false;
    				String msg=buildMessageError(m_label);   				
    				Window wind=SwingUtilities.windowForComponent(input);
    				gTable.getMessageListener().showErrorMessage(msg,wind);
    				input.setInputVerifier(this);
    			}
    			return false;
    		}
    		return true;	
    	}    	
    }

	@Override
	public void cancelChangeValue() {
		this.editorComponent.setText(oldValue);
	}

	@Override
	public void setValue(Object value) {
		this.editorComponent.setText((String)value);
	}
}