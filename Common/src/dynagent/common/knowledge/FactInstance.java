package dynagent.common.knowledge;

import java.io.Serializable;

	public class FactInstance implements IPropertyDef,Serializable  {

	 private static final long serialVersionUID = 4942554744717992037L;
	
	 String CLASSNAME=null;
	 Integer IDTO=null;	    
	 Integer IDO=null;	    
	 Integer PROP=null;  
	 String VALUE=null;
	 Integer VALUECLS=null;
	 Double QMIN=null ;
	 Double QMAX=null;
	 String OP=null;
	 int operacion;
	 FactInstance valorInicial=null;
	 private boolean existia_BD=false;
	 String RANGENAME=null;
	 String systemValue;
	 boolean appliedSystemValue=false;
	 boolean incremental=false;
	 String destinationSystem=null;
	 String rdn=null;
	 String rdnValue=null;
		
	//TODO MUY IMPORTANTE QUITAR ESTE CONSTRUCTOR, QUE SERÁ SUSTITUIDO POR EL SIGUIENTE MÁS COMPLETO
	public FactInstance(Integer idto,Integer ido,Integer prop,String value,Integer valueCls,Double qmin,Double qmax,String op,String classname){
		if(classname!=null)
			CLASSNAME = new String(classname);
		if(value!=null)
			VALUE = new String(value);
		IDTO = idto;
		IDO = ido;
		PROP = prop;
		VALUECLS = valueCls;
		QMIN = qmin;
		QMAX = qmax;
		OP = op;
	}
	
	
	
	public FactInstance(Integer idto,Integer ido,Integer prop,String value,Integer valueCls,String rangename,Double qmin,Double qmax,String op,String classname){
		CLASSNAME = classname;
		IDTO = idto;
		IDO = ido;
		PROP = prop;
		VALUE = value;
		VALUECLS = valueCls;
		RANGENAME=rangename;
		QMIN = qmin;
		QMAX = qmax;
		OP = op;
	}
	

	public FactInstance(Integer idto,Integer ido,Integer prop,String value,Integer valueCls,String rangename,Double qmin,Double qmax,String op,String classname,int operation){
		CLASSNAME = classname;
		IDTO = idto;
		IDO = ido;
		PROP = prop;
		VALUE = value;
		VALUECLS = valueCls;
		RANGENAME=rangename;
		QMIN = qmin;
		QMAX = qmax;
		OP = op;
		this.operacion = operation;
	}
	


	public FactInstance() {
		// TODO Auto-generated constructor stub
	}


	public String getRANGENAME() {
		return RANGENAME;
	}

	public void setRANGENAME(String rangename) {
		RANGENAME = rangename;
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
	public String getCLASSNAME() {
		return CLASSNAME;
	}
	public void setCLASSNAME(String name) {
		CLASSNAME = name;
	}
	public String getOP() {
		return OP;
	}
	public void setOP(String op) {
		OP = op;
	}
	public int getPROP() {
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
	
	public String toQueryString(){
		String stringfact = "";
		stringfact += "(FactInnstance ";
		stringfact += "( CLASSNAME " + this.getCLASSNAME() + " )";
		stringfact += "( ORDER " + this.getOrder()+ " )";
		stringfact += "( OPERACION " + this.getOperacion()+ " )";
		stringfact += "( IDO " + this.getIDO() + " )";
		stringfact += "( IDTO " + this.getIDTO() + " )";
		stringfact += "( PROP " + this.getPROP() + " )";
		stringfact += "(VALUE \"" +this.getVALUE()+ "\")";
		stringfact += "( VALUECLS " + this.getVALUECLS() + " )";
		stringfact += "( QMIN " + this.getQMIN() + " )";
		stringfact += "( QMAX " + this.getQMAX() + " )";
		stringfact += "( OP " + this.getOP() + " )";
		stringfact += "( EXISTIA_BD " + this.getExistia_BD() + " )";
		stringfact += "( RANGENAME " + this.getRANGENAME()+ " )";
		stringfact += "( SYSTEM_VALUE " + this.getSystemValue() + " )";
		stringfact += "( APPLIED_SYSTEM_VALUE " + this.isAppliedSystemValue() + " )";
		stringfact += "( INITIAL_VALUES " + this.getInitialValues() + " )";
		stringfact += "( INCREMENTAL " + this.isIncremental() + " )";
		stringfact += "( DESTINATION_SYSTEM " + this.getDestinationSystem() + " )";
		stringfact += "( RDN " + this.getRdn() + " )";
		stringfact += "( RDNVALUE " + this.getRdnValue() + " )";
		stringfact += ")";
		return stringfact;
	}
	
	
	public String toString(){
		return this.toQueryString();
	}
	
	public String toInstanceString(){
		String stringfact = "";
		
		stringfact += "\n\t (instance ";
		stringfact += "( NAME " + this.getCLASSNAME() + " )";
		
		if(this.getIDO() == null)
			stringfact += "( IDO nil )";
		else
			stringfact += "( IDO " + this.getIDO() + " )";
	
		if(this.getIDTO() == null)
			stringfact += "( IDTO nil )";
		else
			stringfact += "( IDTO " + this.getIDTO() + " )";
		
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

		stringfact += "( OPERACION " + this.getOperacion() + " )";
		stringfact += "( EXISTIA_BD " + this.getExistia_BD() + " )";
		stringfact += "( SYSTEM_VALUE " + this.getSystemValue() + " )";

		if(this.getInitialValues() != null)
			stringfact += "( INITIAL_VALUES " + this.getInitialValues() + " )";
		else
			stringfact += "( INITIAL_VALUES nil )";
		
		stringfact += "( INCREMENTAL " + this.isIncremental() + " )";
		
		if(this.getDestinationSystem() != null)
			stringfact += "( DESTINATION_SYSTEM " + this.getDestinationSystem() + " )";
		else
			stringfact += "( DESTINATION_SYSTEM nil )";
		
		if(this.getRdn() != null){
			stringfact += "( RDN " + this.getRdn() + " )";
		}
		
		if(this.getRdnValue() != null){
			stringfact += "( RDNVALUE " + this.getRdnValue() + " )";
		}
		
		stringfact += ")";
	
		return stringfact;
	}
	
	
	public FactInstance toFactInstance()
	{
		return this;
	}
	
	
	  public int getOrder() {
			
			return this.operacion;
		}

	public boolean getExistia_BD() {
	
		return this.existia_BD;
	}


	public void setExistia_BD(boolean existia_BDD) {
		this.existia_BD = existia_BDD;
		
	}

// UN FactInstance nunca se debe sincronizar con la base de datos
	public boolean syncWithDataBase() {
		return false;
	}

	public IPropertyDef getInitialValues() {
		
		return this.valorInicial;
	}
	public void setInitialValues(IPropertyDef f)
	{
	this.valorInicial= (FactInstance)f;	
	}

	public int getOperacion() {
		return operacion;
	}
	public boolean isequal(IPropertyDef f){
		boolean resultado=false;
		if(f!=null){
			resultado=(this.getIDO()==f.getIDO()||(this.getIDO()!=null&&f.getIDO()!=null&&this.getIDO().intValue()==f.getIDO().intValue()))&&
			(this.getIDTO()==f.getIDTO()||(this.getIDTO()!=null&&f.getIDTO()!=null&&this.getIDTO().intValue()==f.getIDTO().intValue()))&&
			(this.getPROP()==f.getPROP())&&
			(this.getVALUECLS()==f.getVALUECLS()||(this.getVALUECLS()!=null&&f.getVALUECLS()!=null&&this.getVALUECLS().intValue()==f.getVALUECLS().intValue()))&&
			(this.getVALUE()==f.getVALUE()||(this.getVALUE()!=null&&f.getVALUE()!=null&&this.getVALUE().equals(f.getVALUE())))&&
			(this.getOP()==f.getOP()||(this.getOP()!=null&&f.getOP()!=null&&this.getOP().equals(f.getOP())))&&
			(this.getQMAX()==f.getQMAX()||(this.getQMAX()!=null&&f.getQMAX()!=null&&this.getQMAX().doubleValue()==f.getQMAX().doubleValue()))&&
			(this.getQMIN()==f.getQMIN()||(this.getQMIN()!=null&&f.getQMIN()!=null&&this.getQMIN().doubleValue()==f.getQMIN().doubleValue()));
		}
		 return resultado;
		 
	}


	public void setOperacion(int order) {
		this.operacion = order;
	}


	public void setOrder(int order) {
		this.operacion = order;
	}
	
	public boolean isTemporalDeleted(){
		return this.getVALUE()==null&&this.getQMAX()==null&&this.getQMIN()==null&&this.getOP()==null;
	}
	
	public FactInstance clone(){
		FactInstance fi = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		fi.setOperacion(this.getOperacion());
		fi.setExistia_BD(this.getExistia_BD());
		fi.setSystemValue(this.getSystemValue());
		fi.setAppliedSystemValue(this.isAppliedSystemValue());
		fi.setDestinationSystem(this.getDestinationSystem());
		fi.setIncremental(this.isIncremental());
		IPropertyDef initialValues=this.getInitialValues();
		if(initialValues!=null)
			fi.setInitialValues(((FactInstance)initialValues).clone());
		fi.setRdn(this.getRdn());
		fi.setRdnValue(this.getRdnValue());
		return fi;
	}

	public boolean initialValuesChanged() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getSystemValue() {
		return systemValue;
	}

	public void setSystemValue(String systemValue) {
		this.systemValue=systemValue;
	}

	public boolean isIncremental() {
		return incremental;
	}
	public void setIncremental(boolean incremental) {
		this.incremental = incremental;
	}

	public String getDestinationSystem() {
		return destinationSystem;
	}
	public void setDestinationSystem(String destinationSystem) {
		this.destinationSystem = destinationSystem;
	}

	/*public String getRdnValue() {
		return rdnValue;
	}
	public void setRdnValue(String rdnValue) {
		this.rdnValue = rdnValue;
	}*/

	public boolean isAppliedSystemValue() {
		return appliedSystemValue;
	}
	public void setAppliedSystemValue(boolean appliedSystemValue) {
		this.appliedSystemValue=appliedSystemValue;
	}

	public void setRdn(String rdn) {
		this.rdn=rdn;
	}

	public String getRdn() {
		return this.rdn;
	}

	public String getRdnValue() {
		return rdnValue;
	}

	public void setRdnValue(String rdnValue) {
		this.rdnValue = rdnValue;
	}
}
