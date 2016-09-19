package dynagent.server.reports.clasificator;

import java.util.HashSet;
import java.util.Iterator;

/**Clase que almacena las coordenadas de los elementos de una tabla*/
public class Table {

	private HashSet<Coordinates> hElements;
	
	public Table() {
		this.hElements = new HashSet<Coordinates>();
	}

	public void addElement(Coordinates element) {
		hElements.add(element);
	}
	public HashSet<Coordinates> getHElements() {
		return hElements;
	}
	public void setHElements(HashSet<Coordinates> elements) {
		hElements = elements;
	}

	/**Obtiene coordenadas de elementos de están a la misma altura en la banda.*/
	public HashSet<Coordinates> getByTop(String band, int top) {
		HashSet<Coordinates> hCoordinates = new HashSet<Coordinates>();
		Iterator it = hElements.iterator();
		while (it.hasNext()) {
			Coordinates coordinates = (Coordinates)it.next();
			if (coordinates.getBand().equals(band) && coordinates.getTop()==top)
				hCoordinates.add(coordinates);
		}
		return hCoordinates;
	}
	/*public HashSet<Coordinates> getByLeftAndWidth(int left, int width) {
		HashSet<Coordinates> hCoordinates = new HashSet<Coordinates>();
		Iterator it = hElements.iterator();
		while (it.hasNext()) {
			Coordinates coordinates = (Coordinates)it.next();
			if (coordinates.getLeft()==left && coordinates.getWidth()==width)
				hCoordinates.add(coordinates);
		}
		return hCoordinates;
	}*/
	
}
