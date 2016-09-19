package dynagent.ejb.old;
/*package dynagent.ejb;

public class processType extends Object{
	private int id;
	private  String label;
	private int startState;

        public processType( int id, String label, int startState ){
            this.id=id;
            this.label=label;
            this.startState=startState;
        }

        public int getId(){
            return id;
        }

        public String getLabel(){
            return label;
        }

        public int getStartState(){
            return startState;
        }

	public int getStartContext( metaData md ){
		Iterator itr= md.m_trans.keySet().iterator();
		while( itr.hasNext() ){
			Integer id= (Integer)itr.next();
			taskTransition tt= md.getTrans( id );
			if( tt.userFlowStartAction==startAction )
				return tt.userFlowStartContext;
		}
		return 0;
	}
        public Object clone(){
            return new processType(id,label,startState);
        }
}
*/