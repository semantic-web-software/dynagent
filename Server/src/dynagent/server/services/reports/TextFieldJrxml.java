package dynagent.server.services.reports;

public class TextFieldJrxml {
	private Integer x=null;
	private Integer y=null;
	private Integer width=null;
	private Integer height=null;
	private TextFieldExpression textFieldExp=null;
	
	private Integer sizeFont;
	private String tamLine;
	private String textAlignment; //Right,Center
	private String pattern;
	
	public TextFieldJrxml(FieldJrxml cond, Integer x, Integer y, Integer width, Integer height,String tamLine, String textAlignment, String pattern, Integer sizeFont) {
		this.textFieldExp=new TextFieldExpression(cond);
		this.x=x;
		this.y=y;
		this.width=width;
		this.height=height;
	
		this.tamLine=tamLine;
		this.textAlignment = textAlignment;
		this.pattern = pattern;
		this.sizeFont = sizeFont;
	}
	public TextFieldJrxml(String expression, String txtClass, Integer x, Integer y, Integer width, Integer height,String tamLine, String textAlignment, String pattern, Integer sizeFont) {
		this.textFieldExp=new TextFieldExpression(expression, txtClass);
		this.x=x;
		this.y=y;
		this.width=width;
		this.height=height;
	
		this.tamLine=tamLine;
		this.textAlignment = textAlignment;
		this.pattern = pattern;
		this.sizeFont = sizeFont;
	}
	public TextFieldJrxml(){
		
	}
	
	public Integer getHeight() {
		return height;
	}
	public void setHeight(Integer height) {
		this.height = height;
	}
	public TextFieldExpression getTextFieldExp() {
		return textFieldExp;
	}
	public void setTextFieldExp(TextFieldExpression textFieldExp) {
		this.textFieldExp = textFieldExp;
	}
	public Integer getWidth() {
		return width;
	}
	public void setWidth(Integer width) {
		this.width = width;
	}
	public Integer getX() {
		return x;
	}
	public void setX(Integer x) {
		this.x = x;
	}
	public Integer getY() {
		return y;
	}
	public void setY(Integer y) {
		this.y = y;
	}
	public String toString(){
		String cad="<textField isStretchWithOverflow=\"true\"";
		if (this.pattern!=null)
			cad=cad+" pattern=\""+this.pattern+"\"";
		cad=cad+" isBlankWhenNull=\"true\" evaluationTime=\"Now\" hyperlinkType=\"None\"  hyperlinkTarget=\"Self\" >"+
					"<reportElement ";
		cad=cad+"x=\""+this.x+"\" y=\""+this.y+"\" width=\""+this.width+"\" height=\""+this.height+"\"";
		cad=cad+" key=\"textField\"/>"+
		"	<box>"+
		"		<topPen lineWidth=\""+this.tamLine+"\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
		"		<leftPen lineWidth=\""+this.tamLine+"\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
		"		<bottomPen lineWidth=\""+this.tamLine+"\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
		"		<rightPen lineWidth=\""+this.tamLine+"\" lineStyle=\"Solid\" lineColor=\"#000000\"/>"+
		"	</box>";
		
		if (this.textAlignment==null || this.textAlignment.equals("Left"))
			cad=cad+"	<textElement verticalAlignment=\"Middle\">";
		else
			cad=cad+"	<textElement textAlignment=\""+this.textAlignment+"\" verticalAlignment=\"Middle\">";
		cad=cad+"		<font pdfFontName=\"Helvetica\" size=\"" + sizeFont + "\" isBold=\"false\"/>" +
				"	</textElement>";
		cad=cad+this.textFieldExp.toString();
		cad=cad+"</textField>";
		
		return cad;
	}

	public String getTamLine() {
		return tamLine;
	}
	public void setTamLine(String tamLine) {
		this.tamLine = tamLine;
	}

}
