package dynagent.knowledge.old;
/*package dynagent.knowledge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Set;
import org.jdom.Element;

import dynagent.ejb.*;

public class Contexto extends Object {
	public int process=0;
	public int task=0;
	public int tran=0;
	public int action=0;
	public int id=0;
	public int toRoot=0;
	public int to=0;
	public int idRel=0;
	public int idRolCtx=0;
	public int idRolCurr=0;
	public int idFilter=0;
	public int idImagen=0;
	public int nMax=0, nMin=0;
	public boolean childIsSup;
	public boolean fusionado=false;
	public boolean metaCtx=false;
       	public boolean filterPersistence=false;

	public HashMap properties=new HashMap(), dominios= new HashMap(), detail= new HashMap();
	public HashMap reports=new HashMap();

	private HashMap refDomCondnados= new HashMap();
	public ArrayList filterFix= new ArrayList();
	public ArrayList areasFuncionales= new ArrayList();

	public Object clone(){
		Contexto res= new Contexto(metaCtx,id,process,task,tran,action,toRoot,to,idRel,idRolCtx,
					  idRolCurr,idFilter,nMin,nMax,childIsSup,idImagen,filterPersistence );
                res.fusionado=fusionado;
		res.filterFix= (ArrayList)metaData.cloneObject(filterFix);
		res.refDomCondnados=(HashMap)metaData.cloneObject(refDomCondnados);
		res.properties= (HashMap)metaData.cloneObject(properties);
		res.dominios= (HashMap)metaData.cloneObject(dominios);
		res.detail= (HashMap)metaData.cloneObject(detail);
		res.areasFuncionales= (ArrayList)metaData.cloneObject(areasFuncionales);
                res.reports=(HashMap)metaData.cloneObject(reports);

		return res;
	}

	public void addAreaFuncional( String id ){
		areasFuncionales.add( id );
	}

	public void addReport( int rol, String reportOID ){
		Integer iRol= new Integer(rol);
		ArrayList lista= getReport( iRol );
		if( lista==null ){
			lista= new ArrayList();
			reports.put( iRol, lista );
		}
		lista.add( reportOID );
	}

	public ArrayList getReport( Integer iRol ){
		return (ArrayList)reports.get( iRol );
	}

	public ArrayList getAreasFunc(){
		return (ArrayList)areasFuncionales.clone();
	}

	public void addAcceso( int userRol, String props, int idDom, Element detail ){
		Integer iRol= new Integer( userRol );
		if( props!=null ) properties.put( iRol, new access(props) );
		if( detail!=null ) this.detail.put( iRol, detail );
		dominios.put( iRol, new Integer(idDom==0 ? helperConstant.DOM_DEFAULT:idDom));
	}

	public void setDom( Integer userRol, int idDom ){
		dominios.put( userRol, new Integer(idDom==0 ? helperConstant.DOM_DEFAULT:idDom));
	}

	public boolean userRolEnable( int userRol ){
		Integer idu= new Integer( userRol );
		return properties.containsKey( idu );
	}
	public boolean userRolEnable( Integer userRol ){
		return properties.containsKey( userRol );
	}
	public boolean userEnable( metaData md, String user ){
		return getUserRolSet( md, user ).size()>0 ;
	}
	public boolean userRolEnable( Integer userRol, int modo ){
		if( !properties.containsKey( userRol ) ) return false;
		access acceso = getAcceso( userRol );
		System.out.println( "CTX ROL ENABLE:"+acceso+","+userRol+","+modo);
		return rolProperties.modeEnable( acceso, modo ) ;
	}

	public Integer getDom( int userRol ){
		return getDom( new Integer(userRol) );
	}

	public Integer getDom( Integer userRol ){
		if( dominios.containsKey( userRol ) )
			return (Integer)dominios.get( userRol );
		else
			return new Integer(helperConstant.DOM_DEFAULT);
	}

	public Integer getDom(){//se supone que se llama desde el cliente donde solo hay un rol
		if( dominios.size()>1 || dominios.size()==0 ) return null;
		Iterator itr=dominios.keySet().iterator();
		Integer idu=(Integer)itr.next();
		return (Integer)dominios.get( idu );
	}

	public ArrayList getUserRolSet( metaData md, String user ){
		ArrayList res= new ArrayList();
		Iterator itr= properties.keySet().iterator();
		while( itr.hasNext() ){
			Integer idRol=(Integer)itr.next();
                        System.out.println("USER,ROL:"+user+","+idRol);
			if( !md.userPlayRol( user, idRol ) ) continue;
			res.add( idRol );
		}
		return res;
	}

	public Set getUserRolSet( ){
		return properties.keySet();
	}

	public Element getDetail( Integer userRol ){
		return (Element)detail.get( userRol );
	}

	public access getAcceso( int userRol ){
		return getAcceso( new Integer(userRol) );
	}

	public access getAcceso( Integer userRol ){
            if( userRol.intValue()==helperConstant.USER_ROL_SYSTEM )
                return new access(true,true,true,true,true,true);
            	return new access(true,true,true,true,true,true,true);

            if( id==helperConstant.CTX_TASK )
            	//return new access(true,false,false,false,false,false);
                return new access(true,false,false,false,false,false,true);
            else
                return (access)properties.get( userRol );
	}

	public access getAcceso(){//se supone que se llama desde el cliente donde solo hay un rol
		if( properties.size()>1 || properties.size()==0 ) return null;
		Iterator itr=properties.keySet().iterator();
		Integer idu=(Integer)itr.next();
		return (access)properties.get( idu );
	}


	public void addRefDomCondicionado( int uRol, Object par ){
		Integer idu= new Integer(uRol);
		ArrayList lista= (ArrayList)refDomCondnados.get( idu );
		if( lista==null ){
			lista= new ArrayList();
			refDomCondnados.put( idu, lista );
		}
		lista.add( par );
	}

	public ArrayList getRefDomCondicionado( Integer uRol ){
		return (ArrayList)refDomCondnados.get( uRol );
	}

	public boolean isFromThisArea( String area ){
		if( area==null ) return true;
		return areasFuncionales.contains( area );
	}

	public boolean equals( Object obj ){
		if( !(obj instanceof Contexto) ) return false;
		Contexto ctxB=(Contexto)obj;
		return ( task==ctxB.task && tran==ctxB.tran && to==ctxB.to && toRoot==ctxB.toRoot &&
			idRel== ctxB.idRel && idRolCtx== ctxB.idRolCtx && idRolCurr==ctxB.idRolCurr);
	}

	public Contexto( boolean metaCtx,int id, int to, int toRoot, int idRel, int idRolCtx,
			int idRolCurr, int idFilter, int nMin, int nMax, boolean childIsSup,
			int imagen ){
		this.metaCtx=metaCtx;
		this.idImagen=imagen;
		this.id=id;
		this.toRoot=toRoot;
		this.to=to;
		this.idRel=idRel;
		this.idRolCtx=idRolCtx;
		this.idRolCurr=idRolCurr;
		//this.idDom=idDom==0 ? helperConstant.DOM_DEFAULT:idDom;
		this.idFilter=idFilter;
		this.nMin=nMin;
		this.nMax=nMax;
		this.childIsSup= childIsSup;
	}

	public Contexto( boolean metaCtx,int id, int process,int task, int tran, int action, int toRoot, int to, int idRel,
			int idRolCtx, int idRolCurr, int idFilter, int nMin, int nMax,
			boolean childIsSup, int imagen, boolean filterPersistence ){
		this.metaCtx=metaCtx;
		this.idImagen=imagen;
		this.process=process;
		this.id=id;
		this.task=task;
		this.tran=tran;
		this.action=action;
		this.to=to;
		this.toRoot=toRoot;
		this.idRel=idRel;
		this.idRolCtx=idRolCtx;
		this.idRolCurr=idRolCurr;
		//this.idDom=idDom==0 ? helperConstant.DOM_DEFAULT:idDom;
		this.idFilter=idFilter;
                this.filterPersistence=filterPersistence;
		this.nMin=nMin;
		this.nMax=nMax;
		this.childIsSup= childIsSup;
	}

	public int getImageID(){ return idImagen; }

	public void addFilterFix( boolean optional, String oidFilter, int ctxRef,int refSource,Element detail, int incrustar ){
		fixProperties fp= new fixProperties( optional, oidFilter, ctxRef, refSource, detail, incrustar );
		filterFix.add( fp );

	}

	public Iterator getFixList(){
		return filterFix.iterator();
	}
        public boolean hasFixing(){
            return filterFix.size()>0;
        }
	public Element toElement( ArrayList userRolList){
		boolean userEnable=false;

		Element eCtx= new Element("ITEM");
		eCtx.setAttribute("ID", String.valueOf(id) );
		eCtx.setAttribute("META", (metaCtx ? "TRUE":"FALSE") );

		if( getImageID()!=0 )
			eCtx.setAttribute("ID_IMG", String.valueOf(getImageID()));
		eCtx.setAttribute("ID_TO", String.valueOf(to) );
		eCtx.setAttribute("ID_TO_ROOT", String.valueOf(toRoot) );
		eCtx.setAttribute("ID_FILTER", String.valueOf(idFilter) );
		eCtx.setAttribute("ID_ACTION", String.valueOf(action) );
		if( process!=0 ){
			eCtx.setAttribute("ID_PROCESS", String.valueOf(process) );
			eCtx.setAttribute("ID_TASK", String.valueOf(task) );
			eCtx.setAttribute("ID_TRAN", String.valueOf(tran) );
		}
                if( filterPersistence )
                    eCtx.setAttribute("F_PERSISTENCE", "TRUE" );
		eCtx.setAttribute("REL", String.valueOf(idRel) );
		eCtx.setAttribute("ROL_CTX", String.valueOf(idRolCtx) );
		eCtx.setAttribute("ROL_CURR", String.valueOf(idRolCurr) );
		eCtx.setAttribute("N_MIN", String.valueOf(nMin) );
		eCtx.setAttribute("N_MAX", String.valueOf(nMax) );
		eCtx.setAttribute("CHILD_IS_SUP", (childIsSup ? "TRUE":"FALSE") );
		Iterator iX= getFixList();
		while( iX.hasNext() ){
                    fixProperties fp= (fixProperties)iX.next();
                    Element eFix= new Element("FIX");
                    eFix.setAttribute("NODE", fp.filterNode );
                    eFix.setAttribute("OPTIONAL", fp.optional ? "TRUE":"FALSE");
                    if( fp.ctxFix!=0 )
                        eFix.setAttribute("CTX", String.valueOf( fp.ctxFix ));
                    if( fp.refNodeFix!=0 )
                        eFix.setAttribute("REF_SOURCE", String.valueOf( fp.refNodeFix ));
                    if( fp.incrustar!=0 )
                        eFix.setAttribute("INCRUSTAR", String.valueOf( fp.incrustar ));
                    if( fp.detail!=null ){
                        Iterator iAva=fp.detail.getChildren().iterator();
                        while( iAva.hasNext() ){
                            Element ava=(Element)iAva.next();
                            eFix.addContent( (Element)ava.clone());
                        }
                    }
                    eCtx.addContent( eFix );
		}
		for( int r=0;r<userRolList.size();r++){
			Integer userRol= (Integer)userRolList.get(r);
			if( !userRolEnable( userRol ) ) continue;
			userEnable=true;

			Element rolProp= new Element("ROL_PROPERTIES");
			eCtx.addContent( rolProp );
			Integer idDom= getDom( userRol.intValue());
			rolProp.setAttribute("ID_DOMINIO", idDom.toString() );
			rolProp.setAttribute("ACCESO", getAcceso(userRol).toString() );
			rolProp.setAttribute("ROL", userRol.toString() );
			if( getDetail(userRol)!=null ){
				Iterator iDetRol=getDetail(userRol).getChildren().iterator();
				while( iDetRol.hasNext() ){
					Element child=(Element)iDetRol.next();
					rolProp.addContent((Element)child.clone());
				}
			}
			ArrayList lRpt=getReport( userRol );
			if( lRpt!=null && lRpt.size()>0 ){
				Element eRpt= new Element("REPORTS");
				rolProp.addContent( eRpt );
				for( int g=0; g<lRpt.size(); g++ ){
					Element rptItem= new Element("ITEM");
					eRpt.addContent( rptItem );
					rptItem.setAttribute( "OID", (String)lRpt.get(g) );
				}
			}
		}

		//if( userEnable || id==helperConstant.CTX_TASK ){
                //Lo deshabilito porque ciertos ctx, aun sin acceso, son necesarios por metadata para los fix
			ArrayList areas= getAreasFunc();
			for( int a=0; a<areas.size(); a++ ){
				Element area= new Element("AREAF");
				eCtx.addContent( area );
				area.setAttribute("ID", areas.get(a).toString() );
			}
			return eCtx;
		}else
			return null;
	}

   public void fixAVAs( metaData md, Element eFilter ){
	if( idFilter==0 ) return;
	//System.out.println("DOC1:"+id);
	Iterator iF= getFixList();
	while( iF.hasNext() ){
		fixProperties fp= (fixProperties)iF.next();
		//if( fp.optional ) continue;
		//System.out.println("DOC2:"+fp.filterNode);
                //jdomParser.print("FIXAVASFILTER",eFilter);
		Element nodAfec=jdomParser.findElementByAt(	eFilter,
								"FILTER",
								"OID",
				   				fp.filterNode,
								true,
			   					true);
                if( nodAfec==null )
                    nodAfec=jdomParser.findElementByAt(eFilter,
                            "FILTER",
                            "OID_FUSIONADO",
                            fp.filterNode,
                            true,
                            true);

		if( fp.detail!=null ){
			Iterator iAva=fp.detail.getChildren("AVA").iterator();
			while( iAva.hasNext() ){
				Element ava=(Element)iAva.next();
                                //jdomParser.print("AVA",ava);
                                Element old= jdomParser.findElementByAt(nodAfec,
                                        "AVA",
                                        "TA_POS",
                                        ava.getAttributeValue("TA_POS"),
                                        false);
                                //jdomParser.print("OLD",old);
                                if(old==null)
                                    nodAfec.addContent((Element)ava.clone());
			}
                        //jdomParser.print("FILTERPOST",eFilter);
		}
		if( fp.incrustar!=0 ){
			Element filInc= (Element)md.getFilter( new Integer( fp.incrustar ) ).clone();
			Element newChild= new Element("FILTER");
			int maxRef= hallaMaxRef( eFilter );
			incremtaRefs( maxRef+1, filInc );
			nodAfec.addContent( newChild );
			fusionControl.incrustaFilter( md, newChild, filInc );
		}
    	}
   }

   private int hallaMaxRef( Element eFilter ){
	int max= Integer.parseInt(eFilter.getAttributeValue("REF"));
	Iterator itr= eFilter.getChildren("FILTER").iterator();
	while(itr.hasNext()){
		Element child=(Element)itr.next();
		max= Math.max( max, hallaMaxRef( child ));
	}
	return max;
   }

   private void incremtaRefs( int inc, Element eFilter ){
	int ref= Integer.parseInt(eFilter.getAttributeValue("REF"));
	eFilter.setAttribute("REF", String.valueOf( ref+inc ));
	Iterator itr= eFilter.getChildren("FILTER").iterator();
	while(itr.hasNext()){
		Element child=(Element)itr.next();
		incremtaRefs( inc, child );
	}
   }
}
*/