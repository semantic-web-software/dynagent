package dynagent.gui.adapter.old;

public class InfoPlay {
	private Integer cardmin;
	private Integer cardmax;
	private String name;
	private Integer rol;
	private Integer ido;
	
	public InfoPlay(Integer ido,Integer cardmin,Integer cardmax,String name,Integer rol){
		this.cardmin=cardmin;
		this.cardmax=cardmax;
		this.name=name;
		this.rol=rol;
		this.ido=ido;
	}
	public Integer getCardmax() {
		return cardmax;
	}

	public void setCardmax(Integer cardmax) {
		this.cardmax = cardmax;
	}

	public Integer getCardmin() {
		return cardmin;
	}

	public void setCardmin(Integer cardmin) {
		this.cardmin = cardmin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getRol() {
		return rol;
	}

	public void setRol(Integer rol) {
		this.rol = rol;
	}

	public Integer getIdo() {
		return ido;
	}

	public void setIdo(Integer ido) {
		this.ido = ido;
	}
}
