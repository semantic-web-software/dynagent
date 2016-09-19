package dynagent.ejb.old;
/*package dynagent.ejb;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.naming.NamingException;
import javax.swing.JOptionPane;

import org.jdom.Element;

import dynagent.application.session;
import dynagent.application.sessionable;
import dynagent.communication.docServer;
import dynagent.knowledge.Contexto;
import dynagent.knowledge.access;
import dynagent.knowledge.metaData;
import dynagent.knowledge.scope;
import dynagent.knowledge.instance.attribute;
import dynagent.knowledge.instance.attributeContainer;
import dynagent.knowledge.instance.contextData;
import dynagent.knowledge.instance.extendedValue;
import dynagent.knowledge.instance.instance;
import dynagent.knowledge.instance.InstanceLockedException;
import dynagent.knowledge.instance.range;
import dynagent.knowledge.instance.relation;
import dynagent.knowledge.instance.relationListener;
import dynagent.knowledge.instance.selectData;
import dynagent.ruleengine.meta.api.ObjectProperty;

public class docDataModel implements domDataModel, relationListener {

    //HashMap m_tapos= new HashMap();
    metaData m_md = null;
    HashMap m_filterMap;
    private instance m_instance;
    docServer m_server;
    int m_currTask;
    int m_idTo;
    Contexto m_ctx = null;
    Element m_contextosDeRelacion = null;

    //HashMap m_objetos=new HashMap();
    HashMap m_objDocs = new HashMap();
    public HashMap m_filtrosDeRelacion = new HashMap();
    appControl m_control;
    docDataModel m_parent;
    public selectData m_threadData;
    String m_user;
    public ArrayList m_cambios = new ArrayList();
    boolean m_inicializado = false, m_serverSide;
    public Integer m_userRol;
    scope m_currentScope;
    HashMap m_operationScope = new HashMap();

    public Object clone() {
        docDataModel res = new docDataModel((instance) m_instance.clone());
        copyData(res, this);
        return res;
    }

    public static void copyData( docDataModel res, docDataModel input ){
        //res.m_tapos
        res.m_md = input.m_md;
        res.m_ctx = input.m_ctx;
        res.m_filterMap = (HashMap)metaData.cloneObject(input.m_filterMap);
        res.m_server = input.m_server;
        res.m_currTask = input.m_currTask;
        res.m_idTo = input.m_idTo;
        res.m_contextosDeRelacion = (Element) input.m_contextosDeRelacion.clone();
        //res.m_objetos=(HashMap)m_md.cloneObject(m_objetos);
        res.m_objDocs = (HashMap)metaData.cloneObject(input.m_objDocs);
        jdomParser.indexaNodesPorIntAt(res.m_filtrosDeRelacion,
                                       res.m_contextosDeRelacion,
                                       "FILTER",
                                       "ID",
                                       true);
       // res.m_filtrosDeRelacion = (HashMap)metaData.cloneObject(input.m_filtrosDeRelacion);
        res.m_control = input.m_control;
        if( input.m_threadData!=null )
            res.m_threadData = (selectData)input.m_threadData.clone();
        res.m_user = input.m_user;
        res.m_inicializado = input.m_inicializado;
        res.m_serverSide = input.m_serverSide;
        res.m_userRol = new Integer(input.m_userRol.intValue());
        res.m_currentScope = (scope)input.m_currentScope.clone();
        res.m_operationScope = (HashMap)metaData.cloneObject(input.m_operationScope);
        res.m_parent = input.m_parent;
    }

    docDataModel(instance instance) {
        setInstance(instance);
    }

    public docDataModel(boolean serverSide,
                        String user,
                        Integer userRol,
                        docDataModel parent,
                        appControl control,
                        metaData md,
                        Contexto ctx,
                        int idto,
                        instance ins,
                        session ses,
                        int operation,
                        access myAccess,
                        docServer server,
                        int currTask)
            throws SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException{
        this(serverSide,
             user,
             userRol,
             parent,
             control,
             md,
             ctx,
             idto,
             ins,
             ses,
             new scope(md, userRol, myAccess, operation),
             server,
             currTask);
    }

    public docDataModel(boolean serverSide,
                        String user,
                        Integer userRol,
                        docDataModel parent,
                        appControl control,
                        metaData md,
                        Contexto ctx,
                        int idto,
                        instance ins,
                        session ses,
                        scope myScope,
                        docServer server,
                        int currTask) throws SystemException,
            ApplicationException, RemoteSystemException, CommunicationException, DataErrorException {
        m_ctx = ctx;
        m_currentScope = myScope;
        m_operationScope.put(new Integer(myScope.getOperation()),
                             m_currentScope);
        m_user = user;
        m_userRol = userRol;
        m_serverSide = serverSide;
        m_parent = parent;
        if (m_parent != null)
            m_threadData = m_parent.m_threadData;
        m_md = md;
        m_control = control;
        m_idTo = idto;
        m_currTask = currTask;
        m_instance = ins;
        m_server = server;

        Iterator iRel = ins.getRelationIterator(ins.getIDO());
        while (iRel.hasNext()) {
            ObjectProperty rel = (ObjectProperty) iRel.next();
            subUpdateRelMaps(ses,rel);
        }
        //System.out.println("DOC, pre server");
        if (server != null) {
            System.out.println("DOC, server not null, rol " + m_userRol + "," +
                               m_idTo);

            m_contextosDeRelacion = m_currentScope.getContexts(m_idTo,
                    helperConstant.TO_BASE,
                    m_ctx.action,
                    true);
            jdomParser.indexaNodesPorIntAt(m_filtrosDeRelacion,
                                           m_contextosDeRelacion, "FILTER",
                                           "ID", true);
            try {
                if (m_parent == null && m_currTask != 0) {
                    m_threadData = server.getThreadData(0, 0, m_currTask);
                }
            } catch (NamingException ne) {
                throw new SystemException(SystemException.ERROR_SISTEMA,
                                          ne.getMessage());

            }
            Iterator itr = m_contextosDeRelacion.getChildren("CONTEXT").
                           iterator();
            while (itr.hasNext()) {
                Element eCtx = (Element) itr.next();
                Integer idCtx = new Integer(eCtx.getAttributeValue("ID"));
                Contexto cc = m_md.getContext(idCtx);
                if (cc.idFilter == 0) {
                    continue;
                }
                Element eFilter = eCtx.getChild("FILTER");
                //System.out.println("DOC1:"+idCtx);
                fixThreadData(m_md, cc.filterFix, eFilter, m_threadData);
            }
            updateTransitiveData(ses,false);
            m_inicializado = true;
            try{
             System.out.println("CTXS:"+jdomParser.returnXML( m_contextosDeRelacion ) );
                }catch(Exception e){;}
        }
    }

    public void setMetadata(metaData md) {
        m_md = md;
    }

    public docDataModel getRootDocument() {
        if (m_parent == null) {
            return this;
        }
        return m_parent.getRootDocument();
    }

    public docDataModel getParentDocument() {
        return m_parent;
    }

    public String getRDN() {
        return m_instance.getRdn();
    }


    private void subUpdateRelMaps( Element item, String sido, String sto, String ctx, String filter ){
     Integer ido= new Integer(sido);
     Integer to= new Integer(sto);
     Integer ctxId= ctx==null ? null:new Integer(ctx);
     Integer fId= filter==null ? null:new Integer(filter);
     subUpdateRelMaps( item, ido, to, ctxId, fId );
      }

    private void subUpdateRelMaps(session ses, ObjectProperty item) {
        //m_objetos.put( ido, item );
        //Integer ctxId = new Integer(item.getContextID());
        // Contexto ctx = m_md.getContext(ctxId);
        // item.addRelationListener(this);
        m_instance.addRelation(ses, m_instance.getIDO(), item);
    }

     private void subUpdateRelMaps( instance item, Integer ido, Integer to, Integer ctxId, Integer filterId ){
             //m_objetos.put( ido, item );
             Contexto ctx= m_md.getContext(ctxId);
             relation rel= new relation(ctx,ido.intValue(),to.intValue(),m_md.getCategoriaRel(ctx.idRel));
             rel.setRelationInstance(item);
             m_instance.addRelation( rel );
             m_TOs.put( ido, to );
             if( ctxId!=null ){
                 m_objectCtx.put( ido, ctxId );
                 m_objectFilter.put( ido, filterId );
             }
     }


    public int getIdTo() {
        return m_idTo;
    }

    public ArrayList getSuperiorObjectSet(session ses,
                                          Integer idto,
                                          boolean justDocumentsNotRels)
            throws SystemException, ApplicationException, RemoteSystemException,CommunicationException,DataErrorException {
        System.out.println( "DOCDM TOs size "+m_TOs.size()+","+idto);
           try{
         System.out.println("INST "+jdomParser.returnXML( m_instance ) );
           }catch(Exception e){;}
        Iterator itr= m_instance.getRelationIterator(false);
        ArrayList res = new ArrayList();
        while (itr.hasNext()) {
            ObjectProperty posible = (ObjectProperty)itr.next();
            Integer ido=new Integer(posible.getIdProp());
            Integer to = new Integer(posible.getTypeProperty());
            boolean esDoc = posible.getCategory().isStructural(); // () ==  helperConstant.CAT_ESTRUCTURAL;
            if (m_md.isSpecializedFrom(to, idto) ){
                if (!justDocumentsNotRels ||
                    justDocumentsNotRels &&
                    esDoc)
                    res.add(ido);
                    try{
                     System.out.println("AñadO "+jdomParser.returnXML(getObject(ido) ) );
                         }catch(Exception e){;}
                else
                    if (esDoc) {
                        docDataModel doc = getSubDoc(ses, ido,false);
                        res.addAll(doc.getSuperiorObjectSet(ses, idto, justDocumentsNotRels));
                    }
             }
        }
        return res;
    }

    public static ArrayList fixThreadData(metaData md,ArrayList fixList,Element eFilter,selectData thrData) {
        ArrayList res = new ArrayList();
        Iterator iF = fixList.iterator();
        while (iF.hasNext()) {
            fixProperties fp = (fixProperties) iF.next();
            //if( fp.optional ) continue;
            //System.out.println("DOC2:"+fp.filterNode);
            if (fp.ctxFix != 0) {
                Integer idCtxFix = new Integer(fp.ctxFix);
                Iterator iSub = jdomParser.findElementsByAt(eFilter,
                        "FILTER",
                        "OID",
                        fp.filterNode,
                        true).iterator();
                while (iSub.hasNext()) {
                    Element subNodeF = (Element) iSub.next();
                    subNodeF.setAttribute("CONTEXT", String.valueOf(fp.ctxFix));
                    //System.out.println("DOC3:");
                    contextData thrItem = null;
                    if(thrData != null) {
                        Iterator iT = thrData.getIterator();
                        while (iT.hasNext()) {
                            contextData item = (contextData) iT.next();
                            Integer thCtx = new Integer(item.getContextID());
                            Contexto thCc = md.getContext(thCtx);
                            if(md.ctxIsSpecializedFrom(thCtx, idCtxFix)){
                                thrItem = item;
                                break;
                            }
                        }
                    }
                    if (thrItem != null &&
                        thrItem.getIDO()!= 0) {
                        subNodeF.setAttribute("ID_O",String.valueOf(thrItem.getIDO()));
                        subNodeF.setAttribute("RDN",thrItem.getRdn());
                        res.add(new fixData(fp,thrItem.getIDO()));
                        if (fp.optional) {
                            subNodeF.setAttribute("OPTIONAL_FIX", "TRUE");
                        }
                    }
                }
            }
        }
        return res;
    }

    public Element getContextos() {
        return m_contextosDeRelacion;
    }

    private void setInstance(instance instance) {
        m_instance = instance;
    }

    public Integer getContexto(Integer ido) {
        if(ido.intValue() == -1)
            return new Integer(m_ctx.id);
        else
            return new Integer(m_instance.getRelation(ido).getIdoRel());
    }

    public boolean fieldSupported(String campo) {
        return true;
    }

    public Integer getTO(int object) {
        ObjectProperty rel= m_instance.getRelation(object);
        if( rel==null ) return null;
        else
            return new Integer(rel.getTypeProperty());
    }

    public ObjectProperty getObject(Integer object) {
        return m_instance.getRelation(object.intValue());
    }

    public ObjectProperty findObject(session ses,Integer object, boolean justDocumentsNotRels) throws
            RemoteSystemException, CommunicationException,DataErrorException {
        try {
            ObjectProperty obj = m_instance.getRelation(object);
            if (obj != null &&
                (!justDocumentsNotRels ||
                 justDocumentsNotRels 
                  obj.hasInstanceData(false) )) {
                return obj;
            }

            Iterator itr = m_objDocs.keySet().iterator();
            while (itr.hasNext()) {
                Integer ido = (Integer) itr.next();
                docDataModel doc = getSubDoc(ses,ido,false);
                ObjectProperty res = doc.findObject(ses,object, justDocumentsNotRels);
                if (res != null) {
                    return res;
                }
            }
        } catch (SystemException e) {
            e.printStackTrace();
        } catch (ApplicationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getFilterId(int object) {
        if (m_ctx.idFilter == 0) {
            Contexto ctx = m_md.getRootContext(new Integer(m_ctx.id));
            return ctx.idFilter;
        } else {
            return m_ctx.idFilter;
        }
    }

    public int getInstanceID() {
        return m_instance.getIDO();
    }

    public instance getInstance() throws RemoteSystemException,
            CommunicationException {
        return m_instance;
    }

    public int[] getNewObjIndex(metaData md, int idToAtom, int idCtxAtom,
                                boolean parteYaAnexada) {
        docDataModel doc = this;
        int niveles = 1;
        while (doc.getParentDocument() != null) {
            doc = doc.getParentDocument();
            niveles++;
        }

        //System.out.println("GETINDEX "+parteYaAnexada);
        if( root!=null ){
         try{
         System.out.println(jdomParser.returnXML( root ) );
         }catch(Exception e){;}
         System.out.println("HERMANOS:"+root.getChild("PARTES").getChildren("ATOM").size());
          }
        int numHermanosTotal = 0;
        int numHermanos = parteYaAnexada ? -1 : 0;
        Integer idCtx = new Integer(idCtxAtom);
        Contexto ctx= md.getRootContext(idCtx);
        Iterator itr = m_instance.getRelationIterator(ctx.idRel);
        while (itr.hasNext()) {
            ObjectProperty herm = (ObjectProperty) itr.next();
            numHermanosTotal++;
            Integer idCtxH = new Integer(herm.getIdoRel());
            if (md.ctxIsSpecializedFrom(idCtxH, idCtx) ||
                md.ctxIsSpecializedFrom(idCtxH, idCtx)) {
                numHermanos++;
            }
        }

        String objectIndex = "-" +
                             String.valueOf(niveles * 100 + numHermanosTotal +
                                            (parteYaAnexada ? 0 : 1));
        //System.out.println(objectIndex);
        int[] lista = new int[2];
        lista[0] = Integer.parseInt(objectIndex);
        lista[1] = numHermanos + (parteYaAnexada ? 0 : 1);
        return lista;
    }

    public void updateTransitiveData(session ses, boolean fixWarnning) throws
            ApplicationException, SystemException, RemoteSystemException,
            CommunicationException, DataErrorException {
        try{
         System.out.println("PRE UPDATE TRANSITIVE_:"+jdomParser.returnXML(m_instance));
          }catch(Exception e){;}

        ArrayList cambios = filterFixing(fixWarnning, true, 0);

        for (int i = 0; i < cambios.size(); i++) {
            fixMap map = (fixMap) cambios.get(i);
            Contexto ctx = m_md.getContext(map.ctxSuperAfectado);
            if (ctx.idRel != helperConstant.REL_TRANSITIVA) {
                continue;
            }
            if (map.getSourceFixSize() == 0) {
                continue;
            }
            subUpdateTranData(ses,m_currentScope, map.ctxSuperAfectado,
                              map.filtroAfectado);
        }

        if (!m_inicializado) {
            Iterator iC = m_contextosDeRelacion.getChildren("CONTEXT").iterator();
            while (iC.hasNext()) {
                Element eCtx = (Element) iC.next();
                Integer idCtxAfect = new Integer(eCtx.getAttributeValue("ID"));
                Contexto ctx = m_md.getContext(idCtxAfect);
                if (ctx.idRel != helperConstant.REL_TRANSITIVA) {
                    continue;
                }
                if (ctx.filterFix.size() != 0) {
                    fixProperties fp = (fixProperties) ctx.filterFix.get(0);
                    if (fp.detail == null) {
                        continue;
                    }
                }
                subUpdateTranData(ses,m_currentScope, idCtxAfect,new Integer(ctx.idFilter));
            }
        }

        try{
         System.out.println("POST UPDATE TRANSITIVE_:"+jdomParser.returnXML(m_instance));
          }catch(Exception e){;}
    }

    private void subUpdateTranData(session ses,
                                   scope myScope,
                                   Integer ctxSuperAfectado,
                                   Integer filtroAfectado) throws
            SystemException, ApplicationException, RemoteSystemException,
            CommunicationException,DataErrorException {
        Contexto ctx = m_md.getContext(ctxSuperAfectado);
        if (!m_serverSide &&
            m_md.isSpecializedFrom(ctx.to, helperConstant.TO_INDICE)) {
            return;
        }

        Iterator iR = m_instance.getRelationIterator(false);
        while (iR.hasNext()) {
            ObjectProperty rel = (ObjectProperty) iR.next();
            if (rel.getIdoRel() == ctxSuperAfectado.intValue()) {
                m_instance.delRelation(ses,new Integer(rel.getIdProp()));
            }
        }

        Element filter = (Element) m_filtrosDeRelacion.get(filtroAfectado);
        jdomParser.print("FILTER AFECTADO ",filter);
        selectData queryRes = m_server.serverGetQuery(myScope, m_currTask,m_user,m_md.getBusiness(),filter);
        actualizaDocRelacionesFromSelection(ses,-1,filter,queryRes,ctxSuperAfectado.intValue(),false);
    }

    public boolean actualizaDocRelacionesFromSelection(session ses,
            int tableIndex,
            Element filter,
            selectData dataSelection,
            int relationContext,
            boolean dataBaseUpdated) throws DataErrorException,SystemException,
            ApplicationException, RemoteSystemException, CommunicationException {
        if (filter == null) {
            filter = (Element) m_filtrosDeRelacion.get(new Integer(tableIndex));
        }

        int rolCurrent = 0, rolChild = 0, sRel = 0;
        sRel = Integer.parseInt(filter.getAttributeValue("ID_REL"));
        jdomParser.print("SREL", filter);
        if (filter.getAttributeValue("ROL_CHILD") == null) {
            rolChild = Integer.parseInt(filter.getAttributeValue("ROL_CURRENT"));
            rolCurrent = Integer.parseInt(filter.getAttributeValue("ROL_CONTEXT"));
        } else {
            rolChild = Integer.parseInt(filter.getAttributeValue("ROL_CHILD"));
            rolCurrent = Integer.parseInt(filter.getAttributeValue("ROL_CURRENT"));
        }
        boolean exito = true;
        jdomParser.print("actualizaDocRelacionesFromSelection selection",dataSelection.toElement());
        Iterator iSel= dataSelection.getIterator();
        while(iSel.hasNext()){
            instance selRow=(instance)iSel.next();
            jdomParser.print("actualizaDocRelacionesFromSelection item",selRow.toElement());
            String rdn = selRow.getRdn();
            if (!actualizaDocRelaciones(ses,
                                        selRow,
                                        filter,
                                        selRow.getIDO(),
                                        selRow.getType(),
                                        sRel, rolCurrent,
                                        rolChild,
                                        rdn,
                                        relationContext,
                                        dataBaseUpdated)) {
                dataSelection.remove(selRow.getIDO());

                exito = false;
            }
        }
        return exito;
    }


    public boolean actualizaDocRelaciones(session ses,
                                          instance dataRow,
                                          Element filter,
                                          int idoSelection,
                                          int idToSelection,
                                          Integer relationContext,
                                          String rdn,
                                          boolean dataBaseUpdated) throws
            SystemException, ApplicationException, RemoteSystemException,
            CommunicationException,DataErrorException {
        Contexto ctx = m_md.getContext(relationContext);
        return actualizaDocRelaciones(ses,
                                      dataRow,
                                      filter,
                                      idoSelection,
                                      idToSelection,
                                      ctx.idRel,
                                      ctx.idRolCtx,
                                      ctx.idRolCurr,
                                      rdn,
                                      relationContext.intValue(),
                                      dataBaseUpdated);

    }

    public boolean actualizaDocRelaciones(session ses,
                                          instance dataRow,
                                          Element filter,
                                          int idoSelection,
                                          int idToSelection,
                                          int idRel,
                                          int rolCurrent,
                                          int rolChild,
                                          String rdn,
                                          int relationContext,
                                          boolean dataBaseUpdated) throws
            SystemException, ApplicationException, RemoteSystemException,
            CommunicationException,DataErrorException {

        ObjectProperty insRel = m_instance.getRelation(idoSelection);
        int idFilter = Integer.parseInt(filter.getAttributeValue("ID"));

        boolean newRel = insRel == null;
        int category = m_md.getCategoriaRel(idRel);
        boolean rolChildIsSuper= m_md.rolChildIsSuperior(m_instance.getType(),idToSelection,idRel,rolChild,rolCurrent);
        if(newRel)
           insRel = new relation(m_instance,
                                  rolChildIsSuper,
                                  idoSelection,
                                  idToSelection,
                                  idRel,
                                  rolChild,
                                  rolCurrent,
                                  relationContext,
                                  idFilter,
                                  category,
                                  rdn,
                                  ses,
                                  (dataBaseUpdated ? dynagent.application.action.GET:dynagent.application.action.NEW));
        	insRel = new ObjectProperty();
        subUpdateRelMaps(ses,insRel);

        if(!newRel)
           // actualizaRelAvas(ses,insRel, filter, dataRow);

         //if(m_instance.getAttributeValue("ID_O")!=null)

        try {
            if (newRel) {
                addRel( ses,-1, insRel, false); //en las siguientes lineas ya actualizo los avas.Revisarlo
                // actualizaRelAvas(ses,insRel, filter, dataRow);
                processCacheInstance(ses,relationContext, insRel, idoSelection, idToSelection);
            }
        } catch (ParseException pe) {
            throw new SystemException(SystemException.ERROR_DATOS,
                                      pe.getMessage());
        }

        return true;
    }

    private void processCacheInstance(session ses,int currCtxId, ObjectProperty nodeRel, int ido, int idTo) throws ApplicationException,
            SystemException, RemoteSystemException, CommunicationException {
        //Ver en doc, filter fixing: cache politica
        System.out.println("DOCDM, preupdateCache " + currCtxId);
        //jdomParser.print("", nodeRel);
        Contexto currCtx = m_md.getContext(new Integer(currCtxId));
        scope mYscope = null;
        try {
            mYscope = new scope(m_md, m_userRol, currCtx,
                                getCurrentScope().getOperation());
        } catch (DataErrorException de) {
            de.printStackTrace();
            throw new SystemException(SystemException.ERROR_DATOS,
                                      de.getMessage());
        }

        Element rootCtxs = mYscope.getContexts(idTo, helperConstant.TO_BASE,
                                               currCtx.action, true);
        Iterator itr = rootCtxs.getChildren().iterator();
        while (itr.hasNext()) {
            Element eCtxChild = (Element) itr.next();
            Iterator iT = m_md.getContextosSet().iterator();
            while (iT.hasNext()) {
                Integer id = (Integer) iT.next();
                Contexto ctxTarget = m_md.getContext(id);
                if (ctxTarget.hasFixing() &&
                    ctxTarget.action == currCtx.action) {
                    Iterator iF = ctxTarget.getFixList();
                    while (iF.hasNext()) {
                        fixProperties fp = (fixProperties) iF.next();
                        if (fp.ctxFix != 0 &&
                            fp.detail == null &&
                            fp.ctxFix ==
                            Integer.parseInt(eCtxChild.getAttributeValue("ID"))) {
                            System.out.println("DOCDM, updateCache " + ido);
                            actualizaCacheInstancia(ses,
                                    getCurrentScope(),
                                    m_instance,
                                    new Integer(ido),
                                    idTo,
                                    false);
                            return;
                        }
                    }
                }
            }
        }
    }

    public void actualizaRelAvas(session ses, attributeContainer insRelSumidero,Element filter,instance dataSelRow)
    throws DataErrorException{

        int pos = 0;
        if (dataSelRow == null ) {
            return;
        }
        // selectData newData= dataSelRow.getSelection(m_md,filter);
        selectData newData = new selectData();
        jdomParser.print("DocDataModel actualizaRelAvas selection ",newData.toElement());
        if( !newData.hasData() ){
            System.out.println("DEBUG DocDataModel, actualizaRelAvas: NO DATA to update");
            jdomParser.print("FILTER ",filter);
            try{
                jdomParser.print("INSTANCE ", dataSelRow.toElement());
            }catch(Exception e){
                System.out.println("INSTANCE NULL");
            }
            return;
        }
        Iterator iIns= newData.getIterator();
        while( iIns.hasNext() ){
            instance obj= (instance)iIns.next();
            Iterator iVir = obj.getAttIterator(false,false);
            while (iVir.hasNext()) {
                attribute newAt = (attribute) iVir.next();
                System.out.println("actualizaRelAvas iterator " + newAt);
                escalarAVA(ses, insRelSumidero, obj.getType(), obj.getIDO(), newAt.getVirtualREF(),
                           newAt.getTapos(), newAt.getValue());
                attribute atSumidero= insRelSumidero.getAttribute(newAt.getTapos(),newAt.getVirtualREF());
                subActualizaAt(ses, insRelSumidero, atSumidero, newAt.getVirtualTO(),
                               newAt.getVirtualIDO(), newAt.getVirtualREF(),
                               newAt.getTapos(), newAt.getValue());
            }
        }
    }

    private static boolean subLookForCtx(metaData md,selectData thrData,attributeContainer ins,fixProperties fp,HashMap lista) {
        boolean exito = false;
        int idCtx = fp.ctxFix;
        if (ins != null ) {
            Integer relCtx = new Integer(fp.ctxFix);
            System.out.println("LOOK FOR " + relCtx);
            if (md.getFusionControl().contextIsFussioned(relCtx)) {
                System.out.println("INLOOK1");
                ArrayList fus = md.getFusionControl().getOriginalContexts(relCtx);
                for (int i = 0; i < fus.size(); i++) {
                    Integer id = (Integer) fus.get(i);
                    if (id.intValue() == idCtx) {
                        subFilterFix(ins, fp, lista);
                        exito = true;
                    }
                }
            }
            else
                if( md.getFusionControl().contextHasFussionedPeer( relCtx ) ){
             System.out.println("INLOOK2");
             if( relCtx.intValue()==idCtx){
              subFilterFix(rel, fp, lista );
              exito=true;
             }else{
              System.out.println("INLOOK3");
              Integer id=md.getFusionControl().getContextFusionedPeer(relCtx);
              ArrayList fus=md.getFusionControl().getOriginalContexts( id );
              for( int i=0; i<fus.size();i++){
               Integer id2=(Integer)fus.get(i);
               if( id2.intValue()==idCtx ){
                subFilterFix(rel,fp, lista );
                exito=true;
               }
              }
             }
                } else {
                System.out.println("INLOOK4");
                if (relCtx.intValue() == idCtx) {
                    subFilterFix(ins, fp, lista);
                    exito = true;
                }
            }
        }
        return exito;
    }

    private static void lookForContextSource(metaData md,
                                             selectData thrData,
                                             selectData queryData,
                                             attributeContainer ins,
                                             fixProperties fp,
                                             HashMap lista) {
        int idCtxSource = fp.ctxFix;
        int refNodeSource = fp.refNodeFix;
        System.out.println("BUSCANDO EN INSTANCE para " + idCtxSource);
        Contexto ctx = md.getContext(new Integer(idCtxSource));
        if( ins instanceof ObjectProperty &&
            ((ObjectProperty)ins).getIdoRel()==idCtxSource)
            subLookForCtx(md, thrData, ins, fp, lista);

        if (ctx != null &&
            ctx.toRoot == helperConstant.TO_USER_CFG &&
            queryData != null &&
            queryData.hasData()) {
            Iterator itr = queryData.getIterator();
            while (itr.hasNext()) {
                instance obj = (instance) itr.next();
                Iterator iAt = obj.getAttIterator(refNodeSource);
                while (iAt.hasNext()) {
                    attribute at = (attribute) iAt.next();
                    Integer ido = new Integer(at.getVirtualIDO());
                    if (ido.intValue() != 0)
                        lista.put(ido, at);
                }
            }
        }
        boolean exito = false;

        if (ins instanceof attributeContainer ) {
            instance root=null;
            if( ins instanceof instance )
                root=(instance)ins;
            else
                if( ins instanceof relation && ((relation)ins).hasInstanceData(false))
                    root = ((relation) ins).getRelationInstance();

            if( root!=null ){
                Iterator itr = root.getRelationIterator(false);
                while (itr.hasNext()) {
                    attributeContainer atom = (attributeContainer) itr.next();
                    //exito=subLookForCtx( md, threadData, atom, fp, lista );
                    lookForContextSource(md, thrData, null, atom, fp, lista);
                }
            }
        }
        //no ha habido exito
        if (thrData != null) {
            Iterator itr = thrData.getIterator();
            while (itr.hasNext()) {
                contextData item = (contextData) itr.next();
                if( item.getContextID()==idCtxSource)
                    subFilterFix(item, fp, lista);
            }
        }
    }

    private static boolean subFilterFix(attributeContainer instCtx,fixProperties fp,HashMap lista) {
        if(	Integer.parseInt(instCtx.getAttributeValue("ID_O"))>0 ||
         instCtx.getAttributeValue("EXTRACTED_IDO")!=null ){
        Integer ido = instCtx.getAttributeValue("EXTRACTED_IDO") == null ?
                      new Integer(instCtx.getIDO()) :
                      new Integer(instCtx.getAttributeValue("EXTRACTED_IDO"));

        if( fp.detail == null || fp.detail.getChild("AVA") == null){//es un fix de ctx por su ID_O
            lista.put(ido, instCtx);
            return true;
        }
        else{
            //jdomParser.print("SUBFFIX_INS", instCtx);
            jdomParser.print("SUBFFIX_AVA", fp.detail);
            ArrayList ats = new ArrayList();
            Iterator iAva = fp.detail.getChildren("AVA").iterator();
            while (iAva.hasNext()) {
                Element ava = (Element) iAva.next();
                attribute at = instCtx.getAttribute(Integer.parseInt(ava.getAttributeValue("SOURCE_TAPOS")), 0);
                if (at != null) {
                    at = (attribute) at.clone();
                    at.setTapos(Integer.parseInt(ava.getAttributeValue("TA_POS")));
                    ats.add(at);
                }
            }
            if (ats.size() > 0){
                lista.put(ido, ats);
                return true;
            }
        }
        return false;
    }

    public ArrayList filterFixing(boolean fixWarnning,
                                  boolean update,
                                  int filterID) throws ApplicationException {

        return filterFixing(m_md, m_threadData, null, m_instance,
                            m_contextosDeRelacion, fixWarnning, update,
                            filterID);
    }

    public HashMap lookForContextSource(fixProperties fp) {
        HashMap fixList = new HashMap();
        lookForContextSource(m_md, m_threadData, null, m_instance, fp, fixList);
        return fixList;
    }

    public static ArrayList filterFixing(metaData md,
                                         selectData threadData,
                                         selectData queryData,
                                         instance ins,
                                         Element contextos,
                                         boolean fixWarnning,
                                         boolean update,
                                         int filterID) throws
            ApplicationException {

        ArrayList res = new ArrayList();
        if (contextos == null) {
            return res;
        }

        instance rootIns = ins;
        while (rootIns.getParent()!= null) {
            rootIns = rootIns.getParent();
        }

        Iterator itr = contextos.getChildren("CONTEXT").iterator();
        while (itr.hasNext()) {
            Element eCtx = (Element) itr.next();
            Element filterAfect = eCtx.getChild("FILTER");
            if (filterAfect == null) {
                continue;
            }

            if (filterID != Integer.parseInt(filterAfect.getAttributeValue("ID"))) {
                continue;
            }
            Integer idctx = new Integer(eCtx.getAttributeValue("ID"));
            Contexto ctxAfect = md.getContext(idctx);
            //System.out.println("FILTERFIX, CTX:"+idctx);
            subFilterFixing(md,
                            threadData,
                            queryData,
                            rootIns,
                            ctxAfect,
                            fixWarnning,
                            update,
                            filterAfect,
                            res);
        }
        return res;
    }

    public static void subFilterFixing(metaData md,
                                       selectData threadData,
                                       selectData queryData,
                                       instance rootIns,
                                       Contexto ctxAfect,
                                       boolean fixWarnning,
                                       boolean update,
                                       Element filterAfect,
                                       ArrayList res) throws
            ApplicationException {
        Iterator lista = ctxAfect.getFixList();
        while (lista.hasNext()) {
            fixProperties fp = (fixProperties) lista.next();
             En fix sobre ido, osea fix sobre selecciones, no sobre ava, Si no es opcional y no hay source para este
            fix se genera una excepcion diciendo
            que primero debe asignar el dato xxx donde xxx es el label del "sumidero", no se usa el label
            del source. Por tanto si el nodo sumidero ya es el root del filtro, no tendria
             sentido la excpcion
            fixWarnning= fixWarnning &&
                         !(fp.filterNode!=null &&
                           fp.ctxFix != 0 &&
                           fp.filterNode.equals(filterAfect.getAttributeValue("OID")));

            System.out.println("FILTERFIX, FP:" + fp.ctxFix + "," +
                               fp.filterNode + "," + fp.refNodeFix + "," +
                               fp.optional + "," + fp.incrustar + "," +
                               fp.detail);

            jdomParser.print("FILTER AFECT", filterAfect);
            if( queryData!=null)
                jdomParser.print("QUERYDATA",queryData.toElement());
            if(rootIns!=null)
                jdomParser.print("INSTANCE ", rootIns.toElement());

            //if( fp.optional ) continue;
            if (fp.ctxFix == 0) {
                continue; //ya lo trate en el constructos del docDataModel
            }
            if (fp.incrustar != 0) {
                continue;
            }
            System.out.println("FILTERFIX, FP->2");
            HashMap lisObj = new HashMap();
            lookForContextSource(md, threadData, queryData, rootIns, fp, lisObj);

            if (lisObj.size() == 0)
                if( fixWarnning ) {
                    Contexto ctxFix = md.getContext(new Integer(fp.ctxFix));
                    if (ctxFix.nMin == 0)
                        continue;
                    // Es decir, que este fix no sea opcional significa que de tener valor debe ser provisto por el fix
                    // el usuario no puede seleccionarlo. Pero si el ctx source es opcional por nMin, no excepcion
                    try {
                        jdomParser.print("APPEXCEP:TOFIX ", filterAfect);
                        System.out.println("APPEXCEP:AFECT");
                        System.out.println("CONTEXTO:ROOT,fix " + ctxAfect.id + "," +
                                           fp.ctxFix);
                    } catch (Exception e) {
                        ;
                    }
                    throw new ApplicationException(ApplicationException.
                                                   CONTEXTO_NO_MAPEADO,
                                                   fp.filterNode);
                }else
                    continue;
            if (!update) {
                continue;
            }
            Element node = jdomParser.findElementByAt(filterAfect,
                    "FILTER",
                    "OID",
                    fp.filterNode,
                    true,
                    true);

            if (node == null) {
                node = jdomParser.findElementByAt(filterAfect,
                                                  "FILTER",
                                                  "OID_FUSIONADO",
                                                  fp.filterNode,
                                                  true,
                                                  true);
            }

            if (node == null) {
                JOptionPane.showMessageDialog(null,
                        "Error en updateTransitive Data, subnodo ref not found " +
                                              fp.filterNode);
                try {
                    System.out.println("Error en updateTransitive Data." +
                                       ctxAfect.id + "," +
                                       jdomParser.returnXML(filterAfect));
                } catch (Exception e) {
                    ;
                }
                return ;
            }

            if (node.getAttributeValue("ID_O") != null &&
                node.getAttributeValue("ID_O").length() > 0) {
                ArrayList oldList = extraeLista(node.getAttributeValue("ID_O"));
                if (lisObj.size() == oldList.size() && fp.detail == null) {
                    System.out.println("LISTAOBJ:" + lisObj.size() + "," +
                                       oldList.size());
                    boolean iguales = true;
                    for (int i = 0; i < oldList.size(); i++) {
                        if (!lisObj.containsKey(oldList.get(i))) {
                            iguales = false;
                            break;
                        }
                        System.out.println("OBJ:" +
                                           lisObj.get(oldList.get(i)));
                    }
                    if (iguales) {
                        continue;
                    }
                }
            }
            boolean existsDBInstance = false;
            Iterator iL = lisObj.keySet().iterator();
            while (iL.hasNext()) {
                Integer ido = (Integer) iL.next();
                if (ido.intValue() > 0) {
                    existsDBInstance = true;
                    break;
                }
            }
            if (!existsDBInstance && fp.detail == null) {
                continue;
            }

            Integer ref = new Integer(ctxAfect.idFilter);
            fixMap fm = new fixMap(new Integer(ctxAfect.id),
                                   ref,
                                   Integer.parseInt(node.getAttributeValue("REF")),
                                   fp.ctxFix,
                                   lisObj);
            fm.printData();
            if (res != null) {
                res.add(fm);
            }
            setFilterFix_IDO(node, fm);
            setFilterFixAVA(md, node, fm);
        }
        jdomParser.print("POST FILTER AFECT", filterAfect);
    }

    public static ArrayList extraeLista(String str) {
        ArrayList lista = new ArrayList();
        if (str == null) {
            return lista;
        }
        if (str.indexOf("{") == -1) {
            lista.add(new Integer(str));
            return lista;
        }
        int ini = str.indexOf("{"), end = str.indexOf("}");
        String[] res = str.substring(ini + 1, end).split(";");
        for (int i = 0; i < res.length; i++) {
            lista.add(new Integer(res[i]));
        }
        return lista;
    }

    private static void setFilterFix_IDO(Element node, fixMap fm) {

        if (fm.getSourceFixSize() == 0) {
            return;
        }

        String idoAt = (fm.getSourceFixSize() > 1) ? "{" : "";
        Iterator itr = fm.getIDO_KeySet().iterator();
        int pos = 0;
        String rdn = null;
        while (itr.hasNext()) {
            Integer ido = (Integer) itr.next();
            if (ido.intValue() <= 0)
                continue;
           // if(fm.idoFuente.size() == 1){
           Object source= fm.getIdoSource(ido);
                if (source instanceof Element) { //puede ser ArrayList que seraa un AVA
                    ///////////////////
                    Exception ex= new Exception("DEBUG setFilterFix_IDO aposta");
                    ex.printStackTrace();
                    int i=2/0;
                    *//** @todo revisar si puede ser un Element *//*
                    ////////////////////////
                    Element dato = (Element) source;
                    if (dato.getAttributeValue("RDN") != null) {
                        rdn = dato.getAttributeValue("RDN");
                    } else
                    if (dato.getAttributeValue("TA_POS") != null) {
                        int tapos = Integer.parseInt(dato.getAttributeValue("TA_POS"));
                        if (tapos == helperConstant.TAPOS_RDN)
                            rdn = dato.getText();
                    }
                    if (pos++ > 0)
                        idoAt += ";";
                    idoAt += ido.toString();
                }
                if (source instanceof attribute) { //puede ser ArrayList que seraa un AVA
                    attribute dato = (attribute) source;
                    if (dato.getTapos() == helperConstant.TAPOS_RDN)
                        rdn = dato.getValue().toString();
                    if (pos++ > 0)
                        idoAt += ";";
                    idoAt += ido.toString();
                }
                if (source instanceof attributeContainer) {
                    attributeContainer dato = (attributeContainer) source;
                    if (pos++ > 0)
                        idoAt += ";";
                    idoAt += ido.toString();
                }
           // }
        }
        if (fm.getSourceFixSize() > 1) {
            idoAt += "}";
        }
        if (pos > 0) {
            node.setAttribute("ID_O", idoAt);
        }
        if (rdn != null) {
            node.setAttribute("RDN_SOURCE", rdn);
        }
    }

    private static void setFilterFixAVA(metaData md, Element node, fixMap fm) {
        System.out.println("SET_FIX_AVA " + fm.getSourceFixSize());
        if (fm.getSourceFixSize() == 0) {
            return;
        }
        Iterator itr = fm.getIDO_KeySet().iterator();
        int pos = 0;
        String rdn = null;
        while (itr.hasNext()) {
            Integer ido = (Integer) itr.next();
            Object data = fm.getIdoSource(ido);
            if(data instanceof ArrayList) { //son avas
                System.out.println("SET_FIX_AVA, si array");
                for (int i = 0; i < ((ArrayList) data).size(); i++) {
                    attribute at = (attribute) ((ArrayList) data).get(i);
                    subSetFilterFixAVA(md, at, node);
                }
            }
        }
    }

    private static void subSetFilterFixAVA(metaData md, attribute at, Element node) {
        Element ava = jdomParser.findElementByAt(node,
                                                 "AVA",
                                                 "TA_POS",
                                                 String.valueOf(at.getTapos()),
                                                 false);
        if( ava==null ){
            ava = new Element("AVA");
            ava.setAttribute("TA_POS", String.valueOf(at.getTapos()));
            ava.setAttribute("ID_TM", String.valueOf(at.getMemberType()));
            node.addContent(ava);
        }
        jdomParser.print("AVA", ava);
        if (at.getMemberType()== helperConstant.TM_MEMO ||
            at.getMemberType()== helperConstant.TM_TEXTO ||
            at.getMemberType()== helperConstant.TM_BOOLEANO_EXT ){

            if( at.getValue() instanceof extendedValue )
                ava.setAttribute("VALUE",
                                 helperConstant.valueToString(helperConstant.TM_BOOLEANO,
                                                              (Boolean)((extendedValue)at.getValue()).getValue()));
            else
                ava.setText(at.getValue().toString());

        }else{
            if( at.getValue() instanceof range ){
                ava.setAttribute("VAL_MIN",((range) at.getValue()).min.toString());
                ava.setAttribute("VAL_MAX",((range)at.getValue()).max.toString());
            }else
            if( at.getValue() instanceof ArrayList )
                ava.setAttribute("VALUE", helperConstant.valueToString(at.getMemberType(),at.getValue()));
            else
                ava.setAttribute("VALUE", at.getValue().toString());
        }
        if( at.getValue() instanceof range )
            ava.setAttribute("OP","RANGE");
        else
            ava.setAttribute("OP","=");
    }

    public HashMap getNumericValues(session ses,int to,int object) throws SystemException,
            ApplicationException, RemoteSystemException, CommunicationException,DataErrorException {
        //Si object fuera distinto de cero se lo pediriamos a tableDomModel. Aqui siempre son del root
        if (object == -1) {
            HashMap moa = new HashMap();
            Iterator itr = m_instance.getDirectAttIterator();
            if( !itr.hasNext() )  return moa;
            while (itr.hasNext()) {
                attribute item = (attribute) itr.next();
                Object value= item.getValue();
                if( value instanceof range ||
                    value instanceof ArrayList )//enumerado
                    continue;
                int tm = item.getMemberType();
                if (tm == helperConstant.TM_ENUMERADO ||
                    tm == helperConstant.TM_ENTERO ||
                    tm == helperConstant.TM_REAL) {
                    if (item.getValue() != null &&
                        item.getAttributeValue("VALUE").matches(".+[;\\s].+")) {
                        continue;
                    }
                    //System.out.println("INSERTANDO MAP:"+"0@"+tapos+";"+item.getAttributeValue("VALUE"));

                    if( value instanceof Integer )
                        moa.put("0@"+item.getTapos(),new Double(((Integer)value).intValue()));
                    else
                        if( value instanceof Double )
                            moa.put("0@"+item.getTapos(),value);
                        else
                            throw new DataErrorException("VALUE INSTANCEOF " +value.getClass());
                } else {
                    moa.put("0@" + item.getTapos(), null);
                }
            }
            return moa;
        } else {
            docDataModel doc = getSubDoc(ses,object,false);
            return doc.getNumericValues( ses,to,-1);
        }
    }

    public void resetRestriction(int[] to_ido, String idCampo) throws
            NumberFormatException {
    }

    public boolean isNull(session ses,int[] to_ido, String campo, Object newVal) throws
            SystemException, ApplicationException, RemoteSystemException,
            CommunicationException,DataErrorException {
        if (newVal != null) {
            if (newVal instanceof String) {
                return ((String) newVal).length() == 0;
            } else {
                return false;
            }
        }
        if (to_ido[1] == -1) {
            Integer tapos = getTAPOS(campo);
            Object val = getData( ses, to_ido, campo);
            if (val == null) {
                return true;
            }
            if (val instanceof String) {
                return ((String) val).length() == 0;
            } else {
                return false;
            }
        } else {
            docDataModel doc = getSubDoc(ses,to_ido[1],false);
            return doc.isNull( ses,new int[]{to_ido[0],-1}, campo, newVal);
        }
    }


    public Object getData(session ses,int[] to_ido, String campo) throws SystemException,
            ApplicationException, RemoteSystemException, CommunicationException,DataErrorException {
        //System.out.println("DOC GETDATA "+object+","+campo);
        if(to_ido[1] == -1) { //se trata de un objeto de este nivel
            Integer tapos = getTAPOS(campo);
            return m_instance.getAttributeValue(tapos.intValue(),0);
        }else {
            docDataModel doc = getSubDoc(ses,to_ido[1],false);
            return doc.getData( ses,new int[]{to_ido[0],-1}, campo);
        }
    }

    public boolean containsObject(int object) {
        if (object == -1) {
            return true;
        }
        return m_objDocs.containsKey(new Integer(object));
    }

    public docDataModel getSubDoc(int object, session subsession, int operation, boolean lock) throws
            SystemException, ApplicationException, RemoteSystemException,
            CommunicationException,DataErrorException {
        Integer obj = new Integer(object);
        return getSubDoc(obj, subsession, operation, lock);
    }

    public docDataModel getSubDoc( session ses, int object, boolean lock) throws SystemException,
            ApplicationException, RemoteSystemException, CommunicationException,DataErrorException {
        Integer obj = new Integer(object);
        return getSubDoc(obj, ses, m_currentScope.getOperation(),lock);
    }

    public docDataModel getSubDoc(session ses, Integer obj, boolean lock) throws SystemException,
            ApplicationException, RemoteSystemException, CommunicationException,DataErrorException {
        return getSubDoc(obj, ses, m_currentScope.getOperation(),lock);
    }

    public docDataModel getSubDoc(Integer obj, session subsession, int operation, boolean lock) throws
            SystemException, ApplicationException, RemoteSystemException,
            CommunicationException,DataErrorException {
        if (m_objDocs.containsKey(obj)) {
            docDataModel doc = (docDataModel) m_objDocs.get(obj);
            doc.operationChange(operation);
            return doc;
        } else {
            return buildSubDoc(obj, subsession, operation,lock);
        }
    }

    public scope getCurrentScope() {
        return m_currentScope;
    }

    public void operationChange(int operation) {
        if (getCurrentScope().getOperation() != operation) {
            m_operationScope.put(new Integer(operation),
                                 new scope(m_md,
                                           m_userRol,
                                           getCurrentScope().getRootAccess(),
                                           operation));
        }

    }

    private docDataModel buildSubDoc(final Integer obj, session subsession,int operation, boolean lock) throws SystemException,
            ApplicationException, RemoteSystemException, CommunicationException,DataErrorException {
        ObjectProperty eObj = m_instance.getRelation(obj);
        System.out.println("BUILDSUBDOC:" + obj);
        if( eObj==null ){
            jdomParser.print("INSTANCE",m_instance.toElement());
        }
        int idto = eObj.getIdProp();
        Integer ctxChild=new Integer(m_instance.getRelation(obj).getIdoRel());
        scope myScope = new scope(m_md,
                                  m_userRol,
                                  m_currentScope.getAccess(ctxChild),
                                  operation);

        instance ins= actualizaCacheInstancia(subsession,
                                              myScope,
                                              m_instance,
                                              obj,
                                              idto,
                                              lock);

         docDataModel doc = new docDataModel(m_serverSide,
                                            m_user,
                                            m_userRol,
                                            this,
                                            m_control,
                                            m_md,
                                            m_md.getContext(ctxChild),
                                            idto,
                                            ins,
                                            subsession,
                                            myScope,
                                            m_server,
                                            m_currTask);
        m_objDocs.put(obj, doc);
        if( subsession!=null) {
            subsession.addSessionable(new sessionable(){
                public void rollback(session ses){
                        //Si no hacemos esto docDataModel cachea una instancia que puede haber sufrido un rollback
                        //por tanto habria que crear de nuevo el doc DataModel.
                        m_objDocs.remove(obj);
                }
                public boolean exists(){return true;}
                public void delete(session ses){}
                public boolean exists(boolean checkAllSession) {
                    return true;
                }
                public boolean hasChanged() {
                    return true;
                }
                public boolean isNull() {
                    return false;
                }
            });
        }
        return doc;
    }

    private instance actualizaCacheInstancia(session ses, scope myScope, instance root,  Integer ido, int idTo, boolean lock) throws
            SystemException, ApplicationException, RemoteSystemException,
            CommunicationException {
        //instance instanceObject=(instance)m_objetos.get( ido );
        ObjectProperty rel = root.getRelation(ido.intValue());
        //if (!rel..hasInstanceData(true)) {
        if(true){
            //quiere decir que el objeto está un nodo relacion
            instance instanceObject = null;
            try {
                instanceObject = (instance) m_server.serverGetInstance(myScope,
                        m_user,
                        idTo,
                        ido.intValue(),
                        getContexto(ido).intValue(),
                        m_currTask,
                        false,
                        false,
                        lock).clone();
            } catch (SystemException se) {
                if (lock)
                    m_control.addLockedInstance(ido.intValue());
                se.printStackTrace();
                return instanceObject;
            }catch(InstanceLockedException il){
                il.printStackTrace();
                //no puede pasar porque no bloqueamos
            }
            if (lock) {
                m_control.addLockedInstance(ido.intValue());
            }
            // rel.setRelationInstance(ses,instanceObject);
            return instanceObject;
        }else
            // return rel.getRelationInstance();
        	return null;
    }

    public Vector getDirectValues(int object) {
        //solo se usa en formDataModel destinado a TxControl
        return null;
    }

    public void setNewRestriction(session ses,int[] to_ido, String idCampo, Element nodo) throws DataErrorException{
    }

    public void addRel(session ses,int object, ObjectProperty insRel, boolean updateAvas ) throws ParseException,DataErrorException {
        if (object == -1) {
            Integer ido = new Integer(insRel.getIdProp());
            // insRel.addRelationListener(this);
            m_instance.addRelation(ses,insRel);
            if (updateAvas) {
              //  subUpdateAvas(ses,insRel);
            }
        }
    }

    public void addPart(session ses,int object, instance instanceObject, int idCtx,boolean dataBaseUpdated) throws ParseException,
            DataErrorException, RemoteSystemException, CommunicationException,SystemException {
        if (object == -1) {
            System.out.println("DocDataModel adding part "+object);
            Integer ido = new Integer(instanceObject.getIDO());

            if(!m_instance.hasRelation(ido,true)) {
                System.out.println("DocDataModel adding part no existia");
                Integer iIdCtx = new Integer(idCtx);
                Contexto ctx = m_md.getContext(iIdCtx);
                Contexto ctxRoot=m_md.getRootContext(iIdCtx);
                boolean relIsUp= m_md.rolChildIsSuperior(m_instance.getType(),instanceObject.getType(),idCtx);
                relation rel= new relation(m_instance,
                                           relIsUp,
                                           ctx,
                                           ctxRoot.idFilter,
                                           ido.intValue(),
                                           instanceObject.getType(),
                                           m_md.getCategoriaRel(ctxRoot.idRel),
                                           instanceObject.getRdn(),
                                           ses,
                                           (dataBaseUpdated ? dynagent.application.action.GET:dynagent.application.action.NEW));
                rel.setRelationInstance(ses,instanceObject);
                rel.addRelationListener(this);
                ObjectProperty rel = new ObjectProperty();
                m_instance.addRelation(ses,rel);
            }else{
                ObjectProperty rel=m_instance.getRelation(ido);
                if( !rel.exists() )
                    //el registro existe pero en la ultima sesion está eliminada
                    rel.create(ses);
            }
        }
    }

    public void addPart(session ses,int object, docDataModel subdoc, int idCtx,boolean dataBaseUpdated) throws ParseException,
            DataErrorException, RemoteSystemException, CommunicationException,SystemException {
        addPart( ses,object,subdoc.getInstance(),idCtx,dataBaseUpdated);
    }

    public void setRdn(session ses,String rdn) throws SystemException,
            RemoteSystemException, CommunicationException,DataErrorException {
        if (rdn != null) {
            m_instance.setRdn(ses,rdn);
        }
        try {
            setData(ses, new int[]{getIdTo(),-1}, "0@" + helperConstant.TAPOS_RDN, rdn);
        } catch (ApplicationException ae) {
            ae.printStackTrace();
        }
    }

    public docDataModel newInstance(session ses,Integer userRol, int object, int idCtx,
                                    int idTo, String strRdn, boolean updateAVAs) throws
            ParseException, InstantiationException, IllegalAccessException,
            ApplicationException, SystemException,
            RemoteSystemException, CommunicationException,DataErrorException {
        if (object == -1) {
            Integer iIctx = new Integer(idCtx);
            Contexto ctx = m_md.getContext(iIctx);
            Element rootDom = m_md.getDominio(ctx.getDom(userRol));
            Contexto ctxRoot = m_md.getRootContext(iIctx);
            int[] intObjIndex = getNewObjIndex(m_md, idTo, idCtx, false);
            instance ins= new instance(ses,dynagent.application.action.NEW,idTo,intObjIndex[0]);

            Element metaTO = null;
            metaTO = m_md.getMetaTO(new Integer(idTo));
            System.out.println("NEWREL:" + strRdn);

            Integer ido = new Integer(intObjIndex[0]);
            addPart(ses,object,ins,idCtx,false);

            docDataModel subdoc = getSubDoc(ses,ido,false);
            if(updateAVAs)
                updateAVAs(ses,ins);

            inicializarValoresFijos(ses,subdoc, rootDom, ins, 0);
            subdoc.setRdn(ses,strRdn);
            return subdoc;
        } else {
            return null;
        }
    }


    public void inicializarValoresFijos(session ses,docDataModel doc, Element eDom, instance data, int nivel)
            throws SystemException, ApplicationException, IllegalAccessException, InstantiationException,
            RemoteSystemException, CommunicationException,DataErrorException {
        int object=data.getIDO();
        if (object == doc.getInstanceID()) {
            object = -1;
        }
        System.out.println("DBG:" + object);
        //dominios restDom= m_simpleForm.getDominio( object );
        dominios restDom = new dominios(false, m_md, doc, doc, eDom);
        restDom.aplicaDominio(ses,data.getType(),object, true);
    }


    private void updateAVAs(session ses, instance atom) throws ParseException,DataErrorException {
        Iterator itr = atom.getDirectAttIterator();
        while(itr.hasNext()) {
            attribute at = (attribute)itr.next();
            escalarAVA(ses, atom,atom.getType(),atom.getIDO(),0,at.getTapos(),at.getValue());
        }

        if ( atom.getParent() != null) {
            itr = atom.getRelationIterator(false);
            while(itr.hasNext()) {
                ObjectProperty rel = (ObjectProperty)itr.next();
                subUpdateAvas(ses,rel);
            }
        }
    }

    private void subUpdateAvas(session ses,ObjectProperty rel) throws ParseException,DataErrorException {
        Iterator iAva = rel.getAttIterator(false,false);
        while (iAva.hasNext()) {
            attribute ava = (attribute)iAva.next();
            Integer tapos = new Integer(ava.getTapos());
            Object val = ava.getValue();
            escalarAVA(ses,
                       rel,
                       ava.getVirtualTO(),
                       ava.getVirtualIDO(),
                       ava.getVirtualREF(),
                       tapos.intValue(),
                       val);
        }
    }

    public void removePart(session ses, int object, Integer ido, Integer idto, boolean unlink_y_eliminar) {
        if (object == -1) {
             m_instance.delRelation(ses,ido);
            //m_objDocs.remove(ido);
        }
    }

    public void setData(session ses, int[] to_idoSource,String campo, Object val) throws
            SystemException, ApplicationException, RemoteSystemException,
            CommunicationException,DataErrorException {
        Object old = getData(ses,to_idoSource, campo);
        System.out.println("SET DATA:" + to_idoSource[1] + "," + campo + "," + val + "," + old);
        if (to_idoSource[1] == -1) {
            int tm = getIDTM(campo);
            if (helperConstant.equals(tm, val, old)) {
                return;
            }
            m_cambios.add(dominios.buildAva(m_md, campo, val));
            actualizaAtomAt(ses,m_instance, getIdTo(), getInstanceID(), campo, val);
        }else {
            docDataModel doc = getSubDoc(ses,to_idoSource[1],true);
            doc.setData( ses,new int[]{to_idoSource[0],-1}, campo, val);
        }
    }

    public void resetCambios() {
        m_cambios.clear();
    }

    public ArrayList getCambios() {
        return m_cambios;
    }

    public attribute actualizaAtomAt(session ses, attributeContainer atom, int toSource, int idoSource, String campo, Object value)
    throws DataErrorException{
        //se supone que aunque en campo no se codifique el idFilter al que pertenece el campo, solo se dice el subnodo,
        // el atom que me llega está en el subnodo correcto puesto que un mismo objeto no puede estar relacionado
        //con este root por dos veces.

        //System.out.println("IN ACTUALIZA ATOM;AAT1:"+campo+","+value);
        String[] parts = campo.split("@");
        int ref = Integer.parseInt(parts[0]);
        int tapos = Integer.parseInt(parts[1]);

        if (ref != 0) {
            return null; //no está soportado actualmente un table set de elementos subinferiores
        }
        //System.out.println("AAT2");
        attribute insAt = atom.getAttribute(tapos,0);
        System.out.println("AAT3:" + insAt);
        escalarAVA(ses,atom, toSource, idoSource, ref, tapos, value);
        attribute res = subActualizaAt(ses,atom,insAt,toSource, idoSource, ref,tapos,value);
        //jdomParser.print("RES INST AVA", res);
        //jdomParser.print("RES ATOM", atom);
        try{
          System.out.println("ATOM:"+jdomParser.returnXML(atom));
          }catch(Exception e){;}
        return res;
    }

    private void escalarAVA(session ses, attributeContainer atom, int toSource, int idoSource, int ref, int tapos, Object value)
    throws DataErrorException{

        attributeContainer parent = atom;
        //System.out.println("AAT4:"+idfilEmbed);
        ObjectProperty rel=null;
        Element filtEmbed=null;
        while(true) {
            if( parent instanceof instance ){
                instance tmp=((instance)parent).getParent();
                if( tmp==null ) break;
                // rel= ((instance)parent).getParentRelation();
                parent=tmp;
            }else
            if( parent instanceof relation ){
                // rel= (relation)parent;
              //  parent=((relation)parent).getParent();
            }else
                break;
            if( filtEmbed==null ){
                Integer idFEmbed= new Integer(rel.getIdoRel());
                filtEmbed= m_md.getFilter(idFEmbed);
            }
            if(rel.getIdoRel() !=0 && parent instanceof contextData) {
                //System.out.println("AAT5:"+parent.getAttributeValue("ID_FILTER"));
                contextData root= (contextData)parent;
                Integer idfilRoot = new Integer(root.getIdFilter());
                Integer idCtxRoot = new Integer(root.getContextID());
                Element filtRoot = m_md.getFilter(idCtxRoot, idfilRoot);
                //System.out.println("PRE DOC ACT:"+idfilRoot+","+tapos+","+idfilEmbed);
                int refRoot = m_md.findEmbedAtInRoot(filtRoot,
                                                     tapos,
                                                     ref,
                                                     filtEmbed);
                if(refRoot == -1) {
                    continue;
                }
                actualizaRelAvas(ses, parent, toSource, idoSource,refRoot, tapos, value);
                try{
                 System.out.println("POSTAVA:"+jdomParser.returnXML(parent));
                    }catch(Exception e){;}
            }
         }
    }

    public attribute actualizaRelAvas(session ses, attributeContainer insRelSumidero, String campo,
                                      int toVirtSource, int idoVirtSource, Object value)
    throws DataErrorException{
        String[] parts = campo.split("@");
        int ref = Integer.parseInt(parts[0]);
        int tapos = Integer.parseInt(parts[1]);
        return actualizaRelAvas(ses,insRelSumidero, toVirtSource, idoVirtSource, ref, tapos, value);
    }

    public attribute actualizaRelAvas(session ses, attributeContainer insRelSumidero,
                                       int toVirtSource, int idoVirtSource,int refSource,int taposSource, Object value)
    throws DataErrorException{
        attribute insAva = insRelSumidero.getAttribute(taposSource,refSource);
        return subActualizaAt(ses, insRelSumidero, insAva, toVirtSource, idoVirtSource, refSource,taposSource, value);
    }

    public attribute subActualizaAt(session ses,attributeContainer root, attribute avaSumidero,
                                    int toVirtSource, int idoVirtSource, int refSource,int taposSource,Object value)
    throws DataErrorException{
        System.out.println("subActualizaAt");
        int tm = m_md.getID_TM(new Integer(taposSource));
        *//** @todo asegurarse que el atsumidero es distinto del source *//*
        if(taposSource == helperConstant.TAPOS_RDN &&
           root instanceof instance )
            ((instance)root).setRdn(ses,(String)value);

        if(avaSumidero == null) {
            System.out.println("subActualizaAt insAva null");
            avaSumidero = new attribute(taposSource, tm, value, ses, dynagent.application.action.NEW);
            if( refSource!=0 ){
                avaSumidero.setVirtualREF(refSource);
                avaSumidero.setVirtualIDO(idoVirtSource);
                avaSumidero.setVirtualIDO(toVirtSource);
            }
            root.addAttribute(ses,avaSumidero, refSource);
        }else
            avaSumidero.setValue(ses,value);
        return avaSumidero;
    }

    public static void setAvaValue(metaData md, Element at, String value) {
        if (value == null || value.length() == 0) {
            return;
        }

        Integer tapos = new Integer(at.getAttributeValue("TA_POS"));
        int tm = md.getID_TM(tapos);
        if (tm == helperConstant.TM_TEXTO || tm == helperConstant.TM_MEMO) {
            at.setText(value);
        } else {
            at.setAttribute("VALUE", value);
        }
    }

    public HashMap refreshFilterMap(session ses, int object) throws SystemException,
            ApplicationException, RemoteSystemException, CommunicationException,DataErrorException {
        if (object == -1) {
            //los calculos siempre son para la sesion actual
            if( !m_instance.hasRelations(false)) return null;
            if (m_filterMap == null) {
                m_filterMap = new HashMap();
            }
            m_filterMap.clear();
            m_md.buildRelationalDataMap(m_filterMap,m_instance, null, null);
            m_md.buildPartsDataMap(m_instance, m_filterMap, null);
            return m_filterMap;
        } else {
            docDataModel doc = getSubDoc(ses,object,false);
            return doc.refreshFilterMap(ses, -1);
        }
    }

    public HashMap getFiltrosDeRelacion(session ses,int object) throws SystemException,
            ApplicationException, RemoteSystemException, CommunicationException,DataErrorException  {
        if (object == -1) {
            return m_filtrosDeRelacion;
        } else {
            docDataModel doc = getSubDoc(ses,object,false);
            return doc.getFiltrosDeRelacion( ses, -1);
        }
    }

    public Integer getTAPOS(String campo) {
        String[] parts = campo.split("@");
        return new Integer(parts[1]);
    }

    public int getIDTM(String campo) {
        return m_md.getID_TM(getTAPOS(campo));
    }

    public void relationChange(relation rel, int action) {
        if( action==dynagent.application.action.DEL ){
            Integer ido= new Integer(rel.getIDO());
            if( m_objDocs.containsKey(ido) )
                m_objDocs.remove(ido);
        }
    }
}
*/