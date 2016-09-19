package dynagent.ruleengine.src.ruler.ERPrules.datarules;

public class DoubleChanged implements Comparable{

	boolean changed=false;
	String infoAux=null;
	public String getInfoAux() {
		return infoAux;
	}

	public void setInfoAux(String infoAux) {
		this.infoAux = infoAux;
	}

	Double valor=0.0D;
	long changeTime=0L;//tiempo sucede el cambio
	
	
	public DoubleChanged(Double valor,boolean changed){
		this.valor=valor;
		this.changed=changed;
	}
	
	public DoubleChanged(Double valor,boolean changed,long changeTime){
		this.valor=valor;
		this.changed=changed;
		this.changeTime=changeTime;
	}
	
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		//System.out.println("DOUBLE CHANGED "+valor);
		if( arg0 instanceof DoubleChanged){
			DoubleChanged vB=(DoubleChanged)arg0;
			if(vB.isChanged()) this.setChanged(true);

			if(this.isChanged()) vB.setChanged(true);

			if(valor<vB.getValor()) return -1;
			else if( valor>vB.getValor()) return 1; 
		}
		return 0;
	}

	public long getChangeTime(){
		return changeTime;
	}
	public void setChangeTime(long t){
		changeTime=t;
	}
	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public Double getValor() {
		return valor;
	}

	public void setValor(double valor) {
		this.valor = valor;
	}
	
	public String toString(){
		return "(DoubleChanged valor:"+valor+"  changed:"+changed+"  infoAux:"+infoAux+")";
		
	}

}
