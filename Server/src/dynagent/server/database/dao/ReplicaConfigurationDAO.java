package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import dynagent.common.basicobjects.ReplicaConfiguration;

public class ReplicaConfigurationDAO extends ObjectDAO{

	public ReplicaConfigurationDAO() {
		super("Replica_Configuration", true);
	}

	public void insert(ReplicaConfiguration in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public void update(String set, String where) throws SQLException{
		i.update(this, set, where);
	}
	
	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		dynagent.common.basicobjects.ReplicaConfiguration in = new dynagent.common.basicobjects.ReplicaConfiguration();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0 && rs.getString(i+1)!=null)
				in.setIdTo(new Integer(rs.getString(i+1)));
			if(i == 1 && rs.getString(i+1)!=null)
				in.setPropLink(rs.getInt(i+1));
			if(i == 2 && rs.getString(i+1)!=null)
				in.setIdtoSup(rs.getInt(i+1));
			if(i == 3 && rs.getString(i+1)!=null)
				in.setLocalOrigin(rs.getBoolean(i+1));
		}
		return in;
	}

	@Override
	public String getValues(Object v) {
		ReplicaConfiguration ind = (ReplicaConfiguration)v;
		
		String stringvalue = "";
	
		if(ind.getIdTo() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getIdTo()+ ",";	
		if(ind.getPropLink() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getPropLink()+ ",";	
		if(ind.getIdtoSup() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getIdtoSup() + ",";
		if(ind.isLocalOrigin())
			stringvalue += "'Y'";
		else
			stringvalue += "'N'";
		return stringvalue;
	}
	
	public String getParams(){
		return " (ID_TO, PROP_LINK, IDTO_SUP, LOCAL_ORIGIN) ";
	}
	
	public String getPK(){
		return "ID_TO";
	}

}
