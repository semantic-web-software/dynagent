package calendar;

import java.util.ArrayList;
import java.util.HashMap;

public class DatesBigCalendar {
		
	public static PanelCalendar pc;	
	public static int row, column;		
	
	public DatesBigCalendar(PanelCalendar pc){		
		DatesBigCalendar.pc=pc;	
	}
	
	public static ArrayList<Task> getEventsDay(String dia, int mes, int anyo){
		HashMap <String, ArrayList<Task>> hm = pc.getEventos().getEventos();
		if(dia.length()==1)
			dia = "0"+dia;
		String date;
		if(mes<10)
			date = dia+"/0"+mes+"/"+anyo;
		else
			date = dia+"/"+mes+"/"+anyo;
		if(hm.get(date)==null)
			return new ArrayList<Task>();
		else
			return hm.get(date);
	}
	
	public static ArrayList<Task> getEventsHour(String dia, int mes, int anyo, String hora){
		HashMap <String, ArrayList<Task>> hm = pc.getEventos().getEventos();
		if(dia.length()==1)
			dia = "0"+dia;
		String date;
		if(mes<10)
			date = dia+"/0"+mes+"/"+anyo;
		else
			date = dia+"/"+mes+"/"+anyo;
		if(hm.get(date)==null)
			return new ArrayList<Task>();		
		else{
			ArrayList<Task> tasks = hm.get(date);
			ArrayList<Task> res = new ArrayList<Task>();
			for(int i=0;i<tasks.size();i++){
				String hor = tasks.get(i).getExecutionDate().substring(11, 13);
				if(hora.equals(hor)){
					res.add(tasks.get(i));
				}
			}
			/*Collections.sort(a);
			ArrayList<String> b = new ArrayList<String>();
			for(int i=0;i<a.size();i++){
				b.add(a.get(i).substring(a.get(i).indexOf(" "), a.get(i).length()));
			}*/
			return res;			
		}
	}	
	
	public static Eventos getEventos(){
		return pc.getEventos();
	}
}
