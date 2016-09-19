package dynagent.tools.importers.configxml;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.ColumnProperty;
import dynagent.common.basicobjects.Groups;
import dynagent.common.basicobjects.Properties;
import dynagent.common.communication.IndividualData;
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
import dynagent.common.knowledge.Category;
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.action;
import dynagent.common.utils.QueryConstants;
import dynagent.server.database.dao.GroupsDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.DeletableObject;
import dynagent.server.services.InstanceService;

public class importGroups extends ObjectConfig {

	
	private LinkedHashMap<Integer, LinkedList<Groups>> listgroups;
	
	public importGroups(Element groupsXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(groupsXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listgroups=new LinkedHashMap<Integer, LinkedList<Groups>>();
	}
	
	@Override
	public boolean configData() throws Exception {
		deleteGroups();
		return extractAllGroups();
	}
	@Override
	public void importData() throws Exception {
		insertGroups();
	}
	
	private void deleteGroups() throws SQLException, NamingException, NoSuchColumnException{
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_GROUPS, fcdb), instanceService.getDataBaseMap(), fcdb);
	}

	private void insertGroups() throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException, InterruptedException, NoSuchColumnException {
		Iterator<Integer> itgp=this.listgroups.keySet().iterator();
		int countido=0;
		TClaseDAO tdao = new TClaseDAO();
		PropertiesDAO propDAO = new PropertiesDAO();
		Integer idto = tdao.getTClaseByName(Constants.CLS_GROUPS).getIDTO();
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();   
		while(itgp.hasNext()){
			int sec=itgp.next();
			Groups gp=listgroups.get(sec).getFirst();//Cogemos el primero para mirar el class y classParent
			countido--;
			int ido=QueryConstants.getIdo(countido, idto);
			String rdn=gp.getNameGroup();
			list.add(new FactInstance(idto, ido, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			
			//insertar el resto de las DataProperties
			int idtoClass = tdao.getTClaseByName(Constants.CLS_CLASS).getIDTO();
			
			if(gp.getClassName()!=null){
				int prop = propDAO.getIdPropByName(Constants.PROP_DOMINIO);
				int value = QueryConstants.getIdo(tdao.getTClaseByName(gp.getClassName()).getTableId(), idtoClass);
				list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
			}
			
			if(gp.getUTaskName()!=null){
				String utaskName = gp.getUTaskName();
				int prop = propDAO.getIdPropByName(Constants.PROP_SCOPE);
				idtoClass= gp.getUTask();
				int idoClass=Auxiliar.getIdo(idtoClass, utaskName, fcdb, instanceService.getDataBaseMap());
				list.add(new FactInstance(idto, ido, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
			}
			
			for(Groups in:listgroups.get(sec)){//Procesamos todos
				//System.out.println("---> "+in.toString());
				Properties property=propDAO.getPropertyByName(in.getPropName());
				int prop = propDAO.getIdPropByName(Constants.PROP_PROPERTY);
				if(new Category(property.getCAT()).isDataProperty()){
					idtoClass = Constants.IDTO_DATA_PROPERTY;
				}else{
					idtoClass = Constants.IDTO_OBJECT_PROPERTY;
				}
				int idoClass = QueryConstants.getIdo(property.getTableId(),idtoClass);
				
				list.add(new FactInstance(idto, ido, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
				
				prop = propDAO.getIdPropByName(Constants.PROP_ORDER);
				list.add(new FactInstance(idto, ido, prop, null, Constants.IDTO_INT, null, new Double(in.getOrder()), new Double(in.getOrder()), null, null, action.NEW));

				//System.out.println("---> OK");
			}
		}
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
	}
	
	private boolean extractAllGroups() throws Exception {
		Iterator itgp = getChildrenXml().iterator();
		boolean success=true;
		int sec=1;
		while(itgp.hasNext()){
			Element groupElem = (Element)itgp.next();
			Groups gp = new Groups();
			try{
				String groupName=null;
				if (groupElem.getAttribute(ConstantsXML.NAME)!=null){
					groupName=groupElem.getAttributeValue(ConstantsXML.NAME).toString();
					gp.setNameGroup(groupName);
				
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.NAME+"' es obligatorio en el XML");
				}
				
				if((groupElem.getAttributeValue(ConstantsXML.REPORT_ATRB) != null && groupElem.getAttributeValue(ConstantsXML.UTASK_ATRB) != null)
						|| (groupElem.getAttributeValue(ConstantsXML.REPORT_ATRB) != null && groupElem.getAttributeValue(ConstantsXML.ACTION_ATRB) != null)
						|| (groupElem.getAttributeValue(ConstantsXML.UTASK_ATRB) != null && groupElem.getAttributeValue(ConstantsXML.ACTION_ATRB) != null)){
						
						throw new ConfigException("Error: Sólo esta permitido definir en el mismo nodo uno de los siguientes atributos: "+ConstantsXML.UTASK_ATRB+", "+ConstantsXML.ACTION_ATRB+", "+ConstantsXML.REPORT_ATRB);
				}
						
				if(groupElem.getAttributeValue(ConstantsXML.REPORT_ATRB) != null){
					String utname=groupElem.getAttributeValue(ConstantsXML.REPORT_ATRB);
					// Miramos que existe ese report en el xml ya que en base de datos aun no existe
					if(configImport.containsReportName(utname)){
						gp.setUTask(Auxiliar.getIdtoClass(Constants.CLS_REPORT_INDIVIDUAL, fcdb));
						gp.setUTaskName(utname);
					}else{
						throw new ConfigException("Error: el report "+utname+" no existe en el xml");
						//System.err.println("WARNING: el report "+utname+" no existe en el xml");
					}
				}
				
				if(groupElem.getAttributeValue(ConstantsXML.UTASK_ATRB) != null){
					String utname=groupElem.getAttributeValue(ConstantsXML.UTASK_ATRB);
					// Miramos que existe esa userTask en el xml ya que en base de datos aun no existe
					if(configImport.containsUserTask(utname)){
						gp.setUTask(Auxiliar.getIdtoClass(Constants.CLS_MENU, fcdb));
						gp.setUTaskName(utname);
					}else{
						throw new ConfigException("Error: la UserTask "+utname+" no existe en el modelo ni esta definida en el xml");
						//System.err.println("WARNING: la UserTask "+utname+" no existe en el modelo ni esta definida en el xml");
					}
				}
				
				if(groupElem.getAttributeValue(ConstantsXML.ACTION_ATRB) != null){
					String utname=groupElem.getAttributeValue(ConstantsXML.ACTION_ATRB);
					Integer idtout=Auxiliar.getIdtoClass(utname, fcdb);
					if (idtout!=null){
						gp.setUTask(Auxiliar.getIdtoClass(Constants.CLS_ACTION_INDIVIDUAL, fcdb));//a.setTASK(idtout);
						gp.setUTaskName(utname);
					}else{
						throw new ConfigException("Error: la Acción "+utname+" no existe en el modelo ni esta definida en el xml");
						//System.err.println("WARNING: la Acción "+utname+" no existe en el modelo ni esta definida en el xml");
					}
				}
				
				String className=null;
				if (groupElem.getAttribute(ConstantsXML.CLASS)!=null){
					className=groupElem.getAttributeValue(ConstantsXML.CLASS).toString();
					Integer idtoClass=Auxiliar.getIdtoClass(className, fcdb);
					if(idtoClass!=null){
						gp.setClassName(className);
						gp.setIdtoClass(idtoClass);
					}else{
						//throw new ConfigException("Error: La Clase "+className+" no existe en el modelo");
						continue;
					}
				}
				
				if(groupElem.getAttribute(ConstantsXML.ORDER)!=null){
					String order=groupElem.getAttributeValue(ConstantsXML.ORDER).toString();
					gp.setOrder(Integer.valueOf(order));
				}else{
					gp.setOrder(10);
				}
				
				Iterator itrgp=groupElem.getChildren().iterator();
				if (itrgp.hasNext()){
					LinkedList<Groups> list=new LinkedList<Groups>();
					do{		
						Element gpElem = (Element)itrgp.next();
						
						if (gpElem.getAttribute(ConstantsXML.PROP)!=null){
							String propName=gpElem.getAttributeValue(ConstantsXML.PROP).toString();
							Integer idProp=Auxiliar.getIdProp(propName, gp.getIdtoClass(), fcdb);
							if(idProp!=null){
								gp.setPropName(propName);
								gp.setIdProp(idProp);
							}else{
								//throw new ConfigException("Error: La property "+propName+" no existe"+(className!=null?" o no pertenece a "+ className:""));
								//System.err.println("WARNING: La property "+propName+" no existe"+(className!=null?" o no pertenece a "+ className:""));
								continue;
							}
						
						}else{
							throw new ConfigException("Error: El atributo '"+ConstantsXML.PROP+"' es obligatorio en el XML");
						}
						
						list.add(gp.clone());
					}while(itrgp.hasNext());
					if(!list.isEmpty()){
						listgroups.put(sec, list);
						sec++;
					}
				}else{
					throw new ConfigException("Error: No se han definido nodos "+ConstantsXML.GROUP+" para el group con "+ConstantsXML.NAME+": "+groupName);
				}
			}catch(ConfigException ex){
				System.err.println(ex.getMessage());
				success=false;
			}catch(Exception ex){
				throw ex;
			}
		}
		return success;
	}

}
