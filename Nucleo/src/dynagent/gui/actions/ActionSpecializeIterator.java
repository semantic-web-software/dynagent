package dynagent.gui.actions;


import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.naming.NamingException;

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
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.access;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.sessions.IChangePropertyListener;
import dynagent.common.sessions.Session;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.actions.commands.FindCommandPath;
import dynagent.gui.actions.commands.NewCommandPath;
import dynagent.gui.actions.commands.NewRelCommandPath;
import dynagent.gui.actions.commands.RelCommandPath;
import dynagent.gui.actions.commands.SelectCommandPath;
import dynagent.gui.actions.commands.commandPath;
import dynagent.gui.forms.utils.ActionException;
import dynagent.ruleengine.src.sessions.DDBBSession;
import dynagent.ruleengine.src.sessions.DefaultSession;

public class ActionSpecializeIterator extends ActionIterator {
	private int ido;
	private int idto;
	private int idProp;
	/*private Session session;*/
	private Session sessionParent;
	
	private ActionSpecializeIterator(int ido,int idto,int idProp,Session session,Session sessionParent,ArrayList<commandPath> commandList){
		super(commandList,session);
		this.ido=ido;
		this.idto=idto;
		this.idProp=idProp;
		/*this.session=session;*/
		this.sessionParent=sessionParent;
	}
	
	public static ActionIterator createInstance(commandPath cPath,KnowledgeBaseAdapter kba) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		
		ActionSpecializeIterator actionSpecialize=null;
		
		ArrayList<commandPath> commandList=new ArrayList<commandPath>();
		
		if(cPath instanceof NewCommandPath || cPath instanceof NewRelCommandPath){
			Integer idtoUserTask=cPath.getIdtoUserTask();
			Integer userRol=cPath.getUserRol();
			Session sessionParent=cPath.getSession();
			//int operation=access.NEW;
			
			int idRange=-1;
			ObjectProperty property=null;
			int idObject=-1;
			int idtoObject=-1;
			int idProp=-1;			
			
			Session session=kba.createDefaultSession(/*kba.getDefaultSession()*/sessionParent,idtoUserTask,true, true, true, true,sessionParent instanceof DDBBSession);
			session.addIMessageListener(Singleton.getInstance().getActionManager());
			boolean success=false;
			try{
				if(cPath instanceof NewCommandPath){
					NewCommandPath newCP=(NewCommandPath)cPath;
					idObject= newCP.getIdo();
					idtoObject= newCP.getIdto();
					idProp=newCP.getIdProp();
//					int valueCls=newCP.getValueCls();
//					// Obtenemos el idRange pidiendo la property ya que valueCls es una clase y no un filtro
//					property=kba.getChild(idObject,idProp, userRol, idtoUserTask, session);
//					//System.out.println("Property del asistente:"+property+" idObject:"+idObject+" idProp:"+idProp+" valueCls:"+valueCls);
//					idRange=kba.getIdRange(property,valueCls);
					idRange=newCP.getValue();
				}
				if(cPath instanceof NewRelCommandPath){
					NewRelCommandPath newRelCP=(NewRelCommandPath)cPath;
					idObject= newRelCP.getIdo();
					idtoObject= newRelCP.getIdto();
					idProp=newRelCP.getIdProp();
//					int valueCls=newRelCP.getValueCls();
//					// Obtenemos el idRange pidiendo la property ya que valueCls es una clase y no un filtro
//					property=kba.getChild(idObject,idProp, userRol, idtoUserTask, session);
//					//System.out.println("Property del asistente:"+property+" idObject:"+idObject+" idProp:"+idProp+" valueCls:"+valueCls);
//					idRange=kba.getIdRange(property,valueCls);
					idRange=newRelCP.getValue();
				}
				
				HashMap<Integer,Integer> specialized=new HashMap<Integer,Integer>();
				
				/*if(!kba.isAbstractClass(idRange)){//TODO Comentado hasta que no se active el clasificador
					specialized.put(idRange,kba.getClass(idRange));
					Iterator<Integer> itr=getSpecialized(kba,idRange, userRol, idtoUserTask, session).iterator();
					while(itr.hasNext()){
						int ido=itr.next();
						specialized.put(ido, kba.getClass(ido));
					}
				}*/
				
				if(kba.isAbstractClass(idRange)/* || specialized.size()>1 //TODO Comentado hasta que no se active el clasificador*/){
					property=kba.getChild(idObject,idtoObject,idProp, userRol, idtoUserTask, session);
					//System.err.println("Sesion:"+session.getID()+" con padre:"+session.getIDMadre()+" Inicio");
					
					//TODO Comentar si se activa la clasificacion del clasificador en el caso de no ser abstracta
					Iterator<Integer> itr=getSpecialized(kba,idRange, userRol, idtoUserTask, session).iterator();
					while(itr.hasNext()){
						int ido=itr.next();
						if(kba.isAbstractClass(ido) || kba.getAccessIndividual(ido, userRol, idtoUserTask).getNewAccess()){
							specialized.put(ido, kba.getClass(ido));
						}
					}
					
					//System.err.println("Especializados:"+specialized+" idRange:"+idRange+" userRol:"+userRol+" idtoUserTask:"+idtoUserTask);
					
					int valueCls=idRange;
					Session sessionEnd=null;
					if(!specialized.isEmpty()){
						//System.err.println("kba.getClass(specialized.get(0)):"+kba.getClass(specialized.keySet().iterator().next())+" kba.getClass(idRange):"+kba.getClass(idRange));
						if(specialized.size()>1)//Si no hay mas de un especializado nos saltamos la ventana de seleccion
							commandList.add(new SelectCommandPath(property,specialized,idtoUserTask,userRol,session));
						else{
							valueCls=specialized.keySet().iterator().next();
							/*sessionEnd=kba.createDefaultSession(sessionParent,idtoUserTask,true, true, true, true,sessionParent instanceof DDBBSession);
							session.setForceParent(false);
							session.rollBack();*/
							sessionEnd=session;
						}
						
					}else{
						System.err.println("!!!  WARNING: El rango de la property es abstracto y no tiene especializados. Property:"+property);
						commandList.add(new FindCommandPath(property,idRange,idtoUserTask,userRol,session));
					}
					
					// TODO NewCommandPath o NewRelCommandPath se crea ahora para saber que hay mas pasos en el iterador. La sesion se le asigna en setResultStep
					if(cPath instanceof NewCommandPath)
						commandList.add(new NewCommandPath(idObject,idtoObject,idProp,valueCls/*idRange*/,idtoUserTask,userRol,sessionEnd));
					else commandList.add(new NewRelCommandPath(idObject,idtoObject,idProp,valueCls/*idRange*/,Constants.LEVEL_PROTOTYPE,idtoUserTask,userRol,sessionEnd));
		     		
					actionSpecialize= new ActionSpecializeIterator(idObject,idtoObject,idProp,session,sessionParent,commandList);
					success=true;
				}
			}finally{
				if(!success){
					session.setForceParent(false);
					session.rollBack();
				}
			}
		}
		
		return actionSpecialize;
				
    }
	
	//Devuelve los especializados comprobando que, si solo encuentra un especializado, ver si este es abstracto, en cuyo caso descendemos a sus especializados.
	private static HashSet<Integer> getSpecialized(KnowledgeBaseAdapter kba,int id,Integer userRol,Integer idtoUserTask,Session session) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		HashSet<Integer> list=new HashSet<Integer>();
		ArrayList<Integer> specialized=kba.getSpecializedFilters(id, userRol, idtoUserTask, session);
		if(specialized.size()==1){
			if(kba.isAbstractClass(specialized.get(0)))
				list.addAll(getSpecialized(kba, specialized.get(0), userRol, idtoUserTask, session));
			else{ 
				list.add(specialized.get(0));
				//list.addAll(getSpecialized(kba, specialized.get(0), userRol, idtoUserTask, session));//TODO Comentado hasta que no se active el clasificador
			}
		}else {
			list.addAll(specialized);
//			Iterator<Integer> it=specialized.iterator();//TODO Comentado hasta que no se active el clasificador
//			while (it.hasNext()){
//				Integer hijo=it.next();
//				//Si no es abstracta nos interesan los especializados ya que si no no habria manera de llegar a ellos
//				if(!kba.isAbstractClass(hijo))
//					list.addAll(getSpecialized(kba, hijo, userRol, idtoUserTask, session));
//			}
		}		
		return list;
	}

	public boolean setResultStep(KnowledgeBaseAdapter kba,IFormData form) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, ActionException {
		
		LinkedHashMap<Integer,Integer> mapResult=form.getResult();
		
		if(mapResult.isEmpty()){
			ActionException ex=new ActionException("Debe realizar una selección antes de avanzar al siguiente paso");
			ex.setUserMessage("Debe realizar una selección antes de avanzar al siguiente paso");
			throw ex;
		}
		
		int value=mapResult.keySet().iterator().next();
		int valueCls=mapResult.get(value);
		commandPath cPath=m_commandList.get(m_currentStep);
		Integer idtoUserTask=cPath.getIdtoUserTask();
		Integer userRol=cPath.getUserRol();
		
		//System.out.println("Value seleccionado asistente:"+value);
		//Pedimos el rdn para que el motor cargue el individuo de base de datos si no lo tuviera
		kba.getRDN(value, valueCls, userRol, idtoUserTask, session);
		if(kba.isAbstractClass(value)){
			HashMap<Integer,Integer> rows=new HashMap<Integer,Integer>();
			Iterator<Integer> itr=kba.getSpecializedFilters(value, userRol, idtoUserTask, session).iterator();
			while(itr.hasNext()){
				int ido=itr.next();
				rows.put(ido, kba.getClass(ido));
			}
			if(rows.isEmpty()){
				ObjectProperty property=kba.getChild(ido,idto,idProp, userRol, idtoUserTask, session);
				add(new FindCommandPath(property,value,idtoUserTask,userRol,session));
			}else{
				//TODO la property quizas no deberia ser un parametro de selectCommandPath
				ObjectProperty property=kba.getChild(ido,idto,idProp, userRol, idtoUserTask, session);
				add(new SelectCommandPath(property,rows,idtoUserTask,userRol,session));
			}
		}else{
			//TODO Para session hay que hacerle rollback pero aqui no podriamos porque si el usuario vuelve para atras los facts deben existir. Hay que ver donde lo hariamos 
			commandPath cPathLast=m_commandList.get(m_commandList.size()-1);
			Session sessionNew=kba.createDefaultSession(/*sessionParent*/session,cPathLast.getIdtoUserTask(), true, true, true, true,/*false*/true);
			boolean success=false;
			try{
				/*if(aclass.getSpecializeAccess() || !kba.isFull(value, userRol, idtoUserTask, sessionNew)){*/
					//session.rollBack();
					//System.err.println("Sesion:"+sessionNew.getID()+" con padre:"+sessionNew.getIDMadre()+" ResultStep");
					cPathLast.setSession(sessionNew);
					if(cPathLast instanceof NewCommandPath)
						((NewCommandPath)cPathLast).setValue(value);
					else ((NewRelCommandPath)cPathLast).setValue(value);
				/*}else{
					//session.rollBack();
					add(new RelCommandPath(ido,idProp,value,value,idtoUserTask,userRol,sessionNew));
					m_commandList.remove(m_commandList.size()-1);
					*/
					
	//				 try {
	//					kba.setValue(/*property,*/ido,idProp, kba.getValueOfString(String.valueOf(value),/*idtoFilter*/value),null/*, new session()*/,/*operation*/userRol,idtoUserTask,session);
	//					m_commandList.remove(m_commandList.size()-1);
	//				} catch (CardinalityExceedException e) {
	//					// TODO Auto-generated catch block
	//					e.printStackTrace();
	//				} catch (OperationNotPermitedException e) {
	//					// TODO Auto-generated catch block
	//					e.printStackTrace();
	//				} catch (IncompatibleValueException e) {
	//					// TODO Auto-generated catch block
	//					e.printStackTrace();
	//				} catch (NotFoundException e) {
	//					// TODO Auto-generated catch block
	//					e.printStackTrace();
	//				}
				/*}*/
				success=true;
			}finally{
				if(!success){
					sessionNew.setForceParent(false);
					sessionNew.rollBack();
				}
			}
			
			//m_commandList.add(new NewRelCommandPath(ido,idProp,value,idtoUserTask,userRol,sessionParent));
		}
		
		//System.err.println("commandList en result:"+m_commandList);
		return true;
	}

	// Nos vale el de ActionIterator, no hace falta sobreescribirlo
	/*
	public void endSteps() throws NotFoundException, ApplicationException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException {
		// Si estamos en el ultimo paso hacemos rollback de la sesion inicial. Si no ya se encargan los formularios
		if(!session.isFinished())
			session.rollBack();
	}
	 */	
	@Override
	// Lo sobreescrimos ya que si vamos para atras debemos borrar el paso actual ya que este debe recalcularse a partir de la seleccion del paso anterior.
//	public commandPath previous(){
//		commandPath command=null;
//		try{
//			// Lo borramos cuando no se trate del ultimo de la lista ya que lo utilizamos en resultStep.
//			// Ademas si lo borraramos el asistente no sabria que hay un paso siguiente
//			if(m_commandList.size()!=m_currentStep+1)
//				m_commandList.remove(m_currentStep);
//			else{
//				commandPath cPath=m_commandList.get(m_currentStep);
//				cPath.getSession().rollBack();
//			}
//			command=super.previous(); 
//		}catch(Exception ex){
//			ex.printStackTrace();
//			Singleton.getInstance().getComm().logError(ex);
//		}
//		
//		return command;
//	}
	
	public boolean setCancelStep(KnowledgeBaseAdapter kba,IFormData form) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, ActionException {
					
		// Lo borramos cuando no se trate del ultimo de la lista ya que lo utilizamos en resultStep.
		// Ademas si lo borraramos el asistente no sabria que hay un paso siguiente
		if(m_commandList.size()!=m_currentStep+1)
			m_commandList.remove(m_currentStep);
		else{
			commandPath cPath=m_commandList.get(m_currentStep);
			Session session=cPath.getSession();
			session.setForceParent(false);
			if(form instanceof IChangePropertyListener)
				session.removeIchangeProperty((IChangePropertyListener)form);
			session.rollBack();
		}
			return true;

	}
	

}
