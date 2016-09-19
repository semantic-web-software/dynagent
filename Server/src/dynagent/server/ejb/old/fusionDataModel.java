package dynagent.ejb.old;
/*package dynagent.ejb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.jdom.Element;

import dynagent.application.session;
import dynagent.communication.docServer;
import dynagent.knowledge.Contexto;
import dynagent.knowledge.fusionControl;
import dynagent.knowledge.metaData;
import dynagent.knowledge.scope;
import dynagent.knowledge.instance.instance;
import dynagent.ruleengine.meta.api.ObjectProperty;

public class fusionDataModel extends docDataModel{
  fusionControl m_fusion;
  public fusionDataModel(       boolean serverSide,
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
                                int currTask)
          throws SystemException, ApplicationException,RemoteSystemException,CommunicationException,DataErrorException{
    super(serverSide,user,userRol,parent,control,md,ctx,idto,ins,ses,myScope,server,currTask);
    m_fusion= md.getFusionControl();
  }

  public fusionDataModel( docDataModel doc ) throws RemoteSystemException,CommunicationException{
      super(doc.getInstance());
      copyData( this, doc );
      m_fusion= doc.m_md.getFusionControl();
  }

  public Object getData( session ses, int[] to_ido, String campo )
  throws SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException{
    return super.getData(ses,unnfusionObject(to_ido[1],campo),unnfusField(campo));
  }
  private int[] unnfusionObject( int object, String campo ){
  //se supone que los datos de nivel inferior no están fusionados
      if (object == -1 || object == getInstanceID()) {
          int[] newIdo = get_IDO_FromFusionAT(getTAPOS(campo));
          return newIdo;
      } else{
          int[] res= {getIdTo(), object};
          return res;
      }
  }

  private String unnfusField( String campo ){
      try{
          Integer taposFus = getTAPOS(campo);
          if (!m_fusion.isAtFusioned(taposFus)){
              System.out.println("UNFUSAT: NO FUS");
              return campo;
          }else {
              System.out.println("UNFUSAT: SI FUS "+m_fusion.getATSource(taposFus));
              String[] parts = campo.split("@");
              return parts[0] + "@" + m_fusion.getATSource(taposFus);
          }
      }catch(DataErrorException de){
          de.printStackTrace();
          return null;
      }
  }
  public boolean containsObject( int object ){
      return super.containsObject(object);
  }
  public void setData( session ses,int[] to_ido, String campo, Object newVal )
         throws SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException{
      super.setData(ses, unnfusionObject(to_ido[1],campo),unnfusField(campo),newVal);
  }
  public Integer getTAPOS( String campo ){
      return super.getTAPOS(campo);
  }
  public boolean fieldSupported( String campo ){
      return super.fieldSupported(unnfusField(campo));
  }
  public void resetRestriction( int[] to_ido, String idCampo)
	  throws NumberFormatException{
      super.resetRestriction(unnfusionObject( to_ido[1], idCampo ),unnfusField(idCampo));
  }
  public boolean isNull( session ses, int[] to_ido, String idCampo, Object newVal )
          throws SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException{
      return super.isNull(ses,unnfusionObject( to_ido[1], idCampo ),unnfusField(idCampo),newVal);
  }
  public Vector getDirectValues( int object ){
      return null;
  }
  public HashMap getNumericValues(session ses,int to,int object)
          throws SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException{
      //Esta funcion solo se utiliza por calculo, por lo que calculo deberaa rediserarse como una clase amiga, lo que implica darle
      //visibilidad a nivel de paquete.
      return super.getNumericValues(ses,to,object);
  }
  public HashMap getFiltrosDeRelacion(session ses, int object)
          throws SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException{
      return super.getFiltrosDeRelacion(ses,object);
  }
  public void setNewRestriction( session ses, int[]to_Ido,String idCampo, Element nodo)
          throws DataErrorException{
      super.setNewRestriction(ses,unnfusionObject( to_Ido[1],idCampo), unnfusField(idCampo),nodo);
  }
  public HashMap refreshFilterMap(session ses,int object)
          throws SystemException,ApplicationException,RemoteSystemException,CommunicationException,DataErrorException{
      return super.refreshFilterMap(ses,object);
  }
  private int[] get_IDO_FromFusionAT( Integer fusTapos ){
      System.out.println("getIDOFromFusAt:"+fusTapos);
      int[] res={getIdTo(),-1};
      if (!m_fusion.isAtFusioned(fusTapos)){
          System.out.println("getIDOFromFusAt, not AT fus");
	  return res;
      }else {
	  Integer idCtxRoot = getContexto( new Integer(-1));
	  if (!m_fusion.contextIsFussioned(idCtxRoot)){
              System.out.println("getIDOFromFusAt, AT fus but not Ctx");
	      return res; //podria estar fusionado el At desde otro contexto
	  }else{
              try{
                  Integer idCtxLow = m_fusion.getLowContextFromAT(fusTapos);
                  System.out.println("getIDOFromFusAt, AT fus and Ctx low " +
                                     idCtxLow);
                  //si el AT está fusionado desde otro contexto,
                  //este ctxLow no es low de el ctxRoot de arriba

                  try{
                      Iterator itr = getInstance().getRelationIterator(true);
                      while (itr.hasNext()) {
                          ObjectProperty rel = (ObjectProperty) itr.next();
                          Integer idoLow = new Integer(rel.getIdProp());
                          Integer idCtxObj = new Integer(rel.getIdoRel());
                          if (m_md.ctxIsSpecializedFrom(idCtxObj, idCtxLow)) {
                              System.out.println("getIDOFromFusAt, FOUND IDO " +
                                                 idoLow);
                              res[0]=new Integer(rel.getTypeProperty()).intValue();
                              res[1]=idoLow.intValue();
                              return res;
                          }
                          //Si está fusionado no puede haber más de un obj child con este contexto
                      }
                  }catch(RemoteSystemException de){de.printStackTrace();
                  }catch(CommunicationException de){de.printStackTrace();}
              }catch(DataErrorException de){de.printStackTrace();}

	  }
      }
      return res;
  }

  public instance getInstance(session ses)
          throws RemoteSystemException,CommunicationException{
        try {
            return getFusionInstance(ses);
        } catch (CommunicationException ex) {
            ex.printStackTrace();
            return null;
        } catch (RemoteSystemException ex) {
            ex.printStackTrace();
            return null;
        } catch (SystemException ex) {
            ex.printStackTrace();
            return null;
        }
  }

  public instance getUnnfusionInstance() throws RemoteSystemException,CommunicationException{
      return super.getInstance();
  }

  private instance getFusionInstance(session ses)
          throws SystemException,RemoteSystemException,CommunicationException{
      instance instSource= super.getInstance();
      instance atomClone= (instance)instSource.clone();
      if( m_fusion.fusionaInstancia(//ses,
                                    //m_currentScope,
                                    //m_md.getBusiness(),
                                    m_server,
                                    m_user,
                                    m_userRol,
                                    m_currentScope.getOperation(),
                                    m_currTask,
                                    getContexto(new Integer(-1)),
                                    atomClone))
             return atomClone;
         else
             return instSource;
  }
}
*/