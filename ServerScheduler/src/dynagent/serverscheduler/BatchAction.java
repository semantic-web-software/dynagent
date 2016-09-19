package dynagent.serverscheduler;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

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
import dynagent.common.exceptions.RuleEngineException;
import dynagent.common.exceptions.ServerException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.Session;
import dynagent.common.utils.BatchControl;
import dynagent.common.utils.IBatchListener;
import dynagent.ruleengine.src.sessions.DefaultSession;

public class BatchAction {

	private int idtoUserTask;
	private IKnowledgeBaseInfo ik;
	private String user;
	private Integer userRol;
	private HashMap<String, Integer> m_idoStateMap;
	private IBatchListener batchListener;
	private String params;
	
	public BatchAction(int idtoUserTask,IKnowledgeBaseInfo ik, String params) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		this.idtoUserTask=idtoUserTask;
		this.ik=ik;
		this.user=ik.getUser();
		this.userRol=null;
		this.params=params;
		
		batchListener=new BatchControl(ik);
		
		buildListState();
	}
	
	private void buildListState(){
		Iterator<Integer> itrState = ik.getIndividualsOfLevel(Constants.IDTO_ESTADOREALIZACION,Constants.LEVEL_INDIVIDUAL).iterator();
		m_idoStateMap=new HashMap<String, Integer>();
		while (itrState.hasNext()) {
			int ido = itrState.next();
			String rdn = ik.getRdnIfExistInRuler(ido);//Sabemos que el ido existe en motor ya que es un enumerado
			m_idoStateMap.put(rdn, ido);
		}
	}
	
	private void setState(int ido,int idto, String rdnValue,Integer userRol,Integer idtoUserTask,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		int valueCls=Constants.IDTO_ESTADOREALIZACION;
		int idProp=Constants.IdPROP_ESTADOREALIZACION;
		
		ObjectProperty property=(ObjectProperty)ik.getProperty(ido, idto, idProp, userRol, rdnValue, idtoUserTask, ses);
		Value oldValue=property.getUniqueValue();
		Integer valueNew=m_idoStateMap.get(rdnValue);
		Value newValue=new ObjectValue(valueNew,valueCls);

		ik.setValue(ido, idto, idProp, oldValue, newValue, userRol, rdnValue, idtoUserTask, ses);
	}
	
	/**
	 * Procesa cada posición del source del BatchListener enviando a base de datos cada procesamiento de forma separada y devolviendo el resultado de cada uno
	 */
	public LinkedHashMap<String,String> process() throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException {
		LinkedHashMap<String,String> mapResult=new LinkedHashMap<String, String>(); 
		boolean oldReusable=ik.getRootSession().isReusable();
		ik.getRootSession().setReusable(true);
		Session sess=new DefaultSession(ik, ik.getRootSession(), idtoUserTask, true, true, true, true, false);
		try{
			int idoUserTask=ik.createPrototype(idtoUserTask, Constants.LEVEL_FILTER, userRol, user, idtoUserTask, sess);
			
			if(params!=null){
				Value oldRdn=ik.getProperty(idoUserTask, idtoUserTask, Constants.IdPROP_RDN, userRol, user, idtoUserTask, sess).getUniqueValue();
				Value newRdn=new StringValue(params);
				ik.setValue(idoUserTask, idtoUserTask, Constants.IdPROP_RDN, oldRdn, newRdn, userRol, user, idtoUserTask, sess);
			}
			
			setState(idoUserTask, idtoUserTask, Constants.INDIVIDUAL_INFORMADO, userRol, idtoUserTask, sess);
			setState(idoUserTask, idtoUserTask, Constants.INDIVIDUAL_PENDIENTE, userRol, idtoUserTask, sess);

			ArrayList<HashMap<Integer, Integer>> mapSources=batchListener.getSources();
			if(mapSources!=null){
				Iterator<HashMap<Integer,Integer>> itrGroups=mapSources.iterator();
				while(itrGroups.hasNext()){
					HashMap<Integer,Integer> map=itrGroups.next();
					for(Integer ido:map.keySet()){
						boolean success=false;
						String rdn=null;
						int idto=map.get(ido);
						Session sessionNew=new DefaultSession(ik,ik.getRootSession(),idtoUserTask, true, true, true, false/*TODO Probar que realmente necesitamos false y no true*/, true);
						try{
							ObjectProperty propIterator=(ObjectProperty)ik.getProperty(idoUserTask,idtoUserTask,Constants.IdPROP_ITERATOR, userRol, user, idtoUserTask, sessionNew);
							
							//Primero desvinculamos los idos que haya ya en source porque solo nos interesa los que nosotros digamos. Si no seria un problema al volver atras en el asistente ya que la sesion sigue siendo la misma
							ik.setValue(idoUserTask, idtoUserTask, Constants.IdPROP_ITERATOR, propIterator.getValues(), null, userRol, user, idtoUserTask, sessionNew);
							
							ik.loadIndividual(ido, idto, 1, true, true, userRol, user, idtoUserTask, sessionNew);//Cargarlo con varios niveles puede venir bien en algunos casos pero en otros penalizar
							
							rdn=ik.getAliasOfClass(idto, idtoUserTask);
								if(ik.existInMotor(ido)){
									DataProperty propertyRdn=(DataProperty)ik.getProperty(ido, idto, Constants.IdPROP_RDN, userRol, user, idtoUserTask, sessionNew);
									rdn+=" "+propertyRdn.getUniqueValue().getValue_s();
								}else{//Si no esta cargado significa que ha habido algun error al intentar cargarlo
									rdn+="("+ido+")";
									throw new Exception();
								}
							
							ObjectValue val=new ObjectValue(ido,idto);
							ik.setValue(idoUserTask, idtoUserTask, Constants.IdPROP_ITERATOR, null, val, userRol, user, idtoUserTask, sessionNew);
		
							setState(idoUserTask, idtoUserTask, Constants.INDIVIDUAL_REALIZADO, userRol, idtoUserTask, sessionNew);
							
							sessionNew.commit();
							success=true;
							mapResult.put(rdn, "Completado");
						}catch(RuleEngineException ex){
							ex.printStackTrace();
							mapResult.put(rdn,ex.getUserMessage());
						}catch(ServerException ex){
							ex.printStackTrace();
							mapResult.put(rdn,ex.getUserMessage());
						}catch(Exception ex){
							ex.printStackTrace();
							mapResult.put(rdn,"Error al intentar procesar la operación");
						}finally{
							if(!success){
								sessionNew.setForceParent(false);
								sessionNew.rollBack();
							}
						}
					}
				}
			}
		}finally{
			ik.getRootSession().setReusable(oldReusable);
			
			sess.rollBack();//Hacemos rollback de la creacion de la usertask y la asignacion de los estados
		}
		
		return mapResult;
	}
}
