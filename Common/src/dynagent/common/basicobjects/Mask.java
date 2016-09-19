package dynagent.common.basicobjects;

public class Mask {
	private Integer uTask=null;
	private Integer idto=null;
	private Integer prop=null;
	private Integer length=null;
	private String expression=null;
	
	private String uTaskName=null;
	private String idtoName=null;
	private String propName=null;
	
	public Mask(Integer uTask, Integer idto, Integer prop, String expression, Integer length){
		this.uTask=uTask;
		this.idto=idto;
		this.prop=prop;
		this.expression=expression;
		this.length=length;
	}
	
	public Mask(){
		
	}
			
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public Integer getIdto() {
		return idto;
	}
	public void setIdto(Integer idto) {
		this.idto = idto;
	}
	public String getIdtoName() {
		return idtoName;
	}
	public void setIdtoName(String idtoName) {
		this.idtoName = idtoName;
	}
	public Integer getLength() {
		return length;
	}
	public void setLength(Integer length) {
		this.length = length;
	}
	public Integer getProp() {
		return prop;
	}
	public void setProp(Integer prop) {
		this.prop = prop;
	}
	public String getPropName() {
		return propName;
	}
	public void setPropName(String propName) {
		this.propName = propName;
	}
	public Integer getUTask() {
		return uTask;
	}
	public void setUTask(Integer task) {
		uTask = task;
	}
	public String getUTaskName() {
		return uTaskName;
	}
	public void setUTaskName(String taskName) {
		uTaskName = taskName;
	}
	
	@Override
	public Mask clone() throws CloneNotSupportedException {
		Mask m=new Mask();
		m.setExpression(expression);
		m.setLength(length);
		m.setIdto(idto);
		m.setIdtoName(idtoName);
		m.setProp(prop);
		m.setPropName(propName);
		m.setUTask(uTask);
		m.setUTaskName(uTaskName);
		return m;
	}
	
	public String toString(){
		return "(MASK (EXPRESSION "+this.getExpression()+") (LENGTH "+this.getLength()+") (CLASS "+this.getIdtoName()+") (PROP "+this.getPropName()+") (UTASK "+this.getUTaskName()+"))";
	}


}
