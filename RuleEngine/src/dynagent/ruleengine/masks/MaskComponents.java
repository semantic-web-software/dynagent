package dynagent.ruleengine.masks;

import java.util.ArrayList;
import java.util.Iterator;

import dynagent.common.basicobjects.Alias;
import dynagent.common.basicobjects.Mask;
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

public class MaskComponents implements IMasks{
	private ArrayList<Mask> listMask;

	private IKnowledgeBaseInfo ik=null;
	public MaskComponents(IKnowledgeBaseInfo ik,ArrayList<Mask> listMask){
		this.listMask=listMask;
		this.ik=ik;
	}
	
		
	private ArrayList<Mask> searchMaskByProp(Integer prop) {
		ArrayList<Mask> result=new ArrayList<Mask>();
		Iterator<Mask> itmask=this.listMask.iterator();
		while(itmask.hasNext()){
			Mask mask=itmask.next();
			
			if (mask.getProp()!=null && mask.getProp().equals(prop)){
				result.add(mask);
			}
		}
		return result;
	}
	
	
	public void addMaskList(ArrayList<Mask> listMask) {
		this.listMask.addAll(listMask);
	}
	
	public void removeMaskList(ArrayList<Mask> listMask) {
		this.listMask.removeAll(listMask);		
	}
	public void setIk(IKnowledgeBaseInfo ik) {
		this.ik=ik;
	}
	public IKnowledgeBaseInfo getIk() {
		return this.ik;
	}


	public String getExpression(int prop, Integer idto, Integer utask) throws NotFoundException {
		String result=null;
		if (listMask!=null && !this.listMask.isEmpty()){
			ArrayList<Mask> resultMask=searchMaskByProp(prop);
			
			if (!resultMask.isEmpty()){
				Iterator<Mask> itresult=resultMask.iterator();
				boolean existsMaskWithClass=false;
				boolean existsMaskWithUtask=false;
				while(itresult.hasNext()){
					Mask m=itresult.next();
					if(m.getIdto()!=null && m.getIdto().equals(idto)){
						existsMaskWithClass=true;
					}
					if(m.getUTask()!=null && m.getUTask().equals(utask)){
						existsMaskWithUtask=true;
					}
				}
				if(idto!=null && utask!=null && existsMaskWithClass && existsMaskWithUtask){
					itresult=resultMask.iterator();
					while(itresult.hasNext()){
						Mask mk=itresult.next();
						if(mk.getIdto()!=null && mk.getUTask()!=null){
							if(mk.getIdto().equals(idto) && mk.getUTask().equals(utask)){
								return result=mk.getExpression();
							}
						}
					}
					return result;
				}
				if(idto!=null && existsMaskWithClass && !existsMaskWithUtask){
					itresult=resultMask.iterator();
					while(itresult.hasNext()){
						Mask mk=itresult.next();
						if(mk.getIdto()!=null && mk.getUTask()==null){
							if(mk.getIdto().equals(idto)){
								return result=mk.getExpression();
							}
						}
					}
					return result;
				}
				if( utask!=null && existsMaskWithUtask && !existsMaskWithClass){
					itresult=resultMask.iterator();
					while(itresult.hasNext()){
						Mask mk=itresult.next();
						if(mk.getUTask()!=null && mk.getIdto()==null){
							if(mk.getUTask().equals(utask)){
								return result=mk.getExpression();
							}
						}
					}
					return result;
				}
								
				Iterator<Mask> itrMask=resultMask.iterator();
				while(itrMask.hasNext()){
					Mask mask=itrMask.next();
					if (mask.getIdto()==null && mask.getUTask()==null)
						return result=mask.getExpression();
				}
					
			}
		}
		return result;
	}

	public Integer getLength(int prop, Integer idto, Integer utask) {
		Integer result=null;
		if (listMask!=null && !this.listMask.isEmpty()){
			ArrayList<Mask> resultMask=searchMaskByProp(prop);
			
			if (!resultMask.isEmpty()){
				Iterator<Mask> itresult=resultMask.iterator();
				boolean existsMaskWithClass=false;
				boolean existsMaskWithUtask=false;
				while(itresult.hasNext()){
					Mask m=itresult.next();
					if(m.getIdto()!=null && m.getIdto().equals(idto)){
						existsMaskWithClass=true;
					}
					if(m.getUTask()!=null && m.getUTask().equals(utask)){
						existsMaskWithUtask=true;
					}
				}
				if(idto!=null && utask!=null && existsMaskWithClass && existsMaskWithUtask){
					itresult=resultMask.iterator();
					while(itresult.hasNext()){
						Mask mk=itresult.next();
						if(mk.getIdto()!=null && mk.getUTask()!=null){
							if(mk.getIdto().equals(idto) && mk.getUTask().equals(utask)){
								return result=mk.getLength();
							}
						}
					}
					return result;
				}
				if(idto!=null && existsMaskWithClass && !existsMaskWithUtask){
					itresult=resultMask.iterator();
					while(itresult.hasNext()){
						Mask mk=itresult.next();
						if(mk.getIdto()!=null && mk.getUTask()==null){
							if(mk.getIdto().equals(idto)){
								return result=mk.getLength();
							}
						}
					}
					return result;
				}
				if( utask!=null && existsMaskWithUtask && !existsMaskWithClass){
					itresult=resultMask.iterator();
					while(itresult.hasNext()){
						Mask mk=itresult.next();
						if(mk.getUTask()!=null && mk.getIdto()==null){
							if(mk.getUTask().equals(utask)){
								return result=mk.getLength();
							}
						}
					}
					return result;
				}
								
				Iterator<Mask> itrMask=resultMask.iterator();
				while(itrMask.hasNext()){
					Mask mask=itrMask.next();
					if (mask.getIdto()==null && mask.getUTask()==null)
						return result=mask.getLength();
				}
					
			}
		}
		return result;
	}
		
}
