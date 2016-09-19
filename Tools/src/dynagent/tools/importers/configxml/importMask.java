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
import dynagent.common.basicobjects.Mask;
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
import dynagent.server.database.dao.MaskDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.DeletableObject;
import dynagent.server.services.InstanceService;

public class importMask extends ObjectConfig{
	
	private LinkedList<Mask> listmask;
	public importMask(Element propertiesXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(propertiesXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listmask=new LinkedList<Mask>();
	}
	
	@Override
	public boolean configData() throws Exception {
		deleteMasks();
		return extractAllMask();
	}
	@Override
	public void importData() throws Exception {
		insertMasks();
	}
	
	private void deleteMasks() throws SQLException, NamingException, NoSuchColumnException{
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_MASK, fcdb), instanceService.getDataBaseMap(), fcdb);
	}

	private boolean extractAllMask() throws Exception {	
		Iterator itmasks = getChildrenXml().iterator();
		boolean success=true;
		while(itmasks.hasNext()){
			Element mkElem = (Element)itmasks.next();
			Mask m = new Mask();
			try{
				if((mkElem.getAttributeValue(ConstantsXML.REPORT_ATRB) != null && mkElem.getAttributeValue(ConstantsXML.UTASK_ATRB) != null)
						|| (mkElem.getAttributeValue(ConstantsXML.REPORT_ATRB) != null && mkElem.getAttributeValue(ConstantsXML.ACTION_ATRB) != null)
						|| (mkElem.getAttributeValue(ConstantsXML.UTASK_ATRB) != null && mkElem.getAttributeValue(ConstantsXML.ACTION_ATRB) != null)){
						
						throw new ConfigException("Error: Sólo esta permitido definir en el mismo nodo uno de los siguientes atributos: "+ConstantsXML.UTASK_ATRB+", "+ConstantsXML.ACTION_ATRB+", "+ConstantsXML.REPORT_ATRB);
				}
						
				if(mkElem.getAttributeValue(ConstantsXML.REPORT_ATRB) != null){
					String utname=mkElem.getAttributeValue(ConstantsXML.REPORT_ATRB);
					// Miramos que existe ese report en el xml ya que en base de datos aun no existe
					if(configImport.containsReportName(utname)){
						m.setUTask(Auxiliar.getIdtoClass(Constants.CLS_REPORT_INDIVIDUAL, fcdb));
						m.setUTaskName(utname);
					}else{
						throw new ConfigException("Error: el report "+utname+" no existe en el xml");
						//System.err.println("WARNING: el report "+utname+" no existe en el xml");
					}
				}
				
				if(mkElem.getAttributeValue(ConstantsXML.UTASK_ATRB) != null){
					String utname=mkElem.getAttributeValue(ConstantsXML.UTASK_ATRB);
					// Miramos que existe esa userTask en el xml ya que en base de datos aun no existe
					if(configImport.containsUserTask(utname)){
						m.setUTask(Auxiliar.getIdtoClass(Constants.CLS_MENU, fcdb));
						m.setUTaskName(utname);
					}else{
						throw new ConfigException("Error: la UserTask "+utname+" no existe en el modelo ni esta definida en el xml");
						//System.err.println("WARNING: la UserTask "+utname+" no existe en el modelo ni esta definida en el xml");
					}
				}
				
				if(mkElem.getAttributeValue(ConstantsXML.ACTION_ATRB) != null){
					String utname=mkElem.getAttributeValue(ConstantsXML.ACTION_ATRB);
					Integer idtout=Auxiliar.getIdtoClass(utname, fcdb);
					if (idtout!=null){
						m.setUTask(Auxiliar.getIdtoClass(Constants.CLS_ACTION_INDIVIDUAL, fcdb));//a.setTASK(idtout);
						m.setUTaskName(utname);
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
				if(mkElem.getAttribute(ConstantsXML.CLASS)!=null){
					String classname=mkElem.getAttributeValue(ConstantsXML.CLASS).toString();
					m.setIdtoName(classname);
					Integer idtoClass=Auxiliar.getIdtoClass(classname, fcdb);
					if (idtoClass!=null){
						m.setIdto(idtoClass);
					}else{
						throw new ConfigException("Error: No existe la clase "+classname+" no existe en el modelo");
					}
				}
				if(mkElem.getAttribute(ConstantsXML.PROP)!=null){
					String propName=mkElem.getAttributeValue(ConstantsXML.PROP).toString();
					m.setPropName(propName);
					Integer idProp;
					if (m.getIdto()!=null){
						idProp=Auxiliar.getIdProp(propName,m.getIdto(), fcdb);
					}else{
						idProp=Auxiliar.getIdProp(propName, fcdb);
					}
					
					if (idProp!=null){
						m.setProp(idProp);
					}else{
						throw new ConfigException("Error: La property "+propName+" no existe o no corresponde a la clase");
					}
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.PROP+"' es obligatorio en el XML");
				}
				
				if(mkElem.getAttribute(ConstantsXML.REGULAR_EXPRESSION)!=null){
					String mask=mkElem.getAttributeValue(ConstantsXML.REGULAR_EXPRESSION).toString();
					m.setExpression(mask/*.replaceAll("\\\\", "\\\\\\\\")*/);//Se necesita para insertar correctamente las \ de la expression regular. AHORA HACE QUE NO FUNCIONE BIEN POR LO QUE LO COMENTAMOS
				}
				
				if(mkElem.getAttribute(ConstantsXML.LENGTH)!=null){
					String length=mkElem.getAttributeValue(ConstantsXML.LENGTH).toString();
					m.setLength(Integer.valueOf(length));
					
				}
				
				if(mkElem.getAttribute(ConstantsXML.REGULAR_EXPRESSION)==null && mkElem.getAttribute(ConstantsXML.LENGTH)==null){
					throw new ConfigException("Error: Es obligatorio definir el atributo '"+ConstantsXML.REGULAR_EXPRESSION+"' ó el atributo "+ConstantsXML.LENGTH+" para la property '"+m.getPropName()+"' en el XML");
				}
				
				listmask.add(m);
				
				// Si hemos filtrado por clase, lo aplicamos tambien a sus especializados
				if(m.getIdto()!=null && m.getProp()!=null){
					Iterator<Integer> itr=Auxiliar.getIdtoSpecialized(m.getIdto()).iterator();
					while(itr.hasNext()){
						int idto=itr.next();
						Mask mSpecialized=m.clone();
						mSpecialized.setIdto(idto);
						mSpecialized.setIdtoName(Auxiliar.getClassName(idto));
						
						listmask.add(mSpecialized);
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
	private void insertMasks() throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException, InterruptedException, NoSuchColumnException {
		Iterator<Mask> itMask=this.listmask.iterator();
		TClaseDAO tdao = new TClaseDAO();
		PropertiesDAO propDAO = new PropertiesDAO();
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();
		int countido=0;
		while(itMask.hasNext()){
			Mask in=itMask.next();
			
			Integer idto = tdao.getTClaseByName(Constants.CLS_MASK).getIDTO();
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
			
			if(in.getExpression()!=null){
				int prop = propDAO.getIdPropByName(Constants.PROP_REGULAR_EXPRESSION);
				list.add(new FactInstance(idto, ido, prop, in.getExpression().replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			}
			
			if(in.getLength()!=null){
				int prop = propDAO.getIdPropByName(Constants.PROP_LENGTH);
				list.add(new FactInstance(idto, ido, prop, null, Constants.IDTO_INT, null, new Double(in.getLength()), new Double(in.getLength()), null, null, action.NEW));
			}
			
			
			property=propDAO.getPropertyByName(in.getPropName());
			int prop = propDAO.getIdPropByName(Constants.PROP_PROPERTY);
			if(new Category(property.getCAT()).isDataProperty()){
				idtoClass = Constants.IDTO_DATA_PROPERTY;
			}else{
				idtoClass = Constants.IDTO_OBJECT_PROPERTY;
			}
			idoClass = QueryConstants.getIdo(property.getTableId(),idtoClass);
			
			list.add(new FactInstance(idto, ido, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
			
			//System.out.println("---> OK");
		}
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
	}
	
}

