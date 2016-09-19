package dynagent.ruleengine.src.ruler.queryDef;

import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IPropertyDef;

public class QueryInstanceDef {
	
	 public String NAME=null;
	 public Integer IDTO=null;	    
	 public Integer IDO=null;	    
	 public Integer PROP=null;  
	 public String VALUE=null;
	 public Integer VALUECLS=null;
	 public Double QMIN=null ;
	 public Double QMAX=null;
	 public String OP=null;
	
	 
	 private int searchByIdto=-1;
	 private int searchByIdo=-1;
	 private int searchByProp=-1;
	 private int searchByValue=-1;
	 private int searchByValueCls=-1;
	 private int searchByOp=-1;
	 private int searchByqMax=-1;
	 private int searchByqMin=-1;
	 private int searchByName=-1;
	 private int toDelete = 0;
	 
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
	public String getNAME() {
		return NAME;
	}
	public void setNAME(String name) {
		NAME = name;
	}
	public String getOP() {
		return OP;
	}
	public void setOP(String op) {
		OP = op;
	}
	public Integer getPROP() {
		return PROP;
	}
	public void setPROP(Integer prop) {
		PROP = prop;
	}
	public Double getQMAX() {
		return QMAX;
	}
	public void setQMAX(Double qmax) {
		QMAX = qmax;
	}
	public Double getQMIN() {
		return QMIN;
	}
	public void setQMIN(Double qmin) {
		QMIN = qmin;
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

	public int getSearchByIdo() {
		return searchByIdo;
	}
	public void setSearchByIdo(int searchByIdo) {
		this.searchByIdo = searchByIdo;
	}
	public int getSearchByIdto() {
		return searchByIdto;
	}
	public void setSearchByIdto(int searchByIdto) {
		this.searchByIdto = searchByIdto;
	}
	public int getSearchByName() {
		return searchByName;
	}
	public void setSearchByName(int searchByName) {
		this.searchByName = searchByName;
	}
	public int getSearchByOp() {
		return searchByOp;
	}
	public void setSearchByOp(int searchByOp) {
		this.searchByOp = searchByOp;
	}
	public int getSearchByProp() {
		return searchByProp;
	}
	public void setSearchByProp(int searchByProp) {
		this.searchByProp = searchByProp;
	}
	public int getSearchByqMax() {
		return searchByqMax;
	}
	public void setSearchByqMax(int searchByqMax) {
		this.searchByqMax = searchByqMax;
	}
	public int getSearchByqMin() {
		return searchByqMin;
	}
	public void setSearchByqMin(int searchByqMin) {
		this.searchByqMin = searchByqMin;
	}
	public int getSearchByValue() {
		return searchByValue;
	}
	public void setSearchByValue(int searchByValue) {
		this.searchByValue = searchByValue;
	}
	public int getSearchByValueCls() {
		return searchByValueCls;
	}
	public void setSearchByValueCls(int searchByValueCls) {
		this.searchByValueCls = searchByValueCls;
	}

	public int getToDelete() {
		return toDelete;
	}
	public void setToDelete(int toDelete) {
		this.toDelete = toDelete;
	}
	/**
	 * @deprecated
	 */

	
	public String toInstanceString() {
		String stringfact = "";
		
		stringfact += "\n\t (instance ";
		stringfact += "( NAME " + this.getNAME() + " )";
		
		if(this.getIDO() == null)
			stringfact += "( IDO nil )";
		else
			stringfact += "( IDO " + this.getIDO() + " )";
		
		
		if(this.getIDTO() == null)
			stringfact += "( IDTO nil )";
		else
			stringfact += "( IDTO " + this.getIDTO() + " )";
		
		if(this.getPROP() == null)
			stringfact += "( PROP nil )";
		else
			stringfact += "( PROP " + this.getPROP() + " )";
			
		if(this.getVALUE() == null)
			stringfact += "( VALUE nil )";
		else
			stringfact += "( VALUE " + this.getVALUE() + " )";
		
		
		if(this.getVALUECLS() == null)
			stringfact += "( VALUECLS nil )";
		else{
			if(this.getVALUECLS()==null)
				stringfact += "( VALUECLS nil )";
			else
				stringfact += "( VALUECLS " + this.getVALUECLS() + " )";
		}
			
		if(this.getQMIN() == null)
			stringfact += "( QMIN nil )";
		else
			stringfact += "( QMIN " + this.getQMIN() + " )";
		
		if(this.getQMAX() == null)
			stringfact += "( QMAX nil )";
		else
			stringfact += "( QMAX " + this.getQMAX() + " )";
		
		
		if(this.getOP() == null)
			stringfact += "( OP nil )";
		else
			stringfact += "( OP " + this.getOP() + " )";
	
		return stringfact;
	}
	
	public String toString(){
		return "Query Instance: IDTO: "+IDTO+",IDO: "+IDO+",PROP: "+PROP+",NAME: "+NAME+",VALUE: "+VALUE+", VALUECLS: "+VALUECLS+",QMAX: "+QMAX+",QMIN: "+QMIN+", OP: "+OP+"SearchByIdto="+searchByIdto+",SearchByIdo="+searchByIdo+
		",searchByProp="+searchByProp+",searchByName="+searchByName+",searchByValue="+searchByValue+",searchByValueCls="+searchByValueCls+",searchByOp="+searchByOp+",searchByQMin="+searchByqMin+",searchByQmax="+searchByqMax;
	}
	 
	 
}
