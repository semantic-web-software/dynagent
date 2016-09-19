package gdev.gawt.tableCellEditor;

import gdev.gawt.GTable;
import gdev.gfld.GTableColumn;

import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import dynagent.common.Constants;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.RowItem;

public abstract class CellEditor extends AbstractCellEditor implements TableCellEditor{

	protected GTable gTable;
	protected GTableColumn column;
	protected Integer lastSelectionCreation;//Mantiene el valor elegido en la ultima creacion
	protected Integer lastSelectionSubCreation;//Mantiene el valor elegido en la ultima subcreacion
	protected boolean creationConfirmation;//Indica si, en una creacion, hay que preguntarle al usuario si lo quiere crear
	private boolean rememberSelectionForCopyRows=false;
	
	public CellEditor(GTable gTable,GTableColumn column){
		this.gTable=gTable;
		this.column=column;
		this.creationConfirmation=column!=null?column.hasFinder():false;//Nos interesa preguntar siempre cuando hay finder ya que puede haber ambiguedad para el usuario de si esta creando o buscando
	}
	
//	public boolean isCellEditable(EventObject anEvent) {
//		Exception ex=new Exception();
//		ex.printStackTrace();
//		System.err.println("CLASEEEEEEEEE "+this);
//		//gTable.getTable().notifyAll();
//		gTable.getTable().validate();
////		if (anEvent instanceof MouseEvent){
////			boolean editable=((MouseEvent)anEvent).getClickCount() >= clickCountToStart;
////			return  editable;
////		}
////		return true;
//		
//		System.err.println(this.getCellEditorValue());
//		System.err.println("lead:"+gTable.getTable().getSelectionModel().getLeadSelectionIndex());
//		System.err.println("anchor:"+gTable.getTable().getSelectionModel().getAnchorSelectionIndex());
//		System.err.println("min:"+gTable.getTable().getSelectionModel().getMinSelectionIndex());
//		System.err.println("column:"+gTable.getTable().getEditingColumn());
//		System.err.println("row:"+gTable.getTable().getEditingRow());
//		System.err.println("columnSelected:"+gTable.getTable().getSelectedColumn());
//		System.err.println("rowSelected:"+gTable.getTable().getSelectedRow());
//		System.err.println("gTable.getOldRowSelected():"+gTable.getOldRowSelected());
//		System.err.println("gTable.getRowSelected():"+gTable.getRowSelected());
//		
//		return false;
//	}

	public void cancelCellEditing() {
		//System.err.println("CancellEditing");
		fireEditingCanceled();
	}

	public boolean shouldSelectCell(EventObject e) {
		return true;
	}

	public boolean stopCellEditing() {
		RowItem rowItem = gTable.getModel().getRowData().get(gTable.getTable().getSelectedRow());
		
		//Si se trata de una creacion vemos si, en el caso de que haya alguna ambiguedad en que tipo de objeto crear(abstractas), ver si podemos resolver la ambiguedad a partir del dato
		//introducido por el usuario. En el caso de que siga habiendo ambiguedad preguntamos al usuario para que la resuelva.
		if(column!=null && this.getCellEditorValue()!=null && (rowItem.matchesState(RowItem.CREATION_STATE) || rowItem.matchesState(RowItem.SUBCREATION_STATE))){
			/*NOTA: Tener en cuenta que el tipo de estados del rowItem que pueden entrar en esta condición son los siguientes:
			 * CREATION_STATE
			 * CREATION_STATE+SUBCREATION_STATE
			 * CREATION_STATE+FINDER_STATE
			 * SUBCREATION_STATE
			 * */
			
			//En caso de tener que preguntar al usuario mostramos el mismo tipo elegido en una anterior seleccion
			Integer defaultPossibility=rowItem.getState()==RowItem.CREATION_STATE?lastSelectionCreation:lastSelectionSubCreation;
			
			String idParent=null;
			String id=column.getId();
			Object value=null;
			Integer valueCls=null;
			boolean creationConfirmation=this.creationConfirmation && !this.gTable.isProcessingPasteRows();
			if(rowItem.getState()==RowItem.SUBCREATION_STATE)//SUBCREATION_STATE
				idParent=rowItem.getColumnIdParent(column.getColumn());
			else if(rowItem.matchesState(RowItem.FINDER_STATE)){//CREATION_STATE+FINDER_STATE
				id=rowItem.getColumnIdParent(column.getColumn());
				value=rowItem.getColumnIdo(column.getColumn());
				valueCls=rowItem.getColumnIdto(column.getColumn());
				creationConfirmation=false;
			}else{//CREATION_STATE, CREATION_STATE+SUBCREATION_STATE
				value=this.getCellEditorValue();
			}	
			
			Integer type=getTypeForValue(idParent, id, value, valueCls, defaultPossibility, creationConfirmation);
			
			if(type==null){
				//System.err.println("Cancelada la creacion o subcreacion");
				cancelChangeValue();//Si no hay tipo compatible evitamos la edicion
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						gTable.getTable().getEditorComponent().requestFocusInWindow();
					}
				});
				return false;
			}else{
				if(rowItem.getState()==RowItem.CREATION_STATE+RowItem.SUBCREATION_STATE){//CREATION_STATE+SUBCREATION_STATE
					lastSelectionSubCreation=type;
					Integer typeParent=getTypeForValue(null,column.getIdParent(), type, type, defaultPossibility,false);
					if(typeParent==null){
						//System.err.println("Cancelada la creacion");
						cancelChangeValue();
//						SwingUtilities.invokeLater(new Runnable() {
//							
//							@Override
//							public void run() {
//								gTable.getTable().getEditorComponent().requestFocusInWindow();
//							}
//						});
						return false;
					}else{
						lastSelectionCreation=typeParent;
					}
				}else if(rowItem.matchesState(RowItem.CREATION_STATE)){//CREATION_STATE, CREATION_STATE+FINDER_STATE
					lastSelectionCreation=type;
				}else if(rowItem.matchesState(RowItem.SUBCREATION_STATE))//SUBCREATION_STATE
					lastSelectionSubCreation=type;
			}
		}
		return super.stopCellEditing();
	}
	
	//Devuelve el tipo resuelto automaticamente o preguntando al usuario si es necesario. En el caso de pasarle idParent, se busca que posibilidad
	//de las encontradas con id casan con las posibles posibilidades de idParent. Es decir, se buscan las posibilidades en esa columna para value y valueCls
	//y luego se comprueba si son compatibles con el idParent ya que es donde luego se enganchará. 
	private Integer getTypeForValue(String idParent,String id,Object value,Integer valueCls,Integer defaultPossibility,boolean creationConfirmation){
		LinkedHashMap<String,Integer> possibleList;
		if(idParent!=null){
			possibleList=new LinkedHashMap<String, Integer>();
			LinkedHashMap<String,Integer> possibleAuxList=gTable.getModel().getPossibleTypeForValue(id, value, valueCls);
			
			Iterator<String> itr=possibleAuxList.keySet().iterator();
			while(itr.hasNext()){
				String key=itr.next();
				int type=possibleAuxList.get(key);
				if(!gTable.getModel().getPossibleTypeForValue(idParent, type, type).isEmpty()){
					possibleList.put(key, type);
				}
			}
		}else{
			possibleList=gTable.getModel().getPossibleTypeForValue(id, value, valueCls);
		}
		
		LinkedList<String> sortList = new LinkedList<String>();
		sortList.addAll(possibleList.keySet());				
		Collections.sort(sortList, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return Constants.languageCollator.compare(o1,o2);
			}
			
		});
		
		Integer type=null;
		
		String[] possibilities=new String[sortList.size()];
		Iterator<String> itr=sortList.iterator();
		int i=0;
		String defaultPossibilityString=null;
		while(itr.hasNext()){
			possibilities[i]=itr.next();
			if(i==0)
				defaultPossibilityString=possibilities[i];
			else if(Auxiliar.equals(defaultPossibility, possibleList.get(possibilities[i])))
				defaultPossibilityString=possibilities[i];
			i++;
		}
		String res=showConfirmation(possibilities,defaultPossibilityString,creationConfirmation);
		if(res!=null){
			type=possibleList.get(res);
		}
		
		return type;
	}
	
	//Muestra un mensaje de confirmacion o de seleccion de posibilidades, en el caso de que sea necesario. Devuelve el tipo elegido por el usuario o el de por defecto.
	public String showConfirmation(String[] possibilities,String defaultPossibility,boolean creationConfirmation){
		String messageConfirmation="";
		String res=defaultPossibility;
		
		if(creationConfirmation)
			messageConfirmation="Se va a proceder a la creación del objeto utilizando el valor '"+this.getCellEditorValue()+"' ya que no existe en base de datos.\n";
		
		if(possibilities.length>1){
			messageConfirmation+="Seleccione el tipo de objeto. ¿Desea continuar?";
			if(gTable.isProcessingPasteRows()){
				if(!rememberSelectionForCopyRows){
					JCheckBox rememberChk = new JCheckBox ("Aplicar a todos los registros a copiar");
					Object[] msgContent = {messageConfirmation,rememberChk};
					res = gTable.getMessageListener().showInputMessageWithComponents(msgContent,
			                "AVISO DE CREACIÓN",
			                JOptionPane.WARNING_MESSAGE,
			                null,
			                possibilities,
			                defaultPossibility,SwingUtilities.getWindowAncestor(gTable));
					if(res!=null){
						rememberSelectionForCopyRows=rememberChk.isSelected();
					}else{
						gTable.setAbortPasteRows(true);
					}
				}
			}else{
				res = gTable.getMessageListener().showInputMessage(messageConfirmation,
	                "AVISO DE CREACIÓN",
	                JOptionPane.WARNING_MESSAGE,
	                null,
	                possibilities,
	                defaultPossibility,SwingUtilities.getWindowAncestor(gTable));
			}
		}else if(creationConfirmation){
			messageConfirmation+="¿Desea continuar?";
			Object[] options = {"Sí", "No"};
			int resConfir = gTable.getMessageListener().showOptionMessage(messageConfirmation,
					"AVISO DE CREACIÓN",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null,
					options,
					options[0],SwingUtilities.getWindowAncestor(gTable));
			if (resConfir != JOptionPane.YES_OPTION)
				res=null;
		}
		
		return res;
	}
	
	public abstract void cancelChangeValue();

	public Integer getLastSelectionCreation() {
		return lastSelectionCreation;
	}

	public void setLastSelectionCreation(Integer lastSelectionCreation) {
		this.lastSelectionCreation = lastSelectionCreation;
	}

	public Integer getLastSelectionSubCreation() {
		return lastSelectionSubCreation;
	}

	public void setLastSelectionSubCreation(Integer lastSelectionSubCreation) {
		this.lastSelectionSubCreation = lastSelectionSubCreation;
	}

	public GTableColumn getColumn() {
		return column;
	}
	
	public abstract void setValue(Object value);

	public void setRememberSelectionForCopyRows(boolean rememberSelectionForCopyRows) {
		this.rememberSelectionForCopyRows = rememberSelectionForCopyRows;
	}
}
