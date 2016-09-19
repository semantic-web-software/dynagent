package dynagent.common.basicobjects;


import dynagent.common.exceptions.NotFoundException;
import dynagent.common.properties.values.Value;



public interface IndividualValue {
	
	public String getVALOR();
	
	
	public String getID();
	
	
	public Integer getIDO();
	
	public String getPROPNAME()  throws NotFoundException;//TODO NO LANZAR EXCEPCIÓN CUANDO SE MEJORE FACT CON EL CAMPO
	//propname, ahora se calcula dinamicamente haciendo una consulta a fact, cuando se discontinue en fact idto,idprop hay que
	//añadir el campo propname d

	public String getCLASSNAME();
	
	public Integer getIDTO();
	
	public boolean initialValuesChanged();
	
	public String getINITIALVALOR();
	
	public String getPREVALOR();
	
	public String getDESTINATIONSYSTEM();
	
	public Integer getLEVEL();
	
	public String toStringAmpliado();//solo para depuraciones, para ver el operador
	
	public boolean hasCHANGED();
	
	public String getFIRSTVALUE();
	
	public Value getCVALUE();
	public long getChangeTime();
	
}