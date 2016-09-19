package dynagent.ejb.old;
/*

package dynagent.ejb;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Collection;
import java.util.Arrays;
import org.jdom.*;

public class ordenadorAts implements Comparator{
	
	public static final int AT_ELEM=1;
	public static final int REL_ELEM=2;
	public static final int GROUP_ELEM=3;

	private order[] ordenacion=null;
	ordenadorAts( order[] ord){
		ordenacion= ord;
	}

	public int compare( Object oba, Object obb ){
		if( !(oba instanceof HashMap) || !(obb instanceof HashMap) )
			throw new ClassCastException("FUNCTIONS: COMPARATOR, error Cast");
		HashMap atsA=(HashMap)oba;
		HashMap atsB=(HashMap)obb;
		String taF= "0@"+ String.valueOf( helperConstant.TAPOS_FILTER_ROOT );
		for( int c=0; c< ordenacion.length; c++ ){
			order od=(order)ordenacion[ c ];
			
			Double valA=null; 
			Double valB=null; 

			Integer idFA= (Integer)atsA.get( taF );
			Integer idFB= (Integer)atsB.get( taF );

			for( int a= 0; a< od.ats.length; a++){
				if( idFA.intValue()!=od.ats[a].idFilter )
					continue;
				String key= String.valueOf(od.ats[a].idNodeRef)+"@"+od.ats[a].tapos;
				if( !atsA.containsKey(key) )
					break;
				valA= (Double)atsA.get( key );
				break;
			}
			for( int a= 0; a< od.ats.length; a++){
				if( idFB.intValue()!=od.ats[a].idFilter )
					continue;
				String key= String.valueOf(od.ats[a].idNodeRef)+"@"+od.ats[a].tapos;
				if( !atsB.containsKey(key) )
					break;
				valB= (Double)atsB.get( key );
				break;
			}
			if( valA==null && valB==null ) continue;
			if( valA==null && valB!=null ){
				//suponemos el nulo vale menos
				if( od.orden==order.ASC ) return -1;
				else return 1;
			}	
			if( valA!=null && valB==null ){
				//suponemos el nulo vale menos
				if( od.orden==order.ASC ) return 1;
				else return -1;
			}	
			if( valA.equals( valB ) ) continue;
			
			if( od.orden==order.ASC )
				return valA.compareTo( valB );
			else return valB.compareTo( valA );
		}		
		return 0;//por no haber encontrado diferencias
	}
}
*/