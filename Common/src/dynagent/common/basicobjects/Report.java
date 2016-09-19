package dynagent.common.basicobjects;

import java.io.File;
import java.util.ArrayList;

public class Report {
	private String name;
	private String group;
	private boolean directImpresion;
	private boolean preView;
	private int nCopies = 1;
	private boolean displayPrintDialog;
	private ArrayList<String> formatList;
	private ArrayList<SubReport> subReportList;
	private ArrayList<Param> paramList;
	private boolean generateExcel;
	private String prePrint;
	private String postPrint;
	private String printerName;
	private ArrayList<String> functionalAreaList;
	private String comments;
	private boolean printConfirmation;
	private String targetClassName;
	private File file;
	
	public Report(){}
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isDirectImpresion() {
		return directImpresion;
	}
	public void setDirectImpresion(boolean directImpresion) {
		this.directImpresion = directImpresion;
	}

	public boolean isDisplayPrintDialog() {
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

	public String toString(){
		return "(REPORT (NAME "+name+")(GROUP "+group+")(DIRECT_IMPRESION "+directImpresion+")(PREVIEW "+preView+")(N_COPIES "+nCopies+")(DISPLAY_PRINT_DIALOG "+displayPrintDialog+")(PREPRINT "+prePrint+")(POSTPRINT "+postPrint+")(GENERATE_EXCEL "+generateExcel+"))";
	}

	public ArrayList<SubReport> getSubReportList() {
		return subReportList;
	}

	public void setSubReportList(ArrayList<SubReport> subReportList) {
		this.subReportList = subReportList;
	}

	public ArrayList<String> getFormatList() {
		return formatList;
	}

	public void setFormatList(ArrayList<String> formatList) {
		this.formatList = formatList;
	}

	public boolean isPreView() {
		return preView;
	}

	public void setPreView(boolean preView) {
		this.preView = preView;
	}

	public boolean isGenerateExcel() {
		return generateExcel;
	}

	public void setGenerateExcel(boolean generateExcel) {
		this.generateExcel = generateExcel;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public ArrayList<String> getFunctionalAreaList() {
		return functionalAreaList;
	}

	public void setFunctionalAreaList(ArrayList<String> functionalAreaList) {
		this.functionalAreaList = functionalAreaList;
	}

	public String getPostPrint() {
		return postPrint;
	}

	public void setPostPrint(String postPrint) {
		this.postPrint = postPrint;
	}

	public String getPrePrint() {
		return prePrint;
	}

	public void setPrePrint(String prePrint) {
		this.prePrint = prePrint;
	}

	public boolean isPrintConfirmation() {
		return printConfirmation;
	}

	public void setPrintConfirmation(boolean printConfirmation) {
		this.printConfirmation = printConfirmation;
	}

	public String getPrinterName() {
		return printerName;
	}

	public void setPrinterName(String printerName) {
		this.printerName = printerName;
	}

	public String getTargetClassName() {
		return targetClassName;
	}

	public void setTargetClassName(String targetClassName) {
		this.targetClassName = targetClassName;
	}

	public ArrayList<Param> getParamList() {
		return paramList;
	}

	public void setParamList(ArrayList<Param> paramList) {
		this.paramList = paramList;
	}
	
}
