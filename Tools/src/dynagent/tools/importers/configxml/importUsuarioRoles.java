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
import dynagent.common.basicobjects.UsuarioRoles;
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
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.action;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class importUsuarioRoles extends ObjectConfig {

	
	private LinkedList<UsuarioRoles> listUsuarioRoles;
	
	public importUsuarioRoles(Element usuarioRolesXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(usuarioRolesXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listUsuarioRoles=new LinkedList<UsuarioRoles>();
	}
	
	@Override
	public boolean configData() throws Exception {
		return extractAllUsuarioRoles();
	}
	@Override
	public void importData() throws Exception {
		//deleteUsuarioRoles(); //comentar esta llamada solo tiene sentido si se vuelven a configurar usuarios manteniendo individuos
		insertUsuarioRoles();
		//cleanUsuarioRoles();
	}
	
	/*private void cleanUsuarioRoles() throws SQLException, NamingException {
		UsuariosDAO uDAO=new UsuariosDAO();
		uDAO.open();
		HashSet<Integer> idosABorrar = new HashSet<Integer>();
		LinkedList<Object> llo = uDAO.getAllCond("IDO_USUARIO NOT IN(SELECT USUARIO FROM USUARIOROLES)");
		Iterator it = llo.iterator();
		while(it.hasNext()){
			Usuarios u=(Usuarios)it.next();
			idosABorrar.add(u.getIdoUsuario());
		}
		HashSet<Integer> idosABorrar2 = new HashSet<Integer>();
		RolesDAO rDAO=new RolesDAO();
		llo = rDAO.getAllCond("IDO_ROL NOT IN(SELECT ROL FROM USUARIOROLES)");
		it = llo.iterator();
		while(it.hasNext()){
			Roles u=(Roles)it.next();
			idosABorrar2.add(u.getIdoRol());
		}
		//borrar idoUsuario que no este en la tabla usuarioRoles
		if (idosABorrar.size()>0) {
			String idos = dynagent.common.utils.Auxiliar.hashSetIntegerToString(idosABorrar, ",");
			uDAO.deleteCond("IDO_USUARIO IN(" + idos + ")");
			
			O_Datos_AttribDAO odaDAO = new O_Datos_AttribDAO();
			odaDAO.deleteCond("ID_O IN(" + idos + ") OR VAL_NUM IN(" + idos + ")");
			O_Reg_Instancias_IndexDAO oRegDAO = new O_Reg_Instancias_IndexDAO();
			oRegDAO.deleteCond("ID_O IN(" + idos + ")");
		}
		if (idosABorrar2.size()>0) {
			String idos = dynagent.common.utils.Auxiliar.hashSetIntegerToString(idosABorrar2, ",");
			uDAO.deleteCond("IDO_ROL IN(" + idos + ")");
			
			O_Datos_AttribDAO odaDAO = new O_Datos_AttribDAO();
			odaDAO.deleteCond("ID_O IN(" + idos + ") OR VAL_NUM IN(" + idos + ")");
			O_Reg_Instancias_IndexDAO oRegDAO = new O_Reg_Instancias_IndexDAO();
			oRegDAO.deleteCond("ID_O IN(" + idos + ")");
		}
		uDAO.close();
	}*/
	
	private void deleteUsuarioRoles() throws SQLException, NamingException {
//		UsuarioRolesDAO rDAO=new UsuarioRolesDAO();
//		rDAO.open();
//		rDAO.deleteAll();
//		
//		PropertiesDAO pDAO = new PropertiesDAO();
//		Integer propUserRol = pDAO.getIdPropByName(Constants.PROP_USERROL);
//		O_Datos_AttribDAO odaDAO = new O_Datos_AttribDAO();
//		odaDAO.deleteCond("PROPERTY=" + propUserRol);
//
//		rDAO.close();
	}
	
	private void insertUsuarioRoles() throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException, InterruptedException, NoSuchColumnException {
		Iterator<UsuarioRoles> itr=this.listUsuarioRoles.iterator();
		//UsuarioRolesDAO ruDAO=new UsuarioRolesDAO();
		//ruDAO.open();
		ArrayList<IPropertyDef> list=new ArrayList<IPropertyDef>();
		while(itr.hasNext()){
			UsuarioRoles ru=itr.next();
			//System.out.println("---> "+ru.toString());
//			LinkedList<Object> llo = ruDAO.getAllCond("USUARIO=" + ru.getIdoUsuario() + " AND ROL=" + ru.getIdoRol());
//			if (llo.size()==0) {
//				String login = ru.getUsuario();
//				String nameRol = ru.getRol();
//				
//				//buscar el ido de ese usuario
//				UsuariosDAO uDAO=new UsuariosDAO();
//				LinkedList<Object> llo2 = uDAO.getAllCond("LOGIN LIKE '" + login + "'");
//				if (llo2.size()>0) {
//					Usuarios usu = (Usuarios)llo2.getFirst();
//					RolesDAO rDAO = new RolesDAO();
//					LinkedList<Object> llo3 = rDAO.getAllCond("NAME_ROL LIKE '" + nameRol + "'");
//					if (llo3.size()>0) {
//						Roles rol = (Roles)llo3.getFirst();
//						ru.setIdoUsuario(usu.getIdoUsuario());
//						ru.setIdoRol(rol.getIdoRol());
//						ruDAO.insert(ru);
//					
//						/*TClaseDAO clDAO = new TClaseDAO();
//						Integer idtoUser = (Integer)(clDAO.getByID(String.valueOf(Constants.IDTO_USER)).getFirst());
//						Integer idtoUserRol = (Integer)(clDAO.getByID(String.valueOf(Constants.IDTO_USERROL)).getFirst());*/
//						Integer idtoUser = Constants.IDTO_USER;
//						Integer idtoUserRol = Constants.IDTO_USERROL;
//						PropertiesDAO pDAO = new PropertiesDAO();
//						Integer propUserRol = pDAO.getIdPropByName(Constants.PROP_USERROL);
//						/*O_Datos_AttribDAO odaDAO = new O_Datos_AttribDAO();
//						O_Datos_Attrib odaUserRol = new O_Datos_Attrib(idtoUser, usu.getIdoUsuario(), propUserRol, rol.getIdoRol(), nameRol, idtoUserRol, null, null, null, null);
//						odaDAO.insert(odaUserRol);
//						*/
//						list.add(new FactInstance(idtoUser,usu.getIdoUsuario(), propUserRol, String.valueOf(rol.getIdoRol()), idtoUserRol, null, null, null, null, null, action.SET));
//					}
//				}
			
			String login = ru.getUsuario();
			String nameRol = ru.getRol();
				
			Integer idtoUser = Constants.IDTO_USER;
			Integer idtoUserRol = Constants.IDTO_USERROL;
			PropertiesDAO pDAO = new PropertiesDAO();
			Integer propUserRol = pDAO.getIdPropByName(Constants.PROP_USERROL);
			/*O_Datos_AttribDAO odaDAO = new O_Datos_AttribDAO();
			O_Datos_Attrib odaUserRol = new O_Datos_Attrib(idtoUser, usu.getIdoUsuario(), propUserRol, rol.getIdoRol(), nameRol, idtoUserRol, null, null, null, null);
			odaDAO.insert(odaUserRol);
			*/
			list.add(new FactInstance(idtoUser,configImport.getIdoUser(login), propUserRol, String.valueOf(configImport.getIdoUserRol(nameRol)), idtoUserRol, null, null, null, null, null, action.SET));

			//System.out.println("---> OK");
		}
		
		instanceService.serverTransitionObject(Constants.USER_SYSTEM, new IndividualData(list,instanceService.getIk()), null, true, false, null);
//		ruDAO.close();
	}
	
	private boolean extractAllUsuarioRoles() throws Exception {
		Iterator itr = getChildrenXml().iterator();
		boolean success=true;
		while(itr.hasNext()){
			Element rElem = (Element)itr.next();
			UsuarioRoles r = new UsuarioRoles();
			try{
				if (rElem.getAttribute(ConstantsXML.USUARIO)!=null){
					String usuario=rElem.getAttributeValue(ConstantsXML.USUARIO);
					r.setUsuario(usuario);
				} else {
					throw new ConfigException("Error: El atributo '"+ConstantsXML.USUARIO+"' es obligatorio en el XML");
				}
				if (rElem.getAttribute(ConstantsXML.ROL)!=null){
					String rol=rElem.getAttributeValue(ConstantsXML.ROL);
					r.setRol(rol);
				} else {
					throw new ConfigException("Error: El atributo '"+ConstantsXML.ROL+"' es obligatorio en el XML");
				}
				listUsuarioRoles.add(r);
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
