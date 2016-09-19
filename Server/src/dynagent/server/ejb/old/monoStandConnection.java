package dynagent.ejb.old;
/*package dynagent.ejb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.DriverManager;

import javax.naming.NamingException;
import javax.sql.DataSource;

public class monoStandConnection implements businessConnection {
    Connection busCon, proCon;
    int business;
    String login,pwd,url;
    public monoStandConnection(int business, String login, String pwd) throws SQLException,ClassNotFoundException {
        this.business=business;
        url="jdbc:odbc:dyna"+business;
        this.login=login;
        this.pwd=pwd;
        Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        busCon =buildBusConnection();
        proCon= buildProConnection();
    }

    private Connection buildBusConnection() throws SQLException{
        System.out.println("bus "+business+",log "+login+",pwd "+pwd);
	return DriverManager.getConnection(url,login, pwd);
    }

    private Connection buildProConnection() throws SQLException{
        return DriverManager.getConnection("jdbc:odbc:Procesos", login, pwd);
    }

    public void close() throws SQLException {
    }

    public void commit() throws SQLException {
    }
    public int getBusinessCount(){ return 1;}

    public Connection getBusinessConn(int empresa) throws NamingException,
            SQLException {
        return busCon;
    }

    public Connection getProcessConn() throws NamingException, SQLException {
        return proCon;
    }

    public Connection getSecondProcessConn() throws NamingException,
            SQLException {
        return buildProConnection();
    }

    public Connection getSecondTempNoTransactionConnection(int empresa) throws
            NamingException, SQLException {
        return buildBusConnection();
    }

    public int getUserBusiness(String user) {
        return business;
    }

    public Connection getUserConn(String user) throws NamingException,
            SQLException {
        return busCon;
    }

    public void rollback() throws SQLException {
    }

    public int updateNewUser(String user, ArrayList roles) throws SQLException {
        return 0;
    }

    public void updateNewUser(int empresa, String user) throws SQLException {
    }
}
*/