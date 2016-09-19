package dynagent.common.communication;

import java.util.ArrayList;

import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IPropertyDef;

public class IndividualData {

	private ArrayList<IPropertyDef> aIPropertyDef;
	private ArrayList<Reservation> reservationList;
	private IKnowledgeBaseInfo ik;
	
	public IndividualData() {
		aIPropertyDef = new ArrayList<IPropertyDef>();
		reservationList = new ArrayList<Reservation>();
	}
	
	public IndividualData(ArrayList<IPropertyDef> ipdf,IKnowledgeBaseInfo ik) {
		aIPropertyDef = ipdf;
		this.reservationList=new ArrayList<Reservation>();
		this.ik=ik;
	}
	public IndividualData(ArrayList<IPropertyDef> ipdf,ArrayList<Reservation> reservationList,IKnowledgeBaseInfo ik) {
		aIPropertyDef = ipdf;
		this.reservationList=reservationList;
		this.ik=ik;
	}
	
	public ArrayList<IPropertyDef> getAIPropertyDef() {
		return aIPropertyDef;
	}
	public void addIPropertyDef(IPropertyDef ipd) {
		aIPropertyDef.add(ipd);
	}
	public void addAIPropertyDef(ArrayList<IPropertyDef> ipd) {
		aIPropertyDef.addAll(ipd);
	}
	public void addReservation(Reservation reserv) {
		reservationList.add(reserv);
	}

	public String toString() {
		String aipdStr = "INDIVIDUAL_DATA:\n";
		for (int i=0;i<aIPropertyDef.size();i++)
			aipdStr += aIPropertyDef.get(i) + "\n";
		return aipdStr;
	}
	
	public Element toElement() {
		Element facts = new Element("FACTS");
		for (int i=0;i<aIPropertyDef.size();i++) {
			IPropertyDef ipd = aIPropertyDef.get(i);
			Element fact = new Element("FACT");
			Element newFact = new Element("NEW_FACT");
			elemValues(newFact, ipd);
			fact.addContent(newFact);
			if (ipd.getInitialValues()!=null) {
				Element factChild = new Element("INITIAL_FACT");
				elemValues(factChild, ipd.getInitialValues());
				fact.addContent(factChild);
			}
			facts.addContent(fact);
		}
		for (int i=0;i<reservationList.size();i++) {
			Reservation reserv = reservationList.get(i);
			Element elemReserv = reserv.toElement();
			facts.addContent(elemReserv);
		}
		return facts;
	}
	private void elemValues(Element fact, IPropertyDef ipd) {
		if (ipd.getCLASSNAME()!=null)
			fact.setAttribute("NAME",ipd.getCLASSNAME());
		if (ipd.getIDTO()!=null)
			fact.setAttribute("IDTO",String.valueOf(ipd.getIDTO()));
		if (ipd.getIDO()!=null)
			fact.setAttribute("IDO",String.valueOf(ipd.getIDO()));
		//if (ipd.getPROP()!=null)
			fact.setAttribute("PROP",String.valueOf(ipd.getPROP()));
		if (ipd.getVALUE()!=null) {
			fact.setText(ipd.getVALUE());
			/*Element value = new Element("VALUE");
			fact.addContent(value);
			System.out.println("MACADATAVALUE");
			Auxiliar.printCurrentStackTrace();
			System.out.println(ipd.getVALUE());
			value.addContent(new CDATA(ipd.getVALUE()));

			CDATA CData = (CDATA)value.getContent().get(0);
			System.out.println("CData " + CData.getText());*/
			/*if (ipd.getRdnValue()!=null) {
				fact.setAttribute("RDNVALUE",ipd.getRdnValue());
			} else if (ik!=null && ipd.getVALUECLS()!=null && !Constants.isDataType(ipd.getVALUECLS())) {
				int idoValue=Integer.valueOf(ipd.getVALUE());
				String rdn=ik.getRdnIfExistInRuler(idoValue);
				if(rdn!=null) {
					fact.setAttribute("RDNVALUE",rdn);
				}
			}*/
		}
		if (ipd.getVALUECLS()!=null)
			fact.setAttribute("VALUECLS",String.valueOf(ipd.getVALUECLS()));
		if (ipd.getQMIN()!=null)
			fact.setAttribute("QMIN",String.valueOf(ipd.getQMIN()));
		if (ipd.getQMAX()!=null)
			fact.setAttribute("QMAX",String.valueOf(ipd.getQMAX()));
		if (ipd.getOP()!=null)
			fact.setAttribute("OP",ipd.getOP());
		if (ipd.getSystemValue()!=null)
			fact.setAttribute("SYSTEM_VALUE",ipd.getSystemValue());
		fact.setAttribute("APPLIED_SYSTEM_VALUE",String.valueOf(ipd.isAppliedSystemValue()));
		fact.setAttribute("INCREMENTAL",String.valueOf(ipd.isIncremental()/* && ipd.getExistia_BD()*//*Evitamos enviar como incremental un fact recien creado ya que es un problema para la gestión en BD*/));
		if (ipd.getDestinationSystem()!=null)
			fact.setAttribute("DESTINATION_SYSTEM",ipd.getDestinationSystem());
		
		fact.setAttribute("EXISTIA_BD",String.valueOf(ipd.getExistia_BD()));
		fact.setAttribute("ORDER",String.valueOf(ipd.getOrder()));
		
		if (ipd.getRdn()!=null)
			fact.setAttribute("RDN",ipd.getRdn());
		
		if (ipd.getRdnValue()!=null)
			fact.setAttribute("RDNVALUE",ipd.getRdnValue());
		
	}

	public IKnowledgeBaseInfo getKnowledgeBase() {
		return ik;
	}

	public void setKnowledgeBase(IKnowledgeBaseInfo ik) {
		this.ik = ik;
	}

}
