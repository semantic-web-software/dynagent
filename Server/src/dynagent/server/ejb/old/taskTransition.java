/*package dynagent.ejb;

public class taskTransition extends Object{
	public int id=-1;
	public int stateIni=-1;
	public int stateEnd=-1;
	public int taskType;
	public String label;
	public int userFlowStartAction;
	public int userFlowStartContext;
        public int owningPolicy;
	public taskTransition( int id, int taskType, int stIni, int stEnd, String label, int ufsa, int ufsc, int owningPol ){
		this.id=id;
                owningPolicy=owningPol;
		this.taskType=taskType;
		stateIni=stIni;
		stateEnd=stEnd;
		this.label=label;
		userFlowStartAction=ufsa;
		userFlowStartContext= ufsc;
	}
	public String toString(){
		return label;
	}
        public Object clone(){
            return new taskTransition(id,taskType,stateIni,stateEnd,label,userFlowStartAction,userFlowStartContext,owningPolicy);
        }
}
*/