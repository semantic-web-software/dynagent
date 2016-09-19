package dynagent.tools.parsers.uni.auxiliar;

import java.sql.SQLException;
import java.util.LinkedList;

import javax.naming.NamingException;

import dynagent.common.basicobjects.Instance;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.IDAO;
import dynagent.server.database.dao.InstanceDAO;

public class TInstancesAdvanced {
	private LinkedList tablaInstances=null;
	private IDAO idao;
	
	public TInstancesAdvanced() throws SQLException, NamingException{
		idao = DAOManager.getInstance().getDAO("instances");
		idao.open();
		InstanceDAO insdao = (InstanceDAO)idao.getDAO();
		if(insdao==null)
			System.out.println("insdao=null");
		tablaInstances = insdao.getAll();	
		idao.close();
	}
	
	public LinkedList getTablaInstances(){
		return tablaInstances;
	}
	
	public void setTablaInstances(LinkedList l){
		this.tablaInstances = l;
	}
	
	public IDAO getIDAO(){
		return idao;
	}
	
	public void setIDAO(IDAO idao){
		this.idao = idao;
	}
	
	/* CREO QUE NO HACE FALTA NINGUNA FUNCION COMO ESTA
	 
	 public String getNameById(int idto){
		for(int i = 0; i< tablaInstances.size();i++){
			Properties prop = (Properties) tablaInstances.get(i);
			if(prop.getPROP().intValue() == idto)
				return prop.getNAME();
		}
		return null;
	}
	
	public int getCatById(int idto){
		for(int i = 0; i< tablaProperties.size();i++){
			Properties prop = (Properties) tablaProperties.get(i);
			if(prop.getPROP().intValue() == idto)
				return prop.getCAT().intValue();
		}
		return -1;
	}*/
	
	public void insertNewInstance(String idto, String rol, String ido,
			String property, String value, String valuecls, String clsrel,
			String idorel, String QMin, String QMax, String rolb, String name,
			String op) throws SQLException{
		Instance ins = new Instance();
		ins.setIDTO(idto);
		ins.setIDO(ido);
		ins.setPROPERTY(property);
		ins.setVALUE(value);
		ins.setVALUECLS(valuecls);
		ins.setQMIN(QMin);
		ins.setQMAX(QMax);
		ins.setNAME(name);
		ins.setOP(op);
		InstanceDAO insDAO = new InstanceDAO();
		insDAO.insert(ins);
		
	}
	
	public void close() throws SQLException{
		idao.close();
	}


}
