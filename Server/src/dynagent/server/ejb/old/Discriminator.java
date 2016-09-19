package dynagent.ejb.old;
/*package dynagent.ejb;

import org.jdom.Element;
import java.util.Iterator;
import java.util.ArrayList;

import dynagent.knowledge.instance.instance;
import dynagent.knowledge.instance.attribute;

public class Discriminator{


   public static boolean checkQueryResponse(String user, Element sourceFilter, ArrayList discrimInfo,instance object ){
	if( discrimInfo==null || object==null ) return true;
	for( int i=0; i< discrimInfo.size(); i++){
		Element disc = (Element)discrimInfo.get(i);

		if( 	disc.getAttributeValue("CONTEXT")!=null &&
			disc.getAttributeValue("CONTEXT").equals("CURR_USER") ){
			boolean isNot= 	disc.getAttributeValue("NOT")!=null &&
					disc.getAttributeValue("NOT").equals("TRUE");

			Element refNode= jdomParser.findElementByAt( 	sourceFilter,
									"*",
									"OID",
									disc.getAttributeValue("OID"),
									true,
									true );
			if( refNode==null ) continue;
			int ref= Integer.parseInt(refNode.getAttributeValue("REF"));
                        attribute ava= object.getAttribute(helperConstant.TAPOS_RDN,ref);
                        if( ava==null ) continue;

			boolean coincide= ava.getValue().toString().equals(user);
			if( !isNot && !coincide || isNot && coincide ) return false;
		}
	}
	return true;
   }

   public static ArrayList buildDiscrimInfo( docDataModel doc,Element sourceFilter, Element discrim ){
	if( discrim==null ) return null;
	ArrayList res= new ArrayList();

	Iterator itr= discrim.getChildren( "FILTER_FIXING" ).iterator();
	while( itr.hasNext() ){
		Element nodo= (Element)itr.next();
		if( 	nodo.getAttributeValue("CONTEXT")!=null &&
			nodo.getAttributeValue("CONTEXT").equals("CURR_USER") )
			res.add( nodo.clone() );

	}
	return res;
   }
}*/
