/*package dynagent.ejb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.jdom.Element;

public class fixMap extends Object{
	Integer ctxSuperAfectado;// por si pudiera haber más de una tabla con mismo filtro
	Integer filtroAfectado;
	public int 	subnodoAfectado;
	int 	cxtFuente=0;// que proporcionara el valor a fijar en el filtro
	private HashMap idoFuente;

	fixMap( Integer ctxSup, Integer filter, int subnodo, int ctxSource, HashMap idoSource ){
		ctxSuperAfectado=ctxSup;
		filtroAfectado=filter;
		subnodoAfectado=subnodo;
		cxtFuente=ctxSource;
		idoFuente=idoSource;
	}
	public void printData(){
		System.out.println("FIXMAP:");
		System.out.println("   CTX, FONT:"+ctxSuperAfectado+","+cxtFuente);
		System.out.println("   FILTER,SUB:"+filtroAfectado+","+subnodoAfectado);
		System.out.println("   IDOS:"+idoFuente.size());
		Iterator itr= idoFuente.keySet().iterator();
		while( itr.hasNext() ){
			System.out.println("   IDO:"+itr.next());
		}
	}
	public Set getIDO_KeySet(){
		return idoFuente.keySet();
	}

	public Object getIdoSource( Integer ido ){
		return idoFuente.get( ido );
	}
        public int getSourceFixSize(){
            return idoFuente.size();
        }
}
*/