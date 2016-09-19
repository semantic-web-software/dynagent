package dynagent.knowledge.instance.old;
/*package dynagent.knowledge.instance;

import dynagent.application.*;

import java.util.ArrayList;
import java.io.Serializable;
import java.util.Iterator;

public class attribute extends defaultSessionable implements Serializable {
    public attribute() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int tapos;
    private int tm;
    private int virtualIDO = 0;
    private int virtualREF = 0;
    private int virtualTO = 0;

    private int fusionTaposSource = 0;
    private int fusionRefSource = 0;
    private ArrayList attListenerList = new ArrayList();

    public void addAttributeListener(attributeListener al) {
        attListenerList.add(al);
    }

    public Object clone() {
        attribute res = new attribute(tapos, tm);
        clone(res);
        res.setFusionSource(fusionTaposSource, fusionRefSource);
        res.setVirtualIDO(virtualIDO);
        res.setVirtualREF(virtualREF);
        res.setVirtualTO(virtualTO);
        return res;
    }

    private attribute(int tapos, int memberType) {
        //currSessionExistence=session;
        setTapos(tapos);
        setTm(memberType);
    }

    public attribute(int tapos, int memberType, Object value, session ses,
                     int operation) {
        //currSessionExistence=session;
        super(ses, operation, value);
        setTapos(tapos);
        setTm(memberType);
    }

    public int getTapos() {
        return tapos;
    }

    public int getMemberType() {
        return tm;
    }

    public void setTapos(int tapos) {
        this.tapos = tapos;
    }

    private void setTm(int tm) {
        this.tm = tm;
    }

    public void setVirtualIDO(int ido) {
        virtualIDO = ido;
    }

    public int getVirtualIDO() {
        return virtualIDO;
    }

    public void setVirtualTO(int to) {
        virtualTO = to;
    }

    public int getVirtualTO() {
        return virtualTO;
    }

    public void setVirtualREF(int ref) {
        virtualREF = ref;
    }

    public int getVirtualREF() {
        return virtualREF;
    }

    public void setFusionSource(int tapos, int ref) {
        fusionTaposSource = tapos;
        fusionRefSource = ref;
    }

    public int getFusionTaposSource() {
        return fusionTaposSource;
    }

    public int getFusionRefSource() {
        return fusionRefSource;
    }

    void notifyChange() {
        if (attListenerList != null) {
            for (int i = 0; i < attListenerList.size(); i++) {
                ((attributeListener) attListenerList.get(i)).attributeChanged(this);
            }
        }
    }

    public void setValue(session ses, Object value) {
        if (value != null && getValue() != null && getValue().equals(value)) {
            return;
        }
        int operation = value == null && getValue() != null ?
                        dynagent.application.action.DEL :
                        dynagent.application.action.SET;
        setValue(ses, value, operation);
    }

    private void setValue(session ses, Object value, int operation) {
        if (value == null ||
            value != null && value.toString().length() == 0 ||
            value instanceof String && ((String) value).equals("#NULLVALUE#")) {
            addAction(ses, dynagent.application.action.DEL, null);
        } else {
            addAction(ses, operation, value);
        }
    }

    public String toString() {
        return "ATTRIBUTE " + tapos + ", VALUE " + getValue() + ",OPER:" +
                instance.buildAtChanged(getOperation());
    }

    public boolean hasGhanged() {
        return super.hasChanged();
    }

    private void jbInit() throws Exception {
    }
}
*/