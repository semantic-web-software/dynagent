package dynagent.server.dbmap;

import java.io.Serializable;

import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.gestorsDB.GestorsDBConstants;

/**
 * Clase que guarda la informacion de una clave foranea de una tabla especifica.
 * 
 */
public class ForeignKey implements Serializable {

	private static final long serialVersionUID = -4260234603926414029L;
	public static final int CASCADE = 1;
	public static final int RESTRICT = 2;
	public static final int SETNULL = 3;
	public static final int DONOTHING = 4;

	private String ownerTableName;
	private String ownerColumnName;
	private String referencedTableName;
	private String referencedColumnName;
	private int onUpdate;
	private int onDelete;

	/**
	 * Construye una clave foranea en la que al actualizar y al borrar no se
	 * realiza ninguna acción especial.
	 * 
	 * @param ownerTableName
	 *            Nombre de la tabla en la que se va a introducir la clave
	 *            foranea.
	 * @param ownerColumnName
	 *            Nombre de la columna de la tabla propietaria de la clave que
	 *            se va a usar para referenciar a la columna de la otra tabla.
	 * @param referencedTableName
	 *            Nombre de la tabla a la que se va a referenciar mediante la
	 *            clave foranea.
	 * @param referencedColumnName
	 *            Nombre de la columna a la que se va a apuntar en la tabla
	 *            referenciada.
	 */
	public ForeignKey(String ownerTableName, String ownerColumnName,
			String referencedTableName, String referencedColumnName) {
		this.ownerTableName = ownerTableName;
		this.ownerColumnName = ownerColumnName;
		this.referencedTableName = referencedTableName;
		this.referencedColumnName = referencedColumnName;
		this.onUpdate = ForeignKey.DONOTHING;
		this.onDelete = ForeignKey.DONOTHING;
	}

	/**
	 * @param ownerTableName
	 *            Nombre de la tabla en la que se va a introducir la clave
	 *            foranea.
	 * @param ownerColumnName
	 *            Nombre de la columna de la tabla propietaria de la clave que
	 *            se va a usar para referenciar a la columna de la otra tabla.
	 * @param referencedTableName
	 *            Nombre de la tabla a la que se va a referenciar mediante la
	 *            clave foranea.
	 * @param referencedColumnName
	 *            Nombre de la columna a la que se va a apuntar en la tabla
	 *            referenciada.
	 * @param onUpdate
	 *            Entero que identifica a la acción que se quiere realizar al
	 *            actualizar el campo en la tabla a la que se referencia.
	 * @param onDelete
	 *            Entero que identifica a la acción que se quiere realizar al
	 *            borrar el registro al que se apunta en la tabla a la que se
	 *            referencia.
	 */
	public ForeignKey(String ownerTableName, String ownerColumnName,
			String referencedTableName, String referencedColumnName,
			int onUpdate, int onDelete) {
		this.ownerTableName = ownerTableName;
		this.ownerColumnName = ownerColumnName;
		this.referencedTableName = referencedTableName;
		this.referencedColumnName = referencedColumnName;
		this.onUpdate = onUpdate;
		this.onDelete = onDelete;
	}

	/**
	 * @return the ownerTableName
	 */
	public String getOwnerTableName() {
		return ownerTableName;
	}

	/**
	 * @return the ownerColumnName
	 */
	public String getOwnerColumnName() {
		return ownerColumnName;
	}

	public void setOwnerColumnName(String ownerColumnName) {
		this.ownerColumnName = ownerColumnName;
	}

	/**
	 * @return the referencedTableName
	 */
	public String getReferencedTableName() {
		return referencedTableName;
	}

	/**
	 * @return the referencedColumnName
	 */
	public String getReferencedColumnName() {
		return referencedColumnName;
	}

	/**
	 * @return the onUpdate
	 */
	public int getOnUpdate() {
		return onUpdate;
	}

	/**
	 * @return the onDelete
	 */
	public int getOnDelete() {
		return onDelete;
	}

	/**
	 * Construye la cadena que declara el foreign key, especificando todos los
	 * parámetros necesarios que se tienen almacenados.
	 * 
	 * @param constraintName
	 *            Nombre que se le va a dar a la restriccion de foreign key.
	 * @param gestor
	 *            Nombre del gestor de base de datos que se está usando
	 * @return
	 */
	public String getForeignKeyDeclaration(String constraintName, String gestor) {
		GenerateSQL generateSQL = new GenerateSQL(gestor);
		String characterEnd = generateSQL.getCharacterEnd();
		String characterBegin = generateSQL.getCharacterBegin();
		String result = "CONSTRAINT " + characterBegin + constraintName + characterEnd + " FOREIGN KEY ";
		if (!gestor.equals(GestorsDBConstants.postgreSQL)){
			result += characterBegin + constraintName + characterEnd;
		}
		result += " (" + characterBegin + ownerColumnName + characterEnd + ") REFERENCES " + characterBegin
				+ referencedTableName + characterEnd + " (" + characterBegin + referencedColumnName + characterEnd + ")";
		// String result = "FOREIGN KEY (`" + ownerColumnName +
		// "`) REFERENCES `" + referencedTableName + "` (`"
		// + referencedColumnName + "`) ";
		switch (onDelete) {
		case CASCADE:
			result += " ON DELETE CASCADE";
			break;
		case RESTRICT:
			result += " ON DELETE RESTRICT";
			break;
		case SETNULL:
			result += " ON DELETE SET NULL";
			break;
		default:
			result += " ON DELETE NO ACTION";
			break;
		}
		switch (onUpdate) {
		case CASCADE:
			result += " ON UPDATE CASCADE";
			break;
		case RESTRICT:
			result += " ON UPDATE RESTRICT";
			break;
		case SETNULL:
			result += " ON UPDATE SET NULL";
			break;
		default:
			result += " ON UPDATE NO ACTION";
			break;
		}
		
		result += " DEFERRABLE INITIALLY IMMEDIATE";
		
		return result;
	}

	public boolean equals(Object obj) {
		boolean result = obj != null && obj instanceof ForeignKey;
		if (result) {
			ForeignKey foreignKey = (ForeignKey) obj;
			result = result
					&& foreignKey.getForeignKeyDeclaration("",
							GestorsDBConstants.mySQL).equals(
							this.getForeignKeyDeclaration("",
									GestorsDBConstants.mySQL));
		}
		return result;
	}

	public String toString() {
		return ownerTableName + "('" + ownerColumnName + "') REFERENCES "
				+ referencedTableName + "('" + referencedColumnName + "')";
	}
}
