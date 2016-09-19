package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import dynagent.common.basicobjects.CardMed;

public class CardMedDAO extends ObjectDAO{
	
	public CardMedDAO(){
		super("CardMed",true);
	}
	
	public void insert(CardMed in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public String getValues(Object v) {
		CardMed cm= (CardMed)v;
		
		String stringvalue = "";
	
		if(cm.getIdtoParentName()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + cm.getIdtoParentName()+ "',";	
		
		if(cm.getIdtoName()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + cm.getIdtoName()+ "',";
		
		if(cm.getCardmed()== null)
			stringvalue += "NULL";
		else
			stringvalue += cm.getCardmed();
		
		
		
		return stringvalue;
		
	}
	
	public String getParams(){
		
		return " (IDTO_PARENT_NAME, IDTO_NAME, CARDMED) ";
	}

	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}
