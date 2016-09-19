package dynagent.server.dbmap;

/**
 * Clase que permite almacenar los identificadores de las dos tablas
 * referenciadas por una tabla asociacion y redefine los metodos equals y
 * hashCode para que permitan, independientemente del orden en que se
 * indiquen los identificadores, saber si se refieren a la misma pareja de
 * identificadores.
 */
public class AssocciationPair {

	private int firstIdto;
	private int secondIdto;

	public AssocciationPair(int firstIdto, int secondIdto) {
		this.firstIdto = firstIdto;
		this.secondIdto = secondIdto;
	}

	public boolean equals(Object obj) {
		return (obj instanceof AssocciationPair)
				&& (((AssocciationPair) obj).firstIdto == this.firstIdto)
				&& (((AssocciationPair) obj).secondIdto == this.secondIdto);
	}

	public int hashCode() {
		return (firstIdto + secondIdto) - secondIdto;

	}

	public String toString() {
		return "{" + firstIdto + "," + secondIdto + "}";
	}
}