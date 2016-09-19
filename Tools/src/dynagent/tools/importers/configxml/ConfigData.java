package dynagent.tools.importers.configxml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class ConfigData {

	private LinkedHashMap<String,Integer> mapNameIdGroups;
	private LinkedHashMap<String,Integer> mapNameIdoUsers;
	private LinkedHashMap<String,Integer> mapNameIdoUserRoles;
	private HashMap<String,String> mapUtaskTarget;
	private HashSet<String> reportList;
	private int maxIdGroups;
	private ArrayList<String> userRolList;
	private ArrayList<String> userList;
	
	public ConfigData(){
		mapNameIdGroups=new LinkedHashMap<String, Integer>();
		mapNameIdoUsers=new LinkedHashMap<String, Integer>();
		mapNameIdoUserRoles=new LinkedHashMap<String, Integer>();
		mapUtaskTarget=new HashMap<String, String>();
		reportList=new HashSet<String>();
		userRolList=new ArrayList<String>();
		userList=new ArrayList<String>();
		maxIdGroups=0;
	}
	
	public Integer getIdGroup(String nameGroup){
		return mapNameIdGroups.get(nameGroup);
	}
	
	public void addGroup(String nameGroup,Integer idGroup){
		mapNameIdGroups.put(nameGroup,idGroup);
		maxIdGroups=Math.max(maxIdGroups, idGroup);
	}
	
	public int maxIdGroups(){
		return maxIdGroups;
	}
	
	public boolean containsUserTask(String nameUTask){
		return mapUtaskTarget.keySet().contains(nameUTask);
	}
	
	public boolean containsTargetClass(String nameTargetClass){
		return mapUtaskTarget.values().contains(nameTargetClass);
	}
	
	public void addUserTask(String nameUTask,String nameTargetClass){
		mapUtaskTarget.put(nameUTask,nameTargetClass);
	}
	
	public void addUserRolName(String userRolName){
		userRolList.add(userRolName);
	}
	
	public void addReportName(String reportName){
		reportList.add(reportName);
	}
	
	public boolean containsReportName(String reportName){
		return reportList.contains(reportName);
	}
	
	public HashSet<String> getReportList() {
		return reportList;
	}

	public boolean containsUserRolName(String userRolName){
		return userRolList.contains(userRolName);
	}

	public void addUserName(String userName){
		userList.add(userName);
	}
	
	public boolean containsUserName(String userName){
		return userList.contains(userName);
	}
		
	public void addUser(String user,int ido){
		mapNameIdoUsers.put(user, ido);
	}
	
	public Integer getIdoUser(String user){
		return mapNameIdoUsers.get(user);
	}
	
	public void addUserRol(String userRol,int ido){
		mapNameIdoUserRoles.put(userRol, ido);
	}
	
	public Integer getIdoUserRol(String userRol){
		return mapNameIdoUserRoles.get(userRol);
	}
	
	public LinkedHashMap<String,Integer> getUserRoles(){
		return mapNameIdoUserRoles;
	}
}
