package dynagent.ejb.old;
/*package dynagent.ejb;

import org.jdom.*;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import javax.naming.NamingException;

import dynagent.knowledge.*;
import dynagent.services.InstanceService;

public class Diccionario{

	businessConnection m_pool=null;
	ArrayList m_metaBusiness= new ArrayList();
	InstanceService m_IS;

	public Diccionario( businessConnection pool, InstanceService is ) throws SQLException,JDOMException{
		m_pool=pool;
		m_IS= is;
	}

	public void build() throws SQLException,JDOMException, Exception{
		m_metaBusiness.clear();
		for( int i= 1; i<= m_pool.getBusinessCount(); i++){
                    System.out.println("meta "+i);
			metaData mm= new metaData(i);
			m_metaBusiness.add( mm );
			buildUserRol( i, mm );
			buildEnums( i, mm );
			buildTMs( i, mm );
			buildHerencias( m_pool.getProcessConn(),i, mm );
			buildDirectSpecialization( i, mm );
			buildDominios( m_pool.getProcessConn(), i, mm );
			buildFilters( m_pool.getProcessConn(), i, mm );
			//buildMetaTOs( m_pool.getBusinessConn( i ), i, mm );
			buildTOLabels( i, mm );
			buildGroupLabels( i, mm );
			buildCategoriasRel( i, mm );
			buildATLabels( i, mm );
			buildCtxs( m_pool, i, mm );
			buildCtxsCondicionados( i, mm );
			buildActionData( m_pool.getProcessConn(),i, mm );
			buildTasks( m_pool,i, mm );
			buildProcess( m_pool.getProcessConn(),i, mm );
			buildTrans( m_pool.getProcessConn(),i, mm );
			buildTaskFilter( m_pool.getProcessConn(),i, mm );
			buildReports( m_pool.getProcessConn(),i, mm );
		}
	}

	public void buildUserRol( int business, metaData mm ) throws NamingException, SQLException{

		String sql= 	"SELECT Usuario, Rol FROM S_USER_ROL WITH(NOLOCK) WHERE empresa="+business;

		Statement st= null;
                ResultSet rs=null;
                try{
                    st = m_pool.getProcessConn().createStatement();
                    rs = st.executeQuery(sql);
                    while (rs.next())
                        mm.addUserRol(rs.getString(1), rs.getInt(2));
                }finally{
                    if (rs != null) rs.close();
                    if (st != null) st.close();
                }
	}

	public void buildReports( java.sql.Connection proCon, int business, metaData mm )
		throws NamingException, SQLException{

		String sql= "SELECT OID,NAME,FILTER,PLANTILLA FROM S_DYNAMICS_REPORTS WITH(NOLOCK) WHERE empresa="+business;
                Statement st= null;
                ResultSet rs=null;
                try{
                    st = m_pool.getProcessConn().createStatement();
                    rs = st.executeQuery(sql);
                    while (rs.next()) {
                        mm.addDinReport(rs.getString(1), rs.getString(2),
                                        rs.getInt(3), rs.getString(4));
                    }
                }finally{
                    if (rs != null) rs.close();
                    if (st != null) st.close();
                }
	}

	public static void buildProcess( java.sql.Connection proCon, int business, metaData mm )
		 throws SQLException{
                Statement st= null;
                ResultSet rs=null;
                boolean rsClosed=false;
                String sql= "SELECT ID_PROCESS FROM S_PROCESS WITH(NOLOCK) WHERE EMPRESA=" + business;
		try{
		st= proCon.createStatement();
		rs= st.executeQuery( sql );
		ArrayList procesos=new ArrayList();
		while( rs.next() )
			procesos.add( new Integer(rs.getInt(1)));
                rs.close();
                rsClosed=true;

		for( int i=0;i<procesos.size();i++){
			Integer idPro=(Integer)procesos.get(i);
			sql= 	"SELECT DISTINCT PRO.ID_PROCESS, PRO.OID, CTX.id_Action FROM "+
				"S_TASK_ACTION_CONTEXT 	CTX 	WITH(NOLOCK)	        				INNER JOIN "+
                                "S_TASK_ACTION 		STA     WITH(NOLOCK)	ON CTX.id_Action=STA.id_Action 		INNER JOIN "+
				"TASK_TRANSITION 	TT	WITH(NOLOCK)    ON TT.id_tran=sta.id_tran 		INNER JOIN "+
				"S_TASKS		TAS	WITH(NOLOCK)    ON ( TAS.id_task=TT.id_task AND "+
								"                    TAS.STATE_START= TT.ID_EST ) 	INNER JOIN " +
				"S_PROCESS		PRO	WITH(NOLOCK)    ON TAS.ID_PROCESS=PRO.ID_PROCESS "+


			"WHERE 	STA.USER_ACTION=1 AND STA.PREV_A_TRANS=1 AND TAS.PARENT_TASK IS NULL AND " +
				"PRO.ID_PROCESS= " + idPro;

			rs= st.executeQuery( sql );
                        rsClosed=false;
			if( rs.next() ){
				processType pt= new processType(rs.getInt(1),rs.getString(2),rs.getInt(3));
				mm.addProcess( pt );
			}
                        rs.close();
                        rsClosed=true;
		}
		}finally{
                    if (rs != null && !rsClosed  ) rs.close();
                    if (st != null ) st.close();
                }
	}

	public static void buildTasks( businessConnection pool, int business, metaData mm )
                throws SQLException,NamingException {
            Statement st= null;
            ResultSet rs=null;
            try {
                String sql = "SELECT ID_TASK, OID, STATE_START,TA_POS " +
                             "FROM S_TASKS TA WITH(NOLOCK) INNER JOIN  dyna" + business +
                             ".dbo.T_REG_TA_POS POS WITH(NOLOCK)" +
                             "					ON TA.ID_AT_STATE= POS.ID_TA " +
                             "WHERE EMPRESA=" + business;
                java.sql.Connection proCon = pool.getProcessConn();
                st = proCon.createStatement();
                rs = st.executeQuery(sql);
                while(rs.next()) {
                    int id = rs.getInt(1);
                    String label = rs.getString(2);
                    int state = rs.getInt(3);
                    int taposAtState = rs.getInt(4);
                    mm.addTask(id, label, state, taposAtState);
                    taskType tt = mm.getTask(new Integer(id));
                    java.sql.Connection con2 = pool.getSecondProcessConn();
                    Statement st2 = null;
                    ResultSet rs2 = null;
                    try{
                        st2 = con2.createStatement();
                        rs2 = st2.executeQuery("SELECT ID,LOCK FROM S_STATES WITH(NOLOCK) WHERE EMPRESA=" +
                                               business + " AND id_task=" + id);
                        while (rs2.next())
                            tt.addState(rs2.getInt(1), (rs2.getInt(2) == 0 ? false : true));
                    }catch(SQLException e){
                        throw e;
                    }finally{
                        if (rs2 != null) rs2.close();
                        if (st2 != null) st2.close();
                    }
                }
            }finally{
                if (rs != null) rs.close();
                if (st != null) st.close();
            }
        }

	public static void buildTaskFilter( java.sql.Connection proCon, int business, metaData mm )
		 throws SQLException{
            Statement st= null;
            ResultSet rs=null;
            try{
                String sql =
                        "SELECT 'TASK' AS LEVEL, CTX_OBJ, TA.ID_TRAN, ID_PROCESS, ID_TASK " +
                        "FROM 	S_TASK_ACTION TA WITH(NOLOCK) INNER JOIN " +
                        "TASK_TRANSITION TT WITH(NOLOCK) ON TA.ID_TRAN=TT.ID_TRAN " +
                        "WHERE TA.EMPRESA=" + business +
                        " AND CTX_OBJ IS NOT NULL " +
                        "UNION " +
                        "SELECT 'ACT' AS LEVEL, CONTEXT_REF, TA.ID_TRAN, ID_PROCESS, ID_TASK " +
                        "FROM 	S_FILTER_FIXING FX WITH(NOLOCK) INNER JOIN " +
                        "S_TASK_ACTION_CONTEXT TAC WITH(NOLOCK) ON FX.ID_CTX=TAC.ID_CTX INNER JOIN " +
                        "S_TASK_ACTION TA WITH(NOLOCK) ON TAC.ID_ACTION=TA.ID_ACTION INNER JOIN " +
                        "TASK_TRANSITION TT WITH(NOLOCK) ON TA.ID_TRAN=TT.ID_TRAN " +
                        "WHERE TA.EMPRESA=" + business;

                st = proCon.createStatement();
                rs = st.executeQuery(sql);
                while (rs.next()) {
                    String level = rs.getString(1);
                    int ctx = rs.getInt(2);
                    int tran = rs.getInt(3);
                    int pro = rs.getInt(4);
                    int task = rs.getInt(5);
                    mm.addTaskContextFix(pro, task, tran, ctx, level.equals("TASK"));
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                if (rs != null) rs.close();
                if (st != null) st.close();
            }
	}

	public static void buildTrans( java.sql.Connection proCon, int business, metaData mm ) throws SQLException{
            Statement st= null;
            ResultSet rs=null;
            try{
                String sql = "SELECT TT.ID_TRAN,ID_TASK,ID_EST,ID_EST_NEW,TT.OID,TT.OWNER,TA.ID_ACTION,CTX.ID_CTX 		" +
                             "	FROM 												" +
                             " 	 TASK_TRANSITION TT WITH(NOLOCK)							LEFT JOIN 	" +
                             "	(S_TASK_ACTION TA WITH(NOLOCK)	        						INNER JOIN 	" +
                             "	 S_TASK_ACTION_CONTEXT CTX WITH(NOLOCK) 	ON 	TA.ID_ACTION= CTX.ID_ACTION 	INNER JOIN	" +
                             "	 dyna" + business +
                        ".dbo.T_INDEX_TO TH WITH(NOLOCK)	ON (	CTX.ID_TO=TH.ID_TO	 AND			" +
                             "							TH.ID_TO_H=" + helperConstant.TO_TASK +
                             ")		" +
                             "						)ON (	TA.ID_TRAN=TT.ID_TRAN AND 			" +
                             "							TA.USER_ACTION=1 ) 				" +
                             " 	WHERE TT.EMPRESA=" + business +
                             " AND ID_TO_ROOT IS NULL ";

                st = proCon.createStatement();
                rs = st.executeQuery(sql);
                while (rs.next()) {
                    int id = rs.getInt(1);
                    int idTask = rs.getInt(2);
                    int stIni = rs.getInt(3);
                    int stEnd = rs.getInt(4);
                    String label = rs.getString(5);
                    int owPol = rs.getInt(6);
                    int action = rs.getInt(7);
                    int ctx = rs.getInt(8);
                    mm.addTrans(id,
                                new taskTransition(id, idTask, stIni, stEnd, label,
                            action, ctx, owPol));
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                if (rs != null) rs.close();
                if (st != null) st.close();
            }
	}

	public static void buildActionData( java.sql.Connection proCon, int business, metaData mm )
		throws SQLException{
            Statement st = null;
            ResultSet rs = null;
            try {
                String sql =
                        "SELECT ID_ACTION,ACTION_TYPE,ID_TRAN,OPERATION_TYPE,DETALLE " +
                        " FROM S_TASK_ACTION WITH(NOLOCK) WHERE EMPRESA=" + business;
                st = proCon.createStatement();

                rs = st.executeQuery(sql);
                while (rs.next()) {
                    int id = rs.getInt(1);
                    int type = rs.getInt(2);
                    int tran = rs.getInt(3);
                    int op = rs.getInt(4);
                    String detail = rs.getString(5);
                    Element eDetail = null;
                    if (!rs.wasNull())
                        eDetail = jdomParser.readXML(detail).getRootElement();
                    mm.addActionData(id, type, tran, eDetail, op);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (rs != null) rs.close();
                if (st != null) st.close();
            }
        }

	public static void buildCtxs( businessConnection pool, int business, metaData mm )
		throws NamingException, SQLException, JDOMException{
            Statement st = null;
            ResultSet rs = null;
            boolean rsClosed=false;
            try{
                java.sql.Connection proCon = pool.getProcessConn();
                String sql =
                        "SELECT EMPRESA,ID_ACCESO,USERROL,FIXID,TO_CURRENT,TO_ROOT,ID_REL,ID_ROL_CTX," +
                        "ID_ROL_CURRENT," +
                        "ID_FILTER,N_MIN,N_MAX,ROL_SUP,IMG.ID,PROPERTIES,ID_DOMINIO,DETALLE_ROL," +
                        "FIX_OPTION," +
                        "FIX_NODE,CTX_FIX,REF_FIX,FIX_DETAIL,INCRUSTAR " +
                        "FROM V_ACCESOS VA WITH(NOLOCK) LEFT JOIN " +
                        "dyna" + business +
                        ".dbo.O_IMAGENES IMG WITH(NOLOCK) ON VA.TO_CURRENT=IMG.ID_O " +
                        "WHERE EMPRESA IN(0," + business + ")" +
                        " ORDER BY ID_ACCESO, USERROL, FIXID";

                st = proCon.createStatement();
                rs = st.executeQuery(sql);
                int oldCtx = -1, oldURol = -1, oldFID = -1;
                Contexto ctx = null;
                while (rs.next()) {
                    int empresa = rs.getInt(1);
                    int idCtx = rs.getInt(2);
                    int uRol = rs.getInt(3);
                    int fID = rs.getInt(4);
                    if (oldCtx == idCtx && fID == oldFID && uRol == oldURol)continue;

                    if (oldCtx != idCtx) {
                        ctx = new Contexto(empresa == 0, idCtx, rs.getInt(5),
                                           rs.getInt(6), rs.getInt(7),
                                           rs.getInt(8),
                                           rs.getInt(9), rs.getInt(10),
                                           rs.getInt(11), rs.getInt(12),
                                           (rs.getString(13).equals("CHILD") ? true : false),
                                           rs.getInt(14));
                        mm.addContext(ctx);
                    }

                    if (oldCtx != idCtx ||
                        (uRol != oldURol &&
                         (uRol != 0 || idCtx == helperConstant.CTX_TASK))) {
                        String prop = rs.getString(15);
                        int idDom = rs.getInt(16);
                        String detail = rs.getString(17);
                        Element eDet = rs.wasNull() ? null :
                                       jdomParser.readXML(detail).
                                       getRootElement();
                        ctx.addAcceso(uRol, prop, idDom, eDet);

                        java.sql.Connection con2 = null;
                        sql =
                                "SELECT  OID_REPORT FROM S_CTX_REPORTS WITH(NOLOCK) " +
                                "WHERE EMPRESA=" + business + " AND ID_CTX=" +
                                idCtx + " AND ROL=" + uRol;

                        Statement st2 = null;
                        ResultSet rs2 =null;
                        boolean rs2Closed=false;
                        try{
                            con2= pool.getSecondProcessConn();
                            st2 = con2.createStatement();
                            rs2= st2.executeQuery(sql);
                            while (rs2.next()) {
                                String oidRpt = rs2.getString(1);
                                ctx.addReport(uRol, oidRpt);
                            }
                            //////////////
                            rs2.close();
                            rs2Closed=true;
                            sql =  "SELECT AREAF FROM AREASF_DE_CTX WITH(NOLOCK) WHERE ID_CTX=" +
                                    ctx.id;

                            rs2 = st2.executeQuery(sql);
                            while (rs2.next())
                                ctx.addAreaFuncional(rs2.getString(1));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            if ( rs2 != null && !rs2Closed ) rs2.close();
                            if ( st2 != null ) st2.close();
                            if( con2!=null ) con2.close();
                        }
                        ////////////////////
                    }

                    if (fID != oldFID) {
                        boolean option = rs.getBoolean(18);
                        String oidFilter = rs.getString(19);
                        if (!rs.wasNull()) {
                            int ctxRef = rs.getInt(20);
                            int refSource = rs.getInt(21);
                            String detail = rs.getString(22);
                            Element eDet = rs.wasNull() ? null :
                                           jdomParser.readXML(detail).
                                           getRootElement();
                            ctx.addFilterFix(option, oidFilter, ctxRef,
                                             refSource, eDet, rs.getInt(23));
                        }
                    }
                    oldURol = uRol;
                    oldFID = fID;
                    oldCtx = idCtx;
                }

                sql = "SELECT EMPRESA,ID_CTX,FIXID,USERROL,ID_PROCESS,ID_TASK,ID_TRAN,ID_ACTION,TO_ROOT,TO_CURRENT,ID_REL," +
                      "ID_ROL_CTX,ID_ROL_CURRENT," +
                      "ID_FILTER,N_MIN,N_MAX,ROL_SUP,IMG.ID,F_PERSISTENCE,ACCESO,ID_DOMINIO,FIX_OPTION,FILTER_NOD_FIX,CTX_FIX,REF_FIX," +
                      "FIX_DETAIL,INCRUSTAR FROM " +
                      " V_META_FILTER_DOM VM WITH(NOLOCK) LEFT JOIN " +
                      "dyna" + business +
                      ".dbo.O_IMAGENES IMG WITH(NOLOCK) ON VM.TO_CURRENT=IMG.ID_TO " +
                      " WHERE EMPRESA IN(0," + business + ")" +
                      " ORDER BY ID_CTX,USERROL,FIXID";

                rs.close();
                rsClosed=true;
                rs = st.executeQuery(sql);
                rsClosed=false;
                oldCtx = -1;
                oldFID = -1;
                oldURol = -1;
                ctx = null;
                while (rs.next()) {
                    int empresa = rs.getInt(1);
                    int idCtx = rs.getInt(2);
                    int fID = rs.getInt(3);
                    int uRol = rs.getInt(4);
                    if (oldCtx == idCtx && fID == oldFID && uRol == oldURol)continue;

                    if (oldCtx != idCtx) {
                        ctx = new Contexto(empresa == 0, idCtx, rs.getInt(5),
                                           rs.getInt(6), rs.getInt(7),
                                           rs.getInt(8), rs.getInt(9),
                                           rs.getInt(10), rs.getInt(11),
                                           rs.getInt(12), rs.getInt(13),
                                           rs.getInt(14), rs.getInt(15),
                                           rs.getInt(16),
                                           (rs.getString(17).equals("CHILD") ? true : false),
                                           rs.getInt(18),
                                           rs.getInt(19) == 1);
                        mm.addContext(ctx);

                        sql =
                                "SELECT AREAF FROM AREASF_DE_TRAN WITH(NOLOCK) WHERE ID_TRAN=" +
                                ctx.tran;

                        Statement st2 = null;
                        ResultSet rs2 = null;
                        java.sql.Connection con2=null;
                        try{
                            con2 = pool.getSecondProcessConn();
                            st2 = con2.createStatement();
                            rs2 = st2.executeQuery(sql);
                            while (rs2.next())
                                ctx.addAreaFuncional(rs2.getString(1));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            if (rs2 != null) rs2.close();
                            if (st2 != null) st2.close();
                            if( con2!=null ) con2.close();
                        }
                    }

                    if (oldCtx != idCtx || (uRol != oldURol && uRol != 0)) {
                        String acceso = rs.getString(20);
                        ctx.addAcceso(uRol, acceso, rs.getInt(21), null);
                        System.out.println("ADDACCESS:" + idCtx + "," + uRol +
                                           "," + acceso);
                    }

                    if (oldFID != fID) {
                        boolean option = rs.getBoolean(22);
                        String oidFilter = rs.getString(23);
                        if (!rs.wasNull()) {
                            int ctxRef = rs.getInt(24);
                            int refSource = rs.getInt(25);
                            String detail = rs.getString(26);
                            System.out.println(oidFilter + "," + detail);
                            Element eDet = rs.wasNull() ? null :
                                           jdomParser.readXML(detail).
                                           getRootElement();
                            ctx.addFilterFix(option, oidFilter, ctxRef,
                                             refSource, eDet, rs.getInt(27));
                        }
                    }
                    oldURol = uRol;
                    oldFID = fID;
                    oldCtx = idCtx;
                }
            }catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (rs != null && !rsClosed ) rs.close();
                if (st != null) st.close();
            }
	}

	public static void buildCtxsCondicionados( int business, metaData mm ){
		Iterator itr= mm.getContextosSet().iterator();
		while( itr.hasNext() ){
			Integer idCtx= (Integer)itr.next();
			Contexto ctx= mm.getContext( idCtx );
			Iterator iR=ctx.getUserRolSet().iterator();
			while(iR.hasNext()){
				Integer ur=(Integer)iR.next();
				if( ctx.getDom( ur )==null ) continue;
				mm.addContextRefCondicionados( ctx, ur.intValue() );
			}
		}
	}

	public void buildEnums( int business, metaData mm ) throws NamingException,SQLException{

		String sql= 	"SELECT DISTINCT TA_POS,ID_ENUM, ENUM.NAME FROM "+
				" dyna" + business + ".dbo.T_REG_TA_POS POS WITH(NOLOCK) INNER JOIN "+
				" dyna" + business + ".dbo.T_REG_ATRIBUTOS TRA WITH(NOLOCK) ON POS.ID_TA=TRA.ID_TA INNER JOIN "+
				" dyna" + business + ".dbo.T_REG_SINTAX STX WITH(NOLOCK) ON TRA.ID_STX=STX.ID_STX INNER JOIN " +
				" dyna" + business + ".dbo.T_REG_ENUM ENUM WITH(NOLOCK) ON STX.ID_STX=ENUM.ID_STX" +
				" ORDER BY TA_POS, ID_ENUM ASC";
                Statement st = null;
                ResultSet rs = null;
                try{
                    st = m_pool.getBusinessConn(business).createStatement();
                    rs = st.executeQuery(sql);
                    int oldtapos = 0;
                    ArrayList values = null;
                    while (rs.next()) {
                        int tapos = rs.getInt(1);
                        int id = rs.getInt(2);
                        String name = rs.getString(3);
                        mm.addEnumMap(tapos, id, name);
                    }
                } finally {
                    if (rs != null) rs.close();
                    if (st != null) st.close();
                }
	}

	public void buildTMs( int business, metaData mm ) throws NamingException,SQLException{
		String sql= "SELECT DISTINCT TA_POS, ID_TM FROM "+
				" dyna" + business + ".dbo.T_REG_TA_POS POS WITH(NOLOCK) INNER JOIN "+
				" dyna" + business + ".dbo.T_REG_ATRIBUTOS TRA WITH(NOLOCK) ON POS.ID_TA=TRA.ID_TA INNER JOIN "+
				" dyna" + business + ".dbo.T_REG_SINTAX STX WITH(NOLOCK) ON TRA.ID_STX=STX.ID_STX";
                Statement st = null;
                ResultSet rs = null;
                try{
                    st = m_pool.getBusinessConn(business).createStatement();
                    rs = st.executeQuery(sql);
                    while (rs.next()) {
                        int tapos = rs.getInt(1);
                        int tm = rs.getInt(2);
                        mm.addTMmap(tapos, tm);
                    }
                    mm.addTMmap(helperConstant.TAPOS_METATIPO,
                                helperConstant.TM_ENTERO);
                } finally {
                    if (rs != null) rs.close();
                    if (st != null) st.close();
                }
	}

	public  void buildMetaTOs( Connection con, int business, metaData mm ) throws SQLException, Exception{
		String sql= "SELECT ID_TO FROM dyna" + business + ".dbo.T_REG_TO WITH(NOLOCK)";
                Statement st = null;
                ResultSet rs = null;
                try{
                    st = con.createStatement();
                    rs = st.executeQuery(sql);
                    ArrayList tipos = new ArrayList();
                    while (rs.next())
                        tipos.add(new Integer(rs.getInt(1)));

                    for (int i = 0; i < tipos.size(); i++) {
                        Integer to = (Integer) tipos.get(i);
                        Element eTO = m_IS.Meta_GetTO(to.intValue(), business);
                        mm.addMetaTO(to.intValue(), eTO);
                    }
                }
                finally {
                    if (rs != null) rs.close();
                    if (st != null) st.close();
                }

	}


	public void buildTOLabels( int business, metaData mm ) throws NamingException,SQLException{
		String sql= "SELECT ID_TO, name FROM dyna" + business + ".dbo.T_REG_TO WITH(NOLOCK)";
                Statement st = null;
                ResultSet rs = null;
                try{
                    st = m_pool.getBusinessConn(business).createStatement();
                    rs = st.executeQuery(sql);
                    while (rs.next()) {
                        mm.addTOLabel(rs.getInt(1), rs.getString(2));
                    }
                }finally {
                    if (rs != null) rs.close();
                    if (st != null) st.close();
                }
	}

	public void buildGroupLabels( int business, metaData mm ) throws NamingException,SQLException{
		String sql= "SELECT ID_Grupo, name FROM dyna" + business + ".dbo.T_REG_GRUPOS WITH(NOLOCK)";
                Statement st = null;
                ResultSet rs = null;
                try{
                    st = m_pool.getBusinessConn(business).createStatement();
                    rs = st.executeQuery(sql);
                    while (rs.next()) {
                        mm.addGroupLabel(rs.getInt(1), rs.getString(2));
                    }
                }finally {
                    if (rs != null) rs.close();
                    if (st != null) st.close();
                }
	}

	public void buildCategoriasRel( int business, metaData mm ) throws NamingException,SQLException{
		String sql= "SELECT ID_RELACION, ID_Cat FROM dyna" + business + ".dbo.T_REG_RELACIONES WITH(NOLOCK)";
                Statement st = null;
                ResultSet rs = null;
                try{
                    st = m_pool.getBusinessConn(business).createStatement();
                    rs = st.executeQuery(sql);
                    while (rs.next()) {
                        mm.addCategoriaRel(rs.getInt(1), rs.getInt(2));
                    }
                } finally {
                    if (rs != null) rs.close();
                    if (st != null) st.close();
                }
	}

	public void buildATLabels( int business, metaData mm ) throws NamingException,SQLException{
		String sql= "SELECT POS.TA_POS, TRA.name FROM dyna" + business + ".dbo.T_REG_ATRIBUTOS TRA WITH(NOLOCK) INNER JOIN "+
				" dyna" + business + ".dbo.T_REG_TA_POS POS WITH(NOLOCK) ON TRA.ID_TA=POS.ID_TA ";
                Statement st = null;
                ResultSet rs = null;
                try{
                    st = m_pool.getBusinessConn(business).createStatement();
                    rs = st.executeQuery(sql);
                    while (rs.next()) {
                        mm.addATLabel(rs.getInt(1), rs.getString(2));
                    }
                }finally {
                    if (rs != null) rs.close();
                    if (st != null) st.close();
                }
	}

	public String getEnumLabel( int business, Integer tapos, int value){
		metaData mm=(metaData)m_metaBusiness.get( business -1 );
		return mm.getEnumLabel( tapos, value);
	}

	public int getID_TM( int business, Integer tapos ){
		metaData mm=(metaData)m_metaBusiness.get( business -1 );
		return mm.getID_TM( tapos );
	}

	public metaData getMetaData( int business ){
		return (metaData)m_metaBusiness.get(business-1);
	}

	public static void buildHerencias( java.sql.Connection proCon,int business, metaData mm)
		throws SQLException{

		String sql= "SELECT ID_TO, ID_TO_H FROM dyna" + business + ".dbo.T_INDEX_TO WITH(NOLOCK)";
                Statement st = null;
                ResultSet rs = null;
                try{
                    st = proCon.createStatement();
                    rs = st.executeQuery(sql);
                    while (rs.next()) {
                        int to = rs.getInt(1);
                        int toSup = rs.getInt(2);
                        mm.addSuperiorMap(to, toSup);
                    }
                }finally {
                    if (rs != null) rs.close();
                    if (st != null) st.close();
                }
	}

	public void buildDirectSpecialization( int business, metaData mm) throws NamingException,SQLException{
		String sql= "SELECT ID_TO, ID_TO_Padre FROM dyna" + business + ".dbo.T_Herencias WITH(NOLOCK)";
                Statement st = null;
                ResultSet rs = null;
                try{
                    st = m_pool.getBusinessConn(business).createStatement();
                    rs = st.executeQuery(sql);
                    while (rs.next()) {
                        int to = rs.getInt(1);
                        int toSup = rs.getInt(2);
                        mm.addDirectSpecializedMap(toSup, to);
                    }
                }finally {
                    if (rs != null) rs.close();
                    if (st != null) st.close();
                }
	}

	public static void buildFilters( java.sql.Connection proCon, int business, metaData mm )
		 throws SQLException, JDOMException{

		String sql= "SELECT ID_FILTER, FILTER FROM S_FILTER WITH(NOLOCK) WHERE EMPRESA IN(0," + business+")";
                Statement st = null;
                ResultSet rs = null;
                try{
                    st = proCon.createStatement();
                    rs = st.executeQuery(sql);
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        String filter = rs.getString(2);
                        Element eFilter = jdomParser.readXML(filter).
                                          getRootElement();
                        mm.addFilter(eFilter, id);
                    }
                }finally {
                    if (rs != null) rs.close();
                    if (st != null) st.close();
                }
	}

	public static void buildDominios( java.sql.Connection proCon,int business, metaData mm )
		throws SQLException, JDOMException{

		String sql= "SELECT ID_DOMINIO, DOMINIO FROM S_DOMINIOS WITH(NOLOCK) WHERE EMPRESA=" + business;
                Statement st = null;
                ResultSet rs = null;
                boolean rsClosed=false;
                try{
                    st = proCon.createStatement();
                    rs = st.executeQuery(sql);
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        String dom = rs.getString(2);
                        Element eDom = jdomParser.readXML(dom).getRootElement();
                        mm.addDominio(eDom, id);
                    }
                    sql =
                            "SELECT ID_DOM, ID_SUP FROM DOM_SPECIALIZATION WITH(NOLOCK) WHERE EMPRESA=" +
                            business;
                    rs.close();
                    rsClosed=true;
                    rs = st.executeQuery(sql);
                    rsClosed=false;
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        int idSup = rs.getInt(2);
                        mm.addSuperiorDomMap(id, idSup);
                    }
                    mm.buildTreeDomMap();
                }catch (SQLException e) {
                    throw e;
                } finally {
                    if (rs != null && !rsClosed) rs.close();
                    if (st != null) st.close();
                }
	}

        public String toString( int business, String user ) throws JDOMException,SQLException{
            return jdomParser.returnXML( toElement( business, user ) );
        }

	public Element toElement( int business, String user ) throws JDOMException,SQLException{
            Statement st = null;
            ResultSet rs = null;
            boolean rsClosed=false, stClosed=false;
            try{
		Element root= new Element("METADATA");
		root.setAttribute("BUSINESS", String.valueOf( business ));

		metaData mm=(metaData)m_metaBusiness.get( business -1 );
		ArrayList userRolList= mm.getUserRol( user );
		Element eList= new Element("USER_ROLES");
		eList.setAttribute("USER",user);
		root.addContent(eList);
		for( int u=0; u < userRolList.size(); u++){
			Integer uRol=(Integer)userRolList.get(u);
			Element item=new Element("ITEM");
			item.setAttribute("ROL",uRol.toString());
			eList.addContent( item );
		}

		Element metatos= new Element("METATOS");
		root.addContent( metatos );
		Iterator iTO= mm.m_metaTOs.keySet().iterator();
		while( iTO.hasNext()){
			Integer to= (Integer)iTO.next();
			Element eTO= mm.getMetaTO( to );
			metatos.addContent( (Element)eTO.clone() );
		}
		///////
		Element areasF= new Element("AREAS_FUNCIONALES");
		root.addContent( areasF );

		String sql= "SELECT OID,LABEL FROM AREAS_FUNC WITH(NOLOCK)";
		st= m_pool.getProcessConn().createStatement();
		rs= st.executeQuery( sql );
		while( rs.next() ){
			Element area= new Element("ITEM");
			areasF.addContent( area );
			area.setAttribute("ID", rs.getString(1));
			area.setAttribute("LABEL", rs.getString(2));
		}
                rs.close();
                rsClosed=true;
                st.close();
                stClosed=true;
		////////
		Element supers= new Element("HERENCIAS");
		root.addContent( supers );
		iTO= mm.getHerenciasSet().iterator();
		while( iTO.hasNext()){
			Integer to= (Integer)iTO.next();
			Element eTO= new Element("TYPE");

			Iterator iSups= mm.getSuperiorsSet( to ).iterator();
			String cadenaSups="";
			int countSups=0;
			while( iSups.hasNext() ){
				countSups++;
				Integer sup= (Integer)iSups.next();
				if( cadenaSups.length()>0 )
					cadenaSups+=";";
				cadenaSups+= sup;
			}
			if( countSups>0 ) {
				supers.addContent( eTO );
				eTO.setAttribute( "ID_TO", to.toString() );
				eTO.setText( cadenaSups );
			}
		}
		Element special= new Element("SPECIALIZATION");
		root.addContent( special );

		iTO= mm.getSpecializedSet().iterator();
		while( iTO.hasNext()){
			Integer to= (Integer)iTO.next();
			Element eTO= new Element("TYPE");

			Iterator iSpecs= mm.getSpecializedSet( to ).iterator();
			String cadenaSpecs="";
			int countSpecs=0;
			while( iSpecs.hasNext() ){
				countSpecs++;
				Integer spec= (Integer)iSpecs.next();
				if( cadenaSpecs.length()>0 )
					cadenaSpecs+=";";
				cadenaSpecs+= spec;
			}
			if( countSpecs>0 ) {
				special.addContent( eTO );
				eTO.setAttribute( "ID_TO", to.toString() );
				eTO.setText( cadenaSpecs );
			}
		}

		Element filters= new Element("FILTERS");
		root.addContent( filters );

		Iterator iF= mm.getFiltersSet().iterator();
		while( iF.hasNext()){
			Integer id= (Integer)iF.next();
			Element filter= mm.getFilter(null,id);
			filter.setAttribute("ID", id.toString() );
			filters.addContent( (Element)filter.clone() );
		}

		Element reports= new Element("REPORTS");
		root.addContent( reports );

		Iterator iRp= mm.m_reports.keySet().iterator();
		while( iRp.hasNext()){
			String oid= (String)iRp.next();
			reportType rpt= mm.getDinReport(oid);
			Element item= new Element("ITEM");
			reports.addContent( item );
			item.setAttribute("OID", rpt.oid );
			item.setAttribute("NAME", rpt.name );
			item.setAttribute("FILTER", String.valueOf(rpt.filter) );
			item.setAttribute("PLANTILLA", rpt.plantilla );
		}

		Element dominios= new Element("DOMINIOS");
		root.addContent( dominios );

		Iterator iD= mm.getDominiosSet().iterator();
		while( iD.hasNext()){
			Integer id= (Integer)iD.next();
			Element dom= mm.getDominio(id);
			dom.setAttribute("ID", id.toString() );
			dominios.addContent( (Element)dom.clone() );
		}

		Element ctxs= new Element("CONTEXTOS");
		root.addContent( ctxs );

		Iterator iC= mm.getContextosSet().iterator();
		while( iC.hasNext()){
                    Integer id= (Integer)iC.next();
                    Contexto ctx= mm.getContext(id);
                    Element eCtx= ctx.toElement(userRolList);
                    if( eCtx!=null )
                        ctxs.addContent(eCtx);
		}

		Element ats= new Element("ATRIBUTOS");
		root.addContent( ats );

		Iterator iMM= mm.getMiembrosSet().iterator();
		while( iMM.hasNext()){
			Element atDef= new Element("ITEM");
			ats.addContent( atDef );

			Integer tapos= (Integer)iMM.next();
			int tm= mm.getID_TM(tapos);
			atDef.setAttribute("ID_TM", String.valueOf(tm) );
			atDef.setAttribute("TA_POS", tapos.toString() );
		}

		Element toLab= new Element("TO_LABELS");
		root.addContent( toLab );

		Iterator iTOL= mm.getTOLabelsSet().iterator();
		while( iTOL.hasNext()){
			Element item= new Element("ITEM");
			toLab.addContent( item );

			Integer to= (Integer)iTOL.next();
			String label= mm.getTOLabel(to);
			item.setAttribute("ID_TO", to.toString() );
			item.setAttribute("LABEL", label );
		}

		Element formO= new Element("FORM_ORDER");
		root.addContent( formO );
		sql= "SELECT ID_CONTAINER,ID_ELEMENT,IS_TO,TIPO_ELEM,ORDEN FROM FORM_ORDER WITH(NOLOCK)";

                st= m_pool.getBusinessConn(business).createStatement();
		rs= st.executeQuery( sql );
		while( rs.next()){
			Element item= new Element("ITEM");
			formO.addContent( item );
			item.setAttribute("IDC", String.valueOf(rs.getInt(1)));
			item.setAttribute("IDE", String.valueOf(rs.getInt(2)));
			item.setAttribute("ISTO", rs.getInt(3)==0 ? "FALSE":"TRUE");
			item.setAttribute("TIPO", String.valueOf(rs.getInt(4)));
			item.setAttribute("ORDER", String.valueOf(rs.getInt(5)));
		}
                rs.close();
                rsClosed=true;

		Element proDef= new Element("PROCESS");
		root.addContent( proDef );

		Iterator iPro= mm.m_process.keySet().iterator();
		while( iPro.hasNext()){
			Element item= new Element("ITEM");
			proDef.addContent( item );

			Integer id= (Integer)iPro.next();
			processType pt= mm.getProcess(id);
			item.setAttribute("ID", id.toString() );
			item.setAttribute("LABEL", pt.getLabel() );
			item.setAttribute("START_STATE", String.valueOf(pt.getStartState()) );
		}

		Element tasks= new Element("TASKS");
		root.addContent( tasks );

		Iterator iTask= mm.m_tasks.keySet().iterator();
		while( iTask.hasNext()){
			Element item= new Element("ITEM");
			tasks.addContent( item );
			Integer id= (Integer)iTask.next();
			taskType tt= mm.getTask( id );
			item.setAttribute("ID", id.toString() );
			item.setAttribute("LABEL", tt.label );
			item.setAttribute("START", String.valueOf(tt.stateStart));
			item.setAttribute("AT_ST", String.valueOf(tt.taposAtState));
                        Iterator iSt= tt.getStates();
                        while( iSt.hasNext() ){
                            taskState ts=(taskState)iSt.next();
                            Element eSt= new Element("STATE");
                            item.addContent(eSt);
                            eSt.setAttribute("ID",String.valueOf(ts.value));
                            eSt.setAttribute("LOCK",(ts.lock ? "TRUE":"FALSE"));
                        }
		}

		buildLabels( root, mm.m_AT_labels, "AT_LABELS" );
		buildLabels( root, mm.m_GRUPOS_labels, "GROUP_LABELS" );

		Element taskFilters= mm.getTaskContextFixTree();
		root.addContent( taskFilters );

		Element trans= new Element("TRANSITIONS");
		root.addContent( trans );

		Iterator iTr= mm.getTransKeySet().iterator();
		while( iTr.hasNext()){
			Element item= new Element("ITEM");
			trans.addContent( item );
			Integer idtt= (Integer)iTr.next();
			taskTransition tt= mm.getTrans( idtt );

			item.setAttribute("STATE_INI", String.valueOf(tt.stateIni) );
			item.setAttribute("STATE_END", String.valueOf(tt.stateEnd) );
			item.setAttribute("ID", String.valueOf(tt.id) );
			item.setAttribute("TASK_TYPE", String.valueOf(tt.taskType) );
			item.setAttribute("LABEL", tt.label );
			if( tt.userFlowStartAction!=0 ){
				item.setAttribute("ACT_MAIN", String.valueOf(tt.userFlowStartAction) );
				item.setAttribute("CTX_MAIN", String.valueOf(tt.userFlowStartContext) );
			}
                        item.setAttribute("OW_POLICY", String.valueOf(tt.owningPolicy) );
		}


		Element actions= new Element("ACTIONS");
		root.addContent( actions );

		Iterator iTact= mm.getActionIterator();
		while( iTact.hasNext()){
			Element item= new Element("ITEM");
			actions.addContent( item );
			Integer idAct= (Integer)iTact.next();
			action act= mm.getActionData( idAct );

			item.setAttribute("ID", String.valueOf(act.id) );
			item.setAttribute("TYPE", String.valueOf(act.type) );
			item.setAttribute("TRAN", String.valueOf(act.transition) );
                        item.setAttribute("OP", String.valueOf(act.getOperation()) );
		}

		Element index= new Element("INDICES");
		root.addContent( index );

		Iterator iTx= mm.m_indices.keySet().iterator();
		while( iTx.hasNext()){
			Element item= new Element("ITEM");
			index.addContent( item );
			Integer idDom= (Integer)iTx.next();
			atReference atr=(atReference)mm.m_indices.get( idDom );

			item.setAttribute("ID_FILTER", String.valueOf(atr.idFilter) );
			item.setAttribute("NODE_FILTER", String.valueOf(atr.idNodeRef) );
			if( atr.idFilterCascading!=0 )
				item.setAttribute("CASCADING", String.valueOf(atr.idFilterCascading) );
			item.setAttribute("SUBDOM_REF", String.valueOf(atr.idSubdomRef) );
			item.setAttribute("ID_DOM", idDom.toString() );
			item.setAttribute("TA_POS", String.valueOf( atr.tapos ) );
			item.setAttribute("TAPOS_POINTER", String.valueOf( atr.pointerTapos ) );
		}

		Element enumLab= new Element("ENUM_LABELS");
		root.addContent( enumLab );

		Iterator iENUM= mm.getMapEnumIterator();
		while( iENUM.hasNext()){
			Element item= new Element("AT");
			enumLab.addContent( item );
			Integer tapos= (Integer)iENUM.next();
			item.setAttribute("TA_POS", tapos.toString());

			Iterator iVal= mm.getEnumSet(tapos);
			while( iVal.hasNext() ){

				Element eVal= new Element("VAL");
				item.addContent( eVal );

				Integer val=(Integer)iVal.next();
				String label= mm.getEnumLabel(tapos, val);
				eVal.setAttribute("ID", val.toString() );
				eVal.setAttribute("LABEL", label );
			}
		}
		return root;
		}catch(Exception e){
			e.printStackTrace();
			if( e instanceof JDOMException )
				throw (JDOMException)e;
			else 	return null;
                }finally {
                    System.out.println("DICCION FIN toELEment " +rsClosed);
                    if (rs != null && !rsClosed && !stClosed ) rs.close();
                    if (st != null && !stClosed ) st.close();
                }
	}

	public void buildLabels( Element root, HashMap lista, String label){
		Element sub= new Element(label);
		root.addContent( sub );

		Iterator iSub= lista.keySet().iterator();
		while( iSub.hasNext()){
			Element item= new Element("ITEM");
			sub.addContent( item );

			Integer id= (Integer)iSub.next();
			String iLabel= (String)lista.get( id );
			item.setAttribute("ID", id.toString() );
			item.setAttribute("LABEL", iLabel );
		}
	}
}


*/