package dynagent.tools.importers.configxml;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.common.basicobjects.ColumnProperty;
import dynagent.common.basicobjects.OrderProperty;
import dynagent.common.basicobjects.Properties;
import dynagent.common.communication.IndividualData;
import dynagent.common.knowledge.Category;
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.action;
import dynagent.common.utils.QueryConstants;
import dynagent.server.database.dao.OrderPropertyDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.DeletableObject;
import dynagent.server.services.InstanceService;

public class importOrderProp extends ObjectConfig{

	private LinkedHashMap<Integer,LinkedList<OrderProperty>> listop;
	public importOrderProp(Element orderpropXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(orderpropXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listop=new LinkedHashMap<Integer,LinkedList<OrderProperty>>();
	}
	
	@Override
	public boolean configData() throws Exception {
		deleteOrderProperties();
		return extractAllOrderProperties();
	}
	@Override
	public void importData() throws Exception {
		insertOrderProperties();
	}
	
	private void deleteOrderProperties() throws SQLException, NamingException, NoSuchColumnException{
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_ORDERPROPERTY, fcdb), instanceService.getDataBaseMap(), fcdb);
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_ORDER, fcdb), instanceService.getDataBaseMap(), fcdb);
	}
	
	private void insertOrderProperties() throws Exception{
		Iterator<Integer> itop=this.listop.keySet().iterator();
		int idocount=0;
		TClaseDAO tdao = new TClaseDAO();
		PropertiesDAO propDAO = new PropertiesDAO();
		Integer idto = tdao.getTClaseByName(Constants.CLS_ORDERPROPERTY).getIDTO();
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();   
		while(itop.hasNext()){
			int sec=itop.next();
			idocount--;
			int ido=QueryConstants.getIdo(idocount, idto);
			String rdn=String.valueOf(sec);
			list.add(new FactInstance(idto, ido, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			
			OrderProperty op=listop.get(sec).getFirst();//Cogemos el primero para mirar el class y classParent
			
			//insertar el resto de las DataProperties
			int idtoClass = tdao.getTClaseByName(Constants.CLS_CLASS).getIDTO();
			if(op.getIdtoName()!=null){
				int prop = propDAO.getIdPropByName(Constants.PROP_DOMINIO);
				int value = QueryConstants.getIdo(tdao.getTClaseByName(op.getIdtoName()).getTableId(), idtoClass);
				list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
			}
			
						
			int valuecount=idocount;
			for(OrderProperty in:listop.get(sec)){//Procesamos todos
				//System.out.println("---> "+in.toString());
				valuecount--;
				int valueClass = tdao.getTClaseByName(Constants.CLS_ORDER).getIDTO();
				int value=QueryConstants.getIdo(valuecount, valueClass);
				int prop = propDAO.getIdPropByName(Constants.PROP_FIELDS);
								
				list.add(new FactInstance(idto, ido, prop, String.valueOf(value), valueClass, null, null, null, null, null, action.NEW));
				
				// Nos quedamos con el ido de la columna ya que es sobre el que vamos a trabajar
//				ido = value;
//				idto = valueClass;
				
				String rdnValue = String.valueOf(value); 
				list.add(new FactInstance(valueClass, value, Constants.IdPROP_RDN, "&"+rdnValue+"&", Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
				
				Properties property=propDAO.getPropertyByName(in.getPropName());
				prop = propDAO.getIdPropByName(Constants.PROP_PROPERTY);
				if(new Category(property.getCAT()).isDataProperty()){
					idtoClass = Constants.IDTO_DATA_PROPERTY;
				}else{
					idtoClass = Constants.IDTO_OBJECT_PROPERTY;
				}
				int idoClass = QueryConstants.getIdo(property.getTableId(),idtoClass);
				
				list.add(new FactInstance(valueClass, value, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
				
				prop = propDAO.getIdPropByName(Constants.PROP_ORDER);
				list.add(new FactInstance(valueClass, value, prop, null, Constants.IDTO_INT, null, new Double(in.getOrder()), new Double(in.getOrder()), null, null, action.NEW));
				
				//System.out.println("---> OK");
			}
			idocount=valuecount;
			//System.out.println("---> OK");
		}
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);

	}
	private boolean extractAllOrderProperties() throws Exception{
		Iterator itop = getChildrenXml().iterator();
		boolean success=true;
		int sec=1;
		while(itop.hasNext()){
			Element orderPElem = (Element)itop.next();
			OrderProperty op = new OrderProperty();
			try{
				if (orderPElem.getAttribute(ConstantsXML.CLASS)!=null){
					String className=orderPElem.getAttributeValue(ConstantsXML.CLASS).toString();
					Integer idtoClass=Auxiliar.getIdtoClass(className, fcdb);
					if (idtoClass!=null && Auxiliar.containsProp(op.getProp(),idtoClass,fcdb)){
						op.setIdtoName(className);
						op.setIdto(idtoClass);
					}else{
						//throw new ConfigException("Error: No existe la clase "+className+" en el modelo o no pertenece la property "+op.getProp()+" a dicha clase");
						continue;
					}
					
				}
				op.setSec(sec);
				
				Iterator itrop=orderPElem.getChildren().iterator();
				if (itrop.hasNext()){
					LinkedList<OrderProperty> list=new LinkedList<OrderProperty>();
					do{		
						Element opElem = (Element)itrop.next();
						if (opElem.getAttribute(ConstantsXML.PROP)!=null){
							String propName=opElem.getAttributeValue(ConstantsXML.PROP).toString();
							Integer idProp= Auxiliar.getIdProp(propName, fcdb);
							if (idProp!=null){
								op.setPropName(propName);
								op.setProp(idProp);
							}else{
								//throw new ConfigException("Error: La property "+propName+" no existe ");
								System.err.println("WARNING: La property "+propName+" no existe ");
								continue;
							}
						}else{
							throw new ConfigException("Error: El atributo '"+ConstantsXML.PROP+"' es oblicatorio en el XML");	
						}
						if (opElem.getAttribute(ConstantsXML.ORDER)!=null){
							String order=opElem.getAttributeValue(ConstantsXML.ORDER).toString();
							op.setOrder(Integer.valueOf(order));
						}else{
							throw new ConfigException("Error: El atributo '"+ConstantsXML.ORDER+"' es oblicatorio en el XML");	
						}
												
						list.add(op.clone());
					}while(itrop.hasNext());
					if(!list.isEmpty()){
						listop.put(sec, list);
						sec++;
					}
				}else{
					throw new ConfigException("Error: No se han definido nodos "+ConstantsXML.COLUMN_PROPERTY+" para la orderProperty con "+ConstantsXML.CLASS+": "+Auxiliar.getClassName(op.getIdto()));
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
