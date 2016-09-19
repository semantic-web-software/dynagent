package gdev.gbalancer;

import java.util.Vector;
import java.util.Enumeration;
import java.awt.Rectangle;

import gdev.gfld.GFormGroup;

/**
 *Esta clase contiene las diferentes posibilidades de combinacion de un grupo concreto
 */
public class GGrpCombination
{
    /**
     * Atributo para referenciar al grupo.
     */
    protected GFormGroup m_objGroup;
    /**
     * Este vector contiene todas las combinaciones de un grupo concreto.
     * Cada elemento del vector es del tipo {@link GProcessedGroup}.
     * 
     */
    protected Vector m_vCombinationList;

    /**
     * Este es el constructor de la clase.
     * @param grp Es el grupo del que queremos las diferentes combinaciones.
     */
    public GGrpCombination(GFormGroup grp)
    {
        m_objGroup = grp;
        m_vCombinationList = new Vector();
    }
    /**
     * Este m�todo a�ade una nueva combinaci�n del grupo al vector m_vCombinationList.
     * @param grpToAdd Es la combinaci�n que vamos a a�adir al vector.
     */
    public void addCombination(GProcessedGroup grpToAdd)
    {
        //As the fields are placed in columns, there can be different combinations with the same
        //number of columns. The columns are filled from left to right.
        //So if two combinations have the same number of columns the better one between them
        //is that one having maximum number of fields in the last column.
        //If we find a better combination having same number of columns
        //we simple replace the old one
        int colCntGrpToAdd = grpToAdd.getColumnCount();
        int grpCount = m_vCombinationList.size();
        for(int i = 0;i<grpCount; i++)
        {
            GProcessedGroup grpInList = (GProcessedGroup) m_vCombinationList.elementAt(i);
            int colCntGrpInList = grpInList.getColumnCount();
            if (colCntGrpInList == colCntGrpToAdd)
            {
                GGrpColumn colGrpToAdd = grpToAdd.getColumn(colCntGrpToAdd-1);
                GGrpColumn colGrpInList = grpInList.getColumn(colCntGrpInList-1);
                if(colGrpToAdd.getRowCount()>colGrpInList.getRowCount())
                    m_vCombinationList.setElementAt(grpToAdd,i);
                return;
            }
        }
        m_vCombinationList.addElement(grpToAdd);
    }
    
    /**
     * Devuelve todas las combinaciones del grupo.
     * @return Enumeration - Devuelve todas las combinaciones del grupo. Cada elemento es del tipo {@link GProcessedGroup}
     */
    public Enumeration getCombinations()
    {
        return m_vCombinationList.elements();
    }
    
    /**
     * Devuelve el n�mero de combinaciones del grupo
     * @return int - N�mero de combinaciones del grupo.
     */
    public int getCombinationCount()
    {
        return m_vCombinationList.size();
    }
    
    /**
     * Devuelve una combinaci�n concreta referenciada por el �ndice del vector que contiene todas las combinaciones ({@link #m_vCombinationList})
     * @param index �ndice de la combinaci�n concreta que quiero extraer.
     * @return GProcessedGroup - Devuelve la combinaci�n concreta del grupo. 
     */
    public GProcessedGroup getCombination(int index)
    {
        return (GProcessedGroup)m_vCombinationList.elementAt(index);
    }
    
    /**
     * Este m�todo elimina todas las malas combinaciones del grupo.
     * Una combinaci�n se considera mala si se sale de los l�mites del panel que nos pasan como par�metros.
     * @param panelWidth Ancho m�ximo del panel (no nos podemos exceder de este ancho).
     * @param panelHeight Alto m�ximo del panel (no nos podemos exceder de este alto).
     */
    public void discardPoorCombinations(int panelWidth,int panelHeight)
    {
        Vector vNew = new Vector();
        Enumeration enumComb = m_vCombinationList.elements();
        while(enumComb.hasMoreElements())
        {
            GProcessedGroup pg = (GProcessedGroup)enumComb.nextElement();
            Rectangle rc = pg.getBounds();
            if(rc.width>panelWidth||rc.height>panelHeight)
                continue;
            vNew.add(pg);
        }
        if(vNew.size()>0)
            m_vCombinationList = vNew;
    }
}
