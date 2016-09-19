package dynagent.common.utils;

import java.awt.Color;
import java.util.ArrayList;

import org.jdom.Element;


public class RowItem extends Object implements Comparable {
	private int index;

	private int indexOld;

	private int groupSize = 1;

	private boolean groupExpand = false;

	private boolean filtered = false;

	public ArrayList<Object> columnData;

	public ArrayList<Integer> columnIdo;
	
	public ArrayList<Integer> columnOldIdo;
	
	public ArrayList<Integer> columnIdto;

	public ArrayList<Integer> columnOldIdto;
	
	public ArrayList<String> columnIdParent;
	
	public ArrayList<Integer> columnIdoFilter;
	
	public ArrayList<Integer> columnIdtoFilter;

	private Color color = null;

	private ArrayList groupByColumns;
	
	private boolean nullRow;
	
	private int state;
	
	private boolean permanent;
	
	private GIdRow idRow;
	
	//Si se ponen mas estados tener en cuenta que tienen que ser potencias de 2 ya que pueden tenerse varios estados a la vez
	public static final int IDLE_STATE=0;
	public static final int CREATION_STATE=1;
	public static final int FINDER_STATE=2;
	public static final int REMOVE_STATE=4;
	public static final int SUBCREATION_STATE=8;

	public RowItem(int index, ArrayList groupByColumns) {
		this.index = index;
		this.indexOld=index;
		this.groupByColumns = groupByColumns;
		nullRow=false;
		state=IDLE_STATE;
	}
	
	public RowItem(int index, int columns, ArrayList groupByColumns) {
		this.index = index;
		this.indexOld=index;
		columnData = new ArrayList<Object>();
		for (int i = 0; i < columns; i++) {
			columnData.add(null);
		}
		this.groupByColumns = groupByColumns;
		nullRow=false;
		state=IDLE_STATE;
	}

	@Override
	public String toString() {
		return "index:"+index+" ido:"+idRow.getIdo()+" state:"+state+" columnData:"+columnData;
	}
	
	public boolean isGroup() {
		return groupSize > 1;
	}

	public void setColumnData(int col, Object val) {
		columnData.set(col, val);
	}

	@SuppressWarnings("unchecked")
	public void setColumnData(ArrayList data) {
		columnData = data;
	}

	public void setColumnIdo(int col, Integer val) {
		columnIdo.set(col, val);
	}

	public void setColumnIdo(ArrayList<Integer> ido) {
		columnIdo = ido;
	}
	
	public void setColumnIdoFilter(int col, Integer val) {
		columnIdoFilter.set(col, val);
	}

	public void setColumnIdoFilter(ArrayList<Integer> idoFilter) {
		columnIdoFilter = idoFilter;
	}
	
	public void setColumnOldIdo(ArrayList<Integer> oldIdo) {
		columnOldIdo = oldIdo;
	}
	
	public void setColumnIdto(int col, Integer val) {
		columnIdto.set(col, val);
	}

	public void setColumnIdto(ArrayList<Integer> idto) {
		columnIdto = idto;
	}
	
	public void setColumnIdtoFilter(int col, Integer val) {
		columnIdtoFilter.set(col, val);
	}

	public void setColumnIdtoFilter(ArrayList<Integer> idtoFilter) {
		columnIdtoFilter = idtoFilter;
	}
	
	public void setColumnOldIdto(ArrayList<Integer> oldIdto) {
		columnOldIdto = oldIdto;
	}

	public Object getColumnData(int col) {
		return columnData.get(col);
	}

	public int getColumnSize() {
		return columnData.size();
	}

	public Integer getColumnIdo(int col) {
		return columnIdo.get(col);
	}
	
	public Integer getColumnIdto(int col) {
		return columnIdto.get(col);
	}
	
	public Integer getColumnIdoFilter(int col) {
		return columnIdoFilter.get(col);
	}
	
	public Integer getColumnIdtoFilter(int col) {
		return columnIdtoFilter.get(col);
	}	

	@SuppressWarnings("unchecked")
	public int compareTo(Object ob) throws ClassCastException {
		if (!(ob instanceof RowItem))
			throw new ClassCastException(
			"ERROR, EL OBJETO A COMPARAR NO ES UN ITEM ROW");
		RowItem objB = (RowItem) ob;
		for (int i = 0; i < groupByColumns.size(); i++) {
			int col = ((Integer) groupByColumns.get(i)).intValue();
			Object dataA = columnData.get(col), dataB = objB.getColumnData(col);
			if (dataA != null && dataB == null)
				return 1;
			if (dataA == null && dataB != null)
				return -1;
			if (dataA == null && dataB == null)
				continue;
			if (dataA.equals(dataB))
				continue;
			return ((Comparable) dataA).compareTo(dataB);
		}
		return 0;
	}

	public Integer getColumnOldIdo(int col) {
		return columnOldIdo.get(col);
	}

	public void setColumnOldIdo(int col, Integer val){
		this.columnOldIdo.set(col, val);
	}
	
	public Integer getColumnOldIdto(int col) {
		return columnOldIdto.get(col);
	}

	public void setColumnOldIdto(int col, Integer val){
		this.columnOldIdto.set(col, val);
	}

	public void setColumnIdParent(ArrayList<String> columnIdParent) {
		this.columnIdParent = columnIdParent;
	}
	
	public String getColumnIdParent(int col) {
		return columnIdParent.get(col);
	}

	public boolean isNullRow() {
		return nullRow;
	}

	public void setNullRow(boolean nullRow) {
		this.nullRow = nullRow;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public boolean matchesState( int state ){
		return (state & getState()) >0;
	}

	public ArrayList<Object> getColumnData() {
		return columnData;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public boolean isFiltered() {
		return filtered;
	}

	public boolean isGroupExpand() {
		return groupExpand;
	}

	public void setFiltered(boolean filtered) {
		this.filtered = filtered;
	}

	public void setGroupExpand(boolean groupExpand) {
		this.groupExpand = groupExpand;
	}

	public int getGroupSize() {
		return groupSize;
	}

	public void setGroupSize(int groupSize) {
		this.groupSize = groupSize;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndexOld() {
		return indexOld;
	}

	public void setIndexOld(int indexOld) {
		this.indexOld = indexOld;
	}

	public boolean isPermanent() {
		return permanent;
	}

	public void setPermanent(boolean permanent) {
		this.permanent = permanent;
	}

	public GIdRow getIdRow() {
		return idRow;
	}

	public void setIdRow(GIdRow idRow) {
		this.idRow = idRow;
	}
	
	public boolean containsIdo(int ido){
		return columnIdo.contains(ido);
	}
}