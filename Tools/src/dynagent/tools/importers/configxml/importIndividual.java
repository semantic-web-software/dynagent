package dynagent.tools.importers.configxml;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.naming.NamingException;

import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.KnowledgeAdapter;
import dynagent.server.database.IndividualCreator;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.InstanceService;
	
public class importIndividual extends ObjectConfig{
	
	private LinkedHashMap<ArrayList<O_Datos_Attrib>,String> listindividual;
	private IKnowledgeBaseInfo ik;
	
	public importIndividual(Element individualXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport, String pathImportOtherXml, IKnowledgeBaseInfo ik) throws Exception {
		super(individualXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listindividual=new LinkedHashMap<ArrayList<O_Datos_Attrib>,String>();
		this.ik=ik;
	}
	
	@Override
	public boolean configData() throws Exception {
		return extractAllIndividual();
	}
	@Override
	public void importData() throws Exception {
		insertIndividual();
	}

	private boolean extractAllIndividual() throws Exception {	
		KnowledgeAdapter ka=new KnowledgeAdapter(ik);
		Iterator itIndividual = getChildrenXml().iterator();
		boolean success=true;
		while(itIndividual.hasNext()){
			Element individualElem = (Element)itIndividual.next();
			try{
				O_Datos_Attrib d=new O_Datos_Attrib();
				String classname=null;
				if(individualElem.getAttribute(ConstantsXML.CLASS)!=null){
					classname=individualElem.getAttributeValue(ConstantsXML.CLASS).toString();
					Integer idtoClass=Auxiliar.getIdtoClass(classname, fcdb);
					if (idtoClass!=null){
						d.setIDTO(idtoClass);
					}else{
						throw new ConfigException("Error: No existe la clase "+classname+" no existe en el modelo");
					}
				}
				ArrayList<O_Datos_Attrib> listPropIndividual=new ArrayList<O_Datos_Attrib>();
				Iterator itrProp=individualElem.getChildren().iterator();
				if (itrProp.hasNext()){
					boolean validIndividual=true;
					String rdn=null;
					do{
						Element propElement=(Element)itrProp.next();
						
						String propName;
						if (propElement.getAttribute(ConstantsXML.NAME)!=null){
							propName=propElement.getAttributeValue(ConstantsXML.NAME).toString();
							Integer idProp=Auxiliar.getIdProp(propName, d.getIDTO(), fcdb);
							
							if(idProp!=null){
								d.setPROPERTY(idProp);
							}else{
								throw new ConfigException("Error: La property '"+propName+"' no existe o no pertenece a la clase");
							}
						}else{
							throw new ConfigException("Error: El atributo '"+ConstantsXML.PROP+"' es obligatorio en el nodo");
						}
						
						Integer valueClsInt=null;
						if(propElement.getAttributeValue(ConstantsXML.VALUECLS) != null){
							String valueClsName=propElement.getAttributeValue(ConstantsXML.VALUECLS).toString();
							valueClsInt=Constants.getIdDatatype(valueClsName);
							if (valueClsInt!=null){
								//d.setVALUECLS(valueClsInt);
							}else{
								valueClsInt=Auxiliar.getIdtoClass(valueClsName, fcdb);
								if (valueClsInt!=null){
									//d.setVALUECLS(valueClsInt);
								}else{
									throw new ConfigException("Error: La clase "+valueClsName+" del atributo '"+ConstantsXML.VALUECLS+"' no existe");
								}
							}
											
						}else{
							if(ik.isObjectProperty(d.getPROPERTY()))
								throw new ConfigException("Error: El atributo '"+ConstantsXML.VALUECLS+"' es obligatorio en el nodo con "+ConstantsXML.PROP+": "+propName+" ya que su valor no es de un tipo primitivo");
						}
						
						if(propElement.getAttribute(ConstantsXML.VALUE)!=null){
							String value=propElement.getAttributeValue(ConstantsXML.VALUE).toString();
							int valueCls;
							//System.err.println("IK:"+ik+" idProp:"+idProp);
							if(ik.isDataProperty(d.getPROPERTY())){
								valueCls=ik.getDatatype(d.getPROPERTY());
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
							
							if(Auxiliar.getCompatibilityRange(valueCls, d.getIDTO(), d.getPROPERTY())!=null){
								IPropertyDef factInst=ka.traslateValueToFact(d.getIDTO(), null, d.getPROPERTY() ,valueCls, ka.buildValue(value, valueCls));
								d.setQMIN(factInst.getQMIN());
								d.setQMAX(factInst.getQMAX());
								if(ik.isObjectProperty(d.getPROPERTY())){
									int val=new Integer(factInst.getVALUE());
									d.setVALNUM(val);
									d.setVALTEXTO(ik.getRdnIfExistInRuler(val));
								}else d.setVALTEXTO(factInst.getVALUE());
								d.setVALUECLS(factInst.getVALUECLS());
							}else{
								throw new ConfigException("Error: El valor '"+value+"' no es compatible para la property '"+propName+"'");
							}
							
						}else{
							throw new ConfigException("Error: El atributo '"+ConstantsXML.VALUE+"' es obligatorio en el nodo");
						}
						
						if(d.getPROPERTY().equals(Constants.IdPROP_RDN)){
							if(Auxiliar.getIdo(d.getIDTO(), d.getVALTEXTO(), fcdb, instanceService.getDataBaseMap())!=null){
								System.err.println("WARNING: El individuo con property "+propName+"="+d.getVALTEXTO()+" no será importado ya que existe en la base de datos");
								validIndividual=false;
								break;
							}
							rdn=d.getVALTEXTO();
						}
						listPropIndividual.add(d);
						
					}while(itrProp.hasNext());
					
					if(validIndividual){
						listindividual.put(listPropIndividual,rdn);
					}
				}else{
					throw new ConfigException("Error: No se han definido nodos "+ConstantsXML.PROPERTY+" para el individuo de "+classname);
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
	
	private void insertIndividual() throws SQLException, NamingException, DataErrorException {
		Iterator<ArrayList<O_Datos_Attrib>> itod=this.listindividual.keySet().iterator();
		O_Datos_AttribDAO oDAO= new O_Datos_AttribDAO();
		oDAO.open();
		while(itod.hasNext()){
			ArrayList<O_Datos_Attrib> listData=itod.next();
			Iterator<O_Datos_Attrib> itrData =listData.iterator();
			Integer ido=null;
			boolean first=true;
			while(itrData.hasNext()){
				O_Datos_Attrib d=itrData.next();
				if(first){
					//TODO No funcionaria con las clases que tienen sufijo en index ya que le estamos pasando null
					ido=IndividualCreator.creator(fcdb, DAOManager.getInstance().isCommit(), new GenerateSQL(fcdb.getGestorDB()), d.getIDTO(),listindividual.get(listData),null,null,null,false,null).getIdo();
					first=false;
				}
				d.setIDO(ido);
				//System.out.println("---> "+d.toString());
				oDAO.insert(d);
				//System.out.println("---> OK");
			}
		}
		oDAO.close();
	}

}
