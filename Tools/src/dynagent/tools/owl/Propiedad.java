package dynagent.tools.owl;

	import java.beans.PropertyChangeListener;
	import java.beans.PropertyChangeSupport;


	import org.drools.FactHandle;

	import dynagent.common.basicobjects.Properties;
import dynagent.common.knowledge.Category;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.ruleengine.src.ruler.IPropertyChangeDrools;

	public class Propiedad implements IPropertyChangeDrools{
		private Integer CAT;
		private String RANGENAME=null;
		private String OP=null;
		private String NAME=null;
		private Float QMIN=null;
		private Float QMAX=null;
		private String INVERSA;
		private FactHandle factHandle;
		private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
		private boolean isDataProperty=false;
		private boolean isObjectProperty=false;		
		
		public Propiedad(String name,String rangename,String op,Float qmin,Float qmax,String inversa,Integer cat){
			this.CAT=cat;
			this.NAME=name;
			this.RANGENAME=rangename;
			this.QMAX=qmax;
			this.QMIN=qmin;
			this.INVERSA=inversa;
			this.OP=op;
			Category category=new Category(cat);
			if(category.isDataProperty())
				this.isDataProperty=true;
			else if(category.isObjectProperty())
				this.isObjectProperty=true;
		}
		
		public String getNAME() {
			return NAME;
		}
		
		public FactHandle getFactHandle()
		{
			return factHandle;
		}

		public void setFactHandle(FactHandle factHandle)
		{
			this.factHandle = factHandle;
		}
		
		public Float getQMAX() {
			return QMAX;
		}


		public Float getQMIN() {
			return QMIN;
		}



		public String getOP() {
			return OP;
		}


	

		public Integer getCAT() {
			return CAT;
		}

		

		public String toString(){
			String stringfact = "";
			
			stringfact += "\n\t (property ";
			stringfact += "( NAME " + this.getNAME() + " )";
			
		
			
			if(this.getQMIN() == null)
				stringfact += "( QMIN nil )";
			else
				stringfact += "( QMIN " + this.getQMIN() + " )";
			
			if(this.getQMAX() == null)
				stringfact += "( QMAX nil )";
			else
				stringfact += "( QMAX " + this.getQMAX() + " )";	
			
			if(this.getOP() == null)
				stringfact += "( OP nil ))";
			else
				stringfact += "( OP " + this.getOP() + " )";
			
			if(this.getCAT() == null)
				stringfact += "( CAT nil ))";
			else
				stringfact += "( CAT " + this.getCAT() + " ))";
			
			if(this.getINVERSA() == null)
				stringfact += "(INVERSA nil ))";
			else
				stringfact += "(INVERSA " + this.getINVERSA() + " ))";
			
			
			
			return stringfact;
			
		}
		
		
		
		public void addPropertyChangeListener(PropertyChangeListener pcl) {
			pcs.addPropertyChangeListener(pcl);
		}

		public void removePropertyChangeListener(PropertyChangeListener pcl) {
			pcs.removePropertyChangeListener(pcl);
		}

		public void removePropertyChangeListeners() {
			PropertyChangeListener[] p=pcs.getPropertyChangeListeners();
			for(int i=0;i<p.length;i++){
				removePropertyChangeListener(p[i]);
			}
		}

		public String getINVERSA() {
			return INVERSA;
		}

		

		public Object clone(IKnowledgeBaseInfo ik) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRANGENAME() {
			return RANGENAME;
		}

		public boolean isDATAPROPERTY() {
			return isDataProperty;
		}

		public boolean isOBJECTPROPERTY() {
			return isObjectProperty;
		}
		
	}

