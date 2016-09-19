package dynagent.tools.importers.configxml;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.EssentialProperty;
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
import dynagent.server.database.dao.EssentialPropertyDAO;
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.DeletableObject;
import dynagent.server.services.InstanceService;
	
public class importEssentialProperties extends ObjectConfig{
	
	private LinkedList<EssentialProperty> listEssentialProp= new LinkedList<EssentialProperty>();
	public importEssentialProperties(Element essentialPropXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(essentialPropXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listEssentialProp=new LinkedList<EssentialProperty>();
	}
	
	@Override
	public boolean configData() throws Exception {
		deleteEssentialProps();
		return extractAllEssentialProperties();
	}
	@Override
	public void importData() throws Exception {
		insertEssentialProps();
	}
	
	private void deleteEssentialProps() throws SQLException, NamingException, NoSuchColumnException{
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_ESSENTIALPROP, fcdb), instanceService.getDataBaseMap(), fcdb);
	}

	private boolean extractAllEssentialProperties() throws Exception {	
		Iterator italias = getChildrenXml().iterator();
		boolean success=true;
		while(italias.hasNext()){
			Element alElem = (Element)italias.next();
			EssentialProperty e = new EssentialProperty();
			try{
				
				if((alElem.getAttributeValue(ConstantsXML.REPORT_ATRB) != null && alElem.getAttributeValue(ConstantsXML.UTASK_ATRB) != null)
						|| (alElem.getAttributeValue(ConstantsXML.REPORT_ATRB) != null && alElem.getAttributeValue(ConstantsXML.ACTION_ATRB) != null)
						|| (alElem.getAttributeValue(ConstantsXML.UTASK_ATRB) != null && alElem.getAttributeValue(ConstantsXML.ACTION_ATRB) != null)){
						
						throw new ConfigException("Error: Sólo esta permitido definir en el mismo nodo uno de los siguientes atributos: "+ConstantsXML.UTASK_ATRB+", "+ConstantsXML.ACTION_ATRB+", "+ConstantsXML.REPORT_ATRB);
				}
						
				if(alElem.getAttributeValue(ConstantsXML.REPORT_ATRB) != null){
					String utname=alElem.getAttributeValue(ConstantsXML.REPORT_ATRB);
					// Miramos que existe ese report en el xml ya que en base de datos aun no existe
					if(configImport.containsReportName(utname)){
						e.setUTask(Auxiliar.getIdtoClass(Constants.CLS_REPORT_INDIVIDUAL, fcdb));
						e.setUTaskName(utname);
					}else{
						throw new ConfigException("Error: el report "+utname+" no existe en el xml");
						//System.err.println("WARNING: el report "+utname+" no existe en el xml");
					}
				}
				
				if(alElem.getAttributeValue(ConstantsXML.UTASK_ATRB) != null){
					String utname=alElem.getAttributeValue(ConstantsXML.UTASK_ATRB);
					// Miramos que existe esa userTask en el xml ya que en base de datos aun no existe
					if(configImport.containsUserTask(utname)){
						e.setUTask(Auxiliar.getIdtoClass(Constants.CLS_MENU, fcdb));
						e.setUTaskName(utname);
					}else{
						throw new ConfigException("Error: la UserTask "+utname+" no existe en el modelo ni esta definida en el xml");
						//System.err.println("WARNING: la UserTask "+utname+" no existe en el modelo ni esta definida en el xml");
					}
				}
				
				if(alElem.getAttributeValue(ConstantsXML.ACTION_ATRB) != null){
					String utname=alElem.getAttributeValue(ConstantsXML.ACTION_ATRB);
					Integer idtout=Auxiliar.getIdtoClass(utname, fcdb);
					if (idtout!=null){
						e.setUTask(Auxiliar.getIdtoClass(Constants.CLS_ACTION_INDIVIDUAL, fcdb));//a.setTASK(idtout);
						e.setUTaskName(utname);
					}else{
						throw new ConfigException("Error: la Acción "+utname+" no existe en el modelo ni esta definida en el xml");
						//System.err.println("WARNING: la Acción "+utname+" no existe en el modelo ni esta definida en el xml");
					}
				}
				
				if(alElem.getAttribute(ConstantsXML.CLASS)!=null){
					String classname=alElem.getAttributeValue(ConstantsXML.CLASS).toString();
					e.setIdtoName(classname);
					Integer idtoClass=Auxiliar.getIdtoClass(classname, fcdb);
					if (idtoClass!=null){
						e.setIdto(idtoClass);
					}else{
						throw new ConfigException("Error: No existe la clase "+classname+" no existe en el modelo");
					}
				}
				if(alElem.getAttribute(ConstantsXML.PROP)!=null){
					String propName=alElem.getAttributeValue(ConstantsXML.PROP).toString();
					e.setPropName(propName);
					Integer idProp;
					if (e.getIdto()!=null){
						idProp=Auxiliar.getIdProp(propName,e.getIdto(), fcdb);
					}else{
						idProp=Auxiliar.getIdProp(propName, fcdb);
					}
					
					if (idProp!=null){
						e.setProp(idProp);
					}else{
						throw new ConfigException("Error: La property "+propName+" no existe o no corresponde a la clase");
					}
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.PROP+"' es obligatorio en el XML");
				}
				
				
				listEssentialProp.add(e);
				
				// Si hemos filtrado por clase, lo aplicamos tambien a sus especializados
				if(e.getIdto()!=null && e.getProp()!=null){
					Iterator<Integer> itr=Auxiliar.getIdtoSpecialized(e.getIdto()).iterator();
					while(itr.hasNext()){
						int idto=itr.next();
						EssentialProperty eSpecialized=(EssentialProperty)e.clone();
						eSpecialized.setIdto(idto);
						eSpecialized.setIdtoName(Auxiliar.getClassName(idto));
						
						listEssentialProp.add(eSpecialized);
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
	private void insertEssentialProps() throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException, InterruptedException, NoSuchColumnException {
		Iterator<EssentialProperty> ital=this.listEssentialProp.iterator();
		TClaseDAO tdao = new TClaseDAO();
		PropertiesDAO propDAO = new PropertiesDAO();
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();
		int countido=0;
		while(ital.hasNext()){
			EssentialProperty in =ital.next();
			//System.out.println("---> "+e.toString());
			if(in.getUTask()==null)
				in.setUTask(Auxiliar.getIdtoClass(in.getUTaskName(), fcdb));
			
			Integer idto = tdao.getTClaseByName(Constants.CLS_ESSENTIALPROP).getIDTO();
			countido--;
			int ido=QueryConstants.getIdo(countido, idto);
			String rdn=String.valueOf("&"+ido+"&");
			list.add(new FactInstance(idto, ido, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			
			// insertar el resto de las DataProperties
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
			
			//System.out.println("---> OK");
		}
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
	}

}

