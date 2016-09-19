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

import dynagent.knowledge.metaData;

public class funciones_1 extends Object implements calculo{
	HashMap relationalData;
	HashMap directValues;
	HashMap filterMap;
	String taF= "0@"+String.valueOf( helperConstant.TAPOS_FILTER_ROOT );		
	
	public HashMap getFilterMap(){
		return relationalData;
	}

	public void inicializar( HashMap relationalData, HashMap directValues, HashMap filterMap ){
		this.relationalData=relationalData;
		this.directValues=directValues;
		this.filterMap=filterMap;
	}
	public Object[] unir( Object[] oa,Object[] ob){
		ArrayList aOa= new ArrayList( Arrays.asList( oa ) );
		aOa.addAll( (Collection)Arrays.asList( ob ) );
		return aOa.toArray();
	}

	public void orderBy( Object[] objs, order[] orderCommand ){
		ordenadorAts oats= new ordenadorAts( orderCommand );
		Arrays.sort( objs, oats );
	}

	public HashMap getDirectValues(){
		return directValues;
	}

	private double sumar( ArrayList lista ){
		double res=0;
		for( int i=0; i< lista.size(); i++ ){
			Double item=(Double)lista.get(i);
			if( item==null ) continue;
			res+= item.doubleValue();
		}
		return res;
	}

	private double primeroDe( ArrayList lista ){
		for( int i=0;i<lista.size();i++)
			if( lista.get(i)!=null )
				return ((Double)lista.get(i)).doubleValue();
		return 0;
	}

	private String primeroDeStr( ArrayList lista ){
		for( int i=0;i<lista.size();i++)
			if( lista.get(i)!=null )
				return (String)lista.get(i);
		return "";
	}

	private int sw( double test, int validVal ){
		if( test==validVal ) return 1;
		else return 0;
	}

	private String limpiaDecimal( double val ){
		Double d= new Double( val );
		return String.valueOf(d.intValue());
	}

	public void setDirectValues( HashMap directValues ){
		this.directValues=directValues;
	}

	public void setRelationalData( HashMap relationalData ){
		this.relationalData=relationalData;
	}

	public void setFilterMap( HashMap filterMap ){
		this.filterMap=filterMap;
	}
	private Object[] refiltrar( int filterRoot, int cascadingFilter ){
		ArrayList result= new ArrayList();
		try{
		Integer iFRoot= new Integer(filterRoot);
		if( !relationalData.containsKey( iFRoot ) ) return result.toArray();
		Element fRoot= (Element)filterMap.get( iFRoot );

		ArrayList objetos= (ArrayList)relationalData.get( iFRoot );
		if( cascadingFilter==-1 ) return objetos.toArray();

		Element fCas= md.getFilter( null, new Integer( cascadingFilter ));		

		for( int i=0;i<objetos.size();i++){
			HashMap objAts= (HashMap)objetos.get( i );
			Integer idTO= (Integer)objAts.get( "0@"+String.valueOf( helperConstant.TAPOS_METATIPO ) );
			int idTOCas= Integer.parseInt( fCas.getAttributeValue("ID_TO") );
			if( !md.isSpecializedFrom( idTO.intValue(), idTOCas ))
				continue;

			if( testFilter( objAts, fRoot, fCas ) )
				result.add( objAts );
			
		}
		}catch(Exception e){
			System.out.println("ERROR funciones_1 refiltrar:"+e.getClass()+","+e.getMessage());
			e.printStackTrace();
		}
		return result.toArray();
	} 

	private boolean testFilter( HashMap ats, Element filterRoot, Element filter ){
		Iterator itr= filter.getChildren("AVA").iterator();
		while( itr.hasNext() ){
			Element ava= (Element)itr.next();
			if( !testAVA( ats, ava, filterRoot, filter ) ) return false;
		}
		itr= filter.getChildren("FILTER").iterator();
		while( itr.hasNext() ){
			Element child= (Element)itr.next();
			if( !testFilter( ats, filterRoot, child ) ) return false;
		}
		return true;
	}

	private boolean testAVA( HashMap dataAts, Element ava, Element filterRoot, Element fEM ){
		int tapos= Integer.parseInt( ava.getAttributeValue("TA_POS") );
		int refEmbed= Integer.parseInt( ava.getParent().getAttributeValue("REF") );

		int refRoot= md.findEmbedAtInRoot( filterRoot, tapos, refEmbed, fEM );

		if( refRoot==-1){
			System.out.println("**** WARNING, funciones_1 test AVA, refRoot not Found******");
			System.out.println("TAPOS, refEmbed:" + tapos + "," + refEmbed );
			try{
				System.out.println("FROOT:"+jdomParser.returnXML( filterRoot ));
				System.out.println("FEM:"+jdomParser.returnXML( fEM ));
			
			}catch(Exception e){;}
			return false;
		}
		String relationalAtKey= refRoot + "@" + tapos;

		if( !dataAts.containsKey( relationalAtKey ) ) return false;

		Double dataAt= (Double)dataAts.get( relationalAtKey );

		
		String op= ava.getAttributeValue("OP");
		int tm= md.getID_TM( new Integer(tapos) );
		if( tm == helperConstant.TM_ENUMERADO || tm== helperConstant.TM_ENTERO ) {
				int rest= Integer.parseInt(ava.getAttributeValue("VALUE"));
				int val= dataAt.intValue();
				if( op.equals("=") ){
					System.out.println("CHECKEO OK en :"+tapos);
					return val==rest;
				}
		}
		if( tm== helperConstant.TM_REAL ){
				double rest= Double.parseDouble(ava.getAttributeValue("VALUE"));
				double val= dataAt.doubleValue();
				if( op.equals("=") ){
					return val==rest;
				}
		}
		return false;
	}
	public Object calcular( int dom, String idCampo, int ref){
		switch(dom){
			case 101:
				{
					if(idCampo.equals("0@19"))
						switch(ref){
							case 1:{

								String V_19= "0@19";
								if( !( directValues.containsKey( V_19))) return null;
								double RES=((Double)directValues.get(V_19)).doubleValue()+1;
								return new Double(RES);
							}
						}
				}
			case 104:
				{
					if(idCampo.equals("0@1"))
						switch(ref){
							case 1:{

								ArrayList VAL_F4_0_14=new ArrayList();
								Object[] DATA_F4_0_14= refiltrar( 127,-1);
								for( int i=0;i< DATA_F4_0_14.length;i++){
									HashMap ROW_F4_0_14= (HashMap)DATA_F4_0_14[i];
									if( !( ROW_F4_0_14.containsKey( "0@19"))) continue;
									if( ROW_F4_0_14.get( "0@19")==null){
										VAL_F4_0_14.add( null );
										continue;
										}

									double value=((Double)ROW_F4_0_14.get("0@19")).doubleValue();
									VAL_F4_0_14.add( new Double(value));
								}

								String RES=limpiaDecimal(primeroDe(VAL_F4_0_14));
								return RES;
							}
						}
				}
			case 105:
				{
					if(idCampo.equals("0@1"))
						switch(ref){
							case 2:{

								ArrayList VAL_F4_0_14=new ArrayList();
								Object[] DATA_F4_0_14= refiltrar( 127,-1);
								for( int i=0;i< DATA_F4_0_14.length;i++){
									HashMap ROW_F4_0_14= (HashMap)DATA_F4_0_14[i];
									if( !( ROW_F4_0_14.containsKey( "0@19"))) continue;
									if( ROW_F4_0_14.get( "0@19")==null){
										VAL_F4_0_14.add( null );
										continue;
										}

									double value=((Double)ROW_F4_0_14.get("0@19")).doubleValue();
									VAL_F4_0_14.add( new Double(value));
								}

								String RES=limpiaDecimal(primeroDe(VAL_F4_0_14));
								return RES;
							}
						}
				}
			case 106:
				{
					if(idCampo.equals("0@1"))
						switch(ref){
							case 1:{

								ArrayList VAL_F4_0_14=new ArrayList();
								Object[] DATA_F4_0_14= refiltrar( 127,-1);
								for( int i=0;i< DATA_F4_0_14.length;i++){
									HashMap ROW_F4_0_14= (HashMap)DATA_F4_0_14[i];
									if( !( ROW_F4_0_14.containsKey( "0@19"))) continue;
									if( ROW_F4_0_14.get( "0@19")==null){
										VAL_F4_0_14.add( null );
										continue;
										}

									double value=((Double)ROW_F4_0_14.get("0@19")).doubleValue();
									VAL_F4_0_14.add( new Double(value));
								}

								String RES=limpiaDecimal(primeroDe(VAL_F4_0_14));
								return RES;
							}
						}
				}
			case 108:
				{
					if(idCampo.equals("0@1"))
						switch(ref){
							case 1:{

								ArrayList VAL_F4_0_14=new ArrayList();
								Object[] DATA_F4_0_14= refiltrar( 128,-1);
								for( int i=0;i< DATA_F4_0_14.length;i++){
									HashMap ROW_F4_0_14= (HashMap)DATA_F4_0_14[i];
									if( !( ROW_F4_0_14.containsKey( "1@19"))) continue;
									if( ROW_F4_0_14.get( "1@19")==null){
										VAL_F4_0_14.add( null );
										continue;
										}

									double value=((Double)ROW_F4_0_14.get("1@19")).doubleValue();
									VAL_F4_0_14.add( new Double(value));
								}

								String RES=limpiaDecimal(primeroDe(VAL_F4_0_14));
								return RES;
							}
						}
				}
			case 109:
				{
					if(idCampo.equals("0@1"))
						switch(ref){
							case 2:{

								ArrayList VAL_F4_0_14=new ArrayList();
								Object[] DATA_F4_0_14= refiltrar( 127,-1);
								for( int i=0;i< DATA_F4_0_14.length;i++){
									HashMap ROW_F4_0_14= (HashMap)DATA_F4_0_14[i];
									if( !( ROW_F4_0_14.containsKey( "0@19"))) continue;
									if( ROW_F4_0_14.get( "0@19")==null){
										VAL_F4_0_14.add( null );
										continue;
										}

									double value=((Double)ROW_F4_0_14.get("0@19")).doubleValue();
									VAL_F4_0_14.add( new Double(value));
								}

								String RES=limpiaDecimal(primeroDe(VAL_F4_0_14));
								return RES;
							}
						}
				}
			case 113:
				{
					if(idCampo.equals("0@1"))
						switch(ref){
							case 1:{

								ArrayList VAL_F4_0_14=new ArrayList();
								Object[] DATA_F4_0_14= refiltrar( 127,-1);
								for( int i=0;i< DATA_F4_0_14.length;i++){
									HashMap ROW_F4_0_14= (HashMap)DATA_F4_0_14[i];
									if( !( ROW_F4_0_14.containsKey( "0@19"))) continue;
									if( ROW_F4_0_14.get( "0@19")==null){
										VAL_F4_0_14.add( null );
										continue;
										}

									double value=((Double)ROW_F4_0_14.get("0@19")).doubleValue();
									VAL_F4_0_14.add( new Double(value));
								}

								String RES=limpiaDecimal(primeroDe(VAL_F4_0_14));
								return RES;
							}
						}
				}
			case 116:
				{
					if(idCampo.equals("0@178"))
						switch(ref){
							case 1:{

								String V_176= "0@176";
								if( !( directValues.containsKey( V_176))) return null;
								String V_177= "0@177";
								if( !( directValues.containsKey( V_177))) return null;
								double RES=((Double)directValues.get(V_176)).doubleValue()+((Double)directValues.get(V_177)).doubleValue();
								return new Double(RES);
							}
						}
					if(idCampo.equals("0@174"))
						switch(ref){
							case 2:{

								String V_173= "0@173";
								if( !( directValues.containsKey( V_173))) return null;
								double RES=((Double)directValues.get(V_173)).doubleValue()*166.386;
								return new Double(RES);
							}
						}
					if(idCampo.equals("0@173"))
						switch(ref){
							case 3:{

								String V_174= "0@174";
								if( !( directValues.containsKey( V_174))) return null;
								double RES=((Double)directValues.get(V_174)).doubleValue()/166.386;
								return new Double(RES);
							}
						}
					if(idCampo.equals("0@1"))
						switch(ref){
							case 7:{

								ArrayList VAL_F4_0_14=new ArrayList();
								Object[] DATA_F4_0_14= refiltrar( 127,-1);
								for( int i=0;i< DATA_F4_0_14.length;i++){
									HashMap ROW_F4_0_14= (HashMap)DATA_F4_0_14[i];
									if( !( ROW_F4_0_14.containsKey( "0@19"))) continue;
									if( ROW_F4_0_14.get( "0@19")==null){
										VAL_F4_0_14.add( null );
										continue;
										}

									double value=((Double)ROW_F4_0_14.get("0@19")).doubleValue();
									VAL_F4_0_14.add( new Double(value));
								}

								String RES=limpiaDecimal(primeroDe(VAL_F4_0_14));
								return RES;
							}
						}
				}
		}
		return null;
	}
}*/