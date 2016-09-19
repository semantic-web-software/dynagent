package dynagent.common.basicobjects;

import java.util.ArrayList;
import java.util.HashMap;

import dynagent.common.Constants;

public class UTask {
	public static final int UTASK=0;
	//public static final int ACTION=1;No implementado para gestionarlo en Auxiliar.insertUtask
	public static final int IMPORT=2;
	public static final int EXPORT=3;
	
	private String utaskName=null;
	private Integer targetClass=null;
	private String targetClassName=null;
	private String areaFuncName=null;
	private Integer idtoAreaFunc=null;
	private String sourceClassName=null;
	private Integer sourceClass=null;
	private Integer cminTGC=null;
	private Integer cmaxTGC=null;
	private Integer cminSC=null;
	private Integer cmaxSC=null;
	private Integer cminP=null;
	private Integer cmaxP=null;
	private String paramsName=null;
	private Integer idtoParams=null;
	private ArrayList<String> aURoles=null;
	private boolean listener=false;
	private int type=UTASK;
	private HashMap<String,ArrayList<String>> help=new HashMap<String, ArrayList<String>>();
	private boolean global=false;
	
	public ArrayList<String> getAURoles() {
		return aURoles;
	}
	
	public void setAURoles(ArrayList<String> roles) {
		aURoles = roles;
	}

	public Integer getCmaxP() {
		return cmaxP;
	}

	public void setCmaxP(Integer cmaxP) {
		this.cmaxP = cmaxP;
	}

	public Integer getCminP() {
		return cminP;
	}

	public void setCminP(Integer cminP) {
		this.cminP = cminP;
	}

	public Integer getIdtoParams() {
		return idtoParams;
	}

	public void setIdtoParams(Integer idtoParams) {
		this.idtoParams = idtoParams;
	}

	public String getParamsName() {
		return paramsName;
	}

	public void setParamsName(String paramsName) {
		this.paramsName = paramsName;
	}

	public Integer getCmaxSC() {
		return cmaxSC;
	}

	public void setCmaxSC(Integer cmaxSC) {
		this.cmaxSC = cmaxSC;
	}

	public Integer getCmaxTGC() {
		return cmaxTGC;
	}

	public void setCmaxTGC(Integer cmaxTGC) {
		this.cmaxTGC = cmaxTGC;
	}

	public Integer getCminSC() {
		return cminSC;
	}

	public void setCminSC(Integer cminSC) {
		this.cminSC = cminSC;
	}

	public Integer getCminTGC() {
		return cminTGC;
	}

	public void setCminTGC(Integer cminTGC) {
		this.cminTGC = cminTGC;
	}

	public UTask(){}
	
	public String getAreaFuncName() {
		return areaFuncName;
	}
	public void setAreaFuncName(String areaFuncName) {
		this.areaFuncName = areaFuncName;
	}
	public Integer getSourceClass() {
		return sourceClass;
	}
	public void setSourceClass(Integer sourceClass) {
		this.sourceClass = sourceClass;
	}
	public String getSourceClassName() {
		return sourceClassName;
	}
	public void setSourceClassName(String sourceClassName) {
		this.sourceClassName = sourceClassName;
	}
	public Integer getTargetClass() {
		return targetClass;
	}
	public void setTargetClass(Integer targetClass) {
		this.targetClass = targetClass;
	}
	public String getTargetClassName() {
		return targetClassName;
	}
	public void setTargetClassName(String targetClassName) {
		this.targetClassName = targetClassName;
	}
	public String getUtaskName() {
		return utaskName;
	}
	public void setUtaskName(String utaskName) {
		this.utaskName = utaskName;
	}

	public Integer getIdtoAreaFunc() {
		return idtoAreaFunc;
	}

	public void setIdtoAreaFunc(Integer idtoAreaFunc) {
		this.idtoAreaFunc = idtoAreaFunc;
	}
	
	public String toString(){
		return "(UTASK (AREA_FUNC "+this.areaFuncName+")(SOURCE_CLASS "+this.sourceClassName+")(TARGET_CLASS "+this.targetClassName+
		")(NAME "+this.utaskName+")(PARAMS "+this.paramsName+"))";
	}

	public boolean isListener() {
		return listener;
	}

	public void setListener(boolean listener) {
		this.listener = listener;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public HashMap<String,ArrayList<String>> getHelp() {
		return help;
	}

	public void addHelp(String language,String description) {
		if(!help.containsKey(language))
			this.help.put(language, new ArrayList<String>());
		
		this.help.get(language).add(description);
	}
	
	public String getUTaskType(){
		String utaskType=null;
		switch(this.type){
			case UTASK:
				utaskType=Constants.CLS_MENU;
				break;
			case IMPORT:
				utaskType=Constants.CLS_IMPORT;
				break;
			case EXPORT:
				utaskType=Constants.CLS_EXPORT;
				break;
		}
		return utaskType;
	}

	public void setGlobal(boolean global) {
		this.global=global;
	}

	public boolean isGlobal() {
		return this.global;
	}
	
}
