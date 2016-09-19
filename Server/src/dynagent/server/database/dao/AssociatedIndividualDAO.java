package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import dynagent.common.basicobjects.AssociatedIndividual;
import dynagent.common.utils.Auxiliar;

public class AssociatedIndividualDAO extends ObjectDAO{
	
	public AssociatedIndividualDAO(){
		super("Associated_Individual",true);
	}

	public void insert(AssociatedIndividual in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public String getValues(Object v) {
		AssociatedIndividual a = (AssociatedIndividual)v;
		
		String stringvalue = "";
	
		if(a.getIdtoKey() == null)
			stringvalue += "NULL,";
		else
			stringvalue += a.getIdtoKey()+ ",";	
		
		if(a.getIdtoSufix() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + Auxiliar.arrayIntegerToString(a.getIdtoSufix(),",") + "',";
		
		if(a.getAssociatedIdto() == null)
			stringvalue += "NULL";
		else
			stringvalue += a.getAssociatedIdto();

		return stringvalue;
	}
	
	public String getParams(){
		return " (IDTO_KEY, IDTO_SUFIX, ASSOCIATED_IDTO) ";
	}
	
	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		dynagent.common.basicobjects.AssociatedIndividual as = new dynagent.common.basicobjects.AssociatedIndividual();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0 && rs.getString(i+1)!=null)
				as.setIdtoKey(new Integer(rs.getString(i+1)).intValue());
			else if(i == 1 && rs.getString(i+1)!=null)
				as.setIdtoSufix(Auxiliar.stringToArrayInteger(rs.getString(i+1),","));
			else if(i == 2 && rs.getString(i+1)!=null)
				as.setAssociatedIdto(new Integer(rs.getString(i+1)).intValue());
		}
		return as;
	}
}