package dynagent.knowledge.old;
/*
package dynagent.knowledge;

import org.jdom.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Comparator;

import dynagent.ejb.*;
import dynagent.knowledge.instance.*;

public class metaData{

	HashMap m_mapEnum= new HashMap();
	public HashMap m_areasFuncionales= new HashMap();
	HashMap m_TO_labels= new HashMap();
	public HashMap m_GRUPOS_labels= new HashMap();
	public HashMap m_AT_labels= new HashMap();
	HashMap m_mapTAPOS_TM= new HashMap();
	HashMap m_herencias= new HashMap();
	HashMap m_herenciasDom= new HashMap();
	HashMap m_categoriasRel= new HashMap();
	HashMap m_specializedTree= new HashMap();
	HashMap m_filters= new HashMap();
	HashMap m_dominios= new HashMap();
	HashMap m_AtCondicionantesFuncionales= new HashMap();
	public HashMap m_indices= new HashMap();
	public HashMap m_metaTOs= new HashMap();
	public HashMap m_reports= new HashMap();
	HashMap m_actions= new HashMap();
	public HashMap m_contextos= new HashMap();
	public HashMap m_process= new HashMap();
	public HashMap m_tasks= new HashMap();
	public HashMap m_trans= new HashMap();
	public HashMap m_tranDef= new HashMap();
	public HashMap m_taskFix= new HashMap();
	public HashMap m_specializedTreeLevel= new HashMap();
	public HashMap m_formOrderPkRel=new HashMap(),m_formOrderPkAt=new HashMap();
	public HashMap m_formOrderToRel=new HashMap(),m_formOrderToAt=new HashMap(),m_formOrderToPk=new HashMap();
	public HashMap m_userRol= new HashMap();

	int m_emp=0;
	int m_virtualDomIndex=2000;
	int m_virtualTOIndex=70;
	int m_virtualFilterIndex=2000;
	int m_virtualContextIndex=2000;

	fusionControl m_fusionControl;

	public metaData( int emp ){
		m_emp= emp;
		Element defD=new Element("DOMINIO");
		defD.setAttribute("ID",String.valueOf(helperConstant.DOM_DEFAULT));
		defD.setAttribute("REF","0");
		Element root=new Element("PROPERTY_LIST");
		defD.addContent(root);
		Element propRdn= new Element("ATRIBUTO");
		propRdn.setAttribute("NULLABLE","FALSE");
		propRdn.setAttribute("TA_POS",String.valueOf(helperConstant.TAPOS_RDN));
		propRdn.setAttribute("ACCESS","READ;WRITE");
		root.addContent(propRdn);
		addDominio( defD, helperConstant.DOM_DEFAULT );
		m_fusionControl= new fusionControl( this );
	}

	
	*//**
	 *Añado constructor sin argumentos
	 *//*
	public metaData() {
		// TODO Auto-generated constructor stub
	}

	public int getBusiness(){ return m_emp; }

	private void restoreFusion( Integer idCtx ) throws SystemException{
		//getFusionControl().restoreFusion( idCtx );
	}

	public void addDinReport( String oid, String name, int filter, String plantilla ){
		m_reports.put( oid, new reportType( oid, name, filter, plantilla ) );
	}

	public reportType getDinReport( String oid ){
		return (reportType)m_reports.get(oid);
	}

	public void addUserRol( String login, int rol ){
		ArrayList lista= getUserRol( login );
		if( lista==null ){
			lista= new ArrayList();
			m_userRol.put( login, lista );
		}
		Integer iRol= new Integer( rol );
		if( lista.indexOf( iRol )==-1 )
			lista.add( iRol );
	}

	public ArrayList getUserRol( String login ){
		return (ArrayList)m_userRol.get( login );
	}

	public boolean userPlayRol( String login, Integer rol ){
		return ((ArrayList)m_userRol.get( login )).indexOf( rol )!=-1;
	}

	public Set getUserRolSet(){
		return m_userRol.keySet();
	}

	public fusionControl getFusionControl(){
		return m_fusionControl;
	}

	public Set getContextosSet(){
		return m_contextos.keySet();
	}

	public void addFormOrder( Integer idContainer, Integer idElement, boolean isTO_notPk, int tipo, int order ){
		if( isTO_notPk ){
			if( tipo==ordenadorAts.AT_ELEM ){
				Iterator itr=getSpecializedSet( idContainer ).iterator();
				while( itr.hasNext() ){
					Integer idSpec=(Integer)itr.next();
					if( idSpec.equals( idContainer ) ) continue;
					addFormOrder( idSpec, idElement, true, ordenadorAts.AT_ELEM, order );
				}
				subAddFormOrder( m_formOrderToAt,idContainer,idElement,order );
			}
			if( tipo==ordenadorAts.REL_ELEM )
				subAddFormOrder( m_formOrderToRel,idContainer,idElement,order );

			if( tipo==ordenadorAts.GROUP_ELEM )
				subAddFormOrder( m_formOrderToPk,idContainer,idElement,order );
		}else{
			if( tipo==ordenadorAts.AT_ELEM )
				subAddFormOrder( m_formOrderPkAt,idContainer,idElement,order );
			if( tipo==ordenadorAts.REL_ELEM )
				subAddFormOrder( m_formOrderPkRel,idContainer,idElement,order );
		}
	}

	private void subAddFormOrder( HashMap mapa, Integer idContainer, Integer idElement,int order ){
		HashMap list= (HashMap)mapa.get( idContainer );
		if( list==null ){
			list=new HashMap();
			mapa.put( idContainer, list );
		}
		list.put( idElement, new Integer( order ) );
	}

	private int subGetFormOrder( HashMap mapa, Integer idContainer, Integer idElement ){
		HashMap list= (HashMap)mapa.get( idContainer );
		if( list==null )
			return 0;

		Integer order=(Integer)list.get( idElement );
		if( order==null ) return 0;
		return order.intValue();
	}

	public int getFormOrder( Integer idContainer, Integer idElement, boolean isTO_notPk, int tipo ){
		if( isTO_notPk ){
			if( tipo==ordenadorAts.AT_ELEM )
				return subGetFormOrder( m_formOrderToAt,idContainer,idElement );
			if( tipo==ordenadorAts.REL_ELEM )
				return subGetFormOrder( m_formOrderToRel,idContainer,idElement );

			if( tipo==ordenadorAts.GROUP_ELEM )
				return subGetFormOrder( m_formOrderToPk,idContainer,idElement );

		}else{
			if( tipo==ordenadorAts.AT_ELEM )
				return subGetFormOrder( m_formOrderPkAt,idContainer,idElement );
			if( tipo==ordenadorAts.REL_ELEM )
				return subGetFormOrder( m_formOrderPkRel,idContainer,idElement );
		}
		return 0;
	}

	public void addTaskContextFix( int pro, int task, int tran, int ctx, boolean taskObjectCtx ){
		//si no es taskObjCtx es un filter Fix
		Integer iPro= new Integer( pro );
		HashMap mTask=null;
		if( !m_taskFix.containsKey( iPro ) ){
			mTask= new HashMap();
			m_taskFix.put( iPro, mTask );
		}else
			mTask= (HashMap)m_taskFix.get( iPro );

		Integer iTask= new Integer( task );
		HashMap mTran=null;
		if( !mTask.containsKey( iTask ) ){
			mTran= new HashMap();
			mTask.put( iTask, mTran );
		}else
			mTran= (HashMap)mTask.get( iTask );

		Integer iTran= new Integer( tran );
		ArrayList mCtx=null;
		if( !mTran.containsKey( iTran ) ){
			mCtx= new ArrayList();
			mTran.put( iTran, mCtx );
		}else
			mCtx= (ArrayList)mTran.get( iTran );

		Integer iCtx= new Integer( ctx );
		if( !mCtx.contains( iCtx ) )
			mCtx.add( iCtx );
	}

	public ArrayList getSpecContexts(  Integer userRol, int ctxSup ){
		Contexto ccSup= getContext( new Integer(ctxSup));
		ArrayList res=new ArrayList();
		Iterator itr= m_contextos.keySet().iterator();
		while( itr.hasNext() ){
			Integer idCtx= (Integer)itr.next();
			Contexto ccSpec= getContext( idCtx );
			if( ccSup.action>=0 && ccSpec.action!=ccSup.action ) continue;
			if( ccSup.toRoot>0 && !isSpecializedFrom( ccSpec.toRoot, ccSup.toRoot )  ) continue;
			if( ccSup.toRoot <=0 && ccSpec.toRoot>0 ) continue;
			if( !ccSup.userRolEnable( userRol ) ) continue;

			if( !isSpecializedFrom( ccSpec.to, ccSup.to ) ) continue;

			res.add( idCtx );
		}
		return res;
	}

  public boolean ctxIsSpecializedFrom( Integer idCcSpec, Integer idCcSup ){
	Contexto ccSpec=getContext( idCcSpec );
	Contexto ccSup=getContext( idCcSup );
        System.out.println("ctxIsSpecializedFrom "+idCcSpec+","+ idCcSup);
	return 	ccSpec.idRel==ccSup.idRel &&
		ccSpec.idRolCtx==ccSup.idRolCtx &&
		ccSpec.idRolCurr==ccSup.idRolCurr &&
		ccSpec.childIsSup==ccSup.childIsSup &&
		( 	ccSpec.toRoot==0 && ccSup.toRoot==0 ||
			ccSpec.toRoot!=0 && ccSup.toRoot!=0 && isSpecializedFrom( ccSpec.toRoot, ccSup.toRoot ) ) &&
		isSpecializedFrom( ccSpec.to, ccSup.to );

  }

  public boolean domIsSpecializedFrom( Integer idDomSpec, Integer idDomSup ){
	if( idDomSpec.equals( idDomSup ) ) return true;
	if( !m_herenciasDom.containsKey( idDomSpec ) ) return false;
	HashMap padres=(HashMap)m_herenciasDom.get( idDomSpec );
	return padres.containsKey( idDomSup );

  }

  public boolean hallaRelacion( Element res, int toCurrent, int toContext){
	if( hallaRelacionDirecta( res, toCurrent, toContext ) )
		return true;
	Iterator itr= getSuperiorsSet( new Integer( toContext ) ).iterator();
	while( itr.hasNext() ){
		Integer toCtxSup=(Integer)itr.next();
		if( hallaRelacionDirecta( res, toCurrent, toCtxSup.intValue() ) )
			return true;
	}
	return false;
  }

  private boolean hallaRelacionDirecta( Element res, int toCurrent, int toContext ){
	Integer toCtx=new Integer( toContext );
	Element eTOctx= getMetaTO( toCtx );
	Iterator iRel=eTOctx.getChild("RELACIONES").getChildren("RELACION").iterator();
	while( iRel.hasNext() ){
		Element rel=(Element)iRel.next();
		Iterator iRol=rel.getChildren("ROL").iterator();
		while( iRol.hasNext() ){
			Element rol=(Element)iRol.next();
			Integer toCurrRel= new Integer( rol.getAttributeValue("TO") );
			if( isSpecializedFrom( toCurrent, toCurrRel.intValue() ) ){
				res.setAttribute("ID_REL", rel.getAttributeValue("ID_REL") );
				res.setAttribute("ROL_CONTEXT", rel.getAttributeValue("ID_ROL") );
				res.setAttribute("ROL_CURRENT", rol.getAttributeValue("ID_ROL") );
				boolean childIsSup= rol.getAttributeValue("SUPERIOR").equals("TRUE");
				res.setAttribute("ROL_SUP", ( childIsSup ? "CHILD":"CURRENT") );
				return true;
			}
		}
	}
	return false;
  }

  public Contexto getRootContext( Integer idCtxSpec ){
	Contexto ctxSpec= getContext( idCtxSpec );
	Iterator itr= m_contextos.keySet().iterator();
	while( itr.hasNext() ){
		Integer idCtx= (Integer)itr.next();
		Contexto ctx= getContext( idCtx );
                if(ctxSpec.id!=ctx.id &&
                   ctxSpec.action==ctx.action &&
                   !getFusionControl().contextIsFussioned(idCtx) &&
                   !(ctxSpec.toRoot > 0 && ctx.toRoot == 0) &&
                   !(ctxSpec.toRoot > 0 && !isSpecializedFrom(ctxSpec.toRoot, ctx.toRoot)) &&
                   isSpecializedFrom(ctxSpec.to, ctx.to))
                    return ctx;
        }
	return ctxSpec;
  }

  public Integer getParentContext( Integer idCtx ){
      Contexto ctx= getContext( idCtx );
      if( ctx.toRoot==0 ) return null;
      Iterator itr= m_contextos.keySet().iterator();
      while( itr.hasNext() ){
              Integer id= (Integer)itr.next();
              Contexto ctxParent= getContext( id );
              if( ctxParent.id!=ctx.id &&
                  ctxParent.action==ctx.action &&
                  isSpecializedFrom(  ctx.toRoot, ctxParent.to ) )
                  return id;
      }
      return null;
    }

    public int findAplicableContext( int action, int toRoot, int tipoEspec ){
         Iterator itr= m_contextos.keySet().iterator();
         Contexto ctxOptim=null;
         //System.out.println("FINDCONTEXT para "+action+","+toRoot+","+rel+","+rolCtx+","+rolCurr+","+tipoEspec);
         while( itr.hasNext() ){
                 Integer idCtx= (Integer)itr.next();
                 Contexto ctx= getContext( idCtx );
                 if( toRoot!=0 && !isSpecializedFrom( toRoot, ctx.toRoot ) ) continue;
                 if( ctx.action!=action ) continue;
                 if( !isSpecializedFrom( tipoEspec, ctx.to ) ) continue;
                 //System.out.println("OK2");
                 if( ctxOptim==null || isSpecializedFrom( ctx.to, ctxOptim.to ))
                         ctxOptim=ctx;
         }
         return ctxOptim==null ? -1:ctxOptim.id;
   }

   public int findAplicableContext( int action, int toRoot, int rel, int rolCtx, int rolCurr, int tipoEspec ){
	Iterator itr= m_contextos.keySet().iterator();
	Contexto ctxOptim=null;
	//System.out.println("FINDCONTEXT para "+action+","+toRoot+","+rel+","+rolCtx+","+rolCurr+","+tipoEspec);
	while( itr.hasNext() ){
		Integer idCtx= (Integer)itr.next();
		Contexto ctx= getContext( idCtx );
		if( toRoot!=0 && !isSpecializedFrom( toRoot, ctx.toRoot ) ) continue;
		if( ctx.action!=action ) continue;
		if( ctx.idRel!=0 &&	( 	rel!= ctx.idRel ||
						rolCtx!= ctx.idRolCtx ||
						rolCurr!= ctx.idRolCurr )) continue;

		if( !isSpecializedFrom( tipoEspec, ctx.to ) ) continue;
		//System.out.println("OK2");
		if( ctxOptim==null || isSpecializedFrom( ctx.to, ctxOptim.to ))
			ctxOptim=ctx;
	}
	return ctxOptim==null ? -1:ctxOptim.id;
   }

	public Element getTaskContextFixTree(){
		Element res= new Element("TASK_FILTER");
		Iterator itr= m_taskFix.keySet().iterator();
		while( itr.hasNext() ){
			Integer proId= (Integer)itr.next();
			Element ePro= new Element("PROCESS");
			res.addContent( ePro );
			ePro.setAttribute("ID", proId.toString() );
			HashMap task=(HashMap)m_taskFix.get(proId);
			Iterator iTask=task.keySet().iterator();
			while( iTask.hasNext() ){
				Integer taskId=(Integer)iTask.next();
				Element eTask= new Element("TASK");
				ePro.addContent( eTask );
				eTask.setAttribute("ID",taskId.toString() );
				HashMap tran=(HashMap)task.get(taskId);

				Iterator iTran=tran.keySet().iterator();
				while( iTran.hasNext() ){
					Integer tranId=(Integer)iTran.next();
					Element eTran= new Element("TRAN");
					eTask.addContent( eTran );
					eTran.setAttribute("ID", tranId.toString() );
					ArrayList ctxs=(ArrayList)tran.get(tranId);
					for( int c=0;c <ctxs.size(); c++){
						Integer ctxId=(Integer)ctxs.get( c );
						Element eCtx= new Element("CTX");
						eTran.addContent( eCtx );
						eCtx.setAttribute("ID", ctxId.toString() );
					}
				}
			}
		}
		return res;
	}

    public Element findMetaRol(int metaTO, int toSpec, int rel, int rolCtx, int rolChild)
		 {

	if( rel==helperConstant.REL_TRANSITIVA )	return null;
	Element metaRel= null,metaRolChild=null;

	Element eTO= getMetaTO( new Integer( metaTO ));
	Iterator iMRel= jdomParser.findElementsByAt(eTO.getChild("RELACIONES"),
                                                    "RELACION",
                                                    "ID_REL",
                                                    String.valueOf(rel),
                                                    "ID_ROL",
                                                    String.valueOf(rolCtx),
                                                    false).iterator();
	boolean metaRolChildFounded=false;
	while(iMRel.hasNext()){
		metaRel=(Element)iMRel.next();
       		metaRolChild= jdomParser.findElementByAt(	metaRel,
                               		                 	"ROL",
                                              		     	"ID_ROL",
       	                                       			String.valueOf(rolChild),
               	                            			false);
        	if(metaRolChild==null) continue;

			try{
			System.out.println("NOT FOUND:"+idto+","+rolChild+","+jdomParser.returnXML(metaRel));
			}catch(Exception e){;}
	      		return null;

		boolean instanciaEsMetaTipo=isSpecializedFrom( toSpec,
								Integer.parseInt(metaRolChild.getAttributeValue("TO")));


		if(	metaRolChild!=null && instanciaEsMetaTipo){
			metaRolChildFounded=true;
			break;
		}else
			System.out.println("NOT instanciaEsMeta");
	}

       	if(!metaRolChildFounded){
		try{
		System.out.println("NOT FOUND:"+idto+","+rolChild+","+jdomParser.returnXML(metaRels));
		}catch(Exception e){;}
      		JOptionPane.showMessageDialog(null,"FORM FTRY:+: Error, no existe metaRolChild");
     		return null;
	}
	return metaRolChild;
    }

	public void addTask( int id, String label, int stateStart, int taposAtState ){
		m_tasks.put( new Integer(id), new taskType( label, id, stateStart, taposAtState ) );
	}

	public void addCategoriaRel( int id, int cat ){
		m_categoriasRel.put( new Integer(id), new Integer( cat ) );
	}

	public int getCategoriaRel( int rel ){
		return ((Integer)m_categoriasRel.get( new Integer(rel) )).intValue();
	}
	public void addProcess( processType pt ){
		m_process.put( new Integer(pt.getId()), pt );
	}

	public void addTrans( int id, taskTransition td ){
		m_trans.put( new Integer(id), td );
	}

	public taskTransition getTrans( Integer id ){
		return (taskTransition)m_trans.get( id );
	}

	public Set getTransKeySet( ){
		return m_trans.keySet();
	}

	public String getAreaFuncionalLabel( String id ){
		return (String)m_areasFuncionales.get( id );
	}

	public processType getProcess(Integer id ){
		System.out.println("getProcess " + id);
		return (processType)m_process.get( id );
	}

        public Iterator getProcessIterator(){
                return m_process.values().iterator();
	}

        public Iterator getTaskIterator(){
                return m_tasks.values().iterator();
	}

	public taskTransition getActionTrans(Integer idAction ){
            System.out.println("getActionTrans. Action " + idAction);
            Iterator itr= m_trans.keySet().iterator();
            while( itr.hasNext() ){
                Integer id= (Integer)itr.next();
                taskTransition tt= getTrans( id );
                if( tt.userFlowStartAction==idAction.intValue() )
                    return tt;
            }
            return null;
	}

        public Iterator getTranIterator(){
            return m_trans.values().iterator();
        }

	public taskType getTask( Integer id ){
		return (taskType)m_tasks.get( id );
	}

	public void addActionData( int id, int type, int tran, Element detail, int operation ){
		action act= new action(id, type, tran, detail,operation);
		m_actions.put( new Integer(id), act );
	}

	public action getActionData( Integer id ){
		return (action)m_actions.get(id);
	}


	public Iterator getActionIterator(){
		return m_actions.keySet().iterator();
	}

	public calculo getFunciones(HashMap directValues, HashMap relationalData, HashMap filterMap)
		throws IllegalAccessException,InstantiationException{
		try{
		Class clase= Class.forName( "dynagent.ejb.funciones_" + m_emp );
		calculo fun= (calculo)clase.newInstance();
		fun.inicializar( relationalData, directValues, filterMap );
		return fun;
		}catch( ClassNotFoundException e){
			e.printStackTrace();
			System.out.println("METADATA EMP:" +m_emp+",clase funciones no encontrada");
		}
		return null;
	}

	public void addContext( Contexto ctx ){
		m_contextos.put( new Integer( ctx.id ), ctx );
	}

	public Contexto getContext( Integer id ){
		return (Contexto)m_contextos.get( id );
	}

	public void addContextRefCondicionados( Contexto ctx, int userRol ){
		//System.out.println("CONSTRUYENDO CONTEXTO :" + ctx.id );
		Integer idDom= ctx.getDom(userRol);
		if( !m_dominios.containsKey( idDom ) ){
			System.out.println("ERROR DATA MAP AñadIENDO CONTEXT, el dominio,empresa:"+idDom
						+","+m_emp + " no existe");
			return;
		}
		Element dom= getDominio( idDom );
		if( dom.getChild("POLIMORFISMO")==null ) return;
		Iterator itr= jdomParser.elementsWithAt( dom.getChild("POLIMORFISMO"), "REF", true ).iterator();
		while( itr.hasNext() ){
			Element subdom=(Element)itr.next();
			if( subdom.getChild("ITEM")!=null || subdom.getText()==null ||  subdom.getText().length()==0 )
				continue;//no se trata de un dominio funcional

			//solamente tendra en cuenta las relaciones estructurales

			Integer atRest= new Integer(subdom.getParent().getParent().getAttributeValue("TA_POS"));
			ArrayList atrefs= extractAtReferences( subdom.getText() );
			for( int i=0; i< atrefs.size(); i++){
				atReference atr= (atReference)atrefs.get( i );
				atr.pointerTapos= atRest.intValue();
				atr.idSubdomRef= Integer.parseInt( subdom.getAttributeValue("REF") );
				if( atr.idFilter==-1 ) continue;
				//me quedara con el filtro más restrictivo
				Integer idFilter= new Integer( atr.idFilterCascading==-1 ?
									atr.idFilter:atr.idFilterCascading);
				Element filter= getFilter( idFilter );
				if( filter==null ) continue; //debe estar inicializando
				//System.out.println("MD:filter:"+filter+","+idFilter);
				Integer toFilter= new Integer( filter.getAttributeValue("ID_TO") );
				Integer toNodeRef= toFilter;
				if( atr.idNodeRef>0 ){
					Element nodeRef= jdomParser.findElementByAt( 	filter,
											"FILTER",
											"REF",
											String.valueOf(atr.idNodeRef),
											true );
					toNodeRef= new Integer( nodeRef.getAttributeValue("ID_TO") );
				}
				if( isSpecializedFrom( toNodeRef.intValue(), helperConstant.TO_INDICE )){
					m_indices.put( idDom, atr );
					continue;
				}
				if( 	subdom.getAttributeValue("PROPAGABLE")==null ||
					subdom.getAttributeValue("PROPAGABLE").equals("FALSE") ) continue;
				//la linea siguiente solo aplica en verdaderos dominios con funciones de calculo
				//System.out.println("ATR:"+atr.toString());


				subSetContextRel( ctx, atr, userRol );

				//suponemos que entre 2 TOs solo hay una relación, asi que voy a buscarla
				// por un lado el TO del contexto, por otro el relacionado que debe ser un
				//especializado del TOFilter. POr otra parte el contexto se aplica a cualquier
				// objeto especializado de si mismo.

				// El FILTER RESTRINJE LOS OBJETOS A LOS QUE REFERENCIAMOS EN EL CALCULO DE UNA FUNCION
				// Y POR TANTO PUEDE RESTRINJIR POR ESPECIALIZAción, PERO EL CONTEXTO DEBE
				// SER APLICABLE A CUALQUIER OBJETO ESPECIALIZADO DEL TO CURR DEL CTX, POR TANTO
				// LA RELACION SIEMPRE DEBE PARTIR DEL TO CURR DEL CTX (que puede estar heredada)
				// PERO EL FILTER PUEDE SER UNA INDICAción SUPERIOR Y PUEDE SER QUE NO EXISTA RELACION
				// ENTRE DICHO TO SUP y EL TO del CTX, por Tanto tendra que ir buscando por los especializ
				// del filter.
				subAddContextRefCondicionados( subdom, toFilter, atr, ctx, userRol );
			}
		}
		//System.out.println("END CONSTRUYENDO CONTEXTO :" + ctx.id );
	}

	public boolean containsDomIndex( Integer iIdDOM ){
		return m_indices.containsKey( iIdDOM );
	}

	public atReference getDomIndex( Integer iIdDOM ){
		return (atReference)m_indices.get(iIdDOM);
	}

	private void subSetContextRel( Contexto ctx, atReference atr, int userRol ){
		Iterator itr=m_contextos.keySet().iterator();
		while( itr.hasNext() ){
			Integer id=(Integer)itr.next();
			Contexto subCtx= getContext( id );
			if( 	ctx.id!=id.intValue() &&
				subCtx.idFilter==atr.idFilter &&
				subCtx.tran==ctx.tran ){

				//System.out.println("LOCALIZADO SUB CTX:"+ctx.id+",->"+id+",FILTER:"+subCtx.idFilter+",TRAN:"+subCtx.tran);
				atr.idRel=subCtx.idRel;
				atr.idRolCtx= subCtx.idRolCtx;
				atr.idRolCurr= subCtx.idRolCurr;
				return;
			}
		}
	}

	private void subAddContextRefCondicionados( 	Element subdom,
							Integer toTree,
							atReference atr,
							Contexto ctx,
							int userRol ){
		subLoopAddContextRefCondicionados( subdom, toTree, atr, ctx, userRol );
		Set set=getSpecializedSet( toTree );
		//System.out.println("TOTREE:"+toTree+" "+(set==null));
		if( set==null ) return;
		Iterator iSpec= set.iterator();
		while( iSpec.hasNext() ){
			Integer toSpec= (Integer)iSpec.next();
			boolean exito= subLoopAddContextRefCondicionados( subdom, toSpec, atr, ctx, userRol );
			if( !exito )
				subAddContextRefCondicionados( subdom, toSpec, atr, ctx, userRol );
		}
	}

	public boolean TA_is_functionalCondition( Integer tapos ){
		return m_AtCondicionantesFuncionales.containsKey( tapos );
	}

	public ArrayList getCondicionantesFuncionales( Integer tapos ){
		return (ArrayList)m_AtCondicionantesFuncionales.get( tapos );
	}

	private boolean subLoopAddContextRefCondicionados(	Element subdom,
								Integer toTree,
								atReference atr,
								Contexto ctx,
								int userRol ){
		ArrayList ctxsCondnados= null;
		Element rol=findMetaRelByTO( 	new Integer(ctx.to),
						toTree,
						atr.idRel,
						atr.idRolCtx,
						atr.idRolCurr );
		//System.out.println("TOSpec:"+toTree);
		if( rol!=null ){
			//System.out.println("ROL NOT NULL");
			Element rel= rol.getParent();
			atr.ctxIsRolSup= rol.getAttributeValue("SUPERIOR").equals("FALSE");

			//int categ= Integer.parseInt( rel.getAttributeValue("CATEGORY") );
			//if( categ!=helperConstant.CAT_ESTRUCTURAL ) return true;
			//puede haber otras relaciones en otra rama del arbol de espec.
			//que si sean estructurales
		}
			Integer iTA= new Integer( atr.tapos );
			//System.out.println("MAPEANDO AT:" + iTA);
			if( !m_AtCondicionantesFuncionales.containsKey( iTA ) ){
				ctxsCondnados= new ArrayList();
				m_AtCondicionantesFuncionales.put( iTA, ctxsCondnados );
			}else{
				ctxsCondnados=(ArrayList)m_AtCondicionantesFuncionales.get(iTA);
			}
			int idCtx= ctxsCondnados.indexOf( ctx );
			Contexto currCtx=null;
			if( idCtx!= -1 )
				currCtx= (Contexto)ctxsCondnados.get( idCtx );
			else{
				currCtx=ctx;
				ctxsCondnados.add( ctx );
			}
			int ref= Integer.parseInt( subdom.getAttributeValue("REF") );
			par_atRef_domRef parRef= new par_atRef_domRef( atr, ref );
			currCtx.addRefDomCondicionado( userRol, parRef );
			return true;
			//break; no debo hacer break, puede haber más relaciones estructurales
			// ej. una relacion CPU-Disco y otra CPU-Kit Multimedia

		//return false;
	}

	private Element findMetaRelByTO( Integer toA, Integer toB, int idRel, int idRolCtx, int idRolCurr ){
		Element eTOa= getMetaTO( toA );
		//System.out.println("FIND:" +toA+","+toB+","+eTOa/*+","+eTOa.getChild("RELACIONES")+","+
		//			eTOa.getChild("RELACIONES").getChild("RELACION")*/

/*);
		Iterator iRel= eTOa.getChild("RELACIONES").getChildren("RELACION").iterator();
		while( iRel.hasNext() ){
			Element rel=(Element)iRel.next();
			if( Integer.parseInt(rel.getAttributeValue("ID_REL"))!= idRel ) continue;
			if( Integer.parseInt(rel.getAttributeValue("ID_ROL"))!= idRolCtx ) continue;
			Iterator iRol= rel.getChildren("ROL").iterator();
			while( iRol.hasNext() ){
				Element rol=(Element)iRol.next();
				if( Integer.parseInt(rol.getAttributeValue("ID_ROL"))!= idRolCurr ) continue;
				if( isSpecializedFrom( toB.intValue(), Integer.parseInt( rol.getAttributeValue("TO"))))
					return rol;
			}
		}
		return null;
	}

	public Set getHerenciasSet(){
		return m_herencias.keySet();
	}

	public Set getTOLabelsSet(){
		return m_TO_labels.keySet();
	}

	public Set getSuperiorsSet( Integer toCurren){
		HashMap supers= (HashMap)m_herencias.get( toCurren );
		return supers.keySet();
	}

	public Set getSpecializedSet( Integer toCurrent){
		if( !m_specializedTree.containsKey( toCurrent ) ) return new HashSet();
		HashMap specs= (HashMap)m_specializedTree.get( toCurrent );
		return specs.keySet();
	}

	public void buildSpecializedTreeLevel(){
		Iterator itr= getSpecializedSet().iterator();
		while(itr.hasNext()){
			Integer to=(Integer)itr.next();
			subBuildSpecializedTreeLevel( to, 0 );
		}
	}

	public void subBuildSpecializedTreeLevel( Integer to, int level ){
		HashMap specs=(HashMap)m_specializedTree.get( to );
		Integer currLevel= (Integer)m_specializedTreeLevel.get( to );
		if( currLevel==null )
			m_specializedTreeLevel.put( new Integer(level), to );
		else
			if( currLevel.intValue() >= level )
				return;
			else{
				currLevel= new Integer( level );
				m_specializedTreeLevel.put( currLevel, to );
			}

		Iterator iS=specs.keySet().iterator();
		while(iS.hasNext()){
			Integer toS=(Integer)iS.next();
			subBuildSpecializedTreeLevel( toS, level+1 );
		}
	}

	public Set getSpecializedSet(){
		return m_specializedTree.keySet();
	}

	public Set getFiltersSet(){
		return m_filters.keySet();
	}

	public Set getDominiosSet(){
		return m_dominios.keySet();
	}

	public Set getMiembrosSet(){
		return m_mapTAPOS_TM.keySet();
	}

	public void addEnumMap(int tapos, int val, String name){
		Integer iTapos= new Integer( tapos );
		HashMap values=null;
		if( !m_mapEnum.containsKey( iTapos ) ){
			values= new HashMap();
			m_mapEnum.put( iTapos, values );
		}else
			values= (HashMap)m_mapEnum.get( iTapos );

		values.put( new Integer( val ), name );
	}

	public Iterator getEnumSet( Integer tapos ){
		//System.out.println("METADATA,getEnumSet:"+tapos);
		if( tapos==null ){
			System.out.println("METADATA, tapos null");
			return null;
		}

		if( m_mapEnum==null ){
			System.out.println("METADATA, map null");
			return null;
		}

		if( m_mapEnum.get( tapos )==null ){
			System.out.println("METADATA, values null");
			return null;
		}

		HashMap taposMap=(HashMap)m_mapEnum.get( tapos );
                Object[] lista= taposMap.keySet().toArray();
                Arrays.sort(lista);
                return Arrays.asList(lista).iterator();
	}

	public void addTOLabel(int to, String label){
		m_TO_labels.put( new Integer( to ), label );
	}
	public String getTOLabel(Integer to){
		return (String)m_TO_labels.get( to );
	}

	public void addGroupLabel(int pk, String label){
		m_GRUPOS_labels.put( new Integer( pk ), label );
	}

	public String getGroupLabel(Integer pk){
		return (String)m_GRUPOS_labels.get( pk );
	}

	public void addATLabel(int at, String label){
		m_AT_labels.put( new Integer( at ), label );
	}

	public String getATLabel(Integer tapos){
		return (String)m_AT_labels.get( tapos );
	}

	public void addTMmap( int tapos, int tm ){
		m_mapTAPOS_TM.put( new Integer( tapos ), new Integer( tm ) );
	}

	public void addMetaTO( int idto, Element eTO ){
		Integer iTO= new Integer( idto );
		if( m_metaTOs.containsKey( iTO ) ) return;
		m_metaTOs.put( iTO, eTO );
	}

	public Element getMetaTO( Integer idto ){
		return (Element)m_metaTOs.get( idto );
	}

	public Element getOriginalMetaTO( Integer idto ){
		return getFusionControl().getOriginalTO( idto );
	}

	public String getEnumLabel( Integer tapos, int value){
		HashMap taposMap=(HashMap) m_mapEnum.get( tapos );
		return (String)taposMap.get( new Integer(value) );
	}

	public String getEnumLabel( Integer tapos, Integer value){
		HashMap taposMap=(HashMap) m_mapEnum.get( tapos );
		return (String)taposMap.get( value );
	}

	public Iterator getMapEnumIterator(){
		return m_mapEnum.keySet().iterator();
	}

	public int getID_TM( Integer tapos ){
		//System.out.println("GETIDTM para tapos:"+tapos);
		if( !m_mapTAPOS_TM.containsKey( tapos ) ) return 0;
		return ((Integer)m_mapTAPOS_TM.get( tapos )).intValue();
	}

	public void removeAt( Integer tapos ){
		m_mapEnum.remove( tapos );
		m_mapTAPOS_TM.remove( tapos );
		m_AT_labels.remove(tapos);
	}

	public void addFilter( Element filter, int idFilter ){
		Integer id= new Integer( idFilter );
		m_filters.put( id, filter );
	}

	public Element getFilter( Integer idFilter ){
		Element filter=(Element)m_filters.get( idFilter );
                filter= (Element)filter.clone();
                return filter;
	}

	public Element getFilter( Integer ctxId, Integer idFilter ){
		if( ctxId==null ) return getFilter( idFilter );
		Element newFilter= getFilter( idFilter );
		Contexto cc= getContext( ctxId );
		cc.fixAVAs( this, newFilter );
		return newFilter;
	}

        public boolean rolChildIsSuperior( 	int toRoot,
                                                int toRel,
                                                int idCtx ) throws SystemException{
                Integer id=new Integer(idCtx);
                Contexto ctx= getContext(id);
                Contexto ctxRoot= getRootContext(id);
                return rolChildIsSuperior(toRoot,toRel,ctxRoot.idRel,ctxRoot.idRolCurr,ctx.idRolCtx);
        }

	public boolean rolChildIsSuperior( 	int toRoot,
						int toRel,
						int idRel,
						int rolChild,
						int rolCtx ) throws SystemException{
		// Pedira el original por si una fusion elimino esta relación, pero el cliente lo pide en base a los datos orgininale
		// como pasa en ObjectAssistant

		Element eTORoot= getOriginalMetaTO( new Integer( toRoot ) );
		Element metaRel= null,metaRolChild=null;

		if( idRel==helperConstant.REL_TRANSITIVA ) return false;

		Iterator iMRel= jdomParser.findElementsByAt(	eTORoot.getChild("RELACIONES"),
                                               			"RELACION",
	                                                        "ID_REL",
        	                                                String.valueOf(idRel),
                	                                        "ID_ROL",
                        	                                String.valueOf( rolCtx ),
                                	                        false).iterator();
		boolean metaRolChildFounded=false;
		while(iMRel.hasNext()){
			metaRel=(Element)iMRel.next();
	               	metaRolChild= jdomParser.findElementByAt(	metaRel,
        	                       		                 	"ROL",
               		                               		     	"ID_ROL",
                       		                       			String.valueOf(rolChild),
                               		            			false);
			if(metaRolChild==null) continue;
			boolean instanciaEsMetaTipo=isSpecializedFrom( 	toRel, Integer.parseInt(metaRolChild.getAttributeValue("TO")));
			if(	metaRolChild!=null && instanciaEsMetaTipo){
				metaRolChildFounded=true;
				break;
			}
		}

       		if(!metaRolChildFounded){
			System.out.println("Error, no existe metaRolChild:TO ROOT "+ +toRoot+",TO REL "+
							toRel+",idRel:"+idRel+",rolChild:"+rolChild+",ROL CTX "+rolCtx);
			try{
			System.out.println(jdomParser.returnXML( eTORoot ));
			}catch(Exception e){;}
			throw new SystemException(SystemException.ERROR_DATOS,
						"MetaData.rolChildIsSuperior, no existe metarol");
    		}
		return metaRolChild.getAttributeValue("SUPERIOR").equals("TRUE");
	}

	public void addDominio( Element dom, int idDom ){
		Integer id= new Integer( idDom );
		m_dominios.put( id, dom );
	}

	public void addVirtualDominio( Element dom ){
		m_virtualDomIndex++;
		Integer id= new Integer( m_virtualDomIndex );
		dom.setAttribute("ID",id.toString());
		m_dominios.put( id, dom );
	}

	public Element getDominio( Integer idDom ){
		if( idDom==null ) return null;
		return (Element)m_dominios.get( idDom );
	}

	public void addRecursiveSuperiorMap( int to, int toSuper ){
		Integer iTO= new Integer( to );
		HashMap supers=null;
		if( !m_herencias.containsKey( iTO ) ){
			supers= new HashMap();
			m_herencias.put( iTO, supers );
		}else
			supers=(HashMap)m_herencias.get( iTO );

		Iterator itr= ((HashMap)m_herencias.get( new Integer(toSuper) )).keySet().iterator();
		while( itr.hasNext() ){
			Integer toSs= (Integer)itr.next();
			supers.put( toSs, null );
		}
		supers.put( new Integer( toSuper ), null );
		supers.put( iTO, null );
	}

	public void addSuperiorDomMap( int dom, int domSuper ){
		Integer iDOM= new Integer( dom );
		HashMap supers=null;
		if( !m_herenciasDom.containsKey( iDOM ) ){
			supers= new HashMap();
			m_herenciasDom.put( iDOM, supers );
		}else
			supers=(HashMap)m_herenciasDom.get( iDOM );
		supers.put( new Integer( domSuper ), null );
	}

	public void buildTreeDomMap(){
		HashMap partida= (HashMap)m_herenciasDom.clone();
		m_herenciasDom.clear();
		Iterator itr= partida.keySet().iterator();
	iterDom:
		while( itr.hasNext() ){
			Integer idDom=(Integer)itr.next();
			//voy a buscar los especializados del arbol que no tienen hijos
			Iterator itr2= partida.keySet().iterator();
			while( itr2.hasNext() ){
				Integer idDom2=(Integer)itr2.next();
				if( idDom2.equals(idDom)) continue;
				HashMap sup=(HashMap)partida.get( idDom2 );
				if( sup.containsKey(idDom) )
					continue iterDom;
			}
			updateDomPadres( partida, idDom );
		}
	}

	private ArrayList updateDomPadres( HashMap spec, Integer idDom ){
		ArrayList lista= new ArrayList();
		if( !spec.containsKey( idDom ) ) return lista;	//en este caso se trataria de un dom independiente
								// que ni tiene padres ni hijos
		Iterator iDirect= ((HashMap)spec.get( idDom )).keySet().iterator();
		while( iDirect.hasNext() ){
			Integer idSup=(Integer)iDirect.next();
			lista.add( idSup );
			addSuperiorDomMap( idDom.intValue(), idSup.intValue() );
			ArrayList padres= updateDomPadres( spec, idSup );
			for( int i=0; i<padres.size();i++){
				lista.add( padres.get(i) );
				addSuperiorDomMap( idDom.intValue(), ((Integer)padres.get(i)).intValue() );
			}
		}
		return lista;
	}

	public void addSuperiorMap( int to, int toSuper ){
		Integer iTO= new Integer( to );
		HashMap supers=null;
		if( !m_herencias.containsKey( iTO ) ){
			supers= new HashMap();
			m_herencias.put( iTO, supers );
		}else
			supers=(HashMap)m_herencias.get( iTO );
		supers.put( new Integer( toSuper ), null );
	}

	public void addDirectSpecializedMap( int to, int toSpec ){
		Integer iTO= new Integer( to );
		Integer iTOSpec= new Integer( toSpec );
		HashMap specs=null;
		if( !m_specializedTree.containsKey( iTO ) ){
			specs= new HashMap();
			m_specializedTree.put( iTO, specs );
		}else
			specs=(HashMap)m_specializedTree.get( iTO );

		if( specs.containsKey( iTOSpec ) ) return;

		specs.put( iTOSpec, null );
	}

	public 	boolean isSpecializedFrom( int currTo, int superTo ){
		if( currTo== superTo ) return true;
		Integer iCurrTO= new Integer( currTo );
		Integer iSupTO= new Integer( superTo );
		return isSpecializedFrom( iCurrTO, iSupTO );
	}

	public 	boolean isSpecializedFrom( Integer currTo, Integer superTo ){
		if( currTo.equals( superTo ) ) return true;
		if( !m_herencias.containsKey( currTo ) )
			return false;
		HashMap supers= (HashMap)m_herencias.get( currTo );

		return supers.containsKey( superTo );
	}

	public 	boolean isSuperFrom( int currTo, int specialTo ){
		if( currTo== specialTo ) return true;
		Integer iSpecTO= new Integer( specialTo );
		if( !m_herencias.containsKey( iSpecTO ) )
			return false;
		HashMap supers= (HashMap)m_herencias.get( iSpecTO );
		Integer iCurrTO= new Integer( currTo );

		return supers.containsKey( iCurrTO );
	}

	public int findEmbedAtInRoot( Element fRoot, int tapos, int refEmbed, Element fEM ){
            if (fRoot.getAttributeValue("ID").equals(fEM.getAttributeValue("ID")))
                return refEmbed;
            Element embedIniPathAt = refEmbed == 0 ? fEM :
                                     jdomParser.findElementByAt(fEM,
                    "FILTER",
                    "REF",
                    String.valueOf(refEmbed),
                    true,
                    true);

            if (embedIniPathAt == null) {
                System.out.println("FIND EMBED, node target null");
                jdomParser.print("ROOT ",fRoot);
                jdomParser.print("EMBED ",fEM);
                return -1;
            }
            //System.out.println("FOUND EMBED, node target");

            ArrayList atPath = new ArrayList();
            Element pointerPath = embedIniPathAt;
            while (true) {
                if (pointerPath.getAttributeValue("REF")==null )
                    break;
                atPath.add(pointerPath);
                if (pointerPath.getAttributeValue("REF").equals(fEM.getAttributeValue("REF")))
                    break;
                pointerPath = pointerPath.getParent();
            }
            //System.out.println("BUILD ARRAY:"+atPath.size());
            return subFindEmbedAtInRoot(fRoot, tapos, atPath);
        }

	private int subFindEmbedAtInRoot( Element nodeRoot, int tapos, ArrayList atPath ){
		//System.out.println("TESTING NODE ROOT:" + nodeRoot.getAttributeValue("REF"));
		int refRoot= checkEmbedPathInRoot( nodeRoot, tapos, atPath, atPath.size()-1 );
		//System.out.println("TESTING NODE ROOT RESUL:" + refRoot);
		if( refRoot!=-1 ) return refRoot;
		Iterator itr = nodeRoot.getChildren("FILTER").iterator();
		while( itr.hasNext() ){
			Element child= (Element)itr.next();
			int ref= subFindEmbedAtInRoot( child, tapos, atPath );
			if( ref!=-1) return ref;
		}
		return -1;
	}

	private int checkEmbedPathInRoot(Element testNodeRoot, int tapos, ArrayList embedAtPath, int pos ){

		//System.out.println("TESTING PATH pos:" + pos);
		Element pointerPath= (Element)embedAtPath.get(pos);
		int TOPath= Integer.parseInt( pointerPath.getAttributeValue("ID_TO"));
		int TORoot= Integer.parseInt( testNodeRoot.getAttributeValue("ID_TO"));

		//System.out.println("TESTING PATH TOS:" + TOPath+","+TORoot);
		if( !isSpecializedFrom( TORoot, TOPath ) ) return -1;
		//System.out.println("TESTING PATH TRUE");
		if( pos==0 ){
			//System.out.println("TESTING POS=0");
			int refRoot= Integer.parseInt( testNodeRoot.getAttributeValue("REF") );
			Element atRoot= jdomParser.findElementByAt( 	testNodeRoot,
									(refRoot==0 ? "SELECT":"VIRTUAL"),
									"TA_POS",
									String.valueOf(tapos),
									false );
			if( tapos==1 && refRoot==0 ) return 0;
			if( atRoot==null ) return -1;
			else return refRoot;
		}

		Iterator iRoot= testNodeRoot.getChildren("FILTER").iterator();
		while( iRoot.hasNext() ){
			Element childTestRoot=(Element)iRoot.next();
			int ref= checkEmbedPathInRoot( childTestRoot, tapos, embedAtPath, pos-1 );
			if( ref!=-1 ) return ref;
		}
		return -1;
	}


   public static atReference extractFirstAtReference( String field ){
       ArrayList lista= extractAtReferences("{"+field+"}");
       return (atReference)lista.get(0);
   }
   public static ArrayList extractAtReferences(String script){
	//System.out.println("ATREF:"+script);
	ArrayList res=new ArrayList();
	if(script== null || script.length()==0) return res;
	int end=-1;
	boolean cont= true;
	while(cont){
		atReference atr= new atReference();
		int openB= script.indexOf("{", end +1 );
		if(openB > end + 1 )
			res+= script.substring( end + 1, openB );
		if(openB == -1 ){
			//res+= script.substring( end + 1 );
			break;
		}
		int closeB= script.indexOf("}", end + 2);

		if(closeB<=end + 3 ){
			System.out.println("ERROR DE FORMATO EN AT SCRIPT " + script);
			System.exit(0);
		}
		String contenido= script.substring(openB+1, closeB);
		int pk= 0, rol=0, indexFilter=-1, indexCascadingFilter=-1;
		int separadorFilter= contenido.indexOf('@');
		if(separadorFilter > 0 ){
			String[] items= contenido.substring( 0, separadorFilter).split("[\\.\\:]" );
			if( !items[0].equals("0") ){
				atr.idFilter= Integer.parseInt(items[0]);
				if( 	contenido.indexOf( ":" ) >=0 &&
					contenido.indexOf( ":" )< separadorFilter ){

					indexCascadingFilter= Integer.parseInt(items[1]) ;
					atr.idFilterCascading= Integer.parseInt(items[1]);
				}

				if( indexCascadingFilter < 0 && items.length==2 )
					atr.idNodeRef= Integer.parseInt(items[1]);
				if( indexCascadingFilter >= 0 && items.length==3 )
					atr.idNodeRef= Integer.parseInt(items[2]);
			}
		}

		atr.tapos= Integer.parseInt(contenido.substring(separadorFilter+1));

		end= closeB ;
		if(!(script.length()> end + 1))	cont= false;
		res.add( atr );
	}
	return res;
   }

   public void setMoreRestrictive( Element filter, Element cascadingF ){
	int toF= Integer.parseInt( filter.getAttributeValue("ID_TO") );
	int toC= Integer.parseInt( cascadingF.getAttributeValue("ID_TO") );

	if( toF!=toC )
		filter.setAttribute("ID_TO", String.valueOf( toC ));//se supone que es más restrictivo

	Iterator iAvaC= cascadingF.getChildren("AVA").iterator();
	while( iAvaC.hasNext() ){
		Element avaC=(Element)iAvaC.next();
		Element avaF= jdomParser.findElementByAt( 	filter,
								"AVA",
								"TA_POS",
								avaC.getAttributeValue("TA_POS"),
								false );
		if( avaF== null){
			avaF= (Element)avaC.clone();
			filter.addContent( avaF );
		}
	}

	Iterator iC= cascadingF.getChildren("FILTER").iterator();
	while( iC.hasNext() ){
		Element childC=(Element)iC.next();
		Element childF= jdomParser.findElementByAt( 	filter,
								"FILTER",
								"ID_REL",
								childC.getAttributeValue("ID_REL"),
								"ROL_CONTEXT",
								childC.getAttributeValue("ROL_CONTEXT"),
								false );
		boolean exito= false;
		if( 	childF!=null &&
			childC.getAttributeValue("ROL_CURRENT").equals( filter.getAttributeValue("ROL_CURRENT"))){

			if( isSpecializedFrom( 	Integer.parseInt( childC.getAttributeValue("ID_TO")),
						Integer.parseInt( childF.getAttributeValue("ID_TO")) ) ){
				exito= true;
				setMoreRestrictive( childF, childC );
			}

		}
	}
   }
    public void  buildRelationalDataMap(HashMap filterMap, instance rels, Element filterRoot, Integer idObjRoot){
	if( rels==null ) return;
	Iterator itr= rels.getRelationIterator(false);
	while(itr.hasNext()){
		relation rel= (relation) itr.next();
		subBuildRelationalDataMap(	filterMap,
						rel,
						new Integer(rel.getIDO()),
						new Integer( rel.getType()),
						filterRoot,
						idObjRoot);
	}
    }

    public void  subBuildRelationalDataMap(	HashMap filterMap,
						relation rel,
						Integer idObj,
						Integer idTO,
						Element filterRoot,
						Integer idObjRoot){
	String taIDO= "0@"+ helperConstant.TAPOS_IDO ;
	Integer idFilterRoot= filterRoot==null ? null:new Integer( filterRoot.getAttributeValue("ID") );
	// Integer idFilter= rel.getIdFilter()!=0 ? new Integer(rel.getIdFilter()):null;
	//Integer relContext= rel.getContextID()!=0 ? new Integer(rel.getContextID()):null;
	Integer idFilterKey=idFilterRoot;
	if( idFilterRoot!=null )
		idFilterKey= idFilterRoot;
	else
		idFilterKey= idFilter;
	
	Integer idFilterKeyPreAdapt= idFilterKey;

	ArrayList objects=null;
	if( !filterMap.containsKey( idFilterKey ) ){
		objects= new ArrayList();
		filterMap.put( idFilterKey, objects );
	}else
		objects= (ArrayList)filterMap.get( idFilterKey );

	HashMap ats=null;
	Integer idObjKey= idObjRoot!=null ? idObjRoot:idObj;
	int indexO= -1;

	for( int i=0;i<objects.size();i++){
		HashMap tmpAts=(HashMap)objects.get(i);
		Integer tmpIdo=(Integer)tmpAts.get( taIDO );
		if( tmpIdo.equals( idObjKey ) ){
			indexO=i;
			break;
		}
	}

	if( indexO==-1 ){
		ats= new HashMap();
		objects.add( ats );
		ats.put( "0@"+ helperConstant.TAPOS_METATIPO , idTO);
		ats.put( "0@"+ helperConstant.TAPOS_FILTER_ROOT , idFilterKey);
		//System.out.println("PUTTING FROOT:"+helperConstant.TAPOS_FILTER_ROOT);
		ats.put( taIDO, idObjKey );
	}else
		ats= (HashMap)objects.get( indexO );

	Iterator iAva= rel.getAttIterator(false,false);
	while( iAva.hasNext()){
		attribute ava = (attribute)iAva.next();
		int refSub= ava.getVirtualREF();
		if( refSub== -1 ) continue;

		Integer tapos= new Integer(ava.getTapos());

		int tm= ava.getMemberType();
		if( tm== helperConstant.TM_TEXTO ||
                    tm== helperConstant.TM_MEMO ||
                    tm== helperConstant.TM_FECHA ||
                    tm== helperConstant.TM_FECHAHORA) continue;

		//refSub= getFusionControl().getRefFromOriginalRootFilter( refSub, tapos, idFilterKeyPreAdapt );

		 String key= null;
		if( idFilterRoot != null && idFilter!=null ){
			//System.out.println("IN MD:"+tapos+","+idFilterKey+","+idObjKey);
			Element filter= getFilter( relContext, idFilter );
			int refRoot= findEmbedAtInRoot( filterRoot,
							tapos.intValue(),
							refSub,
							filter);
			key= String.valueOf( refRoot ) + "@" + tapos;
		}else
			key=  String.valueOf(refSub) + "@" + tapos;
			//System.out.println("REL, PUTKEY,val"+key+","+ava.getAttributeValue("VALUE"));
                if(ava.getValue() instanceof ArrayList)
                    continue;
                else{
                    if( ava.getValue() instanceof Double )
                        ats.put(key, ava.getValue());
                    if( ava.getValue() instanceof Integer )
                        ats.put(key, new Double(((Integer)ava.getValue()).intValue()));
                }
	}
   }

   public void buildPartsDataMap(instance ins, HashMap filterMap, Element filterRootToAdapt){
	Integer idFilterRootToAdapt= filterRootToAdapt==null ? null:new Integer( filterRootToAdapt.getAttributeValue("ID") );
	Iterator itr= ins.getEstructIterator(false);
	String taIDO= "0@"+ helperConstant.TAPOS_IDO ;
	while(itr.hasNext()){
		relation rel= (relation) itr.next();
		Integer idSubFilter= new Integer( rel.getIdFilter());
		Integer idSubContext= new Integer( rel.getContextID());
		Element subFilter= getFilter( idSubContext, idSubFilter );

		Integer idFilter= idFilterRootToAdapt!=null ?	idFilterRootToAdapt:idSubFilter;
		Element filter= filterRootToAdapt!=null ? filterRootToAdapt:subFilter;

		Integer idFilterKeyPreAdapt= idFilter;

		Integer idObj= new Integer( rel.getIDO());
		ArrayList objects=null;
		if( !filterMap.containsKey( idFilter ) ){
			objects= new ArrayList();
			filterMap.put( idFilter, objects );
		}else
			objects= (ArrayList)filterMap.get( idFilter );

		HashMap ats=null;
		int indexO=-1;
		for( int i=0;i<objects.size();i++){
			HashMap tmpAts=(HashMap)objects.get(i);
			Integer tmpIdo=(Integer)tmpAts.get( taIDO );
			if( tmpIdo.equals( idObj ) ){
				indexO=i;
				break;
			}
		}

		if( indexO==-1 ){
			ats= new HashMap();
			objects.add( ats );
			ats.put( "0@"+ helperConstant.TAPOS_METATIPO ,
                                 new Integer( rel.getType()));
			ats.put( "0@"+helperConstant.TAPOS_FILTER_ROOT , idFilter );
			System.out.println("PART PUTTING FROOT:"+helperConstant.TAPOS_FILTER_ROOT);
			ats.put( taIDO, idObj );
		}else
			ats= (HashMap)objects.get( indexO );
		Iterator iAva= rel.getDirectAttIterator();
		while( iAva.hasNext()){
			attribute ava = (attribute)iAva.next();
                        int tm=ava.getMemberType();
                        if( (tm== helperConstant.TM_ENUMERADO ||
                            tm== helperConstant.TM_ENTERO ||
                            tm== helperConstant.TM_REAL) &&
                            ava.getValue() instanceof ArrayList) //;para multival y espacio para rango
                            continue;
			if( tm== helperConstant.TM_TEXTO ||
                            tm== helperConstant.TM_MEMO ||
                            tm== helperConstant.TM_FECHA ||
                            tm== helperConstant.TM_FECHAHORA ) continue;

			int ref=0;
			//ref= getFusionControl().getRefFromOriginalRootFilter( ref, tapos, idFilterKeyPreAdapt );

			if( idFilterRootToAdapt!=null )
				ref= findEmbedAtInRoot( filter,ava.getTapos(),0,subFilter);


			String key= String.valueOf(ref) + "@" + ava.getTapos();

			System.out.println("PART, PUTKEY,val"+key+","+ava.getValue());
                        if( ava.getValue() instanceof Double )
                            ats.put( key, ava.getValue() );
                        if( ava.getValue() instanceof Integer )
                            ats.put( key, new Double( ((Integer)ava.getValue()).intValue() ));
		}
                if( rel.hasInstanceData(false)){
                    buildRelationalDataMap(filterMap, rel.getRelationInstance(), filter, idObj);
                    buildPartsDataMap(rel.getRelationInstance(), filterMap, filter); //los niveles inferiores se contextuan bajo el mismo filter
                }
	}
    }
   public Object clone(){
       metaData res= new metaData(m_emp);
       res.m_mapEnum= (HashMap)cloneObject(m_mapEnum);
       res.m_areasFuncionales= (HashMap)cloneObject(m_areasFuncionales);
       res.m_TO_labels= (HashMap)cloneObject(m_TO_labels);
       res.m_GRUPOS_labels= (HashMap)cloneObject(m_GRUPOS_labels);
       res.m_AT_labels=(HashMap)cloneObject(m_AT_labels);
       res.m_mapTAPOS_TM= (HashMap)cloneObject(m_mapTAPOS_TM);
       res.m_herencias= (HashMap)cloneObject(m_herencias);
       res.m_herenciasDom= (HashMap)cloneObject(m_herenciasDom);
       res.m_categoriasRel= (HashMap)cloneObject(m_categoriasRel);
       res.m_specializedTree= (HashMap)cloneObject(m_specializedTree);
       res.m_filters= (HashMap)cloneObject(m_filters);
       res.m_dominios= (HashMap)cloneObject(m_dominios);
       res.m_AtCondicionantesFuncionales= (HashMap)cloneObject(m_AtCondicionantesFuncionales);
       res.m_indices= (HashMap)cloneObject(m_indices);
       res.m_metaTOs= (HashMap)cloneObject(m_metaTOs);
       res.m_reports= (HashMap)cloneObject(m_reports);
       res.m_actions= (HashMap)cloneObject(m_actions);
       res.m_contextos= (HashMap)cloneObject(m_contextos);
       res.m_process= (HashMap)cloneObject(m_process);
       res.m_tasks= (HashMap)cloneObject(m_tasks);
       res.m_trans= (HashMap)cloneObject(m_trans);
       res.m_tranDef= (HashMap)cloneObject(m_tranDef);
       res.m_taskFix= (HashMap)cloneObject(m_taskFix);
       res.m_specializedTreeLevel= (HashMap)cloneObject(m_specializedTreeLevel);
       res.m_formOrderPkRel=(HashMap)cloneObject(m_formOrderPkRel);
       res.m_formOrderPkAt=(HashMap)cloneObject(m_formOrderPkAt);
       res.m_formOrderToRel=(HashMap)cloneObject(m_formOrderToRel);
       res.m_formOrderToAt=(HashMap)cloneObject(m_formOrderToAt);
       res.m_formOrderToPk=(HashMap)cloneObject(m_formOrderToPk);
       res.m_userRol= (HashMap)cloneObject(m_userRol);
       res.m_fusionControl= (fusionControl)m_fusionControl.clone(res);
       return res;
   }

   public static Object  cloneObject(Object obj){
       if( obj instanceof ArrayList )
           return cloneArray((ArrayList)obj);
       else
       if( obj instanceof HashMap )
           return cloneMap((HashMap)obj);
       else
       if( obj instanceof Element )
           return ((Element)obj).clone();
       else
       if( obj instanceof Integer )
           return obj;
       else
       if( obj instanceof String )
           return obj;
       else
       if( obj instanceof atReference )
           return ((atReference)obj).clone();
       else
       if( obj instanceof fixProperties )
           return ((fixProperties)obj).clone();
       else
       if(obj instanceof Contexto)
           return ((Contexto)obj).clone();
       else
       if( obj instanceof atReference )
           return ((atReference)obj).clone();
       else
       if( obj instanceof reportType )
           return ((reportType)obj).clone();
       else
       if( obj instanceof taskTransition )
           return ((taskTransition)obj).clone();
       else
       if( obj instanceof processType )
           return ((processType)obj).clone();
       else
       if( obj instanceof access )
           return ((access)obj).clone();
       else
       if( obj instanceof action )
           return ((action)obj).clone();
       else
       if( obj instanceof taskType)
           return ((taskType)obj).clone();
       //else
       //if( obj instanceof docDataModel)
       //    return ((docDataModel)obj).clone();
       else
       if( obj instanceof instance )
           return ((instance)obj).clone();
       else
       if( obj instanceof attribute )
           return ((attribute)obj).clone();
       //else
       if( obj instanceof relation )
           return ((relation)obj).clone();
       else{
           System.out.println("ERROR CLONE METADATA "+obj);
           return null;
       }
       else
    	   return null;
   }

   private static Object cloneMap( HashMap array ){
       HashMap res= new HashMap();
       Iterator itr= array.keySet().iterator();
       while(itr.hasNext()){
           Object id= itr.next();
           Object item=array.get(id);
           item= cloneObject(item);
           if( item==null )
               System.out.println("CLONEMAP "+id);
           res.put(cloneObject(id),item);
       }
       return res;
   }
   private static Object cloneArray( ArrayList array ){
       ArrayList res= new ArrayList();
       for(int i=0;i<array.size();i++){
           Object item=array.get(i);
           item= cloneObject(item);
           res.add(item);
       }
       return res;
   }
}


*/