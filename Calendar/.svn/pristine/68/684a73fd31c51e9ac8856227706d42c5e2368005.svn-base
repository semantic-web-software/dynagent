package calendar;

import java.util.ArrayList;
import java.util.HashMap;

import tasks.ITaskListener;

public class Eventos implements ITaskListener{
	
	private HashMap <String, ArrayList<Task>> eventos;
	
	public Eventos(){
		initialize();
	}
	
	public void initialize(){
		
		eventos = new HashMap<String, ArrayList<Task>>();
		
		String d1 = new String("27/04/2008");
		String d2 = new String("16/04/2008");
		
		Task t = new Task("Pedido_02", "Realizado", "26/11/2007 10:01:02", "26/11/2007 12:05:06", 10012);		
		ArrayList<Task> a1 = new ArrayList<Task>();
		a1.add(t);
		
		Task t1 = new Task("T1", "Realizado", "26/11/2007 14:15:59", "09/11/2007 10:27:13", 10014);
		Task t2 = new Task("T2", "Realizado", "26/11/2007 14:15:25", "09/11/2007 10:27:13", 10014);
		Task t3 = new Task("T3", "Realizado", "26/11/2007 15:14:59", "09/11/2007 10:27:13", 10014); 
		Task t4 = new Task("T4", "Realizado", "26/11/2007 16:15:59", "09/11/2007 10:27:13", 10014); 
		Task t5 = new Task("T5", "Realizado", "26/11/2007 14:15:59", "09/11/2007 10:27:13", 10014); 
		Task t6 = new Task("T6", "No Realizado", "26/11/2007 14:14:59", "09/11/2007 10:27:13", 10014); 
		Task t7 = new Task("T7", "Realizado", "26/11/2007 14:15:59", "09/11/2007 10:27:13", 10014);
		Task t8 = new Task("T8", "Realizado", "26/11/2007 14:15:59", "09/11/2007 10:27:13", 10014);
		
		ArrayList<Task> a2 = new ArrayList<Task>();
		a2.add(t1);
		a2.add(t2);
		a2.add(t3);
		a2.add(t4);
		a2.add(t5);
		a2.add(t6);
		a2.add(t7);
		a2.add(t8);		
		
		eventos.put(d1, a1);
		eventos.put(d2, a2);
	}
	
	public void updateTasks(int idoUserTask,String labelUserTask,String status,String asignDate,String ejecutionDate){
		
		Task t = new Task(labelUserTask, status, ejecutionDate, asignDate, idoUserTask);
		String dia = ejecutionDate.substring(0,10);
		if(eventos.get(dia)!=null){
			eventos.get(dia).add(t);			
		}else{
			ArrayList<Task> a = new ArrayList<Task>();
			a.add(t);
			eventos.put(dia, a);
		}		
	}	
	
	public void deleteEvento(String day, String name){
		if(eventos.get(day)==null)
			System.err.println("No existen eventos para el dia: "+day);			
		else{
			ArrayList<Task> tasks = eventos.get(day);
			for(int i=0;i<tasks.size();i++){
				Task t = tasks.get(i);
				if(t.getName().equals(name))
					tasks.remove(i);
			}
		}
	}

	public HashMap<String, ArrayList<Task>> getEventos(){
		return eventos;
	}
}
