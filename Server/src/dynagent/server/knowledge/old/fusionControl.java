package dynagent.knowledge.old;
/*package dynagent.knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

import dynagent.application.session;
import dynagent.ejb.CommunicationException;
import dynagent.ejb.DataErrorException;
import dynagent.ejb.RemoteSystemException;
import dynagent.ejb.SystemException;
import dynagent.ejb.docServer;
import dynagent.ejb.helperConstant;
import dynagent.ejb.jdomParser;
import dynagent.knowledge.instance.attribute;
import dynagent.knowledge.instance.attributeContainer;
import dynagent.knowledge.instance.contextData;
import dynagent.knowledge.instance.instance;
import dynagent.knowledge.instance.InstanceLockedException;
import dynagent.knowledge.instance.relation;
import dynagent.knowledge.instance.selectData;
import dynagent.ruleengine.meta.api.ObjectProperty;

class atMapData extends Object {
    private Integer idCtxLow;
    private Integer idCtxRoot;
    private Integer taposSource;
    private Integer taposFus;
    private fusionRestoreData frd;
    atMapData(fusionRestoreData frd, Integer taposSource, Integer taposFus) {
        this.frd = frd;
        this.idCtxLow = frd.ctxLow;
        this.idCtxRoot = frd.ctxRoot;
        this.taposSource = taposSource;
        this.taposFus = taposFus;
    }

    Integer getCtxLow() {
        return idCtxLow;
    }

    fusionRestoreData getRestoreData() {
        return frd;
    }

    Integer getCtxRoot() {
        return idCtxRoot;
    }

    Integer getTaposSource() {
        return taposSource;
    }

    Integer getTaposFus() {
        return taposFus;
    }

    public Object clone() {
        atMapData res = new atMapData((fusionRestoreData) frd.clone(),
                                      taposSource,
                                      taposFus);
        return res;
    }
}


class fusionRestoreData extends Object {
    Element eTO = null;
    Element sourceFilter = null;
    HashMap herencias = null;
    Integer toRootId = null;
    int lowToId = 0;
    boolean lowExtracted = false;
    Integer userRol = null;
    Integer ctxLow = null;
    Integer ctxRoot = null;
    Integer rootDomSource = null;
    public Object clone() {
        fusionRestoreData res = new fusionRestoreData();
        if (eTO != null) {
            res.eTO = (Element) eTO.clone();
        }
        if (sourceFilter != null) {
            res.sourceFilter = (Element) sourceFilter.clone();
        }
        if (herencias != null) {
            res.herencias = (HashMap) metaData.cloneObject(herencias);
        }
        res.toRootId = toRootId;
        res.lowToId = lowToId;
        res.lowExtracted = lowExtracted;
        res.userRol = userRol;
        res.ctxLow = ctxLow;
        res.ctxRoot = ctxRoot;
        res.rootDomSource = rootDomSource;
        return res;
    }
}


class processEnableDom {
    // si la instancia low de una fusion debe deshabilitarse porque su acceso está restrinjido, lo que hara
    // es modificar el dominio low
    boolean disableLow = false;
    ArrayList domChangeList = null;
    Element propRoot = null;

    processEnableDom(scope myScope,
                     metaData md,
                     HashMap fusionDomLog,
                     Contexto newCtx,
                     Contexto ctxLow) {

        access accessLow = myScope.getAccess(ctxLow);
        disableLow = !accessLow.matches(access.SET | access.NEW);
        Integer domLowId = newCtx.getDom(myScope.getUserRol());
        Element domLow = md.getDominio(domLowId);
        Integer newCtxId = new Integer(newCtx.id);
        HashMap domData = (HashMap) fusionDomLog.get(newCtxId);

        if (domData == null) {
            domData = new HashMap();
            fusionDomLog.put(newCtxId, domData);
        }
        if (domLow.getChild("PROPERTY_LIST") == null) {
            domLow.addContent(new Element("PROPERTY_LIST"));
        }
        propRoot = domLow.getChild("PROPERTY_LIST");

        domChangeList = (ArrayList) domData.get(domLowId);
        if (domChangeList == null) {
            domChangeList = new ArrayList();
            domData.put(domLowId, domChangeList);
        }
    }

    void addPropertie(int tapos) {
        if (disableLow) {
            Element prop = jdomParser.findElementByAt(propRoot,
                    "ATRIBUTO",
                    "TA_POS",
                    String.valueOf(tapos),
                    false);
            if (prop == null) {
                prop = new Element("ATRIBUTO");
                prop.setAttribute("TA_POS", String.valueOf(tapos));
                prop.setAttribute("ACCESS", "READ");
                prop.setAttribute("CHANGE", "ADDED");
                propRoot.addContent(prop);
                domChangeList.add(prop.clone());
            } else {
                String acceso = prop.getAttributeValue("ACCESS");
                prop.setAttribute("CHANGE", "MODIFIED");
                if (!acceso.equals("NOREAD")) {
                    prop.setAttribute("OLD_ACCESS", acceso);
                    prop.setAttribute("ACCESS", "READ");
                }
            }
        }
    }
}


public class fusionControl extends Object {
    metaData m_md;
    public HashMap m_fusionContextosLog = new HashMap();
    // public HashMap m_fusionContextosLogReverse= new HashMap();
    // public HashMap m_fusionFilterLog= new HashMap();
    public HashMap m_fusionDomLog = new HashMap();
    // HashMap m_fusionLogTO= new HashMap();
    // HashMap m_fusionLogTO_low= new HashMap();
    int m_virtualAtIndex = -1;
    private HashMap m_atMap = new HashMap();

    public final static int RELATION_OBJECT = 1;

    fusionControl(metaData md) {
        m_md = md;
    }

    public Object clone(metaData md) {
        fusionControl fc = new fusionControl(md);
        fc.m_fusionContextosLog = (HashMap) m_fusionContextosLog.clone();
        Iterator itr = m_fusionContextosLog.keySet().iterator();
        while (itr.hasNext()) {
            Integer id = (Integer) itr.next();
            fusionRestoreData frd = (fusionRestoreData) m_fusionContextosLog.
                                    get(id);
            fc.m_fusionContextosLog.put(id, frd.clone());
        }
        fc.m_fusionDomLog = (HashMap) metaData.cloneObject(m_fusionDomLog);
        itr = m_atMap.keySet().iterator();
        while (itr.hasNext()) {
            Integer id = (Integer) itr.next();
            System.out.println("ATMAP " + id);
            atMapData atmap = (atMapData) m_atMap.get(id);
            if (atmap == null) {
                System.out.println("ATMAP NULO");
            }
            fusionRestoreData frdSource = atmap.getRestoreData();
            if (frdSource == null) {
                System.out.println("FRD SOURCE NULO");
            }
            fusionRestoreData frdClone = (fusionRestoreData) fc.
                                         m_fusionContextosLog.get(frdSource.
                    ctxRoot);
            fc.addFusionAt(frdClone, atmap.getTaposSource(), atmap.getTaposFus());
        }
        fc.m_virtualAtIndex = m_virtualAtIndex;
        return fc;
    }

    public boolean isAtFusioned(Integer fusTapos) {
        return m_atMap.containsKey((fusTapos));
    }

    private atMapData getAtMap(Integer fusTapos) throws DataErrorException {
        if (!isAtFusioned(fusTapos)) {
            throw new DataErrorException("NO AT FUSIONED:" + fusTapos);
        } else {
            return (atMapData) m_atMap.get(fusTapos);
        }
    }

    void addFusionAt(fusionRestoreData frd, Integer taposSource,
                     Integer taposFus) {
        m_atMap.put(taposFus, new atMapData(frd, taposSource, taposFus));
    }

    private int getFusionAt(Integer ctxLow, Integer sourceAT) {
        //Se supone que los newTapos no son ambiguos, ya que virtualmente pertenecen a un unico TO

        Iterator itr = m_atMap.keySet().iterator();
        while (itr.hasNext()) {
            Integer taposFus = (Integer) itr.next();
            try {
                atMapData data = getAtMap(taposFus);
                if (ctxLow.equals(data.getCtxLow()) &&
                    data.getTaposSource().equals(sourceAT)) {
                    return data.getTaposFus().intValue();
                }
            } catch (DataErrorException de) {
                de.printStackTrace();
                return sourceAT.intValue();
            }
        }
        return sourceAT.intValue();
        //throw new DataErrorException("AT NOT FUSIONED, AT:"+sourceAT+",ctx:"+ctxLow);
    }

    private void restoreFusion(Integer idCtx) throws SystemException {
        Contexto ctxNew = m_md.getContext(idCtx);

        //System.out.println("INIRESTORE "+idCtx);
        Integer iTO = new Integer(ctxNew.to);
        if (contextIsFussioned(idCtx)) {
            fusionRestoreData frd = getFusionData(idCtx);
            //System.out.println(frd);
            if (frd.sourceFilter != null) {
                jdomParser.print("RESTORE FILTER:" +
                                 frd.sourceFilter.getAttributeValue("ID"),
                                 frd.sourceFilter);
                m_md.addFilter(frd.sourceFilter,
                               Integer.parseInt(frd.sourceFilter.
                                                getAttributeValue("ID")));
                //m_fusionFilterLog.remove(new Integer(frd.sourceFilter.getAttributeValue("ID")));
            }
            //System.out.println("RESTORE DOM:dom,rol:"+frd.rootDomSource+","+frd.userRol);
            ctxNew.fusionado = false;
            if (frd == null) {
                throw new SystemException(SystemException.ERROR_DATOS,
                                          "Restore Fusion error para " + iTO);
            }
            ctxNew.setDom(frd.userRol, frd.rootDomSource.intValue());
            m_md.m_herencias.put(iTO, frd.herencias);
            m_md.m_metaTOs.put(iTO, frd.eTO);
            //m_fusionLogTO.remove( iTO );
            if (m_fusionDomLog.containsKey(idCtx)) {
                HashMap domData = (HashMap) m_fusionDomLog.get(idCtx);
                Iterator itr = domData.keySet().iterator();
                while (itr.hasNext()) {
                    Integer idDom = (Integer) itr.next();
                    Element dom = m_md.getDominio(idDom);
                    if (dom.getChild("PROPERTY_LIST") != null) {
                        ArrayList lista = (ArrayList) domData.get(idDom);
                        for (int i = 0; i < lista.size(); i++) {
                            Element prop = (Element) lista.get(i);
                            prop.detach();
                        }
                        Iterator iD = dom.getChild("PROPERTY_LIST").getChildren("ATRIBUTO").iterator();
                        while (iD.hasNext()) {
                            Element prop = (Element) iD.next();
                            if (prop.getAttributeValue("CHANGE") != null &&
                                prop.getAttributeValue("OLD_ACCESS") != null) {
                                prop.setAttribute("ACCESS",
                                                  prop.getAttributeValue("OLD_ACCESS"));
                                prop.removeAttribute("OLD_ACCESS");
                            }
                        }
                    }
                }
            }
            Iterator itr= frd.atMap.keySet().iterator();
               while(itr.hasNext()){
                   Integer tapos= (Integer)itr.next();
                   if( tapos.intValue()<0 ){
                    //se creo un nuevo registro debido a conflicto root-low.
                    //No tengo que rehacer el metaTO porque restaurar el original por completo
                    int tm=m_md.getID_TM( taposSource );
                    //No tengo que hacer nada porque los datos virtuales del AT no se han mapeado a
                    //ningun TO, por tanto no afectan, y si volvemos a rtefusionar se machacan
                   }
               }
            //Si borrase indiscriminadamente todas las entradas, la restauración de un ctx aparanteria
            //que otro ctx ya no estan fusionados
            m_fusionContextosLog.remove(idCtx);
            //m_fusionContextosLogReverse.remove(idCtx);
            ArrayList atToDel = new ArrayList();
            Iterator itr = m_atMap.keySet().iterator();
            while (itr.hasNext()) {
                Integer taposFus = (Integer) itr.next();
                try {
                    atMapData data = getAtMap(taposFus);
                    if (data.getCtxRoot().equals(idCtx)) {
                        atToDel.add(taposFus);
                    }
                } catch (DataErrorException de) {
                    de.printStackTrace();
                }
            }
            for (int i = 0; i < atToDel.size(); i++) {
                m_atMap.remove((Integer) atToDel.get(i));
            }
            //m_fusionLogTO_low.remove(new Integer(frd.lowToId));
            //System.out.println("REMOVETOLOW:"+frd.lowToId);
        }
    }

    public int[] findFusionableSubContext(scope myScope, Integer userRol,
                                          int intIdCtx) {
        int[] res = new int[2];
        res[0] = 0; //fusion policy
        res[1] = 0; //fusion CTX
        Integer ctxId = new Integer(intIdCtx);
        Contexto cc = m_md.getContext(ctxId);
        if (cc.idRel == helperConstant.REL_TRANSITIVA) {
            return res;
        }
        List subList = myScope.getContexts(userRol,
                                           cc.to,
                                           helperConstant.TO_BASE,
                                           cc.action,
                                           true).getChildren("CONTEXT");
        Iterator iSub = subList.iterator();
        while (iSub.hasNext()) {
            Element subCtx = (Element) iSub.next();
            Integer subId = new Integer(subCtx.getAttributeValue("ID"));
            Contexto ccS = m_md.getContext(subId);
            if (!(ccS.idFilter == 0 ||
                  ccS.idRel == helperConstant.REL_TRANSITIVA ||
                  ccS.id == helperConstant.DOM_DEFAULT ||
                  ccS.tran != cc.tran)) {

                Element filter = subCtx.getChild("FILTER");
                filter.setAttribute("FUSION_POLICY", "0");

                if (filter.getAttributeValue("FUSION") != null) {
                    continue;
                }

                //System.out.println("SUBCTX:"+ccS.id+",BROTHER "+subList.size());
                if (checkPolicy_RELATION_OBJECT(userRol, ccS, subList.size())) { //a fusionar
                    res[0] = RELATION_OBJECT;
                    res[1] = ccS.id;
                    return res;
                }
            }
        }
        return res;
    }

    public boolean checkPolicy_RELATION_OBJECT(Integer userRol, Contexto ctxLow,
                                               int brotherSize) {
        if (brotherSize == 1 &&
            ctxLow.nMin == ctxLow.nMax &&
            ctxLow.nMax == 1 &&
            visibilityImprovement(m_md, userRol, ctxLow)) {
            return true;
        } else {
            return false;
        }

        //BALANCE DE PESOS
        //Integer toSup= new Integer( ctxLow.toRoot );
        //Element metaSup= m_md.getMetaTO( toSup );
        //int weightSup= attributeCount( metaSup );
        //Integer toLow= new Integer( ctxLow.to );
        //Element metaLow= m_md.getMetaTO( toLow );
        //int weightLow= attributeCount( metaLow );
        //String access=ctxLow.getAcceso(userRol);
        //if( 	weightSup<6 && ((double)weightSup)/((double)weightLow)<0.3 &&
        //	!access.matches(".*((NEW)|(DEL)|(SET)).*") &&
        //	access.indexOf("RREL")>=0 ) return true;
        //else
        //	return false;
    }

    public static boolean visibilityImprovement(metaData md, Integer userRol,
                                                Contexto ctxLow) {
        //Mirare si fusionar este conetxto inferior mejora la visibilidad,
        //es decir, si aumenta el num de campos visible
        Element filter = md.getFilter(new Integer(ctxLow.idFilter));
        int visibilityFilter = jdomParser.getFilterAtList(filter, true).size();
        System.out.println("VISIBIL FILTER " + ctxLow.idFilter + "," +
                           visibilityFilter);
        Element metaTOLow = md.getOriginalMetaTO(new Integer(ctxLow.to)); //podria estar ya fusionado, por eso pido el original

        int visibilityTO = jdomParser.elementsWithAt(metaTOLow.getChild("ATRIBUTOS"), "TA_POS", true).size();
        System.out.println("VISIBIL TO " + ctxLow.to + "," + visibilityTO);
        Element dom = md.getDominio(ctxLow.getDom(userRol));
        if (dom.getChild("PROPERTY_LIST") != null) {
            Iterator itr = dom.getChild("PROPERTY_LIST").getChildren("ATRIBUTO").
                           iterator();
            while (itr.hasNext()) {
                Element at = (Element) itr.next();
                access acc = new access(at.getAttributeValue("ACCESS"));
                if (!acc.getViewAccess()) {
                    System.out.println("VISIBIL TO DEC");
                    visibilityTO--;
                }
            }
        }
        return visibilityTO > visibilityFilter;
    }

    public int attributeCount(Element meta) {
        int count = 0;
        Iterator iPk = meta.getChild("ATRIBUTOS").getChildren("PACKET").
                       iterator();
        while (iPk.hasNext()) {
            Element pk = (Element) iPk.next();
            Iterator iAt = pk.getChildren("ATRIBUTO").iterator();
            while (iAt.hasNext()) {
                iAt.next();
                count++;
            }
        }
        return count;
    }

    public static int generateFusionDomId(int idA, int idB) {
        //Los 2 siguientes if estan en línea con el continue antes domB== domFefault de la funcion findFusionableSubContext
        if (idA == helperConstant.DOM_DEFAULT) {
            return idB;
        }
        if (idB == helperConstant.DOM_DEFAULT) {
            return idA;
        }
        //end nota
        System.out.println("GENERANDO " + idA + "," + idB + "->" +
                           (3000 + idA * 100 + idB));
        return 3000 + idA * 100 + idB;
    }


    public attributeContainer unnAdaptInstance(attributeContainer node) {
        try {
            int currIdctx=0;
            if( node instanceof ObjectProperty )
                currIdctx=((ObjectProperty)node).getIdoRel();
            if( node instanceof contextData )
                currIdctx=((contextData)node).getContextID();
            if (currIdctx != 0) {
                Integer idCtxFus = new Integer(currIdctx);
                if (contextIsFussioned(idCtxFus)) {
                    Iterator itr = node.getAttIterator(false,false);
                    while (itr.hasNext()) {
                        attribute item = (attribute) itr.next();
                        unnAdaptInstanceAt(idCtxFus, item);
                    }
                    if (node instanceof instance) {
                        Iterator iRel = ((instance)node).getRelationIterator(true);
                        while (iRel.hasNext()) {
                            relation child = (relation)iRel.next();
                            unnAdaptInstance(child);
                        }
                    }
                }
            }
            if (node instanceof relation && ((relation)node).getRelationInstance()!=null)
                    unnAdaptInstance(((relation)node).getRelationInstance());
            if( node instanceof instance ){
                Iterator itr = ((instance)node).getRelationIterator(true);
                while (itr.hasNext()) {
                    relation child = (relation) itr.next();
                    unnAdaptInstance(child);
                }
            }
            return node;
        } catch (DataErrorException de) {
            de.printStackTrace();
            return null;
        }
    }

    private void unnFusionAT(Integer tapos) throws DataErrorException {
        if (isAtFusioned(tapos)) {
            m_md.removeAt(tapos);
        }
    }

    private void unnAdaptInstanceAt(Integer ctxRoot, attribute item) throws DataErrorException {
        Integer tapos = new Integer(item.getTapos());
        if (isAtFusioned(tapos))
            item.setTapos(getAtMap(tapos).getTaposSource().intValue());
    }

    public void adaptaInstancia(Integer idCtx, instance atom, boolean extraction) {
        30/03/06 MESSAGE
        if (contextIsFussioned(idCtx)) {
            Contexto cc = m_md.getContext(idCtx);
            Integer toLow = new Integer(atom.getType());
            Integer toSup = new Integer(cc.to);

            atom.setAttribute("ID_FILTER", String.valueOf(cc.idFilter));
            atom.setAttribute("ID_CONTEXT", String.valueOf(cc.id));
            atom.setAttribute("ID_REL", String.valueOf(cc.idRel));
            atom.setAttribute("ROL_CURRENT", String.valueOf(cc.idRolCtx));
            atom.setAttribute("ROL_CHILD", String.valueOf(cc.idRolCurr));
            atom.setAttribute("SUPERIOR", (cc.childIsSup ? "TRUE" : "FALSE"));

            fusionRestoreData frd = (fusionRestoreData) m_fusionContextosLog.get(idCtx);

            frd.lowExtracted = extraction;
            frd.lowToId = toLow.intValue();

            atom.setAttribute("ID_TO", toSup.toString());
        }
    }

    public Element getOriginalTO(Integer to) {
        Iterator itr = m_fusionContextosLog.keySet().iterator();
        while (itr.hasNext()) {
            Integer id = (Integer) itr.next();
            fusionRestoreData frd = (fusionRestoreData) m_fusionContextosLog.
                                    get(id);
            if (frd.toRootId.equals(to)) {
                return frd.eTO;
            }
        }
        return m_md.getMetaTO(to);
    }

    public boolean fusionaInstancia(session ses,
                                    scope myScope,
                                    int empresa,
                                    docServer server,
                                    String user,
                                    Integer userRol,
                                    int operation,
                                    int currTask,
                                    Integer idCtx,
                                    attributeContainer atomClone) throws SystemException,
            RemoteSystemException, CommunicationException {

        //En docDatamodel los datos no se almacenan fusionados, por lo que llamaremos está funcion para construir una instancia fusionada
        // con otras instancias relacionadas, que originalmente aparecen como relación o parte.
        boolean hasBeenFusion = false;
        //System.out.println("FUSINST:"+idCtx+","+contextIsFussioned( idCtx ));
        if( atomClone.getAttributeValue("ID_CONTEXT")!=null)
                   jdomParser.print("INSTAN TO FUS:",atomClone);
        if (idCtx != null && contextIsFussioned(idCtx)) {
            //La siguiente adaptacion de AVA la realizo en el original, por que supongo que si ha habido
            hasBeenFusion = fusInst_processAvas(idCtx, atomClone);
            fusionRestoreData frd = (fusionRestoreData) m_fusionContextosLog.get(idCtx);
            ArrayList objToDel = new ArrayList();
            ArrayList objToFusion = new ArrayList();
            Contexto ctxLow = m_md.getContext(frd.ctxLow);
            if( atomClone instanceof instance ){
                if (((instance) atomClone).hasRelations(true))
                    fusInst_processRelations(operation,
                                             currTask,
                                             user,
                                             userRol,
                                             myScope,
                                             server,
                                             frd,
                                             objToDel,
                                             objToFusion,
                                             (instance) atomClone,
                                             ctxLow);

                Iterator itr = ((instance)atomClone).getEstructIterator(true);
                while (itr.hasNext()) {
                    relation item = (relation) itr.next();
                    if (item.getIDO() > 0 &&
                        m_md.isSpecializedFrom(item.getType(), ctxLow.to)) {
                        objToDel.add(item);
                        objToFusion.add(item);
                    }
                }
                for (int i = 0; i < objToFusion.size(); i++) {
                    hasBeenFusion = true;
                    relation od= (relation)objToDel.get(i);
                    instance parent= null;// od.getParent();
                    parent.delRelation(ses,new Integer(od.getIDO()));
                    fusionInstanceAT(ses,(instance)atomClone, (instance) objToFusion.get(i),frd.ctxLow);
                    fusionInstanceRel((instance)atomClone, (instance) objToFusion.get(i));
                }
            }
        }
        instance root= atomClone instanceof instance ? (instance)atomClone:null;
        if( atomClone instanceof relation && ((relation)atomClone).hasInstanceData(true) )
            root= ((relation)atomClone).getRelationInstance();
        if( root!=null ){
            Iterator itr = root.getRelationIterator(true);
            while (itr.hasNext()) {
                ObjectProperty child = (ObjectProperty) itr.next();
                Integer ctxChild = new Integer(child.getIdoRel());
                hasBeenFusion = hasBeenFusion |
                                fusionaInstancia(ses,
                                                 new scope(m_md, userRol,new access(access.VIEW), operation),
                                                 empresa,
                                                 server,
                                                 user,
                                                 userRol,
                                                 operation,
                                                 currTask,
                                                 ctxChild,
                                                 child);
            }
        }
        	if( atomClone.getAttributeValue("ID_CONTEXT")!=null)
         jdomParser.print("INSTAN TO FUS POST:"+hasBeenFusion,atomClone);
        return hasBeenFusion;
    }

    public void fusionaQueryResponse(Integer idCtx, selectData res) {
        Iterator itr = res.getIterator();
        while (itr.hasNext()) {
            instance obj = (instance) itr.next();
            fusInst_processAvas(idCtx, obj);
        }
    }

    private boolean fusInst_processAvas(Integer idCtx, attributeContainer atom) {
        boolean hasBeenFusioned = false;
        Contexto ctx = m_md.getContext(idCtx);
        Element fusFilter = m_md.getFilter(new Integer(ctx.idFilter));
        //jdomParser.print("FILTER:",fusFilter);
        Iterator itr = atom.getAttIterator(false,false);
        while (itr.hasNext()) {
            attribute ava = (attribute) itr.next();
            //jdomParser.print("FUS AVA:",ava);
            if (ava.getFusionRefSource() == 0) { //no ha sido adaptado
                // al buscar tengo que indicar el ta_pos porque en el nodo select del nuevo filter
                // fusionado source_ref puede no coincidir con su parent
                Element fusNode = jdomParser.findElementByAt(fusFilter,
                        "*",
                        "SOURCE_REF",
                        String.valueOf(ava.getVirtualREF()),
                        "TAPOS_SOURCE",
                        String.valueOf(ava.getTapos()),
                        true);
                //jdomParser.print("FUSNODE:",fusNode);
                if (fusNode != null) {
                    hasBeenFusioned = true;
                    ava.setFusionSource(ava.getTapos(), ava.getVirtualREF());
                    ava.setVirtualREF(Integer.parseInt(fusNode.getAttributeValue("REF")));
                    ava.setTapos(Integer.parseInt(fusNode.getAttributeValue("TA_POS")));
                }
            }
        }
        return hasBeenFusioned;
    }


    private void fusInst_processRelations(int operation,
                                          int currTask,
                                          String user,
                                          Integer userRol,
                                          scope myScope,
                                          docServer server,
                                          fusionRestoreData frd,
                                          ArrayList objToDel,
                                          ArrayList objToFusion,
                                          instance atom,
                                          Contexto ctxLow) throws
            SystemException, RemoteSystemException, CommunicationException {
        Iterator itr = atom.getRelationIterator(true);
        while (itr.hasNext()) {
            relation item = (relation) itr.next();
            if(m_md.isSpecializedFrom(item.getType(), ctxLow.to)) {
                scope lowScope = new scope(m_md,
                                           userRol,
                                           myScope.getAccess(frd.ctxLow),
                                           myScope.getOperation());
                boolean lock = ((operation == access.NEW ||
                                 operation == access.SET) &&
                                lowScope.getRootAccess().matches(access.NEW |
                        access.SET | access.DEL));
                //System.out.println("FUSGET");
                try{
                    instance objRel = server.serverGetInstance(lowScope,
                            user,
                            item.getType(),
                            item.getIDO(),
                            ctxLow.id,
                            currTask,
                            false,
                            false,
                            lock);
                    //jdomParser.print("FUSPOST GET",objRel);
                    objToDel.add(item);
                    objToFusion.add(objRel);
                }catch(InstanceLockedException il){
                    il.printStackTrace();//no puede generarse porque no se bloquea
                }
            }
        }
    }

    private void fusionInstanceAT(session ses,instance root, instance low, Integer idCtxLow) {
        //jdomParser.print("fusionInstance:low",low);
        try{
            idCtxLow = new Integer(m_md.getRootContext(idCtxLow).id);
            Iterator itr = low.getAttIterator(false,false);
            while (itr.hasNext()) {
                attribute item = (attribute) itr.next();
                attribute newItem = (attribute) item.clone();

                Integer taposSource = new Integer(item.getTapos());
                int fusTapos = getFusionAt(idCtxLow, taposSource);
                //here will be equals to tapos source
                if (taposSource.intValue() != helperConstant.TAPOS_RDN) {
                    newItem.setTapos(fusTapos);
                    root.addAttribute(ses,newItem, 0);
                }
            }
        }catch(DataErrorException e){
            e.printStackTrace();
        }
    }


    private void fusionInstanceRel(instance root, instance low) {
        if (low.hasRelations(true)) {
            Iterator itr = low.getRelationIterator(true);
            while (itr.hasNext()) {
                ObjectProperty item = (ObjectProperty)itr.next();
                ObjectProperty newItem = item;
                root.addRelation(null,newItem);//ya deshace unfusion en un cancel
            }
        }
    }

    public void fusionaTO(fusionRestoreData frd, processEnableDom proEnable,
                          int toSup, int toLow) {

        Integer idToSup = new Integer(toSup);
        Integer idToLow = new Integer(toLow);
        //m_fusionLogTO.put( idToSup, frd );

        frd.eTO = m_md.getMetaTO(idToSup);
        frd.toRootId = idToSup;

        Element eSup = (Element) frd.eTO.clone();
        eSup.setAttribute("FUSIONED", "TRUE");
        //System.out.println("FUSTO: SUP, LOW:"+ toSup+","+toLow);

        ArrayList toDelete = new ArrayList();
        Iterator itr = eSup.getChild("RELACIONES").getChildren("RELACION").
                       iterator();
        while (itr.hasNext()) {
            Element rel = (Element) itr.next();
            Element rolChild = rel.getChild("ROL");
            Integer toRol = new Integer(rolChild.getAttributeValue("TO"));

            if (m_md.isSpecializedFrom(idToLow, toRol) ||
                m_md.isSpecializedFrom(toRol, idToLow)) {
                toDelete.add(rel);
            }
        }

        for (int i = 0; i < toDelete.size(); i++) {
            ((Element) toDelete.get(i)).detach();
        }

        Element eLow = m_md.getMetaTO(idToLow);

        itr = eLow.getChild("ATRIBUTOS").getChildren("PACKET").iterator();
        while (itr.hasNext()) {
            Element pack = (Element) itr.next();
            int idPk = Integer.parseInt(pack.getAttributeValue("ID"));
            Element packSup = jdomParser.findElementByAt(eSup.getChild(
                    "ATRIBUTOS"),
                    "PACKET",
                    "ID",
                    String.valueOf(idPk),
                    false);
            if (packSup == null) {
                packSup = new Element("PACKET");
                packSup.setAttribute("ID", pack.getAttributeValue("ID"));
                eSup.getChild("ATRIBUTOS").addContent(packSup);
            }

            Iterator iAt = pack.getChildren("ATRIBUTO").iterator();
            while (iAt.hasNext()) {
                Element at = (Element) iAt.next();
                Element newAt = (Element) at.clone();
                int tapos = Integer.parseInt(at.getAttributeValue("TA_POS"));
                Element atSup = jdomParser.findElementByAt(eSup.getChild(
                        "ATRIBUTOS"),
                        "ATRIBUTO",
                        "TA_POS",
                        String.valueOf(tapos),
                        true);
                tapos = fusionaAT(frd, tapos);
                newAt.setAttribute("TA_POS", String.valueOf(tapos));
                packSup.addContent(newAt);
                proEnable.addPropertie(tapos);
            }
        }

        itr = eLow.getChild("RELACIONES").getChildren("RELACION").iterator();
        while (itr.hasNext()) {
            Element rel = (Element) itr.next();
            Element rolChild = rel.getChild("ROL");
            Integer toRol = new Integer(rolChild.getAttributeValue("TO"));

            //System.out.println("FUSTO: ROL:"+toRol);
            if (!m_md.isSpecializedFrom(idToSup, toRol) &&
                !m_md.isSpecializedFrom(toRol, idToSup)) {
                System.out.println("SE añade");
                eSup.getChild("RELACIONES").addContent((Element) rel.clone());
            } else {
                System.out.println("SALTAMOS");
            }
        }

        m_md.m_metaTOs.put(idToSup, eSup);
        frd.herencias = (HashMap) ((HashMap) m_md.m_herencias.get(idToSup)).clone();
        m_md.addRecursiveSuperiorMap(toSup, toLow);
    }

    public int getATSource(Integer fusTapos) throws DataErrorException {
        if (!isAtFusioned(fusTapos)) {
            return fusTapos.intValue();
        } else {
            atMapData data = getAtMap(fusTapos);
            return data.getTaposSource().intValue();
        }
    }

    private int fusionaAT(fusionRestoreData frd, int tapos) {
        Integer taposSource = new Integer(tapos);
        //los atributos fusionados son negativos
        Integer newTapos = new Integer(m_virtualAtIndex--);

        addFusionAt(frd, taposSource, newTapos);
        int tm = m_md.getID_TM(taposSource);

        m_md.addATLabel(newTapos.intValue(), m_md.getATLabel(taposSource));
        m_md.addTMmap(newTapos.intValue(), tm);
        if (tm == helperConstant.TM_ENUMERADO) {
            m_md.m_mapEnum.put(newTapos,
                               ((HashMap) m_md.m_mapEnum.get(taposSource)).
                               clone());
        }
        return newTapos.intValue();
    }

    public void fusionaFilter(fusionRestoreData frd, int filterSup,
                              int filterLow) throws DataErrorException {
        Integer idFSup = new Integer(filterSup);
        Element fSup = m_md.getFilter(idFSup);
        frd.sourceFilter = (Element) fSup.clone();

        fSup = (Element) fSup.clone();

        Integer idFLow = new Integer(filterLow);
        Element fLow = (Element) m_md.getFilter(idFLow).clone();

        //m_fusionFilterLog.put( idFSup, idFLow );

        fusionaFilter(m_md, fSup, fLow);
        Iterator iAt = jdomParser.elementsWithAt(fSup,
                                                 "TA_POS",
                                                 false).iterator();
        //arriba no debo profundizar por que ya este primer nivel es el unico que ya incluye nodos añadidos fusionados
        while (iAt.hasNext()) {
            Element at = (Element) iAt.next();
            Integer taposSource = new Integer(at.getAttributeValue("TA_POS"));
            at.setAttribute("TAPOS_SOURCE", taposSource.toString());

            if (at.getAttributeValue("FUSIONADO") != null &&
                at.getAttributeValue("FUSIONADO").equals("TRUE")) {
                at.setAttribute("TA_POS",
                                String.valueOf(getFusionAt(frd.ctxLow, taposSource)));
            }
        }

        m_md.addFilter(fSup, filterSup);
    }

    public static void fusionaFilter(metaData md, Element fSup, Element fLow) {
        int toLow = Integer.parseInt(fLow.getAttributeValue("ID_TO"));

        //Se usa hide para que al pinchar en Ejecutar no lo procese
        //en esta fusion se supone que el filter low a fusionar es compatible con el superior.
        //es decir, existe un nodo child en el filter superior que está especializado del nodo root del low
        fLow.setAttribute("HIDE", "TRUE");
        jdomParser.print("FSUP", fSup);
        jdomParser.print("FLOW", fLow);
        ArrayList lista = jdomParser.getFilterAtList(fLow, false);
        for (int i = 0; i < lista.size(); i++) {
            Element at = (Element) lista.get(i);
            int refEmbed = Integer.parseInt(at.getAttributeValue("REF"));
            int tapos = Integer.parseInt(at.getAttributeValue("TA_POS"));
            int sourceRef = md.findEmbedAtInRoot(fSup, tapos, refEmbed, fLow);
            System.out.println("FUSFILTER EMBED " + sourceRef + "," + tapos +
                               "," + refEmbed);
            if (sourceRef != -1) {
                Element lowNode = jdomParser.findElementByAt(fLow,
                        "FILTER",
                        "REF",
                        String.valueOf(refEmbed),
                        true,
                        true);
                lowNode.setAttribute("SOURCE_REF", String.valueOf(sourceRef));
                Iterator iT = lowNode.getChildren().iterator();
                while (iT.hasNext()) {
                    Element item = (Element) iT.next();
                    if (item.getAttributeValue("TA_POS") != null) {
                        item.setAttribute("SOURCE_REF",
                                          String.valueOf(sourceRef));
                    }
                }
            }
        }

        Iterator itr = fSup.getChildren("FILTER").iterator();
        ArrayList aFusionar = new ArrayList();
        while (itr.hasNext()) {
            Element child = (Element) itr.next();
            int to = Integer.parseInt(child.getAttributeValue("ID_TO"));
            System.out.println("PRE A FUSIONAR " + to + "," + toLow + "," +
                               child.getAttributeValue("REF") + "," +
                               md.isSpecializedFrom(to, toLow));
            if (md.isSpecializedFrom(to, toLow)) {
                aFusionar.add(new Integer(child.getAttributeValue("REF")));
            }
        }
        for (int i = 0; i < aFusionar.size(); i++) {
            Integer ref = (Integer) aFusionar.get(i);

            Element child = jdomParser.findElementByAt(fSup,
                    "FILTER",
                    "REF",
                    ref.toString(),
                    false);
            fSup.removeContent(child);
            System.out.println("ASIGN REF:" + ref);
            fSup.setAttribute("REF_FUSIONADO", ref.toString()); //solo valido si solo se fusionan 2
            if (child.getAttributeValue("OID") != null) {
                fSup.setAttribute("OID_FUSIONADO",
                                  child.getAttributeValue("OID"));
            }
        }
        Iterator iSub = fLow.getChildren().iterator();
        while (iSub.hasNext()) {
            Element sub = (Element) ((Element) iSub.next()).clone();
            if (sub.getAttributeValue("TA_POS") != null &&
                Integer.parseInt(sub.getAttributeValue("TA_POS")) !=
                helperConstant.TAPOS_RDN) {

                sub.setAttribute("FUSIONADO", "TRUE");
                //esto lo utilizara para adaptar despues el tapos,ahora no puedo por ser estatico
                if (sub.getName().equals("VIRTUAL")) {
                    sub.setName("SELECT");
                }
                fSup.addContent(sub);
            }
        }
        jdomParser.print("FSUP POST", fSup);
        //reindexFilter( fSup, 0 );
    }

    private fusionRestoreData findRestoreData(Element node) {
        if (node.getAttributeValue("TA_POS") != null) {
            Integer tapos = new Integer(node.getAttributeValue("TA_POS"));
            try {
                if (isAtFusioned(tapos)) {
                    return getAtMap(tapos).getRestoreData();
                }
            } catch (DataErrorException e) {
                e.printStackTrace();
            }
        }
        Iterator itr = node.getChildren().iterator();
        while (itr.hasNext()) {
            Element child = (Element) itr.next();
            fusionRestoreData frd = findRestoreData(child);
            if (frd != null) {
                return frd;
            }
        }
        return null;
    }

    private Element findSourceFilterNode(Element node, Element sourceFilter) {
        String[] labels = {"QUERY", "FILTER"};
        int ref = Integer.parseInt((node.getAttributeValue("SOURCE_REF") != null ?
                                    node.getAttributeValue("SOURCE_REF") :
                                    node.getAttributeValue("REF")));
        Element sourceNode = jdomParser.findElementByAt(sourceFilter,
                labels,
                "REF",
                String.valueOf(ref),
                true,
                true);
        if (sourceNode == null) { //puede haber sido incrustado despues de la fusion
            Element container = node.getParent();
            if (container != null) {
                Element containerSource = findSourceFilterNode(container,
                        sourceFilter);
                containerSource.addContent((Element) node.clone());
            }
            return null;
        } else {
            return sourceNode;
        }
    }

    public Element unnAdaptFilter(Element filter) {
        fusionRestoreData frd = findRestoreData(filter);
        //System.out.println("UNNADAPTFILTER "+frd);
        if (frd == null) {
            return filter;
        } else {
            Element sourceFilter = (Element) frd.sourceFilter.clone();
            //jdomParser.print("FILTER",filter);
            //jdomParser.print("SOURCE",sourceFilter);
            Iterator itr = jdomParser.elementsWithAt(filter, "ID_O", true).
                           iterator();
            while (itr.hasNext()) {
                Element node = (Element) itr.next();
                Element sourceNode = findSourceFilterNode(node, sourceFilter);
                //System.out.println("DBG IDO2" +sourceNode);
                if (sourceNode != null) {
                    sourceNode.setAttribute("ID_O",
                                            node.getAttributeValue("ID_O"));
                }
            }
            itr = jdomParser.elementsWithAt(filter, "TA_POS", true).iterator();
            while (itr.hasNext()) {
                Element at = (Element) itr.next();
                try {
                    Integer tapos = new Integer(at.getAttributeValue("TA_POS"));
                    if (isAtFusioned(tapos)) {
                        //System.out.println("DBG AT");
                        Integer taposSrc = getAtMap(tapos).getTaposSource();
                        Element node = at.getParent();
                        Element sourceNode = findSourceFilterNode(at,
                                sourceFilter);
                        if (sourceNode != null) {
                            //System.out.println("DBG AT2");
                            Element oldAt = jdomParser.findElementByAt(
                                    sourceNode,
                                    "*",
                                    "TA_POS",
                                    taposSrc.toString(),
                                    false);
                            if (oldAt != null) {
                                oldAt.detach();
                            }
                            at = (Element) at.clone();
                            at.setAttribute("TA_POS", taposSrc.toString());
                            if (Integer.parseInt(at.getAttributeValue(
                                    "SOURCE_REF")) > 0) {
                                at.setName("VIRTUAL");
                            }
                            sourceNode.addContent(at);
                        }
                    }
                } catch (DataErrorException e) {
                    e.printStackTrace();
                }
            }
            return sourceFilter;
        }
    }

    private static void reindexFilter(Element node, int nextRef) {
        if (node.getName().equals("FILTER")) {
            if (node.getAttributeValue("SOURCE_REF") == null) {
                node.setAttribute("SOURCE_REF", node.getAttributeValue("REF"));
            }

            node.setAttribute("REF", String.valueOf(nextRef));
            Iterator itr = node.getChildren().iterator();
            while (itr.hasNext()) {
                Element item = (Element) itr.next();
                if (item.getAttributeValue("TA_POS") != null &&
                    item.getAttributeValue("SOURCE_REF") == null) {

                    item.setAttribute("SOURCE_REF",
                                      node.getAttributeValue("REF"));
                    item.setAttribute("REF", String.valueOf(nextRef));
                }
            }
        }
        Iterator itr = node.getChildren("FILTER").iterator();
        while (itr.hasNext()) {
            Element item = (Element) itr.next();
            reindexFilter(item, nextRef + 1);
        }
    }

    public static void incrustaFilter(Element nodo, Element eChild) { //solo valido para configuracion previa a la adaptacion
        nodo.setAttribute("ID_TO", eChild.getAttributeValue("ID_TO"));

        if (nodo.getAttributeValue("OID") == null &&
            eChild.getAttributeValue("OID") != null) {
            nodo.setAttribute("OID", eChild.getAttributeValue("OID"));
        }

        if (nodo.getAttributeValue("REF") == null &&
            eChild.getAttributeValue("REF") != null) {
            nodo.setAttribute("REF", eChild.getAttributeValue("REF"));
        }
        if (nodo.getAttributeValue("FILTER_NAME") == null &&
            eChild.getAttributeValue("FILTER_NAME") != null) {
            nodo.setAttribute("FILTER_NAME",
                              eChild.getAttributeValue("FILTER_NAME"));
        }
        ArrayList selects = new ArrayList();
        Iterator iSub = eChild.getChildren().iterator();
        while (iSub.hasNext()) {
            Element sub = (Element) ((Element) iSub.next()).clone();
            sub.setAttribute("ADAPTED", "TRUE");
            nodo.addContent(sub);
            if (sub.getName().equals("SELECT")) {
                selects.add(sub);
            }
        }

        for (int s = 0; s < selects.size(); s++) {
            ((Element) selects.get(s)).setName("VIRTUAL");
        }
    }

    public static void incrustaFilter(metaData md, Element nodo, Element eChild) {
        nodo.setAttribute("ID_TO", eChild.getAttributeValue("ID_TO"));

        if (nodo.getAttributeValue("OID") == null &&
            eChild.getAttributeValue("OID") != null) {
            nodo.setAttribute("OID", eChild.getAttributeValue("OID"));
        }

        if (nodo.getAttributeValue("REF") == null &&
            eChild.getAttributeValue("REF") != null) {
            nodo.setAttribute("REF", eChild.getAttributeValue("REF"));
        }

        if (nodo.getAttributeValue("ROOT_SCOPE") == null &&
            eChild.getAttributeValue("ROOT_SCOPE") != null) {
            nodo.setAttribute("ROOT_SCOPE",
                              eChild.getAttributeValue("ROOT_SCOPE"));
        }

        int toCtx = Integer.parseInt(nodo.getParent().getAttributeValue("ID_TO"));
        int toCurr = Integer.parseInt(nodo.getAttributeValue("ID_TO"));
        md.hallaRelacion(nodo, toCurr, toCtx);
        ArrayList selects = new ArrayList();
        Iterator iSub = eChild.getChildren().iterator();
        while (iSub.hasNext()) {
            Element sub = (Element) ((Element) iSub.next()).clone();
            sub.setAttribute("ADAPTED", "TRUE");
            nodo.addContent(sub);
            if (sub.getName().equals("SELECT")) {
                selects.add(sub);
            }
        }

        for (int s = 0; s < selects.size(); s++) {
            ((Element) selects.get(s)).setName("VIRTUAL");
        }
    }

    public Element fusionaContextos(scope parentScope, Contexto ctxRoot,
                                    Contexto ctxLow, Integer userRol) {
        access childAccess = parentScope.getAccess(new Integer(ctxRoot.id));
        scope rootScope = new scope(m_md, parentScope.getUserRol(), childAccess,
                                    parentScope.getOperation());
        fusionRestoreData frd = new fusionRestoreData();
        Integer idCtxRoot = new Integer(ctxRoot.id);
        frd.ctxRoot = idCtxRoot;
        frd.ctxLow = new Integer(ctxLow.id);
        fusionaTO(frd,
                  new processEnableDom(parentScope,
                                       m_md,
                                       m_fusionDomLog,
                                       ctxRoot,
                                       ctxLow),
                  ctxRoot.to,
                  ctxLow.to);

        int domA = ctxRoot.getDom(userRol) == null ? helperConstant.DOM_DEFAULT :
                   ctxRoot.getDom(userRol).intValue();
        int domB = ctxLow.getDom(userRol) == null ? helperConstant.DOM_DEFAULT :
                   ctxLow.getDom(userRol).intValue();

        int newDomId = domA == domB ? domA : generateFusionDomId(domA, domB);

        frd.rootDomSource = new Integer(domA);
        frd.userRol = userRol;

        ctxRoot.setDom(userRol,
                       newDomId);

        //System.out.println("FUSDOM:ctx,rol,dom:"+ctxRoot.id+","+userRol+","+newDomId);
        try {
            fusionaFilter(frd, ctxRoot.idFilter, ctxLow.idFilter);
        } catch (DataErrorException e) {
            e.printStackTrace();
        }
        ctxRoot.fusionado = true;

        System.out.println("FUSIONANDO CTX:"+ctxRoot.id);

        Element eNewCtx = parentScope.subGetContext(userRol, ctxRoot, true);
        Element newFilter = eNewCtx.getChild("FILTER");

        newFilter.setAttribute("FUSION", "TRUE");
        newFilter.setAttribute("FUSION_LOW_CONTEXT", String.valueOf(ctxLow.id));

        //m_fusionLogTO_low.put( new Integer(ctxLow.to), frd );
        m_fusionContextosLog.put(idCtxRoot, frd);
        frd.lowToId = ctxLow.to;
        // System.out.println("FUSIONTOLOW PUT:"+ctxLow.to);
        return eNewCtx;
    }

    public Integer getCtxLowFromTO_low(Integer toLow) {
        Iterator itr = m_fusionContextosLog.keySet().iterator();
        while (itr.hasNext()) {
            Integer idCtx = (Integer) itr.next();
            fusionRestoreData frd = (fusionRestoreData) m_fusionContextosLog.
                                    get(idCtx);
            if (m_md.isSpecializedFrom(toLow, new Integer(frd.lowToId))) {
                return frd.ctxLow;
            }
        }
        return null;
    }

    public static Element fusionaDominios(Element domA, Element domB,
                                          boolean registrar) {
        Element res = (Element) domA.clone();
        if (res.getChild("PROPERTY_LIST") == null) {
            res.addContent(new Element("PROPERTY_LIST"));
        }
        if (domB.getChild("PROPERTY_LIST") != null) {
            Iterator itr = domB.getChild("PROPERTY_LIST").getChildren(
                    "ATRIBUTO").iterator();
            while (itr.hasNext()) {
                Element propB = (Element) itr.next();
                String tapos = propB.getAttributeValue("TA_POS");
                Element peer = jdomParser.findElementByAt(res.getChild(
                        "PROPERTY_LIST"),
                        "ATRIBUTO",
                        "TA_POS",
                        tapos,
                        false);
                if (peer != null) {
                    peer.detach();
                }

                res.getChild("PROPERTY_LIST").addContent((Element) propB.clone());
            }
        }
        if (res.getChild("POLIMORFISMO") == null) {
            res.addContent(new Element("POLIMORFISMO"));
        }
        if (domB.getChild("POLIMORFISMO") != null) {
            Iterator itr = domB.getChild("POLIMORFISMO").getChildren("REST").
                           iterator();
            while (itr.hasNext()) {
                Element restB = (Element) itr.next();
                res.getChild("POLIMORFISMO").addContent((Element) restB.clone());
            }
        }
        //falta hacer un put de dominio si registrar
        return res;
    }

    public int fusionaDominios(int idDomA, int idDomB) {
        //esta funcion presupone dominios disjuntos y presupone que en las funciones
        //todas las referencias desde un contexto superior se hacen respecto al filtro A.

        Element res = null;
        if (idDomA != 0 && idDomB == 0) {
            res = m_md.getDominio(new Integer(idDomB));
        }
        if (idDomA == 0 && idDomB != 0) {
            res = m_md.getDominio(new Integer(idDomB));
        }
        if (idDomA == 0 && idDomB == 0) {
            return 0;
        }

        if (idDomA != 0 && idDomB != 0) {
            //System.out.println("DOMA, DOMB:"+idDomA+","+idDomB );
            Element domA = m_md.getDominio(new Integer(idDomA));
            Element domB = m_md.getDominio(new Integer(idDomB));
            res = fusionaDominios(domA, domB, false);
        }
        int newId = generateFusionDomId(idDomA, idDomB);
        m_md.m_dominios.put(new Integer(newId), res);
        return newId;
    }


    public ArrayList getOriginalContexts(Integer ctxId) {
        ArrayList res = new ArrayList();
        if (contextIsFussioned(ctxId)) {
            fusionRestoreData frd = (fusionRestoreData) m_fusionContextosLog.
                                    get(ctxId);
            res.add(frd.ctxRoot);
            res.add(frd.ctxLow);
        }
        return res;
    }

    public Integer getLowContextFromAT(Integer tapos) throws DataErrorException {
        //Parametros de entrada diseñado para soportar la fusion de más de 2 contextos
        if (isAtFusioned(tapos)) {
            return getAtMap(tapos).getCtxLow();
        } else {
            throw new DataErrorException("CONTEXT NOT FUSIONED");
        }
    }

    public boolean filterIsFussioned( Integer filterId ){
      return m_fusionFilterLog.containsKey( filterId );
        }

    public boolean ctxIsLowFussioned(Integer ctxId) {
        Iterator itr = m_fusionContextosLog.keySet().iterator();
        while (itr.hasNext()) {
            Integer ctxRootId = (Integer) itr.next();
            fusionRestoreData frd = (fusionRestoreData) m_fusionContextosLog.
                                    get(ctxRootId);
            if (frd.ctxLow.equals(ctxId)) {
                return true;
            }
        }
        return false;
    }

    public boolean contextIsFussioned(Integer ctxId) {
        if (ctxId == null) {
            return false;
        }
        return m_fusionContextosLog.containsKey(ctxId);
    }

    private fusionRestoreData getFusionData(Integer idCtxRoot) {
        Contexto ctxRoot = m_md.getRootContext(idCtxRoot);
        if (ctxRoot == null) {
            System.out.println("GETFUSDATA ROOT null:" + idCtxRoot);
            return (fusionRestoreData) m_fusionContextosLog.get(idCtxRoot);
        } else {
            if (ctxRoot.id != idCtxRoot.intValue()) {
                idCtxRoot = new Integer(ctxRoot.id);
            }
            System.out.println("GETFUSDATA:" + idCtxRoot + "," + ctxRoot.id);
            return (fusionRestoreData) m_fusionContextosLog.get(idCtxRoot);
        }
    }


     public boolean contextHasFussionedPeer( Integer ctxId ){
      return m_fusionContextosLogReverse.containsKey( ctxId );
     }
     public Integer getContextFusionedPeer( Integer ctxId ){
      if( !contextHasFussionedPeer( ctxId ) )
       return null;
      return (Integer)m_fusionContextosLogReverse.get( ctxId );
     }
}
*/