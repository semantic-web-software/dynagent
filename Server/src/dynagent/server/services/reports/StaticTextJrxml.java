package dynagent.server.services.reports;

public class StaticTextJrxml {
	private String name=null;
	private Integer x=null;
	private Integer y=null;
	private Integer width=null;
	private Integer height=null;
	private String tamLine=null;
	private String backColor=null;
	private String textAlignment=null;
	private Integer sizeFont=null;
	private boolean isBold=false;
	
	public StaticTextJrxml(String name,Integer x, Integer y, Integer width, Integer height,String tamLine, Integer sizeFont, boolean isTitle) {
		this.x=x;
		this.y=y;
		this.width=width;
		this.height=height;

		this.tamLine=tamLine;
		this.sizeFont = sizeFont;
		if (isTitle) {
			this.backColor = "#CCCCCC";
			this.textAlignment = "Center";
			this.isBold = true;
			this.name=name.toUpperCase();
		} else {
			this.backColor = "#000000";
			this.isBold = false;
			this.name=name;
		}
	}
	public StaticTextJrxml(){
		
	}
	
	public Integer getHeight() {
		return height;
	}
	public void setHeight(Integer height) {
		this.height = height;
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
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String toString(){
		/*String cad="<staticText>"+
					"<reportElement ";
		if (!this.backColor.equals("#000000"))
			cad=cad+"mode=\"Opaque\" ";
		cad=cad+"x=\""+this.x+"\" y=\""+this.y+"\" width=\""+this.width+"\" height=\""+this.height+"\"";
		cad=cad+" backcolor=\""+this.backColor+"\"";
		cad=cad+" key=\"staticText-9\"/>"+
		"<box topBorder=\""+this.tamLine+"\" topBorderColor=\"#000000\" leftBorder=\""+this.tamLine+"\" leftBorderColor=\"#000000\" rightBorder=\""+this.tamLine+"\" rightBorderColor=\"#000000\" bottomBorder=\""+this.tamLine+"\" bottomBorderColor=\"#000000\"/>"+
		"	<textElement>"+
		"		<font size=\"8\"/>"+
		"	</textElement>";
		cad=cad+"<text><![CDATA["+this.name+"]]></text></staticText>";
		return cad;*/
		
		String cad="<staticText>"+
		"<reportElement ";
		if (!this.backColor.equals("#000000"))
			cad=cad+"mode=\"Opaque\" ";
		else
			cad=cad+"mode=\"Transparent\" ";
		cad=cad+"x=\""+this.x+"\" y=\""+this.y+"\" width=\""+this.width+"\" height=\""+this.height+"\"";
		cad=cad+" backcolor=\""+this.backColor+"\"";
		cad=cad+" key=\"staticText-9\" positionType=\"Float\"/>"+
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
		if (this.isBold)
			cad=cad+"		<font pdfFontName=\"Helvetica-Bold\" size=\""+ sizeFont + "\" isBold=\"true\"/>";
		else
			cad=cad+"		<font pdfFontName=\"Helvetica\" size=\""+ sizeFont + "\" isBold=\"false\"/>";
		cad=cad+"	</textElement>";
		cad=cad+"<text><![CDATA["+this.name+"]]></text></staticText>";
		return cad;

	}
	
	public String getTamLine() {
		return tamLine;
	}
	public void setTamLine(String tamLine) {
		this.tamLine = tamLine;
	}
}
