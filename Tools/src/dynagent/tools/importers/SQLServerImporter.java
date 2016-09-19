package dynagent.tools.importers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

import javax.naming.NamingException;

public class SQLServerImporter implements IImporter {

	@Override
	public boolean copyDataBase(int original, int destination) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createDataBase(int bns) throws SQLException, NamingException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean dropDataBase(int bns) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setHost(String host) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPort(int port) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createSystemViews(int bns) throws SQLException, NamingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean createBackup(int bns, String backupName,
			Set<String> includedTables, Set<String> excludedTables) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean restoreBackup(int bns, String backupName, boolean onlyData) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean dropConnections(int bns) throws NamingException,
			SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean createSchema(int bns, int template) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean renameDataBase(int original, int destination)
			throws SQLException, NamingException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createBusinessFunctions(int bns) throws SQLException, NamingException, IOException {
		// TODO Auto-generated method stub
		
	}

}
