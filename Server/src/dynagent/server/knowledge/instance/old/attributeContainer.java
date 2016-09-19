package dynagent.knowledge.instance.old;
/*package dynagent.knowledge.instance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import dynagent.application.action;
import dynagent.application.session;
import dynagent.ejb.DataErrorException;
import dynagent.ejb.helperConstant;
import dynagent.knowledge.metaData;




public class attributeContainer extends defaultSessionable implements Serializable, attributeListener {
	int idTo,ido;

   
    String rdn=null;
    private HashMap atList = new HashMap();

   
    
    attributeContainer(){
        super();//solo para que acepte ser serielized
        //System.out.println("ATT CONTAINER SERIALIZED");
    }

    public attributeContainer( session ses, int operation ){
        super(ses,operation, null);
    }

    public void subClone( attributeContainer res, boolean checkDiferentialExistence ){
        Iterator itr= getAttIterator(false,checkDiferentialExistence);
        while( itr.hasNext() ){
            attribute att=(attribute)itr.next();
            *//** @todo La condicion de indice es una chapuza para el GUI, para que envie este dato al servidor
             * y pueda ser utilizado en el calculo sin necesidad de declararlo como transitivo
             *//*

            if( checkDiferentialExistence && !att.hasChanged() && att.getTapos()!=helperConstant.TAPOS_INDICE)
                continue;
            try{
                attribute newAtt=(attribute)att.clone();
                //System.out.println("ATTCONT CLONE:"+att+",new "+newAtt);
                res.addAttribute(null,newAtt, att.getVirtualREF());
            }catch(DataErrorException de){
                de.printStackTrace();
            }
        }
    }

    public boolean hasChanged(){
        if (super.hasChanged()) return true;
        if( isNull() ) return false;
        Iterator itr= getAttIterator(false,true);
        return itr.hasNext();
    }

   

    public boolean hasAttribute(int tapos, int ref, boolean checkDiferentialExistence){
        Integer iTAPOS = new Integer(tapos);
        Integer iRef = new Integer(ref);
        if (!atList.containsKey(iRef)) {
            return false;
        } else {
            HashMap lista = (HashMap) atList.get(iRef);
            if (!lista.containsKey(iTAPOS)) {
                return false;
            }
            attribute att = (attribute) lista.get(iTAPOS);
            if (att == null)return false;

            if (checkDiferentialExistence)
                return att.hasChanged();
            else
                return att.exists();
        }
    }

    public void addAttribute(session ses, attribute att, int ref) throws DataErrorException {
        //System.out.println("ADDATTR "+ref);
        if (att != null && !( att.getOperation()!=action.DEL && att.getValue() == null)) {
           // System.out.println("ADDATTR NOT NULL");
            Integer iRef = new Integer(ref);
            HashMap list = (HashMap) atList.get(iRef);
            if (list == null) {
                list = new HashMap();
                atList.put(iRef, list);
            }
            list.put(new Integer(att.getTapos()), att);
            if( att.getTapos()==helperConstant.TAPOS_RDN && ref==0 )
                setRdn(ses,att.getValue().toString());
            att.addAttributeListener(this);
        } else {
            throw new DataErrorException("ATRIBUTTE OR VALUE NULL: " + att);
        }
    }

    void removeAttribute( attribute att ){
        Integer iRef = new Integer(att.getVirtualREF());
        if( atList.containsKey(iRef) ){
            HashMap list = (HashMap) atList.get(iRef);
            Integer tapos = new Integer(att.getTapos());
            if (list.containsKey(tapos)) {
                list.remove(tapos);
                if (list.size() == 0)
                    atList.remove(iRef);
            }
        }
    }

    void notifyChange(){;}

  
   
   

    void getSelection(metaData md, attributeContainer currIns, int tapos, int ref,int idTo ){
        attribute att = getAttribute(tapos, ref);
       // System.out.println("ATT" + att);
        if (att != null) {
            try {
                att= (attribute)att.clone();
                currIns.addAttribute(null, att, ref);
                if( ref!=0 ){
                    att.setVirtualREF(ref);
                    att.setVirtualTO(getType());
                    att.setVirtualIDO(getIDO());
                }
            } catch (DataErrorException de) {
                de.printStackTrace();
            }
        }
    }
   

    public void delAttribute( session ses, int tapos, int ref ){
        attribute att= getAttribute(tapos,ref);
        if( att!=null ){
            if( ses==null ){
                Integer iRef = new Integer(ref);
                HashMap list = (HashMap)atList.get(iRef);
                list.remove(new Integer(tapos));
            }else
                att.delete(ses);
        }
    }
    
    public int getIDO() {
		return ido;
	}

	public void setIDO(int ido) {
		this.ido = ido;
	}
	
	public int getType() {
		return idTo;
	}

	public void setType(int idTo) {
		this.idTo = idTo;
	}

    public void setRdn( session ses, String rdn ){
        attribute at=getAttribute(helperConstant.TAPOS_RDN,0);
        try{
            if(at == null)
                addAttribute(ses,
                             new attribute(helperConstant.TAPOS_RDN,
                                           helperConstant.TM_TEXTO, rdn, ses, dynagent.application.action.NEW),
                             0);
            else
                at.setValue(ses,rdn);
        }catch(DataErrorException de){
            de.printStackTrace();
        }
        this.rdn= rdn;
    }

    public String getRdn() {
        return rdn;
    }


  

    public attribute getAttribute(int tapos, int ref) {
        //Solo devuelve atributo si existe, por tanto si pretendo recuperara at ordenes que no existan
        // necesito un iterador que me de directamente el atributo
        Integer iTAPOS = new Integer(tapos);
        Integer iRef = new Integer(ref);
        if (!atList.containsKey(iRef)) {
            return null;
        } else {
            HashMap lista = (HashMap) atList.get(iRef);
            if (!lista.containsKey(iTAPOS)) {
                return null;
            }
            attribute att=(attribute) lista.get(iTAPOS);
            if( !att.exists() )
                return null;
            else return att;
        }
    }

    public Object getAttributeValue(int tapos, int ref) {
        attribute att = getAttribute(tapos, ref);
        if (att != null) {
            return att.getValue();
        } else {
            return null;
        }
    }

    public Iterator getAttIterator(int ref) {
        //System.out.println("GETATTITR "+atList.size());
        ArrayList res = new ArrayList();
        HashMap lista = (HashMap) atList.get(new Integer(ref));
        if (lista != null) {
           // System.out.println("GETATTITR LIST "+lista.size());
            Iterator iL = lista.values().iterator();
            while (iL.hasNext()) {
                attribute att = (attribute) iL.next();
               // System.out.println("GETATTITR ATT x");
                if (att.exists()) {
                  //  System.out.println("GETATTITR SI EX");
                    res.add(att);
                }
            }
        }
        return res.iterator();
    }

    public Iterator getDirectAttIterator() {
        return getAttIterator(0);
    }

   

    public boolean hasAttributes(boolean checkDiferentialExistence){
        if( atList.size()==0 )
            return false;
        Iterator itr=atList.values().iterator();
        while(itr.hasNext()){
            HashMap lista = (HashMap)itr.next();
            Iterator iL = lista.values().iterator();
            while (iL.hasNext()) {
                attribute att = (attribute) iL.next();
                if( checkDiferentialExistence ){
                    if (att.hasChanged())
                        return true;
                }else
                if ( att.exists() )
                    return true;
            }
        }
        return false;
    }

    public Iterator getAttIterator(boolean justDirect, boolean checkDiferentialExistence) {
        ArrayList res = new ArrayList();
        Iterator refs = atList.keySet().iterator();
        while (refs.hasNext()) {
            Integer ref = (Integer) refs.next();
            if (justDirect && ref.intValue() > 0) {
                continue;
            } else {
                HashMap lista = (HashMap) atList.get(ref);
                Iterator iL = lista.values().iterator();
                while (iL.hasNext()) {
                    attribute att = (attribute) iL.next();
                    if( checkDiferentialExistence ){
                        if (att.hasGhanged())
                            res.add(att);
                    }else
                    if( att.exists() )
                        res.add(att);
                }
            }
        }
        return res.iterator();
    }


    public void attributeChanged(attribute att) {
        if( att.getTapos()==helperConstant.TAPOS_RDN && att.getValue()!=null )
            rdn=att.getValue().toString();
        if( !att.hasGhanged() && !att.exists())
            removeAttribute(att);
    }
}
*/