package gdev.gawt.utils;

import gdev.gen.GConst;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import dynagent.common.Constants;
import dynagent.common.utils.Auxiliar;

public class TextVerifier extends InputVerifier {
	Pattern patron= null;
	String mask=null;
	boolean genericMask;
	public int sintax;
	boolean modoFilter;

	public static final char SEPARATOR_DATE='/';
	public static final char SEPARATOR_HOUR=':';
	public static final String SEPARATOR_RANGE="[\\s:]";
	public static final char SEPARATOR_RANGE_NUM=':';
	public static final char SEPARATOR_RANGE_DATE='-';
	public static final String MINMAX_NUMBER="[\\<\\>]";
	public static final String UNEQUAL_NUMBER="<>";

	public TextVerifier(String mask, int sintax, boolean modoFilter){
		//super();
		this.sintax=sintax;
		this.modoFilter=modoFilter;

		genericMask=mask==null;
		
		if( 	mask==null &&
				sintax==GConst.TM_INTEGER )
			//mask="(\\d{1,12}|(\\d{1,3}(\\.\\d{3}){1,3}))";
			mask="(\\-?)(\\d{1,12})";
		//mask="(\\-?)(\\d{1,12}|(\\d{1,3}(\\.\\d{3}){1,3}))";

		if( 	mask==null &&
				sintax==GConst.TM_REAL )
			//mask="(\\d{1,12}|(\\d{1,3}(\\.\\d{3}){1,3}))(,\\d*)?";
			mask="(\\-?)(\\d{1,12})([.,]\\d*)?";
		//mask="(\\-?)(\\d{1,12}|(\\d{1,3}(\\.\\d{1,3}){1,3}))(\\.\\d*)?";

		if( 	mask==null &&
				sintax==GConst.TM_DATE ){
			mask= getDateMask();
		}

		if( 	mask==null &&
				sintax==GConst.TM_DATE_HOUR ){
			mask= getDateHourMask();
		}
		
		if( 	mask==null &&
				sintax==GConst.TM_HOUR ){
			mask= getHourMask();
		}
		
		if( mask==null &&
				sintax==GConst.TM_TEXT){
			mask= ".{0,"+GConst.MAX_CHARACTERS_TEXT+"}";
		}

		if(	modoFilter ){
			if(sintax==GConst.TM_INTEGER ||	sintax==GConst.TM_REAL){
				mask= "("+MINMAX_NUMBER+mask+ ")|("+UNEQUAL_NUMBER+mask+ ")|("+mask+"("+SEPARATOR_RANGE+mask+")?)";
			}else if(sintax==GConst.TM_DATE || sintax==GConst.TM_DATE_HOUR)
				mask= mask+ "("+SEPARATOR_RANGE_DATE+mask+")?";
		}

		this.mask= mask;
		if(mask!=null)
			patron= Pattern.compile(mask);
	}
	
	public static String getDateMask(){
		String dia="(0[1-9])|([1-9])|([1-2][0-9])|(3[0-1])";
		String mes="(0[1-9])|([1-9])|(1[0-2])";
		//String año="([0-9])|([0-9][0-9])|([1-2][0-9][0-9][0-9])";
		String año="([1-2][0-9][0-9][0-9])";
		return "(" + dia + ")/("+mes+")/("+año+")";
	}
	
	public static String getDateHourMask(){
		String dia="(0[1-9])|([1-9])|([1-2][0-9])|(3[0-1])";
		String mes="(0[1-9])|([1-9])|(1[0-2])";
		//String año="([0-9])|([0-9][0-9])|([1-2][0-9][0-9][0-9])";
		String año="([1-2][0-9][0-9][0-9])";
		String hora="(0[0-9])|([0-9])|(1[0-9])|(2[0-3])";
		String min="([0-5]+[0-9])";
		String seg="([0-5]+[0-9])";
		return "(" + dia + ")/("+mes+")/("+año+") ("+hora+"):("+min+"):("+seg+")";
	}
	
	public static String getHourMask(){
		String hora="(0[0-9])|([0-9])|(1[0-9])|(2[0-3])";
		String min="([0-5]+[0-9])";
		String seg="([0-5]+[0-9])";
		return "("+hora+"):("+min+"):("+seg+")";
	}
	
	public String buildMessageError(String label){
		String msg="Valor incorrecto en el campo "+ label;

		if(genericMask){
			if( sintax==GConst.TM_DATE){
				msg+=". Ejemplo de formato: '23/11/2010'";
			}else if(sintax==GConst.TM_DATE_HOUR){
				msg+=". Ejemplo de formato: '23/11/2010 13:32:30'";
			}else if(sintax==GConst.TM_HOUR){
				msg+=". Ejemplo de formato: '13:32:30'";
			}else if(sintax==GConst.TM_INTEGER){
				msg+=". Ejemplo de formato: '25'";
				if(modoFilter)
					msg+=", '>25', '<25', '25:30'";
			}else if(sintax==GConst.TM_REAL){
				msg+=". Ejemplo de formato: '5.0'";
				if(modoFilter)
					msg+=", '>5.0', '<5.0', '5.0:8.0'";
			}else if(sintax==GConst.TM_TEXT){
				msg+=". Un campo de texto no puede tener un tamaño mayor a "+GConst.MAX_CHARACTERS_TEXT;
			}else if(sintax==GConst.TM_FILE || sintax==GConst.TM_IMAGE){
				msg+=". No se encuentra el archivo indicado";
			}
		}else if(mask!=null){
			msg+=".\nNo cumple la máscara definida: "+mask;
		}
		
		return msg;
	}

	// Es llamado cuando el usuario edita un campo, al salir de este. Si devuelve true se llama al metodo
	// encargado del evento(actionPerformed,focusLost...)
	public boolean verify(JComponent input) {
		JTextComponent text= (JTextComponent)input;
		boolean exito=true;
		if (text.getText() == null || text.getText().length()==0) return true;
		/*switch(sintax) {
			case GConst.TM_TEXT:
				if(mask!=null){
					Matcher matcher= patron.matcher(text.getText());
					exito= matcher.matches();
					if(exito)
						text.setText(format(text.getText()));
				}
				break;
			case GConst.TM_REAL:{
				Matcher matcher= patron.matcher(text.getText());
				exito= matcher.matches();
				if(exito)
					text.setText(format(text.getText()));
				break;
			}
			case GConst.TM_INTEGER:{
				Matcher matcher= patron.matcher(text.getText());
				exito= matcher.matches();
				if(exito)
					text.setText(format(text.getText()));
				break;
			}
			case GConst.TM_DATE:{
				Matcher matcher= patron.matcher(text.getText());
				exito= matcher.matches();
				if( exito ){
					text.setText(format(text.getText()));
				}
				break;
			}
			case GConst.TM_DATE_HOUR:{
			    Matcher matcher= patron.matcher(text.getText());
			    exito= matcher.matches();
			    if( exito ){
			    	text.setText(format(text.getText()));         
                }
			    break;
			}
		}*/
		if(patron!=null && ((sintax==GConst.TM_TEXT && mask!=null) || (sintax==GConst.TM_MEMO && mask!=null) || (sintax!=GConst.TM_TEXT && sintax!=GConst.TM_MEMO) || (sintax==GConst.TM_DATE && mask!=null) || (sintax==GConst.TM_DATE_HOUR && mask!=null || (sintax==GConst.TM_HOUR && mask!=null)))){
			Matcher matcher= patron.matcher(text.getText());
			exito= matcher.matches();
			if(exito){
				try{
					String formatText=format(text.getText());
					if(!Auxiliar.equals(formatText, text.getText())){//No hacemos el setText sin este chequeo ya que el setText provoca cambio de posicion del cursor aunque el texto sea el mismo
						text.setText(formatText);
					}
				}catch(NumberFormatException e){
					return false;
				}
			}
		}

		return exito;
	}

	public String format( Object val ){
		if( val==null || ( val instanceof String && ((String)val).length()==0 )){
			//return "";
			return null;
		}
		if( sintax==GConst.TM_REAL){
			if( val instanceof String ){
				if( modoFilter && ((String)val).matches(".+"+SEPARATOR_RANGE+".+") ){
					String[] part=((String)val).split(SEPARATOR_RANGE);
					String val0=part[0].equals("null")?null:String.valueOf(parseReal( part[0]));
					String val1=part[1].equals("null")?null:String.valueOf(parseReal( part[1]));
					return 	val0+SEPARATOR_RANGE_NUM+val1;
				}else if( modoFilter && ((String)val).matches(UNEQUAL_NUMBER+".+") ){
					return ""+((String)val).charAt(0)+((String)val).charAt(1)+String.valueOf(parseReal( ((String)val).substring(2)));
				}else if( modoFilter && ((String)val).matches(MINMAX_NUMBER+".+") ){
					return ((String)val).charAt(0)+String.valueOf(parseReal( ((String)val).substring(1)));
				}
				return String.valueOf(parseReal((String)val));
			}
			return val.toString();
		}
		if(sintax==GConst.TM_INTEGER ){
			if( val instanceof String ){				
				if( modoFilter && ((String)val).matches(".+"+SEPARATOR_RANGE+".+") ){
					String[] part=((String)val).split(SEPARATOR_RANGE);
					String val0=part[0].equals("null")?null:String.valueOf(parseInteger( part[0]));
					String val1=part[1].equals("null")?null:String.valueOf(parseInteger( part[1]));
					return 	val0+SEPARATOR_RANGE_NUM+val1;
				}else if( modoFilter && ((String)val).matches(UNEQUAL_NUMBER+".+") ){
					return ""+((String)val).charAt(0)+((String)val).charAt(1)+String.valueOf(parseInteger( ((String)val).substring(2)));
				}else if( modoFilter && ((String)val).matches(MINMAX_NUMBER+".+") ){
					return ((String)val).charAt(0)+String.valueOf(parseInteger( ((String)val).substring(1)));
				}
				return String.valueOf(parseInteger((String)val));
			}
			return val.toString();
		}
		if( sintax==GConst.TM_DATE){
			if( val instanceof String ){
				int sep=((String)val).indexOf(" ");//Estamos en búsqueda
				if( sep==-1 )
					sep=((String)val).indexOf("-",1);//A partir de 1 por si es un numero en milisegundos negativo
				if( sep==-1 )
					sep=((String)val).indexOf(":");
				if( sep!=-1 ){
					String fecha1=parseFecha(((String)val).substring(0,sep));
					String fecha2=parseFecha(((String)val).substring(sep+1));
					return fecha1 + SEPARATOR_RANGE_DATE + fecha2;
				}else
					return parseFecha((String)val);
			}else{
				return parseFecha(val.toString());
			}

			/*long milliseconds=Long.parseLong((String)val);
			Date date=new Date(milliseconds);
			SimpleDateFormat dateFormat=new SimpleDateFormat("dd/MM/yy");
			return dateFormat.format(date);*/
		}else if(sintax==GConst.TM_DATE_HOUR){
			/*String maskFilter = getDateMask();
			maskFilter = maskFilter + "("+SEPARATOR_RANGE +
			maskFilter + ")?";
			Pattern patF = Pattern.compile(maskFilter);
			Matcher matchF = patF.matcher((String)val);
			if (matchF.matches()){//No se admite buscar por horas, solo fechas, por tanto el espacio separa ambas fechas
			*/	
			if( val instanceof String ){
				/*int sep = ((String)val).indexOf(" ");
				if (sep == -1)
					sep = ((String)val).indexOf("-");
				if (sep == -1)
					sep = ((String)val).indexOf(":");*/
				int sep = ((String)val).indexOf("-",1);//A partir de 1 por si es un numero en milisegundos negativo
				if (sep == -1){
					int pos=((String)val).indexOf(":");
					if(pos!=-1 && ((String)val).indexOf(":",pos+1)==-1)
						sep=pos;
				}
				if (sep != -1) {
					String fecha1 = parseFecha(((String)val).substring(0, sep));
					String fecha2 = parseFecha(((String)val).substring(sep + 1));
					return fecha1 + SEPARATOR_RANGE_DATE + fecha2;
				}else return parseFecha((String)val);
			}else{
				return parseFecha(val.toString());
			}

			/*long milliseconds=Long.parseLong((String)val);
			Date date=new Date(milliseconds);
			SimpleDateFormat dateFormat=new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			return dateFormat.format(date);*/
		}else if(sintax==GConst.TM_HOUR){
			/*String maskFilter = getDateMask();
			maskFilter = maskFilter + "("+SEPARATOR_RANGE +
			maskFilter + ")?";
			Pattern patF = Pattern.compile(maskFilter);
			Matcher matchF = patF.matcher((String)val);
			if (matchF.matches()){//No se admite buscar por horas, solo fechas, por tanto el espacio separa ambas fechas
			*/	
			if( val instanceof String ){
				/*int sep = ((String)val).indexOf(" ");
				if (sep == -1)
					sep = ((String)val).indexOf("-");
				if (sep == -1)
					sep = ((String)val).indexOf(":");*/
				int sep = ((String)val).indexOf("-",1);//A partir de 1 por si es un numero en milisegundos negativo
				if (sep == -1){
					int pos=((String)val).indexOf(":");
					if(pos!=-1 && ((String)val).indexOf(":",pos+1)==-1)
						sep=pos;
				}
				if (sep != -1) {
					String fecha1 = parseFecha(((String)val).substring(0, sep));
					String fecha2 = parseFecha(((String)val).substring(sep + 1));
					return fecha1 + SEPARATOR_RANGE_DATE + fecha2;
				}else return parseFecha((String)val);
			}else{
				return parseFecha(val.toString());
			}

			/*long milliseconds=Long.parseLong((String)val);
			Date date=new Date(milliseconds);
			SimpleDateFormat dateFormat=new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			return dateFormat.format(date);*/
		}
		//System.out.println("DBG4");
		if( val instanceof String ){
			//System.outprintln("DBG5");
			return (String)val ;
		}
		else
			return val.toString();
	}

	// TODO comentado, se encargaba de asignar los puntos a los numeros
	//private String subFormat( String txt ) throws ParseException{
		/*NumberFormat nf= NumberFormat.getInstance();
		String mask="\\d{1,3}(\\.\\d{3}){0,4}(,\\d*)?";
		if( !txt.matches(mask) ){
			System.out.println("NO MATCH "+txt);
			return nf.format( Double.parseDouble( txt ));
		}else
			System.out.println("SI MATCH");
		return formatNum( nf.parseObject( txt ) );*/

		//return txt;
	//}

	// TODO comentado, se encargaba de asignar los puntos a los numeros
	//private String formatNum( Object val ){
		/*NumberFormat nf= NumberFormat.getInstance();
		if( val instanceof Double ){
			return nf.format( ((Double)val).doubleValue() );
		}
		if( val instanceof Float ){
			return nf.format( ((Float)val).doubleValue() );
		}
		if( val instanceof Integer ){
			return nf.format( ((Integer)val).intValue() );
		}
		if( val instanceof Long ){
			return nf.format( ((Long)val).intValue() );
		}
		return null;*/

		//return String.valueOf(val);

	//}

	private String parseFecha( String text ){
		//boolean exito=true;

		if(text.length()==0) return "";

		try{
			long milliseconds=Long.parseLong(text);
			Date date=new Date(milliseconds);
			SimpleDateFormat dateFormat=null;

			if(sintax==GConst.TM_DATE)
				dateFormat=new SimpleDateFormat("dd/MM/yyyy");
			else if(sintax==GConst.TM_DATE_HOUR)
				dateFormat=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			else if(sintax==GConst.TM_HOUR){
				date.getTime();
				Calendar calendar=Calendar.getInstance();
				calendar.setTime(date);
				//System.err.println("milliseconds:"+milliseconds+" date:"+date+" date.getTime:"+date.getTime()+" calendar.getTime:"+calendar.getTime()+" calendar.getTimeInMillis:"+calendar.getTimeInMillis());
				dateFormat=new SimpleDateFormat("HH:mm:ss");
				//System.err.println("dateFormat.format:"+dateFormat.format(date));
			}
			else return null;

			return dateFormat.format(date);
		}catch(Exception ex){
			String textFecha="";
			
			if(sintax==GConst.TM_DATE)
				textFecha=text;
			else if(sintax==GConst.TM_DATE_HOUR){
				textFecha=text.split(" ")[0];
			}
				
			String res=null;
			if(sintax==GConst.TM_DATE_HOUR || sintax==GConst.TM_DATE){
				StringTokenizer st= new StringTokenizer (textFecha,SEPARATOR_DATE+"");
				String dia="", mes="", año="";
				dia=st.nextToken();
				mes=st.nextToken();
				año=st.nextToken();
	
				//Esto se necesitaría si permitimos que se metan dos digitos de año
				/*if(año.length()==4)
					res= dia+SEPARATOR_DATE+mes+SEPARATOR_DATE+año.substring(2);
				if(año.length()==2)*/
					res= dia+SEPARATOR_DATE+mes+SEPARATOR_DATE+año;
			}
			if(sintax==GConst.TM_DATE_HOUR || sintax==GConst.TM_HOUR){
				String textHour;
				if(sintax==GConst.TM_DATE_HOUR)
					textHour=text.split(" ")[1];
				else textHour=text.split(" ")[0];
					
				StringTokenizer st1= new StringTokenizer (textHour,SEPARATOR_HOUR+"");
				String hora="", min="", seg="";
				hora=st1.nextToken();
				min=st1.nextToken();
				seg=st1.nextToken();
				
				if(sintax==GConst.TM_DATE_HOUR)
					res+=" ";
				else res="";
				res+= hora+SEPARATOR_HOUR+min+SEPARATOR_HOUR+seg;
			}
			
			if( res!=null ){
//				try{
//				//Date dt = dateUtil.parseFecha(res);
//				Date dt = subParseFecha(res);
//				return helperConstant.valueToString(GConst.TM_DATE,dt);
//				}catch(ParseException pe){
//				Singleton.getComm().logError(SwingUtilities.getWindowAncestor(this),pe);
//				return null;
//				}
				return res;
			}else
				return null;
		}

	}
	
	private Number parseReal(String val){
		String auxVal=val.replaceAll(",", ".");//Las comas las convertimos en punto ya que el double solo acepta puntos
		return Double.parseDouble(auxVal);
	}
	
	private Integer parseInteger(String val){
		return Integer.parseInt(val);
	}
	
	public Object parseNumber(String val){
		Object number=null;
		if(sintax==GConst.TM_REAL)
			number=parseReal(val);
		else if(sintax==GConst.TM_INTEGER )
			number=parseInteger(val);
		
		return number;
	}

	public Long timeFecha(String text){
		Long milliseconds=null;

		if(text!=null){
			String pattern=null;
			if( sintax==GConst.TM_DATE)
				pattern="dd/MM/yyyy";
			else if(sintax==GConst.TM_DATE_HOUR)
				pattern="dd/MM/yyyy HH:mm:ss";
			else if(sintax==GConst.TM_HOUR)
				pattern="HH:mm:ss";

			SimpleDateFormat dateFormat=new SimpleDateFormat(pattern);
			try {
				milliseconds=dateFormat.parse(text).getTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return milliseconds;
	}

//	public Date subParseFecha( String fecha ) throws ParseException{
//	if(fecha==null) throw new ParseException("FECHA ES NULA",0);

//	String patron="";
//	if( fecha.matches("(\\d{1,2}"+SEPARATOR_DATE+"\\d{1,2}"+SEPARATOR_DATE+"\\d{2}(\\d{2})?)") ){
//	System.out.println("MATCH1");
//	patron= "dd" + SEPARATOR_DATE + "MM" + SEPARATOR_DATE + "yy";
//	}else{
//	patron="dd" + SEPARATOR_DATE + "MM" + SEPARATOR_DATE + "yy hh:mm";
//	}

//	SimpleDateFormat dateFormat= new SimpleDateFormat(patron);
//	java.util.Date dia = dateFormat.parse(fecha);
//	return dia;
//	}

}
