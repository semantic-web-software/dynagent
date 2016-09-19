package dynagent.common.basicobjects;

public class License {

	private long expiredDate;
	private int users;
	private Integer type;
	
	public License(long expiredDate, int users, Integer type) {
		super();
		this.expiredDate = expiredDate;
		this.users = users;
		this.type = type;
	}
	
	public long getExpiredDate() {
		return expiredDate;
	}
	public void setExpiredDate(long expiredDate) {
		this.expiredDate = expiredDate;
	}
	public int getUsers() {
		return users;
	}
	public void setUsers(int users) {
		this.users = users;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
}
