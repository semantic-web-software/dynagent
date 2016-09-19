package gdev.gfld;

import dynagent.common.Constants;
import dynagent.common.utils.Auxiliar;

/**
 * Esta clase representa un valor para el tipo Enumerado
 */
public class GValue implements Comparable
{
    /** El id del valor*/
    protected int m_iId;
    /** El nombre*/
    protected String m_strLabel;

    /**
     * Constructor por parámetros del valor
     * @param id El atributo ID del valor
     * @param strLabel El nombre del valor
     */
    public GValue(int id,String strLabel)
    {
        m_iId = id;
        m_strLabel = strLabel;
    }
    
    /**
     * Modifical el nombre
     * @param strLabel Nuevo nombre del valor
     */
    public void setLabel(String strLabel)
    {
        m_strLabel = strLabel;
    }
    /**
     * Obtiene el nombre
     * @return String - Devuelve el nombre del valor
     */
    public String getLabel()
    {
        return m_strLabel;
    }
    
    /**
     * Obtiene el ID del valor
     * @return int - Devuelve el ID del valor
     */
    public int getId()
    {
        return m_iId;
    }

	public int compareTo(Object o) {
		int result;
		GValue obj=(GValue)o;
		if(m_strLabel==null||obj==null||obj.getLabel()==null){
			System.out.println("GVALUE compare error m_strLabel:"+m_strLabel+" objnull:"+(obj==null)+" obj.getLabel():"+(obj==null?"":obj.getLabel()));
			if(m_strLabel!=null && (obj==null||obj.getLabel()==null)) return -1;
			if(m_strLabel==null) return 1;
			return 0;			
		}
		result=Constants.languageCollator.compare(m_strLabel,obj.getLabel());
		if(result==0)
			result=Integer.valueOf(m_iId).compareTo(new Integer(obj.getId()));
		
		return result;
	}
}
