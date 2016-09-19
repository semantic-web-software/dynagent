package dynagent.tools.replica.monitorizador;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.utils.jdomParser;

public class ParserMonitorizadorReplica {
	
	private Integer maxServerId=1;
	private Integer maxTienda=1;
	
	public Integer getMaxServerId() {
		return maxServerId;
	}
	public Integer getMaxTienda() {
		return maxTienda;
	}

	public HashMap<Integer, SlaveData> readXML(String path) throws IOException, JDOMException {
		BufferedReader in = new BufferedReader(new FileReader(path)); 
		StringBuffer dataS = new StringBuffer("");
		String buff = "";
		while(buff!= null){
			dataS.append(buff);
			buff = in.readLine();
		}
		Element xml = jdomParser.readXML(dataS.toString()).getRootElement();
		//System.out.println(jdomParser.returnXML(xml));
		return parser(xml);
	}
	
	private HashMap<Integer,SlaveData> parser(Element xml) {
		HashMap<Integer,SlaveData> hSlaveData = new HashMap<Integer, SlaveData>();
		Iterator it = xml.getChildren().iterator();
		while (it.hasNext()) {
			Element elemDB = (Element)it.next();
			String databaseIP = elemDB.getAttributeValue(ConstantsMonitorizadorReplica.IP);
			Integer business = Integer.parseInt(elemDB.getAttributeValue(ConstantsMonitorizadorReplica.BUSINESS));
			Iterator it2 = elemDB.getChildren().iterator();
			while (it2.hasNext()) {
				Element elemPort = (Element)it2.next();
				String name = elemPort.getName();
				Integer port = Integer.parseInt(elemPort.getAttributeValue(ConstantsMonitorizadorReplica.PORT));
				String masterIdStr = elemPort.getAttributeValue(ConstantsMonitorizadorReplica.MASTER_ID);
				Integer masterId = null;
				if (masterIdStr!=null)
					masterId = Integer.parseInt(masterIdStr);
				
				String hostname = elemPort.getAttributeValue(ConstantsMonitorizadorReplica.HOSTNAME);
				String sufix = elemPort.getAttributeValue(ConstantsMonitorizadorReplica.SUFIX);
				if (sufix!=null)
					maxSufix(Integer.parseInt(sufix));
				SlaveData sd = new SlaveData(name, business, databaseIP, port, hostname, sufix, masterId);
				Integer serverId = Integer.parseInt(elemPort.getAttributeValue(ConstantsMonitorizadorReplica.SERVER_ID));
				maxServerId(serverId);
				hSlaveData.put(serverId, sd);
			}
		}
		return hSlaveData;
	}
	private void maxServerId(Integer serverId) {
		if (maxServerId<serverId)
			maxServerId = serverId;
	}
	private void maxSufix(Integer sufix) {
		if (maxTienda<sufix)
			maxTienda = sufix;
	}
}
