package dynagent.ejb.old;
/*package dynagent.ejb;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JOptionPane;
import org.jdom.*;
import java.text.ParseException;
	
import dynagent.knowledge.*;

public class cuantitativeExtraction{

	public static void adaptaFilter( metaData md, Element filter, Element dominio, boolean adaptaFilter ){
		filter.setAttribute("EXTRACTION","TRUE");
		Element polim= dominio.getChild("POLIMORFISMO");
		if( polim==null){
			polim= new Element("POLIMORFISMO");
			dominio.addContent( polim );
		}
		Element prop= dominio.getChild("PROPERTY_LIST");
		if( prop==null){
			prop= new Element("PROPERTY_LIST");
			dominio.addContent( prop );
		}

		int currTO= Integer.parseInt( filter.getAttributeValue("ID_TO") );
		
		if( !md.isSpecializedFrom( currTO, helperConstant.TO_CUANTITATIVO ) )
			JOptionPane.showMessageDialog(null, "ERROR EN EXTRACTOR, el TO " + 
							filter.getAttributeValue("ID_TO") +
							" no es cuantitativo.");

		String[] labels=  {"SELECT","VIRTUAL"};
		ArrayList totales= jdomParser.findElementsByAt( filter,
								labels,
								"TOTAL",
								"TRUE",
								true );
		for( int i=0;i<totales.size();i++){
			Element at=(Element)totales.get(i);
			Element atRest= new Element( at.getName() );
			Element atMax= new Element( at.getName() );
			atRest.setAttribute("AT_NAME","RESTO");
			atMax.setAttribute("AT_NAME","MAX");
			Integer tapos=	new Integer( at.getAttributeValue("TA_POS") );	
			int tm= helperConstant.TM_ENTERO;	
	
			atRest.setAttribute("TA_POS",String.valueOf( helperConstant.TAPOS_RESTO ));
			atMax.setAttribute("TA_POS",String.valueOf( helperConstant.TAPOS_MAXIMO ));
			
			atRest.setAttribute("ID_TM",String.valueOf( tm ));
			atMax.setAttribute("ID_TM",String.valueOf(tm ));

			Element[] postAts={ atMax, atRest };
			if( adaptaFilter ) preInsertarAts( at, postAts );

			Element propAtMax=new Element("ATRIBUTO");
			prop.addContent( propAtMax );
			propAtMax.setAttribute("TA_POS", String.valueOf( helperConstant.TAPOS_MAXIMO ) );
			propAtMax.setAttribute("ACCESS","READ");

			Element propRest=new Element("ATRIBUTO");
			prop.addContent( propRest );
			propRest.setAttribute("TA_POS", String.valueOf(helperConstant.TAPOS_RESTO) );
			propRest.setAttribute("ACCESS","READ");
			Element rest= new Element("REST");
			polim.addContent( rest );
			rest.setAttribute("TA_POS", String.valueOf(helperConstant.TAPOS_RESTO) );
			Element rule= new Element("RULE");
			rest.addContent( rule );
			Element subdom= new Element("DOMINIO");
			subdom.setAttribute("REF","-1");
			subdom.setAttribute("INLINE","TRUE");
			rule.addContent( subdom );
			
			subdom.setText("{0@" + helperConstant.TAPOS_MAXIMO + "}-{0@" + tapos + "}");			
		}
	}

	public static void adaptaQueryResponse( metaData md, Element filter, Element response ){
		int currTO= Integer.parseInt( filter.getAttributeValue("ID_TO") );
		if( !md.isSpecializedFrom( currTO, helperConstant.TO_CUANTITATIVO ) )
			JOptionPane.showMessageDialog(null, "ERROR EN EXTRACTOR, el TO " + 
							filter.getAttributeValue("ID_TO") +
							" no es cuantitativo.");

		String[] labels=  {"SELECT","VIRTUAL"};
		ArrayList totales= jdomParser.findElementsByAt( filter,
								labels,
								"TOTAL",
								"TRUE",
								true );
		for( int i=0;i<totales.size();i++){
			Element at=(Element)totales.get(i);
			Integer tapos= new Integer( at.getAttributeValue("TA_POS") );
			System.out.println("EXTR1:"+tapos);
			Iterator iAva= jdomParser.findElementsByAt( 	response,
									"ITEM",
									"TA_POS",
									tapos.toString(),
									"REF",	
									at.getParent().getAttributeValue("REF"),
									true ).iterator();
			while( iAva.hasNext()){
			System.out.println("EXTR2:");
				Element ava=(Element)iAva.next();
				Object val=null;
				if( md.getID_TM( tapos )==helperConstant.TM_ENTERO )
					val= new Integer( ava.getAttributeValue("VALUE"));
				if( md.getID_TM( tapos )==helperConstant.TM_REAL )
					val= new Double( ava.getAttributeValue("VALUE"));
				if( val==null ) continue;
			
				Element avRest= new Element("ITEM");
				ava.getParent().addContent( avRest );
				avRest.setAttribute("TA_POS", String.valueOf(helperConstant.TAPOS_RESTO) );
				avRest.setAttribute("VALUE", "0" );
				avRest.setAttribute("REF", ava.getAttributeValue("REF"));

				Element avExt= new Element("ITEM");
				ava.getParent().addContent( avExt );
				avExt.setAttribute("TA_POS", String.valueOf(helperConstant.TAPOS_MAXIMO));
				avExt.setAttribute("VALUE", val.toString() );
				avExt.setAttribute("REF", ava.getAttributeValue("REF"));
			}
		}
	}

	public static boolean checkNewObject( metaData md, Element atom, int actionType, Element detalle )
		throws ParseException{
		int ido= atom.getAttributeValue("ID_O")==null ? -1:Integer.parseInt( atom.getAttributeValue("ID_O") );
		if( ido >0 ) return false;
		System.out.println("ACTION:"+actionType);
		if( 	actionType!=helperConstant.TRANSFORMATION_ACTION ||
			!detalle.getAttributeValue("FUNCTION").equals("EXTRACTO"))

			return true;//ido es menor que cero
		System.out.println("EXTRACTION:"+atom.getAttributeValue("EXTRACTION"));
		if( atom.getAttributeValue("EXTRACTION")==null )
			return true;
		
		Element at= jdomParser.findElementByAt( 	atom.getChild("ATRIBUTOS"),
								"ITEM",
								"EXTRACTION",	
								"TRUE",
								false );

		if( at==null ) return false;//se trata de un set y no aparece porque no ha cambiado


		if( at==null ){
			JOptionPane.showMessageDialog( null, "Error en checkNewObject cuantitative. No at extraccion");
			return true;
		}

		boolean igual= helperConstant.equals( 	helperConstant.TM_ENTERO,
							at.getAttributeValue("VALUE"), at.getAttributeValue("OLD_VAL") );
		System.out.println("IGUAL:"+igual+","+at.getAttributeValue("VALUE")+","+ at.getAttributeValue("OLD_VAL"));
		return !igual;
	}

	private static void postInsertarAts( Element at, Element[] post){
		Element parent=at.getParent();
		ArrayList postChilds= new ArrayList();
		Iterator itr=parent.getChildren(at.getName()).iterator();
		boolean iniPost=false;
		while( itr.hasNext() ){
			Element postChild= (Element)itr.next();
			if( iniPost ) postChilds.add( postChild );
			if( 	!iniPost && 
				postChild.getAttributeValue("TA_POS").equals( at.getAttributeValue("TA_POS") ) )
				iniPost=true;			
		}
		for( int i=0;i<postChilds.size();i++)
			((Element)postChilds.get(i)).detach();
		for( int i=0; i< post.length;i++ )
			parent.addContent( (Element)post[i] );		
		for( int i=0;i<postChilds.size();i++)
			parent.addContent((Element)postChilds.get(i));

	}

	private static void preInsertarAts( Element at, Element[] post){
		Element parent=at.getParent();
		ArrayList postChilds= new ArrayList();
		Iterator itr=parent.getChildren(at.getName()).iterator();
		boolean iniPost=false;
		while( itr.hasNext() ){
			Element postChild= (Element)itr.next();
			if( 	!iniPost && 
				postChild.getAttributeValue("TA_POS").equals( at.getAttributeValue("TA_POS") ) )
				iniPost=true;

			if( iniPost ) postChilds.add( postChild );		
		}
		for( int i=0;i<postChilds.size();i++)
			((Element)postChilds.get(i)).detach();
		for( int i=0; i< post.length;i++ )
			parent.addContent( (Element)post[i] );		
		for( int i=0;i<postChilds.size();i++)
			parent.addContent((Element)postChilds.get(i));

	}

}*/