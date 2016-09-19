package dynagent.common.utils;

import java.util.ArrayList;
import java.util.Iterator;

import dynagent.common.basicobjects.Alias;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;

public class AliasComponents implements IAlias{
	private ArrayList<Alias> listAlias=new ArrayList<Alias>();

	private IKnowledgeBaseInfo ik=null;
	public AliasComponents(IKnowledgeBaseInfo ik,ArrayList<Alias> listAlias){
		this.listAlias=listAlias;
		this.ik=ik;
	}
	
	public String getLabelProp(Integer prop,Integer idto,Integer group, Integer utask) throws NotFoundException{
		String result=ik.getPropertyName(prop);
		if (listAlias!=null && !this.listAlias.isEmpty()){
			ArrayList<Alias> resultAlias=searchAliasByProp(prop);
			
			if (!resultAlias.isEmpty()){
				Iterator<Alias> itresult=resultAlias.iterator();
				boolean existsAliasWithClass=false;
				boolean existsAliasWithUtask=false;
				boolean existsAliasWithGroup=false;
				while(itresult.hasNext()){
					Alias a=itresult.next();
					if(a.getIdto()!=null && a.getIdto().equals(idto)){
						existsAliasWithClass=true;
					}
					if(a.getGroup()!=null && a.getGroup().equals(group)){
						existsAliasWithGroup=true;
					}
					if(a.getUTask()!=null && a.getUTask().equals(utask)){
						existsAliasWithUtask=true;
					}
				}
				if(idto!=null && group!=null && utask!=null && existsAliasWithClass && existsAliasWithGroup && existsAliasWithUtask){
					itresult=resultAlias.iterator();
					while(itresult.hasNext()){
						Alias al=itresult.next();
						if(al.getIdto()!=null && al.getGroup()!=null && al.getUTask()!=null){
							if(al.getIdto().equals(idto) && al.getGroup().equals(group)&& al.getUTask().equals(utask)){
								return result=al.getAlias();
							}
						}
					}
					return result;
				}
				if(idto!=null && group!=null && existsAliasWithClass && existsAliasWithGroup && !existsAliasWithUtask){
					itresult=resultAlias.iterator();
					while(itresult.hasNext()){
						Alias al=itresult.next();
						if(al.getIdto()!=null && al.getGroup()!=null && al.getUTask()==null){
							if(al.getIdto().equals(idto) && al.getGroup().equals(group)){
								return result=al.getAlias();
							}
						}
					}
					return result;
				}
				if(idto!=null && utask!=null && existsAliasWithClass && existsAliasWithUtask && !existsAliasWithGroup){
					itresult=resultAlias.iterator();
					while(itresult.hasNext()){
						Alias al=itresult.next();
						if(al.getIdto()!=null && al.getUTask()!=null && al.getGroup()==null){
							if(al.getIdto().equals(idto) && al.getUTask().equals(utask)){
								return result=al.getAlias();
							}
						}
					}
					return result;
				}
				if( group!=null && utask!=null && existsAliasWithGroup && existsAliasWithUtask && !existsAliasWithClass){
					itresult=resultAlias.iterator();
					while(itresult.hasNext()){
						Alias al=itresult.next();
						if(al.getGroup()!=null && al.getUTask()!=null && al.getIdto()==null){
							if(al.getGroup().equals(group) && al.getUTask().equals(utask)){
								return result=al.getAlias();
							}
						}
					}
					return result;
				}
				if(utask!=null && existsAliasWithUtask && !existsAliasWithClass && !existsAliasWithGroup){
					int g;
					if (prop.equals(308))
						g=388;
					itresult=resultAlias.iterator();
					while(itresult.hasNext()){
						Alias al=itresult.next();
						if(al.getUTask()!=null && al.getGroup()==null && al.getIdto()==null) {
							if(al.getUTask().equals(utask)){
								return result=al.getAlias();
							}
						}
					}
					return result;
				}
				if(group!=null && existsAliasWithGroup && !existsAliasWithUtask && !existsAliasWithClass){
					itresult=resultAlias.iterator();
					while(itresult.hasNext()){
						Alias al=itresult.next();
						if(al.getGroup()!=null ){
							if(al.getGroup().equals(group) && al.getIdto()==null && al.getUTask()==null){
								return result=al.getAlias();
							}
						}
					}
					return result;
				}
				if(idto!=null && existsAliasWithClass && !existsAliasWithUtask && !existsAliasWithGroup){
					itresult=resultAlias.iterator();
					while(itresult.hasNext()){
						Alias al=itresult.next();
						if(al.getIdto()!=null ){
							if(al.getIdto().equals(idto) && al.getGroup()==null && al.getUTask()==null ){
								return result=al.getAlias();
							}
						}
					}
					return result;
				}
				
				Iterator<Alias> itrAlias=resultAlias.iterator();
				while(itrAlias.hasNext()){
					Alias alias=itrAlias.next();
					if (alias.getIdto()==null && alias.getGroup()==null && alias.getUTask()==null)
						return result=alias.getAlias();
				}
					
			}
				
	//			
	//			
	//			
	//			if(pclass==null && utask==null && group==null){
	//				result=resultAlias.get(0).getAlias();
	//			}else{
	//				Iterator<Alias> itresult=resultAlias.iterator();
	//				while(itresult.hasNext()){
	//					Alias alias=itresult.next();
	//					
	//					
	//					
	//					if(pclass!=null && utask!=null && group!=null){
	//						if(alias.getAliasClass().equals(pclass) && alias.getUTask().equals(utask) && alias.getGroup().equals(group)){
	//							result=alias.getAlias();
	//							return result;
	//						}
	//					}else if(pclass!=null && group!=null && utask==null){
	//						if(alias.getAliasClass().equals(pclass) && alias.getGroup().equals(group)){
	//							result=alias.getAlias();
	//							return result;
	//						}
	//					}else if(pclass!=null && group==null && utask!=null){
	//						if(alias.getAliasClass().equals(pclass) && alias.getUTask().equals(utask)){
	//							result=alias.getAlias();
	//							return result;
	//						}
	//					}else if(pclass!=null && utask==null){
	//						if(alias.getAliasClass().equals(pclass)){
	//							result=alias.getAlias();
	//							return result;
	//						}
	//					}else{
	//						if(alias.getUTask().equals(utask)){
	//							result=alias.getAlias();
	//							return result;
	//						}
	//					}
	//				}
	//			}
	//		}
		}
		return result;
	}
	
	private ArrayList<Alias> searchAliasByProp(Integer prop) {
		ArrayList<Alias> result=new ArrayList<Alias>();
		Iterator<Alias> italias=this.listAlias.iterator();
		while(italias.hasNext()){
			Alias alias=italias.next();
			
			if (alias.getProp()!=null && alias.getProp().equals(prop)){
				result.add(alias);
			}
		}
		return result;
	}
	public String getLabelClass(Integer idto, Integer utask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException{
		String result=ik.getClassName(idto);
		if (listAlias!=null && !listAlias.isEmpty()){
			ArrayList<Alias> resultAlias=searchAliasByClass(idto);
			
			if (!resultAlias.isEmpty()){
				Iterator<Alias> itresult=resultAlias.iterator();
				
				boolean existsAliasWithUtask=false;
		
				while(itresult.hasNext()){
					Alias a=itresult.next();
					
					if(a.getUTask()!=null && a.getUTask().equals(utask)){
						existsAliasWithUtask=true;
					}
				}
				
				
				if(utask!=null && existsAliasWithUtask){
					itresult=resultAlias.iterator();
					while(itresult.hasNext()){
						Alias al=itresult.next();
						if(al.getUTask()!=null){
							if(al.getUTask().equals(utask)){
								return result=al.getAlias();
							}
						}
					}
					return result;
				}
				Alias alias=resultAlias.get(0);
				if (alias.getUTask()==null)
					return result=alias.getAlias();
	//			
	//			if(utask==null){
	//				result=resultAlias.get(0).getAlias();
	//			}else{
	//				Iterator<Alias> itresult=resultAlias.iterator();
	//				while(itresult.hasNext()){
	//					Alias alias=itresult.next();
	//					
	//					if(alias.getUTask().equals(utask)){
	//						result=alias.getAlias();
	//						return result;
	//					}
	//					
	//				}
	//			}
			}
		}
		return result;
	}
	private ArrayList<Alias> searchAliasByClass(Integer idto) {
		ArrayList<Alias> result=new ArrayList<Alias>();
		Iterator<Alias> italias=this.listAlias.iterator();
		
		while(italias.hasNext()){
			Alias alias=italias.next();
			if (alias.getIdto()!=null && alias.getIdto().equals(idto) && alias.getProp()==null && alias.getGroup()==null){
				result.add(alias);
			}
		}
		return result;
	}

	public String getLabelUtask(Integer utask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException{
		String result=ik.getClassName(utask);
		if(listAlias!=null && !this.listAlias.isEmpty()){
			ArrayList<Alias> resultAlias=searchAliasByUtask(utask);
			if(!resultAlias.isEmpty()){
				result=resultAlias.get(0).getAlias();
			}
		}
		return result;
	}
	private ArrayList<Alias> searchAliasByUtask(Integer utask) {
		ArrayList<Alias> result=new ArrayList<Alias>();
		Iterator<Alias> italias=this.listAlias.iterator();
		while(italias.hasNext()){
			Alias alias=italias.next();
			if (alias.getUTask()!=null && alias.getUTask().equals(utask) && alias.getProp()==null && alias.getGroup()==null && alias.getIdto()==null){
				result.add(alias);
			}
		}
		return result;
	}

	public String getLabelGroup(Integer group, String nameGroup, Integer utask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException{
		String result=nameGroup;
		if(listAlias!=null && !this.listAlias.isEmpty()){
			ArrayList<Alias> resultAlias=searchAliasByGroup(group);
			if (!resultAlias.isEmpty()){
				Iterator<Alias> itresult=resultAlias.iterator();
				
				boolean existsAliasWithUtask=false;
		
				while(itresult.hasNext()){
					Alias a=itresult.next();
					
					if(a.getUTask()!=null && a.getUTask().equals(utask)){
						existsAliasWithUtask=true;
					}
				}
				
				
				if(utask!=null && existsAliasWithUtask){
					itresult=resultAlias.iterator();
					while(itresult.hasNext()){
						Alias al=itresult.next();
						if(al.getUTask()!=null){
							if(al.getUTask().equals(utask)){
								return result=al.getAlias();
							}
						}
					}
					return result;
				}
				Alias alias=resultAlias.get(0);
				if (alias.getUTask()==null)
					return result=alias.getAlias();
			}
		}
		return result;
	}
	private ArrayList<Alias> searchAliasByGroup(Integer group) {
		ArrayList<Alias> result=new ArrayList<Alias>();
		Iterator<Alias> italias=this.listAlias.iterator();
		while(italias.hasNext()){
			Alias alias=italias.next();
			if (alias.getGroup()!=null && alias.getGroup().equals(group) && alias.getProp()==null && alias.getIdto()==null){
				result.add(alias);
			}
		}
		return result;
	}

	public void addAliasList(ArrayList<Alias> listAlias) {
		this.listAlias.addAll(listAlias);
	}
	
	public void removeAliasList(ArrayList<Alias> listAlias) {
		this.listAlias.removeAll(listAlias);		
	}
	public void setIk(IKnowledgeBaseInfo ik) {
		this.ik=ik;
	}
	public void addAlias(Alias alias) {
		this.listAlias.add(alias);
	}
	public boolean removeAlias(Alias alias) {
		
		return this.listAlias.remove(alias);
	}
	public IKnowledgeBaseInfo getIk() {
		return this.ik;
	}
		
}
