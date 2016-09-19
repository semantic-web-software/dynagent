package dynagent.common.knowledge;

import dynagent.common.sessions.Session;

public interface IHistoryDDBBListener {
	public void initChangeHistory();
	public void changeHistory(int ido,int idto,String rdn,int oldIdo,int operation,Integer idtoUserTask,Session sessionUsed);
	public void endChangeHistory();
}
