package dynagent.common.utils;

import java.util.HashMap;
import java.util.LinkedList;


/**
 * Clase que representa el modelo de los asientos del diario en la exportación a ContaPlus.
 * 
 * @author Darío Veledo García
 */
public class Apunte extends FicherosContaPlus{
	SubCuenta subcuenta=null;
	public SubCuenta getSubcuenta() {
		return subcuenta;
	}

	public void setSubcuenta(SubCuenta subcuenta) {
		this.subcuenta = subcuenta;
	}


	SubCuenta contrapartida=null;
	public SubCuenta getContrapartida() {
		return contrapartida;
	}

	public void setContrapartida(SubCuenta contrapartida) {
		this.contrapartida = contrapartida;
	}

	/**
	 * Constructor de la clase Apunte.
	 */
	public Apunte(){
		super();
		campos = new HashMap<String, Campo>();
		nombreCampos = new LinkedList<String>();
		
		
		insertarCampo("numeroAsiento", 6, 0, true, Double.class);
		insertarCampo("fecha", 8, null, true, Double.class);
		insertarCampo("subCuenta", 12, null, true, String.class);
		insertarCampo("contrapartida", 12, null, true, String.class);
		insertarCampo("importeDebePts", 16, 2, true, Double.class);
		insertarCampo("concepto", 25, null, true, String.class);
		insertarCampo("importeHaberPts", 16, 2, true, Double.class);
		insertarCampo("numeroFacturaAlIva", 8, 0, false, Double.class);
		insertarCampo("baseImponibleIVAPts", 16, 2, true, Double.class);
		insertarCampo("porcentajeIVA", 5, 2, true, Double.class);
		insertarCampo("porcentajeRE", 5, 2, true, Double.class);
		//insertarCampo("documento", 10, null, false, String.class);
		//insertarCampo("departamento", 3, null, false, String.class);
		//insertarCampo("codigoProyecto", 6, null, false, String.class);
		//insertarCampo("punteo", 1, null, false, String.class);
		//insertarCampo("numericoCasacion", 6, 0, false, Double.class);
		//insertarCampo("tipoCasado", 1, 0, false, Double.class);
		//insertarCampo("numeroPago", 6, 0, false, Double.class);
		//insertarCampo("cambioAAplicar", 16, 6, false, Double.class);
		//insertarCampo("importeHaberME", 16, 2, false, Double.class);
		//insertarCampo("importeDebeME", 16, 2, false, Double.class);
		//insertarCampo("auxiliar", 1, null, false, String.class);
		//insertarCampo("serie", 1, null, false, String.class);
		//insertarCampo("sucursal", 4, null, false, String.class);
		//insertarCampo("codigoDivisa", 5, null, false, String.class);
		//insertarCampo("importeAuxME", 16, 2, false, Double.class);
		insertarCampo("noObligatorios12a26", 108, null, false, String.class);//longitud suma de los campos 12 a 26
		insertarCampo("monedaUso", 1, null, true, String.class);
		insertarCampo("importeDebeEuros", 16, 2, true, Double.class);
		insertarCampo("importeHaberEuros", 16, 2, true, Double.class);
		insertarCampo("baseIVAEuros", 16, 2, true, Double.class);
		insertarCampo("noConv", 1, null, true, Boolean.class);
		insertarCampo("codigoActivo", 10, 0, false, Double.class);
		insertarCampo("serie_RT", 1, null, true, String.class);
		insertarCampo("factu_RT", 8, null,true, Double.class);
		insertarCampo("baseImp_RT", 16, 2,true, Double.class);
		insertarCampo("baseImp_RF", 16, 2,true, Double.class);
		insertarCampo("rectifica", 1, null, true, Boolean.class);
		insertarCampo("fecha_RT", 8, null, true, Double.class);
		
		//NO OBLIGAORIOS  39-53
		insertarCampo("noObligatorios39a53", 85, null, false, String.class);//longitud suma de los campos 12 a 26
		
		insertarCampo("razonSoc", 100, null, true, String.class);
		insertarCampo("apellido1", 50, null, true, String.class);
		insertarCampo("apellido2", 50, null, true, String.class);
		 
		//NO OBLIGATORIOS  57-60
		insertarCampo("noObligatorios57a60", 89, null, false, String.class);//longitud suma de los campos 12 a 26
		
		insertarCampo("terIdNif", 1, null, true, Double.class);
		insertarCampo("terIdNif", 15, null, true, String.class);
		insertarCampo("terNom", 40, null, true, String.class);
		insertarCampo("terNif14", 9, null, true, String.class);
		insertarCampo("tBienTran", 1, null, true, Boolean.class);
		insertarCampo("tBienCod", 10, null, true, String.class);
		insertarCampo("transInm", 1, null, true, Boolean.class);

		//NO OBLIGATORIOS  68-87
		insertarCampo("noObligatorios68a87", 286, null, false, String.class);//longitud suma de los campos 12 a 26
		
	}
	
	/**
	 * Constructor de la clase Apunte. Añade por defecto el numero de asiento que se le pasa por parámetros.
	 * 
	 * @param numeroAsiento El número que llevará el asiento.
	 */
	public Apunte(Integer numeroAsiento){
		this();
		modificarCampo("numeroAsiento", numeroAsiento);
	}
	
	
	public String toString(){
		String result="\n(APUNTE ";
		result+="(fecha:"+this.getValor("fecha")+" )";
		if(this.getValor("subCuenta")!=null)
			result+="(subCuenta:"+this.getValor("subCuenta")+" )";
		if(this.getValor("contrapartida")!=null)
			result+="(contrapartida:"+this.getValor("contrapartida")+" )";
		result+="\n(importeDebeEuros:"+this.getValor("importeDebeEuros")+" )";
		result+="(importeHaberEuros:"+this.getValor("importeHaberEuros")+" )";
		result+="(baseIVAEuros:"+this.getValor("baseIVAEuros")+" )";
		if(this.getValor("porcentajeIVA")!=null)
			result+="(porcentajeIVA:"+this.getValor("porcentajeIVA")+" )";
		if(this.getValor("porcentajeRE")!=null)
			result+="(porcentajeRE:"+this.getValor("porcentajeRE")+" )";
		result+="\n(concepto:"+this.getValor("concepto")+" ))";
		return result;
		
		
	}
	
}	


