package dynagent.ejb.old;
/*package dynagent.ejb;

import org.jdom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.*;
import java.util.NoSuchElementException;
import java.util.HashMap;
import java.awt.Font;
import java.awt.font.FontRenderContext;

import dynagent.knowledge.instance.*;
import dynagent.knowledge.metaData;
import java.text.NumberFormat;

public class templateReport{
	Element m_filter;
	selectData m_data;
	Element m_plantilla;
	Element m_doc;
	HashMap m_atTM= new HashMap();
	HashMap m_mapTemplate= new HashMap();
	HashMap m_nodosNoActualizados= new HashMap();
	metaData m_md=null;

	Diccionario m_dic=null;
	int m_empresa=0;
	public templateReport( Diccionario dic, Element filter, selectData data, Element plantilla, int empresa ){
		//el elemento data es el resultado de un Query fijando a un solo objeto
		m_dic=dic;
		m_filter= filter;
		m_data= data;
		m_empresa=empresa;
		m_plantilla= plantilla;
		m_md= m_dic.getMetaData( m_empresa );
	}

	public Element generateReport(){
		Pattern patron= Pattern.compile(".*\\{\\d+.\\d{1,2}@\\d+\\}.*");

		buildMapTemplate( m_plantilla, patron );
		buildMap( m_filter );
		process( m_data );
		Iterator itr= m_nodosNoActualizados.keySet().iterator();
		while( itr.hasNext()){
			String key= (String)itr.next();
			Element node= (Element)m_nodosNoActualizados.get( key );
			node.setText( "" );
		}
		return m_plantilla;
	}

	private void buildMapTemplate( Element template, Pattern pt ){
		Iterator itr= template.getChildren().iterator();
		while(itr.hasNext()){
			Element child=(Element)itr.next();
			String id= child.getAttributeValue("ID");
			if( id!=null && id.length() > 3 ){
				Matcher m= pt.matcher( id );
				if( m.matches() ){
					int ini= id.indexOf("{");
					int end= id.indexOf("}");
					String key=id.substring( ini+1, end );
					m_mapTemplate.put( key,  child );
					m_nodosNoActualizados.put( key, child );
				}
			}
			buildMapTemplate( child, pt );
		}
	}

	private void buildMap(Element filter){

		Iterator iSel= filter.getChildren("SELECT").iterator();
		while(iSel.hasNext()){
			Element select= (Element)iSel.next();
			Integer tapos= new Integer(select.getAttributeValue("TA_POS"));
			m_atTM.put( tapos, new Integer(m_md.getID_TM(tapos)));
		}
		Iterator iVir= filter.getChildren("VIRTUAL").iterator();
		while(iVir.hasNext()){
			Element virt= (Element)iVir.next();
			Integer tapos= new Integer(virt.getAttributeValue("TA_POS"));
			m_atTM.put( tapos, new Integer(m_md.getID_TM(tapos)));
		}
		Iterator iFilter= filter.getChildren("FILTER").iterator();
		while( iFilter.hasNext() ){
			Element child=(Element)iFilter.next();
			buildMap( child );
		}
	}

	private void process( selectData data ){
		try{
		HashMap mapVirtObj= new HashMap();
		HashMap objs=null;
		Integer idFilter= new Integer( m_filter.getAttributeValue("ID") );

		metaData md= m_dic.getMetaData( m_empresa );
                instance objTarget=data.getFirst();
		Iterator itr= objTarget.getAttIterator(false,false);
		while( itr.hasNext()){
			attribute eAtr=(attribute)itr.next();
        		Integer tapos= new Integer(eAtr.getTapos());
			int tm= md.getID_TM( tapos );
			Integer ref= new Integer( eAtr.getVirtualREF());

			Integer ido= new Integer( objTarget.getIDO());

			String prefijo= idFilter.toString() + "."+ eAtr.getVirtualREF();
			String key=  prefijo + "@" + eAtr.getTapos();

			if( m_mapTemplate.containsKey( key ) ){
				Element node= (Element)m_mapTemplate.get( key );
				if( !mapVirtObj.containsKey( ref ) ){
					objs= new HashMap();
					mapVirtObj.put( ref, objs );
				}else
					objs= (HashMap)mapVirtObj.get( ref );

				if( objs.size()== 0 ){
					objs.put( ido, node.getParent() );
					processMetaAt( prefijo, node.getParent(), objTarget, md );
				}else
					if( 	objs.size()>1 ||
						(objs.size()==1 && !objs.containsKey( ido ))){//se trata de una tabla
						Element row= null;
						if( objs.containsKey( ido ) ){
							row= (Element)objs.get( ido );
						}else{
							row= copiaRowLimpio(node.getParent());
							node.getParent().getParent().addContent( row );
							objs.put( ido, row );
							processMetaAt( prefijo, row, objTarget, md );
						}
						node= findCeldaInRow( key, row );
					}
				setHtmlField( eAtr, node, key, tapos, tm );
			}

		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private Element copiaRowLimpio( Element row ){
		Element res= (Element)row.clone();
		Iterator itr= res.getChildren("TD").iterator();
		while( itr.hasNext() ){
			Element celda= (Element)itr.next();
			celda.setText("");
		}
		return res;
	}

	private void processMetaAt( String prefijo, Element row, instance objTarget, metaData md ){
		if( !row.getName().equals("TR") ) return;
		String keyMeta=	prefijo + "@" + helperConstant.TAPOS_METATIPO;
		if( m_mapTemplate.containsKey( keyMeta ) ){
			Element nodeMeta= findCeldaInRow( keyMeta, row );
			Integer idto=new Integer( objTarget.getType());
			nodeMeta.setText(md.getTOLabel( idto ));
		}
	}

	private Element findCeldaInRow(String key, Element row ){
		Element node= jdomParser.findElementByAt( 	row,
						 		"TD",
								"REF",
								"{"+key+"}",
								false );
		return node;
	}

	private void setHtmlField( attribute eAtr, Element htmlNode, String key, Integer tapos, int tm ){
		m_nodosNoActualizados.remove( key );
		if( tm==helperConstant.TM_BOOLEANO || tm==helperConstant.TM_BOOLEANO_EXT ){
			Object ev= eAtr.getValue();
                        Boolean value= (Boolean)(ev instanceof extendedValue ? ((extendedValue)ev).getValue():ev);
			System.out.println("BVAL:"+value);
			if( value.booleanValue() )
				htmlNode.setAttribute("CHECKED","TRUE");
			else{
                            htmlNode.setName("FONT");
                            htmlNode.setAttribute("COLOR","#FF0000" );
                            htmlNode.setText("X");
                        }
		}else
			if( tm==helperConstant.TM_TEXTO ||
                            tm==helperConstant.TM_FECHA ){
				if( htmlNode.getAttributeValue("ID")==null )
					htmlNode.setAttribute("ID", key );
				htmlNode.setText( eAtr.getValue().toString() );
			}else
				if( tm==helperConstant.TM_ENUMERADO ){
					Integer iValue=(Integer)eAtr.getValue();
					if( htmlNode.getAttributeValue("ID")==null )
						htmlNode.setAttribute("ID", key );
					String newVal= String.valueOf(m_dic.getEnumLabel(m_empresa,tapos,iValue.intValue()));
					htmlNode.setText(newVal);
				}else{
					if( htmlNode.getAttributeValue("ID")==null )
						htmlNode.setAttribute("ID", key );
                                        if( tm==helperConstant.TM_REAL ||
                                            tm==helperConstant.TM_ENTERO    ){
                                            NumberFormat nf = NumberFormat.getInstance();

                                            if( eAtr.getValue() instanceof Double )
                                                htmlNode.setText(nf.format(((Double)eAtr.getValue()).doubleValue()));
                                            if( eAtr.getValue() instanceof Integer )
                                                htmlNode.setText(nf.format(((Integer)eAtr.getValue()).intValue()));
                                        }else
                                            htmlNode.setText(eAtr.getValue().toString());
				}
	}

}
*/

