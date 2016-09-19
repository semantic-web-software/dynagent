package dynagent.common.knowledge;
import java.util.ArrayList;
import java.util.Iterator;

import org.jdom.Element;

public class selectData extends Object{
	private ArrayList<instance> list = new ArrayList<instance>();
	private ArrayList<SelectQuery> select = null;

	public selectData(){
	}
	/*public selectData(ArrayList<SelectQuery> select){
        this.select = select;
	}*/
	public void setSelect(ArrayList<SelectQuery> select) {
		this.select = select;
	}
	
    public void addInstance(instance ins){
        if( ins==null ){
            Exception e= new Exception();
            e.printStackTrace();
            return;
        }
        list.add(ins);
    }
    
    public boolean contains(instance ins){
    	return list.contains(ins);
    }
    
    public boolean containsInstanceWithIdo(int ido){
    	Iterator<instance> itr=list.iterator();
    	while(itr.hasNext()){
    		if(itr.next().getIDO()==ido)
    			return true;
    	}
    	return false;
    }
    
    public void addAll(selectData sel){
        list.addAll(sel.list);
    }
    
    public void addAll(ArrayList<instance> list){
        this.list.addAll(list);
    }

    public Iterator<instance> getIterator(){
        return list.iterator();
    }
    public ArrayList<SelectQuery> getSelect() {
		return select;
	}
	public instance getFirst(){
        if( !hasData() ) return null;
        else
            return (instance)list.get(0);
    }

    /*public Iterator getIterator(int idCtx){
        ArrayList res= new ArrayList();
        for( int i=0; i<lista.size(); i++ ){
            instance ins=(instance)lista.get(i);
            if( ins.getContextID()==idCtx )
                res.add(ins);
        }
        return res.iterator();
    }*/

    public boolean hasData(){
        return list.size()>0;
    }

    public int size(){
        return list.size();
    }

    public instance get( int ido ){
        for(int i=0;i<list.size();i++){
            instance obj=(instance)list.get(i);
            if(obj.getIDO()==ido)
                return obj;
        }
        return null;
    }

    public void remove( int ido ){
        for(int i=0;i<list.size();i++){
            instance obj=(instance)list.get(i);
            if(obj.getIDO()==ido){
                list.remove(i);
                return;
            }
        }
    }

    public Object clone(){
        selectData res = new selectData();
    	if (select!=null) {
        	ArrayList<SelectQuery> aSQ = new ArrayList<SelectQuery>();
	        for( int i=0; i<select.size(); i++ )
	            aSQ.add(select.get(i).clone());
	        res.setSelect(aSQ);
    	}
        for( int i=0; i<list.size(); i++ )
            res.addInstance( (instance)((instance)list.get(i)).clone());
        return res;
    }

    public Element toElement(){
        Element res = new Element("SELECTION");
        for( int i=0; i<list.size(); i++ ){
            instance ins = (instance)list.get(i);
            Element eIns = ins.toElement();
            res.addContent(eIns);
        }
    	if (select!=null) {
	        Element resSelect = new Element("SELECT");
	        for( int i=0; i<select.size(); i++ ){
	        	SelectQuery sq = (SelectQuery)select.get(i);
	            Element eSq = sq.toElement();
	            resSelect.addContent(eSq);
	        }
	        res.addContent(resSelect);
    	}
        return res;
    }

    public String toString(){
    	String dev = "SELECT_DATA\n";
	    for( int i=0; i<list.size(); i++ ){
	        instance ins = (instance)list.get(i);
	        dev += ins.toString() + "\n";
    	}
    	if (select!=null) {
	    	dev += "SELECT\n";
		    for( int i=0; i<select.size(); i++ ){
		        dev += select.get(i).toString() + "\n";
	    	}
    	}
	    return dev;
    }
}
