/***
 * FactAccess.java
 * @author: Ildefonso Montero Perez - monteroperez@us.es
 */

package dynagent.ruleengine.src.ruler;

import dynagent.ruleengine.src.data.dao.Peer;

public class FactPeer extends Fact{
	
	private int RELATION;
	private int ROL;
	private int PEER;
	
	public int getPEER() {
		return PEER;
	}

	public void setPEER(int peer) {
		PEER = peer;
	}

	public int getRELATION() {
		return RELATION;
	}

	public void setRELATION(int relation) {
		RELATION = relation;
	}

	public Integer getROL() {
		return ROL;
	}

	public void setROL(int rol) {
		ROL = rol;
	}

	public Peer toPeer(){
		Peer a = new Peer();
		a.setRELATION(this.getRELATION());
		a.setROL(this.getROL());
		a.setPEER(this.getPEER());
		return a;
	}
	
}
