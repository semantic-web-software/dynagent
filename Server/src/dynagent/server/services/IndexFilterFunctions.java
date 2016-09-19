package dynagent.server.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;

import com.sun.jms.service.DBManager;

import dynagent.common.Constants;
import dynagent.common.basicobjects.IndexFilter;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.AuxiliarModel;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;

public class IndexFilterFunctions {

	public static int NOTHING = 0;
	public static int RESTORE = 1;
	public static int SEARCH_LAST = 2;
	
	/*public static String setIncrementIndex(int index, GenerateSQL generateSQL) throws SQLException, NamingException {
		int newIndex = index+1;
		String set = generateSQL.getCharacterBegin() + "INDEX" + generateSQL.getCharacterEnd() + "=" + newIndex;
		return set;
	}
	public static String setDecrementIndex(int index, GenerateSQL generateSQL) throws SQLException, NamingException {
		int newIndex = index-1;
		String set = generateSQL.getCharacterBegin() + "INDEX" + generateSQL.getCharacterEnd() + "=" + newIndex;
		return set;
	}
	
	public static String whereIncrementIndex(IndexFilter indexFilterUpdate, int keyProp, int idto, boolean compruebaFilter) throws SQLException, NamingException {
		String where = "PROPERTY=" + keyProp + " AND ID_TO=" + idto;
		if (indexFilterUpdate.getPrefix()!=null)
			where += " AND PREFIX='" + indexFilterUpdate.getPrefix() + "'";
		else
			where += " AND PREFIX IS NULL";
		if (indexFilterUpdate.getSufix()!=null)
			where += " AND SUFIX='" + indexFilterUpdate.getSufix() + "'";
		else
			where += " AND SUFIX IS NULL";
		if (indexFilterUpdate.getPropPrefix()!=null)
			where += " AND PROPERTY_PREFIX=" + indexFilterUpdate.getPropPrefix();
		else
			where += " AND PROPERTY_PREFIX IS NULL";
		if (compruebaFilter) {
			if (indexFilterUpdate.getPropFilter()!=null)
				where += " AND PROPERTY_FILTER=" + indexFilterUpdate.getPropFilter();
			else
				where += " AND PROPERTY_FILTER IS NULL";
			if (indexFilterUpdate.getValueFilter()!=null)
				where += " AND VALUE_FILTER='" + indexFilterUpdate.getValueFilter() + "'";
			else
				where += " AND VALUE_FILTER IS NULL";
		} else
			where += " AND PROPERTY_FILTER=" + indexFilterUpdate.getPropFilter() + " AND VALUE_FILTER='" + indexFilterUpdate.getValueFilter() + "'";
		return where;
	}*/
	
	public static void createIndex(ArrayList<IndexFilter> aIndexF, int ido, int index, String prefix, String sufix,
			Integer propPrefix, Integer propFilter, String valueFilter, boolean globalSufix, 
			String mascPrefixTemp, Integer propPrefixTemp, Integer contYear, String lastPrefixTemp, Integer minDigits, Integer miEmpresa) {
		IndexFilter indexF = new IndexFilter(ido, mascPrefixTemp, propPrefixTemp, contYear, lastPrefixTemp, propPrefix, prefix, index, sufix, globalSufix, minDigits, propFilter, valueFilter, miEmpresa);
		//System.out.println("indexF " + indexF.toString());
		aIndexF.add(indexF);
	}
	
	public static IndexFilter createIndexFilter(ArrayList<IndexFilter> aIndexF, 
			HashMap<Integer,StringBuffer> valuesFilter) {
		int ido = 0;
		Integer index = null;
		String prefix = null;
		String sufix = null;
		Integer propPrefix = null;
		boolean globalSufix = false;
		String mascPrefixTemp = null;
		Integer propPrefixTemp = null;
		Integer contYear = null;
		String lastPrefixTemp = null;
		Integer minDigits = null;
		Integer miEmpresa = null;

		Integer propertyF = null;
		String valueF = null;
		IndexFilter indexFilterUpdate = null;
		for (int j=0;j<aIndexF.size();j++) {
			IndexFilter indexF = aIndexF.get(j);
			
			miEmpresa = indexF.getMiEmpresa();
			
			if (indexF.getPropFilter()!=null) {
				propertyF = indexF.getPropFilter();
				valueF = indexF.getValueFilter();
				
				if (valuesFilter.get(propertyF)!=null){
					StringBuffer sb = valuesFilter.get(propertyF);
					sb.append("," + valueF);
				} else {
					StringBuffer sb = new StringBuffer(valueF);
					valuesFilter.put(propertyF, sb);
				}
				
				if (indexF.getIs()) {
					ido = indexF.getIdo();
					index = indexF.getIndex();
					prefix = indexF.getPrefix();
					sufix = indexF.getSufix();
					propPrefix = indexF.getPropPrefix();
					globalSufix = indexF.isGlobalSufix();
					mascPrefixTemp = indexF.getMascPrefixTemp();
					propPrefixTemp = indexF.getPropPrefixTemp();
					contYear = indexF.getContYear();
					lastPrefixTemp = indexF.getLastPrefixTemp();
					minDigits = indexF.getMinDigits();

					indexFilterUpdate = new IndexFilter(ido, mascPrefixTemp, propPrefixTemp, contYear, lastPrefixTemp, propPrefix, prefix, index, sufix, globalSufix, minDigits, propertyF, valueF, miEmpresa);
					
					//si la empresa coincide y el filtrado tambien
					//if (miEmpresaDocument!=null && indexF.getMiEmpresa()!=null && miEmpresa.equals(miEmpresaDocument))
						break;
				}
			} else if (index==null) {
				//almacenar si no se ha almacenado nada o (si el almacenado no tenia miEmpresa rellena y miEmpresa viene rellena)
				//boolean seguir = indexFilterUpdate==null || (indexFilterUpdate.getMiEmpresa()==null && miEmpresa!=null);

				//if (seguir) {
					ido = indexF.getIdo();
					index = indexF.getIndex();
					prefix = indexF.getPrefix();
					sufix = indexF.getSufix();
					propPrefix = indexF.getPropPrefix();
					globalSufix = indexF.isGlobalSufix();
					mascPrefixTemp = indexF.getMascPrefixTemp();
					propPrefixTemp = indexF.getPropPrefixTemp();
					contYear = indexF.getContYear();
					lastPrefixTemp = indexF.getLastPrefixTemp();
					minDigits = indexF.getMinDigits();
					
					indexFilterUpdate = new IndexFilter(ido, mascPrefixTemp, propPrefixTemp, contYear, lastPrefixTemp, propPrefix, prefix, index, sufix, globalSufix, minDigits, null, null, miEmpresa);
				//}
			}
		}
		return indexFilterUpdate;
	}
	
	
	public static int getValueTemp(String valuePropPrefixTemp, 
			String mascPrefixTemp, Integer contYear, String lastPrefixTemp, 
			ArrayList<Object> returned) {
		//System.out.println("Inicio de la funcion getValueTemp");
		System.out.println("Parametros: valuePropPrefixTemp " + valuePropPrefixTemp + 
				", mascPrefixTemp " + mascPrefixTemp + ", contYear " + contYear + 
				", lastPrefixTemp " + lastPrefixTemp);
		int result = NOTHING;
		
		if (valuePropPrefixTemp!=null) {
			//obtener mes y año de la fecha
			long time=Double.valueOf(valuePropPrefixTemp).longValue();
			Date dateTime = new Date(time*Constants.TIMEMILLIS);

			Calendar cal = Calendar.getInstance();
			cal.setTime(dateTime);
			int month = cal.get(Calendar.MONTH)+1;
			int year = cal.get(Calendar.YEAR);

			System.out.println("month " + month);
			System.out.println("year " + year);
			
			//valores posibles de la mascara:
			//aamm, amm, a, aa, mm
			String lastYearStr = null;
			String lastMonthStr = null;
			
			String monthStr = String.valueOf(month);
			String yearStr = String.valueOf(year);
			
			String valueTemp = null;
			String valueTempCont = null;
			String yearStrCont = null;
			if (contYear!=null)
				yearStrCont = String.valueOf(contYear);
			if (mascPrefixTemp.equals("aamm") || mascPrefixTemp.equals("amm")) {
				if (mascPrefixTemp.equals("aamm")) {
					yearStr = yearStr.substring(2);
					if (contYear!=null)
						yearStrCont = StringUtils.leftPad(yearStrCont, 2, '0');
					if (lastPrefixTemp!=null) {
						lastYearStr = lastPrefixTemp.substring(0, 2);
						System.out.println("lastYearStr " + lastYearStr);
					}
					System.out.println("yearStr " + yearStr);
				} else if (mascPrefixTemp.equals("amm")) {
					yearStr = yearStr.substring(3);
					if (contYear!=null)
						yearStrCont = StringUtils.leftPad(yearStrCont, 1, '0');
					if (lastPrefixTemp!=null) {
						lastYearStr = lastPrefixTemp.substring(0, 1);
						System.out.println("lastYearStr " + lastYearStr);
					} 
					System.out.println("yearStr " + yearStr);
				}
				monthStr = StringUtils.leftPad(monthStr, 2, '0');
				System.out.println("monthStr " + monthStr);
				valueTemp = yearStr + monthStr;
				//ver contador de año
				if (contYear!=null)
					valueTempCont = yearStrCont + monthStr;
			} else if (mascPrefixTemp.equals("a") || mascPrefixTemp.equals("aa")) {
				if (mascPrefixTemp.equals("a")) {
					yearStr = yearStr.substring(3);
					valueTemp = yearStr;
					if (contYear!=null) {
						yearStrCont = StringUtils.leftPad(yearStrCont, 1, '0');
						valueTempCont = yearStrCont;
					}
				} else if (mascPrefixTemp.equals("aa")) {
					yearStr = yearStr.substring(2);
					valueTemp = yearStr;
					if (contYear!=null) {
						yearStrCont = StringUtils.leftPad(yearStrCont, 2, '0');
						valueTempCont = yearStrCont;
					}
				}
				if (lastPrefixTemp!=null)
					lastYearStr = lastPrefixTemp;
			} else if (mascPrefixTemp.equals("mm")) {
				monthStr = StringUtils.leftPad(monthStr, 2, '0');
				valueTemp = monthStr;
				if (contYear!=null)
					valueTempCont = monthStr;
				if (lastPrefixTemp!=null)
					lastMonthStr = lastPrefixTemp;
			}
			
			//si el año del ultimo prefijo almacenado no coincide con el formado ahora
			//reiniciar indice
			boolean reiniciar = false;
			boolean searchLastIndex = false;
			Integer yearIncrement = null;
			if (lastYearStr!=null) {
				Integer lastYear = Integer.parseInt(lastYearStr);
				System.out.println("lastYear " + lastYear);
				year = Integer.parseInt(yearStr);
				System.out.println("year " + year);
				if (lastYear<year) {
					reiniciar = true;
					if (contYear!=null)
						yearIncrement = year-lastYear;
				} else if (lastYear>year) {
					searchLastIndex = true;
					if (contYear!=null)
						yearIncrement = year-lastYear;
				}
				if (contYear!=null) {
					String newYear = null;
					if (yearIncrement!=null) {
						System.out.println("yearIncrement " + yearIncrement);
						newYear = String.valueOf(Integer.parseInt(yearStrCont) + yearIncrement);
						if (mascPrefixTemp.equals("aamm") || mascPrefixTemp.equals("aa"))
							newYear = StringUtils.leftPad(newYear, 2, '0');
						else if (mascPrefixTemp.equals("a") || mascPrefixTemp.equals("amm"))
							newYear = StringUtils.leftPad(newYear, 1, '0');
					} else
						newYear = yearStrCont;
	
					valueTempCont = newYear;
					if (monthStr!=null)
						valueTempCont += monthStr;
				}
			} else if (lastMonthStr!=null) {
				Integer lastMonth = Integer.parseInt(lastMonthStr);
				if (lastMonth<month)
					reiniciar = true;
				else if (lastMonth>month)
					searchLastIndex = true;
			}
			returned.add(valueTemp);
			if (contYear!=null) {
				returned.add(valueTempCont);
				returned.add(yearIncrement);
			}
			if (reiniciar) result = RESTORE;
			if (searchLastIndex) result = SEARCH_LAST;
		}
		System.out.println("result " + result);
		System.out.println("Fin de la funcion getValueTemp");
		return result;
	}
	public static int getIndexTempDB(FactoryConnectionDB factConnDB, int idto, String allPrefix, String sufix) throws NumberFormatException, SQLException, NamingException {
		//para ese idto y ese prefijo
		int index = 1;
		
		GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		ConnectionDB con = factConnDB.createConnection(true);
		String valTextoSql = null;
		if (sufix==null)
			valTextoSql = "substring(val_texto," + Integer.valueOf(allPrefix.length()+1) + "," + gSQL.getLength("val_texto") + "-" + allPrefix.length() + ")";
		else
			valTextoSql = "substring(val_texto," + Integer.valueOf(allPrefix.length()+1) + "," + gSQL.getLength("val_texto") + "-" + Integer.valueOf(allPrefix.length()+sufix.length()) + ")";
			
		String sql = "Select MAX(cast(" + valTextoSql + " as " + gSQL.getTypeInt() + ")) FROM o_datos_atrib " + //WITH(nolock) " +
			"WHERE id_to=" + idto + " AND property=" + Constants.IdPROP_RDN;
		if (allPrefix!=null)
			sql += " AND val_texto like '" + allPrefix + "%'";
		if (sufix!=null)
			sql += " AND val_texto like '%" + sufix + "'";
		sql += " and isnumeric(" + valTextoSql + ")=1";

		//System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				String valTexto = rs.getString(1);
				if (!rs.wasNull()) {
					index = Integer.parseInt(valTexto) + 1;
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}

		return index;
	}
	
	public static String getLastPrefixTemp(Integer c, String mascPrefixTemp, Integer propPrefixTemp, 
			Integer contYear, Integer propFilter, String valueFilter, FactoryConnectionDB factConnDB, DataBaseMap dataBaseMap) throws NamingException, SQLException, DataErrorException, NoSuchColumnException {
		String lastPrefixTemp = null;
		boolean hasPropPrefixTemp = propPrefixTemp!=null;
		if (hasPropPrefixTemp) {
			//en vez de usar lastPrefixTemp almacenado
			//construirlo con el ultimo prefijo temporal usado
			int chars = mascPrefixTemp.length();
			lastPrefixTemp = dBMaxValueIntOfIdtoProp(c, Constants.IdPROP_RDN, chars, contYear, mascPrefixTemp, propFilter, valueFilter, factConnDB, dataBaseMap);
		}
		return lastPrefixTemp;
	}
	private static String dBMaxValueIntOfIdtoProp(int idto, int prop, Integer chars, 
			Integer contYear, String mascPrefixTemp, 
			Integer propFilter, String valueFilter, FactoryConnectionDB factConnDB, DataBaseMap dataBaseMap) 
			throws SQLException, NamingException, DataErrorException, NoSuchColumnException {
		GenerateSQL generateSQL = new GenerateSQL(factConnDB.getGestorDB());
		IQueryInfo tableView = dataBaseMap.getView(idto);
		if (tableView==null)
			tableView = dataBaseMap.getTable(idto);
		String tableName = generateSQL.getCharacterBegin() + tableView.getName() + generateSQL.getCharacterEnd();
		
		String maxValue = null;
		String sql = null;
		if (propFilter!=null) {
			List<String> columnsName = tableView.getColumnNamesContainingProperty(propFilter);
			if (columnsName==null || columnsName.size()==0 || columnsName.get(0)==null)
				throw new DataErrorException("No existe una columna para la property " + AuxiliarModel.getPropertyName(prop, factConnDB) + " en la tabla/vista " + tableView.getName());
			String columnNameNoCharacter = columnsName.get(0);
			String columnName = generateSQL.getCharacterBegin() + columnNameNoCharacter + generateSQL.getCharacterEnd();
			
			sql = "select " + Constants.PROP_RDN + ", " + columnName + " from " + tableName;
		} else {
			List<String> columnsName = tableView.getColumnNamesContainingProperty(prop);
			if (columnsName==null || columnsName.size()==0 || columnsName.get(0)==null)
				throw new DataErrorException("No existe una columna para la property " + AuxiliarModel.getPropertyName(prop, factConnDB) + " en la tabla/vista " + tableView.getName());
			String columnNameNoCharacter = columnsName.get(0);
			String columnName = generateSQL.getCharacterBegin() + columnNameNoCharacter + generateSQL.getCharacterEnd();
			
			sql = "select " + columnName + ", NULL from " + tableName;
		}
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String propFilterValue = rs.getString(2);
				if (propFilter==null || propFilter!=null && propFilterValue!=null && valueFilter.equals(propFilterValue)) {
					String maxValueTmp = rs.getString(1);
					System.out.println("maxValueTmp " + maxValueTmp);
					if (maxValueTmp!=null) {
						boolean salir = false;
						if (chars!=null && maxValueTmp.length()>chars)
							maxValueTmp = maxValueTmp.substring(0,chars);
						else
							salir = true;
						System.out.println("maxValueTmp " + maxValueTmp);
						if (!salir && Auxiliar.hasIntValue(maxValueTmp)) {
							if (mascPrefixTemp.equals("aamm") || mascPrefixTemp.equals("amm") || 
									mascPrefixTemp.equals("mm")) {
								//se podria mejorar comprobando que lo que devuelve es en realidad 
								//una fecha del tipo de la mascara
								int month = Integer.parseInt(maxValueTmp.substring(maxValueTmp.length()-2));
								if (month<1 || month>12)
									salir = true;
							}
							//NO se podria comprobar que coincide con la property fecha
							//porque podria dar problemas de que se pisen
							
							if (!salir) {
								if (maxValue==null)
									maxValue = maxValueTmp;
								else {
									Integer maxValueInt = Integer.parseInt(maxValue);
									Integer maxValueTmpInt = Integer.parseInt(maxValueTmp);
									if (maxValueInt<maxValueTmpInt)
										maxValue = maxValueTmp;
								}
							}
							System.out.println("maxValue " + maxValue);
						}
					}
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		
		//los que lleven el año hacer un parseo para 
		//pasar el lastPrefix con año contador (cont asoc a año actual) a año real de la factura
		//porque en la mascara se guarda el año, no el contador
		if (maxValue!=null && contYear!=null) {
			long time = System.currentTimeMillis()/Constants.TIMEMILLIS;
			Date dateTime = new Date(time*Constants.TIMEMILLIS);
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateTime);
			int actualYear = cal.get(Calendar.YEAR);
			
			String yearStr = null;
			String monthStr = null;
			if (mascPrefixTemp.equals("aamm") || mascPrefixTemp.equals("amm")) {
				//substring para obtener mes y año dependiendo de mascPrefixTemp
				if (mascPrefixTemp.equals("aamm")) {
					yearStr = maxValue.substring(0, 2);
					monthStr = maxValue.substring(2);
				} else {
					yearStr = maxValue.substring(0, 1);
					monthStr = maxValue.substring(1);
				}
			} else if (mascPrefixTemp.equals("aa") || mascPrefixTemp.equals("a")) {
				yearStr = maxValue;
			}
			
			//obtener contYear y restarle contYear en el value
			Integer incremento = contYear-Integer.parseInt(yearStr);
			int year = actualYear - incremento;
			if (mascPrefixTemp.equals("aamm") || mascPrefixTemp.equals("aa"))
				yearStr = String.valueOf(year).substring(2);
			else
				yearStr = String.valueOf(year).substring(3);
			maxValue = yearStr + monthStr;
			if (mascPrefixTemp.equals("aamm") || mascPrefixTemp.equals("amm"))
				maxValue += monthStr;
		}
		return maxValue;
	}

	private static HashSet<Integer> getDirectSpecialized(HashSet<Integer> idtoPadre, FactoryConnectionDB factConnDB) throws SQLException, NamingException {
		HashSet<Integer> hSpec = AuxiliarModel.getSpecialized(idtoPadre, factConnDB);
		//System.out.println("hSpec " + Auxiliar.hashSetIntegerToString(hSpec, ","));

		HashSet<Integer> hSpecResto = (HashSet<Integer>)hSpec.clone();
		hSpecResto.remove(idtoPadre);
		//ver si son superiores directos
		HashSet<Integer> hDirectSpec = new HashSet<Integer>();
		Iterator<Integer> it = hSpec.iterator();
		while (it.hasNext()) {
			Integer idto = it.next();
			//si idto especializa de sus hermanos -> no es directo
			if (!AuxiliarModel.isSpecialized(idto, hSpecResto, factConnDB))
				hDirectSpec.add(idto);
		}
		return hDirectSpec;
	}

	private static void getSpecializedNoIndex(HashSet<Integer> hc, HashSet<Integer> allSpec, 
			FactoryConnectionDB factConnDB, HashSet<Integer> idtosIndex) throws SQLException, NamingException {
		allSpec.addAll(hc);
		//busqueda de especializados
		HashSet<Integer> directSpecialized = getDirectSpecialized(hc, factConnDB);
		//System.out.println("hc " + Auxiliar.hashSetIntegerToString(hc, ","));
		//System.out.println("directSpecialized " + Auxiliar.hashSetIntegerToString(directSpecialized, ","));
		HashSet<Integer> specializedNextLevel = new HashSet<Integer>();
		Iterator<Integer> it = directSpecialized.iterator();
		while (it.hasNext()) {
			Integer idtoSpec = it.next();
			//si no tiene indice va a allSup y al siguiente nivel
			if (!idtosIndex.contains(idtoSpec)) {
				specializedNextLevel.add(idtoSpec);
			}
		}
		if (specializedNextLevel.size()>0)
			getSpecializedNoIndex(specializedNextLevel, allSpec, factConnDB, idtosIndex);
	}
	
	public static int getActualIndex(Integer c, String prefix, String sufix, Integer propPrefix, Integer propSufix,
			String mascPrefixTemp, Integer propPrefixTemp, Integer contYear, String lastPrefixTemp, Integer miEmpresa, 
			FactoryConnectionDB factConnDB, HashSet<Integer> idtosIndex, DataBaseMap dataBaseMap) throws NamingException, SQLException, DataErrorException, NoSuchColumnException {
		//System.out.println("Inicio de la funcion getActualIndex");
		int indexAct=0;
		if (miEmpresa==null || miEmpresa>0) {
			String sql;
			HashSet<Integer> allSpec = new HashSet<Integer>();
			HashSet<Integer> hc = new HashSet<Integer>();
			hc.add(c);
			//System.out.println("idtosIndex " + Auxiliar.hashSetIntegerToString(idtosIndex, ","));
			getSpecializedNoIndex(hc, allSpec, factConnDB, idtosIndex);
			//antes buscaba en O_Reg_Instancias_Index
			
			Iterator<Integer> it = allSpec.iterator();
			while (it.hasNext()) {
				int idto = it.next();
				GenerateSQL generateSQL = new GenerateSQL(factConnDB.getGestorDB());
				IQueryInfo tableView = dataBaseMap.getView(idto);
				if (tableView==null)
					tableView = dataBaseMap.getTable(idto);
				String tableName = generateSQL.getCharacterBegin() + tableView.getName() + generateSQL.getCharacterEnd();
				String tableIdName = generateSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd();
				
				sql = "SELECT " + Constants.PROP_RDN + ", " + tableIdName + " FROM " + tableName;
				if (miEmpresa!=null) {
					List<String> columnsName = tableView.getColumnNamesContainingProperty(AuxiliarModel.getPropertyByName(Constants.prop_mi_empresa, factConnDB));
					if (columnsName==null || columnsName.size()==0 || columnsName.get(0)==null)
						throw new DataErrorException("No existe una columna para la property " + Constants.prop_mi_empresa + " en la tabla/vista " + tableView.getName());
					String columnNameNoCharacter = columnsName.get(0);
					String columnName = generateSQL.getCharacterBegin() + columnNameNoCharacter + generateSQL.getCharacterEnd();
					
					sql += " WHERE " + columnName + "=" + QueryConstants.getTableId(miEmpresa);
				}
				
				boolean hasPropPrefixTemp = propPrefixTemp!=null;
				boolean hasPropPrefix = propPrefix!=null;
				boolean hasPropSufix = propSufix!=null;
				if(prefix!=null) {
					if (miEmpresa!=null)
						sql += " AND ";
					else
						sql += " WHERE ";
					if(hasPropPrefix || hasPropPrefixTemp)
						sql += Constants.PROP_RDN + " like '%"+prefix+"%'";
					else
						sql += Constants.PROP_RDN + " like '"+prefix+"%'";
				}
				if(sufix!=null) {
					if (miEmpresa!=null || prefix!=null)
						sql += " AND ";
					else
						sql += " WHERE ";
					if(hasPropSufix)
						sql += Constants.PROP_RDN + " like '%"+sufix+"%'";
					else
						sql += Constants.PROP_RDN + " like '%"+sufix+"'";
				}
				System.out.println(sql);
				Statement st = null; 
				ResultSet rs = null;
				ConnectionDB con = null;
				try {
					con = factConnDB.createConnection(true);
					st = con.getBusinessConn().createStatement();
					rs = st.executeQuery(sql);
					while (rs.next()) {
						String rdn = rs.getString(1);
						System.out.println("rdn " + rdn);
						Integer ido = QueryConstants.getIdo(rs.getInt(2), idto);
		
						boolean continuar = true;
						
						if (hasPropPrefixTemp) {
							//buscar el valor en bd para este ido y la property propPrefixTemp
							String valuePropPrefixTemp = dBValueOfIdoProp(ido, idto, propPrefixTemp, factConnDB, dataBaseMap);
							if (valuePropPrefixTemp!=null) {
								//obtener valor
								ArrayList<Object> returned = new ArrayList<Object>();
								int result = getValueTemp(valuePropPrefixTemp, mascPrefixTemp, contYear, lastPrefixTemp, returned);
								if (result==NOTHING) {
									String valueTemp = (String)returned.get(0);
									if (valueTemp!=null) {
										int sizePropPrefixTemp = valueTemp.length();
										if (rdn.length()>sizePropPrefixTemp) {
											String propPrefixTempRdn = rdn.substring(0, sizePropPrefixTemp);
											if (propPrefixTempRdn.equals(valueTemp)) {
												rdn = rdn.substring(sizePropPrefixTemp, rdn.length());
												System.out.println("rdn sin propPrefixTemp " + rdn);
											} else
												continuar = false;
										} else
											continuar = false;
									} else
										continuar = false;
								} else
									continuar = false;
							} else
								continuar = false;
						}
						if (continuar && hasPropPrefix) {
							if (propPrefix.equals(Constants.IdPROP_RDN) && prefix!=null) {
								//value = rdn
								int ind = rdn.indexOf(prefix);
								rdn = rdn.substring(ind, rdn.length());
								System.out.println("rdn sin propPrefix " + rdn);
							} else {
								//buscar el valor en bd para este ido y la property propPrefix
								String value = dBValueOfIdoProp(ido, idto, propPrefix, factConnDB, dataBaseMap);
								if (value!=null) {
									int sizePropPrefix = value.length();
									if (rdn.length()>sizePropPrefix) {
										String propPrefixRdn = rdn.substring(0, sizePropPrefix);
										if (propPrefixRdn.equals(value)) {
											rdn = rdn.substring(sizePropPrefix, rdn.length());
											System.out.println("rdn sin propPrefix " + rdn);
										} else
											continuar = false;
									} else
										continuar = false;
								} else
									continuar = false;
							}
						}
						if (continuar && prefix!=null && (hasPropPrefix || hasPropPrefixTemp)) {
							//chequear que lo siguiente se corresponde con prefijo (si tiene)
							int sizePrefix = prefix.length();
							if (rdn.length()>sizePrefix) {
								String prefixRdn = rdn.substring(0, sizePrefix);
								if (prefixRdn.equals(prefix)) {
									rdn = rdn.substring(sizePrefix, rdn.length());
									System.out.println("rdn sin prefix " + rdn);
								} else
									continuar = false;
							} else
								continuar = false;
						}
						if (continuar) {
							if(prefix!=null && !hasPropPrefix && !hasPropPrefixTemp) {
								int sizePrefix = prefix.length();
								rdn = rdn.substring(sizePrefix, rdn.length());
								System.out.println("rdn sin prefix " + rdn);
							}
							if(sufix!=null) {
								int sizeSufix = sufix.length();
								rdn = rdn.substring(0, rdn.length()-sizeSufix);
								System.out.println("rdn sin sufix " + rdn);
							}
							//System.out.println("rdn " + rdn);
							if (Auxiliar.hasIntValue(rdn)) {
								int indexAux=Integer.parseInt(rdn);
								indexAct=Math.max(indexAct, indexAux);
							} //else
								//System.out.println("no es numero");
						}
						/*String aux="";
						int i=rdn.length()-1;
						while(i>=0){//Recorremos el rdn para quedarnos solo con el numero si lo tuviera
							if(Character.isDigit(rdn.charAt(i)))
								aux=rdn.charAt(i)+aux;
							else if(!aux.isEmpty())
								i=0;//Con esto saldriamos del bucle
							i--;
						}
						if(!aux.isEmpty()){
							int indexAux=Integer.parseInt(aux);
							indexAct=Math.max(indexAct, indexAux);
						}*/
					}
				}finally{
					if(rs!=null)
						rs.close();
					if(st!=null)
						st.close();
					if(con!=null)
						factConnDB.close(con);
				}
			}
		}
		//System.out.println("Fin de la funcion getActualIndex");
		return indexAct;
	}
	private static String dBValueOfIdoProp(int ido, int idto, int prop, FactoryConnectionDB factConnDB, DataBaseMap dataBaseMap) 
			throws SQLException, NamingException, DataErrorException, NoSuchColumnException {
		String value = null;
		
		GenerateSQL generateSQL = new GenerateSQL(factConnDB.getGestorDB());
		IQueryInfo tableView = dataBaseMap.getView(idto);
		if (tableView==null)
			tableView = dataBaseMap.getTable(idto);
		String tableName = generateSQL.getCharacterBegin() + tableView.getName() + generateSQL.getCharacterEnd();
		String tableIdName = generateSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd();
		
		List<String> columnsName = tableView.getColumnNamesContainingProperty(prop);
		if (columnsName==null || columnsName.size()==0 || columnsName.get(0)==null)
			throw new DataErrorException("No existe una columna para la property " + AuxiliarModel.getPropertyName(prop, factConnDB) + " en la tabla/vista " + tableView.getName());
		String columnNameNoCharacter = columnsName.get(0);
		String columnName = generateSQL.getCharacterBegin() + columnNameNoCharacter + generateSQL.getCharacterEnd();
		
		String sql = "select " + columnName + " from " + tableName + 
			"WHERE " + tableIdName + "=" + QueryConstants.getTableId(ido);
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				value = String.valueOf(rs.getObject(1));
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return value;
	}
}
