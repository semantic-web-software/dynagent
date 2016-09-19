package dynagent.ruleengine.src.ruler.ERPrules.datarules;

public class StringChanged {

	boolean changed=false;
	String infoAux=null;
	public String getInfoAux() {
		return infoAux;
	}

	public void setInfoAux(String infoAux) {
		this.infoAux = infoAux;
	}

	String valor=null;
	
	
	
	public StringChanged(String valor,boolean changed){
		this.valor=valor;
		this.changed=changed;
	}
	
	public boolean equals(Object arg0) {
		// TODO Auto-generated method stub
		//System.out.println("DOUBLE CHANGED "+valor);
		if( valor!=null && arg0 instanceof StringChanged){
			return valor.equals(arg0);
		}
		return false;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}
	
	public String toString(){
		return "(StringChanged valor:"+valor+"  changed:"+changed+"  infoAux:"+infoAux+")";
		
	}



}
