package dynagent.tools.importers.configxml;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;
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
import dynagent.common.knowledge.KnowledgeAdapter;
import dynagent.common.knowledge.action;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.DeletableObject;
import dynagent.server.services.InstanceService;


public class importAccess extends ObjectConfig{
	
	private LinkedList<Access> listaccess;
	
	private HashMap<Access,String> mapAccessUtask;
	private HashMap<Access,String> mapAccessAction;
	private HashMap<Access,String> mapAccessReport;
	
	public importAccess(Element accessXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(accessXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listaccess=new LinkedList<Access>();
		mapAccessUtask=new HashMap<Access, String>();
		mapAccessAction=new HashMap<Access, String>();
		mapAccessReport=new HashMap<Access, String>();
	}
	
	@Override
	public boolean configData() throws Exception{
		return configData(false);
	}
	@Override
	public void importData() throws Exception {
		insertAccess();
	}

	public boolean configData(boolean onlyReports) throws Exception{
		if(!onlyReports)//Si fuera solo reports no borramos los accesos porque borrariamos los de la aplicación
			deleteAccess();
		//buildGenericAccess();
		return extractAllAccess(onlyReports);
	}
	
	private void buildGenericAccess(){
		listaccess.add(KnowledgeAdapter.buildAllAccess(null, 0));
	}
	
	public void deleteAccessReports(String uniqueReportInclude) throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException, InterruptedException, NoSuchColumnException {
		TClaseDAO tdao = new TClaseDAO();
		int idtoClass = tdao.getTClaseByName(Constants.CLS_ACCESS_UTASK).getIDTO();
		
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "select perm." + gSQL.getCharacterBegin() + "tableId" + gSQL.getCharacterEnd() + " as idperm " +
				"from informe as inf " + 
				"inner join " + gSQL.getCharacterBegin() + "permiso_ámbito#informe" + gSQL.getCharacterEnd() + " as perminf on (inf." + gSQL.getCharacterBegin() + "tableId" + gSQL.getCharacterEnd() + "=perminf." + gSQL.getCharacterBegin() + "informeId" + gSQL.getCharacterEnd() + ") " + 
				"inner join " + gSQL.getCharacterBegin() + "permiso_ámbito" + gSQL.getCharacterEnd() + " as perm on (perminf." + gSQL.getCharacterBegin() + "permiso_ámbitoId" + gSQL.getCharacterEnd() + "=perm." + gSQL.getCharacterBegin() + "tableId" + gSQL.getCharacterEnd() + ")";
		if (uniqueReportInclude!=null) {
			sql += " where inf.rdn='" + uniqueReportInclude + "'";
		}
		
		List<List<String>> listQ = DBQueries.executeQuery(fcdb, sql);
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();
		for (List<String> list2 : listQ) {
			if (list2.size()>0) {
				int ido = Integer.parseInt(list2.get(0) + StringUtils.leftPad(String.valueOf(idtoClass), 3, '0'));
				list.add(new FactInstance(idtoClass, ido, Constants.IdPROP_OBJECTDELETED, null, null, null, null, null, null, null, action.DEL_OBJECT));
			}
		}
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
	}
	
	private void deleteAccess() throws SQLException, NamingException, NoSuchColumnException{
		for(String className:Access.ACCESS_TYPES){
			DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(className, fcdb), instanceService.getDataBaseMap(), fcdb);
		}
	}
	
	private void insertAccess() throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException, InterruptedException, NoSuchColumnException {
		Iterator<Access> itac=this.listaccess.iterator();
		TClaseDAO tdao = new TClaseDAO();
		PropertiesDAO propDAO = new PropertiesDAO();
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();
		int countido=0;
		while(itac.hasNext()){
			Access in= itac.next();
			//System.out.println("---> "+ac.toString());
			Integer idto = tdao.getTClaseByName(in.getAccessType()).getIDTO();
			countido--;
			int ido=QueryConstants.getIdo(countido, idto);
			String rdn=String.valueOf("&"+ido+"&");
			list.add(new FactInstance(idto, ido, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			
			int idtoClass = tdao.getTClaseByName(Constants.CLS_CLASS).getIDTO();
			if(in.getIDTO()!=null){
				int prop = propDAO.getIdPropByName(Constants.PROP_DOMINIO);
				int value = QueryConstants.getIdo(tdao.getTClaseById(in.getIDTO()).getTableId(), idtoClass);
				list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
			}
			
			int idoClass;
			Properties property;
			
			if(in.getTASKNAMES()!=null){
				int prop = propDAO.getIdPropByName(Constants.PROP_SCOPE);
				idtoClass= in.getTASK();
				Iterator<String> itr=in.getTASKNAMES().iterator();
				while(itr.hasNext()){
					String utaskName=itr.next();
					idoClass=Auxiliar.getIdo(idtoClass, utaskName, fcdb, instanceService.getDataBaseMap());
					list.add(new FactInstance(idto, ido, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
				}
			}
			
			if(in.getPROP()!=null){
				for(Integer idProp:in.getPROP()){
					property=propDAO.getPropertyByID(idProp);
					int prop = propDAO.getIdPropByName(Constants.PROP_PROPERTY);
					if(new Category(property.getCAT()).isDataProperty()){
						idtoClass = Constants.IDTO_DATA_PROPERTY;
					}else{
						idtoClass = Constants.IDTO_OBJECT_PROPERTY;
					}
					idoClass = QueryConstants.getIdo(property.getTableId(),idtoClass);
					
					list.add(new FactInstance(idto, ido, prop, String.valueOf(idoClass), idtoClass, null, null, null, null, null, action.NEW));
				}
			}
			
			if(in.getFUNCTIONALAREA()!=null){
				idtoClass = tdao.getTClaseByName(Constants.CLS_FUNCTIONAL_AREA).getIDTO();
				int prop = propDAO.getIdPropByName(Constants.PROP_FUNCTIONAL_AREA);
				Iterator<Integer> itr=in.getFUNCTIONALAREA().iterator();
				while(itr.hasNext()){
					Integer fa=itr.next();
					int value = fa;
					list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
				}
			}
			
			if(in.getUSER()!=null){
				idtoClass = tdao.getTClaseByName(Constants.CLS_USER).getIDTO();
				int prop = propDAO.getIdPropByName(Constants.PROP_USUARIO);
				Iterator<String> itr=in.getUSER().iterator();
				while(itr.hasNext()){
					String user=itr.next();
					int value = Auxiliar.getIdo(idtoClass, user, fcdb, instanceService.getDataBaseMap());
					list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
				}
			}
			
			if(in.getUSERROL()!=null){
				idtoClass = tdao.getTClaseByName(Constants.CLS_USERROL).getIDTO();
				int prop = propDAO.getIdPropByName(Constants.PROP_USERROL);
				Iterator<String> itr=in.getUSERROL().iterator();
				while(itr.hasNext()){
					String userRol=itr.next();
					int value = Auxiliar.getIdo(idtoClass, userRol, fcdb, instanceService.getDataBaseMap());
					list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
				}
			}
			
			idtoClass = tdao.getTClaseByName(Constants.CLS_ACCESS_TYPE).getIDTO();
			int prop = propDAO.getIdPropByName(Constants.PROP_ACCESS);
			Iterator<String> itr=in.getACCESSTYPENAME().iterator();
			while(itr.hasNext()){
				String accesstypename=itr.next();
				int value = Auxiliar.getIdo(idtoClass, accesstypename, fcdb, instanceService.getDataBaseMap());
				list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
			}
			
			idtoClass = tdao.getTClaseByName(Constants.CLS_PRIORITY).getIDTO();
			prop = propDAO.getIdPropByName(Constants.PROP_PRIORITY);
			int value = Auxiliar.getIdoOfPriority(in.getPRIORITY(), fcdb, instanceService.getDataBaseMap());
			list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));

			idtoClass=tdao.getTClaseByName(Constants.CLS_ACCESS_ACTION).getIDTO();
			prop = propDAO.getIdPropByName(Constants.PROP_ACCESS_ACTION);
			String accessAction=in.getDENNIED()==1?"Denegar":"Asignar";
			value = Auxiliar.getIdo(idtoClass, accessAction, fcdb, instanceService.getDataBaseMap());
			list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
			//System.out.println("---> OK");
		}
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
	}
	private boolean extractAllAccess(boolean onlyReports) throws Exception{
		boolean valclsDP=false;
		Iterator itaccesses = getChildrenXml().iterator();
		boolean success=true;
		while(itaccesses.hasNext()){
			Element accessesElement = (Element)itaccesses.next();
			Access a = new Access();
			try{
				if(accessesElement.getAttributeValue(ConstantsXML.DENNIED) != null){
					String den=accessesElement.getAttributeValue(ConstantsXML.DENNIED);
					if (den.equals("SI")){
						a.setDENNIED(1);
					}else if (den.equals("NO")){
						a.setDENNIED(0);
					}else{
						throw new ConfigException("Error: el valor "+den+" no es valido en el atributo "+ConstantsXML.DENNIED);
					}
				}else{
					throw new ConfigException("Error: Se debe de definir el atributo '"+ConstantsXML.DENNIED+"' obligatoriamente");
				}
				
				if((accessesElement.getAttributeValue(ConstantsXML.REPORT_ATRB) != null && accessesElement.getAttributeValue(ConstantsXML.UTASK_ATRB) != null)
					|| (accessesElement.getAttributeValue(ConstantsXML.REPORT_ATRB) != null && accessesElement.getAttributeValue(ConstantsXML.ACTION_ATRB) != null)
					|| (accessesElement.getAttributeValue(ConstantsXML.UTASK_ATRB) != null && accessesElement.getAttributeValue(ConstantsXML.ACTION_ATRB) != null)){
					
					throw new ConfigException("Error: Sólo esta permitido definir en el mismo nodo uno de los siguientes atributos: "+ConstantsXML.UTASK_ATRB+", "+ConstantsXML.ACTION_ATRB+", "+ConstantsXML.REPORT_ATRB);
				}
					
				
				boolean include=false;
				if(accessesElement.getAttributeValue(ConstantsXML.REPORT_ATRB) != null){
					String utsname=accessesElement.getAttributeValue(ConstantsXML.REPORT_ATRB);
					String[] buf=utsname.split(";");
					a.setTASK(Auxiliar.getIdtoClass(Constants.CLS_REPORT_INDIVIDUAL, fcdb));
					ArrayList<String> utask=new ArrayList<String>();
					for(int i=0;i<buf.length;i++){
						String utname=buf[i];
						// Miramos que existe esa userTask en el xml ya que en base de datos aun no existe
						if(configImport.containsReportName(utname)){
							utask.add(utname);
						}else{
							//throw new ConfigException("Error: la UserTask "+utname+" no existe en el modelo ni esta definida en el xml");
							System.err.println("WARNING: el report "+utname+" no existe en el xml");
							continue;
						}
					}
					if(utask.isEmpty()){
						continue;
						//throw new ConfigException("Error: Definición de usertasks que no existen en la base de datos");
					}
					a.setTASKNAMES(utask);
				}else if(!onlyReports){
					include=true;
				}
				
				if(include){
					if(accessesElement.getAttributeValue(ConstantsXML.AREA_FUNC_ATRB) != null){
						String afname=accessesElement.getAttributeValue(ConstantsXML.AREA_FUNC_ATRB);
						String[] buf=afname.split(";");
						ArrayList<Integer> functionalArea=new ArrayList<Integer>();
						for(int i=0;i<buf.length;i++){
							Integer faIdo=Auxiliar.getIdo(Constants.IDTO_FUNCTIONAL_AREA,buf[i],fcdb,instanceService.getDataBaseMap());
							if(faIdo!=null)
								functionalArea.add(faIdo);
							else{
								//throw new ConfigException("Error: El area funcional '"+buf[i]+"' no existe en la base de datos");
								System.err.println("WARNING: El area funcional '"+buf[i]+"' no existe ni en la base de datos");
								continue;
							}
						}
						if(functionalArea.isEmpty()){
							throw new ConfigException("Error: Definición de areas funcionales que no existen en la base de datos");
						}
						a.setFUNCTIONALAREA(functionalArea);
					}
					
					if(accessesElement.getAttributeValue(ConstantsXML.UTASK_ATRB) != null){
						String utsname=accessesElement.getAttributeValue(ConstantsXML.UTASK_ATRB);
						String[] buf=utsname.split(";");
						a.setTASK(Auxiliar.getIdtoClass(Constants.CLS_MENU, fcdb));
						ArrayList<String> utask=new ArrayList<String>();
						for(int i=0;i<buf.length;i++){
							String utname=buf[i];
							// Miramos que existe esa userTask en el xml ya que en base de datos aun no existe
							if(configImport.containsUserTask(utname)){
								utask.add(utname);
							}else{
								//throw new ConfigException("Error: la UserTask "+utname+" no existe en el modelo ni esta definida en el xml");
								System.err.println("WARNING: la UserTask "+utname+" no existe en el modelo ni esta definida en el xml");
								continue;
							}
						}
						if(utask.isEmpty()){
							continue;
							//throw new ConfigException("Error: Definición de usertasks que no existen en la base de datos");
						}
						a.setTASKNAMES(utask);
					}
					
					if(accessesElement.getAttributeValue(ConstantsXML.ACTION_ATRB) != null){
						String utsname=accessesElement.getAttributeValue(ConstantsXML.ACTION_ATRB);
						String[] buf=utsname.split(";");
						a.setTASK(Auxiliar.getIdtoClass(Constants.CLS_ACTION_INDIVIDUAL, fcdb));
						ArrayList<String> utask=new ArrayList<String>();
						for(int i=0;i<buf.length;i++){
							String utname=buf[i];
							// Miramos que existe esa userTask en el xml ya que en base de datos aun no existe
							Integer idtout=Auxiliar.getIdtoClass(utname, fcdb);
							if (idtout!=null){
								utask.add(utname);
							}else{
								//throw new ConfigException("Error: la UserTask "+utname+" no existe en el modelo ni esta definida en el xml");
								System.err.println("WARNING: la Acción "+utname+" no existe en el modelo ni esta definida en el xml");
								continue;
							}
						}
						if(utask.isEmpty()){
							continue;
							//throw new ConfigException("Error: Definición de usertasks que no existen en la base de datos");
						}
						a.setTASKNAMES(utask);
					}
					if(accessesElement.getAttributeValue(ConstantsXML.CLASS) != null){
						String nameClass=accessesElement.getAttributeValue(ConstantsXML.CLASS);
						Integer idtoclass=Auxiliar.getIdtoClass(nameClass, fcdb);
						if (idtoclass!=null){
							a.setIDTO(idtoclass);
						}else{
							//throw new ConfigException("Error: La clase "+nameClass+" no existe en el modelo");
							continue;
						}
					}
							
					if(accessesElement.getAttributeValue(ConstantsXML.USERROL) != null){
						String userrol=accessesElement.getAttributeValue(ConstantsXML.USERROL);
						String[] buf=userrol.split(";");
						ArrayList<String> userRoles=new ArrayList<String>();
						for(int i=0;i<buf.length;i++){
							if(configImport.containsUserRolName(buf[i]) || Auxiliar.getIdo(Constants.IDTO_USERROL,buf[i],fcdb,instanceService.getDataBaseMap())!=null)
								userRoles.add(buf[i]);
							else{
								//throw new ConfigException("Error: El userRol '"+userrol+"' no existe ni en el xml ni en la base de datos");
								System.err.println("WARNING: El userRol '"+buf[i]+"' no existe ni en el xml ni en la base de datos");
								continue;
							}
						}
						if(userRoles.isEmpty())
							continue;
						
						a.setUSERROL(userRoles);
						
//						if(configImport.containsUserRolName(userrol) || Auxiliar.getIdoUserRol(userrol)!=null)
//							a.setUSERROL(userrol);
//						else{
//							//throw new ConfigException("Error: El userRol '"+userrol+"' no existe ni en el xml ni en la base de datos");
//							System.err.println("WARNING: El userRol '"+userrol+"' no existe ni en el xml ni en la base de datos");
//							continue;
//						}
					}
						
					if(accessesElement.getAttributeValue(ConstantsXML.USER) != null){
						String nameUser=accessesElement.getAttributeValue(ConstantsXML.USER);
						String[] buf=nameUser.split(";");
						ArrayList<String> users=new ArrayList<String>();
						for(int i=0;i<buf.length;i++){
							if(configImport.containsUserName(buf[i]) || Auxiliar.getIdo(Constants.IDTO_USER, buf[i], fcdb, instanceService.getDataBaseMap())!=null){
								users.add(buf[i]);
							}else{
								// throw new ConfigException("Error: El usuario '"+nameUser+"' no existe ni en el xml ni en la base de datos");
								System.err.println("WARNING: El usuario '"+nameUser+"' no existe ni en el xml ni en la base de datos");
								continue;
								
							}
						}
						if(users.isEmpty())
							continue;
						
						a.setUSER(users);
						
						
//						if(configImport.containsUserName(nameUser) || Auxiliar.getIdoUser(nameUser)!=null)
//							a.setUSER(nameUser);
//						else{
//							//throw new ConfigException("Error: El usuario '"+nameUser+"' no existe ni en el xml ni en la base de datos");
//							System.err.println("WARNING: El usuario '"+nameUser+"' no existe ni en el xml ni en la base de datos");
//							continue;
//						}
					}
						
//					if(accessesElement.getAttributeValue(ConstantsXML.IDO) != null){
//						String idoName= accessesElement.getAttributeValue(ConstantsXML.IDO);
//						//TODO Si hay dos individuos con el mismo rdn habria que crear dos objetos access
//						ArrayList<Integer> idos=Auxiliar.getIdos(idoName, fcdb);
//						if (!idos.isEmpty()){
//							a.setIDO(idos.get(0));
//							if(idos.size()>1){
//								System.err.println("Warning: Hay mas de un individuo '"+idoName+"' en base de datos. Se esta utilizando el primero");
//							}
//						}else{
//							throw new ConfigException("Error: El individuo "+idoName+" no existe");
//						}
//					}
						
					if(accessesElement.getAttributeValue(ConstantsXML.PROP) != null||accessesElement.getChild("PROP")!=null){
						List<Element> propList=null;
						if(accessesElement.getAttributeValue(ConstantsXML.PROP) != null){
							propList=new LinkedList<Element>();
							Element unicProp=new Element("PROP");
							propList.add(unicProp);
							unicProp.setAttribute("NAME", accessesElement.getAttributeValue(ConstantsXML.PROP));
						}else{
							propList=accessesElement.getChildren("PROP");
						}
						ArrayList idPropArr=new ArrayList();
						for(int i=0;i<propList.size();i++){
							Element propE=propList.get(i);
							String propName=propE.getAttributeValue("NAME");
							Integer idtoClass=a.getIDTO();
							Integer idProp=Auxiliar.getIdProp(propName, idtoClass, fcdb );
							if(idProp==null){
								System.err.println("ERROR access multiple "+propName+" "+idtoClass);
								continue;
							}
							idPropArr.add(idProp);
						}
						if(idPropArr!=null && idPropArr.size()>0){
							Integer[] ida=new Integer[idPropArr.size()];
							idPropArr.toArray(ida);
							a.setPROP(ida);							
						}else{
							System.err.println("Error nodo acceso sin propiedades reconocidas "+jdomParser.returnXML(accessesElement));
							continue;
						}
					}
//					if(accessesElement.getAttributeValue(ConstantsXML.VALUECLS) != null){
//						String valueClsName=accessesElement.getAttributeValue(ConstantsXML.VALUECLS);
//						Integer valueClsInt=Constants.getIdDatatype(valueClsName);
//						if (valueClsInt!=null){
//							valclsDP=true;
//							a.setVALUECLS(valueClsInt);
//						}else{
//							valueClsInt=Auxiliar.getIdtoClass(valueClsName, fcdb);
//							if (valueClsInt!=null){
//								a.setVALUECLS(valueClsInt);
//							}else{
//								//throw new ConfigException("Error: La clase "+valueClsName+" del ValueCls no existe");
//								continue;
//							}
//						}
//										
//					}
						
//					if(accessesElement.getAttributeValue(ConstantsXML.VALUE_ATRIB) != null){
//						String value=accessesElement.getAttributeValue(ConstantsXML.VALUE_ATRIB);
//						if (valclsDP){
//							a.setVALUE(value);
//						}else{
//							Integer idoVC=Auxiliar.getIdo(a.getVALUECLS(),value, fcdb, instanceService.getDataBaseMap());
//							if (idoVC!=null){
//								a.setVALUE(idoVC.toString());
//							}else{
//								throw new ConfigException("Error: El individuo "+value+" no pertence a la clase "+a.getVALUECLS());
//							}
//						}
//					}
					
					
					if(accessesElement.getAttributeValue(ConstantsXML.PRIORITY) != null){
						a.setPRIORITY(Integer.parseInt(accessesElement.getAttributeValue(ConstantsXML.PRIORITY)));
						if(a.getPRIORITY()>Constants.MAX_ACCESS_PRIORITY){
							System.err.println("WARNING: El acceso "+a+" ha sido definido con prioridad="+a.getPRIORITY()+". Sera importado con prioridad "+Constants.MAX_ACCESS_PRIORITY+" ya que es la máxima permitida");
							a.setPRIORITY(Constants.MAX_ACCESS_PRIORITY);
						}
					}else{
						//Por defecto prioridad 0
						a.setPRIORITY(0);
					}
					
					if(a.getAccessType().equals(Constants.CLS_ACCESS_UTASK) && a.getPRIORITY()!=6){
						System.err.println("WARNING: El acceso "+a+" ha sido definido con prioridad="+a.getPRIORITY()+". Sera importado con prioridad "+Constants.MAX_ACCESS_PRIORITY+" ya que ese tipo de permisos solo admite la máxima prioridad");
						a.setPRIORITY(6);
					}
						
						
					if(accessesElement.getAttributeValue(ConstantsXML.ACCESSTYPE) != null){
						String accesstype=accessesElement.getAttributeValue(ConstantsXML.ACCESSTYPE);
						String[] buf=accesstype.split(";");
						ArrayList<String> accessTypes=new ArrayList<String>();
						for(int i=0;i<buf.length;i++){
							String accessTypeName=ConstantsXML.getAccessTypeName(buf[i]);
							if(accessTypeName!=null){
								if(a.getPROP()!=null && a.getPROP().length>0 && !accessTypeName.equals(Constants.ACCESS_VIEW_NAME) && !accessTypeName.equals(Constants.ACCESS_SET_NAME)){
									throw new ConfigException("Error: el acceso "+accessTypeName+" no es válido para las propiedades. Solo se permite VIEW o SET. Acceso definido: "+a);
								}
								
								Integer type=Constants.getAccessType(accessTypeName);
								if (type!=null){
									accessTypes.add(accessTypeName);
								}else{
									throw new ConfigException("Error: el tipo "+buf[i]+" no es un tipo valido en la aplicación");
								}
								
							}else{
								System.err.println("WARNING: El tipo de acceso "+buf[i]+" no está soportado en el xml");
							}
						}
						if(accessTypes.isEmpty()){
							System.err.println("WARNING: El acceso "+a+" no es importado ya que ninguno de sus tipos de acceso está soportado");
							continue;
						}
						
						a.setACCESSTYPENAME(accessTypes);
						
						listaccess.add(a);
//						String[] buf=accesstype.split(";");
//						//System.out.println("Asigna valor int:"+value);
//						int i=0;
//						do{
//							Access aFull=null;
//							if(i>0){
//								aFull=a.clone();
//								//Si hacemos un clone necesitamos que este nuevo objeto siga relacionado con su userTask si esta existe
//								if(accessUtask.get(a)!=null)
//									accessUtask.put(aFull, accessUtask.get(a));
//							}else aFull=a;
//							
//							Integer type=Constants.getAccessType(buf[i]);
//							if (type!=null){
//								aFull.setACCESSTYPENAME(buf[i]);
//							}else{
//								throw new ConfigException("Error: el tipo "+accesstype+" no es un tipo valido");
//							}
//							i++;
//							
//							listaccess.add(aFull);
//							
//						}while(i<buf.length);
					}else{
						throw new ConfigException("Error: Se debe de definir el atributo '"+ConstantsXML.ACCESSTYPE+"' obligatoriamente");
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
}
