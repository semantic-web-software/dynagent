package dynagent.server.services.querys;

public class StructureQuery {

	private StringBuffer select;
	private StringBuffer from;
	private StringBuffer where;
	//private StringBuffer group;
	private StringBuffer order;
	private Integer limit;
	
	public StructureQuery() {
		select = new StringBuffer("");
		from = new StringBuffer("");
		where = new StringBuffer("");
		//group = new StringBuffer("");
		order = new StringBuffer("");
	}

	public StringBuffer getOrder() {
		return order;
	}

	public void setOrder(StringBuffer order) {
		this.order = order;
	}

	public StringBuffer getFrom() {
		return from;
	}

	public void setFrom(StringBuffer from) {
		this.from = from;
	}

	//public StringBuffer getGroup() {
	//return group;
	//}

	//public void setGroup(StringBuffer group) {
	//this.group = group;
	//}

	public StringBuffer getSelect() {
		return select;
	}

	public void setSelect(StringBuffer select) {
		this.select = select;
	}

	public StringBuffer getWhere() {
		return where;
	}

	public void setWhere(StringBuffer where) {
		this.where = where;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public String toString() {
		StringBuffer dev = new StringBuffer("SELECT " + this.select + " \nFROM " + this.from);
		if (this.where.length()>0)
			dev.append(" \nWHERE " + this.where);
		//if (this.group.length()>0)
		//dev.append(" \nGROUP BY " + this.group);
//		if (this.having.length()>0)
//			dev.append(" \nHAVING " + this.having);
		if (this.order.length()>0)
			dev.append(" \nORDER BY " + this.order);
		if (this.limit!=null)
			dev.append(" \nLIMIT " + limit);

		return dev.toString();
	}
}
