package dynagent.ruleengine.src.ruler.ERPrules.datarules;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;

import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.DataRules;

/**
 * La clase <code>CalendarioHabil</code> es un recurso imprescindible para la
 * gestion de tiempos. Cada calendario viene definido por una fecha de inicio y
 * una fecha de fin. Ademas cuenta con dos listas, la primera es la lista de
 * periodos habiles que contiene todos los periodos en los que se trabaja y las
 * horas que se trabaja cada uno de los dias de la semana, y la segunda es la
 * lista de exclusiones, que representan periodos de tiempo en los que no se
 * trabaja.
 * 
 * Con esto, se pueden tener varios calendarios habiles en funcion de las
 * caracteristicas del recurso o su el regimen de funcionamiento.
 * 
 * Hereda de DataRules, necesario si queremos que las reglas se reevaluen cuando
 * se cambia alguno de los valores del objeto.
 * 
 * @author Dario Veledo.
 */
public class CalendarioHabil extends DataRules{

	/**
	 * El identificador del calendario. Se corresponde con su valor del
	 * RDN en base de datos
	 */
	private String idCalendario;
	/**
	 * Valor en milisegundos desde el 1 de Enero de 1970 ('epoch') de la fecha de INICIO
	 * del periodo
	 */
	private Long inicioCalendario;
	/**
	 * Valor en milisegundos desde el 1 de Enero de 1970 ('epoch') de la fecha de FIN del
	 * periodo.
	 */
	private Long finCalendario;
	private LinkedList<Periodo> periodos;
	private LinkedList<Exclusion> exclusiones;

	/**
	 * Constructor por defecto de la clase. Necesita un objeto <code>IKnowledgeBaseInfo</code>
	 * ya que hereda de DataRules.
	 */
	public CalendarioHabil(String id, IKnowledgeBaseInfo ik) {
		super(ik);
		this.idCalendario = id;
		this.inicioCalendario = null;
		this.finCalendario = null;
		this.periodos = new LinkedList<Periodo>();
		this.exclusiones = new LinkedList<Exclusion>();
		
		/*
		 * Notificamos al motor el cambio en el ID.
		 */
		//pcs.firePropertyChange("id", null, id);
	}
	
	/**
	 * Metodo para obtener el ID del calendario en las reglas.
	 * 
	 * @return El ID del Calendario.
	 */
	public String getID(){
		return idCalendario;
	}
	
	/**
	 * Devuelve la fecha inicio del calendario como un objeto
	 * Date. Se usa para los setValue con TimeValue.
	 * 
	 * @return La fecha de inicio del calendario.
	 */
	public Date getFechaInicio(){
		return new Date(inicioCalendario);
	}
	
	/**
	 * Devuelve la fecha fin del calendario como un objeto
	 * Date. Se usa para los setValue con TimeValue.
	 * 
	 * @return La fecha de fin del calendario.
	 */
	public Date getFechaFin(){
		return new Date(finCalendario);
	}
	
	/**
	 * Aeade un periodo de tiempo a una lista, donde se especifica ademas, 
	 * las horas que se trabajan cada dia de la semana durante ese periodo.
	 * Se encarga de revisar si existia previamente un periodo con el mismo
	 * identificador, en ese caso lo actualizar. Si no encuentra ningun
	 * lo inserta nuevo.
	 * 
	 * En cualquiera de los casos, tras insertar/actualizar, se actualizan
	 * las fechas de inicio/fin del calendario.
	 *  
	 * @param p El <code>Periodo</code> a insertar.
	 */
	public void addPeriodo(Periodo p) {

		/*
		 * Recorremos primero todos los periodos ya existentes por si hubiese alguno
		 * ya con ese identificador. En caso de encontrarlo, lo actualizamos con los
		 * valores nuevos y acabamos.
		 */
		
		int pos=0, posNew=0;
		for (Periodo periodo : periodos) {
			
			if(periodo.getID().equals(p.getID())){
				periodo.actualizarPeriodo(p);
				/*
				 * Actualizamos, en caso de ser necesario, las fechas de inicio y fin
				 * del calendario.
				 */
				actualizarFechaInicioYFin(p.getFechaInicio(), p.getFechaFin());
				return;
			}
			
			if( p.getFechaInicio()<periodo.getFechaInicio() && posNew > pos) posNew=pos;
			pos++;
			
		}
		
		/*
		 * En caso de no haber encontrado un periodo con el mismo id en la lista, 
		 * lo insertamos nuevo y acabamos.
		 */
		actualizarFechaInicioYFin(p.getFechaInicio(), p.getFechaFin());
		periodos.add(posNew,p);
	}

	/**
	 * Añade un periodo de exclusion, es decir, un periodo de tiempo en el que
	 * no se trabaja. Con esto se logra anular sub-periodos de trabajo de los 
	 * que se almacenan en la lista <code>periodos</code>. En casi de que ya
	 * existiese previamente una exclusion con el mismo identificador, se
	 * actualizar la existente. Si no, se inserta nueva.
	 * 
	 * En cualquiera de los casos, tras insertar/actualizar, se actualizan
	 * las fechas de inicio/fin del calendario.
	 * 
	 * @param e La <code>Exclusion</code> a insertar.
	 */
	public void addExclusion(Exclusion e){

		/*
		 * Recorremos primero todos las exclusiones ya existentes por si hubiese alguna
		 * ya con ese identificador. En caso de encontrarla, la actualizamas con los
		 * valores nuevos y acabamos.
		 */
		for (Exclusion exclusion : exclusiones) {
			if(exclusion.getID().equals(e.getID())){
				exclusion.actualizarExclusion(e);
				return;
			}
		}
		
		/*
		 * En caso de no haber encontrado una exclusion con el mismo id en la lista, 
		 * la insertamos nueva y acabamos.
		 */
		exclusiones.add(e);
	}
	
	/**
	 * Elimina de la lista de periodos el periodo especificado por el identificador
	 * que se le pasa por parametros (en caso de existir). Tras eso, actualizamos las 
	 * fechas de inicio y fin del calendario.
	 * 
	 * @param idPeriodo Identificador (RDN) del periodo a borrar.
	 */
	public void removePeriodo(String idPeriodo){
	
		for (Periodo periodo : periodos) {
			if(periodo.getID().equals(idPeriodo)){
				periodos.remove(periodo);
				actualizarFechaInicioYFinEnBorrado(periodo);
				
				return;
			}
		}
	}

	/**
	 * Elimina de la lista de exclusiones la exclusion especificada por el identificador
	 * que se le pasa por parametros (en caso de existir)
	 * 
	 * @param idExclusion Identificador (RDN) de la exclusione a borrar.
	 */
	public void removeExclusion(String idExclusion){
		
		for (Exclusion exclusion : exclusiones) {
			if(exclusion.getID().equals(idExclusion)){
				exclusiones.remove(exclusion);
				
				return;
			}
		}
	}
	
	/**
	 * Sobrecarga del metodo <code>getHorasHabiles(Long ini, Long fin)</code>
	 * que devuelve el numero de horas TOTALES del calendario. Desde la fecha mas
	 * antigua que aparezca en todos los periodos hasta las mas nueva.
	 * 
	 * @return El numero de horas totales habiles del calendario.
	 */
	public double getHorasHabiles(){
		//System.err.println("GETHORASHABILES-1"); 
		if(inicioCalendario == null || finCalendario == null){
			return 0;
		}else{
			return getHorasHabilesMillis(inicioCalendario, finCalendario);
		}
	}
	
	public long getIniToNextDay(long millis){//Devuelve milisegundos
		Calendar ini=Calendar.getInstance();
		ini.setTimeInMillis(millis);
		double horaPartidaDouble=Operaciones.getHoraDouble(ini);
		
		//Ahora me posiciono al comienzo del dia
		ini.set(Calendar.HOUR_OF_DAY,0);
		ini.set(Calendar.MINUTE,0);
		ini.set(Calendar.SECOND,0);
	
		double horasHabilesDia= getHorasHabilesMillis(ini.getTimeInMillis(),ini.getTimeInMillis()+24*3600000-1000);
		
		//comparo con horas habiles dia menos un sg
		if( horaPartidaDouble<= horasHabilesDia-1.0/3600 ){
			//Debo quitar el offset de horas de partida, a las horas no consumidas del dia que comienzan por cero.
			//POr ejemplo, si partimos del dia 30 a parit de la hora 3, y ese dia tiene 8 horas habiles, la fecha deseada comienza a partir de esa misma tercera hora
			return millis;
		}else		
			return getIniDay(ini.get(Calendar.DAY_OF_YEAR)+1);
	}
	
	public long getEndDiaPrevio(long millis){//Devuelve milisegundos
		Calendar fin=Calendar.getInstance();
		fin.setTimeInMillis(millis);		
		double horaPartidaDouble=Operaciones.getHoraDouble(fin);				
		
		//Si de este dia no queda horas habiles devuelve el fin del dia anterior
		if( horaPartidaDouble< 1.0/3600 ){
			return getEndOfDay(fin.get(Calendar.DAY_OF_YEAR)-1);
		}else{			
			//Debo quitar el offset de horas de partida, a las horas no consumidas del dia que comienzan por cero.
			//POr ejemplo, si partimos del dia 30 a parit de la hora 3, y ese dia tiene 8 horas habiles, la fecha deseada comienza a partir de esa misma tercera hora
			return millis;
		}		
	}
	
	public long getEndOfDay(int dayOfYear){
		Calendar fin=Calendar.getInstance();
		fin.setTimeInMillis(finCalendario);		
		fin.set(Calendar.DAY_OF_YEAR,dayOfYear);
		fin.set(Calendar.HOUR_OF_DAY,0);
		fin.set(Calendar.MINUTE,0);
		fin.set(Calendar.SECOND,0);	
		
		//System.err.println("GETEND "+dayOfYear);
		double horas= getHorasHabilesMillis(fin.getTimeInMillis(),fin.getTimeInMillis()+24*3600000-1000);
		//System.err.println("GETEND 2 "+horas);
		Operaciones.setHorasDia(fin,Math.max(0,horas));
		//System.err.println("GETEND 3 "+fin.getTime());
		return fin.getTimeInMillis();
	}
	
	public long getIniDay(int dayOfYear){
		Calendar ini=Calendar.getInstance();
		int maximo=ini.getActualMaximum(Calendar.DAY_OF_YEAR);
		//if(dayOfYear<maximo) dayOfYear++;
		
		ini.set(Calendar.DAY_OF_YEAR,dayOfYear);
		
		ini.set(Calendar.HOUR_OF_DAY,0);
		ini.set(Calendar.MINUTE,0);
		ini.set(Calendar.SECOND,0);						
		
		return ini.getTimeInMillis();
	}
	
	
	public long getFechaFin(long iniSg, double horas,double porcDedicacion/*de 0 a 1*/){
		//Retorna fecha en milisegundos
		long iniM = iniSg*1000L;//en millis					
		
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(iniSg);			
		long horaDoublePartidaInSg=new Double(Operaciones.getHoraDouble(c)*3600).longValue();
		if(porcDedicacion==0.0) return horaDoublePartidaInSg*1000;
		
		double horasDisponibles = horas;
		/*
		 * 
		 * A priori no se si las horas a sumar a la fecha inicio supera a la fecha fin
		 */
		for (Periodo periodo : periodos) {
			long fin=periodo.getFechaFin();
			long fechaPosibleFin=periodo.getFechaFin(iniM,horasDisponibles,exclusiones,porcDedicacion);
			
			if( fechaPosibleFin<= fin){
				//System.err.println("CALDBG:GOOD "+periodo.getID()+" "+periodo.getFechaInicio()+" "+fechaPosibleFin);
				return fechaPosibleFin;
			}else{
				//System.err.println("CALDBG:NEXT "+periodo.getFechaInicio()+" "+horasDisponibles+" "+periodo.getHoras(exclusiones));		
				double horasPeriodo=periodo.getHoras(exclusiones);
				
				horasDisponibles = 	horasDisponibles-horasPeriodo-horaDoublePartidaInSg;	
				horaDoublePartidaInSg-= Math.min(horaDoublePartidaInSg,horasPeriodo);
			}
		}		
		return finCalendario;		
	}
	

	/**
	 * Sobrecarga del metodo <code>getHorasHabiles(Long ini, Long fin)</code>
	 * que devuelve el numero de horas TOTALES del calendario. Recibe las
	 * fechas de inicio y fin en SEGUNDOS.
	 * 
	 * @param ini Fecha de inicio en milisegundos.
	 * @param fin Fecha de fin en milisegundos.
	 * 
	 * @return El numero de horas totales habiles del calendario.
	 */
	public double getHorasHabiles(Long ini, Long fin){
		/*
		 * Pasamos ambos valores a MILISEGUNDOS:
		 */
		ini *= 1000L;
		fin *= 1000L;
		//System.err.println("DEBUG HB 1");
		return getHorasHabilesMillis(ini, fin);
	}
	
	//Horas relativas: se desplazan (retrasando) ahsta situar la hora de inicio a las 00:00, por que solo cuenta la diferencia en un dia
	public double getHorasHabilesRelativas(Long ini, Long fin){
		/*
		 * Pasamos ambos valores a MILISEGUNDOS:
		 */
		ini *= 1000L;
		fin *= 1000L;
		//System.err.println("DEBUG HB 1");
		Calendar c=Calendar.getInstance();
		c.setTimeInMillis(ini);
		int yearIni=c.get(Calendar.YEAR);	
		int diaIni=c.get(Calendar.DAY_OF_YEAR);
		Double horaIni=Operaciones.getHoraDouble(c);
						
		return getHorasHabilesMillis(ini-horaIni.longValue()*3600000, fin-horaIni.longValue()*3600000);
	}
	
	/**
	 * Sobrecarga del metodo <code>getHorasHabiles(Long ini, Long fin)</code>
	 * que devuelve el numero de horas TOTALES del calendario. Recibe objetos de
	 * tipo DATE para hacer las reglas mas claras y legibles.
	 * 
	 * @param ini Date con la fecha de inicio.
	 * @param fin Date con la fecha de fin.
	 * 
	 * @return El numero de horas totales hubiles del calendario.
	 */
	public double getHorasHabiles(Date ini, Date fin){
		if(inicioCalendario == null || finCalendario == null){
			return 0;
		}else{
			return getHorasHabilesMillis(ini.getTime(), fin.getTime());
		}
	}
	
	/**
	 * Obtiene el numero de horas REALES habiles, en funcion del intervalo de
	 * tiempo señalado y de las exclusiones que pudiese haber.
	 * 
	 * @param ini Inicio del periodo de calculo en MILISEGUNDOS.
	 * @param fin Fin del periodo de calculo en MILISEGUNDOS. INCLUSIVE.
	 * 
	 * @return El numero de horas habiles.
	 */
	private double getHorasHabilesMillis(Long ini, Long fin){
		double horasDisponibles = 0;
		//System.err.println("DEBUG HB 2");
		LinkedList<Exclusion> exclusionesImplicadas = new LinkedList<Exclusion>();
		 
		/*
		 * Acumulamos todas las exclusiones que puedan afectar al periodo de 
		 * tiempo determinado por 'ini' y 'fin'
		 */
		for (Exclusion exclusion : exclusiones) {
		
			if(exclusion.getFechaInicio().getTimeInMillis()<=fin && exclusion.getFechaFin().getTimeInMillis()>=ini){
		
				exclusionesImplicadas.add(exclusion);
			}
		}
		
		/*
		 * Por cada periodo, calculamos con cuantas horas contribuye al total.
		 */
		for (Periodo periodo : periodos) {
			//System.err.println("DEBUG HB 3");
			if(periodo.getFechaInicio()<=fin && periodo.getFechaFin()>=ini){
				//System.err.println("DEBUG HB 4");
				horasDisponibles += calcularHorasPeriodo(ini, fin, periodo, exclusionesImplicadas);
			}
		}
		
		return horasDisponibles;
	}

	/**
	 * Calcula el numero de horas disponibles entre 2 fechas. Comprobamos
	 * primero las fechas limite (fecha fin y fecha inicio):
	 * <ul>
	 * <li>Si el periodo del que queremos saber las horas disponibles esta entre
	 * la fecha de inicio y la de fin, devolvemos el total de horas de este
	 * periodo.</li>
	 * <li>Si la fecha inicio o fin del periodo es anterior (o posterior en el
	 * caso de la fecha fin) a la fecha inicial de la busqueda, hacemos que la
	 * fecha inicial/final de busqueda sea la fecha de inicio (o fecha fin) de
	 * la busqueda</li>
	 * 
	 * @param ini Fecha inicial del calculo de horas.
	 * @param fin Fecha final del calculo de horas.
	 * @param periodo Periodo en el que calculamos.
	 * @param porcDedicacion 
	 * 
	 * @return El numero de horas disponibles para el periodo entre las fechas
	 * de inicio y fin.
	 */
	private double calcularHorasPeriodo(Long ini, Long fin, Periodo periodo, LinkedList<Exclusion> exclusionesImplicadas) {
		long inicioParcial = periodo.getFechaInicio();
		long finParcial = periodo.getFechaFin();
		//System.out.println("GETHORASHABILES7");
		/*
		 * Si el periodo esta contenido POR COMPLETO dentro del intervalo
		 * ini-fin, devolvemos el total de horas de ese periodo, tras restar
		 * las exclusiones que pudiese haber.
		 */
		if(periodo.getFechaInicio()>=ini && periodo.getFechaFin()<=fin){
			//System.out.println("GETHORASHABILES8");
			return periodo.getHoras(exclusionesImplicadas);
		}else{
			//System.out.println("GETHORASHABILES9");
			/*
			 * En otro caso, ajustamos los limites de busqueda a los del periodo. 
			 */
			if(periodo.getFechaInicio()<ini){
				inicioParcial = ini;
			}
			
			if(periodo.getFechaFin()>fin){
				finParcial = fin;
			}
			
			return periodo.getHoras(inicioParcial, finParcial, exclusionesImplicadas);
		}		
	}
	
	private double calcularHorasPeriodo(Periodo periodo){
		return calcularHorasPeriodo(periodo.getFechaInicio(), periodo.getFechaFin(), periodo, null);
	}
	
	/**
	 * Metodo que actualiza la fecha de inicio del calendario, que coincide con la fecha de 
	 * inicio del periodo mas antiguo y la fecha de fin, que coincide con la fecha de fin
	 * del periodo mas actual.
	 * 
	 * @param inicioPeriodo Un objeto con la fecha de inicio del periodo.
	 * @param finPeriodo Un objeto con la fecha de fin del periodo.
	 */
	private void actualizarFechaInicioYFin(Long inicioPeriodo, Long finPeriodo){
		
		/*
		 * La primera vez que entramos inicioCalendario y finCalendario son nulos,
		 * asi que les ponemos el valor del primero periodo que entra en el calendario.
		 */
		if(inicioCalendario == null && finCalendario == null){
			inicioCalendario = inicioPeriodo;
			finCalendario = finPeriodo;
		}else{
			/*
			 * Si inicioCalendario y finCalendario tienen valor, actualizamos en 
			 * caso de ser necesario
			 */
			if(inicioPeriodo < this.inicioCalendario){
				this.inicioCalendario = inicioPeriodo;
			}
			
			if(finPeriodo > this.finCalendario){
				this.finCalendario = finPeriodo;
			}	
		}
	}
	
	/**
	 * Actualiza las fechas de inicio y fin del calendario tras el borrado
	 * de un periodo de la lista <code>periodos</code>. En el momento de
	 * actualizar la lista, el periodo ya no debe estar presente.
	 * 
	 * @param periodo El periodo que se ha borrado.
	 */
	private void actualizarFechaInicioYFinEnBorrado(Periodo periodo) {
		inicioCalendario = getMinimoInicio();
		finCalendario = getMaximoFin();
	}
	
	/**
	 * Obtiene el valor de <code>fechaInicio</code> menor de entre todos los
	 * periodos de la la lista <code>periodos</code>
	 * 
	 * @return El inicio del periodo mas antiguo.
	 */
	private Long getMinimoInicio(){
		Long minimoInicio = null;
		
		for (Periodo periodo : periodos) {
			if(minimoInicio == null){
				minimoInicio = periodo.getFechaInicio();
			}else{
				if(periodo.getFechaInicio() < minimoInicio){
					minimoInicio = periodo.getFechaInicio();
				}
			}
		}
		
		return minimoInicio;
	}
	
	/**
	 * Obtiene el valor de <code>fechaFin</code> mayor de entre todos los
	 * periodos de la la lista <code>periodos</code>
	 * 
	 * @return El fin del periodo mas actual.
	 */
	private Long getMaximoFin(){
		Long maximoFin = null;
		
		for (Periodo periodo : periodos) {
			if(maximoFin == null){
				maximoFin = periodo.getFechaFin();
			}else{
				if(periodo.getFechaFin() > maximoFin){
					maximoFin = periodo.getFechaFin();
				}
			}
		}
		
		return maximoFin;
	}
	
	/**
	 * Procesa todos los periodos del <code>CalendarioHabil</code> y devuelve
	 * un mapa de <periodos, horas_totales> en funcon de los periodos y de las exclusiones.
	 * Los valores de los atributos 'periodos' y 'exclusiones' no se ven afectados
	 * en ningun momento.
	 * 
	 * En caso de que exista una exclusion que parta por la mitad un periodo, estos dos
	 * nuevos periodos se almacenan en una lista y se llama recursivamente a esta
	 * misma funciun para que a su vez, calcule los periodos resultantes en funcion
	 * de las exclusiones.
	 * 
	 * Los periodos que se devuelven en el mapa podrian contener basura, ya que realmente
	 * los unicos valores que se necesitan son la fecha de inicio y fecha de final, el
	 * resto son los resultantes de haber clonado el objeto anterior.
	 * 
	 * @return Un mapa con cada periodo habil y el total de horas asociado a ese periodo.
	 */
	private HashMap<Periodo, Double> getPeriodosHabiles(LinkedList<Periodo> listaPeriodos){
		
		//Estos periodos solo van a tener fecha inicio y fecha de fin.
		HashMap<Periodo, Double> periodosHabiles = new HashMap<Periodo, Double>();
		//Periodos a procesar despues del procesado general
		LinkedList<Periodo> periodosPorProcesar = new LinkedList<Periodo>();
		Boolean isExcluible = false;
		//System.err.println(" GET PERIOD x "+listaPeriodos.size());
		for (Periodo periodo : listaPeriodos) {
			//System.err.println(" PERIODO x "+periodo);
			isExcluible = false;
			//Por cada periodo, vemos que exclusiones le afectan.
			Periodo clonPeriodo = periodo.clone();
			GregorianCalendar fechaInicioPeriodo = clonPeriodo.getFechaInicioCalendar();
			GregorianCalendar fechaFinPeriodo = clonPeriodo.getFechaFinCalendar();
			
			for (Exclusion exclusion : exclusiones) {
				//System.err.println(" EXCLUSION x ");
				/*							A			B
				 * 		Periodo: 			|===========|
				 * 		Exclusion:		 |=================|
				 * 						C<A				  D>B
				 */
				if (Operaciones.anteriorOIgualPorDias(exclusion.getFechaInicio(), fechaInicioPeriodo)
						&& Operaciones.posteriorOIgualPorDias(exclusion.getFechaFin(), fechaFinPeriodo)){
					isExcluible = true;
					break;
				}
				
				/*						A					B
				 * 		Periodo: 		|===================|
				 * 		Exclusion:		 |=================|
				 * 						C>A				  D<B
				 */
				if(Operaciones.posteriorPorDias(exclusion.getFechaInicio(), fechaInicioPeriodo)
						&& Operaciones.anteriorPorDias(exclusion.getFechaFin(), fechaFinPeriodo)){
					//clonPeriodoFin sera el periodo entre D y B
					Periodo clonPeriodoFin = periodo.clone();							
					
					
					clonPeriodoFin.setIniToNextDay(exclusion.getFechaFin().get(Calendar.DAY_OF_YEAR));
					
					clonPeriodo.setFechaFin(getEndOfDay(exclusion.getFechaInicio().get(Calendar.DAY_OF_YEAR)-1));
					//Añadimos a la lista de periodos por procesar
					periodosPorProcesar.add(clonPeriodo);
					periodosPorProcesar.add(clonPeriodoFin);
					//No continuamos
					isExcluible = true;
					break;
				}
				
				/*								A					B
				 * 		Periodo: 				|===================|
				 * 		Exclusion:		 |======|
				 * 		Exclusion:		 	|=====|
				 * 		Exclusion:		 		|
				 * 		Exclusion:		 		|=====|
				 */
				if(Operaciones.anteriorOIgualPorDias(exclusion.getFechaInicio(), fechaInicioPeriodo)
						&& Operaciones.anteriorPorDias(exclusion.getFechaFin(), fechaFinPeriodo)
						&& Operaciones.posteriorOIgualPorDias(exclusion.getFechaFin(), fechaInicioPeriodo)){
					clonPeriodo.setIniToNextDay(exclusion.getFechaFin().get(Calendar.DAY_OF_YEAR));
				}
				
				/*								A					B
				 * 		Periodo: 				|===================|
				 * 		Exclusion:		 							|======|
				 * 		Exclusion:		 						|=====|
				 * 		Exclusion:		 					  |=====|
				 * 		Exclusion:		 							|
				 */
				if(Operaciones.posteriorPorDias(exclusion.getFechaInicio(), fechaInicioPeriodo)
						&& Operaciones.posteriorOIgualPorDias(exclusion.getFechaFin(), fechaFinPeriodo)
						&& Operaciones.anteriorOIgualPorDias(exclusion.getFechaInicio(), fechaFinPeriodo)){
					clonPeriodo.setFechaFin(getEndOfDay(exclusion.getFechaInicio().get(Calendar.DAY_OF_YEAR)-1));					
				}
			}
			
			if(!isExcluible){
				/*
				 * Si el periodo no es excluible, es decir, no esta totalmente cubirto por
				 * exclusiones, lo añadimos junto con sus horas totales al mapa.
				 */
				periodosHabiles.put(clonPeriodo, calcularHorasPeriodo(clonPeriodo));
			}
		}
		
		/*
		 * Si hay periodos neuvos resultado de partir alguno de los periodos, llamamos
		 * recursivamente y procesamos. Al terminar, los añadimos a la lista de periodos
		 * habiles que devolveremos. 
		 */
		if(periodosPorProcesar.size() > 0){
			periodosHabiles.putAll(getPeriodosHabiles(periodosPorProcesar));
		}
		
		return periodosHabiles;
	}
	
	/**
	 * Sobrecarga del metodo <code>getPeriodosHabiles()</code> para poder invocarlo sobre
	 * el total de los periodos del calendario.
	 * 
	 * @return Un mapa con cada periodo habil y el total de horas asociado a ese periodo.
	 */
	public HashMap<Periodo, Double> getPeriodosHabiles(){
		return getPeriodosHabiles(periodos);
	}
	
	/**
	 * Redefinicion del metodo toString() para la clase <code>CalendarioHabil</code>.
	 * Con esto, no es necesario llamar al toString de <code>Periodo</code> o de
	 * <code>Exclusion</code> 
	 */
	public String toString(){
		String mensaje;
		mensaje = "---------------------------------------------------------------\n";
		mensaje += "| CALENDARIO: "+idCalendario+"\n";
		mensaje += "| \t Horas Totales: "+getHorasHabiles()+"\n";
		mensaje += "---------------------------------------------------------------\n";
		mensaje += "| Periodos Incluidos: \n";
		
		for (Periodo periodo : periodos) {
			mensaje += periodo.toString();
		}		
		
		mensaje += "|\n";
		
		mensaje += "| Periodos Excluidos: \n";
		
		for (Exclusion exclusion : exclusiones) {
			mensaje += exclusion.toString();
		}
		
		mensaje += "---------------------------------------------------------------\n";
		
		return mensaje;
	}

	/**
	 * Redefinimos el metodo clone, requerido por DataRules.
	 */
	@Override
	public Object clone(IKnowledgeBaseInfo ik) {
		return new CalendarioHabil(getID(), ik);
	}
	
	public double contribucionLinea(double cantidad,Double fechaIniLin,Double fechaIniDispon,Double fechaFinLin,Double fechaFinDispon){
		
		Double contValue= 0.0;
        Double ini=Math.max(fechaIniLin,fechaIniDispon);
        Double fin=Math.min(fechaFinLin,fechaFinDispon);
        double solape=getHorasHabiles(ini.longValue(),fin.longValue());                      
        double periodoLinea=getHorasHabiles(fechaIniLin.longValue(),fechaFinLin.longValue());
        if(periodoLinea<=0.0) return 0.0;
        
        //LA CANTIDAD de la linea va a misa, no se debe rebajar por el porcentaje dedicacion. Es asignar la que tiene en cuenta el porcentaje de dedicacion
		//para calcular fecha fin. Es decir, porciento dedicacion es la orden, y la cantidad partido por las horas habiles en esa fecha es el porciento
		//dedicacion real		                    
        double saturacion=cantidad/periodoLinea;
        //dma.out.println(" DBGDISP DESCUADRE cant"+cantidad+" periodo:"+periodoLinea+" solape:"+solape+" "+(solape*saturacion) +" INI:"+fechaIniLin+" fin:"+fechaFinLin+" qLinea:"+cantidad+" solape:"+solape+ " habil linea:"+periodoLinea);
        if( solape>=0.0 )	return solape*saturacion;
        else return 0.0;
}
}

/**
 * Conjunto de Constantes y funciones auxiliares para la clase CalendarioHabil.
 * 
 * @author Dario Veledo.
 */
class Operaciones{
	
	/**
	 * Una hora en milisegundos.
	 */
	public static final long UNA_HORA = 60 * 60 * 1000L;
	/**
	 * Un dia en milisegundos.
	 */
	public static final long UN_DIA = 24 * 60 * 60 * 1000L;
	public static final int DOMINGO = 1;
	public static final int LUNES = 2;
	public static final int MARTES = 3;
	public static final int MIERCOLES = 4;
	public static final int JUEVES = 5;
	public static final int VIERNES = 6;
	public static final int SABADO = 7;
	
	/**
	 * Devuelve el campo del calendario que se pida.
	 * 
	 * @param fecha
	 * @return El valor del campo requerido.
	 */
	public static int get(long fecha, int campoRequerido){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(fecha);
		
		return c.get(campoRequerido);
	}

	
	/**
	 * Funcion que comprueba si una fecha es excluible en funcion
	 * de los periodos de exclusion que tiene asociados el calendario.
	 * 
	 * @param c Dia a comprobar si es excluible.
	 * @param exclusionesImplicadas Lista con las exclusiones contemporines al dia
	 * que comprobamos.
	 * 
	 * @return <code>True</code> si se puede excluir ese dia. <code>False</code> en otro caso.
	 */
	public static boolean isExcluible(Calendar c,
			LinkedList<Exclusion> exclusionesImplicadas) {
		
		//Si hay alguna exclusion:
		if(exclusionesImplicadas != null){
			for (Exclusion exclusion : exclusionesImplicadas) {
				if(posteriorPorDias(c, exclusion.getFechaInicio()) 
						|| igualesPorDias(c, exclusion.getFechaInicio())){
					
					if(anteriorPorDias(c, exclusion.getFechaFin())
							 || igualesPorDias(c, exclusion.getFechaFin())){
						return true;
					}
				}
			}

			return false;
		}
		
		//En caso de que no haya exclusiones, NUNCA es excluible.
		return false;
	}
	
	/**
	 * Comprueba si una fecha es anterior, comparando por mes y dia,
	 * a otra.
	 * 
	 * @param c1 Fecha anterior.
	 * @param c2 Fecha posterior.
	 * 
	 * @return <code>True</code> si c1 es anterior a c2. <code>False</code> en otro caso.
	 */
	public static boolean anteriorPorDias(Calendar c1, Calendar c2){
		
		return 	c1.get(Calendar.YEAR) < c2.get(Calendar.YEAR) ||
				c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
				c1.get(Calendar.DAY_OF_YEAR) < c2.get(Calendar.DAY_OF_YEAR);
	}
	
	/**
	 * Comprueba si una fecha es posterior, comparando por mes y dia,
	 * a otra.
	 * 
	 * @param c1 Fecha posterior.
	 * @param c2 Fecha anterior.
	 * 
	 * @return <code>True</code> si c1 es posterior a c2. <code>False</code> en otro caso.
	 */
	public static boolean posteriorPorDias(Calendar c1, Calendar c2){
		return anteriorPorDias(c2, c1);
	}
	
	/**
	 * Comprueba si una fecha es igual, comparando por mes y dia,
	 * a otra.
	 * 
	 * @param c1 Fecha.
	 * @param c2 Fecha.
	 * 
	 * @return <code>True</code> si c1 es igual a c2. <code>False</code> en otro caso.
	 */
	public static boolean igualesPorDias(Calendar c1, Calendar c2){
		return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);		
	}
	
	public static boolean anteriorOIgualPorDias(Calendar c1, Calendar c2){
		return ( anteriorPorDias(c1, c2) || igualesPorDias(c1, c2));
	}
	
	public static boolean posteriorOIgualPorDias(Calendar c1, Calendar c2){
		return ( posteriorPorDias(c1, c2) || igualesPorDias(c1, c2));
	}
	
	public static void setHorasDia(Calendar c, double restoHorasHoy){
		Double horasHoy=Math.floor(restoHorasHoy);
		double minutos=Math.floor((restoHorasHoy-horasHoy)*60);
		double segundos= Math.floor(((restoHorasHoy-horasHoy)*60.0-minutos)*60);
		double millis= Math.floor((restoHorasHoy*3600 - (horasHoy*3600+minutos*60+segundos))*1000);
		//System.err.println("set hora "+restoHorasHoy+" "+minutos);
		c.set(Calendar.HOUR_OF_DAY,horasHoy.intValue());
		c.set(Calendar.MINUTE,new Double(minutos).intValue());		
		c.set(Calendar.SECOND,new Double(segundos).intValue());
		c.set(Calendar.MILLISECOND,new Double(millis).intValue());
	}
	public static double getHoraDouble(Calendar c){
		int horaPartida=c.get(Calendar.HOUR_OF_DAY);
		int minPartida=c.get(Calendar.MINUTE);
		int sgPartida=c.get(Calendar.SECOND);
		int millisPartida=c.get(Calendar.MILLISECOND);
		return horaPartida+minPartida/60.0+sgPartida/3600.0+millisPartida/3600000.0;
	}
}