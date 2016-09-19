package gdev.gawt.utils;

import gdev.gen.GConst;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class UtilsFields {
	public final static int BEFORE_TODAY=1;
	public final static int TODAY=2;
	public final static int AFTER_TODAY=3;

	public static String parseBooleanTextValue(int typeField, String val){
		if (typeField == GConst.TM_BOOLEAN)
			return null;

		if (typeField == GConst.TM_BOOLEAN_COMMENTED && val != null	&& val.length() > 0) {
			String[] buff = val.split(":");
			if (buff.length > 1)
				return buff[1]!=null && !buff[1].equals("null")?buff[1]:null;
		}
		return null;
	}

	public static Boolean parseBooleanValue(int typeField, String val) throws ParseException {
		if (typeField == GConst.TM_BOOLEAN){
			if(val!=null){
				String[] buff = val.split(":");
				return (Boolean) parseValue(GConst.TM_BOOLEAN, buff[0]);
			}else return null;
		}

		if (typeField == GConst.TM_BOOLEAN_COMMENTED && val != null	&& val.length() > 0) {
			if (val.indexOf("#NULLVALUE#") >= 0)
				return null;
			else {
				String[] buff = val.split(":");
				return (Boolean) parseValue(GConst.TM_BOOLEAN, buff[0]);
			}
		} else
			return null;
	}


	public static Object parseValue(int typeField, String val) throws ParseException {
		if (val == null || val.length() == 0)
			return null;
		if (typeField == GConst.TM_ENUMERATED) {
			if (val.indexOf(";") == -1)
				return new Integer(val.indexOf(".") == -1 ? val : val
						.substring(0, val.indexOf(".")));
			else {
				String[] lista = val.split(";");
				ArrayList<Integer> res = new ArrayList<Integer>();
				for (int i = 0; i < lista.length; i++)
					res.add(new Integer(lista[i]));
				return res;
			}
		}
		/*if (typeField == GConst.TM_INTEGER || typeField == GConst.TM_REAL) {
			if (val.matches(".+[\\s-:].+")) {
				String[] part = val.split("[\\s-:]");
				return new range((typeField == GConst.TM_INTEGER ? (Object) parseInt(part[0])
						: (Object) parseReal(part[0])),
						(typeField == GConst.TM_INTEGER ? (Object) parseInt(part[1])
								: (Object) parseReal(part[1])));
			}
		}*/
		if (typeField == GConst.TM_INTEGER)
			return new Integer(val.indexOf(".") == -1 ? val : val.substring(0,
					val.indexOf(".")));
		if (typeField == GConst.TM_REAL)
			return new Double(val);
		if (typeField == GConst.TM_TEXT || typeField == GConst.TM_MEMO || typeField == GConst.TM_IMAGE)
			return val;
		if (typeField == GConst.TM_BOOLEAN)
			return new Boolean((val.toUpperCase().equals("TRUE") || val.equals("1") || val.equals("-1")));
		/*if (typeField == GConst.TM_BOOLEAN_COMMENTED) {
			try {
				return new extendedValue(parseBooleanValue(typeField, val),
						parseBooleanTextValue(typeField, val));
			} catch (DataErrorException de) {
				de.printStackTrace();
				throw new ParseException(de.getMessage(), 0);
			}
		}*/
		if (typeField == GConst.TM_DATE || typeField == GConst.TM_DATE_HOUR)
			return parseFecha(val);
		else
			throw new ParseException("TM NO CONOCIDO " + typeField, 0);
	}

	public static Integer parseInt(String val) {
		return new Integer(val.indexOf(".") == -1 ? val : val.substring(0, val.indexOf(".")));
	}

	public static Double parseReal(String val) {
		return new Double(val);
	}

	public static Date parseFecha( String fecha ) throws ParseException{
		if(fecha==null) throw new ParseException("FECHA ES NULA",0);
		String sep="-",patron="";
		if( fecha.indexOf("/")>0) sep="/";
		if( fecha.matches("(\\d{1,2}"+sep+"\\d{1,2}"+sep+"\\d{2}(\\d{2})?)") ){
			//System.outprintln("MATCH1");
			patron= "dd" + sep + "MM" + sep + "yyyy";
		}else{
			patron="dd" + sep + "MM" + sep + "yyyy hh:mm";
		}
		SimpleDateFormat sdf= new SimpleDateFormat(patron);
		java.util.Date dia = sdf.parse(fecha);
		return dia;
	}


	public static boolean equals(int typeField, String valA, String valB) throws ParseException {

		if (isNull(typeField, valA) && isNull(typeField, valB))
			return true;
		if (isNull(typeField, valA) && !isNull(typeField, valB))
			return false;
		if (!isNull(typeField, valA) && isNull(typeField, valB))
			return false;

		if ((typeField == GConst.TM_ENUMERATED || typeField == GConst.TM_INTEGER || typeField == GConst.TM_REAL)
				&& (valA.matches(".+[;\\s].+") || valB.matches(".+[;\\s].+"))) {
			if (valA.matches(".+[;\\s].+") != valB.matches(".+[;\\s].+"))
				return false;
			if (valA.indexOf(";") >= 0 && valB.indexOf(";") >= 0) {
				String[] lisA = valA.split(";");
				String[] lisB = valB.split(";");
				if (lisA.length != lisB.length)
					return false;
				else {
					int[] intA = new int[lisA.length];
					int[] intB = new int[lisA.length];
					for (int i = 0; i < lisA.length; i++) {
						intA[i] = Integer.parseInt(lisA[i]);
						intB[i] = Integer.parseInt(lisB[i]);
					}
					Arrays.sort(intA);
					Arrays.sort(intB);
					for (int i = 0; i < intA.length; i++)
						if (intA[i] != intB[i])
							return false;
					return true;
				}
			}
			if (valA.matches(".+\\s.+"))
				return valA.equals(valB);
			else
				throw new ParseException(
						"helperConstant.equals, not match tm,vaA,valB " + typeField
						+ "," + valA + "," + valB, 0);
		}
		if (typeField == GConst.TM_ENUMERATED || typeField == GConst.TM_INTEGER) {
			int vA = Integer.parseInt((valA.indexOf(".") == -1 ? valA : valA
					.substring(0, valA.indexOf("."))));
			int vB = Integer.parseInt((valB.indexOf(".") == -1 ? valB : valB
					.substring(0, valB.indexOf("."))));
			return vA == vB;
		}
		if (typeField == GConst.TM_REAL) {
			double vA = Double.parseDouble(valA);
			double vB = Double.parseDouble(valB);
			return vA == vB;
		}
		if (typeField == GConst.TM_TEXT || typeField == GConst.TM_MEMO || typeField == GConst.TM_IMAGE)
			return valA.equals(valB);

		if (typeField == GConst.TM_BOOLEAN) {
			boolean vA = valA.equals("1");
			boolean vB = valB.equals("1");
			return vA == vB;
		}
		if (typeField == GConst.TM_BOOLEAN_COMMENTED) {
			Boolean vA = parseBooleanValue(GConst.TM_BOOLEAN_COMMENTED, valA);
			Boolean vB = parseBooleanValue(GConst.TM_BOOLEAN_COMMENTED, valB);
			if (!vA.equals(vB))
				return false;
			String tA = parseBooleanTextValue(GConst.TM_BOOLEAN_COMMENTED, valA);
			String tB = parseBooleanTextValue(GConst.TM_BOOLEAN_COMMENTED, valB);
			if (tA == null && tB == null)
				return true;
			if (tA == null && tB != null || tA != null && tB == null)
				return false;
			return tA.equals(tB);
		}
		if (typeField == GConst.TM_DATE || typeField == GConst.TM_DATE_HOUR){
			String fechaA=valA;
			String fechaB=valB;
			java.util.Date dia=parseFecha( fechaA );
			Calendar cal= Calendar.getInstance();
			cal.setTime( dia );
			int dayY_A= cal.get( Calendar.DAY_OF_YEAR );
			int year_A= cal.get( Calendar.YEAR );
			dia = parseFecha( fechaB);
			cal.setTime( dia );
			int dayY_B= cal.get( Calendar.DAY_OF_YEAR );
			int year_B= cal.get( Calendar.YEAR );
			return year_A == year_B &&  dayY_A == dayY_B ;
		}
		return false;
	}

	public static boolean equals(Object valA, Object valB) {
		if (valA == null && valB != null)
			return false;
		if (valA != null && valB == null)
			return false;
		if (valA == null && valB == null)
			return true;
		if (valA instanceof ArrayList && !(valB instanceof ArrayList)
				|| !(valA instanceof ArrayList) && valB instanceof ArrayList)
			return false;
		return valA.equals(valB);
	}

	public static boolean equals(int tm, Object valA, Object valB) {
		if (isNull(tm, valA) && isNull(tm, valB))
			return true;
		if (isNull(tm, valA) && !isNull(tm, valB))
			return false;
		if (!isNull(tm, valA) && isNull(tm, valB))
			return false;
		if (valA instanceof ArrayList && !(valB instanceof ArrayList)
				|| !(valA instanceof ArrayList) && valB instanceof ArrayList)
			return false;
		return valA.equals(valB);
	}

	static boolean isNull(int tm, Object val) {
		if (val == null)
			return true;
		if (tm != GConst.TM_ENUMERATED)
			return val instanceof String
			&& (((String) val).length() == 0 || ((String) val)
					.equals("#NULLVALUE#"));
		else {
			if (val instanceof String
					&& (((String) val).length() == 0
							|| ((String) val).equals("#NULLVALUE#") || ((String) val)
							.equals("0")))
				return true;
			if (val instanceof Double && ((Double) val).intValue() == 0)
				return true;
			if (val instanceof Integer && ((Integer) val).intValue() == 0)
				return true;
			else
				return false;
		}
	}

	public static int compareToday( String fecha ) throws ParseException{
		if(fecha==null) throw new ParseException("FECHA ES NULA",0);
		String sep="-";
		if( fecha.indexOf("/")>0) sep="/";

		SimpleDateFormat sdf= new SimpleDateFormat("dd" + sep + "MM" + sep + "yyyy hh:mm");
		java.util.Date dia = sdf.parse(fecha);
		return compareToday( dia );
	}

	public static int compareToday( long fecha ){
		return compareToday( new java.util.Date( fecha ) );
	}

	public static int compareToday( java.util.Date dia ){
		Calendar cal= Calendar.getInstance();
		cal.setTime( dia );
		int dayY= cal.get( Calendar.DAY_OF_YEAR );
		int year= cal.get( Calendar.YEAR );
		cal.setTimeInMillis( System.currentTimeMillis() );
		int currDayY= cal.get( Calendar.DAY_OF_YEAR );
		int currYear= cal.get( Calendar.YEAR );
		if(year > currYear || (year == currYear && dayY > currDayY ))
			return AFTER_TODAY;
		else
			if(year == currYear && dayY == currDayY )
				return TODAY;
			else
				return BEFORE_TODAY;
		/*System.out.println(" AFTER TODAY:" +res + " en fecha " + dia);
		System.out.println(" YEAR, CURRYEAR:"  + year + "," + currYear);
		System.out.println(" DAY, CURRDAY:" + dayY + "," + currDayY);
		return res;*/
	}

//	public class extendedValue extends Object implements Serializable{
//	String comment=null;
//	Object value=null;

//	public extendedValue( Object val, String comment ) throws DataErrorException{
//	if( val==null )
//	throw new DataErrorException("Error value null in extended val constructor");
//	this.comment=comment;
//	value=val;
//	}
//	public String getComment(){
//	return comment;
//	}
//	public Object getValue(){
//	return value;
//	}
//	public boolean equals( Object b ){
//	if (!(b instanceof extendedValue))return false;
//	extendedValue eb = (extendedValue) b;
//	if (value == null || eb.value == null ||
//	comment == null && eb.comment != null ||
//	eb.comment == null && comment != null ||
//	comment!=null && eb.comment!=null && !comment.equals(eb.comment))
//	return false;
//	return value.equals(eb.value);
//	}
//	}

//	public class range extends Object implements Serializable{
//	public Object min,max;
//	public range(Object min, Object max){
//	this.min=min;
//	this.max=max;
//	}


//	public String toString(){
//	return min.toString()+" "+max.toString();
//	}
//	}
}
