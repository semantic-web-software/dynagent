package dynagent.gui.utils;

import gdev.gawt.utils.botoneraAccion;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import javax.swing.ImageIcon;

import dynagent.common.Constants;
import dynagent.gui.forms.FormControl;

public class Target extends Object implements Comparable<Target>{
	public int id;
	public String label;
	public FormControl form;
	public ImageIcon icono=null;
	public botoneraAccion botonera;
	public ArrayList<Integer> directReports;
	public int idtoUserTask;

	public Target(int id, String label, ImageIcon icono,FormControl form,botoneraAccion botonera,ArrayList<Integer> directReports,int idtoUserTask){
		this.id=id;
		this.label=label;
		this.icono=icono;
		this.form=form;
		this.botonera=botonera;
		this.directReports=directReports;
		this.idtoUserTask=idtoUserTask;
	}

	public int compareTo(Target o) throws ClassCastException{
		if(!(o instanceof Target))
    		throw new ClassCastException("Error, el objeto a comparar no es un TargetFilter");
		Target target = (Target)o;
		return Constants.languageCollator.compare(this.label, target.label);			
    }
}