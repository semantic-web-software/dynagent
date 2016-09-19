package dynagent.tools.importers.configxml;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NamingException;

import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.Properties;
import dynagent.common.communication.IndividualData;
import dynagent.common.knowledge.Category;
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.action;
import dynagent.common.utils.QueryConstants;
import dynagent.server.database.IndividualCreator;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.DeletableObject;
import dynagent.server.services.InstanceService;

public class importDefValues extends ObjectConfig{
	private HashMap<String, HashMap<String,String>> valorespropiedadXclase=new HashMap<String, HashMap<String,String>>(); 
	private String sufixIndex;

	private IKnowledgeBaseInfo ik;
	public importDefValues(Element cardXML,FactoryConnectionDB fcdb, InstanceService instanceService,String sufixIndex,ConfigData configImport,String pathImportOtherXml,IKnowledgeBaseInfo ik) throws Exception {
		super(cardXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.ik=ik;
		this.sufixIndex = sufixIndex;
	}
	
	@Override
	public boolean configData() throws Exception {
		deleteDefValues();
		return extractAllDefValues();
	}
	@Override
	public void importData() throws Exception {
		insertDefValues();
	}
	
	private void deleteDefValues() throws SQLException, NamingException, NoSuchColumnException{
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_DEFAULT_VALUE, fcdb), instanceService.getDataBaseMap(), fcdb);
	}
	
	private boolean extractAllDefValues() throws Exception{
		//KnowledgeAdapter ka=new KnowledgeAdapter(ik);
		Iterator itDef = getChildrenXml().iterator();
		boolean success=true;
		while(itDef.hasNext()){
			Element cdElem = (Element)itDef.next();
			//Instance inst = new Instance();
			try{
				String className=null;
				String propName=null;
				String value=null;
				
				Integer idtoClass=null;
				Integer idProp=null;
				Integer valueClsInt=null;
				
				if(cdElem.getAttribute(ConstantsXML.CLASS)!=null){
					className=cdElem.getAttributeValue(ConstantsXML.CLASS).toString();
					idtoClass=Auxiliar.getIdtoClass(className, fcdb);
					if (idtoClass!=null){
						/*inst.setIDTO(idtoClass.toString());
						inst.setNAME(className);*/
					}else{
						//throw new ConfigException("Error: La clase '"+className+"' no existe en el modelo");
						continue;
					}
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.CLASS+"' es obligatorio en el nodo");
				}
				
				
				
				if (cdElem.getAttribute(ConstantsXML.PROP)!=null){
					propName=cdElem.getAttributeValue(ConstantsXML.PROP).toString();
					if(idtoClass!=null){
						idProp=Auxiliar.getIdProp(propName, idtoClass, fcdb);
					}else{
						idProp=Auxiliar.getIdProp(propName, fcdb);
					}
					if(idProp!=null){
						//inst.setPROPERTY(idProp.toString());
					}else{
						//throw new ConfigException("Error: La property '"+propName+"' no existe o no pertenece a la clase");
						continue;
					}
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.PROP+"' es obligatorio en el nodo");
				}
				
				if(cdElem.getAttributeValue(ConstantsXML.VALUECLS) != null){
					String valueClsName=cdElem.getAttributeValue(ConstantsXML.VALUECLS).toString();
					valueClsInt=Constants.getIdDatatype(valueClsName);
					if (valueClsInt!=null){
						//inst.setVALUECLS(valueClsInt.toString());
					}else{
						valueClsInt=Auxiliar.getIdtoClass(valueClsName, fcdb);
						if (valueClsInt!=null){
							//inst.setVALUECLS(valueClsInt.toString());
						}else{
							throw new ConfigException("Error: La clase "+valueClsName+" del atributo '"+ConstantsXML.VALUECLS+"' no existe");
						}
					}
				}else{
					if(ik.isObjectProperty(idProp))
						throw new ConfigException("Error: El atributo '"+ConstantsXML.VALUECLS+"' es obligatorio en el nodo con "+ConstantsXML.PROP+": "+propName+" ya que su valor no es de un tipo primitivo");
				}
				
				if(cdElem.getAttribute(ConstantsXML.VALUE)!=null){
					value=cdElem.getAttributeValue(ConstantsXML.VALUE).toString();
					int valueCls;
					System.err.println("IK:"+ik+" idProp:"+idProp);
					if(ik.isDataProperty(idProp)){
						valueCls=ik.getDatatype(idProp);
						if(valueClsInt!=null && !valueClsInt.equals(valueCls)){
							throw new ConfigException("Error: El valor '"+value+"' para la property '"+propName+"' no es compatible con el valueCls indicado:"+valueCls);
						}
					}else{
						/*if(valueClsInt==null){
							ArrayList<Integer> idos=Auxiliar.getIdos(value, fcdb);
							if(!idos.isEmpty()){
								if(idos.size()==1){
									//value=idos.get(0).toString();
									valueCls=Auxiliar.getIdto(idos.get(0), fcdb);
								}else{
									throw new ConfigException("Error: El valor '"+value+"' no es único en base de datos. Se debe indicar el atributo '"+ConstantsXML.VALUECLS+"'");
								}
							}else{
								throw new ConfigException("Error: El valor '"+value+"' no existe en base de datos");
							}
						}else{*/
							Integer ido=Auxiliar.getIdo(valueClsInt,value, fcdb, instanceService.getDataBaseMap());
							if(ido!=null){
								//value=ido.toString();
								//valueCls=Auxiliar.getIdto(ido, fcdb);
								valueCls=valueClsInt;
							}else{
								throw new ConfigException("Error: El valor '"+value+"' para la clase '"+Auxiliar.getClassName(valueClsInt)+"' no existe en base de datos");
							}
						/*}*/
					}
					
					if(Auxiliar.getCompatibilityRange(valueCls, idtoClass, idProp)!=null){
						/*IPropertyDef factInst=ka.traslateValueToFact(idtoClass, null, idProp ,valueCls, ka.buildValue(value, valueCls));
						inst.setQMIN(factInst.getQMIN()!=null?factInst.getQMIN().toString():null);
						inst.setQMAX(factInst.getQMAX()!=null?factInst.getQMAX().toString():null);
						inst.setVALUE(factInst.getVALUE());
						inst.setVALUECLS(factInst.getVALUECLS().toString());
						inst.setOP(Constants.OP_DEFAULTVALUE);*/
					}else{
						throw new ConfigException("Error: El valor '"+value+"' no es compatible para la property '"+propName+"'");
					}
					
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.VALUE+"' es obligatorio en el nodo");
				}
				
				
				if(valorespropiedadXclase.containsKey(className)){
					valorespropiedadXclase.get(className).put(propName, value);
				}else{
					HashMap<String,String> propValor=new HashMap<String, String>();
					propValor.put(propName, value);
					valorespropiedadXclase.put(className,propValor);
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
	
	/*private void insertDefValues() throws Exception {
		Iterator<String> itClases=this.valorespropiedadXclase.keySet().iterator();
		ArrayList<O_Datos_Attrib> regitrosODatosAtrib=new ArrayList<O_Datos_Attrib>();
		while(itClases.hasNext()){
			String clase=itClases.next();
			
			HashMap<String,String> valorXprop=valorespropiedadXclase.get(clase);
			Iterator<String> itPropiedades=valorXprop.keySet().iterator();
			while(itPropiedades.hasNext()){
				String nameProp=itPropiedades.next();
				String valor=valorXprop.get(nameProp);
				Integer  idtoClass=Auxiliar.getIdtoClass(clase, fcdb).intValue();
				Integer idProp=Auxiliar.getIdProp(nameProp, idtoClass,fcdb);
				if(idtoClass==null){
					throw new ConfigException("Error:  Configuracion incorrecta, no se encuentra la clase "+clase);
				}
				if(idProp==null){
					throw new ConfigException("Error:  Configuracion incorrecta, no se encuentra la propiedad "+nameProp);
				}
				int idtoValorXDef=Auxiliar.getIdtoClass(Constants.CLS_DEFAULT_VALUE, fcdb);
				int ido=IndividualCreator.creator(fcdb, DAOManager.getInstance().isCommit(), new GenerateSQL(fcdb.getGestorDB()), idtoValorXDef,null,null,sufixIndex,null,false,null).getIdo();
				//hay que crear registro para la propiedad y para el objeto.
				O_Datos_Attrib regTipoObjeto=new O_Datos_Attrib(idtoValorXDef,ido,Constants.IdPROP_OBJETO,null,clase,Constants.IDTO_STRING,null,null,null,null);
				regitrosODatosAtrib.add(regTipoObjeto);
				
				O_Datos_Attrib regTipoPropiedad=new O_Datos_Attrib(idtoValorXDef,ido,Constants.IdPROP_PROPIEDAD,null,nameProp,Constants.IDTO_STRING,null,null,null,null);
				regitrosODatosAtrib.add(regTipoPropiedad);
				O_Datos_Attrib regValor=new O_Datos_Attrib(idtoValorXDef,ido,Constants.IdPROP_VALOR,null,valor,Constants.IDTO_STRING,null,null,null,null);
				regitrosODatosAtrib.add(regValor);
			}
		}
		if(!regitrosODatosAtrib.isEmpty()){
			//System.out.println("---> "+regitrosODatosAtrib);
			Auxiliar.insertODatosAtrib(regitrosODatosAtrib);
			//System.out.println("---> OK");
		}
	}*/
	
	private void insertDefValues() throws Exception {
		//insertar en o_datos_atrib
		TClaseDAO tdao = new TClaseDAO();
		PropertiesDAO propDAO = new PropertiesDAO();
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();
		int countido=0;
		Iterator<String> itClases=this.valorespropiedadXclase.keySet().iterator();
		while(itClases.hasNext()){
			String className=itClases.next();
			
			HashMap<String,String> valorXprop=valorespropiedadXclase.get(className);
			Iterator<String> itPropiedades=valorXprop.keySet().iterator();
			while(itPropiedades.hasNext()){
				String propName=itPropiedades.next();
				String valor=valorXprop.get(propName);
				
				Integer idto = tdao.getTClaseByName(Constants.CLS_DEFAULT_VALUE).getIDTO();
				countido--;
				int ido=QueryConstants.getIdo(countido, idto);
				String rdn=String.valueOf("&"+ido+"&");
				list.add(new FactInstance(idto, ido, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
				
				int idtoClass = tdao.getTClaseByName(Constants.CLS_CLASS).getIDTO();
				int prop = propDAO.getIdPropByName(Constants.PROP_DOMINIO);
				int value = QueryConstants.getIdo(tdao.getTClaseByName(className).getTableId(), idtoClass);
				list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
				
				Properties property=propDAO.getPropertyByName(propName);
				prop = propDAO.getIdPropByName(Constants.PROP_PROPERTY);
				if(new Category(property.getCAT()).isDataProperty()){
					idtoClass = Constants.IDTO_DATA_PROPERTY;
				}else{
					idtoClass = Constants.IDTO_OBJECT_PROPERTY;
				}
				int idoClass = QueryConstants.getIdo(property.getTableId(),idtoClass);
				list.add(new FactInstance(idto, ido, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
				
				prop = propDAO.getIdPropByName(Constants.PROP_VALUE);
				list.add(new FactInstance(idto, ido, prop, valor, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			}
		}
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
	}
}
