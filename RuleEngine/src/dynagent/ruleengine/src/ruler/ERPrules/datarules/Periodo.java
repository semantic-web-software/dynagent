package dynagent.ruleengine.src.ruler.ERPrules.datarules;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;

import dynagent.common.utils.Auxiliar;

/**
 * Un periodo representa un intervalo de tiempo en el que se especifica el
 * inicio y fin del mismo, asu como las horas disponibles cada dua de la semana
 * para ese periodo. Un periodo puede ser de longitud menor a una semana, en
 * ese caso, no se tendrun en cuenta las horas de los dias que no entren dentro
 * del periodo definido por inicio-fin
 * 
 * Estos periodos no deben solaparse nunca, aunque de esto se encargarun las
 * reglas de negocio que no deben permitir que se inserten periodos solapados.
 * 
 * La(s) regla(s) que mantienen los calendarios actualizados con respecto a los
 * objetos de la base de datos se encargan de verificar la coherencia de los
 * datos que se introducen: - 0 < horas <= 24 - fechaFin > fechaInicio
 * 
 * @author Daruo Veledo.
 */
public class Periodo {

	private String id;
	/**
	 * Valor en milisegundos desde el 1 de Enero de 1970 ('epoch') de la fecha de INICIO
	 * del periodo
	 */
	private Long fechaInicio;
	/**
	 * Valor en milisegundos desde el 1 de Enero de 1970 ('epoch') de la fecha de FIN del
	 * periodo.
	 */
	private Long fechaFin;
	private Integer horasLunes;
	private Integer horasMartes;
	private Integer horasMiercoles;
	private Integer horasJueves;
	private Integer horasViernes;
	private Integer horasSabado;
	private Integer horasDomingo;

	/**
	 * Constructor por defecto de la clase.
	 */
	public Periodo(String id, Long fechaInicio, Long fechaFin,
			Integer horasLunes, Integer horasMartes, Integer horasMiercoles,
			Integer horasJueves, Integer horasViernes, Integer horasSabado, Integer horasDomingo) {
		super();
		this.id = id;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.horasLunes = horasLunes;
		this.horasMartes = horasMartes;
		this.horasMiercoles = horasMiercoles;
		this.horasJueves = horasJueves;
		this.horasViernes = horasViernes;
		this.horasSabado = horasSabado;
		this.horasDomingo = horasDomingo;
	}
	
	/**
	 * Actualiza por completo un periodo que ya existe, no se tiene en cuenta
	 * valores que pudiesen ser iguales a los anteriores.
	 * 
	 * @param p El <code>Periodo</code> a actualizar.
	 */
	public void actualizarPeriodo(Periodo p) {
		this.fechaInicio = p.getFechaInicio();
		this.fechaFin = p.getFechaFin();
		this.horasLunes = p.horasLunes;
		this.horasMartes = p.horasMartes;
		this.horasMiercoles = p.horasMiercoles;
		this.horasJueves = p.horasJueves;
		this.horasViernes = p.horasViernes;
		this.horasSabado = p.horasSabado;
		this.horasDomingo = p.horasDomingo;
	}
	
	/**
	 * Devuelve la fecha de inicio en milisegundos del periodo.
	 * 
	 * @return La fecha inicio del periodo en milisegundos.
	 */
	public Long getFechaInicio() {
		return fechaInicio;
	}
	
	public GregorianCalendar getFechaInicioCalendar(){
		GregorianCalendar fechaInicioCalendar = new GregorianCalendar();
		fechaInicioCalendar.setTimeInMillis(fechaInicio);
		
		return fechaInicioCalendar;
	}

	/**
	 * Devuelve la fecha de fin en milisegundos del periodo.
	 * 
	 * @return La fecha fin del periodo en milisegundos.
	 */
	public Long getFechaFin() {
		return fechaFin;
	}
	
	public GregorianCalendar getFechaFinCalendar(){
		GregorianCalendar fechaFinCalendar = new GregorianCalendar();
		fechaFinCalendar.setTimeInMillis(fechaFin);
		
		return fechaFinCalendar;
	}
	
	/**
	 * Establece la fecha de inicio del periodo. Necesario para calcular
	 * los periodos hubiles en CalendarioHabil.
	 * 
	 * @param fechaInicio the fechaInicio to set
	 */
	public void setFechaInicio(Long fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	/**
	 * Establece la fecha de fin del periodo. Necesario para calcular
	 * los periodos hubiles en CalendarioHabil.
	 * 
	 * @param fechaFin the fechaFin to set
	 */
	public void setFechaFin(Long fechaFin) {
		this.fechaFin = fechaFin;
	}

	/**
	 * Mutodo que devuelve el identificador con el que se ha guardado
	 * el periodo en la base de datos (el 'rdn')
	 * 
	 * @return El rdn del periodo.
	 */
	public String getID(){
		return id;
	}

	/**
	 * Devuelve el numero de horas disponibles entre dos fechas que se pasan 
	 * como parumetros. Se encarga de comprobar si existen periodos de exclusiun
	 * que afecten, y en ese caso, calcula la cantidad de horas que afectan al 
	 * cumputo total.
	 * 
	 * @param ini Fecha inicio del periodo en que buscamos. ini>=Periodo.fechaInicio
	 * @param fin Fecha fin del periodo en que buscamos. fin>=Periodo.fechaFin
	 * @param exclusionesImplicadas Una lista con las exclusiones que puedan suceder
	 * 								entre ini y fin.
	 * 
	 * @return Horas disponibles del periodo.
	 */
	public double getHoras(long ini, long fin, LinkedList<Exclusion> exclusionesImplicadas){
		
		int diaDeLaSemana;
		/*
		 * Longitud del periodo en duas. Sumamos 1 ya que la fecha de fin 
		 * se incluye dentro del periodo.
		 */
			
		Calendar cfin = Calendar.getInstance();
		cfin.setTimeInMillis(fin);
		int diaFin=cfin.get(Calendar.DAY_OF_YEAR);
		/*
		 * Calendario que apunta inicialmente a la fecha
		 * de inicio del periodo y que serviru para ir obteniendo 
		 * informacion de los duas
		 */
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(ini);
			
		double horaDoublePartida=Operaciones.getHoraDouble(c);
		
		double totalHoras = -horaDoublePartida; //Si fin es mayor que ini, comenzare sumando las horas habiles del dia, pero debo descontar de donde parto.
		//System.err.println("GETHORAS hora partida "+horaDoublePartida); 
		/*
		 * Acumulamos las horas disponibles. Repetimos la operaciun
		 * por cada dua del periodo (variable 'longPeriodo')
		 */
		int diaWhile=c.get(Calendar.DAY_OF_YEAR);
		int añoFin=cfin.get(Calendar.YEAR);
		int añoWhile=c.get(Calendar.YEAR);
		
		while(añoFin==añoWhile&& diaWhile<=diaFin) {
			//System.err.println("GETHORAS2 "+cfin.get(Calendar.YEAR)+":"+diaFin+" "+c.get(Calendar.YEAR)+":"+diaWhile); 
			diaDeLaSemana = c.get(Calendar.DAY_OF_WEEK);
			if(!Operaciones.isExcluible(c, exclusionesImplicadas)){		
				int horasDia=getHorasDia(diaDeLaSemana);
				if(diaFin>diaWhile){
					//System.err.println("DIA COMPLETO, Dia: "+c.get(Calendar.DAY_OF_MONTH)+", horas: "+getHorasDia(diaDeLaSemana));
					totalHoras += horasDia;
				}else{										
					double horaFinDouble=Operaciones.getHoraDouble(cfin);
					
					if( horaFinDouble<horasDia ){						
						totalHoras+=Operaciones.getHoraDouble(cfin);
					}else
						totalHoras+=horasDia;
					//System.err.println(" DIA PARCIAL: "+c.get(Calendar.DAY_OF_MONTH)+", horas: "+getHorasDia(diaDeLaSemana)+" "+totalHoras);
				}			
			}
			/*
			 * Aumentamos el calendario en un día, al usar GregorianCalendar
			 * podemos aumentar un día, sin perder los cambios de mes e ignorando
			 * los cambios de horario de verano.
			 */
			c.add(Calendar.DATE, 1);
			añoWhile=c.get(Calendar.YEAR);
			diaWhile=c.get(Calendar.DAY_OF_YEAR);
		}
		
		return totalHoras;
	}
	
	public long getFechaFin(long iniM, double horas, LinkedList<Exclusion> exclusionesImplicadas, double porcDedicacion){
		//retorna en millis
		int diaDeLaSemana;
		//puede ser que la diferencia entre la fecha fin y la ini de entrada sea anterior a la fecha inicio del periodo
		
		if(porcDedicacion==0.0) return iniM;
		
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(iniM);
		double horaDoublePartida=Operaciones.getHoraDouble(c);
		//System.err.println("DENTRO GET FECHA FIN PERIODO "+horaDoublePartida);
		
		double totalHorasADedicacion = -horaDoublePartida*porcDedicacion;
		//aqui voy almacenando las horas sin error de dedicacion, y sera la que utilice el ultimo dia
		double totalHoras = -horaDoublePartida;
		
		Calendar cfin = Calendar.getInstance();
		cfin.setTimeInMillis(this.fechaFin);
		int diaFin=cfin.get(Calendar.DAY_OF_YEAR);
		int diaWhile=c.get(Calendar.DAY_OF_YEAR);
		
		int añoFin=cfin.get(Calendar.YEAR);
		int añoWhile=c.get(Calendar.YEAR);
		while(añoFin==añoWhile&& diaWhile<=diaFin) {
			diaDeLaSemana = c.get(Calendar.DAY_OF_WEEK);
			//System.err.println("DIA "+diaDeLaSemana);
			
			if(!Operaciones.isExcluible(c, exclusionesImplicadas)){
				double horasDia= getHorasDia(diaDeLaSemana);
				double horasDiaDedicacion= horasDia*porcDedicacion;
				
				//System.err.println(""+totalHorasADedicacion+" Dia: "+c.getTime()+", horas dia a dedicacion: "+horasDiaDedicacion+" hora dia "+getHorasDia(diaDeLaSemana)+" "+porcDedicacion);				
				
				if(totalHorasADedicacion+horasDiaDedicacion >=horas){									
					double restoHorasHoy=horas/porcDedicacion-totalHoras;
					Operaciones.setHorasDia(c,restoHorasHoy);
																				
					//System.err.println("ALCANZADO "+" Dia: "+c.getTime()+", horas: "+restoHorasHoy);
					return c.getTime().getTime();
				}else{
					totalHorasADedicacion+=horasDiaDedicacion;
					totalHoras+=horasDia;
				}
			}
			c.add(Calendar.DATE, 1);
			diaWhile=c.get(Calendar.DAY_OF_YEAR);
			añoWhile=c.get(Calendar.YEAR);
		}
		
		return this.fechaFin+3600000;//las horas solicitadas exceden este periodo
	}
	/**
	 * Sobrecarga del metodo getHorasPeriodo que nos devuelve el total de horas
	 * del periodo completo.
	 * 
	 * @param exclusionesImplicadas Una lista con las exclusiones que puedan suceder
	 * 								entre ini y fin.
	 * 
	 * @return El total de horas del periodo completo, teniendo en cuenta las
	 * exclusiones que pudiesen afectarle.
	 */
	public double getHoras(LinkedList<Exclusion> exclusionesImplicadas){
		return getHoras(fechaInicio, fechaFin, exclusionesImplicadas);
	}
	
	/**
	 * Sobrecarga del metodo getHorasPeriodo que nos devuelve el total de horas
	 * del periodo completo.
	 * 
	 * @return El total de horas del periodo completo, SIN TENER en cuenta las
	 * exclusiones que pudiesen afectarle.
	 */
	public double getHoras(){
		return getHoras(fechaInicio, fechaFin, null);
	}
	
	/**
	 * Devuelve el numero de horas que tiene definidas el periodo
	 * para el dua de la semana que se le pasa como parumetro.
	 * 
	 * @param diaDeLaSemana Dua de la semana del que queremos saber el nu de horas.
	 * 
	 * @return Horas hubiles definidas para ese dua de la semana.
	 */
	public int getHorasDia(int diaDeLaSemana){
		switch(diaDeLaSemana){
		case Operaciones.DOMINGO:
			return horasDomingo;
		case Operaciones.LUNES:
			return horasLunes;
		case Operaciones.MARTES:
			return horasMartes;
		case Operaciones.MIERCOLES:
			return horasMiercoles;
		case Operaciones.JUEVES:
			return horasJueves;
		case Operaciones.VIERNES:
			return horasViernes;
		case Operaciones.SABADO:
			return horasSabado;
		default:
			return 0;
		}
	}
	
	public void setFinToDay(int dayOfYear){
		Calendar fin=getFechaInicioCalendar();
		fin.set(Calendar.DAY_OF_YEAR,dayOfYear);
		
		int horasDia= getHorasDia(fin.get(Calendar.DAY_OF_WEEK));
		fin.set(Calendar.HOUR_OF_DAY,Math.max(0,horasDia-1));
		if(horasDia>0){
			fin.set(Calendar.MINUTE,59);
			fin.set(Calendar.SECOND,59);
		}else{
			fin.set(Calendar.HOUR_OF_DAY,0);
			fin.set(Calendar.MINUTE,0);
			fin.set(Calendar.SECOND,0);						
		}
		setFechaFin(fin.getTimeInMillis());
	}
	
	public void setIniToNextDay(int dayOfYear){
		Calendar ini=getFechaFinCalendar();
		int maximo=ini.getActualMaximum(Calendar.DAY_OF_YEAR);
		if(dayOfYear<maximo) dayOfYear++;
		
		ini.set(Calendar.DAY_OF_YEAR,dayOfYear);
		
		ini.set(Calendar.HOUR_OF_DAY,0);
		ini.set(Calendar.MINUTE,0);
		ini.set(Calendar.SECOND,0);						
		
		setFechaInicio(ini.getTimeInMillis());
	}
	/**
	 * Redefiniciun del metodo toString para la clase <code>Periodo</code>
	 */
	public String toString() {
		String mensaje;

		mensaje = "|\t\"" + id + "\"\n";
		Date d = new Date(fechaInicio);
		mensaje += "|\t\tInicio: " + d.toString() + "\n";
		d = new Date(fechaFin);
		mensaje += "|\t\tFin: " + d.toString() + "\n";
		mensaje += "|\t\tHoras: " + horasLunes + ", " + horasMartes + ", "
				+ horasMiercoles + ", " + horasJueves + ", " + horasViernes
				+ ", " + horasSabado + ", " + horasDomingo + "\n";

		return mensaje;
	}

	public Periodo clone() {
		return new Periodo(id, fechaInicio, fechaFin, horasLunes, horasMartes,
				horasMiercoles, horasJueves, horasViernes, horasSabado,
				horasDomingo);
	}
}