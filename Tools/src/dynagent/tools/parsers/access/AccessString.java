package dynagent.tools.parsers.access;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;
import dynagent.tools.importers.model.TableUtils;

public class AccessString {
	
	private String DENNIED;

	private String TASK;

	private String USERROL;

	private String USER;

	private String ACCESSTYPE;

	private String IDTO;

	private String ROL;

	private String IDO;

	private String PROP;

	private String VALUE;

	private String VALUECLS;

	private String CLSREL;

	private String IDOREL;

	private String ROLB;

	public String getACCESSTYPE() {
		return ACCESSTYPE;
	}

	public void setACCESSTYPE(String accesstype) {
		ACCESSTYPE = accesstype;
	}

	public String getCLSREL() {
		return CLSREL;
	}

	public void setCLSREL(String clsrel) {
		CLSREL = clsrel;
	}

	public String getDENNIED() {
		return DENNIED;
	}

	public void setDENNIED(String dennied) {
		DENNIED = dennied;
	}

	public String getIDO() {
		return IDO;
	}

	public void setIDO(String ido) {
		IDO = ido;
	}

	public String getIDOREL() {
		return IDOREL;
	}

	public void setIDOREL(String idorel) {
		IDOREL = idorel;
	}

	public String getIDTO() {
		return IDTO;
	}

	public void setIDTO(String idto) {
		IDTO = idto;
	}

	public String getPROP() {
		return PROP;
	}

	public void setPROP(String prop) {
		PROP = prop;
	}

	public String getROL() {
		return ROL;
	}

	public void setROL(String rol) {
		ROL = rol;
	}

	public String getROLB() {
		return ROLB;
	}

	public void setROLB(String rolb) {
		ROLB = rolb;
	}

	public String getTASK() {
		return TASK;
	}

	public void setTASK(String task) {
		TASK = task;
	}

	public String getUSER() {
		return USER;
	}

	public void setUSER(String user) {
		USER = user;
	}

	public String getUSERROL() {
		return USERROL;
	}

	public void setUSERROL(String userrol) {
		USERROL = userrol;
	}

	public String getVALUE() {
		return VALUE;
	}

	public void setVALUE(String value) {
		VALUE = value;
	}

	public String getVALUECLS() {
		return VALUECLS;
	}

	public void setVALUECLS(String valuecls) {
		VALUECLS = valuecls;
	}
	
	
public String toString(){
		
		String res = "UTASK: ";
		if(TASK==null)
			res+="TODAS";
		else
			res+=TASK;
		res += ", Accesstype "+ACCESSTYPE;
		
		if(USER!=null)
			res+=", User: "+USER;
		if(USERROL!=null)
			res+=", User Rol: "+USERROL;
		if(IDTO!=null)
			res+=", Clase: "+IDTO;
		if(ROL!=null)
			res+=", Rol: "+ROL;
		if(IDO!=null)
			res+=", Individuo: "+IDO;
		if(PROP!=null)
			res+=", Propiedad: "+PROP;
		if(VALUE!=null)
			res+=", Valor: "+VALUE;
		if(VALUECLS!=null)
			res+=", Rango: "+VALUECLS;
		if(CLSREL!=null)
			res+=", Relacion:" +CLSREL;
		if(IDOREL!=null)
			res+=", Individuo Relacion: "+IDOREL;
		if(ROLB!=null)
			res+=", RolB: "+ROLB;
		res+='\n';
		
		return res;
		
	}


public Access translateToAccess(){
	
	
	
	Access acc = new Access();
	
	if(this.IDTO != null && !this.IDTO.equals("TODAS")){
		Integer idto = TableUtils.getClasses().get(this.IDTO);
		if(idto==null){
			System.out.println("La clase "+this.IDTO+" no existe en la BD");
			return null;
		}
		else
			acc.setIDTO(idto);
	}
	
	if(this.IDO!=null && acc.getIDO()!=null){
		if(TableUtils.isIndividualOfClass(this.IDO, this.IDTO)){
			acc.setIDO(TableUtils.getClasses().get(this.IDO));
		}	
		else{
			System.out.println("El individuo "+this.IDO+" no existe en la BD");
			return null;
		}
	}
	
	acc.setACCESSTYPE(Constants.getIdAccess(this.ACCESSTYPE));
	
	if(this.DENNIED.equals("dennied"))
		acc.setDENNIED(1);
	else
		acc.setDENNIED(0);
	
	if(this.VALUECLS!=null){
		Integer idto = TableUtils.getClasses().get(this.IDTO);
		if(idto==null){
			System.out.println("La clase (para rango) "+this.VALUECLS+" no existe en la BD");
			return null;
		}
		else
			acc.setIDTO(idto);
	}
		
	//TODO Comprobar correcto user
	if(this.TASK!=null){
		if(!this.TASK.equals("TODAS") &&TableUtils.isUserTask(this.TASK)){
			Integer idto = TableUtils.getClasses().get(this.IDTO);
			if(idto==null){
				System.out.println("La utask "+this.TASK+" no existe en la BD");
				return null;
			}
			else
				acc.setIDTO(idto);
		}
	}
	
	if(this.USERROL!=null){
		if(!this.USERROL.equals("TODAS") && !this.TASK.equals("TODAS")){
			if(TableUtils.isUserRol(this.TASK, this.USERROL)){
				acc.setUSERROL(TableUtils.getClasses().get(this.USERROL));
			}
			else{
				System.out.println("El user rol "+this.USERROL+" no existe para la utask "+this.TASK);
			}
		}
	}
	//TODO Comprobar que estas 3 definiciones son coherentes en instances
	
	
	
	if(this.PROP!=null && !this.PROP.equals("TODAS") && TableUtils.getProperties().get(this.PROP)!=null){
		acc.setPROP(TableUtils.getProperties().get(this.PROP));
	}
	
	return acc;
}

}
