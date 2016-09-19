package gdev.gbalancer;

import gdev.gbalancer.GViewBalancer.GPosChangeRes;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class GProcessedFieldsSorts {
	
	private int maxWidth=0;
	private int minWidth=9999999;
	private int maxWidthByWidth=0;
	private int minWidthByWidth=9999999;
	private LinkedList<GProcessedField> listFields= new LinkedList<GProcessedField>();		
	private LinkedList<GProcessedField> listFieldsByWidth= new LinkedList<GProcessedField>();
	private HashMap<Integer,HashMap <Integer,Integer>> rowHeight = new HashMap<Integer, HashMap <Integer,Integer>>();
	public GProcessedFieldsSorts(){
		
	}
	public GProcessedFieldsSorts(GProcessedField gfield){
		listFields.add(gfield);
		maxWidth=gfield.getFormField().getMinimumComponentDimension().width;
		minWidth=maxWidth;
		listFieldsByWidth.add(gfield);
		maxWidthByWidth=gfield.getBounds().width;
		minWidthByWidth=maxWidthByWidth;
	}
	
	
	public void addField(GProcessedField gfield){
		int minWidth=gfield.getFormField().getMinimumComponentDimension().width;
		
		if(!listFields.isEmpty()){
			if (minWidth>maxWidth){
				/*if (gfield.getBounds().getHeight()==this.listFields.get(0).getBounds().getHeight()){
					Rectangle rc= gfield.getBounds();
					gfield.setBounds(this.listFields.get(0).getBounds());
					this.listFields.get(0).setBounds(rc);
				}*/
				listFields.add(0, gfield);
				this.maxWidth=minWidth;
			}else if(minWidth<this.minWidth){
				/*if (gfield.getBounds().getHeight()==this.listFields.getLast().getBounds().getHeight()){
					Rectangle rc= gfield.getBounds();
					gfield.setBounds(this.listFields.getLast().getBounds());
					this.listFields.getLast().setBounds(rc);
				}*/
				listFields.add(gfield);
				this.minWidth=minWidth;
			}else{
				int size=this.listFields.size();
				for (int i=1; i<size-1;i++){
					GProcessedField gf=this.listFields.get(i);
					int widhtgf=gf.getFormField().getMinimumComponentDimension().width;
					if(minWidth>widhtgf){
						/*if (gfield.getBounds().getHeight()==gf.getBounds().getHeight()){
							Rectangle rc= gfield.getBounds();
							gfield.setBounds(gf.getBounds());
							gf.setBounds(rc);
						}*/
						this.listFields.add(i, gfield);
						addFieldByWidth(gfield);
						return;
					}
					
				}
				/*if (gfield.getBounds().getHeight()==this.listFields.get(size-1).getBounds().getHeight()){
					Rectangle rc= gfield.getBounds();
					gfield.setBounds(this.listFields.get(size-1).getBounds());
					this.listFields.get(size-1).setBounds(rc);
				}*/
				this.listFields.add(size-1, gfield);
				
			}
				
		}else{
			this.listFields.add(gfield);
			this.maxWidth=gfield.getFormField().getMinimumComponentDimension().width;
			this.minWidth=this.maxWidth;
		}
		
		addFieldByWidth(gfield);
		
	
	}
	private void addFieldByWidth(GProcessedField gfield) {
		int minWidth=gfield.getBounds().width;
		
		if(!listFieldsByWidth.isEmpty()){
			if (minWidth>maxWidthByWidth){
				/*if (gfield.getBounds().getHeight()==this.listFields.get(0).getBounds().getHeight()){
					Rectangle rc= gfield.getBounds();
					gfield.setBounds(this.listFields.get(0).getBounds());
					this.listFields.get(0).setBounds(rc);
				}*/
				listFieldsByWidth.add(0, gfield);
				this.maxWidthByWidth=minWidth;
			}else if(minWidth<this.minWidthByWidth){
				/*if (gfield.getBounds().getHeight()==this.listFields.getLast().getBounds().getHeight()){
					Rectangle rc= gfield.getBounds();
					gfield.setBounds(this.listFields.getLast().getBounds());
					this.listFields.getLast().setBounds(rc);
				}*/
				listFieldsByWidth.add(gfield);
				this.minWidthByWidth=minWidth;
			}else{
				int size=this.listFieldsByWidth.size();
				for (int i=1; i<size-1;i++){
					GProcessedField gf=this.listFieldsByWidth.get(i);
					int widhtgf=gf.getBounds().width;
					if(minWidth>widhtgf){
						/*if (gfield.getBounds().getHeight()==gf.getBounds().getHeight()){
							Rectangle rc= gfield.getBounds();
							gfield.setBounds(gf.getBounds());
							gf.setBounds(rc);
						}*/
						this.listFieldsByWidth.add(i, gfield);
						return;
					}
					
				}
				/*if (gfield.getBounds().getHeight()==this.listFields.get(size-1).getBounds().getHeight()){
					Rectangle rc= gfield.getBounds();
					gfield.setBounds(this.listFields.get(size-1).getBounds());
					this.listFields.get(size-1).setBounds(rc);
				}*/
				this.listFieldsByWidth.add(size-1, gfield);
				
			}
				
		}else{
			this.listFieldsByWidth.add(gfield);
			this.maxWidthByWidth=gfield.getBounds().width;
			this.minWidthByWidth=this.maxWidthByWidth;
		}
	}
	public LinkedList<GProcessedField> getListFields() {
		return listFields;
	}
	public void setListFields(LinkedList<GProcessedField> listFields) {
		this.listFields = listFields;
	}
	public int getMaxWidth() {
		return maxWidth;
	}
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}
	public int getMinWidth() {
		return minWidth;
	}
	public void setMinWidth(int minWidth) {
		this.minWidth = minWidth;
	}
	public void toStingListFields(){
		String result="";
		Iterator<GProcessedField> it= listFields.iterator();
		while(it.hasNext()){
			GProcessedField gf= it.next();
			result+= "<CAMPO="+gf.getFormField().getLabel()+" ANCHO="+gf.getFormField().getMinimumComponentDimension().width+">";
		}
		System.out.println(result);
	}
	
	public void toStingListFieldsWidth(){
		String result="";
		Iterator<GProcessedField> it= listFieldsByWidth.iterator();
		while(it.hasNext()){
			GProcessedField gf= it.next();
			result+= "<CAMPO="+gf.getFormField().getLabel()+" ANCHO="+gf.getBounds().getWidth()+">";
		}
		System.out.println(result);
	}
	public void process() {
		int size=this.listFields.size();
		calculateHeightRows();
		for (int j=0; j<size; j++){
			GProcessedField gf= this.listFields.get(j);
			for (int i=0; i<size;i++){
				
				GProcessedField gf2=this.listFieldsByWidth.get(i);
				if (gf.getFormField().getLabel()!=null && gf2.getFormField().getLabel()!=null && gf.getFormField().getLabel().equals(gf2.getFormField().getLabel()))
					break;
				/*System.out.println("<GF  CAMPO="+gf.getFormField().getLabel()+" ANCHO="+gf.getBounds().getWidth()+" MINWIDHT="+gf.getFormField().getMinimumComponentDimension().width+"" +
						" ROW="+gf.m_iRow+" COLUMN="+gf.m_iColumn+" TAMROW="+rowHeight.get(gf.m_iColumn).get(gf.m_iRow)+">");
				System.out.println("<GF2 CAMPO="+gf2.getFormField().getLabel()+" ANCHO="+gf2.getBounds().getWidth()+" MINWIDHT="+gf2.getFormField().getMinimumComponentDimension().width+"" +
						" ROW="+gf2.m_iRow+" COLUMN="+gf2.m_iColumn+" TAMROW="+rowHeight.get(gf2.m_iColumn).get(gf2.m_iRow)+">");*/
				if (gf.getBounds().height==gf2.getBounds().height && !gf.equals(gf2)){
					processAux(gf,gf2,true);
				}else if(!gf.equals(gf2) && gf.m_iColumn==gf2.m_iColumn){
					HashMap<Integer, Integer> hm= rowHeight.get(gf.m_iColumn);
					if (hm.get(gf.m_iRow).equals(hm.get(gf2.m_iRow))){
						processAux(gf,gf2,false);
						return;
					}
				}
			}
		}
	}
	private void calculateHeightRows() {
		Iterator<GProcessedField> it= this.listFields.iterator();
		while(it.hasNext()){
			GProcessedField gf= it.next();
			int row= gf.m_iRow;
			int column=gf.m_iColumn;
			if (rowHeight.containsKey(column)){
				HashMap<Integer, Integer> hm= rowHeight.get(column);
				if (hm.containsKey(row)){
					if (gf.getBounds().height>hm.get(row)){
						hm.put(row, gf.getBounds().height);
					}
				}else{
					hm.put(row,gf.getBounds().height);
					
				}
			}else{
				HashMap<Integer, Integer> hm= new HashMap<Integer, Integer>();
				hm.put(row, gf.getBounds().height);
				rowHeight.put(column, hm);
			}
		}
	}
	private void processAux(GProcessedField gf, GProcessedField gf2,boolean equalHeight) {
		if (gf.getFormField().getMinimumComponentDimension().width>gf2.getFormField().getMinimumComponentDimension().width  && gf.getFormField().getMinimumComponentDimension().width<=gf2.getBounds().width && gf.getFormField().getOrder()==gf2.getFormField().getOrder() && 
				gf.getFormField().getPriority()== gf2.getFormField().getPriority() && gf.getBounds().width<gf2.getBounds().width){
			Rectangle boundsGF=(Rectangle) gf.getBounds().clone();
			Rectangle boundsGFC=(Rectangle) gf.getComponentBounds().clone();
			Rectangle boundsGF2=(Rectangle) gf2.getBounds().clone();
			Rectangle boundsGFC2=(Rectangle) gf2.getComponentBounds().clone();
			if (equalHeight){
				gf.setBounds(gf2.getBounds());
				gf.setComponentBounds(gf2.getComponentBounds());
			}else{
				gf.getBounds().x=boundsGF2.x;
				gf.getBounds().y=boundsGF2.y;
				gf.getBounds().width=boundsGF2.width;
				gf.getComponentBounds().x=boundsGFC2.x;
				gf.getComponentBounds().y=boundsGFC2.y;
				gf.getComponentBounds().width=boundsGFC2.width;
			}
			//gf.setComponentSecundarioBounds(gf2.getComponentSecundarioBounds());
			Rectangle compSecBounds=gf.getComponentSecundarioBounds();
			int compSecPoint= gf.getComponentBounds().width-compSecBounds.width;
			compSecBounds.x=compSecPoint;
			gf.setComponentSecundarioBounds(compSecBounds);
			
			if (equalHeight){
				gf2.setBounds(boundsGF);
				gf2.setComponentBounds(boundsGFC);
			}else{
				gf2.getBounds().x=boundsGF.x;
				gf2.getBounds().y=boundsGF.y;
				gf2.getBounds().width=boundsGF.width;
				gf2.getComponentBounds().x=boundsGFC.x;
				gf2.getComponentBounds().y=boundsGFC.y;
				gf2.getComponentBounds().width=boundsGFC.width;
			}
			int gfcol=gf.m_iColumn;
			gf.setColumn(gf2.getColumn());
			gf2.setColumn(gfcol);
			
			int gfrow=gf.m_iRow;
			gf.setRow(gf2.getRow());
			gf2.setRow(gfrow);
			Rectangle compSecBounds2=gf2.getComponentSecundarioBounds();
			int compSecPoint2= gf2.getComponentBounds().width-compSecBounds2.width;
			compSecBounds2.x=compSecPoint2;
			gf2.setComponentSecundarioBounds(compSecBounds2);
			reorganizeList();
			process();
			return;
		}
	}
	private void reorganizeList() {
		LinkedList<GProcessedField> lfnew= new LinkedList<GProcessedField>();
		Iterator<GProcessedField> it= this.listFieldsByWidth.iterator();
		while (it.hasNext()){
			GProcessedField gfield= it.next();
			int minWidth=gfield.getBounds().width;
			
			if(!lfnew.isEmpty()){
				if (minWidth>maxWidthByWidth){
					/*if (gfield.getBounds().getHeight()==this.listFields.get(0).getBounds().getHeight()){
						Rectangle rc= gfield.getBounds();
						gfield.setBounds(this.listFields.get(0).getBounds());
						this.listFields.get(0).setBounds(rc);
					}*/
					lfnew.add(0, gfield);
					this.maxWidthByWidth=minWidth;
				}else if(minWidth<this.minWidthByWidth){
					/*if (gfield.getBounds().getHeight()==this.listFields.getLast().getBounds().getHeight()){
						Rectangle rc= gfield.getBounds();
						gfield.setBounds(this.listFields.getLast().getBounds());
						this.listFields.getLast().setBounds(rc);
					}*/
					lfnew.add(gfield);
					this.minWidthByWidth=minWidth;
				}else{
					boolean b=false;
					int size=lfnew.size();
					for (int i=1; i<size-1&&!b;i++){
						GProcessedField gf=lfnew.get(i);
						int widhtgf=gf.getBounds().width;
						if(minWidth>widhtgf){
							/*if (gfield.getBounds().getHeight()==gf.getBounds().getHeight()){
								Rectangle rc= gfield.getBounds();
								gfield.setBounds(gf.getBounds());
								gf.setBounds(rc);
							}*/
							lfnew.add(i, gfield);
							
							b=true;
						}
						
					}
					/*if (gfield.getBounds().getHeight()==this.listFields.get(size-1).getBounds().getHeight()){
						Rectangle rc= gfield.getBounds();
						gfield.setBounds(this.listFields.get(size-1).getBounds());
						this.listFields.get(size-1).setBounds(rc);
					}*/
					if (!b)
						lfnew.add(size-1, gfield);
					
				}
					
			}else{
				lfnew.add(gfield);
				this.maxWidthByWidth=gfield.getBounds().width;
				this.minWidthByWidth=this.maxWidthByWidth;
			}						
			
		}
		this.listFieldsByWidth=lfnew;
		/*System.out.println("LISTA DESPUES DE REORGANIZAR ");
		toStingListFieldsWidth();*/
	}
	
}
