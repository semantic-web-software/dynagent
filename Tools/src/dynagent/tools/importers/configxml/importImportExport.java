package dynagent.tools.importers.configxml;

import java.util.ArrayList;
import java.util.Iterator;

import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.common.basicobjects.UTask;
import dynagent.common.utils.jdomParser;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class importImportExport extends importUtask{
	
	public importImportExport(Element importexportXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(importexportXML,fcdb,instanceService,configImport,pathImportOtherXml);
	}

	protected boolean extractAllUtask() throws Exception{
		ArrayList children=new ArrayList();
		children.addAll(xml.getChildren(ConstantsXML.IMPORT));
		children.addAll(xml.getChildren(ConstantsXML.EXPORT));
		Iterator itut = children.iterator();
		boolean success=true;
		while(itut.hasNext()){
			Element utElem = (Element)itut.next();
			String name=utElem.getName();
			UTask ut = new UTask();
			try{
				if (utElem.getAttribute(ConstantsXML.AREA_FUNC_ATRB)!=null){
					String afName=utElem.getAttributeValue(ConstantsXML.AREA_FUNC_ATRB).toString();
					Integer idoaf=Auxiliar.getIdo(Constants.IDTO_FUNCTIONAL_AREA, afName, fcdb, instanceService.getDataBaseMap());
					if (idoaf!=null){
						ut.setAreaFuncName(afName);
						ut.setIdtoAreaFunc(idoaf);
					}else{
						throw new ConfigException("Error: No se encontro el area funcional "+ afName);
					}
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.AREA_FUNC_ATRB+"' es obligatorio en el nodo");
				}
				
				if (utElem.getAttribute(ConstantsXML.NAME)!=null){
					String utaskName=utElem.getAttributeValue(ConstantsXML.NAME).toString();
					if(!configImport.containsUserTask(utaskName))
						ut.setUtaskName(utaskName);
					else{
						throw new ConfigException("Error: Hay definidas dos userTasks con el mismo nombre '"+utaskName+"'");
					}
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.NAME+"' es obligatorio en el nodo");
				}
				
								
				Element tg_class=utElem.getChild(ConstantsXML.TG_CLASS);
				if (tg_class!=null){
					if(name.equals(ConstantsXML.EXPORT))
						throw new ConfigException("Error: Los nodos '"+ConstantsXML.EXPORT+"' no pueden tener hijo '"+ConstantsXML.TG_CLASS+"'. Ocurre con '"+ut.getUtaskName()+"'.");
					if (tg_class.getAttribute(ConstantsXML.CLASS)!=null){
						String tgcname=tg_class.getAttributeValue(ConstantsXML.CLASS).toString();
						Integer idtotgc=Auxiliar.getIdtoClass(tgcname, fcdb);
						if (idtotgc!=null){
							ut.setTargetClass(idtotgc);
							ut.setTargetClassName(tgcname);
						}else{
							throw new ConfigException("Error: La clase '"+tgcname+"' no existe en el modelo");
						}
					}else{
						throw new ConfigException("Error: El atributo '"+ConstantsXML.CLASS+"' es obligatorio en la TG_CLASS. utElem="+utElem.getAttributeValue(ConstantsXML.NAME).toString());
					}
					
					if (tg_class.getAttribute(ConstantsXML.CARMIN)!=null){
						String cmin=tg_class.getAttributeValue(ConstantsXML.CARMIN).toString();
						ut.setCminTGC(Integer.valueOf(cmin));
					}/*else{
						System.err.println("Error: El atributo '"+ConstantsXML.CARMIN+"' es obligatorio en la TG_CLASS");
						throw new ConfigXMLException();
					}*/
					
					if (tg_class.getAttribute(ConstantsXML.CARMAX)!=null){
						String cmax=tg_class.getAttributeValue(ConstantsXML.CARMAX).toString();
						ut.setCmaxTGC(Integer.valueOf(cmax));
					}/*else{
						System.err.println("Error: El atributo '"+ConstantsXML.CARMAX+"' es obligatorio en la TG_CLASS");
						throw new ConfigXMLException();
					}*/
				}else{
					if(name.equals(ConstantsXML.IMPORT))
						throw new ConfigException("Error: No se ha definido la TargetClass para la UTask '"+ut.getUtaskName()+"'");
				}
				
				
				Element s_class=utElem.getChild(ConstantsXML.S_CLASS);
				if (s_class!=null){
					
					if(name.equals(ConstantsXML.IMPORT))
						throw new ConfigException("Error: Los nodos '"+ConstantsXML.IMPORT+"' no pueden tener hijo '"+ConstantsXML.S_CLASS+"'. Ocurre con '"+ut.getUtaskName()+"'.");
					
					if (s_class.getAttribute(ConstantsXML.CLASS)!=null){
						String scname=s_class.getAttributeValue(ConstantsXML.CLASS).toString();
						Integer idtosc=Auxiliar.getIdtoClass(scname, fcdb);
						if (idtosc!=null){
							ut.setSourceClass(idtosc);
							ut.setSourceClassName(scname);
						}else{
							throw new ConfigException("Error: La clase '"+scname+"' no existe en el modelo");
						}
					}else{
						throw new ConfigException("Error: El atributo '"+ConstantsXML.CLASS+"' es obligatorio en la S_CLASS");
					}
					
					if (s_class.getAttribute(ConstantsXML.CARMIN)!=null){
						String cmin=s_class.getAttributeValue(ConstantsXML.CARMIN).toString();
						ut.setCminSC(Integer.valueOf(cmin));
					}/*else{
						System.err.println("Error: El atributo '"+ConstantsXML.CARMIN+"' es obligatorio en la S_CLASS");
						throw new ConfigXMLException();
					}*/
					
					if (s_class.getAttribute(ConstantsXML.CARMAX)!=null){
						String cmax=s_class.getAttributeValue(ConstantsXML.CARMAX).toString();
						ut.setCmaxSC(Integer.valueOf(cmax));
					}/*else{
						System.err.println("Error: El atributo '"+ConstantsXML.CARMAX+"' es obligatorio en la S_CLASS");
						throw new ConfigXMLException();
					}*/
				}else{
					if(name.equals(ConstantsXML.EXPORT))
						throw new ConfigException("Error: No se ha definido el SourceClass para la UTask '"+ut.getUtaskName()+"'");
				}
				
				Element help=utElem.getChild(ConstantsXML.DESCRIPTION);
				if (help!=null){
					String language=null;
					if (help.getAttribute(ConstantsXML.LANGUAGE)!=null){
						language=help.getAttributeValue(ConstantsXML.LANGUAGE).toString();
					}else{
						throw new ConfigException("Error: El atributo '"+ConstantsXML.LANGUAGE+"' es obligatorio en el nodo "+ConstantsXML.DESCRIPTION);
					}
					String description=jdomParser.returnXML(help.getContent(),false);
					ut.addHelp(language, description);
				}
				
				if (utElem.getAttribute(ConstantsXML.USERROL)!=null){
					String uRoles=utElem.getAttributeValue(ConstantsXML.USERROL).toString();
					ArrayList<String> aURoles = new ArrayList<String>();
					String[] uRolesSpl = uRoles.split(",");
					for (int i=0;i<uRolesSpl.length;i++) {
						String uRol = uRolesSpl[i];
						//TODO comprobar si es correcto
						aURoles.add(uRol);
					}
					ut.setAURoles(aURoles);
				}
				
				/*Element params=utElem.getChild(ConstantsXML.PARAMS);
				if (params!=null){
					if (params.getAttribute(ConstantsXML.CLASS)!=null){
						String pname=params.getAttributeValue(ConstantsXML.CLASS).toString();
						Integer idtop=Auxiliar.getIdtoClass(pname, fcdb);
						if (idtop!=null){
							ut.setIdtoParams(idtop);
							ut.setParamsName(pname);
						}else{
							throw new ConfigException("Error: La clase '"+pname+"' no existe en el modelo");
						}
					}else{
						throw new ConfigException("Error: El atributo '"+ConstantsXML.CLASS+"' es obligatorio en la PARAMS");
					}
					
				}*/
				if(name.equals(ConstantsXML.IMPORT))
					ut.setType(UTask.IMPORT);
				else if(name.equals(ConstantsXML.EXPORT))
					ut.setType(UTask.EXPORT);
				else throw new ConfigException("Error: La etiqueta '"+name+"' no es correcta. Ese hijo no está soportado por "+ConstantsXML.IMPORTEXPORTS);
				
				listUtask.add(ut);
				configImport.addUserTask(ut.getUtaskName(),ut.getTargetClassName());
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
