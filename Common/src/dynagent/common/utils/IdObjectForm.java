package dynagent.common.utils;

public class IdObjectForm {

	private Integer idtoUserTask;
	private Integer ido;
	private Integer idto;
	private Integer idProp;
	private Integer value;
	private Integer valueCls;
	private Integer filterIdProp;
	private String filterValue;
	private String idParent;
	
	private static final String SEPARATOR=":";
	
	public IdObjectForm(){
		this("null"+SEPARATOR+"null"+SEPARATOR+"null"+SEPARATOR+"null"+SEPARATOR+"null"+SEPARATOR+"null"+SEPARATOR+"null"+SEPARATOR+"null"+SEPARATOR+"null");
	}
	public IdObjectForm(String id) {
		String[] buf = id.split(SEPARATOR);
		
		idtoUserTask = !buf[0].equals("null") ? Integer.parseInt(buf[0]): null;
		ido = !buf[1].equals("null") ? Integer.parseInt(buf[1]): null;
		idto = !buf[2].equals("null") ? Integer.parseInt(buf[2]): null;
		idProp = !buf[3].equals("null") ? Integer.parseInt(buf[3]): null;
		value = !buf[4].equals("null") ? Integer.parseInt(buf[4]): null;
		valueCls = !buf[5].equals("null") ? Integer.parseInt(buf[5]): null;
		filterIdProp = !buf[6].equals("null") ? Integer.parseInt(buf[6]): null;
		filterValue = !buf[7].equals("null") ? buf[7]: null;
		idParent = !buf[8].equals("null") ? buf[8]:null;
	}
	
	public static boolean matchFormat(String id){
//		try{
//			String[] buf = id.split(SEPARATOR);
//			
//			Integer idtoUserTask = !buf[0].equals("null") ? Integer.parseInt(buf[0]): null;
//			Integer ido = !buf[1].equals("null") ? Integer.parseInt(buf[1]): null;
//			idto = !buf[2].equals("null") ? Integer.parseInt(buf[2]): null;
//			idProp = !buf[3].equals("null") ? Integer.parseInt(buf[3]): null;
//			valueCls = !buf[4].equals("null") ? Integer.parseInt(buf[4]): null;
//			filterIdProp = !buf[5].equals("null") ? Integer.parseInt(buf[5]): null;
//			filterValue = !buf[6].equals("null") ? buf[6]: null;
//		}catch(Exception ex){
//			return false;
//		}
//		return true;
		return (id.split(SEPARATOR).length==9?true:false);
	}

	public Integer getIdo() {
		return ido;
	}

	public void setIdo(Integer ido) {
		this.ido = ido;
	}

	public Integer getIdProp() {
		return idProp;
	}

	public void setIdProp(Integer idProp) {
		this.idProp = idProp;
	}

	public Integer getIdtoUserTask() {
		return idtoUserTask;
	}

	public void setIdtoUserTask(Integer idtoUserTask) {
		this.idtoUserTask = idtoUserTask;
	}

	public Integer getValueCls() {
		return valueCls;
	}

	public void setValueCls(Integer valueCls) {
		this.valueCls = valueCls;
	}
	
	public String getIdString(){
		return idtoUserTask+SEPARATOR+ido+SEPARATOR+idto+SEPARATOR+idProp+SEPARATOR+value+SEPARATOR+valueCls+SEPARATOR+filterIdProp+SEPARATOR+filterValue+SEPARATOR+idParent;
	}
	public String getFilterValue() {
		return filterValue;
	}
	public void setFilterValue(String filterValue) {
		this.filterValue = filterValue;
	}
	public Integer getFilterIdProp() {
		return filterIdProp;
	}
	public void setFilterIdProp(Integer filterIdProp) {
		this.filterIdProp = filterIdProp;
	}
	public Integer getIdto() {
		return idto;
	}
	public void setIdto(Integer idto) {
		this.idto = idto;
	}
	@Override
	public String toString() {
		return (idtoUserTask!=null?"idtoUserTask:"+idtoUserTask:"")+
				(ido!=null?",ido:"+ido:"")+
				(idto!=null?",idto:"+idto:"")+
				(idProp!=null?",idProp:"+idProp:"")+
				(value!=null?",value:"+value:"")+
				(valueCls!=null?",valueCls:"+valueCls:"")+
				(filterIdProp!=null?",filterIdProp:"+filterIdProp:"")+
				(filterValue!=null?",filterValue:"+filterValue:"")+
				(idParent!=null?",idParent:"+idParent:"");
	}
	public String getIdParent() {
		return idParent;
	}
	public void setIdParent(String idParent) {
		this.idParent = idParent;
	}
	public Integer getValue() {
		return value;
	}
	public void setValue(Integer value) {
		this.value = value;
	}
	

}
