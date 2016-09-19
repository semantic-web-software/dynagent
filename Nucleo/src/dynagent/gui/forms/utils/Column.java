package dynagent.gui.forms.utils;

import dynagent.common.properties.Property;
import dynagent.common.utils.Auxiliar;

public class Column{
	
	private Property propertyParent;
	private Property property;
	private String name;
	private String filterValue;//Utilizado para el pivotado de columnas
	private Integer filterIdProp;//Utilizado para el pivotado de columnas
	private boolean nullable;
	private boolean structuralTree;//true si sus padres,sin contar el directo, son estructurales
	private String parentTree;//Path de properties hasta llegar a la actual. Si es del nivel 1 sera igual a null.
	private String propPath;
	private String idPropPath;
	
	public Column(Property propertyParent, Property property, String propPath, String idPropPath, String name, Integer filterIdProp, String filterValue, boolean nullable, boolean structuralTree, String parentTree) {
		super();
		this.propertyParent = propertyParent;
		this.property = property;
		this.setPropPath(propPath);
		this.setIdPropPath(idPropPath);
		this.name = name;
		this.filterIdProp=filterIdProp;
		this.filterValue=filterValue;
		this.nullable=nullable;
		this.structuralTree=structuralTree;
		this.parentTree=parentTree;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Property getProperty() {
		return property;
	}
	public void setProperty(Property property) {
		this.property = property;
	}
	public String getFilterValue() {
		return filterValue;
	}
	public void setFilterValue(String filterValue) {
		this.filterValue = filterValue;
	}
	public Integer getFilterIdProp() {
		return filterIdProp;
	}
	public void setFilterIdProp(Integer filterIdProp) {
		this.filterIdProp = filterIdProp;
	}
	@Override
	public String toString() {
		return "PropertyParent:"+propertyParent+" Property:"+property+" name:"+name+" filterIdProp:"+filterIdProp+" filterValue:"+filterValue+" parentTree:"+parentTree;
	}
	
	@Override
	public boolean equals(Object o) {
		Column obj = (Column) o;
	    if(obj!=null){
	    	if(Auxiliar.equals(obj.getProperty(), property) || Auxiliar.equals(obj.getPropPath(), propPath)){
	    		if(Auxiliar.equals(obj.getName(), name)){
	    			if(Auxiliar.equals(obj.getFilterValue(), filterValue)){
	    				if(Auxiliar.equals(obj.getFilterIdProp(), filterIdProp)){
	    						return true;
	    				}
	    			}
	    		}
	    	}
	    }
	    return false;
	}
	public boolean isNullable() {
		return nullable;
	}
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
	public Property getPropertyParent() {
		return propertyParent;
	}
	public void setPropertyParent(Property propertyParent) {
		this.propertyParent = propertyParent;
	}
	public boolean isStructuralTree() {
		return structuralTree;
	}
	public void setStructuralTree(boolean structuralTree) {
		this.structuralTree = structuralTree;
	}
	public String getParentTree() {
		return parentTree;
	}
	public void setParentTree(String parentTree) {
		this.parentTree = parentTree;
	}
	public void setPropPath(String propPath) {
		this.propPath = propPath;
	}
	public String getPropPath() {
		return propPath;
	}
	public void setIdPropPath(String idPropPath) {
		this.idPropPath = idPropPath;
	}
	public String getIdPropPath() {
		return idPropPath;
	}

}
