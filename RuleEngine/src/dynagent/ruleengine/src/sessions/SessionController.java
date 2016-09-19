/**
  * @author Hassan Ali Sleiman - hassansleiman@gmail.com
 */
package dynagent.ruleengine.src.sessions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import dynagent.common.knowledge.IChangeServerListener;
import dynagent.common.knowledge.IHistoryDDBBListener;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.sessions.Session;
import dynagent.common.sessions.Sessionable;
import dynagent.common.utils.Auxiliar;




public class SessionController {

	private static SessionController instance=null;
	private static int ID = 0;
	private HashMap<IKnowledgeBaseInfo, HashMap<Long, Session>> mapIkThreadActualSession;
	private HashMap<IKnowledgeBaseInfo,HashMap<Integer,Session>> mapIkSessionsList ;
	private HashMap<Integer,Session> allSessions;
	private SessionController() {
		mapIkSessionsList = new HashMap<IKnowledgeBaseInfo, HashMap<Integer,Session>>();
		mapIkThreadActualSession=new HashMap<IKnowledgeBaseInfo, HashMap<Long,Session>>();
		allSessions=new HashMap<Integer, Session>();
	}

	public static SessionController getInstance() {
		if (instance == null)
			instance =  new SessionController();
		
			return instance;
	}

	public int add(Session s,IKnowledgeBaseInfo ik) {
		if(ik.isDispose() && ik.isEnabled()){
			//System.err.println("SessionController.add con ik dispose:"+ik);
			Auxiliar.printCurrentStackTrace();
			if(ik.getServer()!=null){
				Exception ex=new Exception("SESSIONCONTROLLER.ADD CON IK dispose siendo ik:"+ik+" Session:"+s);
				ik.getServer().logError(null,ex, null);
			}
		}
		if(s.getSesionables()==null){
			//System.err.println("SessionController.add con session dispose:"+s);
			Auxiliar.printCurrentStackTrace();
			if(ik.getServer()!=null){
				Exception ex=new Exception("SESSIONCONTROLLER.ADD CON SESSION dispose siendo ik:"+ik+" Session:"+s);
				ik.getServer().logError(null,ex, null);
			}
		}
		if(!mapIkSessionsList.containsKey(ik)){
			mapIkSessionsList.put(ik, new HashMap<Integer,Session>());
		}
		mapIkSessionsList.get(ik).put(s.getID(),s);
		
		allSessions.put(s.getID(),s);
		//System.out.println("Session : "+s.getID() +" con Padre: " +s.getIDMadre()+ "añadido");
		return s.getID();
	}
	public int getUNIC_ID()
	{
		return ID++;
	}
	public void remove(int idsession,IKnowledgeBaseInfo ik) {
		//System.out.println("********************eliminar idsesion:"+ idsession );
		mapIkSessionsList.get(ik).remove(idsession);
		
		if(mapIkSessionsList.get(ik).isEmpty()){
			mapIkSessionsList.remove(ik);
			mapIkThreadActualSession.remove(ik);
		}
		
		allSessions.remove(idsession);
		//System.out.println("Sesiones existenteS :"+getSessionsList());
		
	}
	public void setActual(Session s,IKnowledgeBaseInfo ik) {
		if(!mapIkThreadActualSession.containsKey(ik)){
			mapIkThreadActualSession.put(ik, new HashMap<Long, Session>());
		}
		if(s==null){
			mapIkThreadActualSession.get(ik).remove(Thread.currentThread().getId());
		}else{
			//Lo asociamos a la thread para evitar los fallos de accesos concurrentes desde diferentes hilos, que provocan que se asocien datos a sesiones erroneas
			if(mapIkSessionsList.get(ik).get(s.getID())!=null){
				mapIkThreadActualSession.get(ik).put(Thread.currentThread().getId(), s);
			}else{
				add(s,ik);
				mapIkThreadActualSession.get(ik).put(Thread.currentThread().getId(), s);
			}	
		}
		
		/*if(actual!=null)
		System.out.println("Empieza Session ----------------------------------------------------------- "+ actual.getID());
		else
		System.out.println("Empieza Session ----------------------------------------------------------- "+ null);*/
	}
	public Session getActualSession (IKnowledgeBaseInfo ik) {
		//TODO:
		//Lanzar exepcion en caso de que sea nulo ya que hay q crear la session antes
		
		if(!mapIkSessionsList.containsKey(ik))
			return null;
		else{
			HashMap<Long,Session> map=mapIkThreadActualSession.get(ik);
			if(map==null){
				return null;
			}else{
				return map.get(Thread.currentThread().getId());
			}
		}
	}
	
	public HashMap<Integer,Session> getSessionsList(IKnowledgeBaseInfo ik) {
		return mapIkSessionsList.containsKey(ik)?mapIkSessionsList.get(ik):new HashMap<Integer,Session>();
	}
	
	public HashMap<Integer,Session> getSessionsList() {
		return allSessions;
	}
	
	public  String toString()
	{
		return "SessionController: sessiones: " + mapIkSessionsList + "\n y actual : "+mapIkThreadActualSession; 
	}
	
	public Session getSession(int id)
	{
		if(allSessions.containsKey(id))
			return allSessions.get(id);
		else{
			System.err.println("WARNING:Busqueda de una session que no existe por aqui "+id+ "en la lista:"+allSessions);
			Auxiliar.printCurrentStackTrace();
		}
		return null;
	}
	
	public boolean containsSession(int id)
	{
		return allSessions.containsKey(id);
	}
	
	public Set<IKnowledgeBaseInfo> getKnowledgeBaseList(){
		return mapIkSessionsList.keySet();
	}
	
//	public int getPosition (int id)
//	{
//		for(int i = 0 ; i <sessionsList.size();i++)
//			if(sessionsList.get(i).getID()==id)
//				return i;
//		
//		return -1;
//	}
//	public static int getSessionsNumber(Sessionable s)
//	{
//		int cont=0;
//		Iterator<Session> sesItr =  sessionsList.iterator();
//		while(sesItr.hasNext())
//		{
//		if(sesItr.next().getSesionables().contains(s))
//			cont++;
//		}
//		return cont;
//	}
	
	public static void removeInstance(){
		instance=null;
		ID=0;
		DDBBSession.changeServerListeners=new ArrayList<IChangeServerListener>();
		DDBBSession.historyDDBBListeners=new ArrayList<IHistoryDDBBListener>();
	}
}
