package dynagent.gui.utils;

import java.util.ArrayList;

import dynagent.common.basicobjects.CardMed;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.gui.KnowledgeBaseAdapter;

public interface ICardMed {
	public ArrayList<Integer> getAllCardMed();
	public CardMed getCardMedByClassName(String name) throws NotFoundException, IncoherenceInMotorException ;
	public CardMed getCardMedByClassParentName(String nameP, String name) throws NotFoundException, IncoherenceInMotorException ;
	public CardMed getCardMedByClassAndParentName(String name, String nameParent) throws NotFoundException, IncoherenceInMotorException ;
	public CardMed getCardMedByIdtoClass(Integer idto) throws NotFoundException, IncoherenceInMotorException ;
	public CardMed getCardMedByIdtoParentClass(Integer idtoP, Integer idto) throws NotFoundException, IncoherenceInMotorException;
	public CardMed getCardMedByClassAndParent(Integer idto, Integer idtoParent) throws NotFoundException, IncoherenceInMotorException ;
	public ArrayList<CardMed> getListCardMed();
	public void removeCardMed(CardMed cm);
	public void addCardMed(CardMed cm);
	public void setKnowledgeBase(KnowledgeBaseAdapter kba);
	public CardMed getCardMedByPropAndParent(Integer idProp, Integer idtoParent) throws NotFoundException, IncoherenceInMotorException;
	public CardMed getCardMedByIdtoParentProp(Integer idtoP, Integer idProp) throws NotFoundException, IncoherenceInMotorException;
}

