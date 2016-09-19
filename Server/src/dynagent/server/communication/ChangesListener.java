package dynagent.server.communication;

import java.util.ArrayList;

import dynagent.common.communication.Changes;
import dynagent.common.communication.ObjectChanged;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.KnowledgeAdapter;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.values.DataValue;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.UnitValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.IChangePropertyListener;
import dynagent.server.ejb.AuxiliarEJB;

public class ChangesListener implements IChangePropertyListener {

	private Changes changes;
	private ArrayList<IPropertyDef> aipd;
	IKnowledgeBaseInfo ik;

	public ChangesListener(ArrayList<IPropertyDef> aipd, IKnowledgeBaseInfo ik) {
		changes = new Changes();
		this.aipd = aipd;
		this.ik = ik;
	}
	public Changes getChanges() {
		return this.changes;
	}
	public ArrayList<IPropertyDef> getAipd() {
		return aipd;
	}

	private IPropertyDef containsIPropertyDef(int ido, int idProp, Value value) throws NotFoundException, IncoherenceInMotorException {
		for (int i=0;i<aipd.size();i++) {
			IPropertyDef ipd = aipd.get(i);
			if (ipd.getIDO()!=null && ipd.getIDO()==ido && ipd.getPROP()==idProp) {
				//ver si value coincide con el valor actual y el oldValue con el antiguo
				if (value!=null) {
					KnowledgeAdapter ka = new KnowledgeAdapter(ik);
					Value ipdValue = ka.buildValue(ipd);
					if (ipdValue!=null && ipdValue.equals(value))
						return ipd;
				}
			}
		}
		return null;
	}

	public void initChangeValue() {
		// TODO Auto-generated method stub
		
	}
	public void changeValue(Integer ido, int idto, int idProp, int valueCls, Value value, Value oldValue, int level, int operation) {
		System.err.println("Cambia valor en ido:"+ido+" idto:"+idto+" idProp:"+idProp+" siendo la operacion:"+operation+" valueCls:"+valueCls+" y el value:"+value);
		//si son = se descartan
		System.out.println("changeValue");
		System.out.println("value " + value);
		System.out.println("oldValue " + oldValue);
		//Hay que tener en cuenta: ido=null -> es una clase
		int idoIdto = 0;
		if (ido==null)
			idoIdto = idto;
		else
			idoIdto = ido;
		//Cuando un fact se inserta en motor pero este no existia en base de datos el oldValue=null. Si existia value=oldValue.
		try{
			if (oldValue!=null && !(value!=null /*&& oldValue!=null*/ && value.equals(oldValue))) {
				IPropertyDef ipd = containsIPropertyDef(idoIdto, idProp, value);
				if (ipd==null) {
					//si no esta -> inserta en changes, si esta -> modificar 
					ObjectChanged oc = changes.getPropertyByIdo(idProp, idoIdto);
					if (oc==null) {
						oc = new ObjectChanged();
						oc.setProp(idProp);
						oc.setNewIdo(idoIdto);
						if (oldValue!=null)
							oc.setOldValue(oldValue);
						oc.setNewValue(value);
						changes.addObjectChanged(oc);
					} else {
						//aqui no hace falta el oldValue
						oc.setNewValue(value);
						//modificar aipd
						KnowledgeAdapter ka = new KnowledgeAdapter(ik);
						IPropertyDef newIpd = null;

						if (value instanceof DataValue) {
							DataProperty p = new DataProperty();
							p.setIdo(idoIdto);
							p.setIdto(idto);
							p.setIdProp(idProp);
							DataValue dv = (DataValue) value;
							newIpd = ka.traslateDataValueToIPropertyDef(p, dv);

						} else if (value instanceof ObjectValue) {
							ObjectProperty p = new ObjectProperty();
							p.setIdo(idoIdto);
							p.setIdto(idto);
							p.setIdProp(idProp);
							ObjectValue ov = (ObjectValue) value;
							newIpd = ka.traslateObjectValueToIPropertyDef(p, ov);
						}
						((FactInstance)ipd).setVALUE(newIpd.getVALUE());
						((FactInstance)ipd).setQMIN(newIpd.getQMIN());
						((FactInstance)ipd).setQMAX(newIpd.getQMAX());
						if (value instanceof UnitValue)
							((FactInstance)ipd).setVALUECLS(newIpd.getVALUECLS());
					}
				}
			}

		} catch (RemoteSystemException e) {
			AuxiliarEJB.error("ChangeListener, changeValue, error RemoteSystem:", e);
		} catch (CommunicationException e) {
			AuxiliarEJB.error("ChangeListener, changeValue, error Communication:", e);
		} catch (InstanceLockedException e) {
			AuxiliarEJB.error("ChangeListener, changeValue, error InstanceLockedException:", e);
		}  catch (NotFoundException e) {
			AuxiliarEJB.error("ChangeListener, changeValue, error NotFound:", e);
		} catch (SystemException e) {
			AuxiliarEJB.error("ChangeListener, changeValue, error System:", e);
		} catch (ApplicationException e) {
			AuxiliarEJB.error("ChangeListener, changeValue, error Application:", e);
		} catch (IncoherenceInMotorException e) {
			AuxiliarEJB.error("ChangeListener, changeValue, error IncoherenceInMotor:", e);
		} catch (IncompatibleValueException e) {
			AuxiliarEJB.error("ChangeListener, changeValue, error IncompatibleValue:", e);
		} catch (CardinalityExceedException e) {
			AuxiliarEJB.error("ChangeListener, changeValue, error CardinalityExceed:", e);
		} catch (OperationNotPermitedException e) {
			AuxiliarEJB.error("ChangeListener, changeValue, error OperationNotPermitedException:", e);
		}
	}

	public void endChangeValue() {
		// TODO Auto-generated method stub
		
	}
	
	public void addChangesServer(Changes newChanges) {
		//public void changeServerValue(int ido, Integer oldIdo, int idProp, Value value, Value oldValue) {
		ArrayList<ObjectChanged> newAoi = newChanges.getAObjectChanged();
		for (int i=0;i<newAoi.size();i++) {
			ObjectChanged newOc = newAoi.get(i);
			Integer idProp = newOc.getProp();
			int newIdo = newOc.getNewIdo();
			Integer oldIdo = newOc.getOldIdo();
			ObjectChanged oc = null;
			Value oldValue = null;
			Value newValue = null;

			oc = changes.getPropertyByIdo(idProp, newIdo);
			if (oc==null && oldIdo!=null)
				oc = changes.getPropertyByIdo(idProp, oldIdo);
			if (idProp!=null) {
				oldValue = newOc.getOldValue();
				newValue = newOc.getNewValue();
			}

			if (oc==null) {
				oc = new ObjectChanged();
				oc.setOldIdo(oldIdo);
				oc.setNewIdo(newIdo);
				oc.clsname=newOc.clsname;
				if (idProp!=null) {
					oc.setProp(idProp);
					oc.setNewValue(newValue);
					if (oldValue!=null)
						oc.setOldValue(oldValue);
				}
				changes.addObjectChanged(oc);
			} else if (idProp!=null) {
				oc.setOldValue(oldValue);
				oc.setNewValue(newValue);
			}
		}
	}
}
