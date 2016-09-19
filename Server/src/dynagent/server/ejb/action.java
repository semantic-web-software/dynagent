package dynagent.server.ejb;
import org.jdom.Element;

public class action extends Object{
	public int type;
	public Element detalle;
	public int id;
	public int transition;
        private int operation=0;

	public action( int id, int type, int tran, Element detail, int operation){
		this.id=id;
		this.type=type;
		detalle=detail;
		transition=tran;
                this.operation=operation;
	}

        public Object clone(){
            Element cDet= detalle==null ? null:(Element)detalle.clone();
            return new action(id,type,transition,cDet,operation);
        }
        public int getOperation(){
            return operation;
        }
}
