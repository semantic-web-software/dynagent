package dynagent.server.services.reports;

public class TextFieldExpression {
	private String expression=null;
	private String txtClass=null;
	
	public TextFieldExpression(FieldJrxml cond){
		this.expression="$F{"+cond.getName()+"}";
		this.txtClass=cond.getFieldClass();
	}
	public TextFieldExpression(String expression, String txtClass){
		this.expression=expression;
		this.txtClass=txtClass;
	}
	public TextFieldExpression(){
		
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getTxtClass() {
		return txtClass;
	}

	public void setTxtClass(String txtClass) {
		this.txtClass = txtClass;
	}
	public String toString(){
		String result = "";
		if (this.txtClass.equals("Date"))
			result = "<textFieldExpression   class=\"java.util.Date\"><![CDATA["+this.expression+"]]></textFieldExpression>";
		else
			result = "<textFieldExpression   class=\"java.lang."+this.txtClass+"\"><![CDATA["+this.expression+"]]></textFieldExpression>";
		return result;
	}
	
}
