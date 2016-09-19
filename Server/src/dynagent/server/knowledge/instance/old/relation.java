/*package dynagent.knowledge.instance;

import dynagent.ejb.helperConstant;
import dynagent.knowledge.Contexto;
import java.util.HashMap;
import dynagent.application.*;
import java.util.ArrayList;
import dynagent.ejb.jdomParser;
import java.util.Iterator;
import dynagent.ejb.DataErrorException;
import org.jdom.Element;
import dynagent.knowledge.metaData;

public class relation extends attributeContainer{
    //property identifier
	int idRel = 0;
   

    //rango
    instance parent;
    boolean superiorRol;
    
    ArrayList relListenerList=new ArrayList();
    defaultSessionable m_instanceLinkSession= null;

    public Object clone() {
        return clone((m_instanceLinkSession==null ? null:(instance)m_instanceLinkSession.getValue()),false);
    }

    private relation clone(instance relInstance, boolean discardChanges) {
        relation res = new relation(null, getOperation(), parent, superiorRol, ido, idTo,  idRel,rdn);
        if( relInstance!=null ){
            relInstance= discardChanges ? relInstance.extractChanges():(instance)relInstance.clone();
            res.setRelationInstance( null, relInstance );
        }
        subClone(res,discardChanges);
        return res;
    }

    public void addRelationListener( relationListener rl ){
        relListenerList.add(rl);
    }

    void notifyChange(){
        if(relListenerList!=null )
            for( int i=0; i<relListenerList.size();i++)
                ((relationListener)relListenerList.get(i)).relationChange(this,getOperation());
    }

    public boolean hasChanged(){
        if( super.hasChanged() )
            return true;
        else{
            if( isNull() )
                return false;
            if( getRelationInstance() == null )
                return false;
            else
                return getRelationInstance().hasChanged();
        }
    }

    public relation extractChanges(){
        if (!hasChanged() &&
            !hasInstanceData(true))
            return null;

        if( hasInstanceData(true) ){
            return clone(getRelationInstance(),true);
            /*instance child = getRelationInstance().extractChanges();
            if (child == null )
                if(!hasChangedFromDatabase())
                    return null;
                else
                    return clone(null,true);
            return clone(child,true);
        }else
            return clone(null,true);
    }

    private relation(session ses,int operation,instance parent,boolean superiorRol,
    				int ido,int to,int idRel,String rdn) {
        super(ses,operation);
        this.superiorRol = superiorRol;
        this.parent = parent;
        this.ido = ido;
        this.idTo = to;
        this.idRel = idRel;
        this.rdn = rdn;
    }

    public relation(instance parent, boolean superiorRol, int ido, int to,
                    int idRel,   String rdn,  session ses, int operation) {
        this(ses, operation, parent, superiorRol, ido, to, idRel, rdn);
    }

    

    public void delete(session ses) {
        addAction(ses, dynagent.application.action.DEL, null);
        notifyChange();
    }

    public void create( session ses ){
        addAction(ses, dynagent.application.action.NEW, null);
        notifyChange();
    }

    public attribute getAttribute(int tapos, int ref) {
        attribute att= super.getAttribute(tapos,ref);
        if (att == null && getRelationInstance() != null)
            att = getRelationInstance().getAttribute(tapos, ref);
        return att;
    }

    public selectData getRelationData( metaData md, Element filter ){
        selectData sd = new selectData();
        instance  currIns = new instance(null, getOperation(), getType(), getIDO());
        sd.addInstance(currIns);
        getSelection(md,currIns,filter);
        return sd;
    }

    void getSelection(metaData md,instance ins,Element filter){
        super.getSelection(md,ins,filter);
        if( hasInstanceData(false) )
            getRelationInstance().getSelection(md,ins,filter);
    }

    void getSelection(metaData md,instance currIns,int tapos,int ref){
    	attribute att = getAttribute(tapos, ref);
        if (att != null) {
            try {
                att=(attribute)att.clone();
                if( ref!=0 ){
                    att.setVirtualREF(ref);
                    att.setVirtualTO(getType());
                    att.setVirtualIDO(getIDO());
                }
                currIns.addAttribute(null, att, ref);
            } catch (DataErrorException de) {
                de.printStackTrace();
            }
        }
    }

    public instance getParent() {
        return parent;
    }

  

    public boolean hasInstanceData(boolean checkDiferentialExistence) {
        if(m_instanceLinkSession==null)
           return false;
       if(checkDiferentialExistence){
           return m_instanceLinkSession.hasChanged();
       }else
           return m_instanceLinkSession.exists();
    }

    public boolean isSuperior() {
        return superiorRol;
    }

    public void setRelationInstance(session ses, instance ins) {

       if( m_instanceLinkSession==null ){
           if( ins==null )
               return;
           m_instanceLinkSession= new defaultSessionable(ses,action.NEW,ins){
                                                             public void notifyChange(){}
                                                         };
             //Como dato de tipo link, crear el dato es new, y borrarlo del (con sentido para mantener un cache)
       }else{
           if( ins==null )
               if( m_instanceLinkSession.exists() )
                   m_instanceLinkSession.delete(ses);
               else return;
           else
               m_instanceLinkSession.addAction(ses, action.NEW, ins);
       }

       if( m_instanceLinkSession.exists() )
            ins.setRelationToContainer(this);
    }

    public void setRdn( session ses, String rdn ){
        this.rdn= rdn;
    }

    public instance getRelationInstance() {
        return m_instanceLinkSession==null ? null:(instance)m_instanceLinkSession.getValue();
    }

    public int getIdRel() {
        return idRel;
    }

   

    public boolean getRolIsSuperior(){
        return superiorRol;
    }

   
    public Element toElement(){
        Element eRel = new Element("ITEM");
        if (getRdn() != null)
            eRel.setAttribute("RDN", getRdn());
        instance.buildAtChanged(eRel, getOperation());
        instance.toElementAtt(getAttIterator(false, false), eRel);
        eRel.setAttribute("ID_O", String.valueOf(getIDO()));
        eRel.setAttribute("ID_TO", String.valueOf(getType()));
        eRel.setAttribute("ID_REL", String.valueOf(getIdRel()));
        eRel.setAttribute("ROL_IS_SUP",
                          (getRolIsSuperior() ? "TRUE" : "FALSE"));     
   
        if( hasInstanceData(true) )
             eRel.addContent(getRelationInstance().toElement());
        return eRel;
    }
}*/
