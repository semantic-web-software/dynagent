package dynagent.server.reports.clasificator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**Clase de elementos analizados en la clase ReportAnalized necesarios para decidir 
 * en la clase ReportUpdater si clonar o no cada nodo del diseño original.*/
public class ElemsAnalized {

	private boolean reportRemove;
	private boolean classified;
	private HashSet<String> fieldsToRemove;
	private HashSet<String> parametersToRemove;
	private HashSet<String> variablesToRemove;
	private HashSet<String> textFieldsToRemove;
	private HashSet<String> staticTextsToRemove;
	private HashSet<String> framesToRemove;
	private HashSet<String> groupsToRemove;
	private HashSet<String> bandsToRemove;
	private HashSet<String> allTextFieldsAndFrames;
	private HashMap<String,Coordinates> newCoordinatesInTable;
	
	public ElemsAnalized(boolean reportRemove, boolean classified, HashSet<String> fieldsToRemove, HashSet<String> parametersToRemove, 
			HashSet<String> variablesToRemove, HashSet<String> textFieldsToRemove, HashSet<String> staticTextsToRemove, 
			HashSet<String> framesToRemove, HashSet<String> groupsToRemove, HashSet<String> bandsToRemove, 
			HashSet<String> allTextFieldsAndFrames, HashMap<String,Coordinates> newCoordinatesInTable) {
		this.reportRemove = reportRemove;
		this.classified = classified;
		this.fieldsToRemove = fieldsToRemove;
		this.parametersToRemove = parametersToRemove;
		this.variablesToRemove = variablesToRemove;
		this.staticTextsToRemove = staticTextsToRemove;
		this.textFieldsToRemove = textFieldsToRemove;
		this.framesToRemove = framesToRemove;
		this.groupsToRemove = groupsToRemove;
		this.bandsToRemove = bandsToRemove;
		this.allTextFieldsAndFrames = allTextFieldsAndFrames;
		this.newCoordinatesInTable = newCoordinatesInTable;
	}

	public boolean isReportRemove() {
		return reportRemove;
	}

	public boolean isClassified() {
		return classified;
	}
	
	public HashSet<String> getBandsToRemove() {
		return bandsToRemove;
	}

	public HashSet<String> getFieldsToRemove() {
		return fieldsToRemove;
	}

	public HashSet<String> getFramesToRemove() {
		return framesToRemove;
	}

	public HashSet<String> getGroupsToRemove() {
		return groupsToRemove;
	}

	public HashSet<String> getParametersToRemove() {
		return parametersToRemove;
	}

	public HashSet<String> getTextFieldsToRemove() {
		return textFieldsToRemove;
	}
	
	public HashSet<String> getStaticTextsToRemove() {
		return staticTextsToRemove;
	}

	public HashSet<String> getVariablesToRemove() {
		return variablesToRemove;
	}

	public HashSet<String> getAllTextFieldsAndFrames() {
		return allTextFieldsAndFrames;
	}

	public HashMap<String, Coordinates> getNewCoordinatesInTable() {
		return newCoordinatesInTable;
	}
	
	public String toString() {
		String dev = "reportRemove " + reportRemove + ", \nfieldsToRemove " + fieldsToRemove + 
		", \nparametersToRemove " + parametersToRemove + ", \nvariablesToRemove " + variablesToRemove + 
		", \nstaticTextsToRemove " + staticTextsToRemove + ", \ntextFieldsToRemove " + textFieldsToRemove + 
		", \nframesToRemove " + framesToRemove + ", \ngroupsToRemove " + groupsToRemove + 
		", \nallTextFieldsAndFrames " + allTextFieldsAndFrames + ", \nbandsToRemove " + bandsToRemove;
		Iterator<String> it = newCoordinatesInTable.keySet().iterator();
		while (it.hasNext()) {
			String key = (String)it.next();
			Coordinates coord = newCoordinatesInTable.get(key);
			dev += "\nnewCoordinatesInTable->field " + key + ", coord " + coord;
		}
		return dev;
	}
}
