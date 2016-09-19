package gdev.gen;

import java.util.HashSet;
import java.util.LinkedHashMap;

public interface IComponentListener {
	public boolean addValueField(String id,Object value,int valueCls) throws AssignValueException, NotValidValueException;
	
	public boolean removeValueField(String id,Object value,int valueCls) throws AssignValueException, NotValidValueException;
	
	public boolean setValueField(String id,Object value,Object valueOld,int valueCls,int valueOldCls) throws AssignValueException, NotValidValueException;
	
	public Integer newRowTable(String id,Integer idtoRow) throws AssignValueException, NotValidValueException, EditionTableException;
	
	public Integer newSubRowTable(String idParentColumn,Integer idtoRow) throws AssignValueException, NotValidValueException;
	
	public boolean removeRowTable(String id,int idoRow,int idtoRow) throws AssignValueException, NotValidValueException;
	
	public void editInForm(int idoParent,int idoToEdit) throws EditionTableException;
	
	public void startEditionTable(String idTable,Integer idoRow,boolean pastingRow) throws EditionTableException;
	
	public void stopEditionTable(String idTable,Integer idoRow,HashSet<Integer> idosEdited,boolean pastingRow) throws EditionTableException;
	
	public void cancelEditionTable(String idTable,Integer idoRow) throws EditionTableException, AssignValueException;
	
	public boolean isNewCreation(int ido);
	
	public LinkedHashMap<String,Integer> getPossibleTypeForValue(String idParent,Object value,Integer valueCls);
	
	public Boolean isNullableForRow(Integer idoParent,Integer ido,String idColumn);
	
	public void showInformation(Integer ido,Integer idProp);
	
	public void setProcessingCopyRowTable(boolean processing);
	
	public boolean isAllowedConfigTables();
}
