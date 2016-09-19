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
import dynagent.common.basicobjects.Usuarios;
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
import dynagent.server.database.Individual;
import dynagent.server.database.IndividualCreator;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.database.dao.UsuariosDAO;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.DeletableObject;
import dynagent.server.services.InstanceService;

public class importUsuarios extends ObjectConfig {

	
	private LinkedList<Usuarios> listUsuarios;
	private FactoryConnectionDB fcdb;
	private String sufixIndex;
	
	public importUsuarios(Element usuariosXML, FactoryConnectionDB fcdb, InstanceService instanceService, String sufixIndex, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(usuariosXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.fcdb = fcdb;
		this.sufixIndex = sufixIndex;
		this.listUsuarios=new LinkedList<Usuarios>();
	}
	
	@Override
	public boolean configData() throws Exception {
		//borrar usuarios ajenos si se han borrado los idos
		deleteUsuarios();
		return extractAllUsuarios();
	}
	@Override
	public void importData() throws Exception {
		//si no se han borrado los idos comprobar que no existe el login en DB antes de insertar
		insertUsuarios();
	}

	//descomentar este codigo solo tiene sentido si se vuelven a configurar usuarios manteniendo individuos
	/*
	//borrar usuarios que no tienen como sufijo el de tu maquina (si hay un sufijo definido)
	private void deleteUsuariosAjenos() throws SQLException, NamingException {
		if (sufixIndex!=null) {
			//buscar idos de usuarios con un sufijo diferente
			HashSet<Integer> idosABorrar = new HashSet<Integer>();
			UsuariosDAO uDAO=new UsuariosDAO();
			uDAO.open();
			LinkedList<Object> llu = uDAO.getAllCond("SUBSTRING(IDO_USUARIO,length(IDO_USUARIO)-2)<>" + sufixIndex);
			Iterator itu = llu.iterator();
			while (itu.hasNext()) {
				Usuarios usu = (Usuarios)itu.next();
				idosABorrar.add(usu.getIdoUsuario());
			}
			if (idosABorrar.size()>0) {
				String idos = dynagent.common.utils.Auxiliar.hashSetIntegerToString(idosABorrar, ",");
				uDAO.deleteCond("IDO_USUARIO IN(" + idos + ")");
				
				O_Datos_AttribDAO odaDAO = new O_Datos_AttribDAO();
				odaDAO.deleteCond("ID_O IN(" + idos + ") OR VAL_NUM IN(" + idos + ")");
				O_Reg_Instancias_IndexDAO oRegDAO = new O_Reg_Instancias_IndexDAO();
				oRegDAO.deleteCond("ID_O IN(" + idos + ")");
			}
			uDAO.close();
		}
	}*/
	private void deleteUsuarios() throws SQLException, NamingException, NoSuchColumnException {
		DeletableObject.deleteAllObjects(Auxiliar.getIdtoClass(Constants.CLS_USER, fcdb), instanceService.getDataBaseMap(), fcdb);
//		//buscar idos de usuarios con un sufijo diferente
//		UsuariosDAO uDAO=new UsuariosDAO();
//		uDAO.open();
//		/*HashSet<Integer> idosABorrar = new HashSet<Integer>();
//		LinkedList<Object> llu = uDAO.getAll();
//		Iterator itu = llu.iterator();
//		while (itu.hasNext()) {
//			Usuarios usu = (Usuarios)itu.next();
//			idosABorrar.add(usu.getIdoUsuario());
//		}*/
//		//if (idosABorrar.size()>0) {
//			uDAO.deleteAll();
//			
//			//en ODA no estaran porque se han borrado individuos
//			/*String idos = dynagent.common.utils.Auxiliar.hashSetIntegerToString(idosABorrar, ",");
//			O_Datos_AttribDAO odaDAO = new O_Datos_AttribDAO();
//			odaDAO.deleteCond("ID_O IN(" + idos + ") OR VAL_NUM IN(" + idos + ")");
//			O_Reg_Instancias_IndexDAO oRegDAO = new O_Reg_Instancias_IndexDAO();
//			oRegDAO.deleteCond("ID_O IN(" + idos + ")");*/
//		//}
//		uDAO.close();
	}
	
	private void insertUsuarios() throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException, InterruptedException, NoSuchColumnException {
		Iterator<Usuarios> itu=this.listUsuarios.iterator();
		HashMap<Integer,String> userAuxMap=new HashMap<Integer, String>();
//		UsuariosDAO uDAO=new UsuariosDAO();
//		uDAO.open();
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();   
		int countido=0;
		while(itu.hasNext()){
			Usuarios u=itu.next();
			//System.out.println("---> "+u.toString());
			/*
			//descomentar este codigo solo tiene sentido si se vuelven a configurar usuarios manteniendo individuos
			LinkedList<Object> llo = uDAO.getAllCond("LOGIN LIKE '" + u.getLogin() + "'");
			if (llo.size()>0) {
				//si esta -> hacer update si hay cambios
				Usuarios usu = (Usuarios)llo.get(0);
				//obtener pwd descifrado -> modificar DataBaseAdapter para que lo devuelva y no tener que
				//hacer una segunda consulta
				System.out.println("pwdDecr " + uDAO.getByID(String.valueOf(usu.getIdoUsuario())).getFirst());
				String pwdDecr = (String)uDAO.getByID(String.valueOf(usu.getIdoUsuario())).getFirst();
				if (!StringUtils.equals(pwdDecr,u.getPwd()) || !StringUtils.equals(usu.getMail(),u.getMail())) {
					String set = "";
					O_Datos_AttribDAO odaDAO = new O_Datos_AttribDAO();
					if (!StringUtils.equals(pwdDecr,u.getPwd())) {
						GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
						String function = generateSQL.getEncryptFunction(InstanceService.keyEncriptacion, u.getPwd());
						set = "PWD=" + function;
						//actualizar o_datos_atrib
						PropertiesDAO pDAO = new PropertiesDAO();
						Integer prop = pDAO.getIdPropByName(Constants.PROP_PASSWORD);
						odaDAO.update("VAL_TEXTO=" + function, 
								"ID_O=" + usu.getIdoUsuario() + " AND PROPERTY=" + prop);
					}
					if (!StringUtils.equals(usu.getMail(),u.getMail())) {
						if (set.length()>0)
							set += ",";
						set += "MAIL='" + u.getMail() + "'";
						//actualizar o_datos_atrib
						PropertiesDAO pDAO = new PropertiesDAO();
						Integer prop = pDAO.getIdPropByName(Constants.PROP_MAIL);
						odaDAO.update("VAL_TEXTO='" + u.getMail() + "'", 
								"ID_O=" + usu.getIdoUsuario() + " AND PROPERTY=" + prop);
					}
					uDAO.update(set, "IDO_USUARIO=" + usu.getIdoUsuario());
				}
			} else {*/
				//si no esta -> 1 crear ido, 2 insertar
//				TClaseDAO clDAO = new TClaseDAO();
//				Integer idtoUser = (Integer)(clDAO.getByID(String.valueOf(Constants.IDTO_USER)).getFirst());
				
				Integer idtoUser = Constants.IDTO_USER;
				
				/*Individual ind=IndividualCreator.creator(fcdb, DAOManager.getInstance().isCommit(), null, idtoUser, u.getLogin(), null, sufixIndex, null,false,null);
				int idoNew = ind.getIdo();
				String rdn = ind.getRdn();*/
				countido--;
				int ido=QueryConstants.getIdo(countido, idtoUser);
				String rdn= u.getLogin();
				
				list.add(new FactInstance(idtoUser,ido, Constants.IdPROP_RDN, rdn, Constants.IDTO_STRING, null, null, null, null, null, action.NEW));
				//insertar en o_datos_atrib:
				//rdn(login),pwd,email
				PropertiesDAO pDAO = new PropertiesDAO();
				Integer propPwd = pDAO.getIdPropByName(Constants.PROP_PASSWORD);
				//GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
				//String function = generateSQL.getEncryptFunction(InstanceService.keyEncriptacion, u.getPwd());
				list.add(new FactInstance(idtoUser,ido,propPwd,/*function*/u.getPwd(),Constants.IDTO_STRING,null,null,null,null,null,action.NEW));
				
//				O_Datos_Attrib odaPwd = new O_Datos_Attrib(idtoUser, idoNew, propPwd, null, function, Constants.IDTO_STRING, null, null, null, null);
//				odaPwd.setEncrypt(true);
//				O_Datos_AttribDAO odaDAO = new O_Datos_AttribDAO();
//				odaDAO.insert(odaPwd);
				
				if (u.getMail()!=null) {
					Integer propMail = pDAO.getIdPropByName(Constants.PROP_MAIL);
					list.add(new FactInstance(idtoUser,ido,propMail,u.getMail(),Constants.IDTO_STRING,null,null,null,null,null,action.NEW));
//					O_Datos_Attrib odaMail = new O_Datos_Attrib(idtoUser, idoNew, propMail, null, u.getMail(), Constants.IDTO_STRING, null, null, null, null);
//					odaDAO.insert(odaMail);
				}
				
//				u.setIdoUsuario(idoNew);
//				uDAO.insert(u);
				userAuxMap.put(ido,rdn);
			//}
			//System.out.println("---> OK");
		}
		
		Changes changes=instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
		
		ArrayList<ObjectChanged> changedList=changes.getAObjectChanged();
		
		for(int j=0;j<changedList.size();j++) {
			ObjectChanged oc = changedList.get(j);
		 	configImport.addUser(userAuxMap.get(oc.getOldIdo()), oc.getNewIdo());
		}
		
//		uDAO.close();
	}
	private boolean extractAllUsuarios() throws Exception {
		Iterator itu = getChildrenXml().iterator();
		boolean success=true;
		while(itu.hasNext()){
			Element uElem = (Element)itu.next();
			Usuarios u = new Usuarios();
			try{
				if (uElem.getAttribute(ConstantsXML.LOGIN)!=null){
					String login=uElem.getAttributeValue(ConstantsXML.LOGIN);
					u.setLogin(login);
				} else {
					throw new ConfigException("Error: El atributo "+ConstantsXML.LOGIN+" es obligatorio en el XML");
				}
				if (uElem.getAttribute(ConstantsXML.PWD)!=null){
					String pwd=uElem.getAttributeValue(ConstantsXML.PWD);
					u.setPwd(pwd);
				} else {
					throw new ConfigException("Error: El atributo "+ConstantsXML.PWD+" es obligatorio en el XML");
				}
				if (uElem.getAttribute(ConstantsXML.NOMBRE)!=null){
					String nombre=uElem.getAttributeValue(ConstantsXML.NOMBRE);
					u.setNombre(nombre);
				}
				if (uElem.getAttribute(ConstantsXML.APELLIDOS)!=null){
					String apellidos=uElem.getAttributeValue(ConstantsXML.APELLIDOS);
					u.setApellidos(apellidos);
				}
				if (uElem.getAttribute(ConstantsXML.ORGANIZACION)!=null){
					String organizacion=uElem.getAttributeValue(ConstantsXML.ORGANIZACION);
					u.setOrganizacion(organizacion);
				}
				if (uElem.getAttribute(ConstantsXML.GRUPO)!=null){
					String grupo=uElem.getAttributeValue(ConstantsXML.GRUPO);
					u.setGrupo(grupo);
				}
				if (uElem.getAttribute(ConstantsXML.MAIL)!=null){
					String mail=uElem.getAttributeValue(ConstantsXML.MAIL);
					u.setMail(mail);
				}
				if (uElem.getAttribute(ConstantsXML.DOMINIO)!=null){
					String dominio=uElem.getAttributeValue(ConstantsXML.DOMINIO);
					u.setDominio(dominio);
				}
				listUsuarios.add(u);
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
