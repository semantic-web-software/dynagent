/***
 * TripleInstance.java
 * @author Jose Antonio Zamora
 * 
 * Esta clase encapsula las categorias de una propiedad
 */

package dynagent.common.knowledge;

import dynagent.common.utils.Auxiliar;



public class Category{
	Integer cat;
	//CATEGORIA 13 ESTA DISPONIBLE!, 23 TB
	public static final Integer iDataProperty=2;
	public static final Integer iObjectProperty=3;
	private static final Integer iInverseFunctional=5;
	private static final Integer iFunctional=7;
	private static final Integer iSymmetric=9;
	private static final Integer iTransitive=11;
	private static final Integer iShared=13;
	private static final Integer iReflexive=17;
	public static final Integer iStructural=19;
	
	
	//Constructores
	public Category(){
		this.cat=1;
	}
	public Category(Integer icate){
		this.cat=icate;
	}

	public boolean isStructural(){
		if(this.getCat()!=null && this.getCat()%iStructural==0)
			return true;
		else
			return false;
	}
	
	public boolean isShared(){
		if (this.getCat() != null && this.getCat()%iShared == 0){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isTransitive(){
		if(this.getCat()!=null && this.getCat()%iTransitive==0)
			return true;
		else
			return false;
	}
	public boolean isSymmetric(){
		if(this.getCat()!=null && this.getCat()%iSymmetric==0)
			return true;
		else
			return false;
	}
	
	
	public boolean isReflexive(){
		if(this.getCat()!=null && this.getCat()%iReflexive==0)
			return true;
		else
			return false;}
	
	
	public boolean isFunctional(){
		if(this.getCat()!=null && this.getCat()%iFunctional==0)
			return true;
		else
			return false;
	}
	
	public boolean isInverseFunctional()
	{
		if(this.getCat()!=null && this.getCat()%iInverseFunctional==0)
			return true;
		else
			return false;
	}
	
	
	
	
	public boolean isObjectProperty()
	{
		if(this.getCat()!=null && this.getCat()%iObjectProperty==0)
			return true;
		else
			return false;
	}
	
	
	public static boolean isObjectProperty(int cat){//metodo estático para consultar externas
		return cat%iObjectProperty==0;
	}
	
	public static boolean isDataProperty(int cat){//metodo estático para consultar externas
		return cat%iDataProperty==0;
	}
	
	
	public boolean isDataProperty()
	{
		if(this.getCat()!=null && this.getCat()%iDataProperty==0)
			return true;
		else
			return false;
	}
	

	
	
	
	
	public void setStructural(){
		if(!this.isStructural())
			this.cat=this.cat*iStructural;
	}

	/**
	 * Marca la catetogoría como compartida. Para que una propiedad sea
	 * compartida también tiene que ser estructural, así que si la categoría
	 * todavía no está marcada como estructural, también la marca como
	 * estructural.
	 */
	public void setShared(){
		if (! isStructural()){
			cat = cat*iStructural;
		}
		cat = cat * iShared;
	}
	
	//DataProperty excluyente a ObjectProperty
	public void setDataProperty(){
		if(this.isObjectProperty()){
			this.deleteObjectProperty();
		}
		if(!this.isDataProperty()){
			this.cat=this.cat*iDataProperty;
		}
	}
	
//	DataProperty excluyente a ObjectProperty
	public void setObjectProperty(){
		if(this.isDataProperty()){
			this.deleteDataProperty();
		}
		if(!this.isObjectProperty())
			this.cat=this.cat*iObjectProperty;
	}
	
	public void setFunctional(){
		if(!this.isFunctional())
			this.cat=this.cat*iFunctional;
	}
	
	
	
	public void setInverseFunctional(){
		if(!this.isInverseFunctional())
			this.cat=this.cat*iInverseFunctional;
	}
	

	
	public void setReflexive(){
		if(!this.isReflexive())
			this.cat=this.cat*iReflexive;
	}
	
	public void setSymmetric(){
		if(!this.isSymmetric())
			this.cat=this.cat*iSymmetric;
	}
	
	
	public void setTransitive(){
		if(!this.isTransitive())
			this.cat=this.cat*iTransitive;
	}
	
	
	
	public void deleteStructural(){
		if(this.isStructural())
			this.cat=this.cat/iStructural;
	}
	
	public void deleteShared(){
		if (isShared()){
			cat = cat / iShared;
		}
	}
	
	public void deleteDataProperty(){
		if(this.isDataProperty())
			this.cat=this.cat/iDataProperty;
	}
	
	
	public void deleteFunctional(){
		if(this.isFunctional())
			this.cat=this.cat/iFunctional;
	}
	
	public void deleteObjectProperty(){
		if(this.isObjectProperty())
			this.cat=this.cat/iObjectProperty;
	}
	
	
	public void deleteInverseFunctional(){
		if(this.isInverseFunctional())
			this.cat=this.cat/iInverseFunctional;
	}
	
	
	
	public void deleteReflexive(){
		if(this.isReflexive())
			this.cat=this.cat/iReflexive;
	}
	
	public void deleteSymmetric(){
		if(this.isSymmetric())
			this.cat=this.cat/iSymmetric;
	}
	
	
	public void deleteTransitive(){
		if(this.isTransitive())
			this.cat=this.cat/iTransitive;
	}
	

	
	
	
	public Integer getCat() {
		return cat;
	}
	public void setCat(Integer cat) {
		this.cat = cat;
	}
	
	public Category modifyCategory(Category cat){
		System.out.println("  Actual category="+cat.toString());
		Category newCat;
		String respuesta=Auxiliar.leeTexto("¿  Desea modificarla  (S/N)?");
		if(respuesta.equals("S")){
			newCat=new Category();
			respuesta=Auxiliar.leeTexto("setDataProperty?  (S/N)");
			if(respuesta.equals("S")){
					newCat.setDataProperty();
			}
			else{
				respuesta=Auxiliar.leeTexto("setObjectProperty?  (S/N)");
				if(respuesta.equals("S")){
					newCat.setObjectProperty();
				}
			}
			respuesta=Auxiliar.leeTexto("setFunctional?  (S/N)");
			if(respuesta.equals("S"))
				newCat.setFunctional();
			respuesta=Auxiliar.leeTexto("setInverseFunctional?  (S/N)");
			if(respuesta.equals("S"))
				newCat.setInverseFunctional();
			
			respuesta=Auxiliar.leeTexto("setReflexive?  (S/N)");
			if(respuesta.equals("S")){
				newCat.setReflexive();
			}
			respuesta=Auxiliar.leeTexto("setSymmetric?  (S/N)");
			if(respuesta.equals("S")){
				newCat.setSymmetric();
			}
			respuesta=Auxiliar.leeTexto("setTransitive?  (S/N)");
			if(respuesta.equals("S")){
				newCat.setTransitive();
			}
			respuesta=Auxiliar.leeTexto("setStructural?  (S/N)");
			if(respuesta.equals("S")){
				newCat.setStructural();
				respuesta = Auxiliar.leeTexto("setShared? (S/N)");
				if (respuesta.equals("S")){
					newCat.setShared();
				}
			}
			cat=newCat;
			return newCat;
		}
		else
			return cat;
		
		
	}
	
	
	public String toString(){
		String result="{";
		if(this.isDataProperty())
			result+="dataProperty"+",";
		if(this.isObjectProperty())
			result+="objectProperty"+",";
		if(this.isFunctional())
			result+="functionalProperty"+",";
		if(this.isInverseFunctional())
			result+="inverseFunctionalProperty"+",";
		if(this.isTransitive())
			result+="transitiveProperty"+",";
		if(this.isSymmetric())
			result+="symmetricProperty"+",";
		if(this.isStructural())
			result+="structuralProperty"+",";
		if(this.isShared()){
			result+="sharedProperty,";
		}
		if(this.isReflexive())
			result+="reflexiveProperty"+",";
		
		
		
		
		//quitamos la última coma si la hubiera
		if(result.length()>1)
			result=result.substring(0, result.length()-1);
		result+="}";
		return result;
	}
	
	public static void main (String[]args){
		Category cat=new Category();
		cat.setDataProperty();
		cat.setFunctional();
		cat.setStructural();
		cat.modifyCategory(cat);
		
	} 
	
	
}