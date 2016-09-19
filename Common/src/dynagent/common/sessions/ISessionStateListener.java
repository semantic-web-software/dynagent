package dynagent.common.sessions;

public interface ISessionStateListener {

	public void sessionClosed(Session session,boolean commit);
}
