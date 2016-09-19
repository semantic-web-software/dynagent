package dynagent.server.services.reports;
/**
 * Esta clase guarda la informacion completa de una consulta determinada, ya sea la principal 
 * o una subconsulta.
 * 
 * @author Dynagent - David
 *
 */
public class ParQuery {
	/**
	 * Contiene la consulta, sentencia SQL.
	 */
	private String query=null;
	/**
	 * Array con los ids de los subreports de esta consulta.<br>
	 * <i>Ejemplo: {"2",..}</i>
	 */
	private String ids[]=null;
	/**
	 * Array con los campos visibles.<br>
	 * <i>Ejemplo: {"2_nombre empresa_String",...} - {id_nombre_tipo,...}</i>
	 */
	private String show[]=null;
	/**
	 * Array con los campos ocultos.<br>
	 * <i>Ejemplo: {"1_id_Integer",...} - {id_nombre_tipo,...}</i>
	 */
	private String hide[]=null;

	/**
	 * <p>Constructor de la clase.</p>
	 *	
	 */
	public ParQuery(){
		
	}
	/**
	 * <p><h6>Constructor con parámetros.</h6></p>
	 * 
	 * @param query String con la consulta (sentencia SQL)
	 * @param ids String con los ids de los subreports - Usamos split con ","
	 * @param show String con los campos visibles - Usamos split con ","
	 * @param hide String con los campos ocultos - Usamos split con ","
	 */
	public ParQuery(String query, String ids, String show, String hide){
		this.query=query;
		this.ids= ids.split(",");
		this.show=show.split(",");
		this.hide=hide.split(",");
		
	}
	
	public String toString() {
		String resultado = "QUERY: "+ query + "\nCOL_MOSTRADAS: ";
		for (int i=0;i<show.length;i++) {
			if (i>0)
				resultado += ", ";
			resultado += show[i];
		}
		resultado += "\nCOL_NO_MOSTRADAS: ";
		for (int i=0;i<hide.length;i++) {
			if (i>0)
				resultado += ", ";
			resultado += hide[i];
		}
		resultado += "\nIDS_SUB_QUERY: ";
		for (int i=0;i<ids.length;i++) {
			if (i>0)
				resultado += ", ";
			resultado += ids[i];
		}
		resultado += "\n";
		return resultado;
	}
	
	/**
	 * @see {@link #ids}
	 * @return Devulve {@link #ids}
	 */
	public String[] getIds(){
		return this.ids;
	}
	/**
	 * @see {@link #query}
	 * @return Devulve {@link #query}
	 */
	public String getQuery(){
		return this.query;
	}
	/**
	 * @see {@link #show}
	 * @return Devulve {@link #show}
	 */
	public String[] getShow(){
		return this.show;
	}
	/**
	 * @see {@link #hide}
	 * @return Devulve {@link #hide}
	 */
	public String[] getHide(){
		return this.hide;
	}
	/**
	 * metodo set de {@link #hide}
	 * @see {@link #hide}
	 * @param hide
	 */
	public void setHide(String hide[]){
		this.hide=hide;
	}
	/**
	 * metodo set de {@link #show}
	 * @see {@link #show}
	 * @param show
	 */
	public void setShow(String show[]){
		this.show=show;
	}
	/**
	 * metodo set de {@link #ids}
	 * @see {@link #ids}
	 * @param ids
	 */
	public void setIds(String ids[]){
		this.ids=ids;
	}
	/**
	 * metodo set de {@link #query}
	 * @see {@link #query}
	 * @param query
	 */
	public void setQuery(String query){
		this.query=query;
	}
	
}
