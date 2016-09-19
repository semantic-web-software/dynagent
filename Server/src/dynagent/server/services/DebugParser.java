package dynagent.server.services;

import java.util.ArrayList;

public class DebugParser {

	private ArrayList<String> aWarnings;
	private ArrayList<String> aErrors;
	
	public DebugParser() {
		aWarnings = new ArrayList<String>();
		aErrors = new ArrayList<String>();
	}
	
	public void addWarning(String warning) {
		aWarnings.add(warning);
	}
	public void addError(String error) {
		aErrors.add(error);
	}

	public boolean hasErrors() {
		return aErrors.size()>0;
	}
	public boolean hasWarnings() {
		return aWarnings.size()>0;
	}
	
	public String toString() {
		String dev = "";
		boolean hasErrors = hasErrors();
		boolean hasWarnings = hasWarnings();
		
		if (hasErrors && hasWarnings) {
			dev += "PARSEO CON ERRORES Y WARNINGS:\n";
			dev += toStringErrors();
			dev += toStringWarnings();
		} else if (hasErrors) {
			dev += "PARSEO CON ERRORES:\n";
			dev += toStringErrors();
		} else if (hasWarnings) {
			dev += "PARSEO CON WARNINGS:\n";
			dev += toStringWarnings();
		} else
			dev += "PARSEO CORRECTO, SIN ERRORES NI WARNINGS\n";
		return dev;
	}
	public String toStringWarnings() {
		String dev = "";
		for (int i=0;i<aWarnings.size();i++)
			dev += "WARNING: " + aWarnings.get(i) + "\n";
		return dev;
	}
	public String toStringErrors() {
		String dev = "";
		for (int i=0;i<aErrors.size();i++)
			dev += "ERROR: " + aErrors.get(i) + "\n";
		return dev;
	}
}
