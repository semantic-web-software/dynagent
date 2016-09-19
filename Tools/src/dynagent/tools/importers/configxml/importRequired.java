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
import dynagent.common.basicobjects.Required;
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
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.DeletableObject;
import dynagent.server.services.InstanceService;
	
public class importRequired extends ObjectConfig{
	/*
	 * Example:
	 * 
	 * 	<REQUIRED>
			<RE CLASS="CLIENTE_EMPRESA" PROP="móvil"/>
			<RE CLASS="CLIENTE_EMPRESA" PROP="fax"/>
			<RE CLASS="CLIENTE_PARTICULAR" PROP="teléfono"/>
		</REQUIRED>
	*/
	private LinkedList<Required> listRequired= new LinkedList<Required>();
	public importRequired(Element requiredXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(requiredXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listRequired=new LinkedList<Required>();
	}
	
	@Override
	public boolean configData() throws Exception {
		deleteRequiredProps();
		return extractAllRequired();
	}
	@Override
	public void importData() throws Exception {
		insertRequireds();
	}
	
	private void deleteRequiredProps() throws SQLException, NamingException, NoSuchColumnException{
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_REQUIRED, fcdb), instanceService.getDataBaseMap(), fcdb);
	}

	private boolean extractAllRequired() throws Exception {	
		Iterator italias = getChildrenXml().iterator();
		boolean success=true;
		while(italias.hasNext()){
			Element alElem = (Element)italias.next();
			Required e = new Required();
			try{
				
				if(alElem.getAttribute(ConstantsXML.CLASS)!=null){
					String classname=alElem.getAttributeValue(ConstantsXML.CLASS).toString();
					e.setClassName(classname);
					Integer idtoClass=Auxiliar.getIdtoClass(classname, fcdb);
					if (idtoClass!=null){
						e.setIdtoClass(idtoClass);
					}else{
						throw new ConfigException("Error: No existe la clase "+classname+" no existe en el modelo");
					}
				}
				if(alElem.getAttribute(ConstantsXML.PROP)!=null){
					String propName=alElem.getAttributeValue(ConstantsXML.PROP).toString();
					e.setProp(propName);
					Integer idProp;
					if (e.getIdtoClass()!=null){
						idProp=Auxiliar.getIdProp(propName,e.getIdtoClass(), fcdb);
					}else{
						idProp=Auxiliar.getIdProp(propName, fcdb);
					}
					
					if (idProp!=null){
						e.setIdProp(idProp);
					}else{
						throw new ConfigException("Error: La property "+propName+" no existe o no corresponde a la clase");
					}
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.PROP+"' es obligatorio en el XML");
				}
				
				
				listRequired.add(e);
				
				// Si hemos filtrado por clase, lo aplicamos tambien a sus especializados
				if(e.getIdtoClass()!=null && e.getProp()!=null){
					Iterator<Integer> itr=Auxiliar.getIdtoSpecialized(e.getIdtoClass()).iterator();
					while(itr.hasNext()){
						int idto=itr.next();
						Required eSpecialized=(Required)e.clone();
						eSpecialized.setIdtoClass(idto);
						eSpecialized.setClassName(Auxiliar.getClassName(idto));
						
						listRequired.add(eSpecialized);
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
	
	private void insertRequireds() throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException, InterruptedException, NoSuchColumnException {
		Iterator<Required> ital=this.listRequired.iterator();
		TClaseDAO tdao = new TClaseDAO();
		PropertiesDAO propDAO = new PropertiesDAO();
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();
		int countido=0;
		while(ital.hasNext()){
			Required in =ital.next();
			//System.out.println("---> "+e.toString());
						
			Integer idto = tdao.getTClaseByName(Constants.CLS_REQUIRED).getIDTO();
			countido--;
			int ido=QueryConstants.getIdo(countido, idto);
			String rdn=String.valueOf("&"+ido+"&");
			list.add(new FactInstance(idto, ido, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			
			// insertar el resto de las DataProperties
			int idtoClass = tdao.getTClaseByName(Constants.CLS_CLASS).getIDTO();
			if(in.getClassName()!=null){
				int prop = propDAO.getIdPropByName(Constants.PROP_DOMINIO);
				int value = QueryConstants.getIdo(tdao.getTClaseByName(in.getClassName()).getTableId(), idtoClass);
				list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
			}
			
			int idoClass;
			Properties property;
			
			if(in.getProp()!=null){
				property=propDAO.getPropertyByName(in.getProp());
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

