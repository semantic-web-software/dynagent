package dynagent.common.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import dynagent.common.Constants;
import dynagent.common.knowledge.UserAccess;
import dynagent.common.knowledge.access;
import dynagent.common.properties.Property;

public class AccessAdapter {

	public final static int VIEW=0;
	public final static int SET=1;
	public final static int NEW_AND_REL=2;
	public final static int DEL_AND_UNREL=3;
	public final static int FIND_AND_REL=4;
	public final static int UNREL=5;
	public final static int HIDDEN_FIND_AND_REL=6;
	public final static int FIND=7;
	
	/*public boolean view;
	public boolean set;
	public boolean newAndUnrel;
	public boolean delAndUnrel;
	public boolean rel;
	public boolean unrel;
	public boolean hiddenRel;*/
	
	private HashMap<Integer,ArrayList<Integer>> m_accessUTask;
	
	
	public AccessAdapter(HashMap<Integer,ArrayList<UserAccess>> objectAccess,Property property,boolean structural,boolean filter) {
		m_accessUTask=new HashMap<Integer, ArrayList<Integer>>();
		//m_accessUserRol=new HashMap<Integer, Integer>();
		
		m_accessUTask.put(VIEW,new ArrayList<Integer>());
		m_accessUTask.put(SET,new ArrayList<Integer>());
		m_accessUTask.put(NEW_AND_REL,new ArrayList<Integer>());
		m_accessUTask.put(DEL_AND_UNREL,new ArrayList<Integer>());
		m_accessUTask.put(FIND_AND_REL,new ArrayList<Integer>());
		m_accessUTask.put(UNREL,new ArrayList<Integer>());
		m_accessUTask.put(HIDDEN_FIND_AND_REL,new ArrayList<Integer>());
		m_accessUTask.put(FIND,new ArrayList<Integer>());
		setAccess(objectAccess,property,structural,filter);
	}
	
	private void setAccess(HashMap<Integer,ArrayList<UserAccess>> objectAccess,Property property,boolean structural, boolean filter){
		access propertyAccess=property!=null?property.getTypeAccess():new access(true,true,true,true,true,false);
		Iterator<Integer> userTasks=objectAccess.keySet().iterator();
		while(userTasks.hasNext()){
			Integer userTask=userTasks.next();
			UserAccess userAccess=objectAccess.get(userTask).get(0);
			access access=userAccess.getAccess();
			//Integer userRol=userAccess.getUserRol();
			if(propertyAccess==null || propertyAccess.getViewAccess()){
				if(access.getViewAccess()){
					m_accessUTask.get(VIEW).add(userTask);
				}
			}
			if(/*propertyAccess==null ||*/ propertyAccess.getSetAccess()){
				//if(access.getViewAccess() /*&& !access.getAbstractAccess()*/){
				//	m_accessUTask.get(REL).add(userTask);
				//}
				if(access.getSetAccess()){
					m_accessUTask.get(SET).add(userTask);
				//	m_accessUTask.get(UNREL).add(userTask);
				}
				if(access.getNewAccess()){
					m_accessUTask.get(NEW_AND_REL).add(userTask);
				}
				if(access.getDelAccess()){
					//Si no estamos en el principal añadimos desvinculación.
					//Nos interesa hacerlo asi para que entre en juego el codigo que decide si se borra o no ese objeto teniendo en cuenta estructurales y estructurales no compartidas
					if(property.getIdProp()!=Constants.IdPROP_TARGETCLASS){
						if(!filter){//No permitimos el borrado o desvinculacion en los filterControl modales para evitar problemas
							m_accessUTask.get(UNREL).add(userTask);
						}
					}else{
						m_accessUTask.get(DEL_AND_UNREL).add(userTask);
					}
				}
				if(!structural){
					if(property.getIdProp()!=Constants.IdPROP_TARGETCLASS)
					{
						if(access.getFindAccess()){
							m_accessUTask.get(FIND_AND_REL).add(userTask);
						}else{
							m_accessUTask.get(HIDDEN_FIND_AND_REL).add(userTask);
						}
						
						if(!access.getDelAccess())
							m_accessUTask.get(UNREL).add(userTask);
					}else{
						if(access.getFindAccess()){
							m_accessUTask.get(FIND).add(userTask);
						}
					}
				}
			}
		}

	}
	
	public ArrayList<Integer> getUserTasksAccess(int access){
		//System.err.println(m_accessUTask);
		return m_accessUTask.get(access);
	}

}
