package dynagent.tools.importers.configxml;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.Element;

import dynagent.common.basicobjects.Range;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class importRanges extends ObjectConfig{
	private LinkedList<Range> listRanges;//Rangos definidos
	private LinkedList<Range> listRangesActual;//Rangos actuales, que seran borrados para ser sustituidos por los definidos
	private HashMap<Integer,HashMap<Integer,ArrayList<Range>>> listIdtoIdPropSpecializ;//Almacena los idto,idProp para el que se ha dicho algo a traves de herencia.
	private HashMap<Integer,HashMap<Integer,ArrayList<Range>>> listIdtoIdProp;//Almacena los idto,idProp para el que se ha dicho algo especificamente
	private IKnowledgeBaseInfo ik;
	public importRanges(Element rangoXML, FactoryConnectionDB fcdb, InstanceService instanceService, IKnowledgeBaseInfo ik, ConfigData configImport,String pathImportOtherXml) throws Exception{
		super(rangoXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.ik=ik;
		this.listRanges=new LinkedList<Range>();
		this.listRangesActual=new LinkedList<Range>();
		this.listIdtoIdProp=new HashMap<Integer, HashMap<Integer,ArrayList<Range>>>();
		this.listIdtoIdPropSpecializ=new HashMap<Integer, HashMap<Integer,ArrayList<Range>>>();
	}
	
	@Override
	public boolean configData() throws Exception {
		return extractAllRanges();
	}
	@Override
	public void importData() throws Exception {
		deleteRangesActual();
		insertRanges();
	}

	private void deleteRangesActual() throws Exception {
		Iterator<Range> itrg=this.listRangesActual.iterator();
		while(itrg.hasNext()){
			Range rg=itrg.next();
			//System.out.println("Delete---> "+rg.toString());
			Auxiliar.deleteRange(rg,fcdb);
			//System.out.println("---> OK");
		}
	}
	
	private void insertRanges() throws NamingException, SQLException {
		Iterator<Range> itrg=this.listRanges.iterator();
		while(itrg.hasNext()){
			Range rg=itrg.next();
			//System.out.println("Insert---> "+rg.toString());
			Auxiliar.insertRange(rg,fcdb);
			//System.out.println("---> OK");
		}
	}
	private boolean extractAllRanges() throws Exception {
		Iterator itrg = getChildrenXml().iterator();
		boolean success=true;
		while(itrg.hasNext()){
			Element rgElem = (Element)itrg.next();
			Range rg = new Range();
			try{
				if(rgElem.getAttributeValue(ConstantsXML.CLASS)!=null){
					String className=rgElem.getAttributeValue(ConstantsXML.CLASS).toString();
					Integer idtoClass=Auxiliar.getIdtoClass(className, fcdb);
					if(idtoClass!=null){
						rg.setClassName(className);
						rg.setIdtoClass(idtoClass);
					}else{
						//throw new ConfigException("Error: La clase '"+className+"' no existe en el modelo");
						System.err.println("WARNING: La clase '"+className+"' no existe en el modelo. No se importa el cambio de rango.");
						continue;
					}
					
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.CLASS+"' es obligatorio en el nodo");
				}
				
				if(rgElem.getAttributeValue(ConstantsXML.PROP)!=null){
					String propName=rgElem.getAttributeValue(ConstantsXML.PROP).toString();
					Integer idProp=Auxiliar.getIdProp(propName, rg.getIdtoClass(), fcdb);
					if (idProp!=null){
						rg.setIdProp(idProp);
						rg.setPropName(propName);
					}else{
						//throw new ConfigException("Error: La property '"+propName+"' no existe en el modelo o no pertenece a "+rg.getClassName());
						System.err.println("WARNING: La property '"+propName+"' no existe en el modelo o no pertenece a "+rg.getClassName()+". No se importa el cambio de rango.");
						continue;
					}
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.PROP+"' es obligatorio en el nodo");
				}
				
				Iterator<Element> itRg=rgElem.getChildren().iterator();
				if(itRg.hasNext()){
					Range rgNew=(Range)rg.clone();
					Range rgActual=(Range)rg.clone();
					do{
						Element inRg=itRg.next();
						if (inRg.getAttributeValue(ConstantsXML.CLASS)!=null){	
							String className=inRg.getAttributeValue(ConstantsXML.CLASS);
							Integer idto= Auxiliar.getIdtoClass(className, fcdb);
							if (idto!=null){
								Integer idtoRangeActual=Auxiliar.getCompatibilityRange(idto, rg.getIdtoClass(),rg.getIdProp());
								if (idtoRangeActual==null){
									Iterator<Integer> itr=Auxiliar.getRanges(rg.getIdtoClass(),rg.getIdProp()).iterator();
									ArrayList<String> listClassNameActual=new ArrayList<String>();
									while(itr.hasNext()){
										int idtoActual=itr.next();
										String classNameActual=Auxiliar.getClassName(idtoActual);
										listClassNameActual.add(classNameActual);
										rgActual.addRange(idtoActual,classNameActual);
									}
									System.err.println("WARNING: Se ha redefinido como rango '"+className+"' para la propiedad '"+rg.getPropName()+"' que no es hija del rango genérico, quitandose del rango "+listClassNameActual);
								}else{
									rgActual.addRange(idtoRangeActual,Auxiliar.getClassName(idtoRangeActual));
								}
								if(inRg.getName().equals(ConstantsXML.VALUE_RANGE)){
									rgNew.addRange(idto,className);
								}else if(inRg.getName().equals(ConstantsXML.DELETE_RANGE)){
									if(!dynagent.common.utils.Auxiliar.equals(idtoRangeActual,idto)){
										throw new ConfigException("Error: El rango '"+className+"' no se puede eliminar ya que no existe en la property '"+rg.getPropName()+"'");
									}
								}else{
									throw new ConfigException("Error: El nodo '"+inRg.getName()+"' no pertenece a "+ConstantsXML.RANGE);
								}
							}else{
								//throw new ConfigException("Error: La clase "+className+" no existe en el modelo");
								System.err.println("WARNING: La clase "+className+" no existe en el modelo. No se importa ese rango.");
								continue;
							}
						}else{
							throw new ConfigException("Error: El atributo "+ConstantsXML.CLASS+" es obligatorio");
						}	
						
					}while(itRg.hasNext());
					
					if(!rgNew.getRanges().isEmpty())
						listRanges.add(rgNew);
					
					listRangesActual.add(rgActual);
					
					
					addListRangeSpecific(rg,rgNew,rgActual);
					// Si hay algo definido anteriormente, que haya sido obtenido a partir de la herencia, se borra. Ya que la definicion especifica tiene prioridad
					removeListRangeSpecialized(rg.getIdtoClass(),rg.getIdProp());
					
					
					// Si hemos filtrado por clase, lo aplicamos tambien a sus especializados
					if(rg.getIdtoClass()!=null){
						Iterator<Integer> itr=Auxiliar.getIdtoSpecialized(rg.getIdtoClass()).iterator();
						while(itr.hasNext()){
							int idto=itr.next();		
														
							HashMap<Integer,ArrayList<Range>> hashIdProp=listIdtoIdProp.get(idto);
							// Lo definimos para el especializado si no hay ningun especifico(asignado directamente, y no por la herencia) ya definido
							if(hashIdProp==null || hashIdProp.get(rgNew.getIdProp())==null || hashIdProp.get(rgNew.getIdProp()).isEmpty()){
								HashMap<Integer,String> hashIdtoNameRangeActualSpecializ=new HashMap<Integer, String>();
								Iterator<Integer> itrRangesNew=rgNew.getRanges().keySet().iterator();
								while(itrRangesNew.hasNext()){
									int idtoRangeNew=itrRangesNew.next();
									Integer idtoRangeActual=Auxiliar.getCompatibilityRange(idtoRangeNew, idto,rgNew.getIdProp());
									if (idtoRangeActual!=null){
										HashMap<Integer,ArrayList<Range>> hashIdPropSpecializ=listIdtoIdPropSpecializ.get(idto);
										if(hashIdPropSpecializ==null){
											hashIdPropSpecializ=new HashMap<Integer, ArrayList<Range>>();
											listIdtoIdPropSpecializ.put(idto, hashIdPropSpecializ);
										}
										ArrayList<Range> listSpecializ=hashIdPropSpecializ.get(rg.getIdProp());
										if(listSpecializ==null || listSpecializ.isEmpty()){
											listSpecializ=new ArrayList<Range>();
											hashIdPropSpecializ.put(rg.getIdProp(), listSpecializ);
										}else{
											ArrayList<Range> auxListSpecializ=new ArrayList<Range>();
											auxListSpecializ.addAll(listSpecializ);
											Iterator<Range> itrSpecializ=auxListSpecializ.iterator();
											while(itrSpecializ.hasNext()){
												Range rgSpec=itrSpecializ.next();
												// Habiendosele asignado anteriormente un rango por herencia. Se remueve esa definicion si estamos sobre una clase mas especializada que la que se utilizo anteriormente
												if(!Auxiliar.getIdtoParents(rgSpec.getIdtoAncestor()).contains(rg.getIdtoClass())){
													listRanges.remove(rgSpec);
													listRangesActual.remove(rgSpec);
													listSpecializ.remove(rgSpec);
												}
											}
										}
										
										hashIdtoNameRangeActualSpecializ.put(idtoRangeActual, Auxiliar.getClassName(idtoRangeActual));
									}else{
										System.err.println(".....INFO: El rango de la clase "+Auxiliar.getClassName(idto)+" no será modificado por herencia desde "+rg.getClassName()+" ya que su rango no es compatible con "+rgNew.getRanges().get(idtoRangeNew));
									}
								}
								
								if(!hashIdtoNameRangeActualSpecializ.isEmpty()){
									
									if(!rgNew.getRanges().isEmpty()){
										Range rgSpecializedNew=(Range)rgNew.clone();
										rgSpecializedNew.setIdtoAncestor(rg.getIdtoClass());
										rgSpecializedNew.setClassNameAncestor(rg.getClassNameAncestor());
										rgSpecializedNew.setIdtoClass(idto);
										rgSpecializedNew.setClassName(Auxiliar.getClassName(idto));
									
										listIdtoIdPropSpecializ.get(idto).get(rg.getIdProp()).add(rgSpecializedNew);
										listRanges.add(rgSpecializedNew);
									}
									
									String className=Auxiliar.getClassName(idto);
									Range rgSpecializedActual=(Range)rgActual.clone();
									rgSpecializedActual.setIdtoAncestor(rg.getIdtoClass());
									rgSpecializedActual.setClassNameAncestor(rg.getClassNameAncestor());
									rgSpecializedActual.setIdtoClass(idto);
									rgSpecializedActual.setClassName(className);
									
									rgSpecializedActual.setRanges(hashIdtoNameRangeActualSpecializ);
									listIdtoIdPropSpecializ.get(idto).get(rg.getIdProp()).add(rgSpecializedActual);
									listRangesActual.add(rgSpecializedActual);
								}
							}
						}
					}
					
				}else{
					throw new ConfigException("Error: Es obligatorio algun hijo '"+ConstantsXML.VALUE_RANGE+"' para el nodo "+ConstantsXML.RANGE);
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
	
	private void addListRangeSpecific(Range rg,Range rgNew,Range rgActual){
		HashMap<Integer,ArrayList<Range>> hashIdProp=listIdtoIdProp.get(rg.getIdtoClass());
		if(hashIdProp==null){
			hashIdProp=new HashMap<Integer, ArrayList<Range>>();
			listIdtoIdProp.put(rg.getIdtoClass(), hashIdProp);
		}
		ArrayList<Range> list=hashIdProp.get(rg.getIdProp());
		if(list==null){
			list=new ArrayList<Range>();
			hashIdProp.put(rg.getIdProp(), list);
		}
		list.add(rgNew);
		list.add(rgActual);
	}
	
	private void removeListRangeSpecialized(Integer idto,int idProp){
		HashMap<Integer,ArrayList<Range>> hashIdPropSpeclz=listIdtoIdPropSpecializ.get(idto);
		if(hashIdPropSpeclz!=null){
			ArrayList<Range> listSpec=hashIdPropSpeclz.get(idProp);
			if(listSpec!=null){
				ArrayList<Range> auxListSpecz=new ArrayList<Range>();
				auxListSpecz.addAll(listSpec);
				Iterator<Range> itr=auxListSpecz.iterator();
				while(itr.hasNext()){
					Range rgWithHerency=itr.next();
					listRanges.remove(rgWithHerency);
					listRangesActual.remove(rgWithHerency);
					
					listSpec.remove(rgWithHerency);
				}
			}
		}
	}

}
