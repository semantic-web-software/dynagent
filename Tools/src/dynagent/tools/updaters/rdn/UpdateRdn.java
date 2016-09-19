package dynagent.tools.updaters.rdn;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.O_Reg_Instancias_Index;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.database.dao.O_Reg_Instancias_IndexDAO;
import dynagent.server.ejb.FactoryConnectionDB;

public class UpdateRdn {

	public void startUpdate(FactoryConnectionDB fcdb) {
		//DAOManager.getInstance().setCommit(true);
		try {
			DAOManager.getInstance().setCommit(false); //antes de abrir DAO

			//DAOManager.getInstance().setCommit(false);
			O_Datos_AttribDAO odatDAO = new O_Datos_AttribDAO();
			O_Reg_Instancias_IndexDAO oregDAO = new O_Reg_Instancias_IndexDAO();
			odatDAO.open();
			//odatDAO.commit();

			try {
				//itera y coge valnum y valueCls
				//si valnum no es nulo
				//busca en oRegInstancias el rdn de ese ido y de esa clase
				ArrayList<Integer> aValNum = new ArrayList<Integer>();
				LinkedList<Object> lodt = odatDAO.getAllCond("VAL_NUM is not null");
				Iterator it = lodt.iterator();
				while (it.hasNext()) {
					O_Datos_Attrib odat = (O_Datos_Attrib)it.next();
					Integer valNum = odat.getVALNUM();
					if (!aValNum.contains(valNum)) {
						aValNum.add(valNum);
						String rdn = null;
						LinkedList<Object> lreg = oregDAO.getAllCond("ID_O=" + valNum);
						Iterator it2 = lreg.iterator();
						if (it2.hasNext()) {
							O_Reg_Instancias_Index oreg = (O_Reg_Instancias_Index)it2.next();
							rdn = oreg.getRdn();
							odatDAO.update("VAL_TEXTO='" + rdn.replaceAll("'", "''") + "'", "VAL_NUM=" + valNum);
						}
					}
				}
				odatDAO.commit();
				//odatDAO.setCommit(true);
				odatDAO.close();
				System.err.println("---> Fin de la actualización");
			} catch (Exception e) {
				System.err.println("ERROR: Actualización con errores. No realizada.");
				e.printStackTrace();
				odatDAO.rollback();
				System.out.println("hace rollback");
				odatDAO.close();
				fcdb.removeConnections();
			}
		} catch (Exception e) {
			System.err.println("ERROR: Actualización con errores. No realizada.");
			e.printStackTrace();
			try {
				fcdb.removeConnections();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
}
