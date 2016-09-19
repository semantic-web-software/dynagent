package dynagent.server.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.ObjectIdentifier;

/**
 * Clase que se encarga de contener toda la informacion de un registro de
 * base de datos.
 */
public class ObjectRepresentation {

	/** Lista de todos los valores que se tienen para este objeto. */
	private List<ColumnValue> objectData;
	/**
	 * Mapa de los identificadores de objetos referenciados organizados por
	 * idto.
	 */
	private Map<Integer, List<Integer>> referencedObjectsByClass;
	/** Identificador de la clase a la que pertenece este objeto */
	private int idto;
	/** Identificador del objeto dentro de la clase. */
	private int tableId;
	/** Valor de la columna {@link IQueryInfo#COLUMN_NAME_DESTINATION} */
	private String destination;
	/** Indica si el objeto tiene algun System Value */
	private Map<ObjectIdentifier, ColumnValue> objectsReferenced;
	/** Indica los niveles que se han consultado del objeto. */
	private int levels;

	/**
	 * Inicializa el objeto que va a representar un registro de base de
	 * datos de la clase indicada
	 * 
	 * @param idto
	 *            Identificador de la clase a la que pertenece el objeto
	 *            del que se va a guardar informacion.
	 */
	public ObjectRepresentation(int idto) {
		this.idto = idto;
		this.objectData = new LinkedList<ColumnValue>();
		this.referencedObjectsByClass = new Hashtable<Integer, List<Integer>>();
		this.objectsReferenced = new Hashtable<ObjectIdentifier, ColumnValue>();
		this.levels = 1;
	}

	/**
	 * Devuelve el objeto que representa toda la informacion de una
	 * ObjectProperty que apunta al objeto indicado.
	 * 
	 * @param idto2
	 *            Clase a la que pertenece el objeto buscado.
	 * @param tableId2
	 *            Identificador del objeto buscado dentro de la clase.
	 * @return Devuelve el objeto que representa la informacion del objeto
	 *         indicado o <code>null</code> si no se tiene informacion del
	 *         objeto indicado.
	 */
	public ColumnValue getColumnValue(Integer idto2, Integer tableId2) {
		return objectsReferenced.get(new ObjectIdentifier(idto2, tableId2));
	}

	/**
	 * añade una representación de una columna al objeto.
	 * 
	 * @param value
	 *            Objeto que contiene toda la informacion referente al valor
	 *            almacenado en base de datos.
	 */
	public void addColumnValue(ColumnValue value,boolean migration) {
		if (value.getColumnName().equals(IQueryInfo.COLUMN_NAME_TABLEID)) {
			tableId = Integer.parseInt(value.getValue());
			return;
		} else if (value.getColumnName().equals(IQueryInfo.COLUMN_NAME_DESTINATION)){
			destination = value.getValue();
			if(!migration){
				return;
			}
		}
		objectData.add(value);
		if (value.getPropertyType() == ColumnValue.OBJECT_PROPERTY) {
			Integer idtoReferenced = value.getReferencedClassIdto();
			Integer tableIdReferenced = Integer.parseInt(value.getValue());
			objectsReferenced.put(new ObjectIdentifier(idtoReferenced, tableIdReferenced), value);
			List<Integer> currentTableIdsReferenced = referencedObjectsByClass.get(idtoReferenced);
			if (currentTableIdsReferenced == null) {
				currentTableIdsReferenced = new ArrayList<Integer>();
				referencedObjectsByClass.put(idtoReferenced, currentTableIdsReferenced);
			}
			if (!currentTableIdsReferenced.contains(tableIdReferenced)) {
				currentTableIdsReferenced.add(tableIdReferenced);
			}
		}
	}

	/**
	 * Devuelve un mapa con los identificadores de los objetos referenciados
	 * por este objeto mediate Object Properties.
	 * 
	 * @return Mapa con los objetos referenciados.
	 */
	public Map<Integer, List<Integer>> getReferencedObjectsByClass() {
		return referencedObjectsByClass;
	}

	public boolean isStructural(Integer referencedIdto, Integer tableId2) {
		System.out.println("IDTO= " + referencedIdto + "; TABLEID=" + tableId2);
		ObjectIdentifier objectIdentifier = new ObjectIdentifier(referencedIdto, tableId2);
		System.out.println(objectIdentifier);
		ColumnValue columnValue = objectsReferenced.get(objectIdentifier);
		return columnValue.isStructuralProperty();
	}

	/**
	 * Devuelve el identificador de la clase a la que pertenece este objeto.
	 * 
	 * @return Identificador numero de la clase a la que pertenece el
	 *         objeto.
	 */
	public int getIdto() {
		return idto;
	}

	/**
	 * Devuelve el identificador del objeto en la tabla de la clase.
	 * 
	 * @return TableId del objeto.
	 */
	public int getTableId() {
		return tableId;
	}

	/**
	 * Consulta los valores que tiene almacenados este objeto.
	 * 
	 * @return Lista de todos los valores almacenados.
	 */
	public List<ColumnValue> getObjectData() {
		return objectData;
	}
	
	/**
	 * Devuelve el valor que tenia la columna 
	 * @return
	 */
	public String getDestination(){
		return destination;
	}

	public int getLevels() {
		return levels;
	}

	public void setLevels(int levels) {
		this.levels = levels;
	}

	public String toString() {
		String result = "ClassIdto=" + idto + "[tableId=" + tableId + " ";
		for (ColumnValue columnValue : objectData) {
			result += ", " + columnValue.getColumnName() + "=" + columnValue.getValue(); 
		}
		return result;
	}
}