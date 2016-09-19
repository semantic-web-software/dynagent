package gdev.gawt.tableCellEditor;

import gdev.gawt.GTable;
import gdev.gawt.utils.ItemList;
import gdev.gen.GConfigView;
import gdev.gfld.GTableColumn;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class ComboBoxEditor extends CellEditor {//extends DefaultCellEditor{
	private HashMap<Integer, ItemList> idoItemListMap;
	
	private static final long serialVersionUID = 1L;
	private JComboBox editorComponent;
	private Color colorDefaultComponent;
	private int clickCountToStart;	
	private boolean mustStop = true;
	private boolean showMessage = true;
	private String m_label;
	private ItemList oldValue;
	
	public ComboBoxEditor(GTableColumn column,Vector<ItemList> listItemList, GTable tf, final FocusListener listener) {
		super(tf,column);
		//super(new JComboBox(listItemList));	
		editorComponent =new JComboBox(listItemList){

			@Override
			public void setBorder(Border b) {
				//setBorder(border/*javax.swing.BorderFactory.createLineBorder(new java.awt.Color(49,106,197))*/);
				//setEditable(true);
				//setBackground(new java.awt.Color(255, 255, 255));
				for (int i=0; i<getComponentCount(); i++) {
					if (getComponent(i) instanceof AbstractButton) {
						((AbstractButton)getComponent(i)).setBorder(b/*javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(224, 223, 227)))*/);
						//((AbstractButton)getComponent(i)).setBackground(new java.awt.Color(224, 223, 227));
					} else {
						if (getComponent(i) instanceof JTextField){
							//((JTextField)getComponent(i)).setSelectionColor(new java.awt.Color(163,184,203));
							//((JTextField)getComponent(i)).setBorder(border/*javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255,255,255))*/);
							//((JTextField)getComponent(i)).setBackground(new java.awt.Color(255, 255, 255));
							//((JTextField)getComponent(i)).setDisabledTextColor(new java.awt.Color(0, 0, 255));
						}
					}
				}
			}
			
		};
		editorComponent.setBorder(GConfigView.borderSelected);
		idoItemListMap=new HashMap<Integer, ItemList>();
		Iterator<ItemList> itr=listItemList.iterator();
		while(itr.hasNext()){
			ItemList itL=itr.next();
			idoItemListMap.put(itL.getIntId(), itL);
		}
		editorComponent.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent ev){
				mustStop=true;
				if(!gTable.getTable().isEditing())
					gTable.getTable().editCellAt(gTable.getTable().getSelectedRow(), gTable.getTable().getSelectedColumn());
			}
			public void focusLost(FocusEvent ev){
				try{
					//System.err.println("FocusLost Combo:"+mustStop);
					if(mustStop && !ev.isTemporary()){					
						if(gTable.getTable().isEditing())
							stopCellEditing();
						listener.focusLost(ev);
					}
//					if(mustStop){
//						stopCellEditing();
//						listener.focusLost(ev);
//					}
				}catch(Exception ex){
					ex.printStackTrace();
					gTable.getServer().logError(SwingUtilities.getWindowAncestor(gTable),ex,"Error al finalizar edición");
				}
			}
		});
		((JComboBox)editorComponent).addPopupMenuListener(new PopupMenuListener(){
			public void popupMenuCanceled(PopupMenuEvent ev){
				try{
					cancelCellEditing();
				}catch(Exception ex){
					ex.printStackTrace();
					gTable.getServer().logError(SwingUtilities.getWindowAncestor(gTable),ex,"Error al cancelar edición");
				}
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent ev){}
			public void popupMenuWillBecomeVisible(PopupMenuEvent ev){}			
		});
		
		InputMap im = editorComponent.getInputMap(JComboBox.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
	        
		Action tabActionEnter = new AbstractAction(){

			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e){
				try{
					stopCellEditing();
					InputMap im = gTable.getTable().getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
					KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
					e.setSource(gTable.getTable());
					gTable.getTable().getActionMap().get(im.get(enter)).actionPerformed(e);
				}catch(Exception ex){
					ex.printStackTrace();
					gTable.getServer().logError(SwingUtilities.getWindowAncestor(gTable),ex,"Error al detener la edicion de la lista desplegable de la tabla");
				}
			}
		};
		editorComponent.getActionMap().put(im.get(enter), tabActionEnter);
		
		KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        
		Action tabActionTab = new AbstractAction(){

			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e){
				try{
					stopCellEditing();
					InputMap im = gTable.getTable().getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
					KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
					e.setSource(gTable.getTable());
					gTable.getTable().getActionMap().get(im.get(tab)).actionPerformed(e);
				}catch(Exception ex){
					ex.printStackTrace();
					gTable.getServer().logError(SwingUtilities.getWindowAncestor(gTable),ex,"Error al detener la edicion de la lista desplegable de la tabla");
				}
			}
		};
		editorComponent.getActionMap().put(im.get(tab), tabActionTab);
		
		colorDefaultComponent=editorComponent.getBackground();
	}	
	
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		//System.err.println("getTableCellEditorComponent Combo");
		mustStop = true;
		JComboBox editor = (JComboBox)editorComponent;
		if(value==null){
			oldValue=null;
			editor.setSelectedItem(null);				
		}else{			
			oldValue=idoItemListMap.get(value);
			editor.setSelectedItem(oldValue);
		}
		
		if(!gTable.getModel().isNullable(row,column))
			editorComponent.setBackground(GConfigView.colorBackgroundRequired);
		else editorComponent.setBackground(colorDefaultComponent);
		
		return editorComponent;
	}
    
    public Object getCellEditorValue(){
		//mustStop = true;
		JComboBox editor = (JComboBox)editorComponent;
		if(editor.getSelectedItem()==null){
    		return null;			
		}else{
			ItemList itemList=((ItemList)editor.getSelectedItem());
			return itemList.getInteger()!=0?itemList.getInteger():null;
		}		
    }
     
    public boolean stopCellEditing() {
    	//System.err.println("StopCellEditing number");
    	InputVerifier verifier = editorComponent.getInputVerifier();
        if(verifier!=null && !verifier.shouldYieldFocus(editorComponent))
        		return false;
        mustStop=false;
        return super.stopCellEditing();        
    }

	@Override
	public void cancelChangeValue() {
		((JComboBox)editorComponent).setSelectedItem(oldValue);
	}

	@Override
	public void setValue(Object value) {
		((JComboBox)editorComponent).setSelectedItem(value);
	}
    
}
