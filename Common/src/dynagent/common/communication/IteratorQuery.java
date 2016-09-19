package dynagent.common.communication;

import java.util.Iterator;

import org.jdom.CDATA;
import org.jdom.Element;

public class IteratorQuery {

	private Iterator<Element> itRows;
	private Iterator<Element> itColumns;
	
	public IteratorQuery(Element elem) {
		if (elem!=null) {
			itRows = elem.getChildren().iterator();
		}
	}
	/*public boolean hasNextValue() {
		boolean next = false;
		if (itColumns.hasNext()) {
			next = true;
		} else {
			if (itRows.hasNext()) {
				Element elemRow = (Element)itRows.next();
				itColumns = elemRow.getChildren().iterator();
				next = true;
			}
		}
		return next;
	}*/
	
	public boolean hasNextColumn(){
		return itColumns != null && itColumns.hasNext();
	}
	
	public String nextColumnValue() {
		String value = null;
		Element elemColumn = (Element)itColumns.next();
		if(elemColumn.getContent().size()>0 ){
			CDATA cData = (CDATA)elemColumn.getContent().get(0);
			if (cData.getText().length()>0)
				value = cData.getText();
		}
		return value;
	}
	public boolean hasNextRow() {
		boolean hasNext = false;
		if (itRows!=null)
			hasNext = itRows.hasNext();
		return hasNext;
	}
	public void nextRow() {
		Element elemRow = (Element)itRows.next();
		itColumns = elemRow.getChildren().iterator();
	}
}
