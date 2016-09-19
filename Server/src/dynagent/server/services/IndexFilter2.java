package dynagent.server.services;

import java.sql.SQLException;

import javax.naming.NamingException;

import dynagent.server.ejb.FactoryConnectionDB;

/**
 * Contiene toda la informacion sobre un índice
 */
public class IndexFilter2 {

	/** Valor que tiene que tener el campo por el que se filtra para que se pueda aplicar este índice. */
	private String filterValue;
	/** Valor que tiene actualmente el índice. */
	private int indexValue;
	/** Campo que se usa para generar el prefijo temporal. */
	private String temporalPrefixField;
	/**
	 * máscara que se aplica al valor del campo que se usa en el prefijo temporal, por ejemplo: <i>mmaaaa</i> o
	 * <i>aa</i>.
	 */
	private String temporalPrefixMask;
	/** Indica si se pone un sufijo global. */
	@Deprecated
	private boolean globalSufix;
	/** Nombre de la propiedad que se usa en el prefijo. */
	private String prefixField;
	/** Valor que se añade siempre como prefijo justo antes del índice. */
	private String prefixValue;
	/** Valor que se añade siempre como sufijo justo detras del índice. */
	private String sufixValue;
	/** Valor que se usa en lugar del año en el prefijo temporal. */
	private Integer yearCount;
	/**
	 * último prefijo temporal que se usa. Indica si se debe reiniciar el contador o si hay que buscar en base de datos
	 * el índice por ser un objeto antiguo.
	 */
	private String lastTemporalPrefix;
	/** Nombre de la propiedad que se va a usar como filtro. */
	private String filterField;
	/** Nombre de la clase sobre la que es aplicable el filtro. */
	private String domain;
	/** TableId de la empresa sobre la que es aplicable este índice. */
	private Integer myBusiness;
	/** Rdn de la empresa sobre la que es aplicable este índice. */
	private String rdnMyBusiness;
	/** mínimo número de digitos que debe tener el contador del índice. */
	private Integer minDigits;
	/** TableId del índice en base de datos. util para incrementar el valor actual del índice. */
	private Integer indexId;

	/**
	 * Constructor sin globalSufix
	 * 
	 * @param tableId
	 *            TableId del índice en base de datos. util para incrementar el valor actual del índice.
	 * @param indexedField
	 *            Nombre de la property sobre la que se aplic el índice.
	 * @param filterValue
	 *            Valor que tiene que tener el campo por el que se filtra para que se pueda aplicar este índice.
	 * @param indexValue
	 *            Valor que tiene actualmente el índice.
	 * @param temporalPrefixField
	 *            Campo que se usa para generar el prefijo temporal.
	 * @param temporalPrefixMask
	 *            máscara que se aplica al valor del campo que se usa en el prefijo temporal, por ejemplo: <i>mmaaaa</i>
	 *            o <i>aa</i>.
	 * @param prefixField
	 *            Nombre de la propiedad que se usa en el prefijo.
	 * @param prefixValue
	 *            Valor que se añade siempre como prefijo justo antes del índice.
	 * @param yearCount
	 *            Cadena que se usa en lugar del año en el prefijo temporal.
	 * @param lastTemporalPrefix
	 *            último prefijo temporal que se uso. Indica si se debe reiniciar el contador o si hay que buscar en
	 *            base de datos el índice por ser un objeto antiguo.
	 * @param filterField
	 *            Nombre de la propiedad que se va a usar como filtro.
	 * @param domain
	 *            Nombre de la clase sobre la que es aplicable el filtro.
	 */
	public IndexFilter2(Integer tableId, String filterValue, int indexValue,
			String temporalPrefixField, String temporalPrefixMask, String prefixField, String prefixValue,
			String sufixValue, Integer yearCount, String lastTemporalPrefix, String filterField, String domain, Integer myBusiness, String rdnMyBusiness, Integer minDigits) {

		this(tableId, filterValue, indexValue, temporalPrefixField, temporalPrefixMask, false,
				prefixField, prefixValue, sufixValue, yearCount, lastTemporalPrefix, filterField, domain, myBusiness, rdnMyBusiness, minDigits);
	}

	/**
	 * Constructor con todos los parámetros.
	 * 
	 * @param tableId
	 *            TableId del índice en base de datos. util para incrementar el valor actual del índice.
	 * @param indexedField
	 *            Nombre de la property sobre la que se aplic el índice.
	 * @param filterValue
	 *            Valor que tiene que tener el campo por el que se filtra para que se pueda aplicar este índice.
	 * @param indexValue
	 *            Valor que tiene actualmente el índice.
	 * @param temporalPrefixField
	 *            Campo que se usa para generar el prefijo temporal.
	 * @param temporalPrefixMask
	 *            máscara que se aplica al valor del campo que se usa en el prefijo temporal, por ejemplo: <i>mmaaaa</i>
	 *            o <i>aa</i>.
	 * @param globalSufix
	 *            Indica si se pone un sufijo global.
	 * @param prefixField
	 *            Nombre de la propiedad que se usa en el prefijo.
	 * @param prefixValue
	 *            Valor que se añade siempre como prefijo justo antes del índice.
	 * @param yearCount
	 *            Cadena que se usa en lugar del año en el prefijo temporal.
	 * @param lastTemporalPrefix
	 *            último prefijo temporal que se uso. Indica si se debe reiniciar el contador o si hay que buscar en
	 *            base de datos el índice por ser un objeto antiguo.
	 * @param filterField
	 *            Nombre de la propiedad que se va a usar como filtro.
	 * @param domain
	 *            Nombre de la clase sobre la que es aplicable el filtro.
	 */
	public IndexFilter2(Integer tableId, String filterValue, int indexValue,
			String temporalPrefixField, String temporalPrefixMask, boolean globalSufix, String prefixField,
			String prefixValue, String sufixValue, Integer yearCount, String lastTemporalPrefix, String filterField, String domain,
			Integer myBusiness, String rdnMyBusiness, Integer minDigits) {
		this.indexId = tableId;
		this.filterValue = filterValue;
		this.indexValue = indexValue;
		this.temporalPrefixField = temporalPrefixField;
		this.temporalPrefixMask = temporalPrefixMask;
		this.globalSufix = globalSufix;
		this.prefixField = prefixField;
		this.prefixValue = prefixValue;
		this.sufixValue = sufixValue;
		this.yearCount = yearCount;
		this.lastTemporalPrefix = lastTemporalPrefix;
		this.filterField = filterField;
		this.domain = domain;
		this.myBusiness = myBusiness;
		this.rdnMyBusiness = rdnMyBusiness;
		this.minDigits = minDigits;
	}

	/**
	 * @return the filterValue
	 */
	public String getFilterValue() {
		return filterValue;
	}

	/**
	 * @return the indexValue
	 */
	public int getIndexValue() {
		return indexValue;
	}

	public void setIndexValue(int indexValue) {
		this.indexValue = indexValue;
	}

	/**
	 * @return the temporalPrefixField
	 */
	public String getTemporalPrefixField() {
		return temporalPrefixField;
	}

	/**
	 * @return the temporalPrefixMask
	 */
	public String getTemporalPrefixMask() {
		return temporalPrefixMask;
	}

	/**
	 * @return the globalSufix
	 * @deprecated El sufijo global ya no se usa en versiones que tienen el modelo relacional de base de datos.
	 */
	public boolean isGlobalSufix() {
		return globalSufix;
	}

	/**
	 * @return the prefixField
	 */
	public String getPrefixField() {
		return prefixField;
	}

	/**
	 * @return the prefixValue
	 */
	public String getPrefixValue() {
		return prefixValue;
	}

	/**
	 * @return the sufixValue
	 */
	public String getSufixValue() {
		return sufixValue;
	}
	
	/**
	 * @return the yearCount
	 */
	public Integer getYearCount() {
		return yearCount;
	}

	/**
	 * @return the lastTemporalPrefix
	 */
	public String getLastTemporalPrefix() {
		return lastTemporalPrefix;
	}

	/**
	 * @param lastTemporalPrefix the lastTemporalPrefix to set
	 */
	public void setLastTemporalPrefix(String lastTemporalPrefix) {
		this.lastTemporalPrefix = lastTemporalPrefix;
	}

	/**
	 * @return the filterField
	 */
	public String getFilterField() {
		return filterField;
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @return the myBusiness
	 */
	public Integer getMyBusiness() {
		return myBusiness;
	}

	/**
	 * @return the rdnMyBusiness
	 */
	public String getRdnMyBusiness() {
		return rdnMyBusiness;
	}

	/**
	 * @return the minDigits
	 */
	public Integer getMinDigits() {
		return minDigits;
	}
	
	/**
	 * @return the indexId
	 */
	public Integer getIndexId() {
		return indexId;
	}


	public void decrementIndexValue(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		indexValue--;
	}

	/**
	 * Resetea el valor del índice.
	 */
	public void resetIndexValue() {
		indexValue = 1;
	}

	/**
	 * Incrementa el contador de año en una unidad y resetea el contador del índice.
	 */
	public void incrementYearCount(int increment) {
		resetIndexValue();
		yearCount = yearCount+increment;
	}
	
	public String toString() {
		return "filterValue " + filterValue + ", indexValue " + indexValue + ", temporalPrefixField " + temporalPrefixField + 
		", temporalPrefixMask " + temporalPrefixMask + ", globalSufix " + globalSufix + ", prefixField " + prefixField + ", prefixValue " + prefixValue + 
		", sufixValue " + sufixValue + ", yearCount " + yearCount + ", lastTemporalPrefix " + lastTemporalPrefix + ", filterField " + filterField + 
		", domain " + domain + ", myBusiness " + myBusiness + ", rdnMyBusiness " + rdnMyBusiness + ", minDigits " + minDigits;
	}

}
