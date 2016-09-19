package gdev.gawt.utils;import dynagent.common.Constants;

/*

import dynagent.common.Constants;
 * itemList.java
 *
 * Created on December 2, 2002, 6:43 AM
 */

/**
 *
 * @author  Administrator
 */
public class ItemList extends Object implements Comparable<ItemList>{
	public String oid1, oid2;
	public boolean initialSelection=false;
	public String label;
	public Integer intId;
	public ItemList(String id1, String id2, String l, boolean iSel) throws NumberFormatException{
		oid1= id1;
		oid2= id2;
		label= l;
		initialSelection= iSel;
		try{
			intId= new Integer( id1 );
		}catch(NumberFormatException pe){
			throw pe; 
		}
	}
	public String toString(){
		return (label==null||label.length()==0) ? " ":label;
	}

	public String getId(){return oid1;}

	public String getId2(){return oid2;}

	public int getIntId() throws NumberFormatException{
		return Integer.parseInt(oid1);
	}

	public Integer getInteger(){
		return intId;
	}

	public boolean isInitialSelected(){
		return initialSelection;
	}
	public boolean equals(Object value){
		if(!(value instanceof ItemList)) return false;
		boolean labelCheck= toString().equals(((ItemList)value).toString());
		if(toString().equals("") &&
			((ItemList)value).toString().equals("")) labelCheck=true;
		return ((getIntId()==((ItemList)value).getIntId()) && labelCheck);
	}
	public Object clone()throws NumberFormatException{
		ItemList res= new ItemList( oid1, oid2, label, initialSelection );
		return (Object)res;
	}
	public int compareTo( ItemList ob ) throws ClassCastException{
		ItemList objB=(ItemList)ob;

		if( equals( objB ) ) return 0;
		//return oid1.compareTo( objB.getId() );
		return Constants.languageCollator.compare(toString(),objB.toString());
	}
}
