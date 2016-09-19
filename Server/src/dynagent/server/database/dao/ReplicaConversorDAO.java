package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import dynagent.common.basicobjects.ReplicaConversor;

public class ReplicaConversorDAO extends ObjectDAO{

	public ReplicaConversorDAO() {
		super("Replica_Conversor", true);
	}

	public void insert(ReplicaConversor in) throws SQLException{
		in.setPrefix(in.getPrefix().replaceAll("'", "''"));
		in.setValueFixed(in.getValueFixed().replaceAll("'", "''"));
		i.insert(this, in, false);
	}
	
	public void update(String set, String where) throws SQLException{
		i.update(this, set, where);
	}
	
	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		dynagent.common.basicobjects.ReplicaConversor in = new dynagent.common.basicobjects.ReplicaConversor();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0 && rs.getString(i+1)!=null)
				in.setIdTo(new Integer(rs.getString(i+1)));
			if(i == 1 && rs.getString(i+1)!=null)
				in.setProp(rs.getInt(i+1));
			if(i == 2 && rs.getString(i+1)!=null)
				in.setPropLink(rs.getInt(i+1));
			if(i == 3 && rs.getString(i+1)!=null)
				in.setIdtoSup(rs.getInt(i+1));
			if(i == 4 && rs.getString(i+1)!=null)
				in.setPropPrefix(rs.getInt(i+1));
			if(i == 5 && rs.getString(i+1)!=null)
				in.setPrefix(rs.getString(i+1));
			if(i == 6 && rs.getString(i+1)!=null)
				in.setValueFixed(rs.getString(i+1));
			if(i == 7 && rs.getString(i+1)!=null)
				in.setValueClsFixed(rs.getInt(i+1));
			if(i == 8 && rs.getString(i+1)!=null)
				in.setIdtoDest(rs.getInt(i+1));
			if(i == 9 && rs.getString(i+1)!=null)
				in.setPropDest(rs.getInt(i+1));
			if(i == 10 && rs.getString(i+1)!=null)
				in.setDownload(rs.getBoolean(i+1));
		}
		return in;
	}

	@Override
	public String getValues(Object v) {
		ReplicaConversor ind = (ReplicaConversor)v;
		
		String stringvalue = "";
	
		if(ind.getIdTo() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getIdTo()+ ",";	
		if(ind.getProp() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getProp()+ ",";	
		if(ind.getPropLink() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getPropLink()+ ",";	
		if(ind.getIdtoSup() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getIdtoSup() + ",";
		if(ind.getPropPrefix() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getPropPrefix()+ ",";	
		if(ind.getPrefix() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + ind.getPrefix().replaceAll("'", "''") + "',";	
		if(ind.getValueFixed() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + ind.getValueFixed().replaceAll("'", "''") + "',";	
		if(ind.getValueClsFixed() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getValueClsFixed()+ ",";	
		if(ind.getIdtoDest() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getIdtoDest()+ ",";	
		if(ind.getPropDest() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getPropDest()+ ",";	
		if(ind.isDownload())
			stringvalue += "'Y'";
		else
			stringvalue += "'N'";
		return stringvalue;
	}
	
	public String getParams(){
		return " (ID_TO, PROP, PROP_LINK, IDTO_SUP, PROP_PREFIX, PREFIX, VALUE_FIXED, VALUE_CLS_FIXED, " +
				"IDTO_DEST, PROP_DEST, DOWNLOAD) ";
	}
	
	public String getPK(){
		return "ID_TO";
	}

}