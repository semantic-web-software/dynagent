/**
 * FileFacade.java
 * @author Ildefonso Montero Perez
 * @description A file based facade to obtain data
 */

package dynagent.server.database;



public class FileFacade extends Facade{
	/*

	private FileInputStream data;
	private String file;
	
	public FileFacade() {
	
			file = Constants.DATAFILE;
			try {
				data = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
	
	}
	
	public FileInputStream getData() {
		return data;
	}

	public void setData(FileInputStream data) {
		this.data = data;
	}

	public void close() {
		try {
			data.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	public void create(String object, Fact value) {
		try {
			FileWriter fwdata = new FileWriter(file);
			String newline = "\n"+object+"#"+value.toString();
			fwdata.append(newline);
			fwdata.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void delete(String object) {
		try {
			BufferedReader bf = new BufferedReader(new FileReader(file));
			FileWriter fwdata = new FileWriter(file);
			String line = "";
			while((line = bf.readLine()) != null){
					if(!(line.substring(0, line.indexOf("#"))).equals(object))
						fwdata.append(line);
			}
			fwdata.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public Fact get(String object) {
		Fact f = new Fact();
		try {
			BufferedReader bf = new BufferedReader(new FileReader(file));
			String line = "";
			while((line = bf.readLine()) != null){
					if((line.substring(0, line.indexOf("#"))).equals(object.substring(object.indexOf("#")+1,object.length())))
						f.load(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		return f;
	}
	
	public LinkedList<Object> gets(String object){ 
		return getMultiple(object); 
	}
	
	public LinkedList<Object> getMultiple(String object){
		LinkedList<Object> pfacts = new LinkedList<Object>();
		try {
			BufferedReader bf = new BufferedReader(new FileReader(file));
			String line = "";
			while((line = bf.readLine()) != null){
					Fact f = new Fact();
					if((line.substring(0, line.indexOf("#"))).equals(object)){
						f.load(line);
						pfacts.addLast(f);
					}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		return pfacts;
	}

	public void open() {
		
	}

	public void set(String object, Fact value) {
		if(get(object) != null)
			delete(object);
		create(object,value);
	}
	*/

	
}
