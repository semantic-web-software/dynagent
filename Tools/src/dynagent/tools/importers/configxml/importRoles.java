package dynagent.tools.importers.configxml;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.Roles;
import dynagent.common.communication.Changes;
import dynagent.common.communication.IndividualData;
import dynagent.common.communication.ObjectChanged;
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
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.action;
import dynagent.common.utils.QueryConstants;
import dynagent.server.database.IndividualCreator;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.RolesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.DeletableObject;
import dynagent.server.services.InstanceService;

public class importRoles extends ObjectConfig {

	
	private LinkedList<Roles> listRoles;
	private String sufixIndex;
//	private HashMap<String,Integer> hRoles;
	
	public importRoles(Element rolesXML, FactoryConnectionDB fcdb, InstanceService instanceService, String sufixIndex, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(rolesXML,fcdb,instanceService,configImport,pathImportOtherXml);
//		this.hRoles = new HashMap<String, Integer>();
		this.sufixIndex = sufixIndex;
		this.listRoles=new LinkedList<Roles>();
	}
	
//	public HashMap<String,Integer> getRoles() {
//		return hRoles;
//	}
	@Override
	public boolean configData() throws Exception {
		deleteRoles();
		return extractAllRoles();
	}
	@Override
	public void importData() throws Exception {
		insertRoles();
	}
	
	//descomentar este codigo solo tiene sentido si se vuelven a configurar usuarios manteniendo individuos
	/*private void deleteRolesAjenos() throws SQLException, NamingException {
		if (sufixIndex!=null) {
			//buscar idos de roles con un sufijo diferente
			HashSet<Integer> idosABorrar = new HashSet<Integer>();
			RolesDAO rDAO=new RolesDAO();
			rDAO.open();
			LinkedList<Object> llr = rDAO.getAllCond("SUBSTRING(IDO_ROL,length(IDO_ROL)-2)<>" + sufixIndex);
			Iterator itr = llr.iterator();
			while (itr.hasNext()) {
				Roles rol = (Roles)itr.next();
				idosABorrar.add(rol.getIdoRol());
			}
			if (idosABorrar.size()>0) {
				UsuarioRolesDAO urDAO=new UsuarioRolesDAO();
				//comprobar que estos idos no estan relacionados con un usuario valido
				LinkedList<Object> llo = urDAO.getAllCond("ROL IN(" + 
						dynagent.common.utils.Auxiliar.hashSetIntegerToString(idosABorrar, ",") + ") " +
						"AND USUARIO IN(SELECT IDO_USUARIO FROM USUARIOS)");
				Iterator it = llo.iterator();
				while (it.hasNext()) {
					UsuarioRoles usu = (UsuarioRoles)it.next();
					Integer ido = usu.getIdoRol();
					idosABorrar.remove(ido);
				}
				
				String idos = dynagent.common.utils.Auxiliar.hashSetIntegerToString(idosABorrar, ",");
				rDAO.deleteCond("IDO_ROL IN(" + idos + ")");
				
				O_Datos_AttribDAO odaDAO = new O_Datos_AttribDAO();
				odaDAO.deleteCond("ID_O IN(" + idos + ") OR VAL_NUM IN(" + idos + ")");
				O_Reg_Instancias_IndexDAO oRegDAO = new O_Reg_Instancias_IndexDAO();
				oRegDAO.deleteCond("ID_O IN(" + idos + ")");
			}
			rDAO.close();
		}
	}*/
	private void deleteRoles() throws SQLException, NamingException, NoSuchColumnException {
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_USERROL, fcdb), instanceService.getDataBaseMap(), fcdb);
//		RolesDAO rDAO=new RolesDAO();
//		rDAO.open();
//		/*HashSet<Integer> idosABorrar = new HashSet<Integer>();
//		LinkedList<Object> llr = rDAO.getAll();
//		Iterator itr = llr.iterator();
//		while (itr.hasNext()) {
//			Roles rol = (Roles)itr.next();
//			idosABorrar.add(rol.getIdoRol());
//		}*/
//		//if (idosABorrar.size()>0) {
//			rDAO.deleteAll();
//			
//			//en ODA no estaran porque se han borrado individuos
//			/*String idos = dynagent.common.utils.Auxiliar.hashSetIntegerToString(idosABorrar, ",");
//			O_Datos_AttribDAO odaDAO = new O_Datos_AttribDAO();
//			odaDAO.deleteCond("ID_O IN(" + idos + ") OR VAL_NUM IN(" + idos + ")");
//			O_Reg_Instancias_IndexDAO oRegDAO = new O_Reg_Instancias_IndexDAO();
//			oRegDAO.deleteCond("ID_O IN(" + idos + ")");*/
//		//}
//		rDAO.close();
	}
	
	private void insertRoles() throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException, InterruptedException, NoSuchColumnException {
		Iterator<Roles> itr=this.listRoles.iterator();
		HashMap<Integer,String> rolesAuxMap=new HashMap<Integer, String>();
		//RolesDAO rDAO=new RolesDAO();
		//rDAO.open();
		int countido=0;
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();   
		while(itr.hasNext()){
			Roles r=itr.next();
			//System.out.println("---> "+r.toString());
			//descomentar este codigo solo tiene sentido si se vuelven a configurar usuarios manteniendo individuos
			/*LinkedList<Object> llo = rDAO.getAllCond("NAME_ROL LIKE '" + r.getNameRol() + "'");
			if (llo.size()>0) {
				//si esta -> hacer update si hay cambios
				Roles rol = (Roles)llo.get(0);
				if (!StringUtils.equals(rol.getArea(),r.getArea())) {
					rDAO.update("AREA='" + r.getArea() + "'", "IDO_ROL=" + rol.getIdoRol() + "'");
					//actualizar o_datos_atrib
					PropertiesDAO pDAO = new PropertiesDAO();
					Integer prop = pDAO.getIdPropByName(Constants.PROP_AREA);
					O_Datos_AttribDAO odaDAO = new O_Datos_AttribDAO();
					odaDAO.update("VAL_TEXTO='" + r.getArea() + "'", 
							"ID_O=" + rol.getIdoRol() + " AND PROPERTY=" + prop);
				}
				hRoles.put(rol.getNameRol(), rol.getIdoRol());
			} else {*/
				//si no esta -> 1 crear ido, 2 insertar
//				TClaseDAO clDAO = new TClaseDAO();
//				Integer idtoUserRol = (Integer)(clDAO.getByID(String.valueOf(Constants.IDTO_USERROL)).getFirst());
				
				Integer idtoUserRol = Constants.IDTO_USERROL;
					
//				int idoNew = IndividualCreator.creator(fcdb, DAOManager.getInstance().isCommit(), null, idtoUserRol, r.getNameRol(), null, sufixIndex, null,false,null).getIdo();
//				
//				O_Datos_AttribDAO odaDAO = new O_Datos_AttribDAO();
//				//insertar en o_datos_atrib:
//				//rdn(login),area
//				if (r.getArea()!=null) {
//					PropertiesDAO pDAO = new PropertiesDAO();
//					Integer propArea = pDAO.getIdPropByName(Constants.PROP_AREA);
//					O_Datos_Attrib odaArea = new O_Datos_Attrib(idtoUserRol, idoNew, propArea, null, r.getArea(), Constants.IDTO_STRING, null, null, null, null);
//					odaDAO.insert(odaArea);
//				}
				
				
				
				countido--;
				int ido=QueryConstants.getIdo(countido, idtoUserRol);
				String rdn=r.getNameRol();
				
				list.add(new FactInstance(idtoUserRol,ido, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));

				//insertar en o_datos_atrib:
				//rdn(login),area
				if (r.getArea()!=null) {
					PropertiesDAO pDAO = new PropertiesDAO();
					Integer propArea = pDAO.getIdPropByName(Constants.PROP_AREA);
					list.add(new FactInstance(idtoUserRol,ido, propArea, r.getArea(), Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
				}

//				r.setIdoRol(idoNew);
//				hRoles.put(r.getNameRol(), idoNew);
//				rDAO.insert(r);
				rolesAuxMap.put(ido,rdn);
			//}
			//System.out.println("---> OK");
		}
		
		Changes changes=instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
		
		ArrayList<ObjectChanged> changedList=changes.getAObjectChanged();
		
		for(int j=0;j<changedList.size();j++) {
			ObjectChanged oc = changedList.get(j);
		 	configImport.addUserRol(rolesAuxMap.get(oc.getOldIdo()), oc.getNewIdo());
		}
		
//		rDAO.close();
	}
	
	private boolean extractAllRoles() throws Exception {
		Iterator itr = getChildrenXml().iterator();
		boolean success=true;
		while(itr.hasNext()){
			Element rElem = (Element)itr.next();
			Roles r = new Roles();
			try{
				if (rElem.getAttribute(ConstantsXML.NAME_ROL)!=null){
					String nameRol=rElem.getAttributeValue(ConstantsXML.NAME_ROL);
					r.setNameRol(nameRol);
				} else {
					throw new ConfigException("Error: El atributo '"+ConstantsXML.NAME_ROL+"' es obligatorio en el XML");
				}
				if (rElem.getAttribute(ConstantsXML.AREA)!=null){
					String area=rElem.getAttributeValue(ConstantsXML.AREA);
					r.setArea(area);
				}
				listRoles.add(r);
				configImport.addUserRolName(r.getNameRol());
			}catch(ConfigException ex){
				System.err.println(ex.getMessage());
				success=false;
			}catch(Exception ex){
				throw ex;
			}
		}
		return success;
	}

	public void setRolesFromDB(FactoryConnectionDB fcdb) throws SQLException, NamingException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
//		RolesDAO rDAO=new RolesDAO();
//		rDAO.open();
//		LinkedList<Object> llo = rDAO.getAll();
//		Iterator itr = llo.iterator();
//		while(itr.hasNext()){
//			Roles r = (Roles)itr.next();
//			//System.out.println("---> "+r.toString());
//			hRoles.put(r.getNameRol(), r.getIdoRol());
//		}
//		rDAO.close();
		IndividualData individualData=instanceService.serverGetFactsInstanceOfClass(Constants.IDTO_USERROL, Constants.USER_SYSTEM, false, 1, false);
		for(IPropertyDef f:individualData.getAIPropertyDef()){
			if(f.getPROP()==Constants.IdPROP_RDN){
				configImport.addUserRol(f.getVALUE(), f.getIDO());
			}
		}
	}
}
