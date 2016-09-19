package dynagent.gui.forms;


import java.awt.AWTException;
import java.awt.HeadlessException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.naming.NamingException;
import javax.swing.JOptionPane;

import org.jdom.JDOMException;

import gdev.gawt.utils.ITableNavigation;
import gdev.gen.AssignValueException;
import gdev.gen.DictionaryWord;
import gdev.gen.EditionTableException;
import gdev.gen.GConfigView;
import gdev.gen.IComponentListener;
import gdev.gen.IDictionaryFinder;
import gdev.gen.NotValidValueException;
import dynagent.common.Constants;
import dynagent.common.communication.communicator;
import dynagent.common.communication.queryData;
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
import dynagent.common.knowledge.IMessageListener;
import dynagent.common.knowledge.PropertyValue;
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.UserAccess;
import dynagent.common.knowledge.access;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.IChangePropertyListener;
import dynagent.common.sessions.Session;
import dynagent.common.utils.AccessAdapter;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.DebugLog;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.Utils;
import dynagent.gui.WindowComponent;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.actions.ActionManager;
import dynagent.gui.actions.IFormData;
import dynagent.gui.actions.commands.SetCommandPath;
import dynagent.gui.actions.commands.commandPath;
import dynagent.gui.forms.builders.FormManager;
import dynagent.gui.forms.utils.ActionException;
import dynagent.ruleengine.src.sessions.DefaultSession;

public abstract class FormControl implements IComponentListener, IFormData, IChangePropertyListener, IDictionaryFinder {
	protected KnowledgeBaseAdapter m_kba;
	protected Integer m_idtoUserTask;
	protected WindowComponent dialog;
	protected ActionManager m_actionManager;
	protected int m_ido;
	protected int m_idto;
	protected Integer m_userRol;
	protected Session m_session;
	protected Session m_sessionMaster;
	protected communicator m_com;
	
	public FormControl(KnowledgeBaseAdapter kba,WindowComponent dialog,Integer idtoUserTask, Integer userRol, Session sess, int ido, int idto){
		m_kba=kba;
		m_ido=ido;
		m_idto=idto;
		m_actionManager=Singleton.getInstance().getActionManager();
		this.dialog=dialog;
		m_idtoUserTask=idtoUserTask;
		m_userRol=userRol;
		m_session=sess;
		m_sessionMaster=sess;
		
		m_com = kba.configServer(idtoUserTask);
		if(m_com==null){
			//Singleton.getInstance().getMessagesControl().showErrorMessage("Al ejecutar la aplicación es necesario el parametro globalurl para los menús que actuan directamente en la central.\nSi accede a ellos trabajará en local", dialog.getComponent());
			m_com=Singleton.getInstance().getComm();
			kba.setServer(m_com);
		}
	}
	
	public KnowledgeBaseAdapter getKnowledgeBase(){
		return m_kba;
	}
	
	public boolean setValueField(String id, Object value, Object valueOld,int valueCls,int valueOldCls) throws AssignValueException, NotValidValueException {
		try{
			dialog.disabledEvents();
			return setValue(id,value,valueOld,valueCls,valueOldCls);
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
				AssignValueException e2=new AssignValueException(ex.getMessage()+" "+ex.getCause());
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
	}
	
	public boolean addValueField(String id, Object value, int valueCls) throws AssignValueException, NotValidValueException {

		try{
			dialog.disabledEvents();
			return setValue(id,value,null,valueCls,valueCls);
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
				AssignValueException e2=new AssignValueException(exec.getMessage()+" "+exec.getCause());
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
			AssignValueException e=new AssignValueException(ex.getMessage());
			e.setStackTrace(ex.getStackTrace());
			throw e;
		}finally{
			dialog.enabledEvents();
		}
	}

	public boolean removeValueField(String id, Object value, int valueCls) throws AssignValueException, NotValidValueException {
		try{
			dialog.disabledEvents();
			return setValue(id,null,value,valueCls,valueCls);
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
			AssignValueException e=new AssignValueException(ex.getMessage()+" "+ex.getCause());
			e.setStackTrace(ex.getStackTrace());
			throw e;
		}finally{
			dialog.enabledEvents();
		}
	}

	public boolean setValue(String id, Object value, Object valueOld, int valueCls, int valueOldCls) throws OperationNotPermitedException, NotFoundException, IncoherenceInMotorException, ApplicationException, CardinalityExceedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, AssignValueException{ 
		
		IdObjectForm idObjectForm=new IdObjectForm(id);
		Integer ido=idObjectForm.getIdo();
		Integer idProp=idObjectForm.getIdProp();

		Value valueObject = m_kba.buildValue(value, valueCls);
		Value valueOldObject = m_kba.buildValue(valueOld, valueOldCls);

		return doSetValue(ido, idProp, valueObject, valueOldObject);
	}
	
	protected boolean doSetValue(int ido, int idProp, Value valueObject, Value valueOldObject) throws OperationNotPermitedException, NotFoundException, IncoherenceInMotorException, ApplicationException, CardinalityExceedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {
		if(!m_session.isFinished()){
			//System.err.println("Asigna valor en ido:"+ido+" idProp:"+idProp+" siendo el value:"+valueObject+" y el valueOld:"+valueOldObject);
			m_kba.setValue(ido,idProp, valueObject, valueOldObject,m_userRol,m_idtoUserTask,m_session);
			return true;
		}else{
			System.err.println("WARNING: Intento de hacer un setValue con una sesion finalizada siendo Value:"+valueObject+" valueOld:"+valueOldObject);
			return false;
		}
	}
	
	public Integer newRowTable(String id,Integer idtoRow) throws AssignValueException, NotValidValueException, EditionTableException {
		Integer idoFilter=null;
		Integer idtoFilter=null;
		boolean success=false;
		DefaultSession sessionAux=m_kba.createDefaultSession(m_session,m_idtoUserTask,false,true,m_session.isLockObjects(),true,false);
		try{
			dialog.disabledEvents();
			IdObjectForm idObjectForm=new IdObjectForm(id);
			Integer ido=idObjectForm.getIdo();
			Integer idto=m_kba.getClass(ido);
			Integer idProp=idObjectForm.getIdProp();
			Integer valueCls=idObjectForm.getValueCls();
			
			int idRange;
			if(idtoRow==null || Auxiliar.equals(m_kba.getClass(valueCls), m_kba.getClass(idtoRow))){
				//Utilizamos el filtro por si tuviera algo indicado que nos interesa para el prototipo
				ObjectProperty property=m_kba.getChild(ido,idto,idProp,m_userRol,m_idtoUserTask,sessionAux);
				idRange=m_kba.getIdRange(property, m_kba.getClass(valueCls));
			}else idRange=idtoRow;
			
			idoFilter=m_kba.createPrototype(idRange, Constants.LEVEL_PROTOTYPE, m_userRol, m_idtoUserTask,sessionAux);
			idtoFilter=m_kba.getClass(idoFilter);
			
			setRelationTable(id, idoFilter, idtoFilter, m_session, sessionAux);
			
			startEdition(idoFilter,sessionAux);
			sessionAux.commit();
			success=true;
		} catch (CardinalityExceedException ex) {
			NotValidValueException e;
			try {
				e=new NotValidValueException(ex.getMessage()+" "+ex.getCause());
				e.setStackTrace(ex.getStackTrace());
				Property prop=ex.getProp();
				if (prop!=null){
					if (prop.getIdo()!=m_ido){
						e.setUserMessage(((RuleEngineException)ex).getUserMessage()+": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask) + " de "+m_kba.getLabelClass(prop.getIdto(), m_idtoUserTask)+" '"+m_kba.getValueData(m_kba.getRDN(prop.getIdo(), prop.getIdto(), m_userRol, m_idtoUserTask, sessionAux))+"'");	
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
				AssignValueException e2=new AssignValueException(exec.getMessage()+" "+ex.getCause());
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
				AssignValueException e2=new AssignValueException(exec.getMessage()+" "+exec.getCause());
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
		}catch(EditionTableException ex){
			throw ex;
		}catch(Exception ex){
			AssignValueException e=new AssignValueException(ex.getMessage()+" "+ex.getCause());
			e.setStackTrace(ex.getStackTrace());
			throw e;
		}finally{
			try{
				if(!success)
					sessionAux.rollBack();
			} catch (Exception exec){
				AssignValueException e2=new AssignValueException(exec.getMessage()+" "+exec.getCause());
				e2.setStackTrace(exec.getStackTrace());
				throw e2;
			}
			dialog.enabledEvents();
		}

		return idoFilter;
	}
	
	protected void setRelationTable(String idTable,int idoRow,int idtoRow,Session sessionParent,Session session) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, SQLException, NamingException, JDOMException, ParseException, EditionTableException, NumberFormatException, AssignValueException{
		setRelation(idTable, idoRow, idtoRow, sessionParent, session);
	}
	
	private void setRelation(String idTable,int idoRow,int idtoRow,Session sessionParent,Session session) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, SQLException, NamingException, JDOMException, ParseException{
		IdObjectForm idObjectForm=new IdObjectForm(idTable);
		Integer ido=idObjectForm.getIdo();
		Integer idProp=idObjectForm.getIdProp();
		Integer valueCls=idObjectForm.getValueCls();
		
		Value valueObject = m_kba.buildValue(idoRow, idtoRow);
		Value valueOldObject = null;
		//System.err.println("Asigna valor en ido:"+ido+" idProp:"+idProp+" valueCls:"+valueCls+" siendo el value:"+valueObject+" y el valueOld:"+valueOldObject);
		m_kba.setValue(ido,idProp, valueObject, valueOldObject,m_userRol,m_idtoUserTask,session);
	}
	
	public boolean removeRowTable(String id,int idoRow,int idtoRow) throws AssignValueException, NotValidValueException {
		try{
			dialog.disabledEvents();
			/*IdObjectForm idObjectForm=new IdObjectForm(id);
			Integer ido=idObjectForm.getIdo();
			Integer idProp=idObjectForm.getIdProp();
			Integer valueCls=idObjectForm.getValueCls();
			
			m_kba.setValue(ido, idProp, null, m_kba.getValueOfString(idoRow, valueCls), m_userRol, m_idtoUserTask, m_session);*/
			//int idtoRow=m_kba.getClass(idoRow);
			setValue(id,null,idoRow,idtoRow,idtoRow);
			
			//m_kba.deleteObject(idoRow, m_kba.getClass(idoRow), null, m_userRol, m_idtoUserTask,m_session);
			
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
	
	public Integer newSubRowTable(String idParentColumn,Integer idtoRow) throws AssignValueException, NotValidValueException{
		Integer idoFilter=null;
		Integer idtoFilter=null;
		boolean success=false;
		DefaultSession sessionAux=m_kba.createDefaultSession(m_session,m_idtoUserTask,false,true,m_session.isLockObjects(),true,false);
		try{
			dialog.disabledEvents();
			IdObjectForm idObjectForm=new IdObjectForm(idParentColumn);
			Integer ido=idObjectForm.getIdo();
			Integer idto=m_kba.getClass(ido);
			Integer idProp=idObjectForm.getIdProp();
			Integer valueCls=idObjectForm.getValueCls();
			
			int idRange;
			if(idtoRow==null || Auxiliar.equals(m_kba.getClass(valueCls), m_kba.getClass(idtoRow))){
				//Utilizamos el filtro por si tuviera algo indicado que nos interesa para el prototipo
				ObjectProperty property=m_kba.getChild(ido,idto,idProp,m_userRol,m_idtoUserTask,sessionAux);
				idRange=m_kba.getIdRange(property, m_kba.getClass(valueCls));
			}else idRange=idtoRow;
			
			idoFilter=m_kba.createPrototype(idRange, Constants.LEVEL_PROTOTYPE, m_userRol, m_idtoUserTask,sessionAux);
			idtoFilter=m_kba.getClass(idoFilter);
			
			setRelation(idParentColumn, idoFilter, idtoFilter, m_session, sessionAux);
			
			startEdition(idoFilter,sessionAux);
			sessionAux.commit();
			success=true;
		} catch (CardinalityExceedException ex) {
			NotValidValueException e;
			try {
				e=new NotValidValueException(ex.getMessage()+" "+ex.getCause());
				e.setStackTrace(ex.getStackTrace());
				Property prop=ex.getProp();
				if (prop!=null){
					if (prop.getIdo()!=m_ido){
						e.setUserMessage(((RuleEngineException)ex).getUserMessage()+": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask) + " de "+m_kba.getLabelClass(prop.getIdto(), m_idtoUserTask)+" '"+m_kba.getValueData(m_kba.getRDN(prop.getIdo(), prop.getIdto(), m_userRol, m_idtoUserTask, sessionAux))+"'");	
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
				AssignValueException e2=new AssignValueException(exec.getMessage()+" "+ex.getCause());
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
				AssignValueException e2=new AssignValueException(exec.getMessage()+" "+exec.getCause());
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
			AssignValueException e=new AssignValueException(ex.getMessage()+" "+ex.getCause());
			e.setStackTrace(ex.getStackTrace());
			throw e;
		}finally{
			try{
				if(!success)
					sessionAux.rollBack();
			} catch (Exception exec){
				AssignValueException e2=new AssignValueException(exec.getMessage()+" "+exec.getCause());
				e2.setStackTrace(exec.getStackTrace());
				throw e2;
			}
			dialog.enabledEvents();
		}

		return idoFilter;
	}
	
	public void initChangeValue() {
		// TODO Auto-generated method stub
		
	}
	
	public void changeValue(Integer ido,int idto,int idProp,int valueCls,Value value,Value oldValue,int level,int operation) throws ParseException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException{
		System.err.println("WARNING: changeValue no implementado por "+this.getClass());
	}
	
	public void endChangeValue() throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NumberFormatException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException {
		// TODO Auto-generated method stub
	}
	
	public void stopEdition(int ido,Session sess) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		System.err.println("WARNING: stopEdition no implementado por "+this.getClass());
	}
	
	public void startEdition(int ido,Session sess) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		System.err.println("WARNING: startEdition no implementado por "+this.getClass());
	}
	
	public void stopEdition(ArrayList<Integer> idos,Session sess) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		System.err.println("WARNING: stopEdition de idos no implementado por "+this.getClass());
	}
	
	public void startEdition(ArrayList<Integer> idos,Session sess) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		System.err.println("WARNING: startEdition de idos no implementado por "+this.getClass());
	}
	
	public void startEditionTable(String idTable,Integer idoRow,boolean pastingRow) throws EditionTableException{
		boolean success=false;
		Singleton.getInstance().getDebugLog().addDebugData(DebugLog.DEBUG_GUI,"startEditionTable","idTable=("+new IdObjectForm(idTable)+") idoRow:"+idoRow);
		if(m_session!=m_sessionMaster){
			System.err.println("ERROR: Intento de edicion de la fila de una tabla estando pendiente la edicion de otra fila");
			Auxiliar.printCurrentStackTrace();
			m_com.logError(dialog.getComponent(),new Exception("Intento de editar en tabla "+idTable+" el individuo:"+idoRow+" estando editandose otro individuo"), "Error en el evento de edicion de la tabla. El error será registrado en el servidor y posiblemente podrá continuar trabajando correctamente");
			try{
				m_session.commit();
			}catch(Exception ex){
				ex.printStackTrace();
				try{
					m_session.rollBack();
				}catch(Exception e){
					e.printStackTrace();
					m_com.logError(dialog.getComponent(),e, "Error en el evento de edición de la tabla. Error al cancelar una edición anterior");
				}
			}
		}
		
		boolean checkCoherence=m_sessionMaster.isCheckCoherenceObjects();
		if(pastingRow){
			checkCoherence=false;
		}
		//Session sessionActual=m_session;
		m_session=m_kba.createDefaultSession(m_sessionMaster,m_idtoUserTask,checkCoherence,m_sessionMaster.isRunRules(),m_sessionMaster.isLockObjects(),m_sessionMaster.isDeleteFilters(),false);
		dialog.disabledEvents();
		try{
			//System.err.println("startEditionTable idTable:"+idTable+" idoRow:"+idoRow+" session:"+m_session+" sessionMaster:"+m_sessionMaster);

			m_session.addIchangeProperty(this, false);
			if(idoRow!=null)
				startEdition(idoRow,m_session);
			success=true;
		}catch(IncompatibleValueException e){
			e.printStackTrace();
			
			String message=e.getUserMessage();
			EditionTableException ex=new EditionTableException(e.getMessage(),true,idoRow);
			ex.setUserMessage(message);
			throw ex;

			
		}catch(CardinalityExceedException e){
			e.printStackTrace();
			
			Property prop=e.getProp();
			String message=e.getUserMessage();
			if (prop!=null){
				try{
					if (prop.getIdo()!=m_ido){
						message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask)+ " de "+m_kba.getLabelClass(prop.getIdto(), m_idtoUserTask);
						String rdn=(String)m_kba.getValueData(m_kba.getRDN(prop.getIdo(), prop.getIdto(), m_userRol, m_idtoUserTask, m_session));
						if(rdn!=null)
							message+=" '"+rdn+"'";						
					}else{
						message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask);
					}
				}catch(Exception ex){
					m_com.logError(dialog.getComponent(),ex, "Error al mostrar error en la edición de la fila");
				}
			}

			EditionTableException ex=new EditionTableException(e.getMessage(),false,prop!=null?prop.getIdo():null);
			ex.setUserMessage(message);
			throw ex;

		}catch(Exception ex){
			ex.printStackTrace();
			EditionTableException e=new EditionTableException(ex.getMessage(),true,null);
			String userMessage;
			if (ex instanceof RuleEngineException){
				userMessage=((RuleEngineException)ex).getUserMessage();
			}else if (ex instanceof ServerException){
				userMessage=((ServerException)ex).getUserMessage();
			}else{
				userMessage="Error al iniciar edición de la fila";
			}
			e.setUserMessage(userMessage);
			throw e;
		}finally{
			dialog.enabledEvents();
			//Ya se encarga elecom despues de llamar a cancelEditionTable
//				try{
//					Session sessionAux=m_session;
//					m_session=sessionActual;
//					sessionAux.rollBack();
//				}catch(Exception ex){
//					System.err.println("ERROR:Session de stopEditionTable no ha podido cancelarse");
//					ex.printStackTrace();
//					m_com.logError(dialog.getComponent(),ex, "Error al cancelar edición de la fila");
//				}
//			}
		}
	}
	
	public void stopEditionTable(String idTable,Integer idoRow,HashSet<Integer> idosEdited,boolean pastingRow) throws EditionTableException{
		//System.err.println("stopEditionTable idTable:"+idTable+" idoRow:"+idoRow+" idosEdited:"+idosEdited+" session:"+m_session+" sessionMaster:"+m_sessionMaster);
		Singleton.getInstance().getDebugLog().addDebugData(DebugLog.DEBUG_GUI,"stopEditionTable","idTable=("+new IdObjectForm(idTable)+") idoRow:"+idoRow);
		if(m_session!=m_sessionMaster){
			boolean checkCoherence=m_session.isCheckCoherenceObjects();
			if(pastingRow){
				checkCoherence=false;
			}
			
			DefaultSession sessionConfirm=m_kba.createDefaultSession(m_session,m_idtoUserTask,checkCoherence,m_session.isRunRules(),m_session.isLockObjects(),m_session.isDeleteFilters(),true);
			//System.err.println("sessionConfirm:"+sessionConfirm);
			boolean success=false;
			String mensajeRespuesta=null;
			Session sessionActual=m_session;
			dialog.disabledEvents();
			try{
				sessionConfirm.addIchangeProperty(this, false);
				if(idoRow!=null){
					Iterator<Integer> itr=idosEdited.iterator();
					while(itr.hasNext()){
						int ido=itr.next();
						if(ido!=m_ido)//No nos interesa desbloquear el formulario padre
							stopEdition(ido,sessionConfirm);
					}
					//if(!idosEdited.contains(idoRow))
					//	stopEdition(idoRow,sessionConfirm);
				}
				m_session=m_sessionMaster;
				sessionConfirm.commit();
				success=true;
				//System.out.println("Despues del commit:"+m_kba.getSizeMotor());
			}catch(IncompatibleValueException e){
				e.printStackTrace();
				
				String message=e.getUserMessage();
				
				EditionTableException ex=new EditionTableException(e.getMessage(),true,idoRow);
				ex.setUserMessage(message);
				throw ex;

				
			}catch(CardinalityExceedException e){
				e.printStackTrace();
				Property prop=e.getProp();
				//System.err.println("Property:"+prop);
				String message=e.getUserMessage();
				if (prop!=null){
					try{
						if (prop.getIdo()!=m_ido){
							message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask)+ " de "+m_kba.getLabelClass(prop.getIdto(), m_idtoUserTask);
							String rdn=(String)m_kba.getValueData(m_kba.getRDN(prop.getIdo(), prop.getIdto(), m_userRol, m_idtoUserTask, m_session));
							if(rdn!=null)
								message+=" '"+rdn+"'";
						}else{
							message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask);
						}
					}catch(Exception ex){
						m_com.logError(dialog.getComponent(),ex, "Error al mostrar error en la edición de la fila");
					}
				}
	
				EditionTableException ex=new EditionTableException(e.getMessage(),false,prop!=null?prop.getIdo():null);
				ex.setUserMessage(message);
				throw ex;

			}catch(Exception ex){
				ex.printStackTrace();
				EditionTableException e=new EditionTableException(ex.getMessage(),true, null);
				String userMessage;
				if (ex instanceof RuleEngineException){
					userMessage=((RuleEngineException)ex).getUserMessage();
				}else if (ex instanceof ServerException){
					userMessage=((ServerException)ex).getUserMessage();
				}else{
					userMessage="Error al terminar edición de la fila";
				}
				e.setUserMessage(userMessage);
				throw e;
			}finally{
				if(!success){
					try{
						m_session=sessionActual;
						sessionConfirm.setForceParent(false);
						sessionConfirm.rollBack();
					}catch(Exception ex){
						System.err.println("ERROR:Session de stopEditionTable no ha podido cancelarse");
						ex.printStackTrace();
						m_com.logError(dialog.getComponent(),ex, "Error al cancelar edición de la fila");
					}
				}
				dialog.enabledEvents();
			}
				
			
		}else{
			System.err.println("ERROR: Intento de detener la edicion de la fila de una tabla sin estar editandose");
			Auxiliar.printCurrentStackTrace();
			m_com.logError(dialog.getComponent(),new Exception("Intento de detener la edicion de la fila de una tabla "+idTable+" sin estar editandose el individuo:"+idoRow), "Error en el evento de edicion de la tabla. El error será registrado en el servidor y posiblemente podrá continuar trabajando correctamente");
		}
	}
	
	public void cancelEditionTable(String idTable,Integer idoRow) throws EditionTableException, AssignValueException{
		Singleton.getInstance().getDebugLog().addDebugData(DebugLog.DEBUG_GUI,"cancelEditionTable","idTable=("+new IdObjectForm(idTable)+") idoRow:"+idoRow);
		Session sessionActual=m_session;
		try{
			m_session=m_sessionMaster;
			sessionActual.rollBack();
			
		} catch (Exception ex) {
			String message=null;
			if (ex instanceof RuleEngineException){
				message=((RuleEngineException)ex).getUserMessage();
			}
			if (ex instanceof ServerException){
				message=((ServerException)ex).getUserMessage();
			}
			EditionTableException e=new EditionTableException(ex.getMessage(),true, null);
			e.setUserMessage(message);
			e.setStackTrace(ex.getStackTrace());
			throw e;
		}
	}
	
	
	public void editInForm(int idoParent,int idoToEdit) throws EditionTableException{
		Singleton.getInstance().getDebugLog().addDebugData(DebugLog.DEBUG_GUI,"editRowTableInForm","idoParent="+idoParent+" idoToEdit:"+idoToEdit);
		//Integer idProp=idObjectForm.getIdProp();
		//Integer valueCls=idObjectForm.getValueCls();
		dialog.disabledEvents();
		try{
			commandPath commandPath=new SetCommandPath(idoParent,m_kba.getClass(idoParent),idoToEdit, m_kba.getClass(idoToEdit), m_idtoUserTask, m_userRol, m_session);
			ArrayList<commandPath> commandList = new ArrayList<commandPath>();
			commandList.add(commandPath);
			exeActions(commandList,null,dialog);
		} catch (Exception ex) {
			String message=null;
			if (ex instanceof RuleEngineException){
				message=((RuleEngineException)ex).getUserMessage();
			}
			if (ex instanceof ServerException){
				message=((ServerException)ex).getUserMessage();
			}
			EditionTableException e=new EditionTableException(ex.getMessage(),true, null);
			e.setStackTrace(ex.getStackTrace());
			e.setUserMessage(message);
			throw e;
		} finally{
			dialog.enabledEvents();
		}
	}
	
	//synchronized para evitar asignar dos valores a la misma columna. Esto ocurre si se pulsa enter sobre la celda justo en el momento en que se estaba construyendo las sugerencias
	//ya que el enter provoca una busqueda exactQuery a base de datos
	public synchronized boolean getDictionary(String idTable, String idColumn, String root, boolean exactQuery, LinkedHashMap<String, DictionaryWord> words){
		System.err.println("getDictionary idTable:"+idTable+" idColumn:"+idColumn+" root:"+root);
		//Auxiliar.printCurrentStackTrace();
		boolean isAppliedLimit=true;
		try{
			DefaultSession session=m_kba.createDefaultSession(m_session,m_idtoUserTask,false,true,false,false,false);
			session.setRulesGroup(Constants.FINDERRULESGROUP);
			try{
				/*IdObjectForm idObjectFormTable=new IdObjectForm(idTable);
				int idoTable=idObjectFormTable.getIdo();
				int idPropTable=idObjectFormTable.getIdProp();
				int valueClsTable=idObjectFormTable.getValueCls();*/
				
				IdObjectForm idObjectFormColumn=new IdObjectForm(idColumn);
				int idoColumn=idObjectFormColumn.getIdo();
				int idtoColumn=idObjectFormColumn.getIdto();
				int idPropColumn=idObjectFormColumn.getIdProp();
				int valueClsColumn=idObjectFormColumn.getValueCls();
				
				int idoFilter=idoColumn;//m_kba.getIdRange(m_kba.getChild(idoTable, idPropTable, m_userRol, m_idtoUserTask, session),valueClsTable);//m_kba.createPrototype(ido,Constants.LEVEL_FILTER, m_userRol, m_idtoUserTask, session);
				boolean isAbstract=m_kba.isAbstractClass(idoColumn);
				// Primero creamos el instance
				final instance inst=m_kba.getTreeObject(idoFilter, m_userRol, m_idtoUserTask, session, true);
				
				//Añadimos los prototipos que haya en motor para que el usuario pueda seleccionarlos
				//importante hacerlo lo primero para que el instance no tenga el root ya fijado
				Iterator<Integer> itrProtos=m_kba.getIndividuals(m_kba.getClass(idoFilter), Constants.LEVEL_PROTOTYPE, true);
				ArrayList<Integer> listCompatibleProto=new ArrayList<Integer>();
				while(itrProtos.hasNext()){
					int idoProto=itrProtos.next();
					if(m_kba.isCompatibleWithFilter(idoProto, inst, m_userRol, m_idtoUserTask)){
						listCompatibleProto.add(idoProto);
					}
				}
				
				//Utilizamos este listener para que el instance se entere de cambios que puedan haber hecho las reglas
				IChangePropertyListener listener=new IChangePropertyListener(){

					public void initChangeValue() {
						// TODO Auto-generated method stub
						
					}
					
					public void changeValue(Integer ido, int idto, int idProp, int valueCls, Value value, Value oldValue, int level, int operation) throws ParseException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException {
						//System.err.println("Cambia valor en ido:"+ido+" idProp:"+idProp+" siendo la operacion:"+operation+" valueCls:"+valueCls+" el value:"+value+" y el valueold:"+oldValue);
						Property property;
						m_kba.setInstance(inst);
						try{
							property = m_kba.getProperty(ido/* m_id */, idto, idProp,m_userRol, m_idtoUserTask, null);
						}finally{
							m_kba.clearInstance();
						}
						//boolean changes=false;
						//System.err.println("Property:"+property);
						if(property!=null){//Significa que es de las properties del instance por lo que la tratamos
							m_kba.setInstance(inst);
							try{
								m_kba.setValue(ido,idProp,value,oldValue, m_userRol,m_idtoUserTask,null);
							}finally{			
								m_kba.clearInstance();
							}
						}
					}

					public void endChangeValue() {
						// TODO Auto-generated method stub
						
					}
					
				};
				session.addIchangeProperty(listener, false);
				if(root!=null && !root.isEmpty()){
					String auxRoot=exactQuery?root:"%"+root+"%"/*Para que busque por contains*/;
					m_kba.setValue(idoColumn, idPropColumn, m_kba.buildValue(auxRoot,Constants.IDTO_STRING), null, m_userRol, m_idtoUserTask, session);
				}
				session.removeIchangeProperty(listener);
				
				Object changedRoot=root;//Nos sirve para identificar si la busqueda realizada por el usuario ha sido modificada por una regla
				try{
					m_kba.setInstance(inst);
					if(root!=null && !root.isEmpty()){
						//System.err.println("Resullll "+m_kba.getValueData(m_kba.getField(idoColumn, idtoColumn, idPropColumn, m_userRol, m_idtoUserTask, session)));
						changedRoot=m_kba.getValueData(m_kba.getField(idoColumn, idtoColumn, idPropColumn, m_userRol, m_idtoUserTask, session));
					}
				}finally{
					m_kba.clearInstance();
				}
				
				
				//Añadimos la lista de protos compatibles. Lo hacemos aqui porque necesitamos saber si changedRoot es distinto de root
				Iterator<Integer> itrCompatibleProto=listCompatibleProto.iterator();
				while(itrCompatibleProto.hasNext()){
					int idoProto=itrCompatibleProto.next();
					int idtoProto=m_kba.getClass(idoProto);
					DataProperty dProperty=m_kba.getField(idoProto, idtoProto, idPropColumn, m_userRol, m_idtoUserTask, m_session);
					//System.err.println("DataProperty de protos:"+dProperty);
					String word=(String)m_kba.getValueData(dProperty);
					if(word==null)
						word="";
					if(word.toUpperCase().contains(root.toUpperCase())){
						String wordForUser=word;
						if(idPropColumn!=Constants.IdPROP_RDN){
							String rdn=(String)m_kba.getValueData(m_kba.getRDN(dProperty.getIdo(), dProperty.getIdto(), m_userRol, m_idtoUserTask, session));
							wordForUser+=" {"+rdn+"}";
						}
						
						if(isAbstract){
							wordForUser+=" {"+m_kba.getLabelClass(dProperty.getIdto(),m_idtoUserTask)+"}";
						}
						
						//Si la busqueda ha sido modificada por una regla y la word encontrada casa completamente con lo que puso la regla
						//modificamos el word poniendole exactamente la busqueda que puso el usuario para que Elecom sepa que casa exactamente.
						//No modificamos lo que le aparece al usuario en el finder ya que esto es mediante wordForUser.
						if(!Auxiliar.equals(changedRoot, root) && Auxiliar.equals(changedRoot, word)){
							word=root;
						}
						
						words.put(wordForUser, new DictionaryWord(dProperty.getIdo(),dProperty.getIdto(),word,true));
					}
				}
//				try{
//					m_kba.setInstance(inst);
//					if(root!=null && !root.isEmpty())
//						m_kba.setValue(idoColumn, idPropColumn, m_kba.buildValue(root,Constants.IDTO_STRING), null, m_userRol, m_idtoUserTask, session);
//					
//				}finally{
//					m_kba.clearInstance();
//				}
								
				//System.err.println("Prop:"+prop);
				
				/*if(idProp!=Constants.IdPROP_RDN){
					inst.addProperty(idoFilter, m_kba.getProperty(idoFilter, Constants.IdPROP_RDN, m_userRol, m_idtoUserTask, session));
				}*/
				
				//System.err.println("instance:"+inst);
				
				ArrayList<SelectQuery> listSelect=new ArrayList<SelectQuery>();
				
				SelectQuery selectQ=new SelectQuery(String.valueOf(idoColumn),idPropColumn,null,null);
				// Si no es el rdn lo pedimos para poder mostrar a que individuo nos referimos ya que si no el rdn del instance no viene relleno
				if(idPropColumn!=Constants.IdPROP_RDN)
					listSelect.add(new SelectQuery(String.valueOf(idoColumn),Constants.IdPROP_RDN,null,null));
				//select.setAlias(propNameHash.get(property));
				listSelect.add(selectQ);
				//System.err.println("instanceee:"+inst);
				//System.err.println("select:"+listSelect);
				//System.err.println(jdomParser.returnXML(m_kba.getQueryXML(inst, listSelect, m_userRol, m_idtoUserTask,false)));
				selectData select = m_com.serverGetQuery(m_kba.getQueryXML(inst, listSelect, m_userRol, m_idtoUserTask, GConfigView.limitFinderResults), m_idtoUserTask, queryData.MODE_ROW);
				
				isAppliedLimit=(select.size()==GConfigView.limitFinderResults);
				
				Iterator<instance> itr=select.getIterator();
				while(itr.hasNext()){
					instance instanceResult=itr.next();
					//System.err.println(instanceResult);
					Iterator<Property> itrProps=instanceResult.getAllProperties().iterator();
					DataProperty dProperty=null;
					do{
						Property prop=itrProps.next();
						if(prop.getIdProp().equals(idPropColumn))
							if((idoColumn!=idoFilter && prop.getIdo()!=instanceResult.getIDO()) || (idoColumn==idoFilter))//Con esto evitamos coger una property distinta a la que nosotros hemos pedido ya que el server devuelve rdns de las properties aunque yo no las pida
								dProperty=(DataProperty)prop;//Sabemos que es dataProperty porque nunca vamos a hacer el finder para un objectProperty
					}while(itrProps.hasNext() && dProperty==null);
					
					String word="";
					if(dProperty!=null)
						word=(String)m_kba.getValueData(dProperty);
					else{
						itrProps=instanceResult.getAllProperties().iterator();
						do{
							Property prop=itrProps.next();
							if(prop.getIdProp().equals(Constants.IdPROP_RDN))
								if((idoColumn!=idoFilter && prop.getIdo()!=instanceResult.getIDO()) || (idoColumn==idoFilter)){//Con esto evitamos coger una property distinta a la que nosotros hemos pedido ya que el server devuelve rdns de las properties aunque yo no las pida
									dProperty=(DataProperty)prop;//Sabemos que es dataProperty porque nunca vamos a hacer el finder para un objectProperty
									System.err.println("WARNING:Los idos obtenidos del finder podrian no ser reales. property:"+dProperty);
								}
						}while(itrProps.hasNext() && dProperty==null);
					}
					
					//Si viene algun resultado de una clase excluida lo descartamos
					if(m_kba.getClass(dProperty.getIdto())==null){
						System.err.println("getDictionary. Excluido:"+dProperty);
						continue;
					}
					
					String wordForUser=word;
					if(idPropColumn!=Constants.IdPROP_RDN){
						m_kba.setInstance(instanceResult);
						try{
							String rdn=(String)m_kba.getValueData(m_kba.getRDN(dProperty.getIdo(), dProperty.getIdto(), m_userRol, m_idtoUserTask, session));
							wordForUser+=" {"+rdn+"}";
						}finally{
							m_kba.clearInstance();
						}
						
					}
					
					if(isAbstract){
						wordForUser+=" {"+m_kba.getLabelClass(dProperty.getIdto(),m_idtoUserTask)+"}";
					}
					
					//Si la busqueda ha sido modificada por una regla y la word encontrada casa completamente con lo que puso la regla
					//modificamos el word poniendole exactamente la busqueda que puso el usuario para que Elecom sepa que casa exactamente.
					//No modificamos lo que le aparece al usuario en el finder ya que esto es mediante wordForUser.
					if(!Auxiliar.equals(changedRoot, root) && Auxiliar.equals(changedRoot, word)){
						word=root;
					}
					
					words.put(wordForUser, new DictionaryWord(dProperty.getIdo(),dProperty.getIdto(),word,false));
					
				}
			}finally{
				try{
					session.rollBack();
				}catch(Exception ex){
					Exception e=new Exception("GETDICTIONARY EXCEPTION EN ROLLBACK");
					e.setStackTrace(ex.getStackTrace());
					m_com.logError(null,e, null);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			m_com.logError(dialog.getComponent(),e,"Error al intentar consultar los datos de ayuda al usuario");
		}
		//System.err.println("final getDictionary idTable:"+idTable+" idColumn:"+idColumn+" root:"+root);
		return isAppliedLimit;
	}
	
	public int getId(){
		return m_ido;
	}
	
	public Session exeActions(ArrayList<commandPath> commandList,ITableNavigation tableNavigation,WindowComponent dialog) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, ApplicationException, IncoherenceInMotorException, ParseException, AssignValueException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, InstanceLockedException, HeadlessException, SQLException, NamingException, JDOMException, AWTException, ActionException{
		//Si el formulario esta incrustado en el principal o se trata de algo diferente a un 'aceptar y accion' hacemos que la nueva ventana sea modal, para no permitir pulsar en la de atras
		boolean modalWindow=this.dialog==this.dialog.getMainDialog()/*Formulario esta incrustado*/ || dialog!=dialog.getMainDialog()/*No se trata de un aceptar y accion*/;
		return m_actionManager.exeOperation(commandList.get(0),m_kba,tableNavigation,dialog,modalWindow);
	}
	public Session getSession() {
		return m_session;
	}
	
	abstract public String confirm() throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, OperationNotPermitedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, NumberFormatException, AssignValueException;
	
	protected String getIdTableParent(IdObjectForm idObjForm,int valueCls,ArrayList<String> listTables) throws NotFoundException, IncoherenceInMotorException {
		String idStringFound=null;

		Iterator<Integer> itrParents=m_kba.getAncestors(valueCls);
		while(idStringFound==null && itrParents.hasNext()){
			int valueClsSup=itrParents.next();
			idObjForm.setValueCls(valueClsSup);
			String idString=idObjForm.getIdString();
			if(listTables.contains(idString)){
				idStringFound=idString;
			}/*else{
				//System.out.println("No existe tabla con id:"+idString+" en transitionControl");
				idStringFound=getIdTableParent(idObjForm,valueClsSup,listTables);
			}*/
		}

		return idStringFound;
	}
	
	public boolean isNewCreation(int ido){
		return Constants.isIDTemporal(ido);
	}
	
	public LinkedHashMap<String,Integer> getPossibleTypeForValue(String idParent,Object value,Integer valueCls){
		LinkedHashMap<String,Integer> mapNameIdto=new LinkedHashMap<String, Integer>();
		
		boolean buildValue=(value!=null || valueCls!=null);
		IdObjectForm idObjectForm=new IdObjectForm(idParent);
		Integer ido=idObjectForm.getIdo();
		Integer idProp=idObjectForm.getIdProp();
		if(valueCls==null){
			valueCls=idObjectForm.getValueCls();
		}
		
		try{
			Value valueObject=null;
			if(buildValue)
				valueObject=m_kba.buildValue(value, Constants.isDataType(valueCls)?valueCls:m_kba.getClass(valueCls));
			Iterator<Integer> itr=m_kba.getClassifiedIdtos(m_kba.getClass(ido), idProp, valueObject, m_userRol, m_idtoUserTask, m_session).iterator();
			while(itr.hasNext()){
				int idto=itr.next();
				//HashMap<Integer,ArrayList<UserAccess>> accessUserTasks=m_kba.getAllAccessIndividual(idto, m_userRol, property, m_idtoUserTask);
				//AccessAdapter accessAdapter=new AccessAdapter(accessUserTasks,property,false,false);
				access access=m_kba.getAccessIndividual(idto, m_userRol, m_idtoUserTask);
				if(access.getNewAccess()){
					mapNameIdto.put(Utils.normalizeLabel(m_kba.getLabelClass(idto, m_idtoUserTask)), idto);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			try{
				m_com.logError(dialog.getComponent(),ex,"Error al intentar clasificar la creación de "+m_kba.getLabelClass(ido, m_idtoUserTask));
			}catch(Exception e){
				m_com.logError(dialog.getComponent(),ex,"Error al intentar clasificar la creación del objeto");
			}
		}
		
		return mapNameIdto;
	}
	
	public Boolean isNullableForRow(Integer idoParent,Integer ido,String idColumn){
		IdObjectForm idObjectForm=new IdObjectForm(idColumn);
		Integer idProp=idObjectForm.getIdProp();
		Boolean isNullable=null;
		try{
			//System.err.println("isNullableForRow con ido:"+ido+" idoParent:"+idoParent);
			if(ido!=null){//Si ido no es null nos basamos en sus cardinalidades sin tener en cuenta el padre
				if(m_kba.isLoad(ido)){
					Property prop=m_kba.getProperty(ido, m_kba.getClass(ido), idProp, m_userRol, m_idtoUserTask, m_session);
					isNullable=(prop.getCardMin()==null || prop.getCardMin()==0);
				}else{
					System.err.println("WARNING: FormControl.isNullableForRow: El ido "+ido+" no esta cargado en motor");
				}
			}else if(idoParent!=null){
				if(m_kba.isLoad(idoParent)){
					if(m_kba.getLevelObject(idoParent)!=Constants.LEVEL_FILTER){
						String[] tree=idObjectForm.getIdParent().split("#");//idParent tiene la rama ido,idProp separados por #
						idProp=new Integer(tree[tree.length-1].split(",")[1]);//El ultimo de la rama es el que se refiere al padre inmediato, nos interesa su idProp
						Property prop=m_kba.getProperty(idoParent, m_kba.getClass(idoParent), idProp, m_userRol, m_idtoUserTask, m_session);
						isNullable=(prop.getCardMin()==null || prop.getCardMin()==0);//Si la property del padre es obligatoria entonces no es nullable
					}
				}else{//Ocurre en algun caso al cancelar un formulario incrustado
					System.err.println("WARNING: FormControl.isNullableForRow: El idoParent "+idoParent+" no esta cargado en motor");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			m_com.logError(dialog.getComponent(),e,"Error al consultar si el valor del campo puede ser vacio");
		}
		return isNullable;
	}

	public WindowComponent getDialog() {
		return dialog;
	}

	public Integer getIdtoUserTask() {
		return m_idtoUserTask;
	}
	
	public void showInformation(Integer ido,Integer idProp){
		if(m_session.isRunRules()){
			//System.err.println("Llama a requestInformation");
			//Si no es una session que dispare reglas no insertamos en motor ya que no serviría de nada
			try{
				m_kba.requestInformation(ido,idProp,m_session);
			}catch(Exception e){
				e.printStackTrace();
				m_com.logError(dialog.getComponent(),e,"Error al solicitar información");
			}
		}
	}
	
	public void setProcessingCopyRowTable(boolean processing){
		Singleton.getInstance().setProcessingCopyRowTable(processing);
	}
	
	public boolean isAllowedConfigTables(){
		return m_kba.canSetUpColumnProperty();
	}
}
