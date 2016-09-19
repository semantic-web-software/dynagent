/***
 * RulerFactoryXML
 * @author: Ildefonso Montero Perez - monteroperez@us.es
 */

package dynagent.ruleengine.src.factories;

import java.util.ArrayList;
import java.util.HashSet;

import org.jdom.CDATA;
import org.jdom.Element;

import dynagent.common.communication.IndividualData;
import dynagent.common.communication.docServer;
import dynagent.common.communication.messageFactory;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.IQuestionListener;
import dynagent.common.knowledge.IMessageListener;
import dynagent.common.knowledge.KnowledgeAdapter;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.DebugLog;
import dynagent.common.xml.metaComClient;

public class RulerFactoryXML  extends RulerFactoryCommon{
	private Element xml;
	
	public RulerFactoryXML(Element xml, int bussiness, docServer server, String engine,String user, DebugLog debugLog, IMessageListener messageListener, IQuestionListener questionListener, ArrayList<String> rules, boolean printRules) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException{
		super(bussiness, server, engine,user, true, debugLog, messageListener,questionListener,printRules);
		//System.out.println(".....inicio constructor RulerFactoryXML");
		double inicio=System.currentTimeMillis();
		this.xml=xml;
		this.inicializeInfoFromXML(rules,user);
		//System.out.println("   <time constructor RulerFactoryXML="+Auxiliar.getSecondsExecucionFrom(inicio));
	}

	private void inicializeInfoFromXML(ArrayList<String> rules, String user) throws SystemException, RemoteSystemException, CommunicationException {

		metaComClient m = new metaComClient(this.xml);
		this.herencias = m.buildParents(this.xml.getChild("HIERARCHIES"));
		this.arrayURoles=m.buildUserRoles(this.xml.getChild("USERROLES"));
		this.accesses=m.buildAccess(this.xml.getChild("ACCESSES"),user,arrayURoles);
		this.instances=m.buildModel(this.xml.getChild("INSTANCES"));
		this.properties=m.buildProp(this.xml.getChild("PROPERTIES"));
		this.rulesFiles=rules;
		this.instances.addAll(KnowledgeAdapter.toInstance(this.getEnumeratedFromXML().getAIPropertyDef()));
		this.arrayOrderProperties=m.buildOrderProp(this.xml.getChild("ORDERPROPERTIES"));			
		this.arrayAlias=m.buildAlias(this.xml.getChild("LISTALIAS"));
		this.arrayCM=m.buildCardMed(this.xml.getChild("LISTCARDMED"));
		this.arrayColumnProperties=m.buildColumnProperties(this.xml.getChild("COLUMNS"));
		this.arrayGroups=m.buildGroups(this.xml.getChild("GROUPS"));
		this.arrayEssentialProperties=m.buildEssentialProperties(this.xml.getChild("ESSENTIALPROPERTIES"));
		this.arrayListenerUtasks=m.buildListenerUtasks(this.xml.getChild("LISTENERUTASKS"));
		this.arrayMasks=m.buildMasks(this.xml.getChild("MASKS"));
		this.arrayRequireds=m.buildRequired(this.xml.getChild("REQUIREDS"));
		this.arrayIndexes=m.buildIndex(this.xml.getChild("INDEXES"));
		this.arrayGlobalUtasks=m.buildGlobalUtasks(this.xml.getChild("GLOBALUTASKS"));
	}

	public IndividualData getEnumeratedFromXML(){
		Element elemEnumeratedClasses = this.xml.getChild("ENUMERATEDCLASSES");
		IndividualData iData = messageFactory.buildIPropertyDef(elemEnumeratedClasses);
		return iData;
	}
	
	/*private String getRulesFromXML() {
		Element rules = this.xml.getChild("RULES");
		CDATA CData = (CDATA)rules.getContent().get(0);
		String fileRules = CData.getText();
		return fileRules;
	}*/
	
}
