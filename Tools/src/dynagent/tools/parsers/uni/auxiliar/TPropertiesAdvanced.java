package dynagent.tools.parsers.uni.auxiliar;

import dynagent.common.basicobjects.Properties;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.IDAO;
import dynagent.server.database.dao.PropertiesDAO;

import java.sql.SQLException;
import java.util.LinkedList;

import java.util.ArrayList;

import javax.naming.NamingException;

public class TPropertiesAdvanced {
	
	private LinkedList tablaProperties=null;
	private IDAO idao;
	
	public TPropertiesAdvanced() throws SQLException, NamingException{
		idao = DAOManager.getInstance().getDAO("properties");
		idao.open();
		PropertiesDAO propsdao = (PropertiesDAO)idao.getDAO();
		if(propsdao==null)
			System.out.println("insdao=null");
		tablaProperties = propsdao.getAll();	
		idao.close();
	}
	
	public LinkedList getTablaProperties(){
		return tablaProperties;
	}
	
	public void setTablaProperties(LinkedList l){
		this.tablaProperties = l;
	}
	
	public IDAO getIDAO(){
		return idao;
	}
	
	public void setIDAO(IDAO idao){
		this.idao = idao;
	}
	
	public String getNameById(int idto){
		for(int i = 0; i< tablaProperties.size();i++){
			Properties prop = (Properties) tablaProperties.get(i);
			if(prop.getPROP().intValue() == idto)
				return prop.getNAME();
		}
		return null;
	}
	
	public int getIdByName(String s){
		for(int i = 0; i< tablaProperties.size();i++){
			Properties prop = (Properties) tablaProperties.get(i);
			if(prop.getNAME().equals(s))
				return prop.getPROP().intValue();
		}
		return -1;
	}
	
	public int getCatById(int idto){
		for(int i = 0; i< tablaProperties.size();i++){
			Properties prop = (Properties) tablaProperties.get(i);
			if(prop.getPROP().intValue() == idto)
				return prop.getCAT().intValue();
		}
		return -1;
	}
	
	public ArrayList getPropertiesDB(){
		ArrayList l = new ArrayList();
		for(int i = 0; i< tablaProperties.size(); i++){
			Properties pr = (Properties) tablaProperties.get(i);
			PropiedadBD prop = new PropiedadBD();
			int idto = pr.getPROP().intValue();
			String name = pr.getNAME();
			Integer cls = pr.getVALUECLS();
			prop.setIdProp(idto);
			if(cls!=null)
			prop.setCls(cls.intValue());
			prop.setName(name);
			Float f = pr.getQMAX();
			if(f!=null){
				int qMax = (int) f.longValue();
				prop.setQMax(qMax);
			}
			
			f = pr.getQMIN();
			if(f!=null){
				int qMin = (int) f.longValue();
				prop.setQMax(qMin);
			}
			
			l.add(prop);
		}
		return l;
	}
	
	public void insertNewProperty(Integer i1,Integer i2, String name) throws SQLException{
		Properties nProp = new Properties();
		nProp.setNAME(name);
		nProp.setCAT(i2);
		nProp.setPROP(i1);
		nProp.setMASK(null);
		nProp.setOP(null);
		nProp.setQMAX(null);
		nProp.setQMIN(null);
		nProp.setVALUE(null);
		nProp.setVALUECLS(null);
		PropertiesDAO nClasDAO = new PropertiesDAO();
		nClasDAO.insert(nProp);
	}
	
	public void close() throws SQLException{
		idao.close();
	}

}
