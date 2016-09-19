package dynagent.ejb.old;
/*package dynagent.ejb;

import org.jdom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Comparator;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;
import java.awt.Dimension;

import dynagent.knowledge.metaData;

interface priorizable{
	public int getPrioridad();
}

class ordenaPrioridades implements Comparator{
	boolean ascendente=false;
	ordenaPrioridades( ){
		this.ascendente=false;
	}
	ordenaPrioridades( boolean ascendente ){
		this.ascendente=ascendente;
	}
	public int compare( Object ob1, Object ob2 ) throws ClassCastException{
		if( !(ob1 instanceof priorizable) || !(ob2 instanceof priorizable))
			throw new ClassCastException("ERROR EN COMPARADOR PRIORIDADES");
		priorizable p1=(priorizable)ob1;
		priorizable p2=(priorizable)ob2;
		if( p1.getPrioridad()==p2.getPrioridad() ) return 0;
		if( p1.getPrioridad()<p2.getPrioridad() ) return (ascendente ? -1:1);
		return (ascendente ? 1:-1);
	}
}

class combina{

   ArrayList lista=new ArrayList();
   int ini, pesoMax;
   ArrayList pesos;
   int[] orden;
   combina( int ini, int pesoMax,  ArrayList pesos, int[] orden){
	this.ini=ini;
	this.pesoMax=pesoMax;
	this.pesos=pesos;
	this.orden=orden;
	combinar();
   }

   private void combinar(){
	for( int index=ini; index<pesos.size(); index++ ){
		if( orden!=null && index>ini ){
			//System.out.println("ORDER "+ orden[index-1]+","+orden[index]);
			break;
		}

		if( orden!=null && index>ini && orden[index-1]<orden[index] ){
			//System.out.println("BREAKORDER");
			break;
		}

		int pesoRestante=pesoMax- ((Integer)pesos.get( index )).intValue();
		if( pesoRestante <0 ) break;
		if( pesoRestante ==0 ){
			ArrayList combina= new ArrayList();
			combina.add( new Integer( index ) );
			lista.add( combina );
			break;
		}

		combina t= new combina( index+1, pesoRestante, pesos, orden);
		if( t.lista.size()==0 ){
			ArrayList combina= new ArrayList();
			combina.add( new Integer( index ) );
			lista.add( combina );
		}

		for( int i=0; i<t.lista.size(); i++ ){
			ArrayList combina= new ArrayList();
			combina.add( new Integer( index ) );
			combina.addAll( (ArrayList)t.lista.get(i) );
			lista.add( combina );
		}
	}
   }

   int getCasoPesoMax(){
	int maxPeso=0;
	int bestCombina=0;
	for( int i=0; i< lista.size(); i++){
		int peso= getPeso( i );
		if( i==0 ) maxPeso=peso;
		if( peso>maxPeso ){
			maxPeso=peso;
			bestCombina=i;
		}
	}
	return bestCombina;
    }

    int getPeso( int caso ){
	ArrayList comb= (ArrayList)lista.get(caso);
	int peso=0;
	for( int i=0; i<comb.size(); i++ ){
		Integer currPeso=(Integer)pesos.get(((Integer)comb.get( i )).intValue());
		peso+=  currPeso.intValue();
	}
	return peso;
    }
}

class itemPointer extends Object implements Comparable,priorizable{
	public Element viewItem;
	public int ancho=0, alto=0;
	public boolean toFlow=false;
	public boolean ordenaPorAncho=true;
	public static boolean considerarPreOrden=false;
	public int tm, tapos, prioridad, longitud=-1;
	public Integer iTAPOS, group;
	public String id;
	String label;
	boolean comentado=false;
	int order=0;
	boolean visible=true;
        boolean multivalued=false;

	void setConsideraPreOrden( boolean c){
		considerarPreOrden=c;
	}

	itemPointer( int prioridad, Element item, int order){
		//usado para tablas
		this.order=order;
		id= item.getAttributeValue("ID");
		tm=viewBalancer.TM_PANEL;
		label=item.getAttributeValue("LABEL");
		iTAPOS= new Integer( id );
		this.prioridad=prioridad;
		viewItem= item;
	}
	public int getPrioridad(){
		return prioridad;
	}


	itemPointer( 	boolean multivalued, Integer group, int tm, String id, int tapos, int prioridad,
			String mask, boolean enable, boolean nullable, String label, int longitud,
			boolean comentado, int order, boolean visible){

		this( 	multivalued,new org.jdom.Element("ITEM"), group, tm, id, tapos,prioridad,
					mask,enable, nullable,label,longitud,
					comentado, order, visible);

	}

	public boolean equals( Object item ){
		if( !(item instanceof itemPointer) ) return false;
		itemPointer ipB=(itemPointer)item;
		return ipB.id.equals( id )&& tm==ipB.tm && iTAPOS.equals(ipB.iTAPOS) && group.equals(ipB.group);
	}

	itemPointer( 	boolean multivalued,Element item, Integer group, int tm, String id, int tapos, int prioridad,
			String mask, boolean enable, boolean nullable, String label, int longitud,
			boolean comentado, int order, boolean visible){
		viewItem=item;
                this.multivalued=multivalued;
		this.visible=visible;
		this.order=order;
		this.comentado=comentado;
		this.longitud=longitud;
		this.id= id;
		this.tapos= tapos;
		this.group=group;
		iTAPOS= new Integer( tapos );
		this.tm=tm;
		this.label=label;
		this.prioridad=prioridad;
		viewItem.setAttribute("ID",String.valueOf(id));
		viewItem.setAttribute("LABEL",label);
                if( multivalued )
                    viewItem.setAttribute("MULTIVALUED","TRUE");
		if( tapos!=0 )
			viewItem.setAttribute("TA_POS",String.valueOf(tapos));
		if( mask!=null )
			viewItem.setAttribute("MASK",mask);
		if(!enable)
			viewItem.setAttribute("ENABLE","FALSE");

		if(!visible)
			viewItem.setAttribute("VISIBLE","FALSE");


		viewItem.setAttribute("NULLABLE", (nullable ? "TRUE":"FALSE"));
                System.out.println("TM "+tm);
		String type=null;
		switch(tm){
			case helperConstant.TM_TABLA:
				type="TABLE";
				break;
			case helperConstant.TM_ENUMERADO:
				type="LIST";
				break;
			case helperConstant.TM_ENTERO:
				type="TEXT";
				break;
			case helperConstant.TM_REAL:
				type="TEXT";
				break;
			case helperConstant.TM_TEXTO:
				type="TEXT";
				break;
			case helperConstant.TM_FECHA:
				type="TEXT";
				break;
                        case helperConstant.TM_FECHAHORA:
                            type="TEXT";
                            break;
			case helperConstant.TM_MEMO:{
				type="TEXT";
				break;
			}
			case helperConstant.TM_BOOLEANO:{
				type="CHECK";
				break;
			}
			case helperConstant.TM_BOOLEANO_EXT:{
				type="CHECK";
				break;
			}
			case helperConstant.TM_IMAGEN:{
				type="IMAGEN";
				break;
			}
		}
		viewItem.setAttribute("TYPE", type);
		viewItem.setAttribute("ID_TM", String.valueOf( tm ));
	}

	public void setDimension( Dimension dim ){
		this.ancho=(int)dim.getWidth();
		this.alto=(int)dim.getHeight();
		viewItem.setAttribute("WIDTH", String.valueOf((int)dim.getWidth()));
		viewItem.setAttribute("HEIGHT", String.valueOf((int)dim.getHeight()));
	}

	int getWidth(){
		if( !visible ) return 0;
		return ancho+getAnchoButton();
	}
	int getTotalWidth(){
		if( !visible ) return 0;
		return getWidth()+getColumnAnchoLabel();
	}
	int getColumnAnchoLabel(){
		if( !visible ) return 0;
		if( 	viewItem.getAttributeValue("WIDTH_LABEL")!=null &&
			viewItem.getAttributeValue("TOPLABEL")==null )
			return Integer.parseInt( viewItem.getAttributeValue("WIDTH_LABEL") );
		else return 0;
	}
	int getAnchoButton(){
		if( !visible ) return 0;
		if( 	viewItem.getAttributeValue("WIDTH_BUTTON")!=null &&
			viewItem.getAttributeValue("TOPLABEL")==null )
			return Integer.parseInt( viewItem.getAttributeValue("WIDTH_BUTTON") );
		else return 0;
	}
	int getHeight(){
		if( !visible ) return 0;
		return alto;
	}
	int getTotalHeight(){
		if( !visible ) return 0;
		if( viewItem.getAttributeValue("TOPLABEL")==null )
			return getHeight();
		else{
			return getHeight()+Integer.parseInt( viewItem.getAttributeValue("HEIGHT_LABEL") );
		}
	}
	public Element getViewItem(){
		return viewItem;
	}

	public int compareTo(Object o)
		throws ClassCastException{
		if(!(o instanceof itemPointer))
			throw new ClassCastException();
		itemPointer b=(itemPointer) o;
		if( considerarPreOrden ){
			if( prioridad!=b.prioridad ){
				return ( prioridad < b.prioridad ? -1:1 );
			}
			if( order==b.order ) return 0;
			else return (order< b.order ? -1:1);
		}

		if(ordenaPorAncho){
			if(getWidth() < b.getWidth()) return -1;
			if(getWidth() == b.getWidth()){
				if( getColumnAnchoLabel()< b.getColumnAnchoLabel() ) return -1;
				if( getColumnAnchoLabel()==b.getColumnAnchoLabel() ) return 0;
			}
		}else{
			if(alto < b.alto) return -1;
			if(alto == b.alto) return 0;
		}
		return 1;
	}
}


class columna extends ArrayList implements priorizable{
	int lonMax=0;
	int lonRef=0;
	int gruesoMax=0;
	int prioridad=0;
	int WidthGap=0,LengthGap=0;
	final static int HORIZ=0;
	final static int VERT=1;
	int type;
	columna( int type, int lonRef, int WidthGap, int LengthGap){
		this.lonRef= lonRef;
		this.type=type;
		this.WidthGap=WidthGap;
		this.LengthGap= LengthGap;
	}
	public int getPrioridad(){
		return prioridad;
	}
	public void add( itemPointer ip ){
		super.add( ip );
		int ancho=ip.getTotalWidth();
		int alto=ip.getTotalHeight();

		//System.out.println("añade "+ancho+","+alto);
		prioridad+= ip.prioridad;
		if( type==HORIZ ){
			lonMax+= ancho;
			gruesoMax= Math.max( gruesoMax, alto+WidthGap*2 );

		}else{
			lonMax+= alto;
			gruesoMax= Math.max( gruesoMax, ancho+WidthGap*2 );
		}

		if( size()==1 ) lonMax+=LengthGap*2;
		else lonMax+=LengthGap;

		if( ancho==0 || alto==0 ){
			//System.out.println("AREA CERO:ANCHO,ALTO "+ancho+","+alto);
			try{
			System.out.println(jdomParser.returnXML( ip.viewItem ));
			}catch(Exception e){;}
		}
	}
	public long getArea(){
		return lonMax*gruesoMax;
	}
	public long getLength(){
		return lonMax;
	}
}

class panelFactory extends ArrayList{
	double eficaciaMin;
	double relAspecto=0;
	int alto;
	ArrayList items;
	ArrayList columnas= new ArrayList();
	ArrayList flow=new ArrayList();
	cfgView cfg;
	int anchoVentana;
	long m_minArea;

	panelFactory( cfgView cfg, long minArea, int alto, int anchoVentana, ArrayList items ){
		this.cfg=cfg;
		this.alto=alto;
		this.items=items;
		m_minArea=minArea;
		this.anchoVentana=anchoVentana;
		//System.out.println("********PANEL FACTORY******** ALTO:"+alto+",SIZE ITEMS:"+items.size());
		build();
		ArrayList pesos= getPesos();
		for( int p=0; p<pesos.size();p++){
			//System.out.println("COLUMNA:"+p+",ANCHO:"+pesos.get(p));
			columna col= (columna)columnas.get( p );
			for( int r=0; r<col.size();r++){
				itemPointer ip=(itemPointer)col.get(r);
				//System.out.println("ROW "+r+","+ip.label);
			}
		}
		//System.out.println("END");
	}

	ArrayList buildGrid( ArrayList indices ){
		ArrayList res=new ArrayList();
		for( int i=0; i< indices.size(); i++ ){
			Integer index= (Integer)indices.get( i );
			res.add( columnas.get( index.intValue() ));
		}
		return res;
	}

	void build(){
		ArrayList pendientes=new ArrayList();
		buildPaneles( cfg, alto, items, pendientes );
		if( pendientes.size()>0 ){
			ArrayList restoPend= new ArrayList();
			cfgView cfgRest= (cfgView)cfg.clone();
			int newAlto= alto;//(int)(alto*cfg.umbralSaltoColumn);
			cfgRest.umbralSaltoColumn= cfg.umbralSaltoColumnOpcional/((double)newAlto);
			//esta última asignacion hace que el nuevo umbral de salto limite el alto respecto
			// al nuevo alto, mas bajo,como lo limitaria el umbral de salto opcional respecto al alto original
			buildPaneles( cfgRest, newAlto, pendientes,restoPend );
			flow.addAll( restoPend );
		}
		flow.addAll( pendientes );
	}

	void buildPaneles(cfgView cfg, int alto, ArrayList items, ArrayList pendientes){
		if( items.size()==0 ) return;
		int ancho=0;
		double relax= 1;
		if( m_minArea< cfg.umbralArea ){
			relax= 1+(double)(cfg.umbralArea-m_minArea)/(double)cfg.umbralArea;
		}
		columna col= null;
		boolean dentroUmbralSalto=false, inSaltoOpcional=false;
		int anchoMedioLabel=0, sumaAnchoLabel=0, numItemsCol=1;
		boolean imprime=false;
		for( int i=0; i< items.size(); i++ ){
			itemPointer ip= (itemPointer)items.get(i);
			if( ip.viewItem.getName().equals("TABLE") )
				imprime=true;
			else imprime=false;

			boolean topLabel=	ip.viewItem.getAttributeValue("TOPLABEL")!=null &&
						ip.viewItem.getAttributeValue("TOPLABEL").equals("TRUE");

			if(	topLabel  && ip.getWidth()>anchoVentana - 40 ||
				!topLabel && ip.getWidth()+ip.getColumnAnchoLabel() >anchoVentana - 40){
				flow.add( ip );
				continue;
			}

			if(imprime){
				if( col!=null )
					System.out.println(ip.label+" alto,altoCol,altoItem:"+alto+","+
							   col.lonMax +","+ ip.getTotalHeight());
				else
					System.out.println(ip.label+" alto,altoCol,altoItem:"+alto+","+
							   0 +","+ ip.getTotalHeight());
			}
			try{
				System.out.println(jdomParser.returnXML(ip.viewItem));
			}catch(Exception e){;}

			//System.out.println("ANCHO "+ip.getWidth());
			int oldSumaAnchoLabel=sumaAnchoLabel;
			if( sumaAnchoLabel==0 && !topLabel )
				sumaAnchoLabel=ip.getColumnAnchoLabel();

			int oldAnchoMedioLabel=anchoMedioLabel;
			anchoMedioLabel= sumaAnchoLabel/numItemsCol;

			if(	!topLabel &&
				((double)Math.abs( ip.getColumnAnchoLabel()-anchoMedioLabel ))/((double)anchoMedioLabel) >
						(relax*cfg.umbralIncAnchoLabel) ){
				flow.add( ip );
				anchoMedioLabel=oldAnchoMedioLabel;
				sumaAnchoLabel=oldSumaAnchoLabel;
				continue;
			}
			if( ip.prioridad>0 ){
				flow.add( ip );
				anchoMedioLabel=oldAnchoMedioLabel;
				sumaAnchoLabel=oldSumaAnchoLabel;
				continue;
			}
			//System.out.println("PASO");
			if( col==null ){
				ancho= ip.getWidth();
				col= new columna( columna.VERT, ancho, cfgView.Hgap, cfgView.GridVgap);
			}
			double incAncho=((double)(ip.getWidth()-ancho))/((double)ancho);
			if(imprime) System.out.println("ANCHO, anchoIP:"+ancho+","+ip.getWidth()+","+incAncho);
			boolean supAncho= incAncho >relax*cfg.umbralDeMejoraDeCompactacionArea*4;
			boolean supAlto=col.lonMax + ip.getTotalHeight() > relax*alto ;
			dentroUmbralSalto=((double)col.lonMax)/((double)alto) - cfg.umbralSaltoColumn >=0 ;
			inSaltoOpcional=((double)col.lonMax)/((double)alto) - cfg.umbralSaltoColumnOpcional>=0 ;
			if(imprime) System.out.println("SUP ANCHO ALTO "+supAncho+","+supAlto+","+dentroUmbralSalto);
			if( supAncho ){
				numItemsCol=1;
				sumaAnchoLabel=0;

				if( 	col.lonMax > alto || ( dentroUmbralSalto && inSaltoOpcional ) ){
					addColumn( col );
					if(imprime) System.out.println("SUP ANCHO, AñadIDA COLUM");
				}else{
					if(imprime) System.out.println("SUP ANCHO, PERDIDA COLUM");
					pendientes.addAll( col );
				}
				ancho= ip.getWidth();
				col= new columna( columna.VERT,ancho, cfgView.Hgap, cfgView.GridVgap );
			}else{
				if( supAlto ){
					numItemsCol=1;
					sumaAnchoLabel=0;
					if(imprime) System.out.println("SUP ALTO "+col.size());
					if( col.size()>0 ){
						addColumn( col );
					}
					ancho= ip.getWidth();
					col= new columna( columna.VERT,ancho, cfgView.Hgap, cfgView.GridVgap );
				}else{
					if( !topLabel ){
						numItemsCol++;
						sumaAnchoLabel+=ip.getColumnAnchoLabel();
					}
				}
			}
			col.add( ip );
		}
		if( col!=null ){
			inSaltoOpcional=((double)col.lonMax)/((double)alto) - cfg.umbralSaltoColumnOpcional>=0 ;
			//actualizo porque puede que se haya creado nueva col
			if( inSaltoOpcional ) addColumn( col );
			else pendientes.addAll( col );
		}
	}

	public void addColumn( columna col ){
		for( int c=0; c<columnas.size();c++){
			columna curr=(columna)columnas.get(c);
			if( curr.lonRef > col.lonRef ){
				columnas.add( c, col );
				return;
			}
		}
		columnas.add( col );
	}

	public ArrayList getPesos(){
		ArrayList pesos= new ArrayList();

		for( int c=0; c< columnas.size(); c++ ){
			columna col= (columna)columnas.get( c );
			pesos.add( new Integer(col.gruesoMax) );
		}
		return pesos;
	}
}


class panelBalancer{
	ArrayList m_grids= new ArrayList();//es una lista de grid, donde un grid es una lista de columnas
	ArrayList m_flow= new ArrayList();// Al inicio flow no es más que una lista de items, pero tras contruir el flow
					//es una lista de rows, un row es una columna horizontal.
	cfgView m_cfg;
	ArrayList m_items;
	long m_area;
	int m_ancho;
	double m_bondad=0;
	public panelBalancer( int ancho, cfgView cfg, ArrayList items ){
		m_items=items;
		m_ancho=ancho;
		m_cfg=cfg;
		double bondadMax=0;
		long areaLimInf= hallaAreaLista( items );
		//System.out.println("AREA MIN "+areaLimInf);
		long areaBest=0;

		ArrayList gridsBest= new ArrayList();
		ArrayList flowBest= new ArrayList();
		double umbralDeMejora=cfg.umbralDeMejoraDeCompactacionArea;
		iteraPanel( umbralDeMejora, items, m_grids,m_flow );
		m_area= hallaArea( m_grids, m_flow );
		m_bondad= getBondad( areaLimInf, hallaEstetica( m_grids, m_flow ), ((double)areaLimInf)/((double)m_area) );
	}

	public void iteraPanel( double umbralDeMejora, ArrayList items, ArrayList grids, ArrayList flow ){
		double incMejora=1+umbralDeMejora;
		boolean comienzo=true, soloFlow=true;
		//System.out.println("-------------------------------ITERA size "+items.size()+","+grids.size()+","+flow.size());
		while( items.size()>0 ){
			long areaLimInfPendiente=hallaAreaLista( items );
			long minArea= (long)(areaLimInfPendiente*incMejora);
			//System.out.println("AREA MIN "+minArea);
			double bondadMax=0;
			long alto= (long)(((double)minArea)/((double)m_ancho));
			if( comienzo && soloFlow ){
				soloFlow= (((double)m_ancho)/((double)alto)) > cfgView.minRelAspectSoloFlow;
				//System.out.println("SOLOFLOW "+soloFlow+","+(((double)m_ancho)/((double)alto)));
				if( soloFlow ){
					boolean existePreorden=false;
					for( int j=0;j<items.size();j++){
						itemPointer ip=(itemPointer)items.get(j);
						if( ip.order>0 ){
							existePreorden=true;
							break;
						}
					}
					//soloFlow= existePreorden;
				}
				comienzo=false;
			}
			//System.out.println("ALTO "+alto);
			long altoOrig=alto;
			long bestAlto=alto;
			int signo=-1, numCambios=0;
			boolean ultimaIteracionMejoramos=false, acaboDeCambiar=false;
			int i=0;
			while( i<=m_cfg.pasosItera*2 && alto>altoOrig*0.4 ){
				i++;
				//System.out.println("ALTO "+alto);
				ArrayList iteraItems= (ArrayList)items.clone();
				ArrayList iteraFlow=new ArrayList();
				ArrayList grid= procesaGrid( minArea,(int)alto,m_ancho,iteraItems,iteraFlow,soloFlow );//iteraItems queda con el resto
				//System.out.println("PROGR1");
				long areaCurrGrid=hallaArea( grid );
				long currIncArea= (long)( incMejora*hallaAreaLista(iteraItems)+
							hallaAreaListaFlow(iteraFlow,m_ancho,incMejora)+
							areaCurrGrid );
				//System.out.println("PROGR2");
				int numGds= grids.size()+ (grid==null ? 0:1);
				if( numGds==1 ) numGds=0;
				double parcialEstetica= ((double)cuentaGrid(grid))/((double)(items.size())+numGds);
				double gradoCompactacion= ((double)areaLimInfPendiente)/((double)currIncArea);
				System.out.println("SUBITERA "+items.size()+","+
								iteraItems.size()+",COL"+ (grid==null ? 0:grid.size())+
								",G "+
								(cuentaGrids(grids)+(grid==null ? 0:cuentaGrid(grid)))+",F "+
								iteraFlow.size());
				double bondad= getBondad(minArea, parcialEstetica,gradoCompactacion);
				//System.out.println("ESTE, COMPAC "+bondad+","+parcialEstetica+","+gradoCompactacion);
				if( iteraFlow!=null && iteraFlow.size()>0 )
					System.out.println("LABELFLOWFIRST:"+((itemPointer)iteraFlow.get(0)).label);

				double bondadMin=getMinBondad( minArea,items.size());
				if( 	bondad> bondadMax ||
					bondad==bondadMax && signo==-1){//si signo==-1 estamos reduciendo area
					bondadMax= bondad;
					bestAlto= alto;
					minArea= currIncArea;
					if( bondad>=bondadMin ) ultimaIteracionMejoramos=true;
					else ultimaIteracionMejoramos=false;
				}else{
					if( bondad>= bondadMin ){
						if( ultimaIteracionMejoramos ) break;
						signo=-1*signo;
					}else{
						if( !acaboDeCambiar ){
							acaboDeCambiar=true;
							signo=-1*signo;
							continue;
						}else
							acaboDeCambiar=false;
					}
					ultimaIteracionMejoramos=false;
				}
				alto= alto + i*(int)(getMultiplicadorIteracionOptimo(items.size(),bondad)*m_cfg.altoMedioDeCampo/2*signo);
				//break;
			}
			ArrayList grid= procesaGrid( minArea,(int)bestAlto, m_ancho, items, flow, soloFlow );
			if( grid!=null ) grids.add( grid );
		}
		buildFlow(flow);
	}
	public double getMultiplicadorIteracionOptimo(int numItems,double bondad){
		double bondadOptima= m_cfg.bondadOptimaMinima*((double)numItems)/((double)(1+numItems));
		if( bondad<=1 && bondad>= bondadOptima )
			return 1;
		return 1.0/(bondad*(2-bondad));
	}

	public double getBondad( long areaMinima, double gradoEstetica,double gradoCompactacion ){
		if( areaMinima < cfgView.umbralArea )
			return gradoEstetica;
		return gradoEstetica*gradoCompactacion;
	}

	public double getMinBondad( long areaMinima, int numItems ){
		if( areaMinima < cfgView.umbralArea )
			return m_cfg.bondadMinima;
		return m_cfg.bondadMinima*((double)numItems)/((double)(1+numItems));
	}
	public double hallaEstetica( ArrayList grids, ArrayList flow ){
		long pesoEstetica=0, restoPesoFlow=0;
		for( int g=0; g<grids.size();g++){
			ArrayList grid=(ArrayList)grids.get(g);
			columna firstCol= (columna)grid.get(0);
			pesoEstetica+= firstCol.getLength()*m_ancho/grids.size();
		}
		for( int c=0; c<flow.size();c++){
			columna col=(columna)flow.get(c);
			long area=col.getArea();
			if(col.size()==1){
				pesoEstetica+= area;
			}else{
				pesoEstetica+=(long)(area*m_cfg.esteticaMinDeFlow);
				restoPesoFlow+=area;
			}

		}
		if( pesoEstetica==0 ) return 0;
		double grado= ((double)pesoEstetica)/((double)(pesoEstetica + restoPesoFlow));
		//System.out.println(" PESO ESTE "+grado+","+pesoEstetica + "," + restoPesoFlow );
		return grado;
	}


	public static double cuentaGrids( ArrayList grids ){
		int cuenta=0;
		for( int g=0; g<grids.size();g++){
			ArrayList grid=(ArrayList)grids.get(g);
			cuenta+=cuentaGrid(grid);
		}
		return cuenta;
	}

	public static double cuentaGrid( ArrayList grid ){
		int cuenta=0;
		if( grid==null ) return 0;
		for( int c=0; c<grid.size();c++){
			columna col=(columna)grid.get(c);
			cuenta+=col.size();
		}
		return cuenta;
	}

	public double cuentaFlow( ArrayList flow ){
		int cuenta=0;
		for( int c=0; c<flow.size();c++){
			columna col=(columna)flow.get(c);
			cuenta+=col.size();
		}
		return cuenta;
	}

	public void reOrdenaItemsPorPrioridad( columna col ){
		Object[] lista=col.toArray();
		Arrays.sort( lista, new ordenaPrioridades() );
		col.clear();
		for( int i=0;i<lista.length;i++)
			col.add( lista[i]);
	}

	public void ordenaColumnas( ArrayList cols ){
		for( int c=0; c<cols.size();c++){
			columna col=(columna)cols.get(c);
			reOrdenaItemsPorPrioridad(col);
		}

		Object[] lista=cols.toArray();
		Arrays.sort( lista, new ordenaPrioridades() );
		cols.clear();
		for( int i=0;i<lista.length;i++)
			cols.add( lista[i]);
	}

	public void ordenaPrioridades(){
		for( int g=0; g<m_grids.size();g++){
			ArrayList grid=(ArrayList)m_grids.get(g);
			ordenaColumnas( grid );
		}
		ordenaColumnas( m_flow );
	}

	private void buildFlow(ArrayList flow){
		ArrayList tmpItems=ordenaListaPorAncho( flow );
		flow.clear();
		while( tmpItems!=null && tmpItems.size()>0 ){
			//System.out.println("TMPSIZE:"+tmpItems.size());
			tmpItems= iteraFlow( tmpItems, flow );
		}
	}

	public static ArrayList ordenaListaPorAncho( ArrayList lista ){
		Object[] array= lista.toArray();
		itemPointer.considerarPreOrden=false;
		Arrays.sort(array);
		int maxOrder=0;
		for( int i=0;i<array.length;i++){
			itemPointer ip=(itemPointer)array[i];
			maxOrder=Math.max( maxOrder,ip.order);
		}
		if( maxOrder>0 ){
			boolean empieza=false;
			for( int i=0;i<array.length;i++){
				itemPointer ip=(itemPointer)array[i];
				if( ip.order>0 ) empieza=true;
				if( empieza && ip.order==0 ) ip.order= maxOrder+1;
			}
			itemPointer.considerarPreOrden=true;
			Arrays.sort(array);//se supone que el ordenado anterior se antiene para igual preOrden
			itemPointer.considerarPreOrden=false;
		}

		return new ArrayList( Arrays.asList(array) );
	}

	private ArrayList iteraFlow( ArrayList items, ArrayList flow ){
		ArrayList pesos=new ArrayList();
		int[] ordenes= new int[items.size()];
		if( items.size()==0 )
			return null;

		boolean existePreOrden=false;
		for( int i=0; i<items.size(); i++ ){
			itemPointer ip=(itemPointer)items.get(i);
			int currPeso=ip.getTotalWidth();
			//System.out.println("ANCHO "+ ip.getTotalWidth());
			pesos.add( new Integer( currPeso ));
			if( ip.order>0 ){
				existePreOrden=true;
				//System.out.println("EXISTE PREORDEN");
			}
			//System.out.println("ARRO "+ip.label+","+ip.order);
			ordenes[i]=ip.order;
		}
		if( !existePreOrden ) ordenes=null;

		combina rep= new combina( 0, m_ancho, pesos,ordenes );
		if( rep.lista.size()==0 ){
			//System.out.println("COMBINA SIZE CERO");
			return null;
		}
		int best=rep.getCasoPesoMax();
		int[] prioridadesCasos=new int[rep.lista.size()];
		int combinaPrioritaria=-1, maxPrior=0;
		for( int caso=0; caso< rep.lista.size(); caso++){
			ArrayList comb= (ArrayList)rep.lista.get(caso);
			int prioComb=0;
			for( int i=0; i<comb.size(); i++ ){
				Integer index=(Integer)comb.get( i );
				prioComb+=((itemPointer)items.get( index.intValue() )).prioridad;
			}
			prioridadesCasos[caso]=prioComb;
			if( prioComb > maxPrior ){
				combinaPrioritaria=caso;
				maxPrior=prioComb;
			}
		}
		if( combinaPrioritaria >= 0 )
			best=combinaPrioritaria;

		//System.out.println("ITERA FLOW "+items.size()+","+m_ancho+","+best+","+rep.lista.size());
		columna row= buildFlow( items, (ArrayList)rep.lista.get(best),m_ancho );
		flow.add( row );
		ArrayList tmp= new ArrayList();
		tmp.add( row );
		return getRestoItems( tmp, items );
	}

	private columna buildFlow( ArrayList items, ArrayList indices, int ancho ){
		columna res=new columna( columna.HORIZ, ancho, m_cfg.Vgap, m_cfg.Hgap );
		for( int i=0; i< indices.size(); i++ ){
			Integer index= (Integer)indices.get( i );
			//System.out.println("añade A FLOW");
			res.add( (itemPointer)items.get( index.intValue() ));
		}
		return res;
	}

	private ArrayList procesaGrid( long minArea, int alto, int anchoVentana, ArrayList items, ArrayList flow, boolean soloFlow ){
		if( soloFlow ){
			flow.addAll( items );
			items.clear();
			return null;
		}

		panelFactory pf= new panelFactory( m_cfg, minArea, alto,anchoVentana, items );
		ArrayList pesos=pf.getPesos();
		if( pesos.size()==0 ){
			flow.addAll( items );
			items.clear();
			//System.out.println("PESOS 0");
			return null;
		}
		//System.out.println("COMBINANNNNNNNDO");
		combina rep= new combina( 0, m_ancho, pesos, null );
		//System.out.println("COMBINANNNNNNNDENDO");
		if( rep.lista.size()==0 ){
			flow.addAll( items );
			items.clear();
			return null;
		}
		int bestCombina=rep.getCasoPesoMax();
		ArrayList grid= pf.buildGrid( (ArrayList)rep.lista.get(bestCombina) );
		//System.out.println("COMBINA "+bestCombina+","+pesos.size()+","+pf.flow.size());
		flow.addAll( pf.flow );

		ArrayList tmpItems=(ArrayList)items.clone();
		items.clear();
		items.addAll( getRestoItems( grid, tmpItems ) );
		for( int f=0;f<pf.flow.size();f++){
			itemPointer ip=(itemPointer)pf.flow.get(f);
			items.remove( items.indexOf( ip ) );
		}
		//System.out.println("COMB END");
		return grid;
	}

	private ArrayList getRestoItems( ArrayList columnas, ArrayList items ){
		HashMap idsM= new HashMap();
		ArrayList resto= new ArrayList();
		if( columnas!=null ){
			for( int i=0; i<columnas.size();i++){
				ArrayList columna=(columna)columnas.get(i);
				for( int r=0;r<columna.size();r++){
					itemPointer ip=(itemPointer)columna.get(r);
					idsM.put( ip.id, ip );
					//System.out.println("OCUPADO "+ip.label);
				}
			}
		}
		for( int i=0; i<items.size();i++){
			itemPointer ip=(itemPointer)items.get(i);
			if( idsM.containsKey( ip.id ) ) continue;
			//System.out.println("RESTO "+ip.label);
			resto.add( ip );
		}
		return resto;
	}

	long hallaAreaLista(ArrayList items){
		long area= 0;
		for( int i=0; i<items.size(); i++){
			itemPointer ip= (itemPointer)items.get(i);
			int ancho= ip.getTotalWidth();
			int alto= ip.getTotalHeight();
			area+= ancho*alto;
		}
		return area;
	}

	long hallaAreaListaFlow(ArrayList items, int anchoVista , double incMejora){
		int altoM=0,longitud=0, altoMin=0;
		for( int i=0; i<items.size(); i++){
			itemPointer ip= (itemPointer)items.get(i);
			int ancho= ip.getTotalWidth();
			int alto= ip.getTotalHeight();
			altoM=Math.max(alto,altoM);
			if( altoMin==0 ) altoMin=alto;
			altoMin=Math.min(alto,altoMin);
			longitud+= ancho;
		}
		if( longitud==anchoVista ) return anchoVista*altoM;
		if( longitud>anchoVista )
			return (long)(anchoVista*altoM + ((int)(longitud/anchoVista))*anchoVista*altoMin*incMejora);
		return anchoVista*altoM;
	}

	public long hallaArea(ArrayList grid){
		long maxAlto=0;
		if( grid==null ) return 0;
		for( int c=0; c<grid.size();c++){
			columna col= (columna)grid.get(c);
			maxAlto= Math.max( maxAlto, col.getLength());
		}
		return maxAlto*m_ancho;
	}

	public long hallaAnchoMinimo(){
		return hallaAnchoMinimo( m_grids,m_flow );
	}

	public long hallaAnchoMinimo(ArrayList grids, ArrayList flow){
		long ancho=0;
		for( int g=0; g<grids.size();g++){
			ArrayList grid=(ArrayList)grids.get(g);
			long anchoGrid=0;
			for( int c=0; c<grid.size();c++){
				columna col= (columna)grid.get(c);
				anchoGrid+= col.gruesoMax;
			}
			ancho+= anchoGrid;
		}
		long maxAncho=0;
		for( int c=0; c<flow.size();c++){
			columna col=(columna)flow.get(c);
			maxAncho=Math.max(maxAncho, col.getLength());
		}
		ancho+=maxAncho;
		return ancho;
	}

	public long hallaArea(ArrayList grids, ArrayList flow){
		long area=0;
		for( int g=0; g<grids.size();g++){
			ArrayList grid=(ArrayList)grids.get(g);
			long maxAlto=0;
			for( int c=0; c<grid.size();c++){
				columna col= (columna)grid.get(c);
				maxAlto= Math.max( maxAlto, col.getLength());
			}
			area+= maxAlto*m_ancho;
		}
		for( int c=0; c<flow.size();c++){
			columna col=(columna)flow.get(c);
			//System.out.println("FLOW "+c+","+col.size()+","+col.getArea());
			area+=col.getArea();
		}
		return area;
	}

	public Rectangle hallaAreaTotal(){
		return new Rectangle((int)m_ancho, (int)hallaAltoTotal());
	}


	public long hallaAltoTotal(){
		return hallaAltoTotal(m_grids,m_flow);
	}

	public long hallaAltoTotal(ArrayList grids, ArrayList flow){
		long alto=0;
		for( int g=0; g<grids.size();g++){
			ArrayList grid=(ArrayList)grids.get(g);
			alto+=cfgView.Vgap;
			long maxAlto=0;
			for( int c=0; c<grid.size();c++){
				columna col= (columna)grid.get(c);
				maxAlto= Math.max( maxAlto, col.getLength());
			}
			alto+= maxAlto;
		}
		for( int c=0; c<flow.size();c++){
			columna col=(columna)flow.get(c);
			alto+=col.gruesoMax;
		}
		return alto;
	}

	public void build( Element root ){
		Element eFlow=null;
		for( int c=0; c<m_flow.size(); c++){
			if( c==0 ){
				eFlow= new Element("FLOW");
				eFlow.setAttribute("V_GAP",String.valueOf( cfgView.Vgap ));
				eFlow.setAttribute("H_GAP",String.valueOf( cfgView.Hgap ));
				root.addContent( eFlow );
			}
			columna col= (columna)m_flow.get(c);
			Element eRow= new Element("ROW");
			eRow.setAttribute("ID",String.valueOf(c));
			eRow.setAttribute("HEIGHT",String.valueOf(col.gruesoMax));
			eRow.setAttribute("WIDTH",String.valueOf(col.lonMax));
			eFlow.addContent( eRow );
			for( int r=0; r<col.size();r++){
				itemPointer ip= (itemPointer)col.get(r);
				eRow.addContent( ip.viewItem );
			}
		}
		for( int g=0; g<m_grids.size(); g++){
			ArrayList grid=(ArrayList)m_grids.get( g );
			Element eGrid= new Element("GRID");
			root.addContent( eGrid );
			boolean convertir=false;
			for( int c=0; c<grid.size();c++){
				columna col= (columna)grid.get(c);
				if( c==0 ){
					convertir= grid.size()==1 && col.size()==1;
					if( convertir ) eGrid.setName("FLOW");
				}
				Element eCol= new Element((convertir ? "ROW":"COL"));
				eCol.setAttribute("ID",String.valueOf(c));
				eGrid.addContent( eCol );
				for( int r=0; r<col.size();r++){
					itemPointer ip= (itemPointer)col.get(r);
					eCol.addContent( ip.viewItem );
				}
			}
		}
	}
}

class groupPanel extends Object implements priorizable, Comparable{
	panelBalancer pf;
	Integer id;
	String label;
	int tipo;
	int alto;
	int ancho;
	int prioridad=0;
	int orden=0;
	final static int CON_BORDE=0;
	final static int SIN_BORDE=1;
	groupPanel( metaData md, Integer idTO, Integer id, String label, panelBalancer pf ){
		this.id=id;
		this.label=label;
		this.pf=pf;

		boolean exito=false;
		if( idTO!=null && md.m_formOrderToPk.containsKey( idTO )){
			HashMap lista= (HashMap)md.m_formOrderToPk.get( idTO );
			if( lista.containsKey( id ) ){
				Integer val=(Integer)lista.get(id);
				orden= val.intValue()+1;
				exito=true;
			}
		}
		if( !exito ){
			if( id.intValue()==0 ) orden=1;
			else orden=2;
		}
	}

	public int getPrioridad(){
		return prioridad;
	}

	public int compareTo( Object obj ){
		if( !(obj instanceof groupPanel) ) throw new ClassCastException();
		groupPanel objB=(groupPanel)obj;
		if( objB.orden==orden ) return 0;
		if( orden< objB.orden ) return -1;
		return 1;
	}

	public void build( Element form){
		Element root= (label==null || id.intValue()==0) ? form:new Element("GROUP");
		if( root.getName().equals("GROUP") ){
			tipo=CON_BORDE;
			root.setAttribute("ID", id.toString() );
			root.setAttribute("LABEL", label );
			form.addContent( root );
		}else
			tipo=SIN_BORDE;
		pf.build( root );
	}
}

public class viewBalancer{
	HashMap grupos= new HashMap();
	int anchoVista=0, maxAnchoVista=0;
	ArrayList m_paneles;
	Element m_form;
	cfgView m_cfg= new cfgView();

	public final static int INPUT_FORM=0;
	public final static int OUTPUT_REPORT=1;

	public final static int TM_PANEL=777;
	int politica=INPUT_FORM;
	double altoRow=0,altoLinea;
	double wChar=0;
	Font fuente=null, fontBold=null;
	FontRenderContext fontRender;

	boolean m_modoFiltrado;
	metaData m_md;
	Dimension m_dim;
	Integer m_idTORoot;

	public void setDimension( Dimension dim ){
		this.anchoVista=(int)dim.getWidth();
                maxAnchoVista=anchoVista;
		m_dim=dim;
	}

	public boolean hasItems(){
		if( grupos.size()==0 ) return false;
		Iterator itr= grupos.keySet().iterator();
		while( itr.hasNext() ){
			Integer id=(Integer)itr.next();
			ArrayList grupo=(ArrayList)grupos.get( id );
			if( grupo==null ) continue;
			if( grupo.size()>0 ) return true;
		}
		return false;
	}


	public viewBalancer( 	metaData md,
				Dimension dim,
				int politica,
				FontRenderContext fontRender,
				Font fuente,
				Element root,
				Integer idTORoot,
				boolean modoFiltrado ){
		m_idTORoot=idTORoot;
		m_modoFiltrado=modoFiltrado;
		m_md=md;
		m_form=root;
		if( dim!=null ) setDimension( dim );
		this.politica= politica;
		this.fuente= fuente;
		fontBold= fuente.deriveFont(Font.BOLD);
		this.fontRender= fontRender;
		getRowHeight();
	}

	public double getRowHeight(){
		Dimension rect=getDimString("12345",false);
		altoLinea= rect.getHeight();
		wChar= rect.getWidth()/5.0;
		altoRow= altoLinea+m_cfg.V_InternalEditPadd*2;
		return altoRow;
	}

	public int getTableButtonSide(){
		return m_cfg.ladoBotonTabla;
	}

	public void addTable( 	Integer group, String id, int prioridad,
				boolean enable, String label, Element tabla, int order ){
		//System.out.println("NEWTABLE "+order);
		itemPointer ip= new itemPointer(false,
                                                tabla,
						group,
						helperConstant.TM_TABLA,
						id,
						0,
						prioridad,
						null,
						enable,
						true,
						label,
						-1,
						false,
						order,
						true);
		subAddItem( anchoVista, group, ip );
	}

	public void addItem( 	boolean multivalued,Integer group, int tm, String id, int tapos, int prioridad,
				String mask, boolean enable, boolean nullable, String label,int longitud,
				boolean comentado, int order, boolean visible ){
		//System.out.println("NEWITEM "+order);
		itemPointer ip=new itemPointer(multivalued,group,tm,id,tapos,prioridad,mask,enable,nullable,label,longitud,comentado,order,visible);
		subAddItem( anchoVista, group, ip );
	}

	public void addFixPanel( int anchoVista, int prioridad, Element item, int order ){
		itemPointer ip= new itemPointer( prioridad, item, order );
		subAddItem( anchoVista, new Integer(0), ip );
	}

	private void subAddItem( int anchoVista, Integer group , itemPointer ip ){
		ip.setDimension( getDimensiones(anchoVista,
                                                ip.tm,
                                                ip.viewItem,
                                                ip.longitud,
                                                ip.comentado,
                                                false,
                                                ip.multivalued ) );
		if( !grupos.containsKey( group ) )
			grupos.put( group, new ArrayList() );

		ArrayList lista=(ArrayList)grupos.get(group);
		lista.add( ip );
	}

	public int getAnchoInset( Integer idGroup, int anchoVista ){
		return anchoVista - (idGroup.intValue()==0 ? 0:2*cfgView.anchoBordeGrupo);
	}

	public int añadeInset( Integer idGroup, int anchoVista ){
		return anchoVista + (idGroup.intValue()==0 ? 0:2*cfgView.anchoBordeGrupo);
	}

	public Dimension process(boolean minimizarAltoFrenteMejoraRelAspecto){
		int oldAnchoVista= anchoVista, bestAncho=0;
		long oldArea= anchoVista*anchoVista,oldAlto=0,bestAlto=0;
		ArrayList bestForm= null;
		double bondadMax=0,lineaMejoraAspecto=1;
		for( int paso=1; paso<= m_cfg.pasosItera; paso++){
			ArrayList form= new ArrayList();
			//reset( anchoVista, grupos );
			//System.out.println("ANCHOVISTA "+anchoVista);
			long alto=0;
			double bondad=0;
			Iterator itr= grupos.keySet().iterator();
			long anchoMaxGroup=0;
			while( itr.hasNext() ){
				Integer idGroup=(Integer)itr.next();
				//System.out.println(">>>>>>>>>>>>>>>>>>>> PANELGROUP "+idGroup);
				ArrayList lista= (ArrayList)grupos.get( idGroup );
				lista= panelBalancer.ordenaListaPorAncho( lista );
				//System.out.println("PANEL LISTA "+lista.size());
				int anchoContent= getAnchoInset(idGroup, anchoVista);
				reset( anchoContent, lista );
				panelBalancer panel= new panelBalancer( anchoContent, m_cfg, lista );
				anchoMaxGroup= Math.max( anchoMaxGroup, (long)añadeInset(idGroup,(int)panel.hallaAnchoMinimo()) );
				bondad+=panel.m_bondad;

				groupPanel gr=new groupPanel( 	m_md,
								m_idTORoot,
								idGroup,
								m_md.getGroupLabel( idGroup ),
								panel );
				form.add( gr );
				long incAlto=panel.hallaAltoTotal();
				//System.out.println("PANEL grids,flow "+panel.m_grids.size()+","+panel.m_flow.size()+","+incAlto);

				if( gr.tipo==gr.CON_BORDE )
					incAlto+= m_cfg.altoEtiquetaGrupo;

				alto+=incAlto ;
			}

			anchoVista= Math.min( maxAnchoVista, (int)anchoMaxGroup );
			long area=anchoVista*alto;
			//if( area==0 ) continue;
			long incArea=area - oldArea;
			//System.out.println("ALTO,ancho:"+alto+","+anchoVista);
			boolean mejoraAspecto=mejoraRelacionAspecto( anchoVista,(int)alto,oldAnchoVista,(int)oldAlto);
			int mult=( area<cfgView.umbralArea ) ? 5:1;
			lineaMejoraAspecto+= mult*(m_cfg.incrementoPorcentualDeIteracion*( mejoraAspecto ?
								cfgView.aspectRelation_improvIncrement:-cfgView.aspectRelation_improvIncrement));
			double bondadGlobal= bondad+lineaMejoraAspecto;
			//System.out.println("ITERAMAIN:"+lineaMejoraAspecto+","+bondadGlobal+",ALTO,ancho:"+alto+","+anchoVista);
			if( bondadGlobal> bondadMax ){
				bondadMax= bondadGlobal;
				bestForm=form;
				bestAlto=alto;
				bestAncho=anchoVista;
			}else break;
			oldAnchoVista= anchoVista;
			anchoVista= anchoVista - (int)(anchoVista*m_cfg.incrementoPorcentualDeIteracion);
			oldArea= area;
			oldAlto=alto;
			if( minimizarAltoFrenteMejoraRelAspecto ) break;
			//if(true) break;
		}
		m_paneles= bestForm;
		ordenaPrioridades( m_paneles );
		anchoVista= bestAncho;
		reset( anchoVista, grupos );
		build();
		m_form.setAttribute("WIDTH", String.valueOf(bestAncho) );
		m_form.setAttribute("HEIGHT", String.valueOf(bestAlto) );
		m_form.setAttribute("V_GAP",String.valueOf( m_cfg.Vgap ));
		m_form.setAttribute("H_GAP",String.valueOf( m_cfg.Hgap ));
		return new Dimension( bestAncho, (int)bestAlto );
	}

	boolean mejoraRelacionAspecto( Rectangle areaA, Rectangle areaB ){
		return mejoraRelacionAspecto( (int)areaA.getWidth(),(int)areaA.getHeight(),(int)areaB.getWidth(),(int)areaB.getHeight());
	}
	boolean mejoraRelacionAspecto( int anchoA, int altoA, int anchoB, int altoB ){
		if( altoB==0 || anchoB==0 ) return true;
		return Math.abs(m_cfg.relAspecto - ((double)anchoA)/((double)altoA)) <
				Math.abs(m_cfg.relAspecto - ((double)anchoB)/((double)altoB));
	}
	void reset(int anchoVista, HashMap grupos){
		this.anchoVista= anchoVista;
		Iterator itr= grupos.keySet().iterator();
		while( itr.hasNext() ){
			Integer id=(Integer)itr.next();
			ArrayList grupo=(ArrayList)grupos.get(id);

		}
	}

	public void reset( int anchoVista, ArrayList grupo ){
		for( int i=0; i< grupo.size(); i++){
			itemPointer ip=(itemPointer)grupo.get( i );
			ip.setDimension( getDimensiones(anchoVista,
                                ip.tm,
                                ip.viewItem,
                                ip.longitud,
                                ip.comentado,
                                false,
                                ip.multivalued ) );
		}
	}

	public void ordenaPrioridades( ArrayList paneles ){
		for( int p=0; p< paneles.size(); p++ ){
			groupPanel gp= (groupPanel)paneles.get(p);
			gp.pf.ordenaPrioridades();
			
			for( int g=0;g<gp.pf.m_grids.size();g++){
				ArrayList grid=(ArrayList)gp.pf.m_grids.get(g);
				for( int c=0;c<grid.size();c++)
					gp.prioridad+= ((columna)grid.get(c)).prioridad;
			}
			for( int c=0;c<gp.pf.m_flow.size();c++)
				gp.prioridad+= ((columna)gp.pf.m_flow.get(c)).prioridad;
		}

		Object[] lista=paneles.toArray();
		Arrays.sort( lista );
		paneles.clear();
		for( int i=0;i<lista.length;i++)
			paneles.add( lista[i]);
	}

	public Dimension getTableDim( Element table, int anchoVista ){
		Dimension dimLabel= getDimString( table.getAttributeValue("LABEL"), true );
		Iterator iCol=table.getChildren("ITEM").iterator();
		int anchoTotal=0;//no incluye ancho etiqueta
		while( iCol.hasNext() ){
			Element col=(Element)iCol.next();
			int tm= Integer.parseInt(col.getAttributeValue("ID_TM"));
			int longitud= -1;//col.getAttributeValue("LABEL").length();
			if( col.getAttributeValue("LENGTH")!=null )
				longitud= Integer.parseInt( col.getAttributeValue("LENGTH") );
			Dimension dim=getDimensiones(anchoVista,tm,col,longitud,false,true,false);
			col.setAttribute("WIDTH", String.valueOf((int)dim.getWidth()));
			anchoTotal+=(int)dim.getWidth();
		}
		int rows=Integer.parseInt(table.getAttributeValue("ROWS"));
		if( rows>1 ) anchoTotal+=m_cfg.anchoScrollBar;
		int alto= (int)(altoRow*rows);
		int numBot=table.getAttributeValue("NUM_BOTONES")==null ? 0:
				Integer.parseInt(table.getAttributeValue("NUM_BOTONES"));
		int anchoBot=numBot*m_cfg.ladoBotonTabla;
		int anchoMax= (int)(anchoTotal + dimLabel.getWidth() + anchoBot);
		boolean topLabel=false;
		if( anchoMax > anchoVista || rows>1 || table.getAttributeValue("HIDE_HEADER")==null ){
			table.setAttribute("TOPLABEL","TRUE");
			table.setAttribute("HEIGHT_LABEL",String.valueOf((int)m_cfg.ladoBotonTabla));
			table.setAttribute("WIDTH_LABEL", String.valueOf((int)dimLabel.getWidth()));
			table.setAttribute("POS_BUTTON","TOP");
		}else{
			//anchoTotal=anchoMax;
			table.setAttribute("POS_BUTTON","RIGHT");
			table.setAttribute("WIDTH_LABEL", String.valueOf((int)dimLabel.getWidth()));
			table.setAttribute("WIDTH_BUTTON", String.valueOf(m_cfg.ladoBotonTabla));//solo tendra un boton
			if( table.getAttributeValue("TOPLABEL")!=null )
				table.removeAttribute("TOPLABEL");

		}

		if( anchoTotal>anchoVista )
			anchoTotal=anchoVista;
		table.setAttribute("WIDTH",String.valueOf(anchoTotal));

		if( table.getAttributeValue("HIDE_HEADER")==null ){
			int cab= table.getAttributeValue("HEADER_LINE")==null ?
					0:Integer.parseInt(table.getAttributeValue("HEADER_LINE"));
			alto+= (int)(cab*altoRow);
		}
		return new Dimension( anchoTotal,alto) ;
	}

	Dimension getDimensiones( int anchoVista,
                                  int tm,
                                  Element viewItem,
                                  int longitud,
                                  boolean comentado,
                                  boolean colTabla,
                                  boolean multivalued ){
		//longitud es la que opcionalmente redefinimos en el archivo meta
		viewItem.setAttribute("ROW_HEIGHT",String.valueOf((int)altoRow));
		String contenido= viewItem.getText();
		Dimension rect=null;
		switch(tm){
			case helperConstant.TM_TABLA:
				return getTableDim( viewItem, anchoVista );
			case helperConstant.TM_ENUMERADO:
				if( politica==INPUT_FORM )
					rect= new Dimension( (int)hallaMaxAnchoEnumerado( viewItem ), (int)altoRow);
				else{
					if(viewItem.getAttributeValue("DEFAULT")==null)
						rect= new Dimension( 	getAnchoMin(multivalued,tm,longitud,wChar,comentado),
									(int)altoRow);
					else
						rect= getDimString(hallaAnchoValorEnumerado(viewItem),true);
				}
				break;
			case 	helperConstant.TM_MEMO:
					if( politica==INPUT_FORM ){
						if(!(m_modoFiltrado||multivalued)){
							rect= new Dimension( anchoVista-m_cfg.grosorBordeForm, (int)((altoRow)*3) );
							viewItem.setAttribute("ROWS","3");
						}else
							rect= new Dimension(getAnchoMin(multivalued,
                                                                helperConstant.TM_TEXTO,
                                                                longitud,
                                                                wChar,
                                                                comentado,colTabla),
                                                                (int)altoRow);
					}else
						rect= getJustifyArea(contenido, anchoVista, (int)altoRow );
				break;
			case helperConstant.TM_IMAGEN:
				rect= new Dimension( getAnchoMin(multivalued,tm,longitud,wChar,comentado), m_cfg.ladoBotonTabla);
				break;
			case helperConstant.TM_ENTERO:
				if(viewItem.getAttributeValue("DEFAULT")==null)
					rect= new Dimension( getAnchoMin(multivalued,tm,longitud,wChar,comentado), (int)altoRow);
				else
					rect= getDimString(viewItem.getAttributeValue("DEFAULT"),false);
				break;
			case helperConstant.TM_BOOLEANO:
				rect= new Dimension( getAnchoMin(multivalued,tm,longitud,wChar,false), (int)altoRow);
				break;
			case helperConstant.TM_BOOLEANO_EXT:
				rect= new Dimension( getAnchoMin(multivalued,tm,longitud,wChar,comentado), (int)altoRow);
				if( comentado ){
					viewItem.setAttribute("COMMENT_WIDTH",
							String.valueOf( (int)(m_cfg.longMinimoExtensionCheck*wChar)));
					viewItem.setAttribute("COMMENT","TRUE");
				}

				break;
 			case helperConstant.TM_REAL:
				if(viewItem.getAttributeValue("DEFAULT")==null)
					rect= new Dimension( getAnchoMin(multivalued,tm,longitud,wChar,comentado), (int)altoRow);
				else
					rect= getDimString(viewItem.getAttributeValue("DEFAULT"),false);
				break;
 			case helperConstant.TM_FECHA:
				if(viewItem.getAttributeValue("DEFAULT")==null)
					rect= new Dimension( getAnchoMin(multivalued,tm,longitud,wChar,comentado), (int)altoRow);
				else
					rect= getDimString(viewItem.getAttributeValue("DEFAULT"),false);
				break;
                        case helperConstant.TM_FECHAHORA:
                            if(viewItem.getAttributeValue("DEFAULT")==null)
                                rect= new Dimension( getAnchoMin(multivalued,tm,longitud,wChar,comentado), (int)altoRow);
                            else
                                rect= getDimString(viewItem.getAttributeValue("DEFAULT"),false);
                            break;
			case helperConstant.TM_TEXTO:
				if( contenido==null || contenido.length()==0 ){
					if( politica==INPUT_FORM)
						rect= new Dimension(	getAnchoMin(multivalued,tm,longitud,wChar,comentado,colTabla),
									(int)altoRow);
					else 	rect= new Dimension(	getAnchoMin(multivalued,tm,longitud,wChar,comentado,colTabla),
									(int)altoRow);
					break;
				}else
					rect= getJustifyArea( contenido, anchoVista, altoRow );
				break;
		}
		Dimension dimLabel= getDimString( viewItem.getAttributeValue("LABEL"), true );
		double anchoTotal= rect.getWidth() + dimLabel.getWidth();
		double ancho= rect.getWidth();
		boolean topLabel=false;
		if( anchoTotal > anchoVista){
			ancho= anchoVista;
			viewItem.setAttribute("TOPLABEL","TRUE");
			viewItem.setAttribute("HEIGHT_LABEL",String.valueOf((int)dimLabel.getHeight()));
			topLabel=true;
		}else{
			if( viewItem.getAttributeValue("TOPLABEL")!=null )
				viewItem.removeAttribute("TOPLABEL");

		}
		viewItem.setAttribute("WIDTH_LABEL", String.valueOf((int)dimLabel.getWidth()));
		viewItem.setAttribute("VERT_CELL_PADD", String.valueOf(m_cfg.CellPadd));
		viewItem.setAttribute("HORIZ_CELL_PADD", String.valueOf(m_cfg.CellPadd));
		viewItem.setAttribute("V_EDIT_PADD", String.valueOf(m_cfg.V_InternalEditPadd));
		viewItem.setAttribute("H_EDIT_PADD", String.valueOf(m_cfg.H_InternalEditPadd));
		rect.setSize( ancho, rect.getHeight() );
		return rect;
	}


	int getAnchoMin( boolean multivalued,int tm, int longitud, double anchoChar, boolean comentado ){
		return getAnchoMin(multivalued,tm,longitud,anchoChar,comentado,false);
	}

	int getAnchoMin( boolean multivalued,int tm, int longitud, double anchoChar, boolean comentado, boolean columnaDeTabla ){
		int mult= (m_modoFiltrado||multivalued) ? 2:1;
		int suma= (m_modoFiltrado||multivalued) ? 1:0;
		switch(tm){
			case helperConstant.TM_ENUMERADO:
				return anchoEdit(anchoChar, (longitud==-1 ? 	m_cfg.longMinimoCampoTexto:
										longitud));
						//Math.max(m_cfg.longMinimoCampoTexto,longitud))*anchoChar);
			case helperConstant.TM_ENTERO:
				return anchoEdit(anchoChar,suma+mult*(longitud==-1 ? 	m_cfg.longMinimoCampoNumerico:
											longitud));
						//Math.max(m_cfg.longMinimoCampoNumerico,longitud))*anchoChar);
			case helperConstant.TM_IMAGEN:
				return m_cfg.ladoBotonTabla*3;//ver,borrar,asignar

			case helperConstant.TM_BOOLEANO:
				return m_cfg.anchoMinimoCampoBool;

			case helperConstant.TM_BOOLEANO_EXT:
				if( !comentado )
					return m_cfg.anchoMinimoCampoBool;
				else
					return m_cfg.anchoMinimoCampoBool + (int)(m_cfg.longMinimoExtensionCheck*anchoChar);
 			case helperConstant.TM_REAL:
				return anchoEdit(anchoChar,suma+mult*(longitud==-1 ?	m_cfg.longMinimoCampoNumerico:
											longitud));
						//Math.max(m_cfg.longMinimoCampoNumerico,longitud))*anchoChar);
 			case helperConstant.TM_FECHA:
				return anchoEdit(anchoChar,suma+mult*(longitud==-1 ? 	m_cfg.longMinimoCampoFecha:
											longitud));
							//Math.max(longitud,m_cfg.longMinimoCampoFecha))*anchoChar);
                        case helperConstant.TM_FECHAHORA:
                            return anchoEdit(anchoChar,suma+mult*(longitud==-1 ? m_cfg.longMinimoCampoFechaHora:
                                    longitud));
			case helperConstant.TM_TEXTO:
				int longc=columnaDeTabla ? m_cfg.longMinimoCampoTextoTabla:m_cfg.longMinimoCampoTexto;
				if( longitud==-1 ){
					return (int)(longc*anchoChar);
				}
				longc= Math.min( longc, longitud );
				return anchoEdit(anchoChar,longc);
		}
		return 0;
	}

	int anchoEdit( double anchoChar, int lon ){
		return (int)(anchoChar*lon + 2*cfgView.H_InternalEditPadd);//el 3 es el ancho de los bordes
	}

	Dimension getDimString( String value, boolean bold ){
		if( value==null){
			return new Dimension( 0, 0 );
		}
		if( bold ){
			Rectangle2D rect=fontBold.getStringBounds( value, fontRender );
			return rect.getBounds().getSize();
		}else {
			Rectangle2D rect=fuente.getStringBounds( value, fontRender );
			return rect.getBounds().getSize();
		}
	}

	Dimension getJustifyArea( String texto, double anchoMax, double altoLinea ){
		double altoMemo= altoRow;
		Dimension r= getDimString( texto, false );
		if( texto!=null && texto.length() > anchoVista ){
			if( r.getHeight() > altoLinea + 5 )
				return r;
			altoMemo= (r.getWidth()/anchoVista)*altoRow;
			return new Dimension( (int)anchoMax, (int)altoMemo );
		}
		return r;
	}

	double hallaMaxAnchoEnumerado( Element item ){
		double max= 0;
		if( politica== INPUT_FORM){
		//esta funcion solo es valida para la polita input form que espera que bajo el elemento data
		//se definan todos los valores de enumerado
			Integer tapos= new Integer( item.getAttributeValue("TA_POS") );

			Iterator iValues= m_md.getEnumSet( tapos );
			while(iValues.hasNext()){
				Integer idenum= (Integer)iValues.next();
				String name= m_md.getEnumLabel( tapos, idenum );
				Dimension rect= getDimString( name, true );
				max= Math.max(max,rect.getWidth()+25);
				//20 es el ancho del selector
			}
		}
		return max;
	}

	String hallaAnchoValorEnumerado( Element item ){
		double max= 0;
		Integer tapos= new Integer( item.getAttributeValue("TA_POS") );

		Iterator iValues= m_md.getEnumSet( tapos );
		while(iValues.hasNext()){
			Integer idenum= (Integer)iValues.next();
			String name= m_md.getEnumLabel( tapos, idenum );
			if( idenum.toString().equals( item.getAttributeValue("VALUE")))
				return name;
		}
		return null;
	}

	private ArrayList hallaGridColumns( Object[] lista, int ini, int end ){
		int col1=0, col2=0;
		for( int i=ini; i<=end; i++ ){
			itemPointer ip= (itemPointer)lista[i];
			int anchoLabel= ip.getTotalWidth() - ip.getWidth();
			col1= Math.max( col1, anchoLabel );
			col2= Math.max( col2, ip.getWidth() );
		}
		ArrayList res=new ArrayList();
		res.add( new Integer(col1) );
		res.add( new Integer(col2) );
		return res;
	}

	void build(){
		for( int p=0; p< m_paneles.size(); p++ ){
			groupPanel gp= (groupPanel)m_paneles.get(p);
			gp.build( m_form );
		}
	}
}


*/