package dynagent.gui.forms;

import gdev.gen.AssignValueException;
import gdev.gen.EditionTableException;
import gdev.gen.NotValidValueException;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;
import javax.swing.JPanel;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.RuleEngineException;
import dynagent.common.exceptions.ServerException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.DataValue;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.GIdRow;
import dynagent.common.utils.IdObjectForm;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.WindowComponent;
import dynagent.ruleengine.src.sessions.DefaultSession;

/* Permite la edicion de varios individuos a la vez mostrando el valor comun de sus properties.
 * Se utiliza un filtro para crear el formulario pero a dicho filtro no se le dan valores(se han modificado
 * los metodos para que hagan el cambio sobre los individuos) salvo en el caso en el que se abre otro
 * formulario modal a partir de este ya que es la unica manera de enterarme, mediante changeValue, de esa asignacion*/

public class TransitionControlCommon extends transitionControl{

	private HashMap<Integer,Integer> idos;
	HashMap<Integer, HashMap<Integer, LinkedList<Value>>> listIndividuos;
	boolean possibleChanges;

	public TransitionControlCommon(Session ses,
			Integer userRol,
			int idoParent,
			HashMap<Integer,Integer> idos,
			Integer idtoUserTask,
			int operation,
			Dimension dim,
			JPanel botonera, KnowledgeBaseAdapter kba, WindowComponent dialog) throws NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, OperationNotPermitedException{


		super(ses, userRol, idoParent, kba.createPrototype(/*idos.get(0)*/idos.get(idos.keySet().iterator().next()), Constants.LEVEL_FILTER, userRol, idtoUserTask, ses), idos.get(idos.keySet().iterator().next()), idtoUserTask, operation, dim, botonera, kba, dialog, null, null, true, true, null);	
		
		//Si la lista de idos son de distintas clases no permitimos la ejecucion conjunta ya que tendriamos problemas con properties que estan en un objeto pero en otro no
		Iterator<Integer> itrIdos=idos.keySet().iterator();
		Integer idto=m_kba.getClass(itrIdos.next());
		while(itrIdos.hasNext()){
			int ido=itrIdos.next();
			if(!m_kba.getClass(ido).equals(idto)){
				OperationNotPermitedException exception=new OperationNotPermitedException("No pueden editarse individuos con properties distintas. Uno es de la clase "+idto+" y otro de la clase "+m_kba.getClass(ido));
				exception.setUserMessage("La selección realizada no puede ser procesada conjuntamente ya que posee caracteristicas distintas");
				throw exception;
			}
				
		}
		listIndividuos = new HashMap<Integer, HashMap<Integer,LinkedList<Value>>>();
		this.idos=idos;
		copySameValues();
		
		startEdition(new ArrayList<Integer>(idos.keySet()),m_session);
		
		possibleChanges=false;
		dialog.getComponent().addWindowFocusListener(new WindowFocusListener(){
			public void windowGainedFocus(WindowEvent e) {
				try {
					if(possibleChanges){
						copySameValues();
						possibleChanges=false;
					}
				} catch (Exception ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}

			public void windowLostFocus(WindowEvent e) {
				/*System.err.println(e.getOppositeWindow()+" "+e.getID()+" "+e.getNewState()+" "+e.getOldState());
				if(e.getOppositeWindow()!=null){
					possibleChanges=true;
				}*/
			}
			
		});
	}
	
	
	
	@Override
	protected void build(JPanel botonera, int operation, Integer idtoUserTask, Integer idObjectParent, int idObject, int idtoObject, boolean multipleMode, boolean popup, boolean scroll, WindowComponent dialog, HashMap<String,String> aliasMap) throws NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, OperationNotPermitedException {
		super.build(botonera, operation, idtoUserTask, idObjectParent, idObject, idtoObject, true, popup, true, dialog, aliasMap);
	}

//	public String confirm() throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, OperationNotPermitedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException{
//		m_kba.deleteObject(m_ido, m_idto, null, m_userRol, m_idtoUserTask, m_session);		
//		//System.out.println("Lista session "+m_session.getSesionables());
//		String msg = super.confirm();
//		return msg;
//	}
	
	private void copySameValues() throws ParseException, AssignValueException, NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException{
		Iterator<Integer> it = idos.keySet().iterator();
		while(it.hasNext()){
			int ido = it.next();
			int idto = idos.get(ido);
			Iterator<Property> itprop = m_kba.getProperties(ido, idto, m_userRol, m_idtoUserTask, m_session);
			while(itprop.hasNext()){
				Property p = itprop.next();
				if(p.getTypeAccess().getViewAccess()){
					HashMap<Integer, LinkedList<Value>> properties;
					if(listIndividuos.get(ido)!=null){
						properties = listIndividuos.get(ido);
						properties.put(p.getIdProp(), p.getValues());					
					}else{
						properties = new HashMap<Integer, LinkedList<Value>>();
						properties.put(p.getIdProp(), p.getValues());
						listIndividuos.put(ido, properties);
					}
				}
			}			
		}

		// Comprobar que tienen las mismas properties
//		it = idos.keySet().iterator();
//		int id = it.next();
//		Iterator<Integer> itp = listIndividuos.get(id).keySet().iterator();
//		while(itp.hasNext()){
//			int idProp = itp.next();
//			it = idos.iterator();
//			while(it.hasNext()){
//				id=it.next();
//				if(!listIndividuos.get(id).containsKey(idProp)){
//
//				}				
//			}
//		}
		
		// Comprobar que tienen los mismos value
		HashMap<Integer, LinkedList<Value>> equals = new HashMap<Integer, LinkedList<Value>>();
		it = idos.keySet().iterator();
		int idInit = it.next();
		Iterator<Integer> itp = listIndividuos.get(idInit).keySet().iterator();
		
		m_formManager.cleanAll();
		
		while(itp.hasNext()){

			int idProp = itp.next();
			it = idos.keySet().iterator();
			LinkedList<LinkedList<Value>> listValues = new LinkedList<LinkedList<Value>>();

			while(it.hasNext()){
				int idInd=it.next();
				LinkedList<Value> val = listIndividuos.get(idInd).get(idProp);
				listValues.add(val);
			}

			LinkedList<Value> valuesToSet = getValuesInCommon(listValues);
			//System.err.println("ValuesToSet: "+valuesToSet);

			//if(valuesToSet.size()==1){					
				//Property property=m_kba.getProperty(m_ido, idProp, m_userRol, m_idtoUserTask, m_session);
				if(!m_kba.getCategoryProperty(idProp).isStructural()/* && property.getTypeAccess().getSetAccess()*/)
					equals.put(idProp, valuesToSet);
			//}
		}		

		Iterator<Integer> ite = equals.keySet().iterator();
		while(ite.hasNext()){
			int idProp = ite.next();
			LinkedList<Value> valuesEq = equals.get(idProp);
			Property property=m_kba.getProperty(m_ido, m_idto, idProp, m_userRol, m_idtoUserTask, m_session);

			IdObjectForm idObjectForm=new IdObjectForm();
			//System.err.println("\n\n\n|||| IDO: "+m_ido+"\n\n");
			idObjectForm.setIdo(m_ido);
			idObjectForm.setIdProp(idProp);
			if(property instanceof DataProperty || (property instanceof ObjectProperty && ((ObjectProperty)property).getEnumList().size()>0)){
				for(int i=0;i<valuesEq.size();i++){
					Value value = (Value)valuesEq.get(i);
					Object valueObject;
					if(value instanceof DataValue){
						valueObject=m_kba.getValueData((DataValue)value);
						idObjectForm.setValueCls(((DataProperty)property).getDataType());
					}else{
						valueObject=((ObjectValue)value).getValue();
						idObjectForm.setValueCls(((ObjectValue)value).getValueCls());
					}
					String ident=idObjectForm.getIdString();
					m_formManager.setValueComponent(ident, valueObject, null);
				}
			}
			else if(property instanceof ObjectProperty){
				selectData selectDataAdd=new selectData();
				String ident=null;
				for(int i=0;i<valuesEq.size();i++){

					ObjectValue value = (ObjectValue)valuesEq.get(i);

					idObjectForm.setValueCls(value.getValueCls());
					ident=idObjectForm.getIdString();
					ArrayList<String> listTables=m_formManager.getIdTables();
					if(!listTables.contains(ident)){//Si no existe esa tabla es que se trata de una tabla abstracta
						ident=getIdTableParent(idObjectForm, value.getValueCls(), listTables);
					}
//					
//					selectData source=new selectData();
//					source.addInstance(m_kba.getTreeObject(value.getValue(), m_userRol, m_idtoUserTask, m_session,KnowledgeBaseAdapter.TABLE_MODE));
//					m_formManager.addRows(ident, source, true);
					
					
//					ObjectProperty objectP=m_kba.getChild(m_ido, m_idto, idProp, m_userRol, m_idtoUserTask, m_session);
//					int idtoRange=m_kba.getClass(m_kba.getIdRange(objectP));
//					boolean structural=m_kba.getCategoryProperty(idProp).isStructural();
					selectDataAdd.addInstance(m_kba.getTreeObjectTable(value.getValue(),value.getValueCls(),ident,m_formManager.getColumnTreeOfTable(ident), m_userRol, m_idtoUserTask,m_session));
				}
				if(ident!=null){
					m_formManager.addRows(ident, selectDataAdd, true);
				}
			}
		}
	}

	private LinkedList<Value> getValuesInCommon(LinkedList<LinkedList<Value>> listValues) {

		Iterator<LinkedList<Value>> it = listValues.iterator();
		if(it.hasNext()){

			LinkedList<Value> valuesToSet = it.next();
			while(it.hasNext()){
				LinkedList<Value> l2 = it.next();
				getValuesInCommonFromTwoList(valuesToSet, l2);
			}
			return valuesToSet;
		}	

		return null;
	}

	private void getValuesInCommonFromTwoList(LinkedList<Value> result, LinkedList<Value> l2){
		LinkedList<Value> l1 = new LinkedList<Value>();
		for(int i=0;i<result.size();i++){
			Value val = result.get(i);
			Value clon = val.clone();
			l1.add(clon);
		}		
		for(int i=l1.size()-1;i>=0;i--){
			result.remove(i);
		}
		for(int i=0;i<l1.size();i++){
			Value value = l1.get(i);
			// TODO salen valores duplicados sin la condicion siguiente
			if(l2.contains(value)/* && !result.contains(value)*/)
				result.add(value);
		}
	}


	public boolean setValue(String id, Object value, Object valueOld,int valueCls,  int valueOldCls) throws OperationNotPermitedException, NotFoundException, IncoherenceInMotorException, ApplicationException, CardinalityExceedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, AssignValueException {

		IdObjectForm idObjectForm=new IdObjectForm(id);
		if(idObjectForm.getIdo().equals(m_ido)){
			DefaultSession df = m_kba.createDefaultSession(m_session, m_idtoUserTask, true, m_session.isRunRules(), m_session.isLockObjects(), m_session.isDeleteFilters(),false);
			boolean success=false;
			try{
				int idProp=idObjectForm.getIdProp();
				Iterator<Integer> itIdos=idos.keySet().iterator();
				while(itIdos.hasNext()){
					Integer ido=itIdos.next();
					int idto = idos.get(ido);
					Value valueObject = m_kba.buildValue(value, valueCls);
					Property property=m_kba.getProperty(ido, idto, idProp, m_userRol, m_idtoUserTask, df);
					Value valueOldObject;
					if(property.getCardMax()!=null && property.getCardMax()==1)
						valueOldObject=!property.getValues().isEmpty()?property.getUniqueValue():null;
					else
						valueOldObject= m_kba.buildValue(valueOld, valueCls);
					if(!property.getValues().contains(valueObject) && !Auxiliar.equals(valueObject,valueOldObject)){
						//System.err.println("Asigna en multiple valor en ido:"+ido+" idProp:"+idProp+" valueCls:"+valueCls+" siendo el value:"+valueObject+" y el valueOld:"+valueOldObject);
						m_kba.setValue(ido,idProp, valueObject, valueOldObject,m_userRol,m_idtoUserTask,df);
					}
				}
				//super.setValue(id, value, valueOld, valueCls);
				df.commit();
				success=true;
//			if(m_formManager.hasComponent(id))
//				m_formManager.setValueComponent(id, value, valueOld);
//			else{
//				ArrayList<String> listTables=m_formManager.getIdTables();
//				//System.out.println("Esta es la lista de ids de tablas:"+listTables);
//				//System.out.println("id a buscar:"+idString);
//				if(listTables.contains(id)){
//					//System.err.println("ChangeValue: ObjectValue con tabla");
//					if(valueOld!=null){
//						if(value==null || (value!=null && !value.equals(valueOld)) ){
//							instance inst=m_formManager.getDataTableFromIdo(id, Integer.valueOf(valueOld));//m_kba.getTreeObject(((ObjectValue)valueOld).getValue(), m_userRol, m_idtoUserTask,m_session,KnowledgeBaseAdapter.TABLE_MODE)
//							if(inst!=null){
//								selectData selectDataRemove=new selectData();
//								selectDataRemove.addInstance(inst);
//								m_formManager.delRows(id, selectDataRemove);
//							}
//						}
//					}
//					if(value!=null){
//						selectData selectDataAdd=new selectData();
//						ObjectProperty objectP=m_kba.getChild(m_ido, idProp, m_userRol, m_idtoUserTask, m_session);
//						int idtoRange=m_kba.getClass(m_kba.getIdRange(objectP));
//						selectDataAdd.addInstance(m_kba.getTreeObjectTable(Integer.valueOf(value),idtoRange,m_idto, m_userRol, m_idtoUserTask,m_session));
//						m_formManager.addRows(id, selectDataAdd, true);
//					}
//				}else{
//					id=getIdTableParent(idObjectForm, valueCls, listTables);
//					if(id!=null){//Si es distinto de null se trataria de un registro de una tabla abstracta
//						//System.err.println("ChangeValue: ObjectValue con tabla asbtracta");
//						if(valueOld!=null){
//							if(value==null || (value!=null && !value.equals(valueOld)) ){
//								instance inst=m_formManager.getDataTableFromIdo(id, Integer.valueOf(valueOld));//m_kba.getTreeObject(((ObjectValue)valueOld).getValue(), m_userRol, m_idtoUserTask,m_session,KnowledgeBaseAdapter.TABLE_MODE)
//								if(inst!=null){
//									selectData selectDataRemove=new selectData();
//									selectDataRemove.addInstance(inst);//m_kba.getTreeObject(((ObjectValue)valueOld).getValue(), m_userRol, m_idtoUserTask,m_session,KnowledgeBaseAdapter.TABLE_MODE));
//									m_formManager.delRows(id, selectDataRemove);
//								}
//							}
//						}
//						if(value!=null){
//							selectData selectDataAdd=new selectData();
//							ObjectProperty objectP=m_kba.getChild(m_ido, idProp, m_userRol, m_idtoUserTask, m_session);
//							int idtoRange=m_kba.getClass(m_kba.getIdRange(objectP));
//							selectDataAdd.addInstance(m_kba.getTreeObjectTable(Integer.valueOf(value),idtoRange,m_idto, m_userRol, m_idtoUserTask,m_session));
//							m_formManager.addRows(id, selectDataAdd, true);
//						}
//					}else{//Se podria tratar del registro de una de las tablas, recorremos todas las tablas buscando ese ido
//						//System.err.println("ChangeValue: ObjectValue buscando en todas las tablas");
//						//System.out.println("Esta es la lista de ids de tablas:"+listTables);
//						//System.out.println("id a buscar:"+idString);
//						Iterator<String> itr=listTables.iterator();
//						int idoSearch=m_ido;//!=null?m_ido:m_idto;//Solo sirve para las businessClass que sea idto
//						while(itr.hasNext()){
//							String idTable=itr.next();
//							int numRow=m_formManager.getRowCount(idTable);
//							for(int i=0;i<numRow;i++){
//								instance inst=m_formManager.getDataTableFromIndex(idTable, i);
//								m_kba.setInstance(inst);
//								Iterator<Property> itrProp=null;
//								try{
//									itrProp=m_kba.getProperties(idoSearch, m_userRol, m_idtoUserTask, m_session);
//								}finally{
//	    							m_kba.clearInstance();
//	    						}
//								if(itrProp.hasNext()){
//									if(valueOld!=null){
//										if(value==null/* || (value!=null && !value.equals(valueOld))*/ ){
//											selectData selectDataAdd=new selectData();
//											selectDataAdd.addInstance(/*inst*/m_kba.getTreeObjectTable(inst.getIDO(),inst.getIdTo(),m_idto, m_userRol, m_idtoUserTask,m_session));
//											//m_formManager.delRows(idTable, selectDataRemove);
//											m_formManager.addRows(idTable, selectDataAdd, true);
//										}
//									}
//									if(value!=null){
//										selectData selectDataAdd=new selectData();
//										int idtoRange=m_kba.getClass(new IdObjectForm(idTable).getValueCls());										
//										//Creamos un nuevo treeObject ya que si el valor es de base de datos el instance anterior esta obsoleto ya que no tiene los datos de ese individuo
//										//Para ellos usamos ido y no value porque se trata de un objectProperty que se le ha añadido al ido del instance de una fila
//										selectDataAdd.addInstance(m_kba.getTreeObjectTable(inst.getIDO(),idtoRange/*inst.getIdTo()*/,m_idto, m_userRol, m_idtoUserTask,m_session));
//										//System.out.println("registro tabla:"+selectData.getFirst());
//										/*if(operation==action.DEL){
//											if(value!=null)//TODO Esta comprobacion hay que quitarla cuando las sesiones avisen correctamente y no con null
//												m_formManager.delRows(idTable, selectData);
//										}else*/ m_formManager.addRows(idTable, selectDataAdd, true);
//									}
//								}
//							}
//						}
//					}
//				}
//			}
		}finally{
			if(!success)
				df.rollBack();
		}
		}else{
			super.setValue(id, value, valueOld, valueCls, valueOldCls);
		}
		copySameValues();
		hasUserModified=true;
		return true;
	}

	@Override
	public void changeValue(Integer ido, int idto, int idProp, int valueCls, Value value, Value valueOld, int level, int operation) throws ParseException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException {
		//super.changeValue(ido, idto, idProp, valueCls, value, valueOld, level,operation);
		if(ido==m_ido && !dialog.getComponent().isActive() && m_session.getState()==Session.USE_STATE){
			if(idos!=null){
				Iterator<Integer> itIdos=idos.keySet().iterator();
				while(itIdos.hasNext()){
					int idoM=itIdos.next();
					int idtoM=idos.get(idoM);
					Value valueObject = value;
					Property property=m_kba.getProperty(idoM, idtoM, idProp, m_userRol, m_idtoUserTask, m_session);
					Value valueOldObject=null;
					if(property.getCardMax()!=null && property.getCardMax()==1)
						valueOldObject=property.getUniqueValue();
					
					if(!property.getValues().contains(valueObject) && !Auxiliar.equals(valueObject,valueOldObject)){
						//System.err.println("Asigna en multiple valor en ido:"+idoM+" idProp:"+idProp+" valueCls:"+valueCls+" siendo el value:"+valueObject+" y el valueOld:"+valueOldObject);
						m_kba.setValue(idoM,idProp, valueObject, valueOldObject,m_userRol,m_idtoUserTask,m_session);
					}
				}
				/*try {
					getSameValues();
				} catch (AssignValueException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}
		}
	}
	
	@Override
	protected void setRelationTable(String idTable, int idoRow, int idtoRow, Session sessionParent, Session session) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, SQLException, NamingException, JDOMException, ParseException, EditionTableException, NumberFormatException, AssignValueException {
		//super.setRelationTable(idTable, idoRow, sessionParent, session);
		
		IdObjectForm idObjectForm=new IdObjectForm(idTable);
		int idProp=idObjectForm.getIdProp();
		Integer valueCls=idObjectForm.getValueCls();
		
			Iterator<Integer> itIdos=idos.keySet().iterator();
			while(itIdos.hasNext()){
				int ido=itIdos.next();
				int idto=idos.get(ido);
				Value valueObject = m_kba.buildValue(idoRow, idtoRow);
				Property property=m_kba.getProperty(ido, idto, idProp, m_userRol, m_idtoUserTask, session);
				Value valueOldObject=null;
				if(property.getCardMax()!=null && property.getCardMax()==1)
					valueOldObject=property.getUniqueValue();
				
				if(!property.getValues().contains(valueObject) && !Auxiliar.equals(valueObject,valueOldObject)){
					//System.err.println("Asigna en multiple valor en ido:"+ido+" idProp:"+idProp+" valueCls:"+valueCls+" siendo el value:"+valueObject+" y el valueOld:"+valueOldObject);
					m_kba.setValue(ido,idProp, valueObject, valueOldObject,m_userRol,m_idtoUserTask,session);
				}
			}
			//super.setValue(id, value, valueOld, valueCls);
		
		selectData selectDataAdd=new selectData();
//		ObjectProperty objectP=m_kba.getChild(m_ido, m_idto, idProp, m_userRol, m_idtoUserTask, session);
//		int idtoRange=m_kba.getClass(m_kba.getIdRange(objectP));
//		boolean structural=m_kba.getCategoryProperty(idProp).isStructural();
		selectDataAdd.addInstance(m_kba.getTreeObjectTable(idoRow,idtoRow,idTable,m_formManager.getColumnTreeOfTable(idTable), m_userRol, m_idtoUserTask,session));
		m_formManager.addRows(idTable, selectDataAdd, true);
		
	}

	@Override
	public boolean removeRowTable(String id, int idoRow, int idtoRow) throws AssignValueException, NotValidValueException {
		try{
			dialog.disabledEvents();
			IdObjectForm idObjectForm=new IdObjectForm(id);
			int idProp=idObjectForm.getIdProp();
			Integer valueCls=idObjectForm.getValueCls();
			
			//Puede que un cancelEditionTable anterior haya quitado ya estos enlaces
			if(m_kba.isPointed(idoRow, idtoRow)){
				Iterator<Integer> itIdos=idos.keySet().iterator();
				while(itIdos.hasNext()){
					int ido=itIdos.next();
					
					Value valueObject = null;
					Value valueOldObject=m_kba.buildValue(idoRow, idtoRow);
					
					if(!Auxiliar.equals(valueObject,valueOldObject)){
						//System.err.println("Asigna en multiple valor en ido:"+ido+" idProp:"+idProp+" valueCls:"+valueCls+" siendo el value:"+valueObject+" y el valueOld:"+valueOldObject);
						m_kba.setValue(ido,idProp, valueObject, valueOldObject,m_userRol,m_idtoUserTask,m_session);
					}
				}
			}
			m_formManager.delRow(id, new GIdRow(idoRow,idtoRow,null));
			
		} catch (CardinalityExceedException ex) {
			NotValidValueException e;
			try {
				e=new NotValidValueException(ex.getMessage()+" "+ex.getCause());
				e.setStackTrace(ex.getStackTrace());
				Property prop=ex.getProp();
				if (prop!=null){
					if (prop.getIdo()!=m_ido){
						e.setUserMessage(((RuleEngineException)ex).getUserMessage()+": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask) + " de "+m_kba.getLabelClass(prop.getIdto(), m_idtoUserTask)+" '"+m_kba.getValueData(m_kba.getRDN(prop.getIdo(), prop.getIdto(), m_userRol, m_idtoUserTask, m_session))+"'");	
					}else{
						e.setUserMessage(((RuleEngineException)ex).getUserMessage()+": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask));
					}
//					e.setUserMessage(((RuleEngineException)ex).getUserMessage()+": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_kba.getGroup(prop.getIdProp(),prop.getIdto(),m_idtoUserTask),m_idtoUserTask));
				}else{
					e.setUserMessage(((RuleEngineException)ex).getUserMessage());
				}
			} catch (Exception exec){
				AssignValueException e2=new AssignValueException(exec.getMessage()+" "+exec.getCause());
				e2.setStackTrace(exec.getStackTrace());
				throw e2;
			}
			throw e;
		} catch (IncompatibleValueException ex) {
			NotValidValueException e;
			try {
				e=new NotValidValueException(ex.getMessage()+" "+ex.getCause());
				e.setStackTrace(ex.getStackTrace());
				e.setUserMessage(((RuleEngineException)ex).getUserMessage());
			} catch (Exception exec){
				AssignValueException e2=new AssignValueException(exec.getMessage());
				e2.setStackTrace(exec.getStackTrace());
				throw e2;
			}
			throw e;
		} catch (OperationNotPermitedException ex) {
			NotValidValueException e;
			try {
				e=new NotValidValueException(ex.getMessage()+" "+ex.getCause());
				e.setStackTrace(ex.getStackTrace());
				e.setUserMessage(((RuleEngineException)ex).getUserMessage());
			} catch (Exception exec){
				AssignValueException e2=new AssignValueException(ex.getMessage()+" "+ex.getCause());
				e2.setStackTrace(exec.getStackTrace());
				throw e2;
			}
			throw e;
		}catch(ServerException ex){
			ex.printStackTrace();
			AssignValueException e=new AssignValueException(ex.getMessage()+" "+ex.getCause());
			e.setUserMessage(ex.getUserMessage());
			e.setStackTrace(ex.getStackTrace());
			throw e;
		}catch(RuleEngineException ex){
			ex.printStackTrace();
			AssignValueException e=new AssignValueException(ex.getMessage()+" "+ex.getCause());
			e.setUserMessage(ex.getUserMessage());
			e.setStackTrace(ex.getStackTrace());
			throw e;
		}catch(Exception ex){
			ex.printStackTrace();
			AssignValueException e=new AssignValueException(ex.getMessage()+" "+ex.getCause());
			e.setStackTrace(ex.getStackTrace());
			throw e;
		}finally{
			dialog.enabledEvents();
		}

		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		possibleChanges=true;
		super.actionPerformed(e);
	}

	
//	@Override LO NECESITARÍAMOS SI m_ido FUERA UN PROTOTIPO. DE MOMENTO ES UN FILTRO PERO SI NOS DIERA PROBLEMAS TENDRIAMOS QUE CONVERTIRLO EN PROTOTIPO Y DESCOMENTAR ESTE CÓDIGO PARA EVITAR QUE VAYA A BASE DE DATOS
//	public String confirm() throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, OperationNotPermitedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException{
//		DefaultSession sessionConfirm=m_kba.createDefaultSession(m_session,m_idtoUserTask,m_session.isCheckCoherenceObjects(),m_session.isRunRules(),m_session.isLockObjects(),m_session.isDeleteFilters(),true);
//		sessionConfirm.addIchangeProperty(this, false);
//		Session oldSession=m_session;
//		m_session=sessionConfirm;
//		boolean success=false;
//		String mensajeRespuesta=null;
//		try{
//			DataProperty propRDN=m_kba.getRDN(m_ido, m_idto, m_userRol, m_idtoUserTask, m_session);
//			String rdn=(String)m_kba.getValueData(propRDN);
//			m_kba.deleteObject(m_ido, m_idto, rdn, m_userRol, m_idtoUserTask, m_session);
//			mensajeRespuesta=super.confirm();
//			success=true;
//		}finally{
//			if(!success){
//				m_session=oldSession;
//				sessionConfirm.setForceParent(false);
//				try{
//					sessionConfirm.rollBack();
//				}catch(Exception ex){
//					System.err.println("No se ha podido hacer rollback de la session");
//					ex.printStackTrace();
//				}
//			}
//		}
//		return mensajeRespuesta;
//	}
	
	
}
