package dynagent.ruleengine;

public class Null {
	
	public static final int NULL = 0;
	public static final int NOTNULL = 1;
	private int searchBy;

	public Null (int value){
		if(value == NULL || value == NOTNULL)
			searchBy = value;
		else{
			//System.out.println("Valor erroneo en el constructor, inicializamos con NULL( = 0)");
			searchBy = NULL;
		}
	}

	public int getSearchBy() {
		return searchBy;
	}

	public void setSearchBy(int searchBy) {
		if(searchBy == NULL || searchBy == NOTNULL)
			this.searchBy = searchBy;
	}
	
	public String toString(){
		if (searchBy==0)
			return "NULL";
		else
			return "NOT NULL";
		
	}	
	
}
