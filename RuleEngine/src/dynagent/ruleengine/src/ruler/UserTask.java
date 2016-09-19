/***
 * UserTask.java
 * @author Ildefonso Montero Perez 
 * (UTask (id int)  (class int) (target int) (owner string) (policy int) (owningLevel int) (userRol int))
 */

package dynagent.ruleengine.src.ruler;

public class UserTask {

	private int id;
	private int cls;
	private int farea;
	private int idTg;
	private int clsTg;
	private String owner;
	private int policy;
	private int owningLevel;
	private int userRol;
	
	public int getCls() {
		return cls;
	}
	public void setCls(int cls) {
		this.cls = cls;
	}
	public int getClsTg() {
		return clsTg;
	}
	public void setClsTg(int clsTg) {
		this.clsTg = clsTg;
	}
	public int getFarea() {
		return farea;
	}
	public void setFarea(int farea) {
		this.farea = farea;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getIdTg() {
		return idTg;
	}
	public void setIdTg(int idTg) {
		this.idTg = idTg;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public int getOwningLevel() {
		return owningLevel;
	}
	public void setOwningLevel(int owningLevel) {
		this.owningLevel = owningLevel;
	}
	public int getPolicy() {
		return policy;
	}
	public void setPolicy(int policy) {
		this.policy = policy;
	}
	public int getUserRol() {
		return userRol;
	}
	public void setUserRol(int userRol) {
		this.userRol = userRol;
	}
}
