package dynagent.common.knowledge;

public class UserAccess {
	//private int usertask;
	private int userRol;
	private dynagent.common.knowledge.access access;
	
	//Constructores
	public UserAccess(/*int usertask,*/int userRol, access acc){
		this.userRol=userRol;
		//this.usertask=usertask;
		this.access=acc;
	}
	public UserAccess(){
		
	}
	
	//Setter and getters
	public dynagent.common.knowledge.access getAccess() {
		return access;
	}
	public void setAccess(dynagent.common.knowledge.access access) {
		this.access = access;
	}
	public int getUserRol() {
		return userRol;
	}
	public void setUserRol(int userRol) {
		this.userRol = userRol;
	}
	/*public int getUsertask() {
		return usertask;
	}
	public void setUsertask(int usertask) {
		this.usertask = usertask;
	}*/

}
