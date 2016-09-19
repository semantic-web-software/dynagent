/***
 * Access.java
 * @author: Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.common.basicobjects;

import java.util.ArrayList;

import dynagent.common.Constants;
import dynagent.common.utils.Auxiliar;

public class Access {

	public static String[] ACCESS_TYPES={Constants.CLS_ACCESS_PROPERTY,Constants.CLS_ACCESS_CLASS,Constants.CLS_ACCESS_UTASK,Constants.CLS_ACCESS_GENERIC};
	
	private Integer DENNIED;

	private Integer TASK;
	private ArrayList<String> TASKNAME;

	private ArrayList<String> USERROL;

	private ArrayList<String> USER;

	private ArrayList<String> ACCESSTYPENAME;

	private Integer IDTO;

	private Integer IDO;

	private Integer[] PROP;

	private String VALUE;

	private Integer VALUECLS;

	private int PRIORITY;
	
	private ArrayList<Integer> FUNCTIONALAREA;
	
	
	public Access() {
		super();
	}
	
	

	@Override
	public boolean equals(Object o) {
		Access a=(Access)o;
		if(Auxiliar.equals(a.getIDTO(),this.getIDTO()) &&
				Auxiliar.equals(a.getIDO(),this.getIDO()) &&
				Auxiliar.equals(a.getPROP(),this.getPROP()) &&
				Auxiliar.equals(a.getVALUE(),this.getVALUE()) &&
				Auxiliar.equals(a.getVALUECLS(),this.getVALUECLS()) &&
				Auxiliar.equals(a.getUSERROL(),this.getUSERROL()) &&
				Auxiliar.equals(a.getUSER(),this.getUSER()) &&
				Auxiliar.equals(a.getTASK(),this.getTASK()) &&
				Auxiliar.equals(a.getTASKNAMES(),this.getTASKNAMES()) &&
				Auxiliar.equals(a.getDENNIED(),this.getDENNIED()) &&
				Auxiliar.equals(a.getACCESSTYPENAME(),this.getACCESSTYPENAME()) &&
				Auxiliar.equals(a.getPRIORITY(),this.getPRIORITY())){
			return true;
		}
		return false;
	}



	public Integer getDENNIED() {
		return DENNIED;
	}

	public void setDENNIED(Integer dennied) {
		DENNIED = dennied;
	}

	public Integer getIDO() {
		return IDO;
	}

	public void setIDO(Integer ido) {
		IDO = ido;
	}

	public Integer getIDTO() {
		return IDTO;
	}

	public void setIDTO(Integer idto) {
		IDTO = idto;
	}

	public Integer[] getPROP() {
		return PROP;
	}

	public void setPROP(Integer[] prop) {
		PROP = prop;
	}

	public void setPROP(Integer prop) {
		if(prop!=null){
			Integer[] propArr=new Integer[1];
			propArr[0]=prop;
			PROP = propArr;
		}
	}

	public Integer getTASK() {
		return TASK;
	}

	public void setTASK(Integer task) {
		TASK = task;
	}



	public String getVALUE() {
		return VALUE;
	}

	public void setVALUE(String value) {
		VALUE = value;
	}

	public Integer getVALUECLS() {
		return VALUECLS;
	}

	public void setVALUECLS(Integer valuecls) {
		VALUECLS = valuecls;
	}

	public Access(Integer idto,Integer ido,Integer prop,String value,Integer valuecls,ArrayList<String> userRol,ArrayList<String> user, Integer usertask,Integer dennied,ArrayList<String> accesstypename, int priority){
		Integer[] propArr=null;
		if(prop!=null){
			propArr=new Integer[1];
			propArr[0]=prop;			
		}
		this.IDTO=idto;
		this.IDO=ido;
		this.PROP=propArr;
		this.VALUE=value;
		this.VALUECLS=valuecls;
		this.USERROL=userRol;
		this.USER=user;
		this.TASK=usertask;
		this.DENNIED=dennied;
		this.ACCESSTYPENAME=accesstypename;
		this.PRIORITY=priority;
		
	}
	
	public Access(Integer idto,Integer ido,Integer[] prop,String value,Integer valuecls,ArrayList<String> userRol,ArrayList<String> user, Integer usertask,Integer dennied,ArrayList<String> accesstypename, int priority){
		this.IDTO=idto;
		this.IDO=ido;
		this.PROP=prop;
		this.VALUE=value;
		this.VALUECLS=valuecls;
		this.USERROL=userRol;
		this.USER=user;
		this.TASK=usertask;
		this.DENNIED=dennied;
		this.ACCESSTYPENAME=accesstypename;
		this.PRIORITY=priority;
	} 
	
	public Access(Integer dennied, Integer task, ArrayList<String> userrol, ArrayList<String> user,ArrayList<String> accesstypename, Integer idto, Integer ido,
			Integer[] prop, String value, Integer valuecls, int priority) {
		
		DENNIED = dennied;
		TASK = task;
		USERROL = userrol;
		USER = user;
		ACCESSTYPENAME = accesstypename;
		IDTO = idto;
		IDO = ido;
		PROP = prop;
		VALUE = value;
		VALUECLS = valuecls;
		PRIORITY=priority;
	}

	public String toString() {
		return "<Access  ido = " + this.IDO + ", idto: " + IDTO + " property: "+ this.PROP+ " ACCESS:" +this.ACCESSTYPENAME +", USERROL: "+this.USERROL+", USERTASK:"+this.TASK+" ,PRIORITY: "+this.getPRIORITY()+" ,DENNIED: "+this.DENNIED+"...|\n";
	}



	public int getPRIORITY() {
		return PRIORITY;
	}



	public void setPRIORITY(int priority) {
		PRIORITY = priority;
	}
	
	public Access clone(){
		Access acc=new Access(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getUSERROL(),this.getUSER(),this.getTASK(),this.getDENNIED(),this.getACCESSTYPENAME(), this.getPRIORITY());
		
		return acc;
	}



	public ArrayList<String> getACCESSTYPENAME() {
		return ACCESSTYPENAME;
	}



	public void setACCESSTYPENAME(ArrayList<String> accesstypename) {
		ACCESSTYPENAME = accesstypename;
	}

	public void setACCESSTYPENAME(String accesstypename) {
		ACCESSTYPENAME = new ArrayList<String>();
		ACCESSTYPENAME.add(accesstypename);
	}

	public ArrayList<String> getUSER() {
		return USER;
	}



	public void setUSER(ArrayList<String> user) {
		USER = user;
	}



	public ArrayList<String> getUSERROL() {
		return USERROL;
	}



	public void setUSERROL(ArrayList<String> userrol) {
		USERROL = userrol;
	}

	public String getAccessType(){
		if(getPROP()!=null && getPROP().length>0)
			return Constants.CLS_ACCESS_PROPERTY;
		else if(getIDTO()!=null)
			return Constants.CLS_ACCESS_CLASS;
		else if(getTASK()!=null || getTASKNAMES()!=null)
			return Constants.CLS_ACCESS_UTASK;
		else return Constants.CLS_ACCESS_GENERIC;
	}



	public ArrayList<String> getTASKNAMES() {
		return TASKNAME;
	}



	public void setTASKNAMES(ArrayList<String> taskname) {
		TASKNAME = taskname;
	}


	public void setFUNCTIONALAREA(ArrayList<Integer> functionalAreas) {
		FUNCTIONALAREA = functionalAreas;
	}
	
	public ArrayList<Integer> getFUNCTIONALAREA(){
		return FUNCTIONALAREA;
	}

}
