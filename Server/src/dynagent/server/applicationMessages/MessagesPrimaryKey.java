package dynagent.server.applicationMessages;

import java.io.Serializable;

public class MessagesPrimaryKey implements Serializable {

	public String destinatary;

	public Integer idEntity;

	public String idEvent;

	public MessagesPrimaryKey() {
		
	}

	public MessagesPrimaryKey(String destinatary, Integer idEntity, String idEvent) {
		this.destinatary = destinatary;
		this.idEntity = idEntity;
		this.idEvent = idEvent;
	}
	
	public String getDestinatary() {
		return destinatary;
	}
	public Integer getIdEntity() {
		return idEntity;
	}
	public String getIdEvent() {
		return idEvent;
	}

	public boolean equals(Object other) {
		if (other instanceof MessagesPrimaryKey) {
			return (destinatary.equals(((MessagesPrimaryKey) other).destinatary) && 
					idEntity.equals(((MessagesPrimaryKey) other).idEntity) && 
					idEvent.equals(((MessagesPrimaryKey) other).idEvent));
		}
		return false;
	}

	public int hashCode() {
		return destinatary.concat(String.valueOf(idEntity)).concat(idEvent).hashCode();
	}
}
