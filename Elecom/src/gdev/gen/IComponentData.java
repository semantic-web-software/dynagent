package gdev.gen;

import java.text.ParseException;


public interface IComponentData {
	
	public void setValue(Object newValue,Object oldValue) throws ParseException,AssignValueException;
	
	public void initValue() throws ParseException,AssignValueException;
	
	public Object getValue();
	
	public void clean() throws ParseException, AssignValueException;

}
