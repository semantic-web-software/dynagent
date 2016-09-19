package dynagent.gui.utils;

import java.util.ArrayList;
import java.util.Iterator;

import dynagent.common.basicobjects.CardMed;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.utils.Auxiliar;
import dynagent.gui.KnowledgeBaseAdapter;

public class CardMedComponent implements ICardMed{
	private KnowledgeBaseAdapter kba;
	private ArrayList<CardMed> listCM= new ArrayList<CardMed>();
	public CardMedComponent(ArrayList<CardMed> listCM){
		
		this.listCM=listCM;
		
	}
	
	public void setListCM(ArrayList<CardMed> listCM){
		this.listCM=listCM;
	}
	
	public void setKnowledgeBase(KnowledgeBaseAdapter kba){
		this.kba=kba;
	}
	public void addCardMed(CardMed cm) {
		this.listCM.add(cm);
	}

	public ArrayList<Integer> getAllCardMed() {
		ArrayList<Integer> result= new ArrayList<Integer>();
		Iterator<CardMed> itcm= this.listCM.iterator();
		while(itcm.hasNext()){
			CardMed cm= itcm.next();
			result.add(cm.getCardmed());
		}
		return result;
	}

	public CardMed getCardMedByClassAndParent(Integer idto, Integer idtoParent) throws NotFoundException, IncoherenceInMotorException  {
		CardMed cm=null;
		if (idtoParent!=null){
			
			//System.err.println("IDTOPARENT "+idtoParent);
			cm=getCardMedByIdtoParentClass(idtoParent, idto);
		}
		if(cm==null){
			return getCardMedByIdtoClass(idto);
		}else{
			return cm;
		}
		
	}
	
	public CardMed getCardMedByPropAndParent(Integer idProp, Integer idtoParent) throws NotFoundException, IncoherenceInMotorException  {
		CardMed cm=null;
		if (idtoParent!=null){
			
			//System.err.println("IDTOPARENT "+idtoParent);
			cm=getCardMedByIdtoParentProp(idtoParent, idProp);
		}
		if(cm==null){
			return getCardMedByIdProp(idProp);
		}else{
			return cm;
		}
		
	}

//	private CardMed findCMinList(ArrayList<CardMed> cmwithparent, Integer idto) {
//		Iterator<CardMed> itcm= cmwithparent.iterator();
//		while(itcm.hasNext()){
//			CardMed cm= itcm.next();
//			if (cm.getIdto()!=null && cm.getIdto().equals(idto)){
//				return cm;
//			}
//		}
//		return null;
//	}

	
	public CardMed getCardMedByClassAndParentName(String name, String nameParent) throws NotFoundException, IncoherenceInMotorException  {
		Integer idto= kba.getIdClass(name);
		Integer idtoParent= kba.getIdClass(nameParent);
		return getCardMedByClassAndParent(idto, idtoParent);
	}

	public CardMed getCardMedByClassName(String name) throws NotFoundException, IncoherenceInMotorException  {
		Integer idto= kba.getIdClass(name);
		
		return getCardMedByIdtoClass(idto);
	}

	public CardMed getCardMedByClassParentName(String nameP, String name) throws NotFoundException, IncoherenceInMotorException  {
		return getCardMedByIdtoParentClass(kba.getIdClass(nameP),kba.getIdClass(name));
	}

	public CardMed getCardMedByIdtoClass(Integer idto) throws NotFoundException, IncoherenceInMotorException  {
		Iterator<CardMed> itcm= this.listCM.iterator();
		while(itcm.hasNext()){
			CardMed cm= itcm.next();
			if (Auxiliar.equals(cm.getIdto(),idto) && cm.getIdtoParent()==null){
				return cm;
			}
		}
		Iterator<Integer> itParents = kba.getAncestors(idto);
		while(itParents.hasNext()){
			Integer idtoP=itParents.next();
			itcm= this.listCM.iterator();
			while(itcm.hasNext()){
				CardMed cm= itcm.next();
				if (Auxiliar.equals(cm.getIdto(),idtoP) && cm.getIdtoParent()==null){
					return cm;
				}
			}
		}
		return null;
	}
	
	public CardMed getCardMedByIdProp(Integer idProp) throws NotFoundException, IncoherenceInMotorException  {
		Iterator<CardMed> itcm= this.listCM.iterator();
		while(itcm.hasNext()){
			CardMed cm= itcm.next();
			if (Auxiliar.equals(cm.getIdProp(),idProp) && cm.getIdtoParent()==null){
				return cm;
			}
		}
		return null;
	}

	public CardMed getCardMedByIdtoParentClass(Integer idtoP, Integer idto) throws NotFoundException, IncoherenceInMotorException  {
		
		Iterator<CardMed> itcm= this.listCM.iterator();
		while(itcm.hasNext()){
			CardMed cm=itcm.next();
			if (cm.getIdtoParent()!=null && cm.getIdtoParent().equals(idtoP) && Auxiliar.equals(cm.getIdto(),idto)){
				return cm;
			}
		}
		Iterator<Integer> itParents = kba.getAncestors(idtoP);
		while(itParents.hasNext()){
			Integer idtoPa=itParents.next();
			itcm= this.listCM.iterator();
			while(itcm.hasNext()){
				CardMed cm= itcm.next();
				// System.err.println("IDTOH "+idtoPa+" CM "+cm.toString());
				if (cm.getIdtoParent()!=null && cm.getIdtoParent().equals(idtoPa) && Auxiliar.equals(cm.getIdto(),idto)){
					return cm;
				}
			}
		}
		
		Iterator<Integer> itParentClass=kba.getAncestors(idto);
		while (itParentClass.hasNext()){
			Integer idtoPC=itParentClass.next();
			itcm= this.listCM.iterator();
			while(itcm.hasNext()){
				CardMed cm= itcm.next();
				if (cm.getIdtoParent()!=null && cm.getIdtoParent().equals(idtoP) && Auxiliar.equals(cm.getIdto(),idtoPC)){
					return cm;
				}
			}
		}
		
		itParents = kba.getAncestors(idtoP);
		while(itParents.hasNext()){
			Integer idtoPa=itParents.next();
			Iterator<Integer> itParentC=kba.getAncestors(idto);
			while (itParentC.hasNext()){
				Integer idtoPC=itParentC.next();
				itcm= this.listCM.iterator();
				while(itcm.hasNext()){
					CardMed cm= itcm.next();
					if (cm.getIdtoParent()!=null && cm.getIdtoParent().equals(idtoPa) && Auxiliar.equals(cm.getIdto(),idtoPC)){
						return cm;
					}
				}
			}
			
		}
		
		return null;
	}
	
	public CardMed getCardMedByIdtoParentProp(Integer idtoP, Integer idProp) throws NotFoundException, IncoherenceInMotorException  {
		
		Iterator<CardMed> itcm= this.listCM.iterator();
		while(itcm.hasNext()){
			CardMed cm=itcm.next();
			if (cm.getIdtoParent()!=null && cm.getIdtoParent().equals(idtoP) && Auxiliar.equals(cm.getIdProp(),idProp)){
				return cm;
			}
		}
		Iterator<Integer> itParents = kba.getAncestors(idtoP);
		while(itParents.hasNext()){
			Integer idtoPa=itParents.next();
			itcm= this.listCM.iterator();
			while(itcm.hasNext()){
				CardMed cm= itcm.next();
				// System.err.println("IDTOH "+idtoPa+" CM "+cm.toString());
				if (cm.getIdtoParent()!=null && cm.getIdtoParent().equals(idtoPa) && Auxiliar.equals(cm.getIdProp(),idProp)){
					return cm;
				}
			}
		}
		
		return null;
	}

	public ArrayList<CardMed> getListCardMed() {
		return this.listCM;
	}

	public void removeCardMed(CardMed cm) {
		this.listCM.remove(cm);		
	}

}
