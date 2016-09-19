package dynagent.common.utils;

/**
 * Clase que representa el modelo de las líneas de las subcuentasen la exportación a ContaPlus.
 * 
 * @author Darío Veledo García
 */
public class SubCuenta extends FicherosContaPlus {
	
	/**
	 * Constructor de la clase SubCuentas.
	 */
	public SubCuenta() {
		super();

		insertarCampo("codigo", 12, 0, true, String.class);
		insertarCampo("titulo", 40, 0, true, String.class);
		insertarCampo("nif", 15, 0, false, String.class);
		insertarCampo("domicilio", 35, 0, false, String.class);
		insertarCampo("poblacion", 25, 0, false, String.class);
		insertarCampo("provincia", 20, 0, false, String.class);
		insertarCampo("codigoPostal", 5, 0, false, String.class);
		insertarCampo("divisa", 1, 0, false, Boolean.class);
		insertarCampo("codigoDivisa", 5, 0, false, String.class);
		insertarCampo("documentos", 1, 0, false, Boolean.class);
		insertarCampo("ajusteME", 1, 0, false, Boolean.class);
		insertarCampo("tipoIVA", 1, 0, false, String.class);
		insertarCampo("proye", 9, 0, false, String.class);
		insertarCampo("subequiv", 12, 0, false, String.class);
		insertarCampo("subcierre", 12, 0, false, String.class);
		insertarCampo("linterrump", 1, 0, false, Boolean.class);
		insertarCampo("segmento", 12, 0, false, String.class);
		insertarCampo("tpc", 5, 2, false, Double.class);
		insertarCampo("recEquiv", 5, 2, false, Double.class);
		
		
		//faltarian del 20 al 35 que como no son oblig y están al final de la línea no los informamos.
		
		
		
		
		
		
	}
}
