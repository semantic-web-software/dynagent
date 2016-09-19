package dynagent.ejb.old;
/*package dynagent.ejb;

import org.jdom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.HashMap;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.Dimension;

import dynagent.knowledge.*;
import dynagent.communication.queryData;
import dynagent.knowledge.instance.*;

class grupoDesordenado extends Object implements Comparable{
	public int nivel;
	ArrayList lista= new ArrayList();

	grupoDesordenado(int nivel){
		this.nivel=nivel;
	}

	public void addItem( Object item){
		lista.add( item );
	}

	public Iterator iterator( ){
		return lista.iterator();
	}

	public int compareTo(Object o)
		throws ClassCastException{
		if(!(o instanceof grupoDesordenado))
			throw new ClassCastException();
		grupoDesordenado b=(grupoDesordenado) o;

		if(nivel < b.nivel) return -1;
		if(nivel == b.nivel) return 0;
		return 1;
	}
}

public class instanceReportAdaptor{
	Element m_filter;
	selectData m_data;
	Element m_plantilla;
	Element m_doc;
	HashMap m_atLabel= new HashMap();
	HashMap m_atTM= new HashMap();
	ArrayList m_grupos= new ArrayList();
	Diccionario m_dic=null;
	int m_empresa=0;
	metaData m_md;
	public instanceReportAdaptor( Diccionario dic, Element filter, selectData qdata, Element plantilla, int empresa ){
		//el elemento data es el resultado de un Query fijando a un solo objeto
		m_dic=dic;
		m_md= m_dic.getMetaData( empresa );
		m_filter= filter;
		m_data= qdata;
		m_empresa=empresa;
		m_plantilla= plantilla;
		try{
			System.out.println("IR FILTER:"+jdomParser.returnXML(filter));
			//System.out.println("IR DATA:"+jdomParser.returnXML(data));
		}catch(Exception e){;}
	}

	public Element generateReport(){

		buildMap( m_filter, 0 );
		Object[] lista= m_grupos.toArray();
		Arrays.sort( lista );
		int pos=1;
		Element formView= new Element("FORM");

		grupoDesordenado gd=(grupoDesordenado)m_grupos.get(0);
		pos=buildPanel( formView, null,gd.iterator(), pos );
		for( int i= m_grupos.size()-1; i>0; i--){
			gd=(grupoDesordenado)m_grupos.get(i);
			pos=buildPanel( formView, null,gd.iterator(), pos );
		}
		instanceReport ir= new instanceReport( instanceReport.IMPRESORA, formView,null, m_plantilla );

		return ir.generateReport();
	}


	private void buildMap(Element filter, int nivel){	//esta funcion solo sirve para detectar las etiquetas de los
						//atributos, habria que sustituirla por adecuado servicio de diccionario

		grupoDesordenado gd= new grupoDesordenado( nivel );
		m_grupos.add( gd );

		Iterator iSel= filter.getChildren("SELECT").iterator();
		while(iSel.hasNext()){
			Element select= (Element)iSel.next();
			Integer tapos= new Integer(select.getAttributeValue("TA_POS"));
			m_atLabel.put( tapos, select.getAttributeValue("AT_NAME"));
			m_atTM.put( tapos, new Integer(select.getAttributeValue("ID_TM")));
			gd.addItem( select );
		}
		Iterator iVir= filter.getChildren("VIRTUAL").iterator();
		while(iVir.hasNext()){
			Element virt= (Element)iVir.next();
			Integer tapos= new Integer(virt.getAttributeValue("TA_POS"));
			m_atLabel.put( tapos, virt.getAttributeValue("AT_NAME"));
			m_atTM.put( tapos, new Integer(virt.getAttributeValue("ID_TM")));
			gd.addItem( virt );
		}
		Iterator iFilter= filter.getChildren("FILTER").iterator();
		while( iFilter.hasNext() ){
			Element child=(Element)iFilter.next();
			buildMap( child, nivel + 1 );
		}
	}

	private int buildPanel( org.jdom.Element viewParent, Integer group, Iterator atrList, int pos){
		viewBalancer balancer= new viewBalancer(m_md,
							new Dimension(400,600),
							viewBalancer.OUTPUT_REPORT,
							new FontRenderContext( null, true, true ),
							new Font( "Verdana", Font.PLAIN, 24 ),
							viewParent,
							null,//corregir
							false);
		while( atrList.hasNext()){
	         	Integer intPos= new Integer(pos);
			org.jdom.Element eAtr=(org.jdom.Element)atrList.next();
        		Integer intTapos= new Integer(eAtr.getAttributeValue("TA_POS"));

	                // DATA ITEMS ////////////////////////////////////////////////////////
			Integer iTM= (Integer)m_atTM.get( intTapos );

			int adaptedSintax= iTM.intValue();
			if( iTM.intValue()==helperConstant.TM_ENUMERADO ) adaptedSintax= helperConstant.TM_TEXTO;
			Element dataItem= new Element("ITEM");
			dataItem.setAttribute("ID", String.valueOf(pos));

			dataItem.setAttribute("SINTAX",String.valueOf(adaptedSintax));

			String ref= eAtr.getParent().getAttributeValue("REF");

			Iterator iData= m_data.getFirst().getAttIterator(Integer.parseInt(ref));

			attribute dataDb=null;
			while( iData.hasNext()){
				dataDb= (attribute)iData.next();
				if(dataDb.getTapos()==intTapos.intValue())
					break;
			}
			if( dataDb==null)
				System.out.println("ERROR EN INSTANCE REPORT ADAPTOR, no se encontro data Item");

			String label=null;

			if( intTapos.intValue()==helperConstant.TAPOS_RDN )
				label= eAtr.getParent().getAttributeValue("TO_NAME");
			else
				if( eAtr.getParent().getParent()==null )
					label= (String)m_atLabel.get( intTapos );
				else
					label= (String)m_atLabel.get( intTapos ) + " de " +
						eAtr.getParent().getAttributeValue("TO_NAME");

			dataItem.setAttribute("LABEL", label );

			if( dataDb.getValue() instanceof String && dataDb.getValue().toString().length()>0){
				dataItem.setText( dataDb.getValue().toString());
				System.out.println("TEXT NO NULO:"+dataDb.getValue().toString());
			}
			else
				if( iTM.intValue()==helperConstant.TM_ENUMERADO ){
					int iValue=((Integer)dataDb.getValue()).intValue();
					dataItem.setText(String.valueOf( m_dic.getEnumLabel( m_empresa,intTapos, iValue )));
				}else{
                                    dataItem.setAttribute("DEFAULT",dataDb.getValue().toString());
                                }
			System.out.println("POS:"+pos);
			int prioridad=1;
			if( eAtr.getParent().getParent()==null && intTapos.intValue()==helperConstant.TAPOS_RDN )
				prioridad=2;
			balancer.addItem(group,
					adaptedSintax,
					"0@"+intTapos.toString(),
					intTapos.intValue(),
					prioridad,
					eAtr.getAttributeValue("MASK"),
					true,
					true ,
					label,
					-1,
					false,0 );     
			pos++;
		}
		balancer.process(true);
		return pos;
	}

}


*/