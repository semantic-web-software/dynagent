package dynagent.knowledge.old;
/*package dynagent.knowledge;

import org.jdom.Element;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Comparator;

import dynagent.ejb.*;

class parDomRol extends Object{
	public parDomRol( Element dom, Integer rol ){
		this.dom=dom;
		this.rol=rol;
	}
	Element dom;
	Integer rol;
}

class userRolPrior extends Object{
	public userRolPrior( Integer rol, int prior ){
		this.prior=prior;
		this.rol=rol;
	}
	int prior;
	Integer rol;
}

class comparaDom implements Comparator{
	boolean  ordenRestrictivoCreciente;
	public comparaDom( boolean  ordenRestrictivoCreciente ){
		this.ordenRestrictivoCreciente= ordenRestrictivoCreciente;
	}
	public int compare( Object objA, Object objB ){
		if( !(objA instanceof parDomRol) || !(objB instanceof parDomRol))
			throw new ClassCastException();
		Element domA=ordenRestrictivoCreciente ? ((parDomRol)objA).dom:((parDomRol)objB).dom;
		Element domB=ordenRestrictivoCreciente ? ((parDomRol)objB).dom:((parDomRol)objA).dom;
		boolean hasA= 	domA.getChild("PROPERTY_LIST")!=null &&
				domA.getChild("PROPERTY_LIST").getChild("ATRIBUTO")!=null ;

		boolean hasB= 	domB.getChild("PROPERTY_LIST")!=null &&
				domB.getChild("PROPERTY_LIST").getChild("ATRIBUTO")!=null ;

		System.out.println("hasA,hasB:"+hasA+","+hasB);
		if( !hasA && hasB ) return -1;
		if( hasA && !hasB ) return 1;
		if( !hasA && !hasB ) return 0;
		int comp= subCompareRestrictive( domA, domB );
		System.out.println("COMP:"+comp);
		if( comp!=0 ) return comp;
		return -1*subCompareRestrictive( domB, domA );
	}

	private int subCompareRestrictive( Element domA, Element domB ){
		Iterator itr= domA.getChild("PROPERTY_LIST").getChildren("ATRIBUTO").iterator();
		while( itr.hasNext() ){
			Element atA=(Element)itr.next();
			Element peer= jdomParser.findElementByAt( 	domB.getChild("PROPERTY_LIST"),
									"ATRIBUTO",
									"TA_POS",
									atA.getAttributeValue("TA_POS"),
									false );
			//System.out.println("PEER:"+peer);
			if( peer==null ) return 1;
			int comp= compareRestrictive( atA.getAttributeValue("ACCESS"), peer.getAttributeValue("ACCESS"));
			if( comp!=0 ) return comp;
		}
		return 0;
	}

	private int compareRestrictive( String accessA, String accessB ){
		if( 	accessA==null && accessB==null ) return 0;
		if( 	accessA==null && accessB!=null ) return -1;
		if( 	accessA!=null && accessB==null ) return 1;
		if( 	accessA.length()>0 && accessB.length()==0 ) return -1;//long==0 es lo mas restrictivo.
		if( 	accessA.length()==0 && accessB.length()>0 ) return 1;
		if( 	accessA.length()==0 && accessB.length()==0 ) return 0;

		int comp= subCompareRestrictive( accessA, accessB );
		if( comp!=0 ) return comp;
		comp= subCompareRestrictive( accessB, accessA );
		return -1*comp;
	}


	private int subCompareRestrictive( String accessA, String accessB ){
		String[] listaA= accessA.split(";");
		for( int i=0; i<listaA.length; i++ ){
			if( accessB.indexOf( listaA[i] )==-1 ) return -1;
		}
		return 0;
	}

	public boolean equals( Object obj ){
		return false;
	}
}

public class rolProperties{
	public static String getMaxAccess( Element eContext ){
		Iterator itr= eContext.getChildren("ROL_PROPERTIES").iterator();
		String res="";
		while( itr.hasNext() ){
			Element userRol=(Element)itr.next();
			String[] acceso= userRol.getAttributeValue("ACCESSS").split(";");
			for( int i=0;i<acceso.length;i++){
				if( res.indexOf( acceso[i] )==-1 ){
					if( res.length()==0 ) res=acceso[i];
					else res+=";"+acceso[i];
				}
			}
		}
		return res;
	}

	public static ArrayList getRol( String user, boolean ordenRestrictivoCreciente  ){
		//System.out.println("GETROL");
		HashMap roles= new HashMap();
		if( ctxRoot.id== helperConstant.CTX_TASK ){
			ArrayList va= new ArrayList();
			va.add( new Integer( helperConstant.USER_ROL_SYSTEM ) );
			return va;
		}
		//Iterator iCtx=  eContextos==null ? null:eContextos.getChildren("CONTEXTO").iterator();
		//Contexto currCtx=ctxRoot;

//		while( true ){
//			setRolAccess( user, md, currCtx, modo, roles );
//			if( eContextos==null || !iCtx.hasNext() ) break;
//			Element eCtx=(Element)iCtx.next();
//			Integer idCurr= new Integer(eCtx.getAttributeValue("ID"));
//			currCtx= md.getContext(idCurr);
//		}

		class comparador implements Comparator{
			boolean creciente=true;
			public comparador( boolean  orden ){
				creciente= orden;
			}
			public int compare( Object objA, Object objB ){
				if( !(objA instanceof userRolPrior) || !(objB instanceof userRolPrior))
					throw new ClassCastException();
				int pA= ((userRolPrior)objA).prior;
				int pB= ((userRolPrior)objB).prior;
				if( pA<pB ) return (creciente ? 1:-1)*(-1);
				if( pB<pA ) return (creciente ? 1:-1);
				return 0;
			}
		}

		Iterator iM= roles.keySet().iterator();
		Object[] lista= new Object[roles.size()];
		int pos=0;
		while( iM.hasNext()){
			Integer rol=(Integer)iM.next();
			Integer prior= (Integer)roles.get(rol);
			lista[pos++]= new userRolPrior( rol, prior.intValue() );
		}

		Arrays.sort(lista, new comparador(ordenRestrictivoCreciente));
		ArrayList<Integer> res= new ArrayList<Integer>();
		for( int i=0; i<lista.length; i++)
			res.add(((userRolPrior)lista[i]).rol);
		return res;
	}

	private static void setRolAccess(String user, metaData md, Contexto ctx, int modo, HashMap roles ){

		ArrayList lista= ctx.getUserRolSet( md, user );//como estamos en el applet se supone que solo hay roles
								//de este usuario
		ArrayList rolesValidos= new ArrayList();

		for(int i=0;i<lista.size();i++){
			Integer userRol=(Integer)lista.get(i);
			if( user!=null && !md.userPlayRol( user, userRol ) ){
				//incrementaPeso( roles, userRol, 40 );
				continue;
			}
			access acceso= ctx.getAcceso( userRol );
			if( 	modeEnable( acceso, modo ) ){
				incrementaPeso( roles, userRol, puntuaRol( md, ctx, userRol ) );
			}else
				incrementaPeso( roles, userRol, 40 );
		}
	}

	public static boolean modeEnable( access acceso, int modo ){
		return  	modo==access.NEW && acceso.getNewAccess() ||
				modo==access.SET && acceso.getSetAccess() ||
				modo==access.VIEW && acceso.getViewAccess() ||
				modo==appControl.OBJ_SELECTION && acceso.getViewAccess();
	}

	private static void incrementaPeso( HashMap roles, Integer userRol, int increm ){
		if( !roles.containsKey( userRol ) ) roles.put( userRol, new Integer(0) );
		Integer peso= (Integer)roles.get( userRol );
		roles.put( userRol, new Integer( peso.intValue() + increm ) );
	}


	private static int puntuaRol( metaData md, Contexto ctx, Integer rol ){
		Element dom = md.getDominio( ctx.getDom( rol ) );
		if( dom.getChild("PROPERTY_LIST")==null ) return 0;

		int peso=0;
		Iterator itr= dom.getChild("PROPERTY_LIST").getChildren("ATRIBUTO").iterator();
		while( itr.hasNext() ){
			Element prop=(Element)itr.next();
			String acceso= prop.getAttributeValue("ACCESS");
			if( acceso==null || acceso.length()==0 ){
				peso+=10;
				continue;
			}

			if( !acceso.equals( "FILTER" ) && acceso.indexOf( "READ" )==-1 ){
				peso+=10;
				continue;
			}
			if( acceso.equals( "FILTER" ) ){
				peso+=5;
				continue;
			}
			if( acceso.indexOf( "WRITE" )==-1 )
				peso+=5;

		}
		return peso;
	}

	public static Element restarDom( Element TOa, Element domA, Element domB ){
		//devuelve domA - domB. Se supone que domA es mas abierto
		if( domB==null ) return domA;
		boolean hasA= 	domA.getChild("PROPERTY_LIST")!=null &&
				domA.getChild("PROPERTY_LIST").getChild("ATRIBUTO")!=null ;

		boolean hasB= 	domB.getChild("PROPERTY_LIST")!=null &&
				domB.getChild("PROPERTY_LIST").getChild("ATRIBUTO")!=null ;
		if( !hasA && !hasB ) return null;
		if( hasA && !hasB ) return domA;

		Element res= (Element)domA.clone();
		if( res.getChild("PROPERTY_LIST")==null )
			res.addContent( new Element("PROPERTY_LIST") );
		Element prop= res.getChild("PROPERTY_LIST");

		Iterator itr= TOa.getChild("ATRIBUTOS").getChildren("PACKET").iterator();
		while( itr.hasNext() ){
			Element pk=(Element)itr.next();
			Iterator iAt= pk.getChildren("ATRIBUTO").iterator();
			while( iAt.hasNext() ){
				Element at=(Element)iAt.next();
				Integer tapos= new Integer( at.getAttributeValue("TA_POS") );

				Element restA= jdomParser.findElementByAt( 	prop,
										"ATRIBUTO",
										"TA_POS",
										tapos.toString(),
										false );

				Element restB= jdomParser.findElementByAt( 	domB.getChild("PROPERTY_LIST"),
										"ATRIBUTO",
										"TA_POS",
										tapos.toString(),
										false );

				if( restB==null ){
					if( restA==null ){
						restA= new Element("ATRIBUTO");
						restA.setAttribute("TA_POS", at.getAttributeValue("TA_POS"));
						restA.setAttribute("ACCESS","");
						prop.addContent( restA );
						continue;
					}else
						restA.setAttribute("ACCESS", "");
				}
			}
		}
		itr= domB.getChild("PROPERTY_LIST").getChildren("ATRIBUTO").iterator();
		while( itr.hasNext() ){
			Element restB=(Element)itr.next();
			Integer tapos= new Integer( restB.getAttributeValue("TA_POS") );
			Element restA= jdomParser.findElementByAt( 	prop,
									"ATRIBUTO",
									"TA_POS",
									tapos.toString(),
									false );
			if( restA==null ){
				restB=(Element)restB.clone();
				restB.setAttribute("ACCESS",restaAccesos("READ;WRITE",restB.getAttributeValue("ACCESS")));
				prop.addContent( restB );
			}else
				restA.setAttribute("ACCESS", restaAccesos(	restA.getAttributeValue("ACCESS"),
										restB.getAttributeValue("ACCESS") ));
		}
		return res;
	}

	public static String restaAccesos( String accA, String accB ){
		if( accB.length()==0 ) return accA;
		String res="";
		if( 	!accB.equals( "FILTER" ) && accB.indexOf( "READ" )==-1 ) res="READ";
		else
			return "";

		if( !accB.equals( "FILTER" ) && accB.indexOf( "WRITE" )==-1 ){
			if( res.length()==0 ) res+=";";
			res+="WRITE";
		}
		return res;
	}
}
*/