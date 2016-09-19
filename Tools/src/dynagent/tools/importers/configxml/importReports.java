package dynagent.tools.importers.configxml;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Param;
import dynagent.common.basicobjects.Report;
import dynagent.common.basicobjects.SubReport;
import dynagent.common.communication.IndividualData;
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

public class importReports extends ObjectConfig{
	private String path;
	private String pathImportFile;
	private LinkedList<Report> listreport;
	public importReports(Element reportsXML, String path, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml,String pathImportFile) throws Exception {
		super(reportsXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listreport=new LinkedList<Report>();
		this.path=path;
		this.pathImportFile=pathImportFile;
	}
	
	@Override
	public boolean configData() throws Exception {
		return configData(null);
	}
	@Override
	public void importData() throws Exception {
		importAllReports();
	}
	
	public boolean configData(String uniqueNameInclude) throws Exception {
		//path=getPathReports();
//		if(uniqueNameInclude==null){
//			deleteAllReports();
//		}
		return extractAllReports(uniqueNameInclude);
	}
	
	private void deleteAllReports() throws SQLException, NamingException, NoSuchColumnException{
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_REPORT_INDIVIDUAL, fcdb), instanceService.getDataBaseMap(), fcdb);
	}
	
	private void importAllReports() throws Exception{
		Iterator<Report> itrp=this.listreport.iterator();
		TClaseDAO tdao = new TClaseDAO();
		PropertiesDAO propDAO = new PropertiesDAO();
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();
		HashSet<String> listNameFilesImported=new HashSet<String>();  
		int countido=0;
		
		File userFiles = new File(pathImportFile);
		if (! userFiles.exists()){
			userFiles.mkdirs();
		}
		
		while(itrp.hasNext()){
			Report in=itrp.next();
			Integer idto = tdao.getTClaseByName(Constants.CLS_REPORT_INDIVIDUAL).getIDTO();
			countido--;
			int ido=QueryConstants.getIdo(countido, idto);
			String rdn=in.getName();
			list.add(new FactInstance(idto, ido, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			
			int idtoClass = tdao.getTClaseByName(Constants.CLS_CLASS).getIDTO();
			int prop = propDAO.getIdPropByName(Constants.PROP_DOMINIO);
			int value = QueryConstants.getIdo(tdao.getTClaseByName(in.getTargetClassName()).getTableId(), idtoClass);
			list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
			
			String extension = ".jrxml";
			prop = propDAO.getIdPropByName(Constants.PROP_REPORT_FILE);
			File fileIn=in.getFile();
			long milis = System.currentTimeMillis();
			String fileName = rdn+extension;/*milis+extension;
			while(listNameFilesImported.contains(fileName)){
				//Evitamos que se asigne el mismo milisegundo que le asignamos a otro archivo como nombre ya que puede ocurrir si el archivo es pequeño
				milis++;
				fileName = milis+extension;
			}
			listNameFilesImported.add(fileName);*/
			File fileOut=new File(pathImportFile,fileName);
			dynagent.common.utils.Auxiliar.copyFile(fileIn, fileOut);
			list.add(new FactInstance(idto, ido, prop, fileName, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			
			fileName = rdn+"_orig"+extension;/*(milis+1)+extension;//Ponemos un nombre diferente para original_file
			listNameFilesImported.add(fileName);*/
			fileOut=new File(pathImportFile,fileName);
			dynagent.common.utils.Auxiliar.copyFile(fileIn, fileOut);
			prop = propDAO.getIdPropByName(Constants.PROP_REPORT_ORIGINAL_FILE);//Guardamos el mismo fichero en originalFile pero con otro nombre
			list.add(new FactInstance(idto, ido, prop, fileName, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			
			prop = propDAO.getIdPropByName(Constants.PROP_REPORT_NCOPIES);
			list.add(new FactInstance(idto, ido, prop, null, Constants.IDTO_INT, null, new Double(in.getNCopies()), new Double(in.getNCopies()), null, null, action.NEW));
			
			prop = propDAO.getIdPropByName(Constants.PROP_REPORT_GENERATE_EXCEL);
			list.add(new FactInstance(idto, ido, prop, null, Constants.IDTO_BOOLEAN, null, new Double(in.isGenerateExcel()?1:0), new Double(in.isGenerateExcel()?1:0), null, null, action.NEW));
			
			prop = propDAO.getIdPropByName(Constants.PROP_REPORT_PREVIEW);
			list.add(new FactInstance(idto, ido, prop, null, Constants.IDTO_BOOLEAN, null, new Double(in.isPreView()?1:0), new Double(in.isPreView()?1:0), null, null, action.NEW));
			
			prop = propDAO.getIdPropByName(Constants.PROP_DIRECTIMPRESION);
			list.add(new FactInstance(idto, ido, prop, null, Constants.IDTO_BOOLEAN, null, new Double(in.isDirectImpresion()?1:0), new Double(in.isDirectImpresion()?1:0), null, null, action.NEW));
			
			prop = propDAO.getIdPropByName(Constants.PROP_REPORT_PRINT_DIALOG);
			list.add(new FactInstance(idto, ido, prop, null, Constants.IDTO_BOOLEAN, null, new Double(in.isDisplayPrintDialog()?1:0), new Double(in.isDisplayPrintDialog()?1:0), null, null, action.NEW));

			prop = propDAO.getIdPropByName(Constants.PROP_CHECKPRINTING);
			list.add(new FactInstance(idto, ido, prop, null, Constants.IDTO_BOOLEAN, null, new Double(in.isPrintConfirmation()?1:0), new Double(in.isPrintConfirmation()?1:0), null, null, action.NEW));
			
			if(in.getPostPrint()!=null){
				prop = propDAO.getIdPropByName(Constants.PROP_REPORT_POSTPRINT);
				list.add(new FactInstance(idto, ido, prop, in.getPostPrint(), Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			}
			
			if(in.getPrePrint()!=null){
				prop = propDAO.getIdPropByName(Constants.PROP_REPORT_PREPRINT);
				list.add(new FactInstance(idto, ido, prop, in.getPrePrint(), Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			}
			
			if(in.getPrinterName()!=null){
				prop = propDAO.getIdPropByName(Constants.PROP_REPORT_PRINTER);
				list.add(new FactInstance(idto, ido, prop, in.getPrinterName(), Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			}
			
			if(in.getComments()!=null){
				prop = propDAO.getIdPropByName(Constants.PROP_REPORT_COMMENT);
				list.add(new FactInstance(idto, ido, prop, in.getComments(), Constants.IDTO_MEMO, null, null, null, null, null, action.NEW));
			}
			
			if(in.getFunctionalAreaList()!=null){
				prop = propDAO.getIdPropByName(Constants.PROP_FUNCTIONAL_AREA);
				idtoClass = Constants.IDTO_FUNCTIONAL_AREA;
				for(String functionalArea:in.getFunctionalAreaList()){
					value = Auxiliar.getIdo(idtoClass, functionalArea, fcdb, instanceService.getDataBaseMap());
					list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
				}
			}
			
			if(in.getFormatList()!=null){
				prop = propDAO.getIdPropByName(Constants.PROP_REPORT_FORMAT);
				idtoClass = Constants.IDTO_REPORT_FORMAT;
				for(String format:in.getFormatList()){
					value = Auxiliar.getIdo(idtoClass, format, fcdb, instanceService.getDataBaseMap());
					list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
				}
			}
			
			value=ido;
			if(in.getParamList()!=null){
				for(Param param:in.getParamList()){
					
					Integer valueCls = null;
					Integer idoClass = null;
					value--;
					if(Constants.isDataType(param.getValueCls())){
						valueCls=tdao.getTClaseByName(Constants.CLS_DATA_PARAM).getIDTO();
						prop = propDAO.getIdPropByName(Constants.PROP_PARAM_DATATYPE);
						idtoClass = tdao.getTClaseByName(Constants.CLS_DATATYPE).getIDTO();
						idoClass = Auxiliar.getIdo(idtoClass, Constants.getDatatype(param.getValueCls()), fcdb, instanceService.getDataBaseMap());
					}else{
						valueCls=tdao.getTClaseByName(Constants.CLS_OBJECT_PARAM).getIDTO();
						prop = propDAO.getIdPropByName(Constants.PROP_PARAM_OBJECTTYPE);
						idtoClass = tdao.getTClaseByName(Constants.CLS_CLASS).getIDTO();
						idoClass = Auxiliar.getIdo(idtoClass, Auxiliar.getClassName(param.getValueCls()), fcdb, instanceService.getDataBaseMap());
					}
					list.add(new FactInstance(valueCls, value, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
					
					rdn=Constants.DEFAULT_RDN_CHAR+value+Constants.DEFAULT_RDN_CHAR;
					list.add(new FactInstance(valueCls, value, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
					
					prop = propDAO.getIdPropByName(Constants.PROP_NAME);
					list.add(new FactInstance(valueCls, value, prop, param.getName(), Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
					
					if(param.getCardMin()!=null){
						prop = propDAO.getIdPropByName(Constants.PROP_CARMIN);
						list.add(new FactInstance(valueCls, value, prop, null, Constants.IDTO_INT, null, new Double(param.getCardMin()), new Double(param.getCardMin()), null, null, action.NEW));
					}
					
					if(param.getCardMax()!=null){
						prop = propDAO.getIdPropByName(Constants.PROP_CARMAX);
						list.add(new FactInstance(valueCls, value, prop, null, Constants.IDTO_INT, null, new Double(param.getCardMax()), new Double(param.getCardMax()), null, null, action.NEW));
					}
					
					if(param.getDefaultValue()!=null){
						prop = propDAO.getIdPropByName(Constants.PROP_DEFAULTVALUE);
						list.add(new FactInstance(valueCls, value, prop, param.getDefaultValue(), Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
					}
					
					//Se lo enganchamos al report
					prop = propDAO.getIdPropByName(Constants.PROP_REPORT_PARAM);
					list.add(new FactInstance(idto, ido, prop, String.valueOf(value), valueCls, null, null, null, null, null, action.NEW));
				}
			}
			
			if(in.getSubReportList()!=null){
				Integer valueCls=tdao.getTClaseByName(Constants.CLS_SUBREPORT_INDIVIDUAL).getIDTO();
				for(SubReport subReport:in.getSubReportList()){
					value--;
				
					rdn=subReport.getName();
					list.add(new FactInstance(valueCls, value, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
					
					prop = propDAO.getIdPropByName(Constants.PROP_REPORT_FILE);
					fileIn=subReport.getFile();
					milis = System.currentTimeMillis();
					fileName = rdn+extension;/*milis+extension;
					while(listNameFilesImported.contains(fileName)){
						//Evitamos que se asigne el mismo milisegundo que le asignamos a otro archivo como nombre ya que puede ocurrir si el archivo es pequeño
						milis++;
						fileName = milis+extension;
					}
					listNameFilesImported.add(fileName);*/
					fileOut=new File(pathImportFile,fileName);
					dynagent.common.utils.Auxiliar.copyFile(fileIn, fileOut);
					list.add(new FactInstance(valueCls, value, prop, fileName, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
					
					fileName = rdn+"_orig"+extension;/*(milis+1)+extension;//Ponemos un nombre diferente para original_file
					listNameFilesImported.add(fileName);*/
					fileOut=new File(pathImportFile,fileName);
					dynagent.common.utils.Auxiliar.copyFile(fileIn, fileOut);
					prop = propDAO.getIdPropByName(Constants.PROP_REPORT_ORIGINAL_FILE);//Guardamos el mismo fichero en originalFile pero con otro nombre
					list.add(new FactInstance(valueCls, value, prop, fileName, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
					
					prop = propDAO.getIdPropByName(Constants.PROP_REPORT_GENERATE_EXCEL);
					list.add(new FactInstance(valueCls, value, prop, null, Constants.IDTO_BOOLEAN, null, new Double(subReport.isGenerateExcel()?1:0), new Double(subReport.isGenerateExcel()?1:0), null, null, action.NEW));
					
					if(subReport.getComments()!=null){
						prop = propDAO.getIdPropByName(Constants.PROP_REPORT_COMMENT);
						list.add(new FactInstance(valueCls, value, prop, subReport.getComments(), Constants.IDTO_MEMO, null, null, null, null, null, action.NEW));
					}
					
					// Se lo enganchamos al report
					prop = propDAO.getIdPropByName(Constants.PROP_REPORT_SUBREPORT);
					list.add(new FactInstance(idto, ido, prop, String.valueOf(value), valueCls, null, null, null, null, null, action.NEW));
				}

			}
			
			ido=value;
		}
		
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
	}


	private boolean extractAllReports(String uniqueNameInclude) throws Exception{
		Iterator<Element> itr = getChildrenXml().iterator();
		boolean success=true;
		while(itr.hasNext()){
			Element rElem = itr.next();
			Report rp = new Report();
			try{
				if (rElem.getAttribute(ConstantsXML.NAME)!=null){
					String name=rElem.getAttributeValue(ConstantsXML.NAME);
					Integer idto=Auxiliar.getIdtoClassFromTableClass(name, fcdb);
					if(idto==null || idto<0)
						rp.setName(name);
					else{
						throw new ConfigException("ERROR: El report '"+name+"' no puede llamarse igual que una clase/individuo del modelo. Renombrelo empezando por 'rp@' delante");
					}
				}else{
					throw new ConfigException("ERROR: El atributo '"+ConstantsXML.NAME+"' es obligatorio en el nodo");
				}
				
				if(uniqueNameInclude==null || uniqueNameInclude.equalsIgnoreCase(rp.getName())){
					String path=this.path;
					if (rElem.getAttribute(ConstantsXML.PATH)!=null){
						if(!path.endsWith("/") && !path.endsWith("\\"))
							path+="\\";
						path+=rElem.getAttributeValue(ConstantsXML.PATH);
						File directory=new File(path);
						if(directory.exists()){
							if(!directory.isDirectory()){
								throw new ConfigException("ERROR: El atributo '"+ConstantsXML.PATH+"' tiene que ser un directorio pero tiene el valor "+directory.getAbsolutePath());
							}
						}else {
							//throw new ConfigException("ERROR: El atributo '"+ConstantsXML.PATH+"' hace mención al directorio "+directory.getAbsolutePath()+" que no existe");
							System.err.println("WARNING: El atributo '"+ConstantsXML.PATH+"' hace mención al directorio "+directory.getAbsolutePath()+" que no existe");
							continue;
						}
					}
					
					if (rElem.getAttribute(ConstantsXML.FILE)!=null){
						String fileName=rElem.getAttributeValue(ConstantsXML.FILE);
						String extension = ".jrxml";
						File fileIn=new File(path,fileName.endsWith(".jrxml")?fileName:fileName+extension);
						if(fileIn.exists()){
							rp.setFile(fileIn);
						}else throw new ConfigException("ERROR: El atributo '"+ConstantsXML.FILE+"' hace mención al archivo "+fileIn.getAbsolutePath()+" que no existe");
					}else{
						throw new ConfigException("ERROR: El atributo '"+ConstantsXML.FILE+"' es obligatorio en el nodo");
					}
					
					if(rElem.getAttribute(ConstantsXML.CLASS)!=null){
						String classname=rElem.getAttributeValue(ConstantsXML.CLASS).toString();
						rp.setTargetClassName(classname);
						Integer idtoClass=Auxiliar.getIdtoClass(classname, fcdb);
						if (idtoClass==null){
							//throw new ConfigException("Error: No existe la clase "+classname+" en el modelo");
							System.err.println("WARNING: La clase "+classname+" del atributo '"+ConstantsXML.CLASS+"' no existe. No se importará el report "+rp.getName());
							continue;
						}
					}
					
					if (rElem.getAttribute(ConstantsXML.DIRECT_IMPRESION)!=null){
						if (rElem.getAttributeValue(ConstantsXML.DIRECT_IMPRESION).equals("TRUE"))
							rp.setDirectImpresion(true);
					}
					if (!rp.isDirectImpresion()) {
						if (rElem.getAttribute(ConstantsXML.PREVIEW)!=null){
							if (!rElem.getAttributeValue(ConstantsXML.PREVIEW).equals("FALSE"))
								rp.setPreView(true);
							else rp.setPreView(false);
						} else
							rp.setPreView(true);
					} else
						rp.setPreView(true);
					
					ArrayList<String> formatList = new ArrayList<String>();
					rp.setFormatList(formatList);
					if (rElem.getAttribute(ConstantsXML.FORMAT)!=null){
						String[] formatSpl = rElem.getAttributeValue(ConstantsXML.FORMAT).split(";");
						for (int i=0;i<formatSpl.length;i++) {
							String format = formatSpl[i];
							formatList.add(format);
						}
					} else {
						formatList.add(Constants.PDF);
						formatList.add(Constants.RTF);
					}
					if (rElem.getAttribute(ConstantsXML.SEC_PREPRINT)!=null){
						rp.setPrePrint(rElem.getAttributeValue(ConstantsXML.SEC_PREPRINT));
					}
					if (rElem.getAttribute(ConstantsXML.SEC_POSTPRINT)!=null){
						rp.setPostPrint(rElem.getAttributeValue(ConstantsXML.SEC_POSTPRINT));
					}
					if (rElem.getAttribute(ConstantsXML.PRINTER)!=null){
						rp.setPrinterName(rElem.getAttributeValue(ConstantsXML.PRINTER));
					}
					if (rElem.getAttribute(ConstantsXML.N_COPIES)!=null){
						rp.setNCopies(Integer.parseInt(rElem.getAttributeValue(ConstantsXML.N_COPIES)));
					}
					
					if (rElem.getAttribute(ConstantsXML.PRINT_CONFIRMATION)!=null){
						if (rElem.getAttributeValue(ConstantsXML.PRINT_CONFIRMATION).equals("TRUE"))
							rp.setPrintConfirmation(true);
					}
					if (rElem.getAttribute(ConstantsXML.GENERATE_EXCEL)!=null){
						if (rElem.getAttributeValue(ConstantsXML.GENERATE_EXCEL).equals("TRUE")) {
							formatList.add(Constants.EXCEL);
							rp.setGenerateExcel(true);
						}
					}
					if (rElem.getAttribute(ConstantsXML.DISPLAY_PRINT_DIALOG)!=null){
						if (rElem.getAttributeValue(ConstantsXML.DISPLAY_PRINT_DIALOG).equals("TRUE"))
							rp.setDisplayPrintDialog(true);
					}
					ArrayList<String> areaFuncList=new ArrayList<String>();
					if (rElem.getAttribute(ConstantsXML.AREA_FUNC_ATRB)!=null){
						String[] areaFuncs=rElem.getAttributeValue(ConstantsXML.AREA_FUNC_ATRB).split(";");
						for(int i=0;i<areaFuncs.length;i++){
							String areaFuncName=areaFuncs[i];
							if(Auxiliar.getIdo(Constants.IDTO_FUNCTIONAL_AREA,areaFuncName,fcdb,instanceService.getDataBaseMap())!=null)
								areaFuncList.add(areaFuncName);
							else{
								throw new ConfigException("ERROR: El userRol '"+areaFuncName+"' no está en la configuración ni existe en la base de datos");
							}
						}
					}
					rp.setFunctionalAreaList(areaFuncList);
					
					Element cElem=rElem.getChild(ConstantsXML.COMMENTS);
					if(cElem!=null){
						rp.setComments(cElem.getTextNormalize());
					}
					
					Iterator itrSubReport=rElem.getChildren(ConstantsXML.SUBREPORT).iterator();
					if (itrSubReport.hasNext()){
						ArrayList<SubReport> list=new ArrayList<SubReport>();
						do{
							SubReport subReport=new SubReport(); 
							Element sbElem = (Element)itrSubReport.next();
							if (sbElem.getAttribute(ConstantsXML.NAME)!=null){
								String name=sbElem.getAttributeValue(ConstantsXML.NAME);
								Integer idto=Auxiliar.getIdtoClassFromTableClass(name, fcdb);
								if(idto==null || idto<0)
									subReport.setName(name);
								else{
									throw new ConfigException("ERROR: El subreport '"+name+"' no puede llamarse igual que una clase/individuo del modelo. Renombrelo empezando por 'rp@' delante");
								}
							}else{
								throw new ConfigException("ERROR: El atributo '"+ConstantsXML.NAME+"' es obligatorio en el nodo "+ConstantsXML.REPORT);
							}
							
							if (sbElem.getAttribute(ConstantsXML.FILE)!=null){
								String fileName=sbElem.getAttributeValue(ConstantsXML.FILE);
								String extension = ".jrxml";
								File fileIn=new File(path,fileName.endsWith(".jrxml")?fileName:fileName+extension);
								if(fileIn.exists()){
									subReport.setFile(fileIn);
								}else throw new ConfigException("ERROR: El atributo '"+ConstantsXML.FILE+"' hace mención al archivo "+fileIn.getAbsolutePath()+" que no existe");
							}else{
								throw new ConfigException("ERROR: El atributo '"+ConstantsXML.FILE+"' es obligatorio en el nodo");
							}
							
							if (sbElem.getAttribute(ConstantsXML.GENERATE_EXCEL)!=null){
								if (sbElem.getAttributeValue(ConstantsXML.GENERATE_EXCEL).equals("TRUE")){
									formatList.add(Constants.EXCEL);
									subReport.setGenerateExcel(true);
								}
							}
							
							Element csElem=sbElem.getChild(ConstantsXML.COMMENTS);
							if(csElem!=null){
								subReport.setComments(csElem.getTextNormalize());
							}
							
							list.add(subReport);
						}while(itrSubReport.hasNext());
						rp.setSubReportList(list);
					}
					
					boolean insert=true;
					Iterator itrParam=rElem.getChildren(ConstantsXML.PARAM).iterator();
					if (itrParam.hasNext()){
						ArrayList<Param> list=new ArrayList<Param>();
						do{
							Param param=new Param(); 
							Element pElem = (Element)itrParam.next();
							if (pElem.getAttribute(ConstantsXML.NAME)!=null){
								String name=pElem.getAttributeValue(ConstantsXML.NAME);
								param.setName(name);
							}else{
								throw new ConfigException("ERROR: El atributo '"+ConstantsXML.NAME+"' es obligatorio en el nodo "+ConstantsXML.PARAM);
							}
							
							if (pElem.getAttribute(ConstantsXML.VALUECLS)!=null){
								String valueClsName=pElem.getAttributeValue(ConstantsXML.VALUECLS).toString();
								Integer valueClsInt=Constants.getIdDatatype(valueClsName);
								if (valueClsInt==null){
									valueClsInt=Auxiliar.getIdtoClass(valueClsName, fcdb);
									if (valueClsInt==null){
										//throw new ConfigException("Error: La clase "+valueClsName+" del atributo '"+ConstantsXML.VALUECLS+"' no existe");
										System.err.println("WARNING: La clase "+valueClsName+" del atributo '"+ConstantsXML.VALUECLS+"' no existe. No se importará el report "+rp.getName());
										insert=false;
										continue;
									}
								}
								param.setValueCls(valueClsInt);
							}else{
								throw new ConfigException("Error: El atributo '"+ConstantsXML.VALUECLS+"' es obligatorio en el nodo "+ConstantsXML.PARAM);
							}
							
							if (pElem.getAttribute(ConstantsXML.CARMIN)!=null){
								param.setCardMin(Integer.parseInt(pElem.getAttributeValue(ConstantsXML.CARMIN)));
							}
							
							if (pElem.getAttribute(ConstantsXML.CARMAX)!=null){
								param.setCardMax(Integer.parseInt(pElem.getAttributeValue(ConstantsXML.CARMAX)));
							}
							
							if (pElem.getAttribute(ConstantsXML.DEFVAL)!=null){
								param.setDefaultValue(pElem.getAttributeValue(ConstantsXML.DEFVAL));
							}
							
							list.add(param);
						}while(itrParam.hasNext() && insert);
						rp.setParamList(list);
					}
					
					if(insert){
						listreport.add(rp);
						configImport.addReportName(rp.getName());
					}
				}
			}catch(ConfigException ex){
				System.err.println(ex.getMessage());
				success=false;
			}catch(Exception ex){
				throw ex;
			}
		}
		
		if(uniqueNameInclude!=null && listreport.isEmpty() && success){
			System.err.println("ERROR: El report '"+uniqueNameInclude+"' no existe en el archivo de configuracion");
			success=false;
		}
		return success;
	}
	
	/*public String getPathReports() throws Exception{
		String path=null;
		if (xml.getAttribute(ConstantsXML.PATH)!=null){
			path=xml.getAttributeValue(ConstantsXML.PATH).toString();
		}else{
			System.err.println("Error: El atributo '"+ConstantsXML.PATH+"' es obligatorio en el XML");
			throw new Exception();	
		}
		
		return path;
	}*/
	
}
