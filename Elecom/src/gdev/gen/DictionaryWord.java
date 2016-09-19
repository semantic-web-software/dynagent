package gdev.gen;

public class DictionaryWord {

	private int ido;
	private int idto;
	private String word;
	private boolean temporal;//Indica si es un objeto que aun no existe en base de datos
	
	public DictionaryWord(int ido, int idto, String word, boolean temporal) {
		super();
		this.ido = ido;
		this.idto = idto;
		this.word = word;
		this.temporal = temporal;
	}

	@Override
	public String toString() {
		return "ido:"+ido+" idto:"+idto+" word:"+word+" temporal:"+temporal;
	}

	public int getIdo() {
		return ido;
	}

	public void setIdo(int ido) {
		this.ido = ido;
	}

	public int getIdto() {
		return idto;
	}

	public void setIdto(int idto) {
		this.idto = idto;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public boolean isTemporal() {
		return temporal;
	}

	public void setTemporal(boolean temporal) {
		this.temporal = temporal;
	}
	
}
