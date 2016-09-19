package dynagent.ejb.old;
/*package dynagent.ejb;

import org.jdom.Element;

public class fixProperties extends Object{
	public boolean optional=false;
	public String filterNode=null;//sumidero o afectado
	public int ctxFix=0;//fix es source
	public int refNodeFix;
	public Element detail;
	public int incrustar=0;

	public fixProperties( boolean optional, String node, int ctx, int ref, Element detail, int incrustar){
		this.optional=optional;
		filterNode=node;
		ctxFix=ctx;
		refNodeFix=ref;
		this.detail=detail;
		this.incrustar=incrustar;
	}

	public String toString(){
		return "FIXPROPERTIES:ctxFix "+ctxFix+",filterNode "+filterNode+", refNodeFix "+refNodeFix+", incrs "+incrustar;
	}
        public Object clone(){
            Element cDetail= detail==null ? null:(Element)detail.clone();
            fixProperties res= new fixProperties(optional,filterNode,ctxFix,refNodeFix,cDetail,incrustar);
            return res;
        }
}
*/