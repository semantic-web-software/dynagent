package dynagent.server.reports.clasificator;

/**Clase con las coordenadas de un elemento*/
public class Coordinates {

	private String key;
	private String band;
	private int left;
	private int top;
	private int width;
	private int height;
	
	public Coordinates(String key, String band, int left, int top, int width, int height) {
		this.key = key;
		this.band = band;
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
	}

	public String getKey() {
		return key;
	}
	public String getBand() {
		return band;
	}
	public int getHeight() {
		return height;
	}
	public int getLeft() {
		return left;
	}
	public int getTop() {
		return top;
	}
	public int getWidth() {
		return width;
	}
	
	public void setBand(String band) {
		this.band = band;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public void setLeft(int left) {
		this.left = left;
	}
	public void setTop(int top) {
		this.top = top;
	}
	public void setWidth(int width) {
		this.width = width;
	}

	public String toString() {
		return "key " + key + ", band " + band + 
		", left " + left + ", top " + top + ", width " + width + ", height " + height;
	}
}
