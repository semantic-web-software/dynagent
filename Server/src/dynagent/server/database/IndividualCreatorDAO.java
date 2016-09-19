package dynagent.server.database;


public class IndividualCreatorDAO {
/*
	public static Individual creator(int idto, GenerateSQL gSQL, String rdn, String valuePropPrefix, String sufixIndex, String destination, boolean replica, Integer id) throws SQLException, DataErrorException, NamingException {
		
		O_Datos_AttribDAO odatDAO = new O_Datos_AttribDAO();
		O_Reg_Instancias_IndexDAO oreg = new O_Reg_Instancias_IndexDAO();
		odatDAO.open();
		
		int autonum = subInsertRowObject(oreg, idto);
		Integer ido = null;
		if (sufixIndex!=null) {
			String idoStr = String.valueOf(autonum) + sufixIndex;
			ido = Integer.parseInt(idoStr);
		} else
			ido = autonum;
		
		if (rdn==null)
			rdn = getIndex(odatDAO, gSQL, ido, idto, valuePropPrefix, destination);
		
		if (rdn==null)
			rdn = String.valueOf(ido);
		
		oreg.update("rdn='" + rdn.replaceAll("'", "''") + "', id_o="+ido, "autonum=" + autonum);
		
		O_Datos_Attrib inRdn = new O_Datos_Attrib(idto, ido, Constants.IdPROP_RDN, null, rdn.replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, destination);
		odatDAO.insert(inRdn);
		if (replica) {
			Replica_DataDAO replDAO = new Replica_DataDAO();
			Replica_Data repRdn = new Replica_Data(id, "NEW", idto, ido, Constants.IdPROP_RDN, null, null, rdn.replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, destination, System.currentTimeMillis());
			replDAO.insert(repRdn);
		}
		
		odatDAO.close();
		
		Individual ind = new Individual(ido, rdn);
		return ind;
	}
	
	public static int subInsertRowObject(O_Reg_Instancias_IndexDAO oreg, int id_to) throws SQLException {
		String random = String.valueOf((new Random()).nextInt());
		O_Reg_Instancias_Index or = new O_Reg_Instancias_Index(id_to, random);
		oreg.insert(or);
		//System.out.println("INSERT ROW");
		O_Reg_Instancias_Index oReg = (O_Reg_Instancias_Index)oreg.getAllCond("rdn='" + random + "'" + " AND id_to=" + id_to).getFirst();
		return oReg.getAutonum();
	}

	//TODO propPrefix, prefijos temporales y filtros no soportado
	private static String getIndex(O_Datos_AttribDAO odatDAO, GenerateSQL gSQL, int ido, int idto, String valuePropPrefix, String destination) 
			throws SQLException, NamingException, DataErrorException  {
		String changeRdn = null;
		HashMap<Integer, ArrayList<IndexFilter>> propIndexFilter = new HashMap<Integer, ArrayList<IndexFilter>>();
		getIndexDBDAO(propIndexFilter, idto);
		
		if (propIndexFilter.size()==0)
			//throw new DataErrorException("Falta indice para idto " + idto);
			System.out.println("Falta indice para idto " + idto);
		else {
			Iterator it = propIndexFilter.keySet().iterator();
			while (it.hasNext()) {
				Integer keyProp = (Integer)it.next();
				HashMap<Integer,StringBuffer> valuesFilter = new HashMap<Integer,StringBuffer>();
				IndexFilter indexFilterUpdate = IndexFilterFunctions.createIndexFilter(keyProp, propIndexFilter, valuesFilter);
				if (indexFilterUpdate!=null) {
						String change = "";
						Integer propPrefix = indexFilterUpdate.getPropPrefix();
						if (propPrefix!=null)
							change = valuePropPrefix;
						String prefix = indexFilterUpdate.getPrefix();
						//System.out.println("prefix " + prefix);
						if (prefix!=null)
							change += prefix;
						int index = indexFilterUpdate.getIndex();
						//System.out.println("index " + index);
						change += index;
						String sufix = indexFilterUpdate.getSufix();
						//System.out.println("sufix " + sufix);
						if (sufix!=null)
							change += sufix;
						
						if (keyProp==Constants.IdPROP_RDN)
							changeRdn = change;
						else {
							O_Datos_Attrib in = new O_Datos_Attrib(idto, ido, keyProp, null, change, Constants.IDTO_STRING, null, null, null, destination);
							odatDAO.insert(in);
						}
						//ahora incremento de indice
						incrementIndexDAO(indexFilterUpdate.getIdo(), gSQL);
						//IndexFilter.incrementIndex(indexFilterUpdate, keyProp, idto, true, fcdb);
					//}
				}
			}
		}
		return changeRdn;
	}
	
	private static boolean getIndexDBDAO(HashMap<Integer, ArrayList<IndexFilter>> propIndexFilter, int idto) throws SQLException, NamingException {
		boolean hasPropIndexFilter = false;
		IndexDAO sind = new IndexDAO();
		LinkedList<Object> llo = sind.getAllCond("ID_TO=" + idto);
		Iterator it = llo.iterator();
		while (it.hasNext()) {
			Index ind = (Index)it.next();
			int ido = ind.getIdo();
			int prop = ind.getProperty();
			int index = ind.getIndex();
			String prefix = ind.getPrefix();
			String sufix = ind.getSufix();
			Integer propPrefix = ind.getPropPrefix();
			Integer propFilter = ind.getPropFilter();
			if (propFilter!=null)
				hasPropIndexFilter = true;
			String valueFilter = ind.getValueFilter();
			boolean globalSufix = ind.isGlobalSufix();
			
			String mascPrefixTemp = ind.getMascPrefixTemp();
			Integer propPrefixTemp = ind.getPropPrefixTemp();
			Integer contYear = ind.getContYear();
			String lastPrefixTemp = ind.getLastPrefixTemp();
			
			//if (valueFilter!=null)
				//valueFilter = parserValueFilterDAO(valueFilter);
			IndexFilterFunctions.createIndex(propIndexFilter, ido, prop, index, prefix, sufix, propPrefix, propFilter, valueFilter, globalSufix, mascPrefixTemp, propPrefixTemp, contYear, lastPrefixTemp);
		}
		return hasPropIndexFilter;
	}
	public static void incrementIndexDAO(int ido, GenerateSQL gSQL) throws SQLException, NamingException {
		//IndexDAO indDAO = new IndexDAO();
		//indDAO.update(IndexFilter.setIncrementIndex(indexFilterUpdate.getIndex(), gSQL), IndexFilter.whereIncrementIndex(indexFilterUpdate, keyProp, idto, true));
		String nameProp = Constants.PROP_INICIO_CONTADOR;
		String sqlPropIndex = "(SELECT PROP FROM PROPERTIES WHERE NAME='" + nameProp + "')";
		O_Datos_AttribDAO odatDAO = new O_Datos_AttribDAO();
		String set = "Q_MIN=Q_MIN+1,Q_MAX=Q_MAX+1";
		String where = "ID_O=" + ido + " AND PROPERTY=" + sqlPropIndex;
		odatDAO.update(set, where);
	}

*/
}