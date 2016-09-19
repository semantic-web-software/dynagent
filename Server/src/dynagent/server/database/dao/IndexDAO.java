package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Index;
import dynagent.common.basicobjects.IndexName;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.exceptions.DataErrorException;
import dynagent.server.database.IndividualCreator;
import dynagent.server.gestorsDB.GenerateSQL;

public class IndexDAO extends ObjectDAO{

	public IndexDAO(){
		super("S_INDEX",true);
	}
	
	public void insert(IndexName in, String sufixIndex) throws SQLException, DataErrorException, NamingException{
		//insertar en o_datos_atrib
		O_Datos_AttribDAO odatDAO = new O_Datos_AttribDAO();
		TClaseDAO tdao = new TClaseDAO();
		PropertiesDAO propDAO = new PropertiesDAO();
		
		Integer idtoIndex = tdao.getTClaseByName(Constants.CLS_INDICE).getIDTO();
		int ido = 0;
		if (in.getDomain().equals(Constants.CLS_INDICE)) {
			GenerateSQL gSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
			//el rdn se genera a partir de un indice
			String rdn = "1";
			ido = IndividualCreator.creator(DAOManager.getInstance().getFactConnDB(), DAOManager.getInstance().isCommit(), gSQL, idtoIndex, rdn, null, sufixIndex, null,false,null).getIdo();
		} else {
			GenerateSQL gSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
			//el rdn se genera a partir de un indice
			ido = IndividualCreator.creator(DAOManager.getInstance().getFactConnDB(), DAOManager.getInstance().isCommit(), gSQL, idtoIndex, null, null, sufixIndex, null,false,null).getIdo();
		}
		
		//insertar el resto de las DataProperties
		int propDominio = propDAO.getIdPropByName(Constants.PROP_DOMINIO);
		O_Datos_Attrib inDominio = new O_Datos_Attrib(idtoIndex, ido, propDominio, null, in.getDomain().replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, null);
		odatDAO.insert(inDominio);
		
		int propIndex = propDAO.getIdPropByName(Constants.PROP_INICIO_CONTADOR);
		O_Datos_Attrib inIndex = new O_Datos_Attrib(idtoIndex, ido, propIndex, null, null, Constants.IDTO_INT, Double.parseDouble(String.valueOf(in.getIndex())), Double.parseDouble(String.valueOf(in.getIndex())), null, null);
		odatDAO.insert(inIndex);

		if (in.getPrefix()!=null) {
			int propPrefix = propDAO.getIdPropByName(Constants.PROP_PREFIJO);
			O_Datos_Attrib inPrefix = new O_Datos_Attrib(idtoIndex, ido, propPrefix, null, in.getPrefix().replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, null);
			odatDAO.insert(inPrefix);
		}
		if (in.getSufix()!=null) {
			int propSufix = propDAO.getIdPropByName(Constants.PROP_SUFIJO);
			O_Datos_Attrib inSufix = new O_Datos_Attrib(idtoIndex, ido, propSufix, null, in.getSufix().replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, null);
			odatDAO.insert(inSufix);
		}
		if (in.getPropFilter()!=null) {
			int propPropFilter = propDAO.getIdPropByName(Constants.PROP_CAMPO_FILTRO);
			O_Datos_Attrib inPropFilter = new O_Datos_Attrib(idtoIndex, ido, propPropFilter, null, in.getPropFilterName().replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, null);
			odatDAO.insert(inPropFilter);
		}
		if (in.getValueFilter()!=null) {
			int propValueFilter = propDAO.getIdPropByName(Constants.PROP_VALOR_FILTRO);
			O_Datos_Attrib inValueFilter = new O_Datos_Attrib(idtoIndex, ido, propValueFilter, null, in.getValueFilter().replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, null);
			odatDAO.insert(inValueFilter);
		}
		if (in.getPropPrefix()!=null) {
			int propPropPrefix = propDAO.getIdPropByName(Constants.PROP_CAMPO_EN_PREFIJO);
			O_Datos_Attrib inPropPrefix = new O_Datos_Attrib(idtoIndex, ido, propPropPrefix, null, in.getPropPrefixName().replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, null);
			odatDAO.insert(inPropPrefix);
		}
		int globalSufixInt = 0;
		if (in.isGlobalSufix())
			globalSufixInt = 1;
		
		int propGlobalSufix = propDAO.getIdPropByName(Constants.PROP_SUFIJO_GLOBAL);
		O_Datos_Attrib inPropGlobalSufix = new O_Datos_Attrib(idtoIndex, ido, propGlobalSufix, null, null, Constants.IDTO_BOOLEAN, Double.parseDouble(String.valueOf(globalSufixInt)), Double.parseDouble(String.valueOf(globalSufixInt)), null, null);
		odatDAO.insert(inPropGlobalSufix);
		
		
		if (in.getMascPrefixTemp()!=null) {
			int propMascPrefixTemp = propDAO.getIdPropByName(Constants.PROP_MASC_PREFIX_TEMP);
			O_Datos_Attrib inMascPrefixTemp = new O_Datos_Attrib(idtoIndex, ido, propMascPrefixTemp, null, in.getMascPrefixTemp().replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, null);
			odatDAO.insert(inMascPrefixTemp);
		}
		if (in.getPropPrefixTemp()!=null) {
			int propPropPrefixTemp = propDAO.getIdPropByName(Constants.PROP_CAMPO_EN_PREFIJO_TEMP);
			O_Datos_Attrib inPropPrefixTemp = new O_Datos_Attrib(idtoIndex, ido, propPropPrefixTemp, null, in.getPropPrefixTempName().replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, null);
			odatDAO.insert(inPropPrefixTemp);
		}
		if (in.getContYear()!=null) {
			int propContYear = propDAO.getIdPropByName(Constants.PROP_CONTADOR_AÑO);
			O_Datos_Attrib inContYear = new O_Datos_Attrib(idtoIndex, ido, propContYear, null, null, Constants.IDTO_INT, Double.parseDouble(String.valueOf(in.getContYear())), Double.parseDouble(String.valueOf(in.getContYear())), null, null);
			odatDAO.insert(inContYear);
		}
		if (in.getLastPrefixTemp()!=null) {
			int propLastPrefixTemp = propDAO.getIdPropByName(Constants.PROP_ULTIMO_PREFIJO_TEMP);
			O_Datos_Attrib inLastPrefixTemp = new O_Datos_Attrib(idtoIndex, ido, propLastPrefixTemp, null, in.getLastPrefixTemp().replaceAll("'", "''"), Constants.IDTO_STRING, null, null, null, null);
			odatDAO.insert(inLastPrefixTemp);
		}

		if (in.getMinDigits()!=null) {
			int propMinDigits = propDAO.getIdPropByName(Constants.PROP_MIN_DIGITS);
			O_Datos_Attrib inMinDigits = new O_Datos_Attrib(idtoIndex, ido, propMinDigits, null, null, Constants.IDTO_INT, Double.parseDouble(String.valueOf(in.getMinDigits())), Double.parseDouble(String.valueOf(in.getMinDigits())), null, null);
			odatDAO.insert(inMinDigits);
		}
		if (in.getMiEmpresa()!=null) {
			int idtoMiEmpresa = (Integer)tdao.getByName(Constants.CLS_MI_EMPRESA).getFirst();
			int propMiEmpresa = propDAO.getIdPropByName(Constants.prop_mi_empresa);
			O_Datos_Attrib inMiEmpresa = new O_Datos_Attrib(idtoIndex, ido, propMiEmpresa, in.getMiEmpresa(), in.getMiEmpresaRdn().replaceAll("'", "''"), idtoMiEmpresa, null, null, null, null);
			odatDAO.insert(inMiEmpresa);
		}
	}
	
	/*public void update(String set, String where) throws SQLException{
		i.update(this, set, where);
	}*/
	
	public String getValues(Object v) {
		Index ind= (Index )v;
		
		String stringvalue = "";
	
		stringvalue += ind.getIdo()+ ",";	
		
		if(ind.getIdto() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getIdto()+ ",";	
		
		if(ind.getProperty()== null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getProperty()+ ",";
		
		if(ind.getMascPrefixTemp() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + ind.getMascPrefixTemp().replaceAll("'", "''")+ "',";
		if(ind.getPropPrefixTemp() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getPropPrefixTemp()+ ",";
		if(ind.getContYear()== null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getContYear()+ ",";
		if(ind.getLastPrefixTemp() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + ind.getLastPrefixTemp().replaceAll("'", "''")+ "',";
		
		if(ind.getPropPrefix() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getPropPrefix()+ ",";
		
		if(ind.getPrefix() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + ind.getPrefix().replaceAll("'", "''")+ "',";
		
		stringvalue += ind.getIndex()+ ",";
		
		if(ind.getSufix() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + ind.getSufix().replaceAll("'", "''")+ "',";

		if(ind.isGlobalSufix())
			stringvalue += "1,";
		else
			stringvalue += "0,";

		if(ind.getMinDigits() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getMinDigits()+ ",";
		
		if(ind.getPropFilter() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getPropFilter()+ ",";
		
		if(ind.getValueFilter() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + ind.getValueFilter().replaceAll("'", "''")+ "',";
		
		if(ind.getMiEmpresa() == null)
			stringvalue += "NULL";
		else
			stringvalue += ind.getMiEmpresa();
		
		return stringvalue;
		
	}
	
	public String getParams(){
		GenerateSQL generateSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		return " (ID_O, ID_TO, PROPERTY, " +
				"MASC_PREFIX_TEMP, PROPERTY_PREFIX_TEMP, CONT_YEAR, LAST_PREFIX_TEMP, " +
				"PROPERTY_PREFIX, PREFIX, " +
				generateSQL.getCharacterBegin() + "INDEX" + generateSQL.getCharacterEnd() + ", " +
				"SUFIX, GLOBAL_SUFIX, MIN_DIGITS, PROPERTY_FILTER, VALUE_FILTER, MI_EMPRESA) ";
	}
	
	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		Index ind= new Index();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0 && rs.getString(i+1)!=null)
				ind.setIdo(new Integer(rs.getString(i+1)));
			else if(i == 1 && rs.getString(i+1)!=null)
				ind.setIdto(new Integer(rs.getString(i+1)));
			else if (i == 2 && rs.getString(i+1)!=null)
				ind.setProperty(new Integer(rs.getString(i+1)));
			
			else if (i == 3 && rs.getString(i+1)!=null)
				ind.setMascPrefixTemp(rs.getString(i+1));
			else if (i == 4 && rs.getString(i+1)!=null)
				ind.setPropPrefixTemp(new Integer(rs.getString(i+1)));
			else if (i == 5 && rs.getString(i+1)!=null)
				ind.setContYear((int)rs.getDouble(i+1));
			else if (i == 6 && rs.getString(i+1)!=null)
				ind.setLastPrefixTemp(rs.getString(i+1));
			
			else if (i == 7 && rs.getString(i+1)!=null)
				ind.setPropPrefix(new Integer(rs.getString(i+1)));
			else if (i == 8 && rs.getString(i+1)!=null)
				ind.setPrefix(rs.getString(i+1));
			else if (i == 9 && rs.getString(i+1)!=null)
				ind.setIndex((int)rs.getDouble(i+1));
			else if (i == 10 && rs.getString(i+1)!=null)
				ind.setSufix(rs.getString(i+1));
			else if (i == 11 && rs.getString(i+1)!=null)
				ind.setGlobalSufix(rs.getBoolean(i+1));
			else if (i == 12 && rs.getString(i+1)!=null)
				ind.setMinDigits(new Integer(rs.getString(i+1)));
			
			else if (i == 13 && rs.getString(i+1)!=null)
				ind.setPropFilter(new Integer(rs.getString(i+1)));
			else if (i == 14 && rs.getString(i+1)!=null)
				ind.setValueFilter(rs.getString(i+1));
			else if (i == 15 && rs.getString(i+1)!=null)
				ind.setMiEmpresa(rs.getInt(i+1));
		}
		return ind;
	}
}
