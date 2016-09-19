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
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.UTask;
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
import dynagent.common.utils.jdomParser;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.DeletableObject;
import dynagent.server.services.InstanceService;

public class importUtask extends ObjectConfig{
	
	protected LinkedList<UTask> listUtask;
//	private HashMap<String,Integer> hRoles;
	
	public importUtask(Element utasksXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(utasksXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listUtask=new LinkedList<UTask>();
	}

//	public void setRoles(HashMap<String,Integer> hRoles) {
//		this.hRoles = hRoles;
//	}
	@Override
	public boolean configData() throws Exception {
		deleteUTasks();
		return extractAllUtask();
	}
	@Override
	public void importData() throws Exception {
		insertUTasks();
	}
	
	private void deleteUTasks() throws SQLException, NamingException, NoSuchColumnException{
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_MENU, fcdb), instanceService.getDataBaseMap(), fcdb);
	}

	protected boolean extractAllUtask() throws Exception{
		Iterator itut = getChildrenXml().iterator();
		boolean success=true;
		while(itut.hasNext()){
			Element utElem = (Element)itut.next();
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
					throw new ConfigException("Error: El atributo '"+ConstantsXML.AREA_FUNC_ATRB+"' es obligatorio en el nodo, name:"+utElem.getAttribute(ConstantsXML.NAME));
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
				
				if (utElem.getAttribute(ConstantsXML.LISTENER)!=null){
					boolean listener=new Boolean(utElem.getAttributeValue(ConstantsXML.LISTENER).toString());
					ut.setListener(listener);
				}
				
				if (utElem.getAttribute(ConstantsXML.GLOBAL)!=null){
					boolean global=new Boolean(utElem.getAttributeValue(ConstantsXML.GLOBAL).toString());
					ut.setGlobal(global);
				}
				
				Element tg_class=utElem.getChild(ConstantsXML.TG_CLASS);
				if (tg_class!=null){
					if (tg_class.getAttribute(ConstantsXML.CLASS)!=null){
						String tgcname=tg_class.getAttributeValue(ConstantsXML.CLASS).toString();
						Integer idtotgc=Auxiliar.getIdtoClass(tgcname, fcdb);
						if (idtotgc!=null){
							ut.setTargetClass(idtotgc);
							ut.setTargetClassName(tgcname);
						}else{
							//throw new ConfigException("Error: La clase '"+tgcname+"' no existe en el modelo");
							continue;//Avanzamos a la siguiente iteracion ya que no nos interesa insertar esta utask
						}
					}else{
						throw new ConfigException("Error: El atributo '"+ConstantsXML.CLASS+"' es obligatorio en la TG_CLASS : utaskName="+utElem.getAttributeValue(ConstantsXML.NAME).toString());
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
					throw new ConfigException("Error: No se ha definido la TargetClass para la UTask '"+ut.getUtaskName()+"'");
				}
				
				
				Element s_class=utElem.getChild(ConstantsXML.S_CLASS);
				if (s_class!=null){
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
					String[] uRolesSpl = uRoles.split(";");
					for (int i=0;i<uRolesSpl.length;i++) {
						String uRol = uRolesSpl[i];
						//TODO comprobar si es correcto
						aURoles.add(uRol);
					}
					ut.setAURoles(aURoles);
				}
				
				Element params=utElem.getChild(ConstantsXML.PARAMS);
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
					
					if (params.getAttribute(ConstantsXML.CARMIN)!=null){
						String cmin=params.getAttributeValue(ConstantsXML.CARMIN).toString();
						ut.setCminP(Integer.valueOf(cmin));
					}/*else{
						System.err.println("Error: El atributo '"+ConstantsXML.CARMIN+"' es obligatorio en la S_CLASS");
						throw new ConfigXMLException();
					}*/
					
					if (params.getAttribute(ConstantsXML.CARMAX)!=null){
						String cmax=params.getAttributeValue(ConstantsXML.CARMAX).toString();
						ut.setCmaxP(Integer.valueOf(cmax));
					}/*else{
						System.err.println("Error: El atributo '"+ConstantsXML.CARMAX+"' es obligatorio en la S_CLASS");
						throw new ConfigXMLException();
					}*/
				
				
				}
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
	private void insertUTasks() throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException, InterruptedException, NoSuchColumnException {
		//InstanceDAO iDAO = new InstanceDAO();
		//iDAO.open();
		Iterator<UTask> itut=this.listUtask.iterator();
		TClaseDAO tdao = new TClaseDAO();
		PropertiesDAO propDAO = new PropertiesDAO();
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();
		int countido=0;
		while(itut.hasNext()){
			UTask in=itut.next();
			//System.out.println("---> "+ut.toString());
			
			Integer idto = tdao.getTClaseByName(in.getUTaskType()).getIDTO();
			countido--;
			int ido=QueryConstants.getIdo(countido, idto);
			String rdn=in.getUtaskName();
			list.add(new FactInstance(idto, ido, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
			
			int idtoClass = tdao.getTClaseByName(Constants.CLS_CLASS).getIDTO();
			if(in.getTargetClassName()!=null){
				int prop = propDAO.getIdPropByName(Constants.PROP_DOMINIO);
				int value = QueryConstants.getIdo(tdao.getTClaseByName(in.getTargetClassName()).getTableId(), idtoClass);
				list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
			}
			
//			if(in.getSourceClassName()!=null){
//				int prop = propDAO.getIdPropByName(Constants.PROP_DOMINIO);
//				int value = QueryConstants.getIdo(tdao.getTClaseByName(in.getSourceClassName()).getTableId(), idtoClass);
//				list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
//			}
			
			idtoClass = tdao.getTClaseByName(Constants.CLS_FUNCTIONAL_AREA).getIDTO();
			int prop = propDAO.getIdPropByName(Constants.PROP_FUNCTIONAL_AREA);
			int value = Auxiliar.getIdo(idtoClass, in.getAreaFuncName(), fcdb, instanceService.getDataBaseMap());
			list.add(new FactInstance(idto, ido, prop, String.valueOf(value), idtoClass, null, null, null, null, null, action.NEW));
			
			prop = propDAO.getIdPropByName(Constants.PROP_GLOBAL);
			double bolvalue = in.isGlobal()?1:0;
			list.add(new FactInstance(idto, ido, prop, null, Constants.IDTO_BOOLEAN, null, bolvalue, bolvalue, null, null, action.NEW));
			
			// System.out.println("---> OK");
		}
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
	}

}
