package dynagent.ejb.old;
/*package dynagent.ejb;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.apache.commons.lang.StringUtils;

import com.microsoft.sqlserver.jdbc.SQLServerXADataSource;

public class poolDB implements businessConnection{
    final static int maxNumBusiness=100;
	java.sql.Connection[] m_pool= new java.sql.Connection[maxNumBusiness];
       	java.sql.Connection[] m_poolSecond= new java.sql.Connection[maxNumBusiness];
	HashMap m_userIndex= new HashMap();
	java.sql.Connection m_proConn;
      	java.sql.Connection m_proConnSecond;
	public int numeroEmpresas=0;
	private boolean autoCommit= false;
	InitialContext ic;
	boolean standAloneApp=false;

	String user="dynagent";
	String password="dynagent";
	String usuarioInterno="#EMPRESA_";
        String databaseIP="localhost";
        String processDB="process";
        Transaction xaTransaction=null;

        final static int COMMIT=1;
        final static int ROLLBACK=2;
        final static int CLOSE=3;

	public poolDB(String dbIP) throws NamingException,SQLException {
            ic= new InitialContext();
            if( dbIP!=null )
                databaseIP=dbIP;
            build();
	}

	public poolDB(String dbIP, boolean autoC) throws NamingException,SQLException{
            ic= new InitialContext();
            autoCommit= autoC;
            if( dbIP!=null )
                databaseIP=dbIP;
            build();
	}

	public poolDB(String dbIP, boolean autoC, boolean standAloneApp, Transaction xaTran) throws SQLException, ClassNotFoundException{
                xaTransaction=xaTran;
		try{
		if( !standAloneApp ) return;//este inicializador solo es para las standAloneApp
		this.standAloneApp=true;
		autoCommit= autoC;
                if( dbIP!=null )
                    databaseIP=dbIP;
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

		build();
		}catch(NamingException e){;}
	}

	public int getUserBusiness( String user ){
		Integer index= (Integer)m_userIndex.get( user );
		if( index==null ) return -1;
		return index.intValue();
	}

        public int getBusinessCount(){
            return numeroEmpresas;
        }

	public java.sql.Connection getUserConn( String user ) throws NamingException,SQLException{
		Integer index= (Integer)m_userIndex.get( user );
		if (index!=null)
			return getBusinessConn( index.intValue() );
		else
			return null;
	}

	public java.sql.Connection getBusinessConn( int empresa ) throws NamingException,SQLException{
            System.out.println("POOLDB BUSCON index "+(empresa-1));
		java.sql.Connection con= m_pool[empresa-1];
		if( con==null || con.isClosed()){
                    System.out.println("CON NULA");
                    con= buildConnection(autoCommit,"dyna"+empresa, empresa, true, true);
		}else
                     System.out.println("CON NOT NULA");
		return con;
	}
	
	public java.sql.Connection getBusinessConn( String nameBD) throws NamingException,SQLException{
       
		java.sql.Connection con = null;
		if (nameBD.length()>3 && StringUtils.equals(nameBD.substring(0, 4),"dyna"))
			con = getBusinessConn(Integer.parseInt(nameBD.substring(4, nameBD.length())));
		else
			con = buildConnection(autoCommit,nameBD, numeroEmpresas, true, true);
	java.sql.Connection con= m_pool[3];
	if( con==null || con.isClosed()){
                System.out.println("CON NULA");
                con= buildConnection(autoCommit,nameBD, 4, true, true);
	}else
                 System.out.println("CON NOT NULA");

        return con;
}

        public java.sql.Connection getSecondTempNoTransactionConnection( int empresa )
                    throws NamingException,SQLException{
                return buildConnection(autoCommit,"dyna"+empresa, empresa, true, false);
        }

        static String getMSSQL_DB_URL( String db, String databaseIP, String user, String pwd ){
            return "jdbc:sqlserver://"+databaseIP+";databaseName="+db+";user="+user+";password="+pwd;
        }

	public java.sql.Connection getProcessConn() throws NamingException,SQLException{
		buildProcessConn(autoCommit);
		return m_proConn;
	}

	void conItera(int operation) throws SQLException{
            for (int i = 0; i < maxNumBusiness; i++) {
                subConItera( operation, m_pool[i] );
                subConItera( operation, m_poolSecond[i] );
            }
            subConItera( operation, m_proConn );
            subConItera( operation, m_proConnSecond );
        }

        void subConItera(int op, java.sql.Connection con) throws SQLException {
            if( con == null ||
                con.isClosed() )
                return;
            switch( op ){
            case COMMIT:
                con.commit();
                break;
            case ROLLBACK:
                con.rollback();
                break;
            case CLOSE: {
                System.out.println("CLOSED");
                con.close();
            }
            }
        }
        public void commit() throws SQLException{
            if (isManagedTran())return;
            conItera(COMMIT);
        }
	public void rollback() throws SQLException{
            if (isManagedTran())return;
            conItera(ROLLBACK);
	}

	public void close() throws SQLException{
            conItera(CLOSE);
            m_proConn=null;
            m_proConnSecond=null;
            for (int i = 0; i < maxNumBusiness; i++) {
                m_pool[i]=null;
                m_poolSecond[i]=null;
            }
        }

	private void build() throws NamingException,SQLException{
		java.sql.Connection  con=null;
		buildProcessConn(autoCommit);
		String sql= "SELECT indexEmpresa, Login FROM Usuarios";
		Statement st= m_proConn.createStatement();
		ResultSet rs= st.executeQuery( sql );
		while( rs.next() ){
			Integer index= new Integer( rs.getInt( 1 ));
			String login= rs.getString( 2 );
			m_userIndex.put( login, index );
		}
		st.close();
		sql= "SELECT NUMERO_EMPRESAS FROM configuracion";
		st= m_proConn.createStatement();
		rs= st.executeQuery( sql );
		rs.next();
		numeroEmpresas= rs.getInt(1);
		st.close();
		for( int i=0; i< numeroEmpresas; i++ ){
			m_userIndex.put( usuarioInterno+ (i+1), new Integer(i+1) );
					//lo utilizara en instanceService cuando solo se la empresa
					// pero no el usuario que comenza la acción
                        int indexBusiness=i+1;
                        if(standAloneApp)
                            buildConnection(autoCommit,"dyna"+indexBusiness,indexBusiness,true,true);
		}
                if( !standAloneApp )
                    close();
	}

	private java.sql.Connection buildConnection( boolean autoC,
                                                     String dbName,
                                                     int index,
                                                     boolean bussinessDB,
                                                     boolean mainConnection )
                throws NamingException,SQLException{
            java.sql.Connection[] pool = mainConnection ? m_pool : m_poolSecond;
            if (index == 0) {
                System.out.println("ERRORPOOL, indice cero");
                return null;
            }
            int pos = index - 1;
            java.sql.Connection con = null;

            if (standAloneApp) {
                if (!isManagedTran()) {
                    String url = getMSSQL_DB_URL(dbName, databaseIP, user, password);
                    System.out.println("CONECTANDO URL "+url+" USER "+user+" PWD "+password);
                    con = DriverManager.getConnection(url, user, password);
                } else
                    con = getXAConnection(dbName);
            } else {
                System.out.println("POOLDB NEW DES CON index "+pos);
                DataSource ds = (DataSource) ic.lookup("java:jdbc/" + dbName);
                con = ds.getConnection();
            }
            if (!isManagedTran())
                con.setAutoCommit(autoC);
            if( bussinessDB ){
                pool[pos] = con;
                System.out.println("POOLED BUSCON "+pos);
            }else{
                if( mainConnection )
                    m_proConn= con;
                else
                    m_proConnSecond= con;
            }
            return con;
        }

	public java.sql.Connection getSecondProcessConn() throws NamingException,SQLException{
		System.out.println("POOL, second con");
                if( m_proConnSecond!=null && !m_proConnSecond.isClosed())
                    return m_proConnSecond;
		return buildConnection( true, processDB, -1, false, false );
	}

	private java.sql.Connection buildProcessConn(boolean autoc)
                throws NamingException,SQLException{
            if( m_proConn!=null && !m_proConn.isClosed())
                return m_proConn;
            m_proConn =  buildConnection( autoc, processDB, -1, false, true );
            return m_proConn;
	}

        boolean isManagedTran(){
            return xaTransaction!=null || !standAloneApp;
        }

        private java.sql.Connection getXAConnection(String dbname)
                throws SQLException{
            SQLServerXADataSource xaDS = new SQLServerXADataSource();
            xaDS.setUser(user);
            xaDS.setPassword(password);
            xaDS.setURL(getMSSQL_DB_URL(dbname,databaseIP,user,password));
            javax.sql.XAConnection xaCon = xaDS.getXAConnection();
            XAResource xaRes= xaCon.getXAResource();
            try {
                xaTransaction.enlistResource(xaRes);
            } catch (java.lang.IllegalStateException ex) {
            } catch (RollbackException ex) {
            } catch (javax.transaction.SystemException ex) {
                    *//** @todo Handle this exception *//*
            }
            java.sql.Connection con = xaCon.getConnection();
            return con;
        }

        public int updateNewUser( String user ) throws SQLException, NamingException {
            if( user==null ) return -1;

            if( m_userIndex.containsKey( user ) ){
                        return getUserBusiness( user );
                }
                String sql= "SELECT indexEmpresa FROM Usuarios INNER JOIN S_USER_ROL SUR ON "+
                                " Usuarios.Login= SUR.USUARIO WHERE Login='"+user.replaceAll("'","''")+"'";
                Statement st = null;
                st = getProcessConn().createStatement();
                ResultSet rs= st.executeQuery( sql );
                Integer empresa= null;

                while( rs.next() ){
                        if( empresa==null ){
                                empresa= new Integer( rs.getInt( 1 ));
                                m_userIndex.put( user, empresa );
                        }
                }
                st.close();
                if( empresa==null ) return -1;
                return empresa.intValue();
	}

	public int updateNewUser( String user, ArrayList roles )
                throws SQLException, NamingException {
		if( m_userIndex.containsKey( user ) ){
			return getUserBusiness( user );
		}
		String sql= "SELECT indexEmpresa,rol FROM Usuarios INNER JOIN S_USER_ROL SUR ON "+
				" Usuarios.Login= SUR.USUARIO WHERE Login='"+user.replaceAll("'","''")+"'";
		Statement st= getProcessConn().createStatement();
		ResultSet rs= st.executeQuery( sql );
		Integer empresa= null;

		while( rs.next() ){
			if( empresa==null ){
				empresa= new Integer( rs.getInt( 1 ));
				m_userIndex.put( user, empresa );
			}
			roles.add( new Integer(rs.getInt( 2 )));
		}
		st.close();
		if( empresa==null ) return -1;
		return empresa.intValue();
	}

	public void updateNewUser( int empresa, String user ) throws SQLException{
		m_userIndex.put( user, new Integer( empresa ) );
	}
}
*/