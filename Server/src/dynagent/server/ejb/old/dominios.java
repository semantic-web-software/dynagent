package dynagent.ejb.old;
/*package dynagent.ejb;

import org.jdom.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import java.text.DecimalFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;

import dynagent.knowledge.*;
import dynagent.application.*;
import dynagent.knowledge.instance.*;

public class dominios implements fieldControl{
	//esta clase deberaa ser listener de los dataModel en cuanto a object deleted para eliminar basura
	private HashMap m_iniciadorEvento= new HashMap();
	Element m_dom=null;
	private calculo m_calc= null;
	private domDataModel m_domSourceModel, m_domDocModel;
	HashMap m_conditionFunctionValues= new HashMap();
	HashMap m_condicionantes_List= new HashMap();
	ArrayList m_scriptControls= new ArrayList();
	boolean m_inicializacion=true;
	metaData m_md;
	boolean m_serverSide;
	HashMap m_nullables= new HashMap();
	boolean m_recalcularFunciones=true;
	public dominios( boolean serverSide, metaData md, domDataModel domModelOrigen, domDataModel docModel, Element dominio )
		throws IllegalAccessException,InstantiationException{
		m_md=md;
		m_serverSide=serverSide;
		m_dom= dominio;
		m_domSourceModel= domModelOrigen;
		m_domDocModel= docModel;
		if( md!=null ){
			m_calc=  md.getFunciones( null,null,null);//los valores deben hallarse para cada object
			System.out.println("DOMINIOS CALC:"+m_calc);
		}else
			System.out.println("DOMINIOS CALC NULO");
		BuildPropertiesMap();
		BuildRestrictionMap();
	}

	void addScriptControl( scriptControl sc){
		m_scriptControls.add( sc );
	}

	public void setRecalcularFunciones( boolean r ){
		m_recalcularFunciones=r;
	}

	public void endInizialization(){
		m_inicializacion=false;
	}
	boolean isNullable( String idCampo ){
		Boolean nullb= (Boolean)m_nullables.get( idCampo );
		if( nullb==null ) return true;
		return nullb.booleanValue();
	}

	boolean functionValuesContainsKey( int object, Element nodo ){
		HashMap values= (HashMap)m_conditionFunctionValues.get( new Integer(object) );
		if( values==null ) return false;

		return values.containsKey( nodo );
	}

	void removeFunctionValue( int object, Element nodo ){
		HashMap values= (HashMap)m_conditionFunctionValues.get( new Integer(object) );
		if( values==null ) return;

		values.remove( nodo );
	}

	void putFunctionValue( int object, Element nodo ){
		HashMap values= (HashMap)m_conditionFunctionValues.get( new Integer(object) );
		if( values==null ){
			values= new HashMap();
			m_conditionFunctionValues.put( new Integer( object ), values );
		}
		values.put( nodo, null );
	}


	void BuildPropertiesMap(){
		if( m_dom==null || m_dom.getChild("PROPERTY_LIST")==null ) return;
		Iterator itr= m_dom.getChild("PROPERTY_LIST").getChildren( "ATRIBUTO" ).iterator();
		while( itr.hasNext() ){
			Element at=(Element)itr.next();
			boolean nullable= 	!(at.getAttributeValue("NULLABLE")!=null &&
						at.getAttributeValue("NULLABLE").equals("FALSE"));
			String key= at.getAttributeValue("ID");
			if( key==null )
				key="0@"+at.getAttributeValue("TA_POS");
			m_nullables.put( key, new Boolean(nullable) );
		}
	}

	void BuildRestrictionMap(){
		try{
		if( m_dom==null || m_dom.getChild("POLIMORFISMO")==null ) return;
		Iterator iRest= m_dom.getChild("POLIMORFISMO").getChildren("REST").iterator();
		while(iRest.hasNext()){
			org.jdom.Element eRest= (org.jdom.Element)iRest.next();
			String atId= eRest.getAttributeValue("ID");
			Iterator iRule= eRest.getChildren("RULE").iterator();
			while(iRule.hasNext()){
				org.jdom.Element eRule = (org.jdom.Element) iRule.next();
				Iterator iCond= eRule.getChildren("COND").iterator();
				//Ahora compruebo si hay atributos condicionales
			condiciones:
				while(iCond.hasNext()){
					org.jdom.Element eCond=(org.jdom.Element)iCond.next();
					Iterator iRangeCond=eCond.getChildren("RANGE").iterator();
					while(iRangeCond.hasNext()){
						org.jdom.Element rangeCond= (org.jdom.Element)iRangeCond.next();
						if( rangeCond.getAttributeValue("TA_POS")==null )
							continue condiciones;
						//este atributo no ha sido mapeado, no pertenece a este TO
						String key= rangeCond.getAttributeValue("ID");
						if( key==null )
							key = "0@"+rangeCond.getAttributeValue("TA_POS");
						setCondicionante( key, eCond );
					}
				}
				org.jdom.Element dom= eRule.getChild("DOMINIO");
				if( 	dom.getText()!=null &&
					dom.getChild("ITEM")==null ){//es un dominio funcional
					String[] items= dom.getText().split("\\{");
					for( int i=0; i< items.length; i++){
						if( items[i].length()==0 ||
						    items[i].indexOf("@")>=0 ) continue;
						int end= items[i].indexOf("}");
						if( end==-1) continue;
						String key = items[i].substring(0,end);
						if( key.indexOf("@")==-1 )
							key="0@"+key;
						setCondicionante( key, dom );
					}
				}
			}
		}
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
						"ERROR EN BUILD MAP"+ e.getClass()+":"+e.getMessage());}
	}

	void setCondicionante(String idAt, org.jdom.Element condicionado){
		ArrayList condiciones=null;
		if(m_condicionantes_List.containsKey(idAt))
			condiciones= (ArrayList)m_condicionantes_List.get(idAt);
		else{
			condiciones= new ArrayList();
			m_condicionantes_List.put(idAt , condiciones);
		}
		if(!condiciones.contains(condicionado)){
			//System.out.println("ASIGNANDO CONDICIONADO:"+idAt);
			condiciones.add(condicionado);
		}
	}

      public boolean estateInicialization(){
            return m_inicializacion;
      }

   public void clearRegIniEventos(){
	m_iniciadorEvento.clear();
   }

   public void eventDataChanged(session ses,int to, int object, String atCond)
	throws NumberFormatException, SystemException,ApplicationException{
	System.out.println("IN EV 1 "+object+","+atCond+" contains key:"+m_iniciadorEvento.containsKey(atCond));
	try{
	subEventDataChanged(ses,to,object,atCond);
	clearRegIniEventos();
	System.out.println("END EV "+object+","+atCond+" contains key "+m_iniciadorEvento.containsKey(atCond));
	}catch(SystemException e){
		e.printStackTrace();
		throw e;
	}catch(NumberFormatException e){
		e.printStackTrace();
		throw e;
	}catch(ApplicationException e){
		e.printStackTrace();
		throw e;
	}catch(Exception e){
		e.printStackTrace();
	}
   }

   private void subEventDataChanged(session ses,int to, int object, String atCond)
	throws NumberFormatException, SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException{
	if( m_iniciadorEvento.containsKey(atCond) ) return;
	m_iniciadorEvento.put( atCond, null );

	System.out.println("DENTRO DATA CHANGED,OBJ:"+object+",AT:"+atCond);
	try{
		System.out.println("DOM:"+jdomParser.returnXML(m_dom));
	}catch(Exception e){
		System.out.println("EVENT DATA CHANGED ERROR, DOM nulo");
	}
	boolean exito= false;
	if( !m_domSourceModel.fieldSupported( atCond ) ){
		System.out.println("FIELD NOT SUPPORTED:");
		m_iniciadorEvento.remove( atCond );
		return;
	}
	System.out.println("DOM2:");
	if(m_domSourceModel.containsObject(object)){
		System.out.println("DOM3:");
		try{
                int[] to_ido= new int[]{to, object};
		m_domDocModel.setData( ses, to_ido, atCond, getSourceData( ses, to,object, atCond ) );
		}catch(Exception e){
			e.printStackTrace();
		}
	}else{
		System.out.println("NOT CONTAINS OBJ:");
		m_iniciadorEvento.remove( atCond );
		return;
	}
	//System.out.println("DOM4:");
	if(!m_condicionantes_List.containsKey(atCond)){
		//System.out.println("NO CONDICIONANTES:");
		*//***** INI CODIGO TEMPORAL HASTA INCLUIR RDNSCRIPTS EN DOMINIOS ****//*
			recalculaFuncion( ses,to,object, null);
		*//***** END CODIGO TEMPORAL ****//*
		m_iniciadorEvento.remove( atCond );
		return;
	}
	HashMap atsRestrinjidos= new HashMap();
	ArrayList domActivosFuncionales= new ArrayList();
	ArrayList condicionantes= (ArrayList)m_condicionantes_List.get(atCond);
	Iterator iCond= condicionantes.iterator();
	//compruebo si alguno de los condicionantes donde aparece se activa
	//parto de la base que un mismo atributo es restrinjido en un unico
	// nodo del doc XML
iteraMain:
	while(iCond.hasNext()){
		org.jdom.Element eCond= (org.jdom.Element)iCond.next();
		try{
			System.out.println("COND:"+jdomParser.returnXML(eCond));
		}catch(Exception e){;}
		if( eCond.getName().equals("DOMINIO") ){
			Integer ref= new Integer( eCond.getAttributeValue("REF") );
			org.jdom.Element rule= eCond.getParent();
			org.jdom.Element atRest= rule.getParent();

			String idCampo= atRest.getAttributeValue("ID");
			if( idCampo==null ) idCampo="0@"+atRest.getAttributeValue("TA_POS");
			if( m_iniciadorEvento.containsKey(idCampo) ) continue;

			if( rule.getChild("COND")==null ){
				domActivosFuncionales.add( ref );
				continue;
			}
			Iterator it= rule.getChildren("COND").iterator();
			while( it.hasNext() ){
				org.jdom.Element subECond=(org.jdom.Element)it.next();
				if(subCheckActiveCond( ses,to,object, atCond, subECond, domActivosFuncionales,atsRestrinjidos ))
					continue iteraMain;
			}
			continue;
		}
		subCheckActiveCond( ses,to,object, atCond, eCond, domActivosFuncionales,atsRestrinjidos );
	}
	recalculaFuncion( ses,to,object, domActivosFuncionales);
	m_iniciadorEvento.remove( atCond );

   }

	private boolean subCheckActiveCond(session ses,
                                           int to,
                                           int object,
                                           String atCond,
                                           org.jdom.Element eCond,
                                           ArrayList domActivosFuncionales,
                                           HashMap atsRestrinjidos)
                throws SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException{

		boolean indirecta= 	eCond.getAttributeValue("DEPENDENCIA")!=null &&
					eCond.getAttributeValue("DEPENDENCIA").equals("INDIRECTA") ;


		String sat=eCond.getParent().getParent().getAttributeValue("TA_POS");
		int at=Integer.parseInt(sat);
		String idCampo= eCond.getParent().getParent().getAttributeValue("ID");

		boolean active= checkMultiDominio(ses,
                                                  to,
                                                  object,
                                                  eCond.getChildren("RANGE").iterator(),
                                                  indirecta,
                                                  atCond );
		boolean containKey= functionValuesContainsKey(object,eCond);
		if( idCampo==null ) idCampo="0@"+sat;

		if( !atsRestrinjidos.containsKey( idCampo ))
			atsRestrinjidos.put( idCampo, new Boolean(false));
		Boolean afected= (Boolean)atsRestrinjidos.get( idCampo );
		if( afected.booleanValue() ) return true;

    		if(active){
			// si almacena esta key es que la última vez fue true
			// y por tanto no ha cambiado la restricción
			org.jdom.Element subdom= eCond.getParent().getChild("DOMINIO");
			boolean esDomFuncional= subdom.getText()!=null &&
						subdom.getChild("ITEM")==null;
			atsRestrinjidos.put( idCampo, new Boolean(true));

			if(!esDomFuncional){
				if(containKey) return true;
				// he pasado luego la última vez fue false y hemos cambiado.
				// Hago un put porque active
				m_conditionFunctionValues.put(eCond,null);
				putFunctionValue( object, eCond );
				m_domSourceModel.setNewRestriction(ses,
                                                                   new int[]{to,object},
                                                                   idCampo,
                                                                   eCond.getParent().getChild("DOMINIO"));
			}else{
				Integer refSubDom= new Integer( subdom.getAttributeValue("REF") );
				domActivosFuncionales.add( refSubDom );
			}
			return true;
		}else{
                	if(containKey){
				refreshRestriction(ses,to,object, atCond, idCampo);
				removeFunctionValue( object, eCond);
           		}
			return false;
		}
	}

	public static Integer getTAPOS( String campo){
		int ini= campo.indexOf("@");
		Integer tapos= new Integer( campo.substring(ini+1) );
		return tapos;
	}

    public static Element buildAva( metaData md, String at, Object valor ){
	Element ava=new Element("ITEM");
	ava.setAttribute("TA_POS",getTAPOS(at).toString() );
        if( valor instanceof Element ){
            ava.addContent((Element)valor);
        }else{
            int tm = md.getID_TM(getTAPOS(at));
            if (tm == helperConstant.TM_TEXTO ||
                tm == helperConstant.TM_MEMO) {
                if (valor != null && ((String) valor).length() > 0)
                    ava.setText(helperConstant.valueToString(tm, valor));
            } else
            if (tm == helperConstant.TM_ENUMERADO) {
                ava.setAttribute("VALUE",
                                 helperConstant.valueToString(tm, valor));
            } else {
                System.out.println("___TM_VAL " + at + "," + tm + "," +
                                   helperConstant.valueToString(tm, valor));
                if (valor == null)
                    ava.setAttribute("VALUE", "#NULLVALUE#");
                else
                    ava.setAttribute("VALUE",
                                     helperConstant.valueToString(tm, valor));
            }
        }
	return ava;
    }



  	public Element aplicaDominio( session ses, int to, int object, boolean online )
                  throws SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException{
		HashMap asignacion= getAsignacionFijaDeAtributos(ses,to,object, online);
		if( !online ){
			Element cambios=new Element("CAMBIOS");
			Iterator iA= asignacion.keySet().iterator();
			while( iA.hasNext()){
				//System.out.println("DENTRO ASIGNACION");
				String at=(String)iA.next();
				Object valor= asignacion.get( at );
				cambios.addContent( buildAva( m_md, at, valor ));
				//m_domSourceModel.setData( object, at, valor );
			}
			return cambios;
		}else
			return null;

	}


    private void printFilterMap( HashMap fm, HashMap values ){
	if( fm==null ){
		System.out.println("ERROR FILTER MAP ES NULO");
		return;
	}
	System.out.println("DIRECT VALUES SIZE:"+values.size());
	Iterator itr= values.keySet().iterator();
	while( itr.hasNext() ){
		String at= (String)itr.next();
		System.out.println("AT,val:"+at+","+values.get(at));

	}
	System.out.println("FILTER MAP SIZE:"+fm.size());
	itr= fm.keySet().iterator();
	while( itr.hasNext() ){
		Integer idf=(Integer)itr.next();
		System.out.println("FILTER:"+idf);
		ArrayList obs= (ArrayList)fm.get( idf );
		for( int ob=0; ob<obs.size();ob++){
			System.out.println("OBJECT:"+ob);
			HashMap ats=(HashMap)obs.get(ob);
			Iterator iA= ats.keySet().iterator();
			while( iA.hasNext()){
				Object key= iA.next();
				if( key instanceof Integer ){
					Integer val= (Integer)ats.get( key );
					System.out.println("		TIPO,val:"+(Integer)key+","+val);
				}
				else{
					String at=(String)key;
					Object val=ats.get( key );
					if( val instanceof Double ){
						Double aval= (Double)val;
						System.out.println("		AT,val:"+at+","+aval);
					}else
						if( val instanceof Integer){
							Integer aval= (Integer)val;
							System.out.println("		AT,val:"+at+","+aval);
						}else
							System.out.println("		AT,val:"+at+","+val+" NOMATACH");
				}
			}
		}
	}
    }
  public void recalculaFuncion(session ses,int to,int object, ArrayList domFuncActives)
          throws SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException {
	//showMsg("IN TX DATA CHANGED");
	if( !m_recalcularFunciones ) return;
	System.out.println("IN RECALC FUNC:"+object+",dom:"+m_dom.getAttributeValue("ID"));
	if( domFuncActives==null || domFuncActives.size()==0 ) return;
	System.out.println("DOM NOT NULL:"+domFuncActives.size());

	//todavia no soportamos calcular funciones de un subelemento relacionado. Por tanto si object!=-1
	// filterMap quedara a nulo

	System.out.println("CALC:"+m_calc);
	System.out.println("DOC:"+m_domDocModel);
	m_calc.setRelationalData( m_domDocModel.refreshFilterMap(ses,object) );
	m_calc.setDirectValues( m_domDocModel.getNumericValues(ses,to,object) );
	m_calc.setFilterMap( m_domDocModel.getFiltrosDeRelacion(ses,object) );

	printFilterMap( m_calc.getFilterMap(), m_calc.getDirectValues() );
	//System.out.println("FILTER MAP SIZE:"+filterMap.size());
	//if( filterMap.size()==0 ) return;


	int refDom= Integer.parseInt( m_dom.getAttributeValue("ID") );

iterarPorDom:
	for( int i=0; i< domFuncActives.size(); i++){
		Integer refSubDom= (Integer)domFuncActives.get(i);
		Element subdom= jdomParser.findElementByAt( m_dom,
							    "DOMINIO",
							    "REF",
							    refSubDom.toString(),
							    true);
		if( subdom==null ){
			JOptionPane.showMessageDialog(null,"ERROR SUBDOM NULO:" + refSubDom );
			return;
		}
		Element atRest= subdom.getParent().getParent();
		Integer taposRest= new Integer( atRest.getAttributeValue("TA_POS") );
		String idCampo= atRest.getAttributeValue("ID");
		if( idCampo==null ) idCampo="0@"+taposRest.toString();
		if( m_iniciadorEvento.containsKey(idCampo ) ) continue;
		//System.out.println("DATA CHANGED6:"+idCampo);
		//if( !m_domSourceModel.fieldSupported( idCampo ) ) continue;

		Object value= getSourceData(ses,to,object,idCampo);
		System.out.println("DATA CHANGED7:"+idCampo+","+value+","+ refDom+","+taposRest+","+refSubDom);

		Object newVal=null;
		if( subdom.getAttributeValue("INLINE")!=null )
			newVal= scriptlet.calculaDirectExpresion( m_calc.getDirectValues(), subdom.getText() );
		else
			newVal= m_calc.calcular( refDom, idCampo, refSubDom.intValue());

		System.out.println("DATA CHANGED8:"+value+","+newVal+","+ refDom+","+taposRest+","+refSubDom);
		if( newVal==null ) continue;
		if( value==null && newVal==null ) continue;
		if( value!=null && newVal!=null && value.equals(newVal) ) continue;
		//System.out.println("DATA CHANGED 11");
		int tm= m_md.getID_TM( taposRest );

		switch( tm ){
			case helperConstant.TM_ENTERO:
				Integer ii= new Integer( ((Double)newVal).intValue() );
				m_domSourceModel.setData( ses,new int[]{to,object},idCampo, ii );
				m_domDocModel.setData( ses,new int[]{to,object}, idCampo,ii );
				subEventDataChanged(ses,to,object, idCampo);
				m_calc.setDirectValues( m_domDocModel.getNumericValues(ses,to,object) );

				break;
			case helperConstant.TM_REAL:

				DecimalFormatSymbols dfs=new DecimalFormatSymbols();
				dfs.setDecimalSeparator('.');

				DecimalFormat df= new DecimalFormat( "0.00", dfs );
				try{
				Double dd= new Double( df.format( (Double)newVal ) );

				m_domSourceModel.setData( ses,new int[]{to,object},idCampo, dd );
				m_domDocModel.setData( ses,new int[]{to,object}, idCampo,dd );
				subEventDataChanged(ses,to,object, idCampo);
				m_calc.setDirectValues( m_domDocModel.getNumericValues(ses,to,object) );

				}catch(Exception e){
					JOptionPane.showMessageDialog(null, "hubo error:"+object+","+idCampo+","+newVal+","+e.getMessage());
				}
				break;
			//default:
				//Actualmente no hay funcioens de texto compiladas..
				//m_form.setAttributeValue( m_formFtry.getPosFromTA(taposRest), newVal );
		}
	}
    }


	public HashMap getAsignacionFijaDeAtributos(session ses,int to,int object, boolean online)
                throws SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException{

		HashMap asignacion= new HashMap();
		//System.out.println("IN ASIGNACION:dom "+m_dom.getAttributeValue("ID"));
		if( m_calc==null || m_domDocModel==null || m_domSourceModel==null ) return asignacion;
		//System.out.println("ASIGNACION2");
		m_calc.setRelationalData( m_domDocModel.refreshFilterMap(ses,object) );
		m_calc.setDirectValues( m_domDocModel.getNumericValues(ses,to,object) );
		m_calc.setFilterMap( m_domDocModel.getFiltrosDeRelacion(ses,object) );
		printFilterMap( m_calc.getFilterMap(), m_calc.getDirectValues() );


		ArrayList doms= getActivesDoms(ses,to,object, false);
		//System.out.println("DOMS ACTIVES SIZE:"+doms.size());

		for( int i=0; i< doms.size(); i++){
			Integer ref=(Integer)doms.get(i);
			org.jdom.Element dom= jdomParser.findElementByAt(m_dom.getChild("POLIMORFISMO"),
								"DOMINIO",
								"REF",
								ref.toString(),
								true );

			String at= dom.getParent().getParent().getAttributeValue("ID");
			Integer tapos=new Integer(dom.getParent().getParent().getAttributeValue("TA_POS"));
			if( at==null )
				at="0@"+tapos;
			//if( !m_domSourceModel.fieldSupported( at ) ) continue;
			int tm= m_md.getID_TM(tapos);
			try{
			System.out.println("AT REST:"+m_serverSide+","+tapos+",dom:"+jdomParser.returnXML( dom ));
			}catch(Exception e){;}
			if(!m_serverSide &&
                           m_dom.getAttributeValue("ID")!=null ){
                            Integer idRootDom= new Integer( m_dom.getAttributeValue("ID") );
                            if(m_md.m_indices.containsKey( idRootDom ) ){
                                atReference atr = (atReference) m_md.m_indices.get(idRootDom);
                                //System.out.println("SI CONTIENE:"+atr.idSubdomRef);
                                if (atr.idSubdomRef == ref.intValue())continue;
                           }
			}

			if( 	dom.getText()!=null &&
				dom.getText().length() >0 &&
				dom.getChild("ITEM")==null ){
				//System.out.println("CALC1:"+dom.getText());
				if(!dom.getText().matches(".*[;\\.{}()@].*")){//no matcher implica que un valor fijo
					//System.out.println("CALC2");
					try{
					Object valor= helperConstant.parseValue( tm, dom.getText());
					if( valor!=null ){
						if( !online ) asignacion.put( at, valor );
						else{
							m_domSourceModel.setData( ses,new int[]{to,object}, at, valor );
							m_domDocModel.setData( ses,new int[]{to,object}, at,valor );
							subEventDataChanged(ses,to,object, at);
							m_calc.setDirectValues( m_domDocModel.getNumericValues(ses,to,object) );
						}
					}
					}catch(ParseException pe){
						System.out.println("ERROR GETASIGN:FIJA en dominios."+pe.getMessage());
						pe.printStackTrace();
					}
				}else{
					//System.out.println("CALC3");
					if( m_calc!=null ){
						//System.out.println("CALC4");
						Element atAfectado= dom.getParent().getParent();
						Integer idat= new Integer( atAfectado.getAttributeValue("TA_POS") );
						int idDom= Integer.parseInt( m_dom.getAttributeValue("ID") );
						String key= atAfectado.getAttributeValue("ID") ;
						if( key==null )
							key="0@"+ idat ;

						HashMap map=m_calc.getDirectValues();
						//if( !m_domSourceModel.fieldSupported( key ) )continue;
						int refSubDom= Integer.parseInt( dom.getAttributeValue("REF") );
						//System.out.println("idDom,KEY,sub:"+idDom+","+key+","+refSubDom+","+m_calc);
						Object newVal= m_calc.calcular( idDom, key, refSubDom);
						if( newVal==null )
							System.out.println("NEWVAL NULO");
						else
							System.out.println("NEWVAL CLASS "+newVal.getClass() + ","+newVal);

						//System.out.println(getSourceData( object, key ).getClass());
						//System.out.println(getSourceData( object, key ));
						Object oldVal= getSourceData( ses,to,object, key );
						String strOld= oldVal==null ? null:oldVal.toString();
						//System.out.println("OLDVAL->NEW"+oldVal+","+newVal);
						try{
						if( 	newVal!=null &&
							!helperConstant.equals( m_md.getID_TM( idat ),
										newVal.toString(),
										strOld )){
							Object finalVal=newVal;
							if( m_md.getID_TM( idat )==helperConstant.TM_ENTERO ){
								finalVal= new Integer(((Double)newVal).intValue()) ;
							}

							//System.out.println("CALC5:"+finalVal+","+object+","+at);
							if( !online ) asignacion.put( at, finalVal );
							else{
								m_domSourceModel.setData( ses,new int[]{to,object}, at, finalVal );
								m_domDocModel.setData( ses,new int[]{to,object}, at,finalVal );
								subEventDataChanged(ses,to,object, at);
								m_calc.setDirectValues( m_domDocModel.getNumericValues(ses,to,object) );
							}
						}
						}catch(ParseException pe){
							System.out.println("ERROR GETASIGN:FIJA en dominios.EQUALS"+pe.getMessage());
							pe.printStackTrace();
						}
					}
				}
			}

			if( 	dom.getChild("ITEM")!=null ){
				try{
				Element nodo=null;
				boolean isDefault=false, imponer=false;
				if(	dom.getChildren("ITEM").size()==1 &&
					dom.getChild("ITEM").getAttributeValue("VAL_MIN").equals(
					dom.getChild("ITEM").getAttributeValue("VAL_MAX")) &&
					dom.getChild("ITEM").getAttributeValue("DEFAULT")==null ){
					nodo= dom.getChild("ITEM");
					imponer=true;
				}

				if( nodo==null ){
					nodo= jdomParser.findElementByAt(dom,
									"ITEM",
									"DEFAULT",
									"TRUE",
									false);
					isDefault= nodo!=null;
				}

				Object valor= helperConstant.parseValue( tm,
									nodo.getAttributeValue("VAL_MIN"));

				if( valor!=null ){
					if( !online ) asignacion.put( at, valor );
					else{
						Object oldVal=m_domDocModel.getData( ses,new int[]{to,object} , at );
						boolean oldValIsNull= 	oldVal==null ||
									tm==helperConstant.TM_ENUMERADO &&
									((Integer)oldVal).intValue()==0;
						System.out.println("DEF,IMP,OLD:"+isDefault+","+imponer+","+oldVal);
						if( 	imponer ||
							isDefault && oldValIsNull  ){
							m_domSourceModel.setData( ses,new int[]{to,object}, at, valor );
							m_domDocModel.setData( ses,new int[]{to,object}, at,valor );
							subEventDataChanged(ses,to,object, at);
							m_calc.setDirectValues( m_domDocModel.getNumericValues(ses,to,object) );
						}
					}
				}
				}catch(ParseException pe){
					System.out.println("ERROR GETASIGN:FIJA en dominios."+pe.getMessage());
					pe.printStackTrace();
				}

			}
		}
		return asignacion;
	}


	public boolean checkDominio(int object, Iterator it, Object objVal) throws NumberFormatException{
		while(it.hasNext()){
			org.jdom.Element eRange=(org.jdom.Element) it.next();
			if(!checkDominio(object, eRange, objVal)) return false;
		}
		return true;
	}

	public boolean checkDominio(int object, org.jdom.Element range, Object objVal)
		throws NumberFormatException{
		if( range.getAttributeValue("TA_POS")==null ) return false;

		String rId= range.getAttributeValue("ID");
		if( rId==null ) rId="0@"+range.getAttributeValue("TA_POS");
		return checkDominio(object, range, rId, objVal);
	}

	private Object getSourceData( session ses, int to,int object, String idCampo )
                throws SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException{
		if( m_domSourceModel.containsObject( object ) ){
			System.out.println("SI CONTIENE SOURCE");
			return m_domSourceModel.getData( ses,new int[]{to,object}, idCampo );
		}else{
			System.out.println("NO CONTIENE SOURCE");
			return m_domDocModel.getData( ses,new int[]{to,object}, idCampo );
		}
	}

        public boolean checkMultiDominio(session ses, int to,int object, Iterator itr, boolean esIndirecta, String atChanged)
        throws NumberFormatException, SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException {
  		while(itr.hasNext()){
            		org.jdom.Element eRange= (org.jdom.Element) itr.next();
			String idCampo=eRange.getAttributeValue("ID");
			if( idCampo==null ) idCampo="0@"+eRange.getAttributeValue("TA_POS");
			if( esIndirecta && atChanged.equals(idCampo) )
				return false;
			Object objVal= getSourceData( ses,to,object, idCampo );
              		if(!checkDominio(object, eRange, objVal)) return false;
                }
                return true;
        }

	public boolean checkDominio(int object, org.jdom.Element range, String idCampo, Object objVal )
		throws NumberFormatException{
		if( range.getText()!=null && range.getChild("ITEM")==null )
			return true;// se trata de un dominio funcional
		try{
		System.out.println("RANGE:"+jdomParser.returnXML(range));
		System.out.println("CAMPO, VAL:"+idCampo+","+objVal);
		}catch(Exception e){;}
		if( !m_domSourceModel.fieldSupported( idCampo ) ) return true;

		if( objVal==null )
			return !(	range.getAttributeValue("NULLABLE")!=null &&
					range.getAttributeValue("NULLABLE").equals("FALSE") );
		int tm= m_md.getID_TM(m_domSourceModel.getTAPOS( idCampo ));

		Iterator iDom= range.getChildren("ITEM").iterator();
		boolean exito= false;
		while(iDom.hasNext()){
			org.jdom.Element item= (org.jdom.Element)iDom.next();
			switch(tm){
				case helperConstant.TM_BOOLEANO:{
					Boolean val= (Boolean)objVal;
					int intval= Integer.parseInt( item.getAttributeValue("VAL_MIN") );
					exito= val.booleanValue()==(intval==1);
					break;
				}
				case helperConstant.TM_ENUMERADO:{
					int vmin= Integer.parseInt(item.getAttributeValue("VAL_MIN"));
					int vmax= Integer.parseInt(item.getAttributeValue("VAL_MAX"));
					int val = ((Integer)objVal).intValue();
					if(val>=vmin && val<= vmax )
						exito=true;
					break;
				}
				case helperConstant.TM_ENTERO:{
					int vmin= Integer.parseInt(item.getAttributeValue("VAL_MIN"));
					int vmax= Integer.parseInt(item.getAttributeValue("VAL_MAX"));
					int val = ((Integer)objVal).intValue();
					if(val>=vmin && val<= vmax )
						exito=true;
					break;
				}
				case helperConstant.TM_REAL:{
					double vmin= Double.parseDouble(item.getAttributeValue("VAL_MIN"));
					double vmax= Double.parseDouble(item.getAttributeValue("VAL_MAX"));
					double val= ((Double)objVal).doubleValue();
					if(val>=vmin && val<= vmax )
						exito=true;
					break;
				}
			}
			if(exito) break;
		}

		return exito;
	}

	public ArrayList getActivesFunctionalsDoms(session ses,int to,int object, boolean ignoraIndirecta)
		throws SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException{
		ArrayList domActivos= new ArrayList();
		if( m_dom==null || m_dom.getChild("POLIMORFISMO")==null) return domActivos;
		Iterator iRest= m_dom.getChild("POLIMORFISMO").getChildren("REST").iterator();
getActivesFDomsItera:
		while(iRest.hasNext()){
			org.jdom.Element eRest= (org.jdom.Element)iRest.next();
			String atId= eRest.getAttributeValue("ID");
			if( atId==null ) atId="0@"+eRest.getAttributeValue("TA_POS");
			Iterator iRule= eRest.getChildren("RULE").iterator();
			while(iRule.hasNext()){
				org.jdom.Element eRule = (org.jdom.Element) iRule.next();
				org.jdom.Element eDom= eRule.getChild("DOMINIO");
				if( 	eDom.getText()!=null &&
					eDom.getText().length()>0 &&
					eDom.getChild("ITEM")==null ){

					if( eRule.getChild("COND")== null ){
						Integer ref= new Integer( eDom.getAttributeValue("REF") );
						domActivos.add( ref );
						continue getActivesFDomsItera;// un mismo at no puede tener
								//mas de una rule/dom activa
					}
					Iterator iCond= eRule.getChildren("COND").iterator();
					//Ahora compruebo si hay atributos condicionales
					while(iCond.hasNext()){
						org.jdom.Element eCond=(org.jdom.Element)iCond.next();

						boolean active=checkMultiDominio(ses,
                                                        to,
                                                        object,
                                                        eCond.getChildren("RANGE").iterator(),
                                                        false,
                                                        null);
						if( active ){
							Integer ref= new Integer( eDom.getAttributeValue("REF") );
							domActivos.add( ref );
							continue getActivesFDomsItera;// un mismo at no puede tener
									//mas de una rule/dom activa
						}
					}
				}
			}
		}
		return domActivos;
	}

	public ArrayList getActivesDoms(session ses,int to,int object, boolean ignoraIndirecta)
                throws SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException{
		ArrayList domActivos= new ArrayList();
		if( m_dom==null || m_dom.getChild("POLIMORFISMO")==null) return domActivos;
		Iterator iRest= m_dom.getChild("POLIMORFISMO").getChildren("REST").iterator();
getActivesDomsItera:
		while(iRest.hasNext()){
			org.jdom.Element eRest= (org.jdom.Element)iRest.next();

			String atId= eRest.getAttributeValue("ID");
			if( atId==null ) atId="0@"+eRest.getAttributeValue("TA_POS");
			if( m_iniciadorEvento.containsKey( atId ) ) continue;

			//if( !m_domSourceModel.fieldSupported( atId ) ) continue;
			Iterator iRule= eRest.getChildren("RULE").iterator();
			while(iRule.hasNext()){
				org.jdom.Element eRule = (org.jdom.Element) iRule.next();
				org.jdom.Element eDom= eRule.getChild("DOMINIO");

				if( eRule.getChild("COND")== null ){
					Integer ref= new Integer( eDom.getAttributeValue("REF") );
					domActivos.add( ref );
					continue getActivesDomsItera;// un mismo at no puede tener
							//mas de una rule/dom activa
				}
				Iterator iCond= eRule.getChildren("COND").iterator();
				//Ahora compruebo si hay atributos condicionales
				while(iCond.hasNext()){
					org.jdom.Element eCond=(org.jdom.Element)iCond.next();

					boolean active= checkMultiDominio(ses,
                                                to,
                                                object,
                                                eCond.getChildren("RANGE").iterator(),
                                                false,null);
					if( active ){
						Integer ref= new Integer( eDom.getAttributeValue("REF") );
						domActivos.add( ref );
						continue getActivesDomsItera;// un mismo at no puede tener
								//mas de una rule/dom activa
					}
				}
			}
		}
		return domActivos;
	}

	void refreshRestriction(session ses,int to,int object, String atChanged, String idForm)
                throws SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException{
		// idForm es el atributo al que aplicar las restricciones, no el condicionante
		boolean restrinjido= false;
		try{
		if( m_dom==null || m_dom.getChild("POLIMORFISMO")==null ) return;
		Iterator iRest= m_dom.getChild("POLIMORFISMO").getChildren("REST").iterator();
		while(iRest.hasNext()){
			org.jdom.Element eRest= (org.jdom.Element)iRest.next();
			String restId= eRest.getAttributeValue("ID");
			if( restId==null ) restId= "0@"+eRest.getAttributeValue("TA_POS");
			if(!restId.equals(idForm)) continue;
			restrinjido= true;
			//Si paso de aque es que este atributo posee restricciones
			//Ahora compruebo si hay atributos condicionales y si están activas
			//las reglas. [DEF] Me quedara con la primera regla activa

			//[DEF] no puede haber más de una restricción para un mismo atributo
			Iterator iRule=eRest.getChildren("RULE").iterator();
			while(iRule.hasNext()){
				org.jdom.Element eRule= (org.jdom.Element)iRule.next();
				Iterator iCond= eRule.getChildren("COND").iterator();
				org.jdom.Element eRestDom=eRule.getChild("DOMINIO");
				if(!iCond.hasNext()){
					//quiere decir que, en esta rule, el atributo está restrinjido
					// pero no condicionado. Entonces Puedo considerar esta
					// regla activa y ademas [DEF] no tiene sentido que haya
					// más reglas para este atr.
//V1:ADD
					m_domSourceModel.setNewRestriction(ses,new int[]{to,object}, idForm, eRestDom );
//V1:END
					return;
				}else
					while(iCond.hasNext()){
						org.jdom.Element eCond= (org.jdom.Element)iCond.next();
						boolean esIndirecta= eCond.getAttributeValue("DEPENDENCIA")!=null &&
								eCond.getAttributeValue("DEPENDENCIA").equals("INDIRECTA") ;
						Iterator iRange= eCond.getChildren("RANGE").iterator();
						if(!checkMultiDominio(	ses,
                                                        to,
                                                        object,
                                                        iRange,
                                                        esIndirecta,
                                                        atChanged)) continue;
						//paso luego rule activa
						m_domSourceModel.setNewRestriction(ses,new int[]{to,object},idForm, eRestDom );
						return;
					}
			}
			break;//no tengo que buscar más porque este atr no puede tener más restr
		}
		//si estoy restrinjido pero no se ha activado ninguna regla un reset
		if(restrinjido) m_domSourceModel.resetRestriction( new int[]{to,object}, idForm );
		}catch(NumberFormatException e){
			System.out.println("CHANGE REQUEST: ERROR Number Format");}
	}

   public boolean changeRequest( session ses, int to,int object, String idCampo, Object newVal )
	throws NumberFormatException, SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException {
	boolean restrinjido= false;

	System.out.println("CHANGE REQUEST:"+idCampo+","+isNullable( idCampo )+","+newVal+","+
					m_domSourceModel.isNull(ses,new int[]{to,object}, idCampo, newVal));
 	if( !m_domSourceModel.fieldSupported( idCampo ) ) return false;

       	if(	!isNullable( idCampo ) &&
		m_domSourceModel.isNull(ses,new int[]{to,object}, idCampo, newVal))
		return false;

	if( m_dom==null || m_dom.getChild("POLIMORFISMO")==null ) return true;

	Iterator iRest= m_dom.getChild("POLIMORFISMO").getChildren("REST").iterator();
	while(iRest.hasNext()){
		org.jdom.Element eRest= (org.jdom.Element)iRest.next();
		String restId= eRest.getAttributeValue("ID");
		if( restId==null ) restId= "0@"+eRest.getAttributeValue("TA_POS");
		if(!restId.equals(idCampo)) continue;
		restrinjido= true;
		//Si paso de aque es que este atributo posee restricciones
		//Ahora compruebo si hay atributos condicionales y si están activas
		//las reglas. [DEF] Me quedara con la primera regla activa

		//[DEF] no puede haber más de una restricción para un mismo atributo
		Iterator iRule=eRest.getChildren("RULE").iterator();
		while(iRule.hasNext()){
			org.jdom.Element eRule= (org.jdom.Element)iRule.next();
			Iterator iCond= eRule.getChildren("COND").iterator();
			org.jdom.Element eRestDom=eRule.getChild("DOMINIO");
			if(!iCond.hasNext()){
				//quiere decir que, en esta rule, el atributo está restrinjido
				// pero no condicionado. Entonces Puedo considerar esta
				// regla activa y ademas [DEF] no tiene sentido que haya
				// más reglas para este atr.
				if(!checkDominio(object, eRestDom, restId, newVal)) return false;
				else return changeReq_Rol_Condition(ses,to,object, idCampo, newVal);
			}else
			  	while(iCond.hasNext()){
					org.jdom.Element eCond= (org.jdom.Element)iCond.next();
					boolean esIndirecta= 	eCond.getAttributeValue("DEPENDENCIA")!=null &&
								eCond.getAttributeValue("DEPENDENCIA").equals("INDIRECTA") ;
					Iterator iRange= eCond.getChildren("RANGE").iterator();
					if(!checkMultiDominio(	ses,
                                                to,
                                                object,
                                                iRange,
                                                esIndirecta,
                                                idCampo )) continue;
					//paso luego rule activa
					if(!checkDominio(object, eRestDom, idCampo, newVal)) return false;
					else return changeReq_Rol_Condition(ses,to,object,idCampo, newVal);
				}
		}
		break;//no tengo que buscar más porque este atr no puede tener más restr
	}
	if(!restrinjido) return changeReq_Rol_Condition(ses,to,object, idCampo, newVal);
	return true;
   }


   public boolean changeReq_Rol_Condition(session ses, int to,int object, String idCampo, Object newVal)
	throws NumberFormatException, SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException {
	if( !m_domSourceModel.fieldSupported( idCampo ) ) return false;
	if(!m_condicionantes_List.containsKey(idCampo))	return true;

	ArrayList atsAfectados= new ArrayList();
	ArrayList condicionantes= (ArrayList)m_condicionantes_List.get(idCampo);
	Iterator iCond= condicionantes.iterator();
	//compruebo si alguno de los condicionantes donde aparece se activa
	while(iCond.hasNext()){
		org.jdom.Element eCond= (org.jdom.Element)iCond.next();
		if( eCond.getName().equals("DOMINIO") ) continue;//se trata de un dom funcional
		boolean restrinjimos= checkMultiDominio(ses,to,object, eCond.getChildren("RANGE").iterator(),false,null);
		boolean restrinjiamos= functionValuesContainsKey( object, eCond );

		String sat=eCond.getParent().getParent().getAttributeValue("TA_POS");
		String key= eCond.getParent().getParent().getAttributeValue("ID");
		if( key==null ) key= "0@"+sat;
		org.jdom.Element eDom= eCond.getParent().getChild("DOMINIO");

		Object val=getSourceData( ses,to,object, key );
                    if (restrinjimos &&
                        !restrinjiamos &&
                        !(val == null && m_domDocModel != null &&
                          ((docDataModel) m_domDocModel).getInstanceID() < 0) &&
                        !checkDominio(object,
                                      eDom,
                                      key,
                                      val)) {
                        atsAfectados.add(eCond.getParent().getParent().
                                         getAttributeValue("AT_NAME"));
                    }
	}
	//no he encontrado ninguna condición que haya cambiado a más restrictiva
	// y que además de más restrictiva implique un dominio de restricción que
	//no se cumpla
	if( atsAfectados.size()>0 ){
		JTextArea jt= new JTextArea("EL VALOR SELECCIONADO REQUIERE RESETEAR LOS SIGUIENTES CAMPOS:");
		for( int i=0; i< atsAfectados.size();i++ ){
			jt.append( "\n"+ (String)atsAfectados.get(i) );
		}
		jt.append("\n Desea continuar?");
		int option=JOptionPane.showConfirmDialog( null, jt, "ADVERTENCIA", JOptionPane.YES_NO_OPTION );
		if( option== JOptionPane.YES_OPTION ) return true;
		else return false;
	}
	return true;
   }
}
*/