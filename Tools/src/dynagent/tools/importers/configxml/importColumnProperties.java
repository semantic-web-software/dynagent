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
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.TClase;
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
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.action;
import dynagent.common.utils.QueryConstants;
import dynagent.server.database.IndividualCreator;
import dynagent.server.database.dao.ColumnPropertyDAO;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.DeletableObject;
import dynagent.server.services.InstanceService;

public class importColumnProperties extends ObjectConfig{
	
	private IKnowledgeBaseInfo ik;
	private LinkedHashMap<Integer,LinkedList<ColumnProperty>> listcolumnsProp;
	public importColumnProperties(Element columnpropXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport, String pathImportOtherXml,IKnowledgeBaseInfo ik) throws Exception {
		super(columnpropXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listcolumnsProp=new LinkedHashMap<Integer, LinkedList<ColumnProperty>>();
		this.ik=ik;
	}
	
	@Override
	public boolean configData() throws Exception {
		deleteColumnProp();
		return extractAllColumnsProperties();
	}
	@Override
	public void importData() throws Exception {
		insertColumnProp();
	}
	
	private void deleteColumnProp() throws SQLException, NamingException, NoSuchColumnException{
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_COLUMNPROPERTY, fcdb), instanceService.getDataBaseMap(), fcdb);
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_ORDER_WITH_FILTER, fcdb), instanceService.getDataBaseMap(), fcdb);
	}
	
	private void insertColumnProp() throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException, InterruptedException, NoSuchColumnException {
		Iterator<Integer> itcp=this.listcolumnsProp.keySet().iterator();
		int countido=0;
		TClaseDAO tdao = new TClaseDAO();
		PropertiesDAO propDAO = new PropertiesDAO();
		Integer idto = tdao.getTClaseByName(Constants.CLS_COLUMNPROPERTY).getIDTO();
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();   
		while(itcp.hasNext()){
			int sec=itcp.next();
			countido--;
			int ido=QueryConstants.getIdo(countido, idto);
			String rdn=String.valueOf(sec);
			list.add(new FactInstance(idto, ido, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			
			ColumnProperty cp=listcolumnsProp.get(sec).getFirst();//Cogemos el primero para mirar el class y classParent
			//insertar el resto de las DataProperties
			int idtoClass = tdao.getTClaseByName(Constants.CLS_CLASS).getIDTO();
			if(cp.getIdtoName()!=null){
				int prop = propDAO.getIdPropByName(Constants.PROP_TABLE);
				int value = QueryConstants.getIdo(tdao.getTClaseByName(cp.getIdtoName()).getTableId(), idtoClass);
				list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
			}
			
			if(cp.getIdtoParentName()!=null){
				int prop = propDAO.getIdPropByName(Constants.PROP_DOMINIO);
				int value = QueryConstants.getIdo(tdao.getTClaseByName(cp.getIdtoParentName()).getTableId(), idtoClass);
				list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
			}
			
			int value=ido;
			for(ColumnProperty in:listcolumnsProp.get(sec)){//Procesamos todos
				//System.out.println("---> "+in.toString());
				value--;
				int prop = propDAO.getIdPropByName(Constants.PROP_COLUMNS);
				int valueClass = tdao.getTClaseByName(Constants.CLS_ORDER_WITH_FILTER).getIDTO();				
				list.add(new FactInstance(idto, ido, prop, String.valueOf(value), valueClass, null, null, null, null, null, action.NEW));
				
				// Nos quedamos con el ido de la columna ya que es sobre el que vamos a trabajar
//				ido = value;
//				idto = valueClass;
				
				String rdnValue = String.valueOf(value); 
				list.add(new FactInstance(valueClass, value, Constants.IdPROP_RDN, "&"+rdnValue+"&", Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
				
				int idoClass;
				Properties property;
				
				if(in.getPropFilter()!=null){
					property=propDAO.getPropertyByName(in.getPropFilter());
					prop = propDAO.getIdPropByName(Constants.PROP_CAMPO_FILTRO);
					if(new Category(property.getCAT()).isDataProperty()){
						idtoClass = Constants.IDTO_DATA_PROPERTY;
					}else{
						idtoClass = Constants.IDTO_OBJECT_PROPERTY;
					}
					idoClass = QueryConstants.getIdo(property.getTableId(),idtoClass);
					
					list.add(new FactInstance(valueClass, value, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
				}
				
				if(in.getValueFilter()!=null){
					prop = propDAO.getIdPropByName(Constants.PROP_VALOR_FILTRO);
					list.add(new FactInstance(valueClass, value, prop, in.getValueFilter(), Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
				}
				
//				property=propDAO.getPropertyByName(in.getIdPropName());
//				prop = propDAO.getIdPropByName(Constants.PROP_PROPERTY);
//				if(new Category(property.getCAT()).isDataProperty()){
//					idtoClass = Constants.IDTO_DATA_PROPERTY;
//				}else{
//					idtoClass = Constants.IDTO_OBJECT_PROPERTY;
//				}
//				idoClass = QueryConstants.getIdo(property.getTableId(),idtoClass);
//				
//				list.add(new FactInstance(valueClass, value, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
//				
				prop = propDAO.getIdPropByName(Constants.PROP_ORDER);
				list.add(new FactInstance(valueClass, value, prop, null, Constants.IDTO_INT, null, new Double(in.getPriority()), new Double(in.getPriority()), null, null, action.NEW));
				
				prop = propDAO.getIdPropByName(Constants.PROP_PROPERTY_PATH);
				list.add(new FactInstance(valueClass, value, prop, in.getIdPropPath(), Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
				
				//System.out.println("---> OK");
			}
			ido=value;
		}
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
		
	}
	public boolean extractAllColumnsProperties() throws Exception{
		Iterator itcolumnp = getChildrenXml().iterator();
		boolean success=true;
		int sec=1;
		while(itcolumnp.hasNext()){
			Element columnPElem = (Element)itcolumnp.next();
			ColumnProperty cp = new ColumnProperty();
			try{
				if(columnPElem.getAttribute(ConstantsXML.CLASS_PARENT)!=null){
					String classParent=columnPElem.getAttributeValue(ConstantsXML.CLASS_PARENT).toString();
					cp.setIdtoParentName(classParent);
					Integer idtoParent=Auxiliar.getIdtoClass(classParent, fcdb);
					if (idtoParent!=null){
						cp.setIdtoParent(idtoParent);
						
					}else{
						//throw new ConfigException("Error: La Clase "+classParent+" no existe en el modelo");
						continue;
					}
				}
				if(columnPElem.getAttribute(ConstantsXML.CLASS)!=null){
					String className=columnPElem.getAttributeValue(ConstantsXML.CLASS).toString();
					cp.setIdtoName(className);
					Integer idtoClass=Auxiliar.getIdtoClass(className, fcdb);
					if (idtoClass!=null){
						cp.setIdto(idtoClass);
						
					}else{
						//throw new ConfigException("Error: La Clase "+className+" no existe en el modelo");
						continue;
					}
				}
				
				Iterator itrcp=columnPElem.getChildren().iterator();
				if (itrcp.hasNext()){
					LinkedList<ColumnProperty> list=new LinkedList<ColumnProperty>();
					do{
						Element cpElem = (Element)itrcp.next();
						if(cpElem.getAttribute(ConstantsXML.PROPPATH)!=null){
							String propPathName=cpElem.getAttributeValue(ConstantsXML.PROPPATH).toString();
							
							String[] split=propPathName.split("#");
							int length=split.length;
							String propName=null;
							boolean exit=false;
							for(int j=0;j<length;j++){
								propName=split[j];
								Integer idProp=Auxiliar.getIdProp(propName, null, fcdb);
								if (idProp==null){
									//throw new ConfigException("Error: La property "+propName+" no existe o no pertenece a "+cp.getIdtoName());
									System.err.println("WARNING: La property "+propName+" no existe");
									exit=true;
									continue;
								}
							}
							
							if(exit==true){
								continue;
							}
							
							//Si termina en objectproperty le añadimos el rdn
							if(ik.isObjectProperty(ik.getIdProperty(propName))){
								propPathName+="#rdn";
							}
							
							cp.setIdPropPath(propPathName);
//							cp.setIdPropName(propName);
//							Integer idProp=Auxiliar.getIdProp(propName, cp.getIdto(), fcdb);
//							if (idProp!=null){
//								cp.setIdProp(idProp);
//								
//							}else{
//								//throw new ConfigException("Error: La property "+propName+" no existe o no pertenece a "+cp.getIdtoName());
//								System.err.println("WARNING: La property "+propName+" no existe o no pertenece a "+cp.getIdtoName());
//								continue;
//							}
						}else{
							throw new ConfigException("Error: El atributo '"+ConstantsXML.PROPPATH+"' es obligatorio en el XML");
						}
						if(cpElem.getAttribute(ConstantsXML.PROP_FILTER)!=null){
							String propfName=cpElem.getAttributeValue(ConstantsXML.PROP_FILTER).toString();
							cp.setPropFilter(propfName);
							Integer idPropF=Auxiliar.getIdProp(propfName, fcdb);
							if (idPropF!=null){
								cp.setIdPropF(idPropF);
								
							}else{
								throw new ConfigException("Error: La property "+propfName+" no existe");
							}
						}
						if(cpElem.getAttribute(ConstantsXML.VALUE_FILTER)!=null){
							String valuef=cpElem.getAttributeValue(ConstantsXML.VALUE_FILTER).toString();
							cp.setValueFilter(valuef);
						}
						if(cpElem.getAttribute(ConstantsXML.ORDER)!=null){
							String order=cpElem.getAttributeValue(ConstantsXML.ORDER).toString();
							cp.setPriority(Integer.valueOf(order));
						}else{
							throw new ConfigException("Error: El atributo '"+ConstantsXML.ORDER+"' para la property "+cp.getIdPropName()+" es obligatorio en el XML");
						}
						
						list.add(cp.clone());
					}while(itrcp.hasNext());
					if(!list.isEmpty()){
						listcolumnsProp.put(sec, list);
						sec++;
					}
				}else{
					throw new ConfigException("Error: No se han definido nodos "+ConstantsXML.COLUMN_PROPERTY+" para la columnProperty con "+ConstantsXML.CLASS+": "+cp.getIdtoName()+" y "+ConstantsXML.CLASS_PARENT+": "+cp.getIdtoParentName());
				}
			}/*catch(ConfigException ex){
				System.err.println(ex.getMessage());
				success=false;
			
			}*/catch(Exception ex){
				throw ex;
			}
		}
		return success;
	}
}
