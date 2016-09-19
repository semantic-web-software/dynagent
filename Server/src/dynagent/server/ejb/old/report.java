package dynagent.ejb.old;
/*package dynagent.ejb;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import java.util.*;
import java.sql.*;


class subReportIndex extends Object{
	Element rowRootCurrent;
	Element rowRootNext;
	report reportChild;
	subReportIndex( Element curr, Element next, report child){
		rowRootCurrent= curr;
		rowRootNext= next;
		reportChild= child;
	}
}

public class report extends Object{
	Element m_html, m_table;
	ArrayList m_titles;
	HashMap m_rowPosGrupos= new HashMap();
	ArrayList m_colPosGrupos= new ArrayList();
	ArrayList m_index= new ArrayList();
	int m_totalColumnas=0;
	HashMap m_subReportIndex= new HashMap();
	int m_subReportLevel=0;//0 based
	boolean m_hasSubReport=false;

	public report(  ArrayList titles, ArrayList colPosGrupos) throws SQLException{
		m_colPosGrupos=colPosGrupos;
		m_titles= titles;
	} 

	public report(  int subReportLevel, ArrayList titles, ArrayList colPosGrupos) throws SQLException{
		m_colPosGrupos=colPosGrupos;
		m_titles= titles;
		m_subReportLevel= subReportLevel;
	} 

	public void insertSubReport( int subReportLevel, ArrayList titles, ResultSet rs) throws SQLException{
		//las columnas clave del report padre y este subreport deben ser siempre la primera y la primera 
		//en el subreport ademas, si contiene nuevos subreport, la sengunda columna es la siguiente clave.

		//System.out.println("IN SUBREPORT1");
		boolean returnWithNext=true;
		while( rs.next() && returnWithNext){
		//System.out.println("IN SUBREPORT2");
			ArrayList colGr= new ArrayList();
			colGr.add( new Integer(0) );
			report rp= new report( subReportLevel, titles, colGr );

			String parentKey="";
			parentKey= rs.getString(subReportLevel);
			rs.previous();

		//System.out.println("IN SUBREPORT3:"+parentKey);

			subReportIndex sir= (subReportIndex)m_subReportIndex.get( parentKey );
			sir.reportChild= rp;
		//System.out.println("IN SUBREPORT4");

			returnWithNext= rp.insertGroup(true,parentKey, 0, 0, 0, rs,false);
		}
	}

	public void insertGroup(int levelGroup, int posIniGroup, int posEndGroup, ResultSet rs, boolean hasSubReport)
		throws SQLException{
		insertGroup(false,null, levelGroup,posIniGroup,posEndGroup,rs,hasSubReport);
	}


	public boolean insertGroup(	boolean isSubreport,
					String keyParentGroup,
					int levelGroup, 
					int posIniGroup, 
					int posEndGroup, 
					ResultSet rs, 
					boolean hasSubReport)
		throws SQLException{

		m_hasSubReport= hasSubReport;
		int numCols= rs.getMetaData().getColumnCount();
		if( levelGroup==0 ) m_totalColumnas= numCols;
		int columnasASaltar= m_totalColumnas - numCols;
		ArrayList oldValue= new ArrayList();
		ArrayList value= new ArrayList();
		ArrayList groupIni= new ArrayList();
		int[] rowSpan= new int[numCols];

		int keyOffset=0;
		if(isSubreport)
			keyOffset= m_subReportLevel+1;
		else
			if(hasSubReport) keyOffset=1;

		for(int i=0;i<numCols;i++){
			oldValue.add(null);
			value.add(null);
			groupIni.add(null);
			rowSpan[i]=1;
		}
		if(levelGroup==0) iniHtml();
		boolean inicio=true;
		Element oldRow=null;
		int indexGroup=0;
		Integer colPosEndGroup= new Integer(posEndGroup);
		ArrayList indexRowPosGroup=(ArrayList)m_rowPosGrupos.get( colPosEndGroup );
		Element endGroupRow=null;
		Element row=null;
		//System.out.println("R0: col a saltar, ini, end:"+columnasASaltar+","+posIniGroup +","+posEndGroup );
		String oldlastLevelKey=""; 
		String lastLevelKey=""; 
		while(rs.next()){
			lastLevelKey=""; 
			//System.out.println("PRE LAST KEY:"+ isSubreport+","+hasSubReport+","+m_totalColumnas+","+keyOffset);
			if(isSubreport){

				String parentKey= buildMultiKey( keyOffset - 1, rs );
				lastLevelKey= rs.getString(keyOffset);
				//System.out.println("IS SUB:"+keyOffset+","+ lastLevelKey+","+keyParentGroup+","+parentKey );

				if( !keyParentGroup.equals( parentKey ) ){
					rs.previous();
					return true;
				}
			}else{
				if(hasSubReport) 	lastLevelKey= rs.getString(1);
				System.out.println("LAST KEY:"+ lastLevelKey+","+hasSubReport);
			}

			if(levelGroup==0) row= insertRow(null);
			else
				if( inicio && indexGroup<indexRowPosGroup.size())
					endGroupRow= (Element)indexRowPosGroup.get(indexGroup);

			if(	oldRow!=null && 
				lastLevelKey.length()!=0 && 
				!lastLevelKey.equals(oldlastLevelKey)){//solo va pasar con levelGroup==0

				subReportIndex sir= new subReportIndex( oldRow, null, null);
				m_subReportIndex.put( oldlastLevelKey, sir );
				System.out.println("KEY SUB INDEX:"+ oldlastLevelKey);
				oldRow.setAttribute("KEYREPORT", oldlastLevelKey);
			}

			boolean propagacionCambioToRight=false;
			for(int c= keyOffset; c< numCols; c++){
				System.out.println("R1");
				String v=getValue( rs, c + 1 );
				value.set(c, v);
				System.out.println("CICLO C:" + c + ",value:"+v);
				System.out.println("R2");
				boolean saltarCeldasIzqResumen=  	levelGroup!=0 &&
										c>= posIniGroup &&
									   	(c<= posEndGroup + columnasASaltar);

				boolean cambioGrupo=    ((lastLevelKey.length()!=0 && 
								!lastLevelKey.equals(oldlastLevelKey)) ||
								inicio ||
								propagacionCambioToRight ||
								(oldValue.get(c)!=null && value.get(c)==null) ||
								(oldValue.get(c)==null && value.get(c)!=null) || 
								(oldValue.get(c)!=null && value.get(c)!=null  && 
								!((String)oldValue.get(c)).equals(value.get(c))));

				if(c==0 && levelGroup!=0 ){
					if(cambioGrupo && indexGroup<indexRowPosGroup.size())
						endGroupRow= (Element)indexRowPosGroup.get(indexGroup++);
					
					row= insertRow(endGroupRow);
					row.setAttribute("LEVEL", String.valueOf(levelGroup));
				}
				System.out.println("R3");
				Integer colInt= new Integer( c );

				if(cambioGrupo){
					System.out.println("R4");
					propagacionCambioToRight= true;
					rowSpan[c]=1;
					String val= value.get(c)==null ? "":(String)value.get(c);
					System.out.println("R4_1");

					Element cell= null;

					cell=insertCell( false, row, val );
					groupIni.set(c,cell);

					if( levelGroup!=0 && c==posIniGroup ){
						cell.setAttribute("COLSPAN", 	String.valueOf(	posEndGroup + 1 -
														posIniGroup + 
														columnasASaltar ));
						cell.setText("");
					}
					if( levelGroup!=0 && c > posIniGroup ){
						cell.setAttribute("BGCOLOR", "#C8D1D7");
					}
					if(levelGroup==0 && m_colPosGrupos.indexOf( colInt )!= -1 ){//la segunda
									//condicion me indica que esta columna debe ser memorizada
									//cuando hay cambio de grupo
						ArrayList rowPos=null;
						if( !m_rowPosGrupos.containsKey(colInt)){
							rowPos= new ArrayList();
							m_rowPosGrupos.put( colInt, rowPos );
						}else rowPos=(ArrayList)m_rowPosGrupos.get( colInt );
						rowPos.add( row );	
					}
					System.out.println("R4_2");

				}else{
					System.out.println("R5");
					rowSpan[c]++;
					Element cellIniGr= (Element)groupIni.get(c);
					if(rowSpan[c]>1) cellIniGr.setAttribute("ROWSPAN",String.valueOf(rowSpan[c]));
				}

			}
			inicio=false;
			System.out.println("R6");

			for( int c=keyOffset; c< numCols; c++){
				oldValue.set(c, value.get(c)); 
			}
			oldRow= row;
			oldlastLevelKey= lastLevelKey;
			System.out.println("R7");

		}
		System.out.println("R8");

		if(levelGroup==0){
			for(int c=keyOffset; c< numCols; c++){
				System.out.println("R8_1");

				Integer colInt= new Integer( c );
				if( m_colPosGrupos.indexOf( colInt )!= -1 && m_rowPosGrupos.size()>0){
					System.out.println("R8_2");

					ArrayList rowPos=(ArrayList)m_rowPosGrupos.get( colInt );
					rowPos.remove(0);
					rowPos.add( null ); //asi tengo marcados inicios de grupos
				}
			}
			if(	lastLevelKey.length()!=0 ){
				subReportIndex sir= new subReportIndex( row, null, null);
				m_subReportIndex.put( lastLevelKey, sir );
				oldRow.setAttribute("KEYREPORT", lastLevelKey);
			}

		}
		return false;
	}

	public String buildMultiKey( int numeroColsKeys, ResultSet rs ) throws SQLException{
		String res="";
		for( int c= 0; c < numeroColsKeys; c++ ){
			res+= (c>0 ? "#":"") + rs.getString( c+1 );
		}
		return res;
	}

	public Element generate(){
		for(int r=0; r< m_index.size();r++){
			Element row= (Element)m_index.get( r );
			if(row!=null){
				m_table.addContent( row );
				String key= row.getAttributeValue("KEYREPORT");
				if(key!=null){
					subReportIndex sir= (subReportIndex)m_subReportIndex.get( key );
					if( sir==null ){
						System.out.println("REPORT ERROR, no sir para key:" + key );
						continue;
					}
					report rp= sir.reportChild;
					if( rp==null ){
						System.out.println("REPORT ERROR, no report para sir de key:" + key );
						continue;
					}
					System.out.println("REPORT GENERATE, subreport de key:" + key );
					Element rowSubReportContainer= new Element("TR");
					m_table.addContent(rowSubReportContainer);
					Element cell= new Element("TD");
					rowSubReportContainer.addContent(cell);
					cell.setAttribute("COLSPAN", String.valueOf(m_totalColumnas - (m_subReportLevel+1)));
													// que son las col visibles
					Element subTable= rp.generate();
					if( subTable==null ){
						System.out.println("REPORT ERROR, subreport nulo con key:"+ key );
						continue;
					}else{
						cell.addContent(subTable);
						cell.setAttribute("ALIGN","CENTER");
					}
				}
			}
		}
		return m_html;
	}

	private void iniHtml(){
		m_table= new Element("TABLE");
		String style="border-collapse:collapse";
		if(m_subReportLevel==0){
			m_html= new Element("HTML");
			Element head= new Element("HEAD");
			m_html.addContent(head);
			Element title= new Element("TITLE");
			head.addContent(title);
			title.setText("PRUEBA");
			Element body= new Element("BODY");
			m_html.addContent(body);
			body.addContent(m_table);
			style+=";width:780px";
		}else
			m_html= m_table;

		m_table.setAttribute("BORDER","1");
		m_table.setAttribute("BORDERCOLOR","#0000");
		m_table.setAttribute("STYLE",style);
		
		buildTitles( hallaProfundidadTitulos( m_titles), 0, new ArrayList(), m_titles );
	}

	private int buildTitles( int maxProfundidad, int level, ArrayList rows, ArrayList lista ){
		System.out.println("IN LEVEL:"+level);
		int numColumns=0;
		Element row= getRowTitle( rows, level );
		row.setAttribute("BGCOLOR", "#D4D4D4");
		ArrayList thisLevelCells= new ArrayList();
		for(int i=0; i< lista.size(); i++){
			if( lista.get(i) instanceof ArrayList ){
				System.out.println("LOCALIZADO ARRAY:"+(String)lista.get(i+1));
				int subNumCols= buildTitles( maxProfundidad, level+1, rows, (ArrayList) lista.get(i++) );
				Element cell= insertCell(true, row, (String)lista.get(i));
				if( subNumCols>1 ) cell.setAttribute( "COLSPAN", String.valueOf(subNumCols) );
				numColumns+= subNumCols;
			}else{
				numColumns++; 
				System.out.println("LOCALIZADO COL:"+(String)lista.get(i));
				Element cell= insertCell(true, row, (String)lista.get(i));	
				thisLevelCells.add( cell );
			}
		}
		if( level< maxProfundidad-1){ //level es cero based
			int rowspan= maxProfundidad - level;
			for( int j=0; j< thisLevelCells.size(); j++ ){
				Element cell= (Element)thisLevelCells.get( j );
				cell.setAttribute( "ROWSPAN", String.valueOf( rowspan ));
			}	
		}
		return numColumns;
	}

	private int hallaProfundidadTitulos( ArrayList tt){
		int subProfundidad=1;
		for(int i=0; i<tt.size(); i++){
			if( !(tt.get(i) instanceof ArrayList) ) continue;
			subProfundidad= Math.max( subProfundidad, 1 + hallaProfundidadTitulos((ArrayList)tt.get(i)));
		}
		return subProfundidad;
	}
	
	private Element getRowTitle( ArrayList rows, int level ){
		Element row= level<rows.size() ? (Element)rows.get( level ):null;
		if(row==null){
			row= insertRow(null);
			rows.add( row );
		}
		return row;
	}


	private Element insertRow(Element lastRow){
		Element row= new Element("TR");
		if(lastRow==null)	m_index.add(row);
		else{
			int pos= m_index.indexOf( lastRow );
			m_index.add(pos, row);
		}
		return row;
	}

	private Element insertCell( boolean isTitle, Element row, String val){
		Element cell= new Element(isTitle ? "TH":"TD");
		if(val!=null){
			cell.setText(val);
		}
		row.addContent(cell);
		return cell;
	}

	private String getValue( ResultSet rs, int col ) throws SQLException{
		boolean exito=false;
		ResultSetMetaData rsm= rs.getMetaData();
		String val="";
		System.out.println("RS de :" + rsm.getColumnCount() + ", col:"+col);
		if(rsm.getColumnType(col)==Types.DATE){
			val= String.valueOf(rs.getDate(col));
			exito=true;
		}
		if(rsm.getColumnType(col)==Types.INTEGER){
			val= String.valueOf(rs.getInt(col));
			exito=true;
		}
		if(!exito){
			val= rs.getString(col);
			exito=true;
		}
		if(!exito) val="ERROR COL:"+col+",type:"+rsm.getColumnType(col);
		System.out.println("RS res:"+val);
		return val;
	}
}
	

	*/