package dynagent.common.basicobjects;

import java.util.ArrayList;

public class SReport {
	private Integer idto;
	private String id;
	private String query;
	private String jrxml;
	private String map;
	private String group;
	private boolean directImpresion;
	private boolean preView;
	private int nCopies;
	private boolean displayPrintDialog;
	private ArrayList<String> formatList;
	private String printOrder;

	public SReport(){
		
	}
	public SReport(Integer idto, String id, String query, String jrxml, String map,String group, 
			boolean directImpresion, boolean preView, int nCopies, boolean displayPrintDialog, 
			ArrayList<String> formatList, String printOrder){
		this.id=id;
		this.idto=idto;
		this.jrxml=jrxml;
		this.map=map;
		this.query=query;
		this.group=group;
		this.directImpresion = directImpresion;
		this.preView = preView;
		this.nCopies = nCopies;
		this.displayPrintDialog = displayPrintDialog;
		this.formatList = formatList;
		this.printOrder = printOrder;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getIdto() {
		return idto;
	}
	public void setIdto(Integer idto) {
		this.idto = idto;
	}
	public String getJrxml() {
		return jrxml;
	}
	public void setJrxml(String jrxml) {
		this.jrxml = jrxml;
	}
	public String getMap() {
		return map;
	}
	public void setMap(String map) {
		this.map = map;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public boolean getDirectImpresion() {
		return directImpresion;
	}
	public void setDirectImpresion(boolean directImpresion) {
		this.directImpresion = directImpresion;
	}
	public boolean getDisplayPrintDialog() {
		return displayPrintDialog;
	}
	public void setDisplayPrintDialog(boolean displayPrintDialog) {
		this.displayPrintDialog = displayPrintDialog;
	}
	public int getNCopies() {
		return nCopies;
	}
	public void setNCopies(int copies) {
		nCopies = copies;
	}
	public String getPrintOrder() {
		return printOrder;
	}
	public void setPrintOrder(String printOrder) {
		this.printOrder = printOrder;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public boolean isPreView() {
		return preView;
	}
	public void setPreView(boolean preView) {
		this.preView = preView;
	}
	public ArrayList<String> getFormatList() {
		return formatList;
	}
	public void setFormatList(ArrayList<String> formatList) {
		this.formatList = formatList;
	}
	public String toString(){
		String result="";
		result="(SREPORT (IDTO "+this.idto+") (IDO "+this.id+") (QUERY "+this.query+") (JRXML "+this.jrxml+") (MAP "+this.map+") (DIRECT_IMPRESION "+this.directImpresion+") (PREVIEW "+preView+") (N_COPIES "+this.nCopies+") (DISPLAY_PRINT_DIALOG "+this.displayPrintDialog+")(PRINT_ORDER "+printOrder+"))";
		return result;
	}
}
	
