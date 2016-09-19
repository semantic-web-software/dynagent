package dynagent.knowledge.old;
/*package dynagent.knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jdom.Element;

import dynagent.ejb.*;

public class scope extends Object{

   HashMap m_ctxChilds= new HashMap();
   metaData m_md;
   scope m_parent;
   int m_operation;
   Integer m_userRol;
   access m_rootAccess;

   //El acceso de las relaciones lo da el contexto, y el de los
   // atributos lo da el dominio

   public Object clone(){
       return new scope(m_md,m_userRol,(access)m_rootAccess.clone(),m_operation );
   }

   public scope( metaData md, Integer userRol, access rootAccess, int operation ){
	m_md=md;
	m_operation=operation;
	m_rootAccess= rootAccess;
	m_userRol=userRol;
   }

   public scope( metaData md, Integer userRol, Integer rootContext, int operation )
	throws DataErrorException{

	this( md, userRol, md.getContext(rootContext), operation );
   }

   public scope( metaData md, Integer userRol, Contexto rootContext, int operation )
	throws DataErrorException{
	m_md=md;
	m_operation=operation;
	if( rootContext.toRoot!=0 )
		throw new DataErrorException("SCOPE: CONTEXT NOT ROOT");

	m_rootAccess= rootContext.getAcceso( userRol );
        if( m_rootAccess==null )
            throw new DataErrorException("Scope error, no acceso de rol "+userRol+" en context root "+rootContext.id);
	m_userRol=userRol;
   }
   public void setMetadata( metaData md ){
       m_md=md;
   }

   public Integer getUserRol(){
	return m_userRol;
   }

   public access getRootAccess(){ return m_rootAccess; }

   public access getAccess( Integer idCtx ){
	return getAccess( m_userRol, idCtx );
   }

   public access getAccess( Contexto ctx ){
	return getAccess( m_userRol, ctx, true );
   }


   private access getAccess( Integer userRol, Integer idCtx ){
	Contexto ctx= m_md.getContext(idCtx);
	return getAccess( userRol, ctx, true );
   }

   private access getAccess( Integer userRol, Contexto ctx, boolean fusionAware ){
	Integer fusTO_low= new Integer( ctx.to );
	Integer ctxLow= m_md.getFusionControl().getCtxLowFromTO_low( fusTO_low );
	if( 	fusionAware &&
		ctxLow!=null ){
               //System.out.println("get access, si fus "+ctxLow);
		//esto quiere decir que este contexto está por debajo del toLow fusionado original
		// Por tanto debe restinjirse su acceso desde el acceso root de dicho to Low, y no de este scope
		return getAccess( userRol, ctx, getAccess( userRol, m_md.getContext(ctxLow) , m_rootAccess ));
	}else{
            //System.out.println("get access, no fus");
            return getAccess(userRol, ctx, m_rootAccess);
        }
   }

   private access getAccess( Integer userRol, Contexto ctx, access rootAccess ){
	access acc= ctx.getAcceso( userRol );
        //System.out.println("get access tmp, ctx,rootAcc,acc,oper:"+ctx.id+","+rootAccess+","+acc+","+m_operation);
	access newAcc= new access( new access( acc, m_operation ), rootAccess );
        //System.out.println("get access new :"+newAcc);
        return newAcc;
   }

   public int getOperation(){
	return m_operation;
   }


   public Element getContexts(  	int toRoot,
					int to,
					int action,
					boolean getSpecialized){
	return getContexts( m_userRol, toRoot, to, action, getSpecialized );
   }


   public Element getContexts(  	Integer userRol,
					int toRoot,
					int to,
					int action,
					boolean getSpecialized){
	Element res= new Element("CONTEXTOS");
	System.out.println("PET GETCTXS:"+action+","+toRoot+","+to+","+userRol);
	Iterator itr= m_md.getContextosSet().iterator();
	while( itr.hasNext() ){
		Integer idCtx= (Integer)itr.next();
		Contexto ctx= m_md.getContext( idCtx );
		//System.out.println("CDBG1:"+idCtx);
		if( action>=0 && ctx.action!=action ) continue;
		//System.out.println("CDBG2");
		if( toRoot>0 && !m_md.isSpecializedFrom( toRoot, ctx.toRoot )  ) continue;
		//System.out.println("CDBG3");
		if( toRoot <=0 && ctx.toRoot>0 ) continue;
		//System.out.println("CDBG4");
		if( !ctx.userRolEnable( userRol ) ) continue;
		//System.out.println("CDBG5");
		if( getSpecialized ){
			//System.out.println("CDBG6");
			if( !m_md.isSpecializedFrom( ctx.to, to ) ) continue;
		}else{
			//System.out.println("CDBG7");
			if( !m_md.isSpecializedFrom( to, ctx.to ) ) continue;//seraan los aplicados
		}
		//System.out.println("CTX:"+idCtx);
		res.addContent( subGetContext( userRol, ctx, getSpecialized ) );
	}
	return res;
   }

   Element subGetContext( Integer userRol, Contexto ctx, boolean getSpecialized ){
	Integer idCtx= new Integer( ctx.id );
	Element eCtx= new Element("CONTEXT");
	eCtx.setAttribute("ID",idCtx.toString());
	eCtx.setAttribute("ID_TO",String.valueOf(ctx.to));
	eCtx.setAttribute("TO_LABEL", m_md.getTOLabel( new Integer( ctx.to ) ) );
	Element eFilter=null;
	String accessStr= getAccess( userRol, ctx, true ).toString();
	if( ctx.idFilter!=0 ){
		eFilter= (Element)m_md.getFilter(new Integer(ctx.idFilter)).clone();
		eFilter.setAttribute("ID", String.valueOf(ctx.idFilter));
		eFilter.setAttribute("ID_CONTEXT", idCtx.toString());
	  	eFilter.setAttribute("ACCESS", accessStr);
		if(  ctx.idRel!=0 ){
			eFilter.setAttribute("ID_REL", String.valueOf( ctx.idRel ));
			eFilter.setAttribute("ROL_CONTEXT", String.valueOf( ctx.idRolCtx ));
			eFilter.setAttribute("ROL_CURRENT", String.valueOf( ctx.idRolCurr ));
			eFilter.setAttribute("ROL_SUP", (ctx.childIsSup ? "CHILD":"CURRENT"));
		}
		eCtx.addContent(eFilter);
		ctx.fixAVAs( m_md, eFilter );
	}
	if( ctx.idRel!=0 ){
		eCtx.setAttribute("ID_REL", String.valueOf( ctx.idRel ));
		eCtx.setAttribute("ROL_CONTEXT",String.valueOf( ctx.idRolCtx ));
		eCtx.setAttribute("ROL_CURRENT",String.valueOf( ctx.idRolCurr ));
		eCtx.setAttribute("ROL_SUP",(ctx.childIsSup ? "CHILD":"CURRENT"));
	}

	eCtx.setAttribute("N_MAX",String.valueOf(ctx.nMax));
	if( eFilter!=null ) eFilter.setAttribute("N_MAX",String.valueOf(ctx.nMax));

	eCtx.setAttribute("N_MIN",String.valueOf(ctx.nMin));
	if( eFilter!=null ) eFilter.setAttribute("N_MIN",String.valueOf(ctx.nMin));

	eCtx.setAttribute("ID_ACTION",String.valueOf(ctx.id));
  	eCtx.setAttribute("ACCESS", accessStr);
	Integer iIdDOM= ctx.getDom(userRol);
	//System.out.println("MD DEBUG ID_ACTION "+ctx.id+","+iIdDOM+",ROL "+userRol);
	if(iIdDOM!=null){
		Element eDom= (Element)m_md.getDominio( iIdDOM ).clone();
		eDom.setAttribute( "ID",iIdDOM.toString() );
		boolean buscoDomAplicablesAInstancias= !getSpecialized;
		if( 	!buscoDomAplicablesAInstancias &&
			m_md.containsDomIndex( iIdDOM ))
			eliminaReferenciaIndice( 	eDom,
							m_md.getDomIndex(iIdDOM) );

		eCtx.addContent(eDom);
	}
	return eCtx;
  }

  private void eliminaReferenciaIndice( Element eDomAdaptar, atReference atrAadaptar ) {

	Element subdomAadaptar= jdomParser.findElementByAt( 	eDomAdaptar.getChild("POLIMORFISMO"),
								"DOMINIO",
								"REF",
								String.valueOf(atrAadaptar.idSubdomRef),
								true );

	if( subdomAadaptar!=null ){
		Element rule= subdomAadaptar.getParent();
		Element atRest= rule.getParent();
		Element polim= atRest.getParent();
		if( atRest.getChildren("RULE").size()==1 )
			polim.removeContent( atRest );
		else
			atRest.removeContent( rule );
	}
   }

   public static scope buildDefaultScope( metaData md, String user, Integer idCtx, int operation ){
	return buildDefaultScope( md, user, md.getContext( idCtx ),operation );
   }

   public static scope buildDefaultScope( metaData md, String user, Contexto ctx, int operation ){
	try{
	ArrayList lista= rolProperties.getRol( user, true );
	Integer userRol= (Integer)lista.get(0);
	return new scope( md, userRol, ctx, operation );
	}catch(DataErrorException e){
		e.printStackTrace();
		return null;
	}
   }
}
*/