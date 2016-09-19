package dynagent.tools.parsers.uni.auxiliar;




//ELIMINAR DE AQUI CUANDO NO HAYA QUE PROBAR MAS
//FraN

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.naming.NamingException;

import dynagent.common.basicobjects.TClase;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.IDAO;
import dynagent.server.database.dao.TClaseDAO;


public class TClaseAdvanced {
	private LinkedList tablaClases=null;
	private IDAO idao;

	
	public TClaseAdvanced() throws SQLException, NamingException{
		idao = DAOManager.getInstance().getDAO("Clases");
		idao.open();
		TClaseDAO clasdao = (TClaseDAO)idao.getDAO();
		if(clasdao==null)
			System.out.println("classdao=null");
		tablaClases = clasdao.getAll();	
		idao.close();
	}
	
	public LinkedList getTablaClases(){
		return tablaClases;
	}
	
	public void setTablaClases(LinkedList l){
		this.tablaClases = l;
	}
	
	public IDAO getIDAO(){
		return idao;
	}
	
	public void setIDAO(IDAO idao){
		this.idao = idao;
	}
	
	public String getNameById(int idto){
		for(int i = 0; i< tablaClases.size();i++){
			TClase clase = (TClase) tablaClases.get(i);
			if(clase.getIDTO() == idto)
				return clase.getName();
		}
		return null;
	}
	
	public int getIdByName(String s){
		for(int i = 0; i< tablaClases.size();i++){
			TClase clase = (TClase) tablaClases.get(i);
			if(clase.getName().equals(s))
				return clase.getIDTO();
		}
		return -1;
	}
	
	public ArrayList getClasesDB(){
		ArrayList l = new ArrayList();
		for(int i = 0; i< tablaClases.size(); i++){
			TClase tclase = (TClase) tablaClases.get(i);
			Clase clase = new Clase();
			int idto = tclase.getIDTO();
			String name = tclase.getName();
			clase.setIdto(idto);
			clase.setName(name);
			l.add(clase);
		}
		return l;
	}
	
	public void insertNewClass(int idto, String name) throws SQLException{
		TClase nClas = new TClase();
		nClas.setIDTO(idto);
		nClas.setName(name);
		TClaseDAO nClasDAO = new TClaseDAO();
		nClasDAO.insert(nClas);
	}
	
	public void close() throws SQLException{
		idao.close();
	}
	
}
