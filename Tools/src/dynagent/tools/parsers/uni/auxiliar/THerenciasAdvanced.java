package dynagent.tools.parsers.uni.auxiliar;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.naming.NamingException;

import dynagent.common.basicobjects.T_Herencias;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.IDAO;
import dynagent.server.database.dao.T_HerenciasDAO;

public class THerenciasAdvanced {
	
	private LinkedList tablaHerencias=null;
	private IDAO idao;
	
	public THerenciasAdvanced() throws SQLException, NamingException{
		idao = DAOManager.getInstance().getDAO("T_Herencias");
		idao.open();
		T_HerenciasDAO clasdao = (T_HerenciasDAO)idao.getDAO();
		if(clasdao==null)
			System.out.println("insdao=null");
		tablaHerencias = clasdao.getAll();	
		idao.close();
	}
	
	public LinkedList getTablaHerencias(){
		return tablaHerencias;
	}
	
	public void setTablaHerencias(LinkedList l){
		this.tablaHerencias= l;
	}
	
	public IDAO getIDAO(){
		return idao;
	}
	
	public void setIDAO(IDAO idao){
		this.idao = idao;
	}
	
	public ArrayList getParents(int idto){
		ArrayList padres = new ArrayList();
		for(int i = 0; i< tablaHerencias.size();i++){
			T_Herencias her = (T_Herencias) tablaHerencias.get(i);
			if(her.getID_TO() == idto)
				padres.add(new Integer(her.getID_TO_Padre()));
		}
		return padres;
	}
	
	public void insertNewHerencia(int idto, int idto_padre) throws SQLException{
		T_Herencias nHer = new T_Herencias();
		nHer.setID_TO(idto);
		nHer.setID_TO_Padre(idto_padre);
		T_HerenciasDAO nHerDAO = new T_HerenciasDAO();
		nHerDAO.insert(nHer);
	}
	
	public ArrayList getHerenciasDB(){
		ArrayList l = new ArrayList();
		for(int i = 0; i< tablaHerencias.size(); i++){
			T_Herencias tHer = (T_Herencias) tablaHerencias.get(i);
			SubclaseDB subclase = new SubclaseDB();
			int idto = tHer.getID_TO();
			int idtoP = tHer.getID_TO_Padre();
			subclase.setIdto(idto);
			subclase.setIdtoPadre(idtoP);
			l.add(subclase);
		}
		return l;
	}
	
	public void close() throws SQLException{
		idao.close();
	}
	
}



