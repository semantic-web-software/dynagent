package dynagent.ruleengine.src.ruler.ERPrules.datarules;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * La clase Exclusion representa intervalos de tiempo, generalmente menores que
 * los de la clase Periodo, en los que se indican intervalos en los que no se
 * trabaja (equivalente a 0 horas en cada dia de la exclusion).
 * 
 * Las exclusiones puede ser dias concretos si la fecha inicio y la fecha de fin
 * se corresponden al mismo dia o periodos de tiempo de mes de un dia.
 * 
 * No es posible insertar 2 exclusiones que se solapen, es decir, que la fecha
 * de inicio de uno sea menor a la fecha de fin de otro ya existente.
 * 
 * @author Dario Veledo.
 */
public class Exclusion {

	/**
	 * Identificador de la exclusion. Es el RDN del objeto EXCLUSION que se
	 * guarda en base de datos.
	 */
	private String idExclusion;
	/**
	 * Valor en milisegundos desde el 1 de Enero de 1970 ('epoch') de la fecha de INICIO
	 * del periodo.
	 */
	private GregorianCalendar fechaInicio;
	/**
	 * Valor en milisegundos desde el 1 de Enero de 1970 ('epoch') de la fecha de FIN del
	 * periodo.
	 */
	private GregorianCalendar fechaFin;

	/**
	 * Constructor por defecto de la clase.
	 */
	public Exclusion(String idExclusion, Date fechaInicio, Date fechaFin) {
		super();
		this.idExclusion = idExclusion;
		// Fecha de Inicio
		GregorianCalendar cInicio = new GregorianCalendar();
		cInicio.setTime(fechaInicio);
		this.fechaInicio = cInicio;
		//Fecha de FIn
		GregorianCalendar cFin = new GregorianCalendar();
		cFin.setTime(fechaFin);
		this.fechaFin = cFin;
	}
	
	/**
	 * Actualiza por completo una exclusion que ya existe, no se tiene en cuenta
	 * valores que pudiesen ser iguales a los anteriores.
	 */
	public void actualizarExclusion(Exclusion e){
		// Fecha de Inicio
		GregorianCalendar cInicio = new GregorianCalendar();
		cInicio.setTime(e.getFechaInicio().getTime());
		this.fechaInicio = cInicio;
		//Fecha de FIn
		GregorianCalendar cFin = new GregorianCalendar();
		cFin.setTime(e.getFechaFin().getTime());
		this.fechaFin = cFin;
	}
	
	/**
	 * Devuelve el identificador de la exclusion en base de datos.
	 * 
	 * @return El rdn de la exclusion.
	 */
	public String getID(){
		return idExclusion;
	}

	/**
	 * Devuelve la fecha de inicio de la exclusion.
	 * 
	 * @return Una instancia de <code>Calendar</code> con la fecha
	 * de inicio del periodo.
	 */
	public GregorianCalendar getFechaInicio() {
		return (GregorianCalendar) fechaInicio.clone();
	}

	/**
	 * Devuelve la fecha de fin de la exclusion.
	 * 
	 * @return Una instancia de <code>Calendar</code> con la fecha
	 * de fin del periodo.
	 */
	public GregorianCalendar getFechaFin() {
		return (GregorianCalendar) fechaFin.clone();
	}
	
	/**
	 * Redefinicion del mrtodo toString para la clase <code>Periodo</code>
	 */
	public String toString(){
		String mensaje;
		
		mensaje = "|\t\""+idExclusion+"\"\n";
		mensaje += "|\t\tInicio: "+fechaInicio.getTime().toString()+"\n";
		mensaje += "|\t\tFin: "+fechaFin.getTime().toString()+"\n";
		
		return mensaje;
	}
}