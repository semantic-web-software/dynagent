package dynagent.common.communication;

import org.jdom.Element;

public class Reservation {
	private int ido;
	private int idProp;
	private double reservation;
	private double available;
	
	public Reservation(int ido, int idProp, double reservation, double available) {
		super();
		this.ido = ido;
		this.idProp = idProp;
		this.reservation = reservation;
		this.available = available;
	}
	public int getIdo() {
		return ido;
	}
	public void setIdo(int ido) {
		this.ido = ido;
	}
	public int getIdProp() {
		return idProp;
	}
	public void setIdProp(int idProp) {
		this.idProp = idProp;
	}
	public double getAvailable() {
		return available;
	}
	public void setAvailable(double amoung) {
		this.available = amoung;
	}
	public double getReservation() {
		return reservation;
	}
	public void setReservation(double reservation) {
		this.reservation = reservation;
	}
	
	public String toString() {
		return "RESERVATION-> IDO:" + this.ido + ", IDPROP:" + idProp + ", RESERVATION:" + reservation + ", AVAILABLE:" + available;
	}
	
	public Element toElement() {
		Element reservationElem = new Element("RESERVATION");
		reservationElem.setAttribute("IDO",String.valueOf(this.getIdo()));
		reservationElem.setAttribute("IDPROP",String.valueOf(this.getIdProp()));
		reservationElem.setAttribute("RESERVATION",String.valueOf(this.getReservation()));
		reservationElem.setAttribute("AVAILABLE",String.valueOf(this.getAvailable()));
		return reservationElem;
	}

}
