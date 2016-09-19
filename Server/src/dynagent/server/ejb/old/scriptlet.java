package dynagent.ejb.old;
/*// 14-10-02 V1 JOB Añado findElementByAt


package dynagent.ejb;
import java.text.ParseException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import dynagent.ejb.helperConstant;

	
public class scriptlet{

	public static Double calculaDirectExpresion( HashMap values, String expresion ){

		System.out.println( "INSCRIPT: "+ expresion + " @ "+ values.size());

		Object arbol= compilar( values, expresion );
		return procesar( values, arbol );
	}

	static Object compilar( HashMap values, String expresion ){
		int op=  extractOperador(expresion );
		if(op!=-1){
			ArrayList res= new ArrayList();
			res.add( expresion.substring( op, op+1 ) );
			res.add( compilar( values, expresion.substring( 0, op )) );
			res.add( compilar( values, expresion.substring( op+1)) );
			return res;
		}else
			return expresion;
		
	}

	static int extractOperador( String expresion ){
		int mas= expresion.indexOf( "+" );
		int men= expresion.indexOf( "-" );
		int por= expresion.indexOf( "*" );
		int sum= mas==-1 ? men:(men==-1 ? mas:Math.min(mas,men));
		return sum==-1 ? por:(por==-1 ? sum:Math.min(sum,por));		
	}

	static Double procesar( HashMap values, Object arbol ){
		if( arbol instanceof String ){
			String cont=(String)arbol;
			System.out.println("SCRIP:"+cont);
			int ini=cont.indexOf( "{" );
			int end=cont.indexOf( "}" );
			cont= cont.substring( ini+1, end ).trim();
			System.out.println("SCRIP:"+cont+","+values.get( cont ));
			return getDouble(values.get( cont ));
		}
		ArrayList data= (ArrayList)arbol;
		String op=(String)data.get(0);
		if( op.equals("+") )
			return 	new Double(	procesar( values, data.get(1) ).doubleValue() + 
						procesar( values, data.get(2) ).doubleValue() );

		if( op.equals("-") )
			return 		new Double(	procesar( values, data.get(1) ).doubleValue() - 
							procesar( values, data.get(2) ).doubleValue());


		if( op.equals("*") ){
			double res=procesar(values,data.get(1)).doubleValue()*procesar( values, data.get(2)).doubleValue();	
			return new Double( res );
		}
		return null;
	}

	static Double getDouble( Object val){
		if( val instanceof Float ){
			Float v=(Float)val;
			return new Double( v.doubleValue() );
		}
		if( val instanceof Integer ){
			Integer v=(Integer)val;
			return new Double( v.intValue() );
		}
		if( val instanceof Double ) return (Double)val;
		return null;
	}
}
	
	*/