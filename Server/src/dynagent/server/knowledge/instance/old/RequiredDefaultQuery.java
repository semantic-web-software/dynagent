/*package dynagent.knowledge.instance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import dynagent.ruleengine.Constants;
import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.meta.api.BooleanValue;
import dynagent.ruleengine.meta.api.DataProperty;
import dynagent.ruleengine.meta.api.DataValue;
import dynagent.ruleengine.meta.api.FloatValue;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.meta.api.IntValue;
import dynagent.ruleengine.meta.api.ObjectProperty;
import dynagent.ruleengine.meta.api.ObjectValue;
import dynagent.ruleengine.meta.api.Property;
import dynagent.ruleengine.meta.api.StringValue;
import dynagent.ruleengine.meta.api.TimeValue;
import dynagent.ruleengine.meta.api.UnitValue;
import dynagent.ruleengine.src.ruler.FactInstance;
import dynagent.ruleengine.src.ruler.IPropertyDef;


public class RequiredDefaultQuery {

	public static void requiredDefault(instance ins, IKnowledgeBaseInfo ik, ArrayList<Integer> idos, ArrayList<IPropertyDef> aReq) 
			throws NotFoundException, IncoherenceInMotorException {
		LinkedList<Integer> procesados = new LinkedList<Integer>();
		for (int i=0;i<idos.size();i++)
			subRequiredDefault(ins, ik, idos.get(i), aReq, procesados);
	}
	
	private static void subRequiredDefault(instance ins, IKnowledgeBaseInfo ik, int ido, ArrayList<IPropertyDef> aReq, LinkedList<Integer> procesados) 
			throws NotFoundException, IncoherenceInMotorException {
		if (!procesados.contains(ido)) {
			procesados.add(ido);
			Iterator it = ins.getAllPropertyIterator(ido, null, null, null, null);
			while (it.hasNext()) {
				Property p = (Property)it.next();
				if (p instanceof ObjectProperty)
					insertOPArray(ins, ik, aReq, (ObjectProperty)p, procesados);
				else if (p instanceof DataProperty)
					insertDPArray(ins, ik, aReq, (DataProperty)p, procesados);
			}
		}
	}
	private static void insertDPArray(instance ins, IKnowledgeBaseInfo ik, ArrayList<IPropertyDef> aReq, DataProperty dp, 
			LinkedList<Integer> procesados) {
		if (dp.getIdProp()==Constants.IdPROP_EXISTS) {
			LinkedList<DataValue> ldv = dp.getValueList();
			BooleanValue bv = (BooleanValue)ldv.get(0);
			Boolean value = bv.getBvalue();
			if (value!=null) {
				IPropertyDef rq = (IPropertyDef) new FactInstance(dp.getIdto(), dp.getIdo(), dp.getIdProp(), String.valueOf(value), 
						null, null, null, null, dp.getName());
				aReq.add(rq);
			}
		} else {
			LinkedList<DataValue> ldv = dp.getValueList();
			//si es un DataProperty con valueList es requerido
			for (int i=0;i<ldv.size();i++) {
				DataValue dv = ldv.get(i);
		        if (dv instanceof UnitValue) {
					UnitValue uv = (UnitValue)dv;
					Float vMin = uv.getValueMin();
					Float vMax = uv.getValueMax();
					Integer clase = uv.getUnit();
					if (vMin!=null || vMax!=null || clase!=null) {
						IPropertyDef rq = (IPropertyDef) new FactInstance(dp.getIdto(), dp.getIdo(), dp.getIdProp(), 
								null, clase, vMin, vMax, null, dp.getName());
						aReq.add(rq);
					}
				} else if (dv instanceof FloatValue) {
					FloatValue fv = (FloatValue)dv;
					Float vMin = fv.getValueMin();
					Float vMax = fv.getValueMax();
					if (vMin!=null  || vMax!=null) {
						IPropertyDef rq = (IPropertyDef) new FactInstance(dp.getIdto(), dp.getIdo(), dp.getIdProp(), 
								null, null, vMin, vMax, null, dp.getName());
						aReq.add(rq);
					}
				} else if (dv instanceof IntValue) {
					IntValue iv = (IntValue)dv;
					Integer vMin = iv.getValueMin();
					Integer vMax = iv.getValueMax();
					if (vMin!=null  || vMax!=null) {
						Float vMinF = null;
						Float vMaxF = null;
						if (vMin!=null)
							vMinF = Float.parseFloat(String.valueOf(vMin));
						if (vMax!=null)
							vMaxF = Float.parseFloat(String.valueOf(vMax));
	
						IPropertyDef rq = (IPropertyDef) new FactInstance(dp.getIdto(), dp.getIdo(), dp.getIdProp(), null, null, 
								vMinF, vMaxF, null, dp.getName());
						aReq.add(rq);
					}
				} else if (dv instanceof BooleanValue) {
					BooleanValue bv = (BooleanValue)dv;
					Boolean value = bv.getBvalue();
					if (value!=null) {
						IPropertyDef rq = (IPropertyDef) new FactInstance(dp.getIdto(), dp.getIdo(), dp.getIdProp(), String.valueOf(value), 
								null, null, null, null, dp.getName());
						aReq.add(rq);
					}
				} else if (dv instanceof StringValue){
		        	StringValue sv = (StringValue)dv;
		        	String value = sv.getValue();
					if (value!=null) {
						IPropertyDef rq = (IPropertyDef) new FactInstance(dp.getIdto(), dp.getIdo(), dp.getIdProp(), String.valueOf(value), 
								null, null, null, null, dp.getName());
						aReq.add(rq);
		        	}
				} else if (dv instanceof TimeValue){
					TimeValue tv = (TimeValue)dv;
					Float value = tv.getRelativeSeconds();
					if (value!=null) {
						IPropertyDef rq = (IPropertyDef) new FactInstance(dp.getIdto(), dp.getIdo(), dp.getIdProp(), 
								null, null, value, value, null, dp.getName());
						aReq.add(rq);
					}
				}
			}
		}
	}

	private static void insertOPArray(instance ins, IKnowledgeBaseInfo ik, ArrayList<IPropertyDef> aReq, ObjectProperty op,
			LinkedList<Integer> procesados) throws NotFoundException, IncoherenceInMotorException {
		// mirar los value que estan en value o en enum y despues iterar por rango
		LinkedList<ObjectValue> lov2 = op.getValueList();
		for (int j=0;j<lov2.size();j++) {
			ObjectValue ov2 = lov2.get(j);
			int valueCls2 = ov2.getValueCls();
			Integer value2 = ov2.getValue();
			LinkedList<Integer> li = op.getRangoList();
			LinkedList<ObjectValue> lov = new LinkedList<ObjectValue>();
			if (li.size()==0)
				lov = op.getEnumList();
			for (int i=0;i<li.size();i++) {
				int valueCls = ik.getClassOf(li.get(i));
				if (valueCls==valueCls2 && value2!=null) {
					IPropertyDef rq = (IPropertyDef) new FactInstance(op.getIdto(), op.getIdo(), op.getIdProp(), 
							String.valueOf(value2), valueCls2, null, null, null, op.getName());
					aReq.add(rq);
					break;
				}
			}
			for (int i=0;i<lov.size();i++) {
				ObjectValue ov = lov.get(i);
				int valueCls = ov.getValueCls();
				if (valueCls==valueCls2 && value2!=null) {
					IPropertyDef rq = (IPropertyDef) new FactInstance(op.getIdto(), op.getIdo(), op.getIdProp(), 
							String.valueOf(value2), valueCls, null, null, null, op.getName());
					aReq.add(rq);
					break;
				}
			}
		}
		LinkedList<Integer> li = op.getRangoList();
		for (int i=0;i<li.size();i++) {
			subRequiredDefault(ins, ik, li.get(i), aReq, procesados);
		}
	}
}*/