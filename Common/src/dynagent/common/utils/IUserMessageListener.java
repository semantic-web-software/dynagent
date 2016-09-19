package dynagent.common.utils;

import java.awt.Component;
import java.awt.Window;

import javax.swing.Icon;

public interface IUserMessageListener {
    public int showOptionMessage(String message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue,Component c);
    public void showErrorMessage(String message,Component c);
	public void showMessage(String message,Component c);
	public String showInputMessage(String message, String title, int messageType, Icon icon, String[] options, String initialValue,Component c);
	public String showInputMessageWithComponents(Object messageAndComponents, String title, int messageType, Icon icon, String[] options, String initialValue,Component c);
}