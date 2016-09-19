package dynagent.server.services;

import java.util.LinkedList;
import java.util.List;

public class SqlStatementInfo {
	
	public static final int INSERT = 0;
	public static final int DELETE = 1;
	public static final int UPDATE = 2;
	
	private int operation;
	private String tableName;
	private List<String> columnNames;
	private List<Object> values;
	
	public SqlStatementInfo(String tableName){
		this.tableName = tableName;
		this.columnNames = new LinkedList<String>();
		this.values = new LinkedList<Object>();
		this.operation = -1;
	}
	
	public void addColumnWithValue(String columnName, Object value){
		columnNames.add(columnName);
		values.add(value);
	}
	
	public void setOperation(int operationCode){
		operation = operationCode;
	}
	
	public String getSqlStatement(int bussiness){
		
		return null;
	}
}
