package dynagent.tools.updaters.tarifas;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.O_Reg_Instancias_Index;
import dynagent.common.exceptions.DataErrorException;
import dynagent.server.database.Individual;
import dynagent.server.database.IndividualCreator;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.database.dao.O_Reg_Instancias_IndexDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;

public class UpdateTarifas {
	private FactoryConnectionDB fcdb;
	private GenerateSQL gSQL;
	
	public UpdateTarifas(FactoryConnectionDB fcdb) {
		this.fcdb = fcdb;
		this.gSQL = new GenerateSQL(fcdb.getGestorDB());
	}
	
	public void startUpdate() {
		//DAOManager.getInstance().setCommit(true);
		try {
			System.err.println("---> Actualizando tarifas");
			DAOManager.getInstance().setCommit(false); //antes de abrir DAO
			O_Datos_AttribDAO odatDAO = new O_Datos_AttribDAO();
			O_Reg_Instancias_IndexDAO oreg = new O_Reg_Instancias_IndexDAO();
			//O_Datos_Attrib_MemoDAO odatMemoDAO = new O_Datos_Attrib_MemoDAO();
			TClaseDAO tdao = new TClaseDAO();
			PropertiesDAO propDAO = new PropertiesDAO();
			odatDAO.open();
			//odatDAO.commit();
			
			String sql = "SELECT Codigo, Descripcion, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T15, " +
					"Cubicaje, UnidadesXbulto, Bultosxpalet, [Punto verde] FROM Tarifas";
			ConnectionDB con = fcdb.createConnection(true);
			Statement st = null;
			ResultSet rs = null;
			System.out.println("sql->"+sql);
			try {
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				while (rs.next()) {
					String codigo = rs.getString(1);
					String descripcion = rs.getString(2);
					Double cubicaje = null;
					String cubicajeStr = rs.getString(14);
					if (!rs.wasNull())
						cubicaje = Double.parseDouble(cubicajeStr.replaceAll(",", "."));
					Double puntoVerde = null;
					String puntoVerdeStr = rs.getString(17);
					if (!rs.wasNull())
						puntoVerde = Double.parseDouble(puntoVerdeStr.replaceAll(",", "."));
					
					Double unidadesXBulto = null;
					String unidadesXBultoStr = rs.getString(15);
					if (!rs.wasNull())
						unidadesXBulto = Double.parseDouble(unidadesXBultoStr.replaceAll(",", "."));
					Double bultosXPalet = null;
					String bultosXPaletStr = rs.getString(16);
					if (!rs.wasNull())
						bultosXPalet = Double.parseDouble(bultosXPaletStr.replaceAll(",", "."));
					
					double[] tarifas = new double[16];
					String t0 = rs.getString(3);
					if (!rs.wasNull())
						tarifas[0] = Double.parseDouble(t0.replaceAll(",", "."));
					String t1 = rs.getString(4);
					if (!rs.wasNull())
						tarifas[1] = Double.parseDouble(t1.replaceAll(",", "."));
					String t2 = rs.getString(5);
					if (!rs.wasNull())
						tarifas[2] = Double.parseDouble(t2.replaceAll(",", "."));
					String t3 = rs.getString(6);
					if (!rs.wasNull())
						tarifas[3] = Double.parseDouble(t3.replaceAll(",", "."));
					String t4 = rs.getString(7);
					if (!rs.wasNull())
						tarifas[4] = Double.parseDouble(t4.replaceAll(",", "."));
					String t5 = rs.getString(8);
					if (!rs.wasNull())
						tarifas[5] = Double.parseDouble(t5.replaceAll(",", "."));
					String t6 = rs.getString(9);
					if (!rs.wasNull())
						tarifas[6] = Double.parseDouble(t6.replaceAll(",", "."));
					String t7 = rs.getString(10);
					if (!rs.wasNull())
						tarifas[7] = Double.parseDouble(t7.replaceAll(",", "."));
					String t8 = rs.getString(11);
					if (!rs.wasNull())
						tarifas[8] = Double.parseDouble(t8.replaceAll(",", "."));
					String t9 = rs.getString(12);
					if (!rs.wasNull())
						tarifas[9] = Double.parseDouble(t9.replaceAll(",", "."));
					String t15 = rs.getString(13);
					if (!rs.wasNull())
						tarifas[15] = Double.parseDouble(t15.replaceAll(",", "."));
					
					trataGenero(odatDAO, tdao, propDAO, oreg, codigo, descripcion, cubicaje, puntoVerde, unidadesXBulto, bultosXPalet, tarifas);
				}
				//TODO ir guardando los nuevos rdn de las tarifas y borrar los que no esten en ese array y no sean apuntados??
				  //mostrar en pantalla los que son apuntados
				
				odatDAO.commit();
				//odatDAO.setCommit(true);
				odatDAO.close();
			} catch (Exception e) {
				System.err.println("ERROR: Actualización de tarifas con errores. No realizada.");
				e.printStackTrace();
				odatDAO.rollback();
				System.out.println("hace rollback");
				odatDAO.close();
				fcdb.removeConnections();
			} finally {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con != null) {
					fcdb.close(con);
				}
			}
		} catch (Exception e) {
			System.err.println("ERROR: Actualización de tarifas con errores. No realizada.");
			e.printStackTrace();
			try {
				fcdb.removeConnections();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void trataGenero(O_Datos_AttribDAO odatDAO, TClaseDAO tdao, PropertiesDAO propDAO, O_Reg_Instancias_IndexDAO oreg, 
			String codigo, String descripcion, Double cubicaje, Double puntoVerde, 
			Double unidadesXBulto, Double bultosXPalet, double[] tarifas) throws SQLException, DataErrorException, NamingException {
		//buscar si en base de datos esta un genero con ese codigo
		Integer idtoGenero = tdao.getTClaseByName("GÉNERO").getIDTO();
		
		LinkedList<Object> lodt = oreg.getAllCond("ID_TO=" + idtoGenero + " AND RDN='" + codigo + "'");
		Iterator it = lodt.iterator();
		if (it.hasNext()) {
			O_Reg_Instancias_Index odt = (O_Reg_Instancias_Index)it.next();
			int ido = odt.getId_o();
			System.err.println("---> Actualizando género con ido " + ido);
			//actualizar valores
			LinkedList<Object> lodt2 = odatDAO.getByID(String.valueOf(ido));
			Iterator it2 = lodt2.iterator();
			
			int propDescripcion = propDAO.getIdPropByName("descripción");
			int propCubica = propDAO.getIdPropByName("cubica");
			int propPuntoVerde = propDAO.getIdPropByName("punto_verde");
			int propTarifasVenta = propDAO.getIdPropByName("tarifas_venta");
			int propTipoEmbalado = propDAO.getIdPropByName("tipo_embalado");
			
			boolean encontradoDescripcion = false;
			boolean encontradoCubicaje = false;
			boolean encontradoPuntoVerde = false;
			boolean encontradoEmbalado = false;
			int[] tarifasTratadas = new int[16];
			while (it2.hasNext()) {
				//ver que property es
				O_Datos_Attrib odt2 = (O_Datos_Attrib)it2.next();
				Integer prop = odt2.getPROPERTY();
				
				if (prop.equals(propDescripcion)) {
					encontradoDescripcion = true;
					if (descripcion==null)
						odatDAO.deleteCond("ID_O=" + ido + " AND PROPERTY=" + prop);
					else if (!odt2.getVALTEXTO().equals(descripcion)) {
						//updateDescripcion
						odatDAO.update("VAL_TEXTO='" + descripcion.replaceAll("'", "''") +"'", "ID_O=" + ido + " AND PROPERTY=" + prop);
					}
				} else if (prop.equals(propCubica)) {
					encontradoCubicaje = true;
					if (cubicaje==null)
						odatDAO.deleteCond("ID_O=" + ido + " AND PROPERTY=" + prop);
					else if (!odt2.getQMIN().equals(cubicaje)) {
						//updateCubicaje
						odatDAO.update("Q_MIN=" + cubicaje + ", Q_MAX=" + cubicaje, "ID_O=" + ido + " AND PROPERTY=" + prop);
					}
				} else if (prop.equals(propPuntoVerde)) {
					encontradoPuntoVerde = true;
					if (puntoVerde==null)
						odatDAO.deleteCond("ID_O=" + ido + " AND PROPERTY=" + prop);
					else if (!odt2.getQMIN().equals(puntoVerde)) {
						//updatePuntoVerde
						odatDAO.update("Q_MIN=" + puntoVerde + ", Q_MAX=" + puntoVerde, "ID_O=" + ido + " AND PROPERTY=" + prop);
					}
				} else if (prop.equals(propTarifasVenta)) {
					//updatePrecios
					int tarifa = updatePrecios(odatDAO, tdao, propDAO, odt2.getVALNUM(), odt2.getVALUECLS(), tarifas);
					tarifasTratadas[tarifa] = 1;
				} else if (prop.equals(propTipoEmbalado)) {
					encontradoEmbalado = true;
					//updateEmbalado
					updateEmbalado(odatDAO, tdao, propDAO, odt2.getVALNUM(), odt2.getVALUECLS(), unidadesXBulto, bultosXPalet);
				}
			}
			//si alguna property no se ha encontrado añadir
			if (!encontradoDescripcion && descripcion!=null) {
				O_Datos_Attrib in = new O_Datos_Attrib(idtoGenero, ido, propDescripcion, null, descripcion, Constants.IDTO_STRING, null, null, null, null);
				odatDAO.insert(in);
			}
			if (!encontradoCubicaje && cubicaje!=null) {
				O_Datos_Attrib in = new O_Datos_Attrib(idtoGenero, ido, propCubica, null, null, Constants.IDTO_DOUBLE, cubicaje, cubicaje, null, null);
				odatDAO.insert(in);
			}
			if (!encontradoPuntoVerde && puntoVerde!=null) {
				O_Datos_Attrib in = new O_Datos_Attrib(idtoGenero, ido, propPuntoVerde, null, null, Constants.IDTO_DOUBLE, puntoVerde, puntoVerde, null, null);
				odatDAO.insert(in);
			}
			for (int i=0;i<tarifasTratadas.length;i++) {
				if (i!=10 && i!=11 && i!=12 && i!=13 && i!=14) {
					if (tarifasTratadas[i]==0) {
						creaPrecio(odatDAO, tdao, propDAO, oreg, ido, idtoGenero, i, tarifas);
					}
				}
			}
			if (!encontradoEmbalado) {
				creaEmbalado(odatDAO, tdao, propDAO, oreg, ido, idtoGenero, unidadesXBulto, bultosXPalet);
			}
		} else {
			//crear todo
			System.err.println("---> Creando género");
			creaGenero(odatDAO, tdao, propDAO, oreg, idtoGenero, 
					codigo, descripcion, cubicaje, puntoVerde, unidadesXBulto, bultosXPalet, tarifas);
		}
	}
	
	private void creaGenero(O_Datos_AttribDAO odatDAO, TClaseDAO tdao, PropertiesDAO propDAO, O_Reg_Instancias_IndexDAO oreg, 
			int idtoGenero, String codigo, String descripcion, Double cubicaje, Double puntoVerde, 
			Double unidadesXBulto, Double bultosXPalet, double[] tarifas) throws SQLException, DataErrorException, NamingException {
		//insertar rdn que ya lo tenemos
		int ido = IndividualCreator.creator(fcdb, DAOManager.getInstance().isCommit(), gSQL, idtoGenero, codigo, null, null, null,false,null).getIdo();
		
		//insertar el resto de las DataProperties
		if (descripcion!=null) {
			int propDescripcion = propDAO.getIdPropByName("descripción");
			O_Datos_Attrib inDescripcion = new O_Datos_Attrib(idtoGenero, ido, propDescripcion, null, descripcion.replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, null);
			odatDAO.insert(inDescripcion);
		}
		if (cubicaje!=null) {
			int propCubica = propDAO.getIdPropByName("cubica");
			O_Datos_Attrib inCubicaje = new O_Datos_Attrib(idtoGenero, ido, propCubica, null, null, Constants.IDTO_DOUBLE, cubicaje, cubicaje, null, null);
			odatDAO.insert(inCubicaje);
		}
		if (puntoVerde!=null) {
			int propPuntoVerde = propDAO.getIdPropByName("punto_verde");
			O_Datos_Attrib inPuntoVerde = new O_Datos_Attrib(idtoGenero, ido, propPuntoVerde, null, null, Constants.IDTO_DOUBLE, puntoVerde, puntoVerde, null, null);
			odatDAO.insert(inPuntoVerde);
		}
		
		for (int i=0;i<15;i++) {
			if (i!=10 && i!=11 && i!=12 && i!=13 && i!=14)
				creaPrecio(odatDAO, tdao, propDAO, oreg, ido, idtoGenero, i, tarifas);
		}
		creaEmbalado(odatDAO, tdao, propDAO, oreg, ido, idtoGenero, unidadesXBulto, bultosXPalet);
	}
	
	private void creaPrecio(O_Datos_AttribDAO odatDAO, TClaseDAO tdao, PropertiesDAO propDAO, O_Reg_Instancias_IndexDAO oreg, 
			int idoGenero, int idtoGenero, int tarifa, double[] tarifas) throws SQLException, DataErrorException, NamingException {
		System.err.println("---> Creando precio");
		double pvp = tarifas[tarifa];
		if (pvp!=0) {
			Integer idtoPrecio = tdao.getTClaseByName("PRECIO").getIDTO();
			Individual ind = IndividualCreator.creator(fcdb, DAOManager.getInstance().isCommit(), gSQL, idtoPrecio, null, null, null, null,false,null);
			int ido = ind.getIdo();
			String rdn = ind.getRdn();

			//enlace
			int propTarifasVenta = propDAO.getIdPropByName("tarifas_venta");
			O_Datos_Attrib inTarifasVenta = new O_Datos_Attrib(idtoGenero, idoGenero, propTarifasVenta, ido, rdn, idtoPrecio, null, null, null, null);
			odatDAO.insert(inTarifasVenta);
			
			int propPVP = propDAO.getIdPropByName("pvp");
			O_Datos_Attrib inPVP = new O_Datos_Attrib(idtoPrecio, ido, propPVP, null, null, Constants.IDTO_DOUBLE, pvp, pvp, null, null);
			odatDAO.insert(inPVP);
			//buscar el ido de esa tarifa
			LinkedList<Object> lodt = oreg.getAllCond("RDN='Tarifa_Precio_" + tarifa + "'");
			Iterator it = lodt.iterator();
			if (it.hasNext()) {
				O_Reg_Instancias_Index odt = (O_Reg_Instancias_Index)it.next();
				int idoTarifa = odt.getId_o();
				Integer idtoTarifa = tdao.getTClaseByName("TARIFA_PRECIO").getIDTO();
				int propTarifaPrecio= propDAO.getIdPropByName("tarifa_precio");
				O_Datos_Attrib inTarifaPrecio = new O_Datos_Attrib(idtoPrecio, ido, propTarifaPrecio, idoTarifa, "Tarifa_Precio_"+tarifa, idtoTarifa, null, null, null, null);
				odatDAO.insert(inTarifaPrecio);
			} else
				throw new DataErrorException("No están todas los enumerados tarifa creados");
		}
	}
	private void creaEmbalado(O_Datos_AttribDAO odatDAO, TClaseDAO tdao, PropertiesDAO propDAO, O_Reg_Instancias_IndexDAO oreg, 
			int idoGenero, int idtoGenero, Double unidadesXBulto, Double bultosXPalet) throws SQLException, DataErrorException, NamingException {
		System.err.println("---> Creando embalado");
		if (unidadesXBulto!=null || bultosXPalet!=null) {
			Integer idtoEmbalado = tdao.getTClaseByName("EMBALADO_DE_CONTENEDOR").getIDTO();

			Individual ind = IndividualCreator.creator(fcdb, DAOManager.getInstance().isCommit(), gSQL, idtoEmbalado, null, null, null, null,false,null);
			int ido = ind.getIdo();
			String rdn = ind.getRdn();
			
			//enlace
			int propTipoEmbalado = propDAO.getIdPropByName("tipo_embalado");
			O_Datos_Attrib inTarifasVenta = new O_Datos_Attrib(idtoGenero, idoGenero, propTipoEmbalado, ido, rdn, idtoEmbalado, null, null, null, null);
			odatDAO.insert(inTarifasVenta);
			
			if (unidadesXBulto!=null) {
				int propUnidadesXBulto = propDAO.getIdPropByName("cantidadXenvase1");
				O_Datos_Attrib inUnidadesXBulto = new O_Datos_Attrib(idtoEmbalado, ido, propUnidadesXBulto, null, null, Constants.IDTO_DOUBLE, unidadesXBulto, unidadesXBulto, null, null);
				odatDAO.insert(inUnidadesXBulto);
			}
			if (bultosXPalet!=null) {
				int propBultosXPalet = propDAO.getIdPropByName("envase1Xenvase2");
				O_Datos_Attrib inBultosXPalet = new O_Datos_Attrib(idtoEmbalado, ido, propBultosXPalet, null, null, Constants.IDTO_DOUBLE, bultosXPalet, bultosXPalet, null, null);
				odatDAO.insert(inBultosXPalet);
			}
		}
	}
	
	private int updatePrecios(O_Datos_AttribDAO odatDAO, TClaseDAO tdao, PropertiesDAO propDAO, 
			int ido, int idtoPrecio, double[] tarifas) throws SQLException, DataErrorException {
		System.err.println("---> Actualizando precio con ido " + ido);
		int numTarifa = -1;
		LinkedList<Object> lodt = odatDAO.getByID(String.valueOf(ido));
		Iterator it = lodt.iterator();
		
		int propPVP = propDAO.getIdPropByName("pvp");
		int propTarifa = propDAO.getIdPropByName("tarifa_precio");
		
		//dependiendo del valor de tarifa -> actualizar precio
		boolean encontradoPVP = false;
		boolean encontradoTarifa = false;
		double tarifa = 0;
		while (it.hasNext()) {
			//ver que property es
			O_Datos_Attrib odt = (O_Datos_Attrib)it.next();
			Integer prop = odt.getPROPERTY();
			
			if (prop.equals(propPVP)) {
				encontradoPVP = true;
			} else if (prop.equals(propTarifa)) {
				encontradoTarifa = true;
				//ver que tarifa es
				//para ello cojo el valTexto que contiene el rdn
				String rdnTarifa = odt.getVALTEXTO();
				String[] str = rdnTarifa.split("_");
				numTarifa = Integer.parseInt(str[str.length-1]);
				tarifa = tarifas[numTarifa];
			}
		}
		if (!encontradoTarifa) {
			throw new DataErrorException("Precio sin tarifa rellena: " + ido);
		}
		if (encontradoPVP) {
			if (tarifa==0)
				//eliminar PRECIO
				odatDAO.deleteCond("ID_O=" + ido + " or VAL_NUM=" + ido);
			else
				odatDAO.update("Q_MIN=" + tarifa + ", Q_MAX=" + tarifa, "ID_O=" + ido + " AND PROPERTY=" + propPVP);
		} else {
			if (tarifa==0)
				//eliminar PRECIO
				odatDAO.deleteCond("ID_O=" + ido + " or VAL_NUM=" + ido);
			else {
				O_Datos_Attrib in = new O_Datos_Attrib(idtoPrecio, ido, propPVP, null, null, Constants.IDTO_DOUBLE, tarifa, tarifa, null, null);
				odatDAO.insert(in);
			}
		}
		return numTarifa;
	}
	private void updateEmbalado(O_Datos_AttribDAO odatDAO, TClaseDAO tdao, PropertiesDAO propDAO, 
			int ido, int idtoEmbalado, Double unidadesXBulto, Double bultosXPalet) throws SQLException {
		System.err.println("---> Actualizando embalado con ido " + ido);
		if (unidadesXBulto!=null || bultosXPalet!=null) {
			LinkedList<Object> lodt = odatDAO.getByID(String.valueOf(ido));
			Iterator it = lodt.iterator();
			
			int propUnidadesXBulto = propDAO.getIdPropByName("cantidadXenvase1");
			int propBultosXPalet = propDAO.getIdPropByName("envase1Xenvase2");
			
			boolean encontradoUnidadesXBulto = false;
			boolean encontradoBultosXPalet = false;
			while (it.hasNext()) {
				//ver que property es
				O_Datos_Attrib odt = (O_Datos_Attrib)it.next();
				Integer prop = odt.getPROPERTY();
				
				if (prop.equals(propUnidadesXBulto)) {
					encontradoUnidadesXBulto = true;
					if (unidadesXBulto==null)
						odatDAO.deleteCond("ID_O=" + ido + " AND PROPERTY=" + prop);
					else if (!odt.getQMIN().equals(unidadesXBulto)) {
						//updateUnidadesXBulto
						odatDAO.update("Q_MIN=" + unidadesXBulto + ", Q_MAX=" + unidadesXBulto, "ID_O=" + ido + " AND PROPERTY=" + prop);
					}
				} else if (prop.equals(propBultosXPalet)) {
					encontradoBultosXPalet = true;
					if (bultosXPalet==null)
						odatDAO.deleteCond("ID_O=" + ido + " AND PROPERTY=" + prop);
					else if (!odt.getQMIN().equals(bultosXPalet)) {
						//updateBultosXPalet
						odatDAO.update("Q_MIN=" + bultosXPalet + ", Q_MAX=" + bultosXPalet, "ID_O=" + ido + " AND PROPERTY=" + prop);
					}
				}
			}
			if (!encontradoUnidadesXBulto && unidadesXBulto!=null) {
				O_Datos_Attrib in = new O_Datos_Attrib(idtoEmbalado, ido, propUnidadesXBulto, null, null, Constants.IDTO_DOUBLE, unidadesXBulto, unidadesXBulto, null, null);
				odatDAO.insert(in);
			}
			if (!encontradoBultosXPalet && bultosXPalet!=null) {
				O_Datos_Attrib in = new O_Datos_Attrib(idtoEmbalado, ido, propBultosXPalet, null, null, Constants.IDTO_DOUBLE, bultosXPalet, bultosXPalet, null, null);
				odatDAO.insert(in);
			}
		} else {
			//eliminar EMBALADO
			odatDAO.deleteCond("ID_O=" + ido + " or VAL_NUM=" + ido);
		}
	}
	
}
