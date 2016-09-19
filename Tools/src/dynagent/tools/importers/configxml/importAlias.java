package dynagent.tools.importers.configxml;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.Alias;
import dynagent.common.basicobjects.ColumnProperty;
import dynagent.common.basicobjects.O_Datos_Attrib;
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
import dynagent.server.database.IndividualCreator;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.DeletableObject;
import dynagent.server.services.InstanceService;
	
public class importAlias extends ObjectConfig{
	
	private LinkedList<Alias> listalias;
	public importAlias(Element aliasXML, FactoryConnectionDB fcdb, InstanceService instanceService,ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(aliasXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listalias=new LinkedList<Alias>();
	}
	
	@Override
	public boolean configData() throws Exception {
		return configData(false);
	}
	@Override
	public void importData() throws Exception {
		insertAlias();
	}

	public void deleteAliasReports(String uniqueReportInclude) throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException, InterruptedException, NoSuchColumnException {
		TClaseDAO tdao = new TClaseDAO();
		int idtoClass = tdao.getTClaseByName(Constants.CLS_ALIAS_UTASK).getIDTO();
		
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "select alias." + gSQL.getCharacterBegin() + "tableId" + gSQL.getCharacterEnd() + " as idalias " +
				"from informe as inf " + 
				"inner join " + gSQL.getCharacterBegin() + "alias_ámbito" + gSQL.getCharacterEnd() + " as alias on (inf." + gSQL.getCharacterBegin() + "tableId" + gSQL.getCharacterEnd() + "=alias." + gSQL.getCharacterBegin() + "ámbitoINFORME" + gSQL.getCharacterEnd() + ")";
		if (uniqueReportInclude!=null) {
			sql += " where inf.rdn='" + uniqueReportInclude + "'";
		}
		
		List<List<String>> listQ = DBQueries.executeQuery(fcdb, sql);
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();
		for (List<String> list2 : listQ) {
			if (list2.size()>0) {
				int ido = Integer.parseInt(list2.get(0) + StringUtils.leftPad(String.valueOf(idtoClass), 3, '0'));
				list.add(new FactInstance(idtoClass, ido, Constants.IdPROP_OBJECTDELETED, null, null, null, null, null, null, null, action.DEL_OBJECT));
			}
		}
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
	}
	
	private void deleteAlias() throws SQLException, NamingException, NoSuchColumnException{
		for(String className:Alias.ALIAS_TYPES){
			DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(className, fcdb), instanceService.getDataBaseMap(), fcdb);
		}
	}
	
	public boolean configData(boolean onlyReports) throws Exception {
		if(!onlyReports)
			deleteAlias();
		return extractAllAlias(onlyReports);
	}
	
	private boolean extractAllAlias(boolean onlyReports) throws Exception {	
		Iterator italias = getChildrenXml().iterator();
		boolean success=true;
		while(italias.hasNext()){
			Element alElem = (Element)italias.next();
			Alias a = new Alias();
			try{
				if((alElem.getAttributeValue(ConstantsXML.REPORT_ATRB) != null && alElem.getAttributeValue(ConstantsXML.UTASK_ATRB) != null)
						|| (alElem.getAttributeValue(ConstantsXML.REPORT_ATRB) != null && alElem.getAttributeValue(ConstantsXML.ACTION_ATRB) != null)
						|| (alElem.getAttributeValue(ConstantsXML.UTASK_ATRB) != null && alElem.getAttributeValue(ConstantsXML.ACTION_ATRB) != null)){
						
						throw new ConfigException("Error: Sólo esta permitido definir en el mismo nodo uno de los siguientes atributos: "+ConstantsXML.UTASK_ATRB+", "+ConstantsXML.ACTION_ATRB+", "+ConstantsXML.REPORT_ATRB);
					}
						
				boolean include=false;
				if(alElem.getAttributeValue(ConstantsXML.REPORT_ATRB) != null){
					String utname=alElem.getAttributeValue(ConstantsXML.REPORT_ATRB);
					// Miramos que existe ese report en el xml ya que en base de datos aun no existe
					if(configImport.containsReportName(utname)){
						a.setUTask(Auxiliar.getIdtoClass(Constants.CLS_REPORT_INDIVIDUAL, fcdb));
						a.setUTaskName(utname);
						include=true;
					}else{
						throw new ConfigException("Error: el report "+utname+" no existe en el xml");
						//System.err.println("WARNING: el report "+utname+" no existe en el xml");
					}
				}else if(!onlyReports){
					include=true;
				}
				
				if(include){
					if(alElem.getAttributeValue(ConstantsXML.UTASK_ATRB) != null){
						String utname=alElem.getAttributeValue(ConstantsXML.UTASK_ATRB);
						// Miramos que existe esa userTask en el xml ya que en base de datos aun no existe
						if(configImport.containsUserTask(utname)){
							a.setUTask(Auxiliar.getIdtoClass(Constants.CLS_MENU, fcdb));
							a.setUTaskName(utname);
						}else{
							throw new ConfigException("Error: la UserTask "+utname+" no existe en el modelo ni esta definida en el xml");
							//System.err.println("WARNING: la UserTask "+utname+" no existe en el modelo ni esta definida en el xml");
						}
					}
					
					if(alElem.getAttributeValue(ConstantsXML.ACTION_ATRB) != null){
						String utname=alElem.getAttributeValue(ConstantsXML.ACTION_ATRB);
						Integer idtout=Auxiliar.getIdtoClass(utname, fcdb);
						if (idtout!=null){
							a.setUTask(Auxiliar.getIdtoClass(Constants.CLS_ACTION_INDIVIDUAL, fcdb));//a.setTASK(idtout);
							a.setUTaskName(utname);
						}else{
							throw new ConfigException("Error: la Acción "+utname+" no existe en el modelo ni esta definida en el xml");
							//System.err.println("WARNING: la Acción "+utname+" no existe en el modelo ni esta definida en el xml");
						}
					}
					/*if(alElem.getAttribute(ConstantsXML.GROUP_ATRB)!=null){
						String groupname=alElem.getAttributeValue(ConstantsXML.GROUP_ATRB).toString();
						a.setGroupName(groupname);
						Integer idtoGroup=Auxiliar.getIdGroup(groupname, configImport);
						if (idtoGroup!=null){
							a.setGroup(idtoGroup);
						}else{
							throw new ConfigException("Error: No existe el grupo "+groupname+" no existe en el modelo");
						}
					}*/
					if(alElem.getAttribute(ConstantsXML.CLASS)!=null){
						String classname=alElem.getAttributeValue(ConstantsXML.CLASS).toString();
						a.setIdtoName(classname);
						Integer idtoClass=Auxiliar.getIdtoClass(classname, fcdb);
						if (idtoClass!=null){
							a.setIdto(idtoClass);
						}else{
							//throw new ConfigException("Error: No existe la clase "+classname+" no existe en el modelo");
							continue;
						}
					}
					if(alElem.getAttribute(ConstantsXML.PROP)!=null){
						String propName=alElem.getAttributeValue(ConstantsXML.PROP).toString();
						a.setPropName(propName);
						Integer idProp;
						if (a.getIdto()!=null){
							idProp=Auxiliar.getIdProp(propName,a.getIdto(), fcdb);
						}else{
							idProp=Auxiliar.getIdProp(propName, fcdb);
						}
						
						if (idProp!=null){
							a.setProp(idProp);
						}else{
							//throw new ConfigException("Error: La property "+propName+" no existe o no corresponde a la clase");
							continue;
						}
					}
					if(alElem.getAttribute(ConstantsXML.ALIAS_ROOT)!=null){
						String alias=alElem.getAttributeValue(ConstantsXML.ALIAS_ROOT).toString();
						a.setAlias(alias);
						
					}else{
						throw new ConfigException("Error: El atributo '"+ConstantsXML.ALIAS_ROOT+"' es obligatorio en el XML");
					}
					
					listalias.add(a);
					
					// Si hemos filtrado por clase, lo aplicamos tambien a sus especializados
					if(a.getIdto()!=null && a.getProp()!=null){
						Iterator<Integer> itr=Auxiliar.getIdtoSpecialized(a.getIdto()).iterator();
						while(itr.hasNext()){
							int idto=itr.next();
							Alias aSpecialized=(Alias)a.clone();
							aSpecialized.setIdto(idto);
							aSpecialized.setIdtoName(Auxiliar.getClassName(idto));
							
							listalias.add(aSpecialized);
						}
					}
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
	private void insertAlias() throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException, InterruptedException, NoSuchColumnException {
		Iterator<Alias> ital=this.listalias.iterator();
		TClaseDAO tdao = new TClaseDAO();
		PropertiesDAO propDAO = new PropertiesDAO();
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();
		int countido=0;
		while(ital.hasNext()){
			Alias in =ital.next();
			
			Integer idto = tdao.getTClaseByName(in.getAliasType()).getIDTO();
			countido--;
			int ido=QueryConstants.getIdo(countido, idto);
			String rdn=String.valueOf("&"+ido+"&");
			list.add(new FactInstance(idto, ido, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			
			int idtoClass = tdao.getTClaseByName(Constants.CLS_CLASS).getIDTO();
			if(in.getIdtoName()!=null){
				int prop = propDAO.getIdPropByName(Constants.PROP_DOMINIO);
				int value = QueryConstants.getIdo(tdao.getTClaseByName(in.getIdtoName()).getTableId(), idtoClass);
				list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
			}
			
			int idoClass;
			Properties property;
			
			if(in.getUTaskName()!=null){
				String utaskName = in.getUTaskName();
				int prop = propDAO.getIdPropByName(Constants.PROP_SCOPE);
				idtoClass= in.getUTask();
				idoClass=Auxiliar.getIdo(idtoClass, utaskName, fcdb, instanceService.getDataBaseMap());
				list.add(new FactInstance(idto, ido, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
			}
			
			if(in.getPropName()!=null){
				property=propDAO.getPropertyByName(in.getPropName());
				int prop = propDAO.getIdPropByName(Constants.PROP_PROPERTY);
				if(new Category(property.getCAT()).isDataProperty()){
					idtoClass = Constants.IDTO_DATA_PROPERTY;
				}else{
					idtoClass = Constants.IDTO_OBJECT_PROPERTY;
				}
				idoClass = QueryConstants.getIdo(property.getTableId(),idtoClass);
				
				list.add(new FactInstance(idto, ido, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
			}
			
			int prop = propDAO.getIdPropByName(Constants.PROP_ALIAS);
			list.add(new FactInstance(idto, ido, prop, in.getAlias(), Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			// System.out.println("---> OK");
		}
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
	}

}
