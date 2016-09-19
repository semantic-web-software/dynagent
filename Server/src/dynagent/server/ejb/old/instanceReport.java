package dynagent.ejb.old;
/*package dynagent.ejb;

import org.jdom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.HashMap;

import dynagent.knowledge.*;

public class instanceReport{
	Element m_view;
	Element m_data;
	Element m_plantilla;
	Element m_doc;
	HashMap m_data_Map= new HashMap();
	public final static int IMPRESORA=0;
	public final static int MONITOR=1;
	int m_medio=MONITOR;

	public instanceReport( int medio, Element view, Element data, Element plantilla ){
		m_medio= medio;
		m_view= view;
		m_data= data;
		m_plantilla= plantilla;
		try{
			System.out.println("DATA:"+jdomParser.returnXML(data));
			System.out.println("VIEW:"+jdomParser.returnXML(view));
		}catch(Exception e){;}
	}
	
	public Element generateReport(){
		Element table= new Element("TABLE");
		String style="border-collapse:collapse";

		Build( table, m_view );

		if( m_plantilla==null){
			m_doc= new Element("HTML");
			Element head= new Element("HEAD");
			m_doc.addContent(head);
			Element title= new Element("TITLE");
			head.addContent(title);
			title.setText("PRUEBA");
			Element body= new Element("BODY");
			m_doc.addContent(body);
			body.addContent(table);
			style+=";width:" + (m_medio==MONITOR ? "780px":"600px");
		}else{
			Element parent= jdomParser.findElementByAt( 	m_plantilla,
									"*",
									"INCLUDE",
									"REPORT",
									true);
			parent.addContent( table );
			table.setAttribute("WIDTH","100%");
			m_doc= m_plantilla;
		}
		table.setAttribute("BORDER","0");
		table.setAttribute("BORDERCOLOR","#0000");
		table.setAttribute("STYLE",style);

		return m_doc;
	}

	private void BuildDataMap(){
		Iterator itr= m_data.getChildren("ITEM").iterator();
		while(itr.hasNext()){
			org.jdom.Element eItem= (org.jdom.Element) itr.next();
			try{
			m_data_Map.put(new Integer(eItem.getAttributeValue("ID")),
					   eItem);
			}catch(NumberFormatException e){
				System.out.println("INSTANCE REPORT:ERROR EN INICIALIZACION DATOS, error en ID FORM:"+ 
					eItem.getAttributeValue("ID"));
			}
		}
		itr= m_data.getChildren("TABLE").iterator();
		while(itr.hasNext()){
			org.jdom.Element eItem= (org.jdom.Element) itr.next();
			try{
			m_data_Map.put(new Integer(eItem.getAttributeValue("ID")),
					   eItem);
			}catch(NumberFormatException e){
				System.out.println("INSTANCE REPORT: ERROR EN INICIALIZACION TABLAS, error en ID FORM:"+ 
					eItem.getAttributeValue("ID"));
			}
		}

	}

	void fusionar( Element body ){
		Iterator itr= body.getChildren().iterator();
		int currentMaxLeft=0, currentMaxRight=0;
		boolean inicio=true;
		while(itr.hasNext()){
			org.jdom.Element eBody = (org.jdom.Element)itr.next();
			if(eBody.getName().equals("FLOW")){
				eBody.setAttribute("FUSION","INI");
			}
			if(eBody.getName().equals("GRID")){
				Element left= eBody.getChild("LEFT");
				Element right= eBody.getChild("RIGHT");
				int anchoLeft= Integer.parseInt( left.getAttributeValue("CONTENT_WIDTH") );
				int anchoRight= Integer.parseInt( right.getAttributeValue("CONTENT_WIDTH") );

				int newMaxLeft= Math.max( currentMaxLeft, anchoLeft );
				int newMaxRight= Math.max( currentMaxRight, anchoRight );
	
				int anchoVista= Integer.parseInt(eBody.getAttributeValue("WIDTH"));

				if( newMaxLeft + newMaxRight <= anchoVista && !inicio ){
					currentMaxLeft= newMaxLeft;
					currentMaxRight= newMaxRight;
					eBody.setAttribute("FUSION","CONT");
				}else{
					currentMaxLeft= anchoLeft;
					currentMaxRight= anchoRight;
					eBody.setAttribute("FUSION","INI");
				}
			}
			inicio=false;
		}
	}

	void Build(Element miTabla, org.jdom.Element body)
		throws NoSuchElementException{
		BuildDataMap();
		fusionar( body );
		Iterator itr= body.getChildren().iterator();
		Element currentTable=null, currentRow=null;
		while(itr.hasNext()){
			org.jdom.Element eBody = (org.jdom.Element)itr.next();

			if( eBody.getAttributeValue("FUSION").equals("INI") ){

				Element row= new Element("TR");
				miTabla.addContent( row );
				Element celda= new Element("TD");
				row.addContent( celda );
				currentTable= new Element("TABLE");
				currentTable.setAttribute("WIDTH","100%");
				celda.addContent( currentTable );
			}


			currentRow= new Element("TR");
			currentTable.addContent( currentRow );

			if(eBody.getName().equals("GRID")){
				BuildGrid(currentRow, eBody);
			}
			if(eBody.getName().equals("FLOW")){
				Element celda= new Element("TD");
				currentRow.addContent( celda );
				int widthTotal= Integer.parseInt( eBody.getAttributeValue("WIDTH") );
				int widthContent= Integer.parseInt( eBody.getAttributeValue("CONTENT_WIDTH") );
				if( widthContent< widthTotal-10 ){
					celda.setAttribute("COLSPAN","2");
					Element celda2= new Element("TD");
					currentRow.addContent( celda2 );	
					celda2.setAttribute("WIDTH",String.valueOf(widthTotal - widthContent) );					
				}else
					celda.setAttribute("COLSPAN","3");

				BuildGridPanel( celda, eBody);
			}
		}
	}

	void BuildGrid(org.jdom.Element row, org.jdom.Element body)
		throws NoSuchElementException {

		Element celdaLeft= new Element("TD");
		row.addContent( celdaLeft );
		celdaLeft.setAttribute("ALIGN","LEFT");
		BuildGridPanel(celdaLeft, body.getChild("LEFT"));

		int widthTotal= Integer.parseInt( body.getAttributeValue("WIDTH") );
		int widthL= Integer.parseInt( body.getChild("LEFT").getAttributeValue("CONTENT_WIDTH") );
		int widthR= Integer.parseInt( body.getChild("RIGHT").getAttributeValue("CONTENT_WIDTH") );

		Element celdaMedio= new Element("TD");
		row.addContent( celdaMedio );
		celdaMedio.setAttribute("WIDTH",String.valueOf( widthTotal - widthL - widthR - 10 )+"px");

		Element celdaRight= new Element("TD");
		row.addContent( celdaRight );
		celdaRight.setAttribute("ALIGN","RIGHT");
		BuildGridPanel(celdaRight, body.getChild("RIGHT"));
	}

	void BuildGridPanel(org.jdom.Element container, org.jdom.Element body)
		throws NoSuchElementException{
		int widthPanel= Integer.parseInt(body.getAttributeValue("WIDTH"));
		Iterator itr= body.getChildren().iterator();
		org.jdom.Element panel= new Element("TABLE");  
		panel.setAttribute("WIDTH","100%");
		container.addContent( panel );
		String buf=null;

		while(itr.hasNext()){
			Element row= new Element("TR");
			panel.addContent( row );

			org.jdom.Element itemView = (org.jdom.Element)itr.next();
			int width= Integer.parseInt(itemView.getAttributeValue("WIDTH"));
			int height= Integer.parseInt(itemView.getAttributeValue("HEIGHT"));
			int formId= Integer.parseInt(itemView.getAttributeValue("ID"));

			org.jdom.Element itemData= 
					(org.jdom.Element)m_data_Map.get(new Integer(formId));

			int externalCode= itemData.getAttributeValue("EXTERNAL_CODE")==null ? 0:
						Integer.parseInt(itemData.getAttributeValue("EXTERNAL_CODE"));

    			boolean enabled= !(itemData.getAttributeValue("ENABLE")!=null && 
						itemData.getAttributeValue("ENABLE").equals("FALSE"));

			String label= itemData.getAttributeValue("LABEL");

			if(itemView.getName().equals("ITEM")){
			    String tipo= itemView.getAttributeValue("TYPE");
			    String color= itemView.getAttributeValue("COLOR");
			    int sintax= Integer.parseInt(itemData.getAttributeValue("SINTAX"));
			    buf= itemData.getAttributeValue("LENGTH");
			    int lenght= buf!=null ? Integer.parseInt(buf):0;
			    String defaultVal= null;
			    switch(sintax){
				case helperConstant.TM_ENUMERADO:
					defaultVal= itemData.getAttributeValue("DEFAULT");
					break;
				case helperConstant.TM_ENTERO:
					defaultVal= itemData.getAttributeValue("DEFAULT");
					break;
				case helperConstant.TM_TEXTO:
					defaultVal= itemData.getText();
					break;
				case helperConstant.TM_MEMO:
					defaultVal= itemData.getText();
					break;
				case helperConstant.TM_REAL:
					defaultVal= itemData.getAttributeValue("DEFAULT");
					break;
				case helperConstant.TM_FECHA:
					defaultVal= itemData.getAttributeValue("DEFAULT");
					break;
				case helperConstant.TM_BOOLEANO:
					defaultVal= itemData.getAttributeValue("DEFAULT");
					break;
			    }
			    if(tipo.equals("TEXT")){
				System.out.println("BUILDING TEXT:" + sintax +"," + defaultVal);
			    	BuildEdit( 	row,
						itemView, 
						formId, 
						sintax, 
						externalCode, 
						defaultVal, 
						label, 
						color, 
						width, 
						widthPanel);			
			    }
			}
			if(itemView.getName().equals("TABLE")){
			}
		}
	}

	void BuildEdit(	org.jdom.Element row, org.jdom.Element itemView,int formId, int sintax, int externalCode,
			String defaultVal, String label, String color, int width, int widthPanel){
		
		Element celdaL= new Element("TD"), celdaR=null;
		row.addContent( celdaL );
		boolean estrecho= width >= widthPanel;

		if( estrecho ){
			celdaL.setAttribute("COLSPAN","2");
			Element table=new Element("TABLE");
			celdaL.addContent( table );
			Element row1=new Element("TR");
			table.addContent( row1 );
			Element titulo= new Element("TD");
			row1.addContent( titulo );
			titulo.setAttribute( "ALIGN","LEFT" );
			titulo.addContent( newFont(label,2,true) );
			Element row2= new Element("TR");
			table.addContent( row2 );
			Element texto= new Element("TD");
			row2.addContent( texto );
			texto.addContent( newFont( (defaultVal!=null ? defaultVal:""),2,false ));
		}else{	
			celdaR= new Element("TD");
			celdaR.setAttribute("ALIGN","RIGHT");
			if( defaultVal!=null )
				celdaR.addContent( newFont( defaultVal, 2, false ));

			row.addContent( celdaR );
			celdaL.addContent( newFont( label, 2, true ));
		}
	}

	Element newFont( String texto, int size, boolean bold){
		Element response=null;
		Element fuente= new Element("FONT");
		fuente.setAttribute("SIZE",String.valueOf( size ));
		fuente.setAttribute("FACE","Verdana,Tahoma,Arial");
		fuente.setText(texto);
		if( bold ){
			response= new Element("B");
			response.addContent( fuente );
		}
		else response= fuente;
		return response;
	}
}
	
	*/