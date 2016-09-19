package dynagent.tools.importers.configxml;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Alias;
import dynagent.common.basicobjects.Cardinality;
import dynagent.common.basicobjects.Properties;
import dynagent.common.communication.IndividualData;
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

public class importCard extends ObjectConfig{
	
	private LinkedList<Cardinality> listcard;
	public importCard(Element cardXML,FactoryConnectionDB fcdb,InstanceService instanceService,ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(cardXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listcard=new LinkedList<Cardinality>();
	}
	
	@Override
	public boolean configData() throws Exception {
		deleteCardinalities();
		return extractAllCardinalities();
	}
	@Override
	public void importData() throws Exception {
		insertCardinalities();
	}
	
	private void deleteCardinalities() throws SQLException, NamingException, NoSuchColumnException{
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_CARDINALITY, fcdb), instanceService.getDataBaseMap(), fcdb);
	}
	
	private boolean extractAllCardinalities() throws Exception{
		Iterator itcd = getChildrenXml().iterator();
		boolean success=true;
		while(itcd.hasNext()){
			Element cdElem = (Element)itcd.next();
			Cardinality cd = new Cardinality();
			try{
				if(cdElem.getAttribute(ConstantsXML.CLASS)!=null){
					String className=cdElem.getAttributeValue(ConstantsXML.CLASS).toString();
					Integer idtoClass=Auxiliar.getIdtoClass(className, fcdb);
					if (idtoClass!=null){
						cd.setClassName(className);
						cd.setIdtoClass(idtoClass);
					}else{
						throw new ConfigException("Error: La clase '"+className+"' no existe en el modelo");
					}
				}
				
				if (cdElem.getAttribute(ConstantsXML.PROP)!=null){
					String propName=cdElem.getAttributeValue(ConstantsXML.PROP).toString();
					Integer idProp=null;
					if(cd.getIdtoClass()!=null){
						idProp=Auxiliar.getIdProp(propName, cd.getIdtoClass(),fcdb);
					}else{
						idProp=Auxiliar.getIdProp(propName, fcdb);
					}
					if(idProp!=null){
						cd.setIdProp(idProp);
						cd.setProp(propName);
					}else{
						throw new ConfigException("Error: La property '"+propName+"' no existe o no pertenece a la clase");
					}
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.PROP+"' es obligatorio en el nodo");
				}
				
				if(cdElem.getAttribute(ConstantsXML.CARMAX)!=null){
					String carmax=cdElem.getAttributeValue(ConstantsXML.CARMAX).toString();
					cd.setCardMax(Integer.valueOf(carmax));
				}/*else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.CARMAX+"' es obligatorio en el nodo");
				}*/
				
				if(cdElem.getAttribute(ConstantsXML.CARMIN)!=null){
					String carmin=cdElem.getAttributeValue(ConstantsXML.CARMIN).toString();
					cd.setCardMin(Integer.valueOf(carmin));
				}/*else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.CARMIN+"' es obligatorio en el nodo");
				}*/
				
				if(cdElem.getAttribute(ConstantsXML.CARMAX)==null && cdElem.getAttribute(ConstantsXML.CARMIN)==null)
					throw new ConfigException("Error: Tiene que indica al menos el atributo '"+ConstantsXML.CARMIN+"' ó '"+ConstantsXML.CARMAX+"' en el nodo");
				
				listcard.add(cd);
				
				//Si hemos filtrado por clase, lo aplicamos tambien a sus especializados
				if(cd.getIdtoClass()!=null){
					Iterator<Integer> itr=Auxiliar.getIdtoSpecialized(cd.getIdtoClass()).iterator();
					while(itr.hasNext()){
						int idto=itr.next();
						Cardinality cdSpecialized=(Cardinality)cd.clone();
						cdSpecialized.setIdtoClass(idto);
						cdSpecialized.setClassName(Auxiliar.getClassName(idto));
						
						listcard.add(cdSpecialized);
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
	private void insertCardinalities() throws Exception {
		Iterator<Cardinality> itcd=this.listcard.iterator();
		TClaseDAO tdao = new TClaseDAO();
		PropertiesDAO propDAO = new PropertiesDAO();
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();
		int countido=0;
		while(itcd.hasNext()){
			Cardinality in =itcd.next();
			//System.out.println("---> "+c.toString());
			//Auxiliar.updateCardinality(in,fcdb);
			
			Integer idto = tdao.getTClaseByName(Constants.CLS_CARDINALITY).getIDTO();
			countido--;
			int ido=QueryConstants.getIdo(countido, idto);
			String rdn=String.valueOf("&"+ido+"&");
			list.add(new FactInstance(idto, ido, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			
			int idtoClass = tdao.getTClaseByName(Constants.CLS_CLASS).getIDTO();
			if(in.getClassName()!=null){
				int prop = propDAO.getIdPropByName(Constants.PROP_DOMINIO);
				int value = QueryConstants.getIdo(tdao.getTClaseByName(in.getClassName()).getTableId(), idtoClass);
				list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
			}
			
			int idoClass;
			Properties property;
			
			if(in.getIdProp()!=null){
				property=propDAO.getPropertyByID(in.getIdProp());
				int prop = propDAO.getIdPropByName(Constants.PROP_PROPERTY);
				if(new Category(property.getCAT()).isDataProperty()){
					idtoClass = Constants.IDTO_DATA_PROPERTY;
				}else{
					idtoClass = Constants.IDTO_OBJECT_PROPERTY;
				}
				idoClass = QueryConstants.getIdo(property.getTableId(),idtoClass);
				
				list.add(new FactInstance(idto, ido, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
			}
			
			if(in.getCardMin()!=null){
				int prop = propDAO.getIdPropByName(Constants.PROP_CARMIN);
				list.add(new FactInstance(idto, ido, prop, null, Constants.IDTO_INT, null, new Double(in.getCardMin()), new Double(in.getCardMin()), null, null, action.NEW));
			}
			
			if(in.getCardMax()!=null){
				int prop = propDAO.getIdPropByName(Constants.PROP_CARMAX);
				list.add(new FactInstance(idto, ido, prop, null, Constants.IDTO_INT, null, new Double(in.getCardMax()), new Double(in.getCardMax()), null, null, action.NEW));
			}
			
			//System.out.println("---> OK");
		}
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
		
	}
}
