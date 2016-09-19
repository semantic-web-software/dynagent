package dynagent.common.knowledge;

public interface IMessageListener {
	public void sendMessage(String message);
	public Boolean sendQuestion(String message, boolean initialSelectionIsYes);
}
