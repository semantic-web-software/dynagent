package dynagent.ruleengine.src.ruler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jess.Funcall;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Token;
import jess.Value;
import jess.ValueVector;
import jess.Variable;
import dynagent.ruleengine.Null;
import dynagent.ruleengine.OrObject;
import dynagent.ruleengine.Exceptions.EngineException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.src.data.dao.Access;
import dynagent.ruleengine.src.data.dao.Instance;
import dynagent.ruleengine.src.data.dao.Properties;
import dynagent.ruleengine.src.data.dao.T_Herencias;
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.ruleengine.src.sessions.SessionController;
import dynagent.server.exceptions.ApplicationException;

public class JessEngine extends CommonEngine implements IRuleEngine{
	private Rete ruler;
	
	
	public Rete getRuler() {
		return ruler;
	}

	public void setRuler(Rete ruler) {
		this.ruler = ruler;
		//createTemplates();
	}
	
	public JessEngine(Rete r){
		ruler = r;
	
		//createTemplates();
	}
	

	
	
	
	
	
	public int deleteFactCondOLD1(Object idto, Object ido, Object prop, Object valuecls, Object value, Object qMax, Object qMin, Object op) throws NotFoundException {
		int numdelete = 0;
		Rete r = this.getRuler();
		
		try {
			String command = "(defquery search (instance ";
			command+=buildInstanceCondition(idto, ido, prop, valuecls, value, null, qMax, qMin, op);
			command+="))";
			r.executeCommand(command);
			Iterator e = r.runQuery("search", new ValueVector());
			
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				jess.Fact f = t.fact(1);
				Value vjess= f.getSlotValue("OBJECT");
				Fact fact=(Fact)vjess.externalAddressValue(r.getGlobalContext());
				fact.setQMAX(null);
				fact.setQMIN(null);
				fact.setVALUE(null);
				fact.setVALUECLS(null);
				fact.setOP(null);
				fact.setDeleted(true);
				numdelete++;
			}
		}catch(JessException ex){
			ex.printStackTrace();
		}
		return numdelete;
	}

	
	

	
	
	public int deleteFactCondRETRACT(Object idto, Object ido, Object prop, Object valuecls, Object value, Object qMax, Object qMin,Object op) {
		int numdelete = 0;
		Rete r = this.getRuler();
		try {
			String command = "(defquery search (instance ";
			command+=buildInstanceCondition(idto, ido, prop, valuecls, value, null, qMax, qMin, op);
			command+="))";
			r.executeCommand(command);
			Iterator e = r.runQuery("search", new ValueVector());
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				jess.Fact f = t.fact(1);
				r.retract(f);
				numdelete++;
			}
		}catch(JessException ex){
			ex.printStackTrace();
		}
		return numdelete;
	}
	
	


	
	public void clearRuler()  {
		Rete r = this.getRuler();
		try {
			r.clear();
		} catch (JessException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public LinkedList<FactAccess> getAllAccessFacts(Object utask, Object idto, Object accesstype, Object ido,Object prop, Object value, Object valuecls, Object userrol, Object user) {
		LinkedList<dynagent.ruleengine.src.ruler.FactAccess> v = new LinkedList<dynagent.ruleengine.src.ruler.FactAccess>();
		Rete r = this.getRuler();		
		try {
			String command = "(defquery search (access ";
			command+=buildAccessCondition(utask, idto, accesstype, ido, prop, value, valuecls, userrol, user);
			command+="))";
			r.executeCommand(command);
			Iterator e= r.runQuery("search", new ValueVector());
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				jess.Fact f = t.fact(1);
				Value vjess= f.getSlotValue("OBJECT");
				FactAccess fact=(FactAccess)vjess.externalAddressValue(r.getGlobalContext());
			
				v.add(fact);
			}
				
		
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		return v;
	}
	
	
	
	
	private String buildAccessCondition(Object utask, Object idto, Object accesstype, Object ido, Object prop, Object value, Object valuecls, Object userrol, Object user) {
	String res="";
		
		if(utask!=null){
			if(utask instanceof OrObject){
				OrObject orutask = (OrObject) utask;
				res+=" (TASK ";
				Object utaskParam1 = orutask.getFirstOp();
				Object utaskParam2 = orutask.getSecondOp();
				if(utaskParam1 instanceof Null ){
					Null nutaskParam1 = (Null) utaskParam1;
					if(nutaskParam1.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?t&~nil ";
				}
				else
					res+=((Integer) utaskParam1).intValue();
				res+=" | ";
				
				if(utaskParam2 instanceof Null ){
					Null nutaskParam2 = (Null) utaskParam2;
					if(nutaskParam2.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?t&~nil ";
				}
				else
					res+=((Integer) utaskParam2).intValue();
				
				res+=")";
				
			}
				
				
			else if(utask instanceof Null ){
				Null nutask = (Null) utask;
				if(nutask.getSearchBy()==Null.NULL)
					res+=" (TASK nil) ";
				else
					res+=" (TASK ?t&~nil) ";
			}
			else
				res+=" (TASK "+((Integer) utask).intValue()+") ";
		}
		
		if(idto!=null){
			if(idto instanceof OrObject){
				OrObject oruidto = (OrObject) idto;
				res+=" (IDTO ";
				Object idtoParam1 = oruidto.getFirstOp();
				Object idtoParam2 = oruidto.getSecondOp();
				if(idtoParam1 instanceof Null ){
					Null nidtoParam1 = (Null) idtoParam1;
					if(nidtoParam1.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?i&~nil ";
				}
				else
					res+=((Integer) idtoParam1).intValue();
				res+=" | ";
				
				if(idtoParam2 instanceof Null ){
					Null nidtoParam2 = (Null) idtoParam2;
					if(nidtoParam2.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?i&~nil ";
				}
				else
					res+=((Integer) idtoParam2).intValue();
				
				res+=")";
				
			}	
			else if(idto instanceof Null ){
				Null nidto = (Null) idto;
				if(nidto.getSearchBy()==Null.NULL)
					res+=" (IDTO nil) ";
				else
					res+=" (IDTO ?i&~nil) ";
			}
			else
				res+=" (IDTO "+((Integer) idto).intValue()+") ";
		}
		
		if(accesstype!=null){
			if(accesstype instanceof OrObject){
				OrObject oruaccesstype = (OrObject) accesstype;
				res+=" (ACCESSTYPE ";
				Object accesstypeParam1 = oruaccesstype.getFirstOp();
				Object accesstypeParam2 = oruaccesstype.getSecondOp();
				if(accesstypeParam1 instanceof Null ){
					Null naccesstypeParam1 = (Null) accesstypeParam1;
					if(naccesstypeParam1.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?a&~nil ";
				}
				else
					res+=((Integer) accesstypeParam1).intValue();
				res+=" | ";
				
				if(accesstypeParam2 instanceof Null ){
					Null naccesstypeParam2 = (Null) accesstypeParam2;
					if(naccesstypeParam2.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?a&~nil ";
				}
				else
					res+=((Integer) accesstypeParam2).intValue();
				
				res+=")";
				
			}	
			else if(accesstype instanceof Null ){
				Null naccesstype = (Null) accesstype;
				if(naccesstype.getSearchBy()==Null.NULL)
					res+=" (ACCESSTYPE nil) ";
				else
					res+=" (ACCESSTYPE ?a&~nil) ";
			}
			else
				res+=" (ACCESSTYPE "+((Integer) accesstype).intValue()+") ";
		}
		
		
		if(ido!=null){
			if(ido instanceof OrObject){
				OrObject oruido = (OrObject) ido;
				res+=" (IDO ";
				Object idoParam1 = oruido.getFirstOp();
				Object idoParam2 = oruido.getSecondOp();
				if(idoParam1 instanceof Null ){
					Null nidoParam1 = (Null) idoParam1;
					if(nidoParam1.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?d&~nil ";
				}
				else
					res+=((Integer) idoParam1).intValue();
				res+=" | ";
				
				if(idoParam2 instanceof Null ){
					Null nidoParam2 = (Null) idoParam2;
					if(nidoParam2.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?d&~nil ";
				}
				else
					res+=((Integer) idoParam2).intValue();
				
				res+=")";
				
			}	
			else if(ido instanceof Null ){
				Null nido = (Null) ido;
				if(nido.getSearchBy()==Null.NULL)
					res+=" (IDO nil) ";
				else
					res+=" (IDO ?d&~nil) ";
			}
			else
				res+=" (IDO "+((Integer) ido).intValue()+") ";
		}
		
		if(prop!=null){
			if(prop instanceof OrObject){
				OrObject oruprop = (OrObject) prop;
				res+=" (PROP ";
				Object propParam1 = oruprop.getFirstOp();
				Object propParam2 = oruprop.getSecondOp();
				if(propParam1 instanceof Null ){
					Null npropParam1 = (Null) propParam1;
					if(npropParam1.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?p&~nil ";
				}
				else
					res+=((Integer) propParam1).intValue();
				res+=" | ";
				
				if(propParam2 instanceof Null ){
					Null npropParam2 = (Null) propParam2;
					if(npropParam2.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?p&~nil ";
				}
				else
					res+=((Integer) propParam2).intValue();
				
				res+=")";
				
			}	
			else if(prop instanceof Null ){
				Null nprop = (Null) prop;
				if(nprop.getSearchBy()==Null.NULL)
					res+=" (PROP nil) ";
				else
					res+=" (PROP ?p&~nil) ";
			}
			else
				res+=" (PROP "+((Integer) prop).intValue()+") ";
		}
		
		if(value!=null){
			if(value instanceof OrObject){
				OrObject oruvalue = (OrObject) value;
				res+=" (VALUE ";
				Object valueParam1 = oruvalue.getFirstOp();
				Object valueParam2 = oruvalue.getSecondOp();
				if(valueParam1 instanceof Null ){
					Null nvalueParam1 = (Null) valueParam1;
					if(nvalueParam1.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?l&~nil ";
				}
				else
					res+=valueParam1;
				res+=" | ";
				
				if(valueParam2 instanceof Null ){
					Null nvalueParam2 = (Null) valueParam2;
					if(nvalueParam2.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?l&~nil ";
				}
				else
					res+=valueParam2;
				
				res+=")";
				
			}	
			else if(value instanceof Null ){
				Null nvalue = (Null) value;
				if(nvalue.getSearchBy()==Null.NULL)
					res+=" (VALUE nil) ";
				else
					res+=" (VALUE ?l&~nil) ";
			}
			else
				res+=" (VALUE "+value+") ";
		}
		
		if(user!=null){
			if(user instanceof OrObject){
				OrObject oruser = (OrObject) value;
				res+=" (USER ";
				Object userParam1 = oruser.getFirstOp();
				Object userParam2 = oruser.getSecondOp();
				if(userParam1 instanceof Null ){
					Null nuserParam1 = (Null) userParam1;
					if(nuserParam1.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?x&~nil ";
				}
				else
					res+=userParam1;
				res+=" | ";
				
				if(userParam2 instanceof Null ){
					Null nuserParam2 = (Null) userParam2;
					if(nuserParam2.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?x&~nil ";
				}
				else
					res+=userParam2;
				
				res+=")";
				
			}	
			else if(user instanceof Null ){
				Null nuser = (Null) user;
				if(nuser.getSearchBy()==Null.NULL)
					res+=" (USER nil) ";
				else
					res+=" (USER ?x&~nil) ";
			}
			else
				res+=" (USER "+user+") ";
		}
		if(valuecls!=null){
			if(valuecls instanceof OrObject){
				OrObject oruvaluecls = (OrObject) valuecls;
				res+=" (VALUECLS ";
				Object valueclsParam1 = oruvaluecls.getFirstOp();
				Object valueclsParam2 = oruvaluecls.getSecondOp();
				if(valueclsParam1 instanceof Null ){
					Null nvalueclsParam1 = (Null) valueclsParam1;
					if(nvalueclsParam1.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?v&~nil ";
				}
				else
					res+=valueclsParam1;
				res+=" | ";
				
				if(valueclsParam2 instanceof Null ){
					Null nvalueclsParam2 = (Null) valueclsParam2;
					if(nvalueclsParam2.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?v&~nil ";
				}
				else
					res+=valueclsParam2;
				
				res+=")";
				
			}	
			else if(valuecls instanceof Null ){
				Null nvaluecls = (Null) valuecls;
				if(nvaluecls.getSearchBy()==Null.NULL)
					res+=" (VALUECLS nil) ";
				else
					res+=" (VALUECLS ?v&~nil) ";
			}
			else
				res+=" (VALUECLS "+valuecls+") ";
		}
		
		if(userrol!=null){
			if(userrol instanceof OrObject){
				OrObject oruuserrol = (OrObject) userrol;
				res+=" (USERROL ";
				Object userrolParam1 = oruuserrol.getFirstOp();
				Object userrolParam2 = oruuserrol.getSecondOp();
				if(userrolParam1 instanceof Null ){
					Null nuserrolParam1 = (Null) userrolParam1;
					if(nuserrolParam1.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?u&~nil ";
				}
				else
					res+=((Integer) userrolParam1).intValue();
				res+=" | ";
				
				if(userrolParam2 instanceof Null ){
					Null nuserrolParam2 = (Null) userrolParam2;
					if(nuserrolParam2.getSearchBy()==Null.NULL)
						res+=" nil ";
					else
						res+=" ?u&~nil ";
				}
				else
					res+=((Integer) userrolParam2).intValue();
				
				res+=")";
				
			}	
			else if(userrol instanceof Null ){
				Null nuserrol = (Null) userrol;
				if(nuserrol.getSearchBy()==Null.NULL)
					res+=" (USERROL nil) ";
				else
					res+=" (USERROL ?u&~nil) ";
			}
			else
				res+=" (USERROL "+((Integer) userrol).intValue()+") ";
		}
		
		return res;
	}

	
	
	
	
	public LinkedList<FactHierarchy> getAllHierarchyFacts(Object idto, Object idtoSup) {
		Rete r = this.getRuler();
		LinkedList<dynagent.ruleengine.src.ruler.FactHierarchy> v = new LinkedList<dynagent.ruleengine.src.ruler.FactHierarchy>();
		try {
			String command = "(defquery search (hierarchy ";
			command+=buildHierarchyCondition(idto, idtoSup);
			command+="))";
			r.executeCommand(command);
			//r.reset();
			//r.store("RESULT", r.runQuery("search", new ValueVector()));
			Iterator e= r.runQuery("search", new ValueVector());
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				jess.Fact f = t.fact(1);
				Value vjess= f.getSlotValue("OBJECT");
				dynagent.ruleengine.src.ruler.FactHierarchy fact=(dynagent.ruleengine.src.ruler.FactHierarchy)vjess.externalAddressValue(r.getGlobalContext());
				v.add(fact);
			}
				
		
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		return v;
	}

	

	public LinkedList<FactProp> getAllPropertyFacts(Object prop, Object name) {
		
		Iterator it = null;
		Rete r = this.getRuler();
		LinkedList<dynagent.ruleengine.src.ruler.FactProp> v= new LinkedList<dynagent.ruleengine.src.ruler.FactProp>();
		try {
			String command = "(defquery search (property";
			command+=buildPropCondition(prop, name);
			command+="))";
			r.executeCommand(command);
			//r.reset();
			//r.store("RESULT", r.runQuery("search", new ValueVector()));
			Iterator e= r.runQuery("search", new ValueVector());
			v = new LinkedList<dynagent.ruleengine.src.ruler.FactProp>();
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				jess.Fact f = t.fact(1);
				Value vjess= f.getSlotValue("OBJECT");
				dynagent.ruleengine.src.ruler.FactProp factprop=(dynagent.ruleengine.src.ruler.FactProp)vjess.externalAddressValue(r.getGlobalContext());
				v.add(factprop);
			}
			
		
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		return v;
	}

	public void insertFact(Object fact) {
		if(fact instanceof Properties)
			insertFactProp((Properties) fact);
		else if(fact instanceof Instance)
			insertFactIns((Instance) fact);
		else if(fact instanceof T_Herencias)
			insertFactHierarchy((T_Herencias) fact);
		else if(fact instanceof Access)
			insertFactAccess((Access) fact);
		else if(fact instanceof Fact)
			insertFactInstance((Fact) fact);
		else if(fact instanceof FactInstance)
			insertFactInstance((FactInstance) fact);
		else{
			System.err.println("   WARNING: JessEngine.inserFact(with "+fact+"    no lo insertará");
		}
	}

	private void insertFactInstance(IPropertyDef fi) {
		/*try {
			Fact f = fi.toFact();
			Rete r = this.getRuler();
			Funcall fu;
			fu = new Funcall("definstance", r);
			fu.add(new Value("instance",RU.ATOM));
			fu.add(new Value(f));
			fu.add(new Value((f.getIDO()==null) ? "static":"dynamic",RU.ATOM));
			fu.execute(r.getGlobalContext());
			} catch (JessException e) {
				e.printStackTrace();
			}*/
			
	}

	private void insertFactAccess(Access access) {
		try {
			
			Rete r = this.getRuler();
			Funcall fu = new Funcall("definstance", r);
			fu.add(new Value("access",RU.ATOM));
			FactAccess fa=access.toFactAccess();
			fu.add(new Value(fa));
			fu.add(new Value("static",RU.ATOM));
			
			fu.execute(r.getGlobalContext());
		} catch (JessException e) {
			e.printStackTrace();
		}
		
	}

	private void insertFactHierarchy(T_Herencias herencias) {
		try {
			FactHierarchy f = (FactHierarchy)herencias.toFactHierarchy();
			Rete r = this.getRuler();
			Funcall fu = new Funcall("definstance", r);
			fu.add(new Value("hierarchy",RU.ATOM));
			fu.add(new Value(f));
			fu.add(new Value("static",RU.ATOM));
			fu.execute(r.getGlobalContext());
		} catch (JessException e) {
			e.printStackTrace();
		}
		
	}

	
	
	private void insertFactIns(Instance instance) {
		/*try {
			
		Rete r = this.getRuler();
		//Fact f = Fact.toFact(instance);
		Funcall fu;
		fu = new Funcall("definstance", r);
		fu.add(new Value("instance",RU.ATOM));
		fu.add(new Value(f));
		fu.add(new Value("static",RU.ATOM));
		fu.execute(r.getGlobalContext());
		} catch (JessException e) {
			e.printStackTrace();
		}
		*/
	}

	private void insertFactProp(Properties properties) {
		try {
			Rete r = this.getRuler();
			Funcall fu = new Funcall("definstance", r);
			FactProp f = (FactProp)properties.toFactProp();
			fu.add(new Value("property",RU.ATOM));
			fu.add(new Value(f));
			fu.add(new Value("static",RU.ATOM));
			fu.execute(r.getGlobalContext());
		} catch (JessException e) {
			e.printStackTrace();
		}
		
	}

	public int modify(IPropertyDef fact, String slot, String value) {
		Rete r = this.getRuler();
		LinkedList<IPropertyDef> v = new LinkedList<IPropertyDef>();
		int numafectados=0;
		try {
			String command = "(defquery search "+fact.toQueryString()+"))";
			r.executeCommand(command);
			Iterator e = r.runQuery("search", new ValueVector());
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				jess.Fact f = t.fact(1);
				Value val = this.obtainValueType(slot,value);				
				jess.Fact newf = r.modify(f, slot, val);
				numafectados++;
			}
			
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		return numafectados;
	}


	private Value obtainValueType(String slot, String value) throws JessException{
		if(value==null)
			return new Value("nil",RU.ATOM);
		if(slot.equals("NAME") || slot.equals("VALUE") || slot.equals("OP") || slot.equals("TIMESTP")){
			return new Value(value,RU.STRING);
		}else if(slot.equals("QMIN") || slot.equals("QMAX")){
			return new Value(new Float(value),RU.FLOAT);
		}else
			return new Value(new Integer(value),RU.INTEGER);
	}
	
	private String buildInstanceCondition(Object idto, Object ido, Object prop, Object valuecls, Object value,Object name, Object qMax, Object qMin, Object op){
		String res = "";
		if(idto!=null){
			if(idto instanceof Null ){
				Null nidto = (Null) idto;
				if(nidto.getSearchBy()==Null.NULL)
					res+=" (IDTO nil) ";
				else
					res+=" (IDTO ?i&~nil) ";
			}
			else
				res+=" (IDTO "+((Integer) idto).intValue()+") ";
		}
		
		if(ido!=null){
			if(ido instanceof Null ){
				Null nido = (Null) ido;
				if(nido.getSearchBy()==Null.NULL)
					res+=" (IDO nil) ";
				else
					res+=" (IDO ?d&~nil) ";
			}
			else
				res+=" (IDO "+((Integer) ido).intValue()+") ";
		}
		
		if(prop!=null){
			if(prop instanceof Null ){
				Null nprop = (Null) prop;
				if(nprop.getSearchBy()==Null.NULL)
					res+=" (PROP nil) ";
				else
					res+=" (PROP ?p&~nil) ";
			}
			else
				res+=" (PROP "+((Integer) prop).intValue()+") ";
		}
		
		if(valuecls!=null){
			if(valuecls instanceof Null ){
				Null nvaluecls = (Null) valuecls;
				if(nvaluecls.getSearchBy()==Null.NULL)
					res+=" (VALUECLS nil) ";
				else
					res+=" (VALUECLS ?v&~nil) ";
			}
			else
				res+=" (VALUECLS "+((Integer) valuecls).intValue()+") ";
		}
		
		if(value!=null){
			if(value instanceof Null){
				Null nvalue = (Null) value;
				if(nvalue.getSearchBy()==Null.NULL)
					res+=" (VALUE nil) ";
				else
					res+=" (VALUE ?l&~nil) ";
			}
			else 
				res+=" (VALUE "+'"'+value+'"'+") ";
		}
		
		if(name!=null){
			if(name instanceof Null ){
				Null nname = (Null) name;
				if(nname.getSearchBy()==Null.NULL)
					res+=" (NAME nil) ";
				else
					res+=" (NAME ?n&~nil) ";
			}
			
			else
				res+=" (NAME "+'"'+(String) name+'"'+") ";
		}
		
		if(qMin!=null){
			if(qMin instanceof Null ){
				Null nqmin = (Null) qMin;
				if(nqmin.getSearchBy()==Null.NULL)
					res+=" (QMIN nil) ";
				else
					res+=" (QMIN ?q&~nil) ";
			}
			else
				res+=" (QMIN "+  qMin+") ";
		}
		
		if(qMax!=null){
			if(qMax instanceof Null ){
				Null nqmax = (Null) qMax;
				if(nqmax.getSearchBy()==Null.NULL)
					res+=" (QMAX nil) ";
				else
					res+=" (QMAX ?r&~nil) ";
			}
			else
				res+=" (QMAX "+  qMax+")";
		}
		
		
		if(op!=null){
			if(op instanceof Null ){
				Null nop = (Null) op;
				if(nop.getSearchBy()==Null.NULL)
					res+=" (OP nil) ";
				else
					res+=" (OP ?q&~nil) ";
			}
			else
				res+=" (OP "+ op+")";
		}
		return res;
		
	}
	
	private String buildPropCondition(Object prop, Object name){
		String res="";
		
		if(prop!=null){
			if(prop instanceof Null ){
				Null nprop = (Null) prop;
				if(nprop.getSearchBy()==Null.NULL)
					res+=" (PROP nil) ";
				else
					res+=" (PROP ?p&~nil) ";
			}
			else
				res+=" (PROP "+((Integer) prop).intValue()+") ";
		}
		
		if(name!=null){
			if(name instanceof Null ){
				Null nname = (Null) name;
				if(nname.getSearchBy()==Null.NULL)
					res+=" (NAME nil) ";
				else
					res+=" (NAME ?n&~nil) ";
			}
			else
				res+=" (NAME "+'"'+(String) name+'"'+") ";
		}
		
			return res;
		
	
	}
	
	private String buildHierarchyCondition(Object idto, Object idtoSup){
		String res="";
		
		if(idto!=null){
			if(idto instanceof Null ){
				Null nidto = (Null) idto;
				if(nidto.getSearchBy()==Null.NULL)
					res+=" (IDTO nil) ";
				else
					res+=" (IDTO ?i&~nil) ";
			}
			else
				res+=" (IDTO "+((Integer) idto).intValue()+") ";
		}
		
		if(idtoSup!=null){
			if(idtoSup instanceof Null ){
				Null nido = (Null) idtoSup;
				if(nido.getSearchBy()==Null.NULL)
					res+=" (IDTOSUP nil) ";
				else
					res+=" (IDTOSUP ?s&~nil) ";
			}
			else
				res+=" (IDTOSUP "+((Integer) idtoSup).intValue()+") ";
		}
		
		return res;
	}
	/*
	private String buildAccessCondition(Object utask, Object accesstype, Object accesstype, Object ido,Object prop, Object value, Object valuecls, Object userrol){
		String res="";
		
		if(utask!=null){
			if(utask instanceof Null ){
				Null nutask = (Null) utask;
				if(nutask.getSearchBy()==Null.NULL)
					res+=" (TASK nil) ";
				else
					res+=" (TASK ?t&~nil) ";
			}
			else
				res+=" (TASK "+((Integer) utask).intValue()+") ";
		}
		
		if(accesstype!=null){
			if(accesstype instanceof Null ){
				Null naccesstype = (Null) accesstype;
				if(naccesstype.getSearchBy()==Null.NULL)
					res+=" (accesstype nil) ";
				else
					res+=" (accesstype ?i&~nil) ";
			}
			else
				res+=" (accesstype "+((Integer) accesstype).intValue()+") ";
		}
		
		if(accesstype!=null){
			if(accesstype instanceof Null ){
				Null naccesstype = (Null) accesstype;
				if(naccesstype.getSearchBy()==Null.NULL)
					res+=" (ACCESSTYPE nil) ";
				else
					res+=" (ACCESSTYPE ?a&~nil) ";
			}
			else
				res+=" (ACCESSTYPE "+((Integer) accesstype).intValue()+") ";
		}
		
		if(ido!=null){
			if(ido instanceof Null ){
				Null nido = (Null) ido;
				if(nido.getSearchBy()==Null.NULL)
					res+=" (IDO nil) ";
				else
					res+=" (IDO ?d&~nil) ";
			}
			else
				res+=" (IDO "+((Integer) ido).intValue()+"| nil) ";
		}
		
		if(prop!=null){
			if(prop instanceof Null ){
				Null nprop = (Null) prop;
				if(nprop.getSearchBy()==Null.NULL)
					res+=" (PROP nil) ";
				else
					res+=" (PROP ?p&~nil) ";
			}
			else
				res+=" (PROP "+((Integer) prop).intValue()+"| nil ) ";
		}

		if(value!=null){
			if(value instanceof Null ){
				Null nvalue = (Null) value;
				if(nvalue.getSearchBy()==Null.NULL)
					res+=" (VALUE nil) ";
				else
					res+=" (VALUE ?l&~nil) ";
			}
			res+=" (VALUE "+'"'+value.toString()+'"'+"| nil ) ";
		}
		
		if(valuecls!=null){
			if(valuecls instanceof Null ){
				Null nvaluecls = (Null) valuecls;
				if(nvaluecls.getSearchBy()==Null.NULL)
					res+=" (VALUECLS nil) ";
				else
					res+=" (VALUECLS ?v&~nil) ";
			}
			else
				res+=" (VALUECLS "+((Integer) valuecls).intValue()+"| nil ) ";
		}
		
		if(userrol!=null){
			if(userrol instanceof Null ){
				Null nuserrol = (Null) userrol;
				if(nuserrol.getSearchBy()==Null.NULL)
					res+=" (USERROL nil) ";
				else
					res+=" (USERROL ?u&~nil) ";
			}
			else
				res+=" (USERROL "+((Integer) valuecls).intValue()+"| nil ) ";
		}
		
		return res;
	}
*/
	
	public LinkedList<IPropertyDef> getAllInstanceFacts(Object idto, Object ido, Object prop, Object valuecls, Object value, Object name, Object qMax, Object qMin,Object op ) {
		LinkedList<IPropertyDef> facts=this.getAllInstanceFactsNoRevert(idto, ido, prop, valuecls, value, name, qMax, qMin, op);
		LinkedList<IPropertyDef> lfactsins=new LinkedList<IPropertyDef>();
		for(int i=0;i<facts.size();i++){
			lfactsins.add(facts.get(i).toFactInstance());
		}
		return lfactsins;
	}
	
	
	protected LinkedList<IPropertyDef> getAllInstanceFactsNoRevert(Object idto, Object ido, Object prop, Object valuecls, Object value, Object name, Object qMax, Object qMin,Object op ) {
		Rete r = this.getRuler();
		LinkedList<IPropertyDef> v= new LinkedList<IPropertyDef>();
		try {
			String command = "(defquery search (instance ";
			command+=buildInstanceCondition(idto, ido, prop, valuecls, value, name, qMax, qMin,op);
			//EXCLUIMOS DE LA CONSULTA LOS FACTS QUE HAN SIDO BORRADOS DENTRO DE UNA SESION
			//command+="(deleted FALSE)";
			command+="))";
			//System.out.println(command);
			r.executeCommand(command);
			Iterator e= r.runQuery("search", new ValueVector());
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				jess.Fact f = t.fact(1);
				Value vjess= f.getSlotValue("OBJECT");
				IPropertyDef fact=(Fact)vjess.externalAddressValue(r.getGlobalContext());
				if(!fact.isTemporalDeleted())
					v.add(fact);
			}
		
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		return v;
	}
	
	
	
	
	public LinkedList<IPropertyDef> getAllInstanceFactsDELETED(Object idto, Object ido, Object prop, Object valuecls, Object name) {
		Rete r = this.getRuler();
		LinkedList<IPropertyDef> v= new LinkedList<IPropertyDef>();
		try {
			String command = "(defquery search (instance ";
			command+=buildInstanceCondition(idto, ido, prop, valuecls, null, name,null, null, null);
			command+="))";
			//System.out.println(command);
			r.executeCommand(command);
			Iterator e= r.runQuery("search", new ValueVector());
			v = new LinkedList<IPropertyDef>();
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				jess.Fact f = t.fact(1);
				Value vjess= f.getSlotValue("OBJECT");
				Fact fact=(Fact)vjess.externalAddressValue(r.getGlobalContext());
				if(fact.isTemporalDeleted())
					v.add(fact);
				
			}
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		return v;
	}
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//FUNCIONES DE JESS TAL Y COMO ESTABAN EN DOCDATAMODEL
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private int deleteFactCondRETRACT(String cond){
		int numdelete = 0;
		Rete r = this.getRuler();
		LinkedList<dynagent.ruleengine.src.ruler.Fact> v = new LinkedList<dynagent.ruleengine.src.ruler.Fact>();
		try {
			String command = "(defquery search "+cond;
			
			r.executeCommand(command);
			Iterator e = r.runQuery("search", new ValueVector());
			
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				jess.Fact f = t.fact(1);
				r.retract(f);
				numdelete++;
			}
		}catch(JessException ex){
			ex.printStackTrace();
		}
		return numdelete;
	}
	
	
	
	
	
	
	
	
	
	
	public int modify (dynagent.ruleengine.src.ruler.Fact fact, String slot, String value) {
		if(value==null)
			value="nil";
		Rete r = this.getRuler();
		LinkedList<dynagent.ruleengine.src.ruler.Fact> v = new LinkedList<dynagent.ruleengine.src.ruler.Fact>();
		int numafectados=0;
		try {
			String command = "(defquery search "+fact.toQueryString()+"))";
			r.executeCommand(command);
			Iterator e = r.runQuery("search", new ValueVector());
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				jess.Fact f = t.fact(1);
				System.err.println("  -------- modify( fact="+f+",slot="+slot+"value="+value);
				Value val = this.obtainValueType(slot,value);				
				jess.Fact newf = r.modify(f, slot, val);
				numafectados++;
				System.err.println("  -------- modify, despues="+f);
				
			}
			
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		return numafectados;
	}
	
	
	
	public int modify (dynagent.ruleengine.src.ruler.FactInstance fact, String slot, String value) {
		
		Rete r = this.getRuler();
		int numafectados=0;
		try {
			String command = "(defquery search "+fact.toQueryString()+"))";
			
			r.executeCommand(command);
				Iterator e = r.runQuery("search", new ValueVector());
				while (e.hasNext())
			{
				Token t = (Token) e.next();
				jess.Fact f = t.fact(1);
				Value val = this.obtainValueType(slot,value);				
				jess.Fact newf = r.modify(f, slot, val);
				numafectados++;
			}
		
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		return numafectados;
	}
	
	
	/***
	 * getAllInstanceFacts:  Devuelve un iterador con los facts instance que satisfacen
	 * la condición que se le pasa como parámetro
	 * @param cond: condición.
	 * @return Iterator
	 */
	public LinkedList<dynagent.ruleengine.src.ruler.IPropertyDef> getAllInstanceFacts(String cond) {
		Rete r = this.getRuler();
		
		LinkedList<dynagent.ruleengine.src.ruler.IPropertyDef> v= new LinkedList<dynagent.ruleengine.src.ruler.IPropertyDef>();
		try {
			String command = "(defquery search "+cond;	
			r.executeCommand(command);
			Iterator e= r.runQuery("search", new ValueVector());
			v = new LinkedList<dynagent.ruleengine.src.ruler.IPropertyDef>();
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				jess.Fact f = t.fact(1);
				Value vjess= f.getSlotValue("OBJECT");
				dynagent.ruleengine.src.ruler.Fact fact=(dynagent.ruleengine.src.ruler.Fact)vjess.externalAddressValue(r.getGlobalContext());
				v.add(fact);
			}
		
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		return v;
		
	}
	
	
	/***
	 * getAllInstancesIterator
	 * @param id_O
	 * @param idoRel
	 * @return
	 */
	public Iterator<dynagent.ruleengine.src.ruler.Fact> getAllInstanceFactsIterator(String cond) {
		Iterator it=null;
		try
		{
			it =this.getAllInstanceFacts(cond).iterator();
			
		}catch (Exception e1) {
			e1.printStackTrace();
		}
		return it;
	
	}
	
	
	/***
	 * getAllHierarchyIterator Devuelve un iterador con los facts de herencia que satisfacen
	 * la condición que se le pasa como parámetro
	 * @param cond: condición.
	 * @return Iterator
	 */
	private Iterator getAllHierarchyFactsIterator(String cond) {
		
		Iterator it = null;
		Rete r = this.getRuler();		
		try {
			String command = "(defquery search "+cond;
			r.executeCommand(command);
			//r.reset();
			//r.store("RESULT", r.runQuery("search", new ValueVector()));
			Iterator e= r.runQuery("search", new ValueVector());
			LinkedList<dynagent.ruleengine.src.ruler.FactHierarchy> v = new LinkedList<dynagent.ruleengine.src.ruler.FactHierarchy>();
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				jess.Fact f = t.fact(1);
				Value vjess= f.getSlotValue("OBJECT");
				dynagent.ruleengine.src.ruler.FactHierarchy fact=(dynagent.ruleengine.src.ruler.FactHierarchy)vjess.externalAddressValue(r.getGlobalContext());
				v.add(fact);
			}
				it = v.iterator();
		
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		return it;
	}
	
	

	/***
	 * getAllAccessFactsIterator Devuelve un iterador con los facts de acceso que satisfacen
	 * la condición que se le pasa como parámetro
	 * @param cond: condición.
	 * @return Iterator
	 */
	public Iterator getAllAccessFactsIterator(String cond) {
		
		Iterator it = null;
		Rete r = this.getRuler();		
		try {
			String command = "(defquery search "+cond;
			r.executeCommand(command);
			//r.reset();
			//r.store("RESULT", r.runQuery("search", new ValueVector()));
			/* mal rendimiento: r.executeCommand("(store RESULT (run-query search))"); */
			Iterator e= r.runQuery("search", new ValueVector());
			//Iterator e = (Iterator) r.fetch("RESULT").externalAddressValue(null);
			LinkedList<dynagent.ruleengine.src.ruler.FactAccess> v = new LinkedList<dynagent.ruleengine.src.ruler.FactAccess>();
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				jess.Fact f = t.fact(1);
				Value vjess= f.getSlotValue("OBJECT");
				FactAccess fact=(FactAccess)vjess.externalAddressValue(r.getGlobalContext());
				
				v.add(fact);
			}
				it = v.iterator();
		
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		return it;
	}

	

	/***
	 * getAllPropertyFactsIterator Devuelve un iterador con los facts de propiedades  que satisfacen
	 * la condición que se le pasa como parámetro
	 * @param cond: condición.
	 * @return Iterator
	 */
	private Iterator getAllPropertyFactsIterator(String cond) {
		
		
		Iterator it = null;
		Rete r = this.getRuler();
		LinkedList<dynagent.ruleengine.src.ruler.FactProp> v= new LinkedList<dynagent.ruleengine.src.ruler.FactProp>();
		try {
			String command = "(defquery search "+cond;
			r.executeCommand(command);
			//r.reset();
			//r.store("RESULT", r.runQuery("search", new ValueVector()));
			Iterator e= r.runQuery("search", new ValueVector());
			v = new LinkedList<dynagent.ruleengine.src.ruler.FactProp>();
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				jess.Fact f = t.fact(1);
				Value vjess= f.getSlotValue("OBJECT");
				dynagent.ruleengine.src.ruler.FactProp factprop=(dynagent.ruleengine.src.ruler.FactProp)vjess.externalAddressValue(r.getGlobalContext());
				v.add(factprop);
			}
			it = v.iterator();
		
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		return it;
	}
	
	
	
	
	
	
	
	public void printMotor() {
		try {
			this.getRuler().executeCommand("(facts)");
		} catch (JessException e) {
			e.printStackTrace();
		}
	}

	public void inicializeRules(String  rulesString) throws EngineException {
		Rete r = this.ruler;
		if ( rulesString!=null) {
			try {
				Funcall fun = new Funcall("bind", r);
				fun.add(new Variable("engine", RU.VARIABLE));
		        fun.add(new Value(this));
		        fun.execute(r.getGlobalContext());
		        r.executeCommand("reset");
		        r.executeCommand("facts");
		        r.executeCommand(rulesString);
		        //new JessRules(kb, r, rulesString);
			} catch (JessException e) {
				e.printStackTrace();
				EngineException ex=new EngineException("Excepcion al inicializar las reglas");
				e.setStackTrace(ex.getStackTrace());
				throw ex;
			}
		}
//	}
		
	}

	
	
	
	
	public String buildPropertyAccessConsultCondition(int idto, Integer ido,
			int idProp, Integer userRol, String user, Integer usertask) {
		String condicion = "(access (IDTO " + idto + " |nil)(PROP " + idProp
				+ "|nil)";
		if (ido != null)
			condicion += "(IDO " + ido + " |nil)";

		else {
			condicion += "(IDO nil)";
		}
		if (userRol != null)
			condicion += "(USERROL " + userRol + " |nil)";

		else {
			condicion += "(USERROL nil)";
		}
		if (user != null)
			condicion += "(USER " + user + " |nil)";

		else {
			condicion += "(USER nil)";
		}
		if (usertask != null)
			condicion += "(TASK " + usertask + " |nil)";

		else {
			condicion += "(TASK nil)";
		}

		condicion += "))";
		return condicion;

	}

	public Iterator<FactAccess> getAccessFactsOfProperty(int idto, Integer ido, int idProp, Integer userRol, String user, Integer usertask) {
		String condicion = this.buildPropertyAccessConsultCondition(idto,ido, idProp, userRol, user, usertask);
		Iterator<FactAccess> itFactAccess = this.getAllAccessFactsIterator(condicion);
		return itFactAccess;
	}

	public Iterator<FactAccess> getAccessFactsOverObject(Integer idto, Integer ido, Integer userRol, String user, Integer usertask) {
		String condicion = this.buildAccessOverObjectConsultCondition(idto,ido,userRol, user, usertask);
		// OBTENEMOS TODOS LOS FACTS ACCESS SOBRE ESE OBJETO (CLASE,..)
		// itFactAccess=this.getAllAccessFactsIterator(condicion);
		Iterator<FactAccess> itFactAccess = this.getAllAccessFactsIterator(condicion);
		return itFactAccess;
	}
	
		
		public String buildAccessOverObjectConsultCondition(Integer idto,Integer ido,Integer userRol, String user, Integer usertask) {
			String condicion = null;
						condicion = "(access (IDTO " + idto + " |nil)";
			if (ido != null)
				condicion += "(IDO " + ido + " |nil)";

			else {
				condicion += "(IDO nil)";
			}
			if (userRol != null)
				condicion += "(USERROL " + userRol + " |nil)";

			else {
				condicion += "(USERROL nil)";
			}
			if (user != null)
				condicion += "(USER " + user + " |nil)";

			else {
				condicion += "(USER nil)";
			}
			if (usertask != null)
				condicion += "(TASK " + usertask + " |nil)";

			else {
				condicion += "(TASK nil)";
			}
			condicion += "))";
			return condicion;
		}

		public void run() {
			try {
				 this.getRuler().run();
		} catch (JessException e) {
				e.printStackTrace();
		}
		}
		

		@Override
		public LinkedList<IPropertyDef> getAllInstanceFactsDELETED(Integer ido,Integer idProp){
			System.out.println("=====================\n==============================WARNING!!!: METODO GETALLINSTACEFACTSDELETEDOPTIMIZED NO IMPLEMENTADO EN JESSENGINE. ");
			System.err.println("=====================\n==============================WARNING!!!: METODO GETALLINSTACEFACTSDELETEDOPTIMIZED NO IMPLEMENTADO EN JESSENGINE. ");
			return null;
		}

	

		public int deleteFact(IPropertyDef fact) {
			// TODO Auto-generated method stub
			return 0;
		}

		public void retractFact(IPropertyDef f) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected LinkedList<IPropertyDef> getAllInstanceFacts(int idto, Integer ido, int prop, String value, Integer valuecls, Double qMin, Double qMax, String op) {
			// TODO Auto-generated method stub
			return null;
		}

		public LinkedList<IPropertyDef> getAllInstanceValuesFacts(int ido, int idProp) {
			// TODO Auto-generated method stub
			return null;
		}

		public LinkedList<IPropertyDef> getAllInstanceFacts(int ido) {
			// TODO Auto-generated method stub
			return null;
		}

		public LinkedList<IPropertyDef> getInstanceFactsWhereIdo(int ido) {
			// TODO Auto-generated method stub
			return null;
		}

		public LinkedList<IPropertyDef> getAllInstanceFactsWithIdto(int idto) {
			// TODO Auto-generated method stub
			return null;
		}

		public LinkedList<FactProp> getPropertyFactsWhereIdProp(int idProp) {
			// TODO Auto-generated method stub
			return null;
		}

		public LinkedList<IPropertyDef> getInstanceFactsWhere(Integer idto, Integer ido, Integer prop, String value, Integer valuecls, Double qmin, Double qmax, String op) {
			// TODO Auto-generated method stub
			return null;
		}

		public LinkedList<IPropertyDef> getInstanceFactsWhereIdoAndIdProp(int idto, int idProp) {
			// TODO Auto-generated method stub
			return null;
		}

		public LinkedList<IPropertyDef> getInstanceFactsWhereIdtoAndIdPropAndIdoNull(int idto, int idProp) {
			// TODO Auto-generated method stub
			return null;
		}

	

		public LinkedList<IPropertyDef> getAllInstanceFactsNoDeletedWithIdo(int idto) {
			// TODO Auto-generated method stub
			return null;
		}

		public int getMotorSize() {
			// TODO Auto-generated method stub
			return 0;
		}

		public LinkedList<IPropertyDef> getInstanceFactsWhere(int id) {
			// TODO Auto-generated method stub
			return null;
		}

		public void retractFactAccess(FactAccess f) {
			// TODO Auto-generated method stub
			
		}

		public void run(String group) {
			// TODO Auto-generated method stub
			
		}
	

		
	
	
	
	
	
	
	/***
	 * getAllInstanceFacts:  Devuelve un iterador con los facts instance que satisfacen
	 * la condición que se le pasa como parámetro
	 * @param cond: condición.
	 * @return Iterator
	 */
	/*public LinkedList<dynagent.ruleengine.src.ruler.IPropertyDef> getAllInstanceFacts(String cond) {
		Rete r = this.getRuler().getR().getR();
		
		LinkedList<dynagent.ruleengine.src.ruler.IPropertyDef> v= new LinkedList<dynagent.ruleengine.src.ruler.IPropertyDef>();
		try {
			String command = "(defquery search "+cond;	
			r.executeCommand(command);
			//r.reset();
			//r.store("RESULT", r.runQuery("search", new ValueVector()));
			Iterator e= r.runQuery("search", new ValueVector());
			v = new LinkedList<dynagent.ruleengine.src.ruler.IPropertyDef>();
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				Fact f = t.fact(1);
				Value vjess= f.getSlotValue("OBJECT");
				dynagent.ruleengine.src.ruler.Fact fact=(dynagent.ruleengine.src.ruler.Fact)vjess.externalAddressValue(r.getGlobalContext());
				v.add(fact);
			}
		
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		return v;
		
	}*/
	
	
	/***
	 * getAllInstancesIterator
	 * @param id_O
	 * @param idoRel
	 * @return
	 */
	
	
	/*public Iterator<dynagent.ruleengine.src.ruler.Fact> getAllInstanceFactsIterator(String cond) {
		Iterator it=null;
		try
		{
			it =this.getAllInstanceFacts(cond).iterator();
			
		}catch (Exception e1) {
			e1.printStackTrace();
		}
		return it;
	
	}
	*/
	
	
	/***
	 * getAllHierarchyIterator Devuelve un iterador con los facts de herencia que satisfacen
	 * la condición que se le pasa como parámetro
	 * @param cond: condición.
	 * @return Iterator
	 */
	/*private Iterator getAllHierarchyFactsIterator(String cond) {
		
		Iterator it = null;
		Rete r = this.getRuler().getR().getR();		
		try {
			String command = "(defquery search "+cond;
			r.executeCommand(command);
			//r.reset();
			//r.store("RESULT", r.runQuery("search", new ValueVector()));
			Iterator e= r.runQuery("search", new ValueVector());
			LinkedList<dynagent.ruleengine.src.ruler.FactHierarchy> v = new LinkedList<dynagent.ruleengine.src.ruler.FactHierarchy>();
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				Fact f = t.fact(1);
				Value vjess= f.getSlotValue("OBJECT");
				dynagent.ruleengine.src.ruler.FactHierarchy fact=(dynagent.ruleengine.src.ruler.FactHierarchy)vjess.externalAddressValue(r.getGlobalContext());
				v.add(fact);
			}
				it = v.iterator();
		
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		return it;
	}
	*/
	
	

	/***
	 * getAllAccessFactsIterator Devuelve un iterador con los facts de acceso que satisfacen
	 * la condición que se le pasa como parámetro
	 * @param cond: condición.
	 * @return Iterator
	 */
	/*public Iterator getAllAccessFactsIterator(String cond) {
		
		Iterator it = null;
		Rete r = this.getRuler().getR().getR();		
		try {
			String command = "(defquery search "+cond;
			r.executeCommand(command);
			//r.reset();
			//r.store("RESULT", r.runQuery("search", new ValueVector()));
			// mal rendimiento: r.executeCommand("(store RESULT (run-query search))"); 
			Iterator e= r.runQuery("search", new ValueVector());
			//Iterator e = (Iterator) r.fetch("RESULT").externalAddressValue(null);
			LinkedList<dynagent.ruleengine.src.ruler.FactAccess> v = new LinkedList<dynagent.ruleengine.src.ruler.FactAccess>();
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				Fact f = t.fact(1);
				Value vjess= f.getSlotValue("OBJECT");
				FactAccess fact=(FactAccess)vjess.externalAddressValue(r.getGlobalContext());
				//FactAccess fact = this.toDynagentAccessFact(f);
				
				v.add(fact);
			}
				it = v.iterator();
		
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		return it;
	}*/

	

	/***
	 * getAllPropertyFactsIterator Devuelve un iterador con los facts de propiedades  que satisfacen
	 * la condición que se le pasa como parámetro
	 * @param cond: condición.
	 * @return Iterator
	 */
	/*private Iterator getAllPropertyFactsIterator(String cond) {
		
		
		Iterator it = null;
		Rete r = this.getRuler().getR().getR();
		LinkedList<dynagent.ruleengine.src.ruler.FactProp> v= new LinkedList<dynagent.ruleengine.src.ruler.FactProp>();
		try {
			String command = "(defquery search "+cond;
			r.executeCommand(command);
			//r.reset();
			//r.store("RESULT", r.runQuery("search", new ValueVector()));
			Iterator e= r.runQuery("search", new ValueVector());
			v = new LinkedList<dynagent.ruleengine.src.ruler.FactProp>();
			while (e.hasNext())
			{
				Token t = (Token) e.next();
				Fact f = t.fact(1);
				Value vjess= f.getSlotValue("OBJECT");
				dynagent.ruleengine.src.ruler.FactProp factprop=(dynagent.ruleengine.src.ruler.FactProp)vjess.externalAddressValue(r.getGlobalContext());
				v.add(factprop);
			}
			it = v.iterator();
		
		} catch (JessException e1) {
			e1.printStackTrace();
		}
		return it;
	}
	*/
	
	
	 
	 /**
		 * Obtiene un objeto fact con los que representamos las facts (instances..) a partir del 
		 * fact de jess
		 * @param f: fact de jess
		 * @return: dynagent.ruleengine.src.ruler.Fact: 
		 * @throws NotFoundException 
		 * @ observación: Para obtener el mapeo slots de jess-indice: en depuración sobre la variable fact de jess
		 * abrir m_deft y en su campo m_indexes se encuentra el hashMap con este mapeo
		 */
		/*private dynagent.ruleengine.src.ruler.Fact toDynagentFact(Fact f) {
			dynagent.ruleengine.src.ruler.Fact fact = new dynagent.ruleengine.src.ruler.Fact();
			try {
			
				if(f.getSlotValue("comment")!=null&&!f.getSlotValue("comment").toString().equals("nil")){
					Value v = f.getSlotValue("comment");
					Rete r = this.getRuler().getR().getR();
//					TODO: hablar con Alfonso
					fact.setComment(((dynagent.ruleengine.src.ruler.Fact)v.externalAddressValue(r.getGlobalContext())).getComment());
				}
				
				if(!f.getSlotValue("IDTO").toString().equals("nil"))
					fact.setIDTO(new Integer(f.getSlotValue("IDTO").toString()));
				if(!f.getSlotValue("ROL").toString().equals("nil"))
					fact.setROL(new Integer(f.getSlotValue("ROL").toString()));
				if(!f.getSlotValue("IDO").toString().equals("nil"))
					fact.setIDO(new Integer(f.getSlotValue("IDO").toString()));
				if(!f.getSlotValue("PROP").toString().equals("nil"))
					fact.setPROP(new Integer(f.getSlotValue("PROP").toString()));
				if(!f.getSlotValue("VALUE").toString().equals("nil")&&!this.prepareName(f.getSlotValue("VALUE").toString()).equals("null"))
					fact.setVALUE(prepareName(String.valueOf(f.getSlotValue("VALUE"))));
				if(!f.getSlotValue("VALUECLS").toString().equals("nil"))
					fact.setVALUECLS(new Integer(prepareName(String.valueOf(f.getSlotValue("VALUECLS")))).intValue());
				if(!f.getSlotValue("ROLB").toString().equals("nil"))
					fact.setROLB(new Integer(f.getSlotValue("ROLB").toString()));
				if(!f.getSlotValue("CLSREL").toString().equals("nil"))
					fact.setCLSREL(new Integer(f.getSlotValue("CLSREL").toString()));
				if(!f.getSlotValue("IDOREL").toString().equals("nil"))
					fact.setIDOREL(new Integer(f.getSlotValue("IDOREL").toString()));
				if(!f.getSlotValue("QMIN").toString().equals("nil"))
					fact.setQMIN(new Float(f.getSlotValue("QMIN").toString()));
				if(!f.getSlotValue("QMAX").toString().equals("nil"))
					fact.setQMAX(new Float(f.getSlotValue("QMAX").toString()));
				if(!f.getSlotValue("OP").toString().equals("nil"))
					fact.setOP(f.getSlotValue("OP").toString());
				if(!f.getSlotValue("NAME").toString().equals("nil"))
					fact.setNAME(f.getSlotValue("NAME").toString());
				
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (JessException e) {
				e.printStackTrace();
			}
			return fact;
		}*/
		
		
		
		
		
		
		/**
		 * Obtiene un objeto fact access con los que representamos las facts (access..) a partir del 
		 * fact de jess
		 * @param f: fact de jess
		 * @return: dynagent.ruleengine.src.ruler.Fact: 
		 * @ observación: Para obtener el mapeo slots de jess-indice: en depuración sobre la variable fact de jess
		 * abrir m_deft y en su campo m_indexes se encuentra el hashMap con este mapeo
		 */
		/*private dynagent.ruleengine.src.ruler.FactAccess toDynagentAccessFact(Fact f){
			dynagent.ruleengine.src.ruler.FactAccess fact = new dynagent.ruleengine.src.ruler.FactAccess();
			try {
				fact.setFactId(f.getFactId());
							
				if(!f.getSlotValue("IDTO").toString().equals("nil"))
					fact.setIDTO(new Integer(f.getSlotValue("IDTO").toString()));
				if(!f.getSlotValue("ROL").toString().equals("nil"))
					fact.setROL(new Integer(f.getSlotValue("ROL").toString()));
				if(!f.getSlotValue("IDO").toString().equals("nil"))
					fact.setIDO(new Integer(f.getSlotValue("IDO").toString()));
				if(!f.getSlotValue("PROP").toString().equals("nil"))
					fact.setPROP(new Integer(f.getSlotValue("PROP").toString()));
				if(!f.getSlotValue("VALUE").toString().equals("nil")&&!this.prepareName(f.getSlotValue("VALUE").toString()).equals("null"))
					fact.setVALUE(prepareName(String.valueOf(f.getSlotValue("VALUE"))));
				if(!f.getSlotValue("VALUECLS").toString().equals("nil"))
					fact.setVALUECLS(new Integer(prepareName(String.valueOf(f.getSlotValue("VALUECLS")))).intValue());
				if(!f.getSlotValue("CLSREL").toString().equals("nil"))
					fact.setCLSREL(new Integer(f.getSlotValue("CLSREL").toString()));
				if(!f.getSlotValue("IDOREL").toString().equals("nil"))
					fact.setIDOREL(new Integer(f.getSlotValue("IDOREL").toString()));
				if(!f.getSlotValue("ROLB").toString().equals("nil"))
					fact.setROLB(new Integer(f.getSlotValue("ROLB").toString()));
				if(!f.getSlotValue("USERROL").toString().equals("nil"))
					fact.setUSERROL(new Integer(f.getSlotValue("USERROL").toString()));
				if(!f.getSlotValue("DENNIED").toString().equals("nil"))
					fact.setDENNIED(new Integer(f.getSlotValue("DENNIED").toString()));
				if(!f.getSlotValue("USER").toString().equals("nil"))
					fact.setUSER(f.getSlotValue("USER").toString());
				if(!f.getSlotValue("ACCESSTYPE").toString().equals("nil"))
					fact.setACCESSTYPE(new Integer(f.getSlotValue("ACCESSTYPE").toString()));
				if(!f.getSlotValue("TASK").toString().equals("nil"))
					fact.setTASK(new Integer(f.getSlotValue("TASK").toString()));
							
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (JessException e) {
				e.printStackTrace();
			}
			return fact;
		}*/
		
		
		
		
		
		/**
		 * Obtiene un objeto fact con los que representamos las properties (properties..) a partir del 
		 * fact de jess
		 * @param f: fact de jess
		 * @return: dynagent.ruleengine.src.ruler.Fact: 
		 * @ observación: Para obtener el mapeo slots de jess-indice: en depuración sobre la variable fact de jess
		 * abrir m_deft y en su campo m_indexes se encuentra el hashMap con este mapeo
		 */
		/*private dynagent.ruleengine.src.ruler.FactProp toDynagentFactProp(Fact f){
			dynagent.ruleengine.src.ruler.FactProp fact = new dynagent.ruleengine.src.ruler.FactProp();
			try {
				if(f.get(5)!=null&&!f.get(5).equals("nil"))
					fact.setQMAX(new Float(f.get(5).toString()).floatValue());
				if(f.get(6)!=null&&!f.get(6).equals("nil"))
					fact.setQMIN(new Float(f.get(6).toString()).floatValue());
				if(f.get(2)!=null&&!f.get(2).equals("nil")&&!f.get(2).equals("null"))
					fact.setNAME(prepareName(String.valueOf(f.get(2).toString())));
				if(f.get(1)!=null&&!f.get(1).equals("nil")&&!f.get(1).equals("null"))
					fact.setMASK(f.get(1).toString());
				if(f.get(7)!=null&&!f.get(7).equals("nil")&&!f.get(7).equals("null"))
					fact.setVALUE(prepareName(String.valueOf(f.get(7))));
				if(f.get(4)!=null&&!f.get(4).equals("nil"))
					fact.setPROP(new Integer(f.get(4).toString()).intValue());
				if(f.get(0)!=null&&!f.get(0).equals("nil")&&!f.get(0).equals("null"))
					fact.setCAT(new Integer(f.get(0).toString()));
				
				if(f.get(3)!=null&&!f.get(3).equals("nil")&&!f.get(3).equals("null"))
					fact.setOP(prepareName(String.valueOf(f.get(3))));
				if(f.get(8)!=null&&!f.get(8).equals("nil")&&!f.get(8).equals("null"))
					fact.setVALUECLS(new Integer(f.get(8).toString()).intValue());
					
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (JessException e) {
				e.printStackTrace();
			}
			return fact;
		}*/
		
		
		/**
		 * Obtiene un objeto fact con los que representamos los facts de herencia (hierarchy..) a partir del 
		 * fact de jess
		 * @param f: fact de jess
		 * @return: dynagent.ruleengine.src.ruler.Fact: 
		 * @ observación: Para obtener el mapeo slots de jess-indice: en depuración sobre la variable fact de jess
		 * abrir m_deft y en su campo m_indexes se encuentra el hashMap con este mapeo
		 */
		/*private FactHierarchy toDynagentFactHierarchy(Fact f){
			dynagent.ruleengine.src.ruler.FactHierarchy fact = new dynagent.ruleengine.src.ruler.FactHierarchy();
			try {
				fact.setIDTO(new Integer(f.get(0).toString()).intValue());
				fact.setIDTOSUP(new Integer(f.get(1).toString()).intValue());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (JessException e) {
				e.printStackTrace();
			}
			return fact;
		}*/
	
	
	/*	public void addFactsToRuler(LinkedList<dynagent.ruleengine.src.ruler.IPropertyDef> facts)
	{
		//System.out.println("****************************Facts a añadir al Ruler : "+facts.size());
  		if(facts.size()>0){
			ConceptLogger.getLogger().writeln(".......begin  addFactsToRuler......\n Se van a añadir a motor:");
			for(int i=0;i<facts.size();i++){
				ConceptLogger.getLogger().writeln((facts.get(i)).toString());
			}
			try
			{
				Rete r = this.getRuler().getR().getR();
				Iterator it = facts.iterator();
				while(it.hasNext()){
					dynagent.ruleengine.src.ruler.Fact factfinal;
					IPropertyDef tempo = (IPropertyDef)it.next();
					if(tempo instanceof FactInstance) {
						//System.out.println("instancia de FactInstance");
						factfinal = ((FactInstance)tempo).toFact();						
					}else
					{
						//System.out.println("Instancia de Fact");
					  factfinal= (dynagent.ruleengine.src.ruler.Fact)tempo;
					}
					//System.out.println("****************************añadir al Ruler : \n"+factfinal);
					Funcall f = new Funcall("definstance", r);
					f.add(new Value("instance",RU.ATOM));
					f.add(new Value(factfinal));
					f.add(new Value(  Constants.isIDClass(factfinal.getIDO()) ? "static":"dynamic",RU.ATOM));
					f.execute(r.getGlobalContext());
					ConceptLogger.getLogger().writeln(".......end ddFactsToRuler........");
					//para facilitar la depuracion
					this.factsToRuler.add(factfinal);
				}
			
			}catch (JessException e) {
				e.printStackTrace();
				
			}
			
		}
	}*/
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
