package dynagent.tools.updaters.articulos;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Configuration;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.O_Reg_Instancias_Index;
import dynagent.common.exceptions.DataErrorException;
import dynagent.server.database.IndividualCreator;
import dynagent.server.database.dao.ConfigurationDAO;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.database.dao.O_Reg_Instancias_IndexDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;

public class UpdateArticulos {
	private FactoryConnectionDB fcdb;
	private GenerateSQL gSQL;
	private HashMap<String, Integer> aMarca;
	private HashMap<String, Integer> aFamilia;
	private HashMap<String, Integer> aSubfamilia;
	
	public UpdateArticulos(FactoryConnectionDB fcdb) {
		this.fcdb = fcdb;
		this.gSQL = new GenerateSQL(fcdb.getGestorDB());
		this.aMarca = new HashMap<String, Integer>();
		this.aFamilia = new HashMap<String, Integer>();
		this.aSubfamilia = new HashMap<String, Integer>();
		
		//TODO cargar marcas, familias y subfamilias de la base de datos 
		//si la base de datos ya contiene articulos
	}
	
	public void startUpdate() {
		//DAOManager.getInstance().setCommit(true);
		try {
			System.err.println("---> Actualizando articulos");
			DAOManager.getInstance().setCommit(false); //antes de abrir DAO
			O_Datos_AttribDAO odatDAO = new O_Datos_AttribDAO();
			O_Reg_Instancias_IndexDAO oreg = new O_Reg_Instancias_IndexDAO();
			//O_Datos_Attrib_MemoDAO odatMemoDAO = new O_Datos_Attrib_MemoDAO();
			TClaseDAO tdao = new TClaseDAO();
			PropertiesDAO propDAO = new PropertiesDAO();
			odatDAO.open();
			//odatDAO.commit();
			
			String sufixTienda = null;
			ConfigurationDAO conf = new ConfigurationDAO();
			LinkedList<Object> llo = conf.getAllCond("label='sufix_tienda'");
			Iterator it = llo.iterator();
			if (it.hasNext())
				sufixTienda = ((Configuration)it.next()).getValue();
			System.out.println("sufixTienda " + sufixTienda);
			
			String sql = "SELECT codigo, concepto, pvp, fabricacion_propia, " +
					"marca, familia, subfamilia FROM articulos";
			ConnectionDB con = fcdb.createConnection(true);
			Statement st = null;
			ResultSet rs = null;
			System.out.println("sql->"+sql);
			try {
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				while (rs.next()) {
					String codigo = rs.getString(1);
					String concepto = rs.getString(2);
					Double pvp = null;
					String pvpStr = rs.getString(3);
					if (!rs.wasNull())
						pvp = Double.parseDouble(pvpStr.replaceAll(",", "."));
					Double fabrPropia = null;
					String fabrPropiaStr = rs.getString(4);
					if (!rs.wasNull()) {
						if (fabrPropiaStr.equals("SI"))
							fabrPropia = new Double(1);
						else
							fabrPropia = new Double(0);
					}
					String marca = rs.getString(5);
					String familia = rs.getString(6);
					String subfamilia = rs.getString(7);
					
					Integer idtoArtTextil = tdao.getTClaseByName("ARTÍCULO_TEXTIL").getIDTO();
					creaArticuloTextil(odatDAO, tdao, propDAO, oreg, sufixTienda, idtoArtTextil, codigo, concepto, pvp, 
							fabrPropia, marca, familia, subfamilia);
				}
				
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
			//insert into replica_master
			//select *,null,null,'A1001' from o_datos_atrib where id_o>11697001;
			
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

/*TODO COMPLETAR SI SE DIERA EL CASO DE ACTUALIZACION DE ARTICULOS
 * private void trataArticuloTextil(O_Datos_AttribDAO odatDAO, TClaseDAO tdao, PropertiesDAO propDAO, O_Reg_Instancias_IndexDAO oreg, 
			String codigo, String concepto, Double pvp, Double fabrPropia, 
			String marca, String familia, String subfamilia) throws SQLException, DataErrorException, NamingException {
		//buscar si en base de datos esta un genero con ese codigo
		Integer idtoArtTextil = tdao.getTClaseByName("ARTÍCULO_TEXTIL").getIDTO();
		
		LinkedList<Object> lodt = oreg.getAllCond("ID_TO=" + idtoArtTextil + " AND RDN='" + codigo + "'");
		Iterator it = lodt.iterator();
		if (it.hasNext()) {
			O_Reg_Instancias_Index odt = (O_Reg_Instancias_Index)it.next();
			int ido = odt.getId_o();
			System.err.println("---> Actualizando articulo textil con ido " + ido);
			//actualizar valores
			LinkedList<Object> lodt2 = odatDAO.getByID(String.valueOf(ido));
			Iterator it2 = lodt2.iterator();
			
			int propDescripcion = propDAO.getIdPropByName("descripción");
			int propPvp= propDAO.getIdPropByName("pvp_iva_incluido");
			int propFabrPropia = propDAO.getIdPropByName("fabricación_propia");
			int propMarca = propDAO.getIdPropByName("marca");
			int propFamilia = propDAO.getIdPropByName("familia");
			int propSubfamilia = propDAO.getIdPropByName("subfamilia");
			
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
				O_Datos_Attrib in = new O_Datos_Attrib(idtoArtTextil, ido, propDescripcion, null, descripcion, Constants.IDTO_STRING, null, null);
				odatDAO.insert(in);
			}
			if (!encontradoCubicaje && cubicaje!=null) {
				O_Datos_Attrib in = new O_Datos_Attrib(idtoArtTextil, ido, propCubica, null, null, Constants.IDTO_DOUBLE, cubicaje, cubicaje);
				odatDAO.insert(in);
			}
			if (!encontradoPuntoVerde && puntoVerde!=null) {
				O_Datos_Attrib in = new O_Datos_Attrib(idtoArtTextil, ido, propPuntoVerde, null, null, Constants.IDTO_DOUBLE, puntoVerde, puntoVerde);
				odatDAO.insert(in);
			}
			for (int i=0;i<tarifasTratadas.length;i++) {
				if (i!=10 && i!=11 && i!=12 && i!=13 && i!=14) {
					if (tarifasTratadas[i]==0) {
						creaPrecio(odatDAO, tdao, propDAO, oreg, ido, idtoArtTextil, i, tarifas);
					}
				}
			}
			if (!encontradoEmbalado) {
				creaEmbalado(odatDAO, tdao, propDAO, oreg, ido, idtoArtTextil, unidadesXBulto, bultosXPalet);
			}
		} else {
			//crear todo
			System.err.println("---> Creando género");
			creaArticuloTextil(odatDAO, tdao, propDAO, oreg, idtoArtTextil, 
					codigo, concepto, pvp, fabrPropia, marca, familia, subfamilia);
		}
	}*/
	
	private void creaArticuloTextil(O_Datos_AttribDAO odatDAO, TClaseDAO tdao, PropertiesDAO propDAO, O_Reg_Instancias_IndexDAO oreg, 
			String sufixTienda, int idtoArtTextil, String codigo, String concepto, Double pvp, Double fabrPropia, 
			String marca, String familia, String subfamilia) throws SQLException, DataErrorException, NamingException {
		//insertar rdn que ya lo tenemos
		int ido = IndividualCreator.creator(fcdb, DAOManager.getInstance().isCommit(), gSQL, idtoArtTextil, codigo, null, sufixTienda, "*",false,null).getIdo();
		
		//insertar el resto de las DataProperties
		if (concepto!=null) {
			int propDescripcion = propDAO.getIdPropByName("descripción");
			O_Datos_Attrib inDescripcion = new O_Datos_Attrib(idtoArtTextil, ido, propDescripcion, null, concepto.replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, "*");
			odatDAO.insert(inDescripcion);
		}
		if (pvp!=null) {
			int propPvp = propDAO.getIdPropByName("pvp_iva_incluido");
			O_Datos_Attrib inPvp = new O_Datos_Attrib(idtoArtTextil, ido, propPvp, null, null, Constants.IDTO_DOUBLE, pvp, pvp, null, "*");
			odatDAO.insert(inPvp);
		}
		if (fabrPropia!=null) {
			int propFabrPropia = propDAO.getIdPropByName("fabricación_propia");
			O_Datos_Attrib inFabrPropia = new O_Datos_Attrib(idtoArtTextil, ido, propFabrPropia, null, null, Constants.IDTO_BOOLEAN, fabrPropia, fabrPropia, null, "*");
			odatDAO.insert(inFabrPropia);
		}
		creaIva(odatDAO, tdao, propDAO, oreg, ido, idtoArtTextil);
		trataMarca(odatDAO, tdao, propDAO, oreg, sufixTienda, ido, idtoArtTextil, marca);
		trataFamilia(odatDAO, tdao, propDAO, oreg, sufixTienda, ido, idtoArtTextil, familia);
		trataSubfamilia(odatDAO, tdao, propDAO, oreg, sufixTienda, ido, idtoArtTextil, subfamilia);
	}
	
	private void creaIva(O_Datos_AttribDAO odatDAO, TClaseDAO tdao, PropertiesDAO propDAO, O_Reg_Instancias_IndexDAO oreg, 
			int idoArtTextil, int idtoArtTextil) throws SQLException, DataErrorException, NamingException {
		System.err.println("---> Creando iva");
		//enlace
		Integer idtoTipoIva = tdao.getTClaseByName("TIPO_IVA").getIDTO();
		int propTipoIva = propDAO.getIdPropByName("iva");
		String rdnIvaGeneral = "General";
		O_Reg_Instancias_Index oregGeneral = (O_Reg_Instancias_Index)oreg.getAllCond("ID_TO=" + idtoTipoIva + " AND RDN='" + rdnIvaGeneral + "'").getFirst();
		int idoIvaGeneral = oregGeneral.getId_o();
		O_Datos_Attrib inIva = new O_Datos_Attrib(idtoArtTextil, idoArtTextil, propTipoIva, idoIvaGeneral, rdnIvaGeneral, idtoTipoIva, null, null, null, "*");
		odatDAO.insert(inIva);
	}
	
	private void trataMarca(O_Datos_AttribDAO odatDAO, TClaseDAO tdao, PropertiesDAO propDAO, O_Reg_Instancias_IndexDAO oreg, 
			String sufixTienda, int idoArtTextil, int idtoArtTextil, String marca) throws SQLException, DataErrorException, NamingException {
		if (marca!=null) {
			System.err.println("---> Creando marca");
			
			Integer idtoMarca = tdao.getTClaseByName("MARCA").getIDTO();
			int propMarca = propDAO.getIdPropByName("marca");
			Integer idoMarca = this.aMarca.get(marca);
			if (idoMarca==null) {
				idoMarca = IndividualCreator.creator(fcdb, DAOManager.getInstance().isCommit(), gSQL, idtoMarca, marca, null, sufixTienda, "*",false,null).getIdo();
				this.aMarca.put(marca, idoMarca);
			}
			//enlace
			O_Datos_Attrib inIva = new O_Datos_Attrib(idtoArtTextil, idoArtTextil, propMarca, idoMarca, marca, idtoMarca, null, null, null, "*");
			odatDAO.insert(inIva);
		}
	}

	private void trataFamilia(O_Datos_AttribDAO odatDAO, TClaseDAO tdao, PropertiesDAO propDAO, O_Reg_Instancias_IndexDAO oreg, 
			String sufixTienda, int idoArtTextil, int idtoArtTextil, String familia) throws SQLException, DataErrorException, NamingException {
		if (familia!=null) {
			System.err.println("---> Creando familia");
			
			Integer idtoFamilia = tdao.getTClaseByName("FAMILIA").getIDTO();
			int propFamilia = propDAO.getIdPropByName("familia");
			Integer idoFamilia = this.aFamilia.get(familia);
			if (idoFamilia==null) {
				idoFamilia = IndividualCreator.creator(fcdb, DAOManager.getInstance().isCommit(), gSQL, idtoFamilia, familia, null, sufixTienda, "*",false,null).getIdo();
				this.aFamilia.put(familia, idoFamilia);
			}
			//enlace
			O_Datos_Attrib inIva = new O_Datos_Attrib(idtoArtTextil, idoArtTextil, propFamilia, idoFamilia, familia, idtoFamilia, null, null, null, "*");
			odatDAO.insert(inIva);
		}
	}
	
	private void trataSubfamilia(O_Datos_AttribDAO odatDAO, TClaseDAO tdao, PropertiesDAO propDAO, O_Reg_Instancias_IndexDAO oreg, 
			String sufixTienda, int idoArtTextil, int idtoArtTextil, String subfamilia) throws SQLException, DataErrorException, NamingException {
		if (subfamilia!=null) {
			System.err.println("---> Creando subfamilia");
			
			Integer idtoSubfamilia = tdao.getTClaseByName("SUBFAMILIA").getIDTO();
			int propSubfamilia = propDAO.getIdPropByName("subfamilia");
			Integer idoSubfamilia = this.aSubfamilia.get(subfamilia);
			if (idoSubfamilia==null) {
				idoSubfamilia = IndividualCreator.creator(fcdb, DAOManager.getInstance().isCommit(), gSQL, idtoSubfamilia, subfamilia, null, sufixTienda, "*",false,null).getIdo();
				this.aSubfamilia.put(subfamilia, idoSubfamilia);
			}
			//enlace
			O_Datos_Attrib inIva = new O_Datos_Attrib(idtoArtTextil, idoArtTextil, propSubfamilia, idoSubfamilia, subfamilia, idtoSubfamilia, null, null, null, "*");
			odatDAO.insert(inIva);
		}
	}
}
