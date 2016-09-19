/*package dynagent.ejb;// 14-10-02 V1 JOB Añado findElementByAt


import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Calendar;
import dynagent.ejb.helperConstant;


public class dateUtil{
	    public final static int BEFORE_TODAY=1;
    	    public final static int TODAY=2;
	    public final static int AFTER_TODAY=3;

	static public int compareToday( String fecha ) throws ParseException{
		if(fecha==null) throw new ParseException("FECHA ES NULA",0);
		String sep="-";
		if( fecha.indexOf("/")>0) sep="/";

		SimpleDateFormat sdf= new SimpleDateFormat("dd" + sep + "MM" + sep + "yy hh:mm");
		java.util.Date dia = sdf.parse(fecha);
		return compareToday( dia );
	}

	static public int compareToday( long fecha ) throws ParseException{
		return compareToday( new java.util.Date( fecha ) );
	}

	static public int compareToday( java.util.Date dia ) throws ParseException{
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
		System.out.println(" AFTER TODAY:" +res + " en fecha " + dia);
		System.out.println(" YEAR, CURRYEAR:"  + year + "," + currYear);
		System.out.println(" DAY, CURRDAY:" + dayY + "," + currDayY);
		return res;
	}

	static public boolean equals( String fechaA, String fechaB ) throws ParseException{
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

	static public java.util.Date parseFecha( String fecha ) throws ParseException{
	    if(fecha==null) throw new ParseException("FECHA ES NULA",0);
            String sep="-",patron="";
            if( fecha.indexOf("/")>0) sep="/";
            if( fecha.matches("(\\d{1,2}"+sep+"\\d{1,2}"+sep+"\\d{2}(\\d{2})?)") ){
                    System.out.println("MATCH1");
                    patron= "dd" + sep + "MM" + sep + "yy";
            }else{
                    patron="dd" + sep + "MM" + sep + "yy hh:mm";
            }
            SimpleDateFormat sdf= new SimpleDateFormat(patron);
	    java.util.Date dia = sdf.parse(fecha);
	    return dia;
	}

        public static String getSqlExeDate( long dd ){
            if( dd==0 ) return null;
            SimpleDateFormat sdf= new SimpleDateFormat("dd/MM/yy hh:mm");
            java.util.Date dia = new Date(dd);
            return sdf.format( dia );
	}

        public static String getSqlExeDate( Date dia ){
            if( dia==null ) return null;
            SimpleDateFormat sdf= new SimpleDateFormat("dd/MM/yy hh:mm");
            return sdf.format( dia );
	}
}


*/