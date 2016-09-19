package dynagent.server.services.querys;

public class DataInfo {
	
	private String type;
	
	private int column;

	private int columnQuery;
	
	public DataInfo(String type, int columnQuery, int column) {
		this.type = type;
		this.columnQuery = columnQuery;
		this.column = column;
	}


	public int getColumn() {
		return column;
	}
	public void setColumn(int column) {
		this.column = column;
	}

	public int getColumnQuery() {
		return columnQuery;
	}
	public void setColumnQuery(int columnQuery) {
		this.columnQuery = columnQuery;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String toString() {
		return "column " + column + ", columnQuery " + columnQuery + ", type " + type;
	}
	
}
