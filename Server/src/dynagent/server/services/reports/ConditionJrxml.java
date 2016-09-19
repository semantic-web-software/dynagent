package dynagent.server.services.reports;

import dynagent.common.utils.QueryConstants;

public class ConditionJrxml {
	private StaticTextJrxml staticTextResult=null;
	
	private StaticTextJrxml staticText=null;
	private String operation=null;
	public ConditionJrxml(){
		
	}
	public ConditionJrxml(String result,String result2, String nameCond, String operation, String nameClass, Integer sizeFont){
		
		this.operation=operation;
		String name=null;
		if(nameClass!=null){
			//name=nameClass+"-"+nameCond;
			name=nameCond + " de " + nameClass;
		}else{
			name=nameCond;
		}
		if(nameCond==null){
			name=nameClass;
		}
		boolean isTitle = false;
		if(this.operation.equals(QueryConstants.LIKE) || this.operation.equals(QueryConstants.IGUAL) ){
			this.staticText= new StaticTextJrxml(name+"="+result,null,null,null, null,"0.0",sizeFont,isTitle);
			//this.staticTextResult=new StaticTextJrxml(result,null,null,null, null,"None");
		}
		if(this.operation.equals(QueryConstants.DISTINTO) || this.operation.equals(QueryConstants.DISTINTO_VALIDO) ){
			this.staticText= new StaticTextJrxml(name+"!="+result,null,null,null, null,"0.0",sizeFont,isTitle);
			//this.staticTextResult=new StaticTextJrxml(result,null,null,null, null,"None");
		}
		if(this.operation.equals(QueryConstants.MAYOR)){
			this.staticText= new StaticTextJrxml(name+">"+result,null,null,null, null,"0.0",sizeFont,isTitle);
			//this.staticTextResult=new StaticTextJrxml(result,null,null,null, null,"None");
		}
		if(this.operation.equals(QueryConstants.MENOR)){
			this.staticText= new StaticTextJrxml(name+"<"+result,null,null,null, null,"0.0",sizeFont,isTitle);
			//this.staticTextResult=new StaticTextJrxml(result,null,null,null, null,"None");
		}
		if(this.operation.equals(QueryConstants.MENOR_IGUAL)){
			this.staticText= new StaticTextJrxml(name+"<="+result,null,null,null, null,"0.0",sizeFont,isTitle);
			//this.staticTextResult=new StaticTextJrxml(result,null,null,null, null,"None");
		}
		if(this.operation.equals(QueryConstants.MAYOR_IGUAL)){
			this.staticText= new StaticTextJrxml(name+">="+result,null,null,null, null,"0.0",sizeFont,isTitle);
			//this.staticTextResult=new StaticTextJrxml(result,null,null,null, null,"None");
		}
		if(this.operation.equals(QueryConstants.CONTAINS)){
			this.staticText= new StaticTextJrxml(name+" contiene "+result,null,null,null, null,"0.0",sizeFont,isTitle);
			//this.staticTextResult=new StaticTextJrxml(result,null,null,null, null,"None");
		}
		if(this.operation.equals(QueryConstants.BETWEEN)){
			this.staticText= new StaticTextJrxml(name+" entre "+"("+result+","+result2+")",null,null,null, null,"0.0",sizeFont,isTitle);
			//this.staticTextResult=new StaticTextJrxml("("+result+","+result2+")",null,null,null, null,"None");
		}
		
	}
	
	public void setPositionTextResult(Integer x,Integer y,Integer width,Integer height){
		this.staticTextResult.setHeight(height);
		this.staticTextResult.setWidth(width);
		this.staticTextResult.setX(x);
		this.staticTextResult.setY(y);
	}
	
	public void setPositionText(Integer x,Integer y,Integer width,Integer height){
		this.staticText.setHeight(height);
		this.staticText.setWidth(width);
		this.staticText.setX(x);
		this.staticText.setY(y);
	}
	
	public String toString(){
		return this.staticText.toString();
	}

	public StaticTextJrxml getStaticText() {
		return staticText;
	}

	public void setStaticText(StaticTextJrxml staticText) {
		this.staticText = staticText;
	}

	public StaticTextJrxml getStaticTextResult() {
		return staticTextResult;
	}

	public void setStaticTextResult(StaticTextJrxml staticTextResult) {
		this.staticTextResult = staticTextResult;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}

}
