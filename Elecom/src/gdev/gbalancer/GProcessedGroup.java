package gdev.gbalancer;

import java.awt.Rectangle;
import java.util.Vector;
import java.util.Enumeration;

import gdev.gfld.GFormGroup;
import gdev.gen.IViewBalancer;
import gdev.gen.GConst;
/**
 * Esta clase representa a un grupo con los campos colocados en una posición concreta.
 * It stores one set of positions for the fields.
 * @see GGrpCombination
 */
public class GProcessedGroup
{
	/**
	 * Atributo que hace referencia al objeto con la información del grupo, obtenida a partir del XML
	 */
	protected GFormGroup m_objFormField;

	/**
	 * Área ocupada por el grupo
	 */
	protected Rectangle m_rcBounds;

	/**
	 * Este vector contiene la lista de los campos que hay en el grupo.  
	 * Cada campo tiene su posición casi definitiva en el grupo y por tanto en el formulario final.
	 * Cada elemento del vector es del tipo {@link GProcessedField}.
	 */
	protected Vector m_vProcessedFieldList;

	/**
	 * Vector con todas las columnas del grupo. Cada elemento del vector es del tipo {@link GGrpColumn}
	 */
	protected Vector m_vColumnList=new Vector();

	/**
	 * El constructor.
	 * @param fg Referencia a la información del grupo leída del XML.
	 */
	public GProcessedGroup(GFormGroup fg)
	{
		m_objFormField = fg;
	}
	/**
	 * Otro constructor con el grupo ya procesado, no con el leído del XML.
	 * @param pg GProcessedGroup
	 */
	public GProcessedGroup(GProcessedGroup pg)
	{
		m_objFormField = pg.m_objFormField;
		m_rcBounds = pg.m_rcBounds.getBounds();
		if(pg.m_vProcessedFieldList!=null)
			m_vProcessedFieldList = new Vector(pg.m_vProcessedFieldList);
		if(pg.m_vColumnList!=null)
			m_vColumnList = new Vector(pg.m_vColumnList);
	}

	/**
	 * Devuelve la referencia al grupo correspondiente leído del XML
	 * @return GFormGroup - Grupo leído del XML.
	 */
	public GFormGroup getFormGroup()
	{
		return m_objFormField;
	}

	/**
	 * Cambia el grupo (leído del XML) al que se hace referencia.
	 * @param fg Nuevo grupo a referenciar
	 */
	public void setFormGroup(GFormGroup fg)
	{
		m_objFormField = fg;
	}

	/**
	 * Devuelve el área ocupada por el grupo procesado.
	 * @return Rectangle - Área ocupada por el grupo.
	 */
	public Rectangle getBounds()
	{
		return m_rcBounds;
	}

	/**
	 * Modifica el área ocupada por el grupo.
	 * @param area Es el nuevo área que ocupa el grupo.
	 */
	public void setBounds(Rectangle area)
	{
		m_rcBounds = area;
	}


	/**
	 * Modifica el vector con la lista de campos del grupo. Se cambia la referencia hacia un nuevo vector.
	 * @param v Nuevo vector con los campos del grupo. Cada elemento es del tipo {@link GProcessedField}
	 */
	public void setProcessedFieldList(Vector v)
	{
		m_vProcessedFieldList = v;
	}

	/**
	 * Devuelve el vector con todos los campos del grupo procesado.
	 * @return Vector - Vector con todos los elementos del grupo procesado. Cada elementos del vector es del tipo {@link GProcessedField}.
	 */
	public Vector getProcessedFieldList()
	{
		return m_vProcessedFieldList;
	}

	/**
	 * Devuelve el número de columnas que contiene el grupo ya procesado.
	 * @return int - Número de columnas del grupo.
	 */
	public int getColumnCount()
	{
		return m_vColumnList.size();
	}

	/**
	 * Añade una nueva columna (ya procesada) al conjunto de columnas del grupo.
	 * @param col - Columna nueva a añadir al vector de columnas (y por tanto al grupo).
	 */
	public void addColumn(GGrpColumn col)
	{
		m_vColumnList.addElement(col);
		assignRowNumbers();
		col.prepareRows();
	}

	/**
	 * Devuelve la columna del grupo para un índice concreto.
	 * @return GGrpCollumn - La columna del índice pasado como parámetro.
	 */
	public GGrpColumn getColumn(int colIndex)
	{
		if(colIndex>=m_vColumnList.size())
			return null;
		return (GGrpColumn)m_vColumnList.elementAt(colIndex);
	}

	/**
	 * Devuelve el vector con todas las columnas del grupo. Devuelve el atributo {@link #m_vColumnList}
	 * @return Vector - Devuelve el vector con todas las columnas del grupo. Cada elemento del vector es del tipo {@link GGrpColumn}
	 */
	public Vector getColumnList()
	{
		return m_vColumnList;
	}

	/**
	 * Asigna a todas las filas su número de fila en la columna.
	 *
	 */
	private void assignRowNumbers()
	{
		for(int i=0;i<m_vColumnList.size();i++)
		{
			int row = -1;
			GGrpColumn col = (GGrpColumn)m_vColumnList.elementAt(i);
			for(int j = 0;j<col.getFieldCount();j++)
			{
				GProcessedField fld = (GProcessedField)col.fieldAt(j);
				if(fld.getSubColumn()==0)
					row++;
				fld.setRow(row);
			}
		}
	}

	/**
	 * Mira si hay solapamiento entre el área como parámetro (rcInput) y alguno de los campos del grupo.
	 * @param rcInput Área que quiero ver si solapa con algún campo.
	 * @return boolean - "true" si hay solapamiento y "false" si no lo hay.
	 */
	private boolean isOverlapping(Rectangle rcInput)
	{
		Enumeration en = m_vProcessedFieldList.elements();
		while(en.hasMoreElements())
		{
			GProcessedField fld = (GProcessedField)en.nextElement();
			Rectangle rc = fld.getBounds();
			if(rc.intersects(rcInput))
				return true;
		}
		return false;
	}
	/**
	 * Este método recoloca los campos de un grupo para que se vean lo mejor posible.
	 * Al final del método llamará al alineamiento de GGrpColumn ({@link GGrpColumn#fineTune(IViewBalancer)}) para todas las columnas y quedará el grupo alineado de la mejor fomra posible.
	 * <p>
	 * El alineamiento que hace este método es el siguiente:
	 * <ol>
	 * <li>Incrementa un 25% el ancho de las tablas si es posible, para que se vean mejor.</li>
	 * <li>Coloca los elementos en su posición dependiendo del alineamiento predefinido en balancer ({@link IViewBalancer#getAlignment()})</li>
	 * <li>Por último iteramos por todas las columnas y para cada una de ellas se llama al método fineTune(...) de GGrpColumn -> {@link GGrpColumn#fineTune(IViewBalancer)}</li>
	 * </ol>
	 * </p>
	 * @param balancer Nos sirve para calcular los márgenes de la interfaz para el grupo.
	 * @see GGrpColumn
	 */
	public void fineTune(IViewBalancer balancer)
	{
		//sometimes it looks really very bad if some elements specially TABLE
		//is having a lower width than the others, increase that one if possible
		//if the space on right side is less than 25% of its width then increase it(in case of TABLE only)
		//if not place them according to the alignment specified
		int rightMostPoint = 0;
		Enumeration en = m_vProcessedFieldList.elements();
		while(en.hasMoreElements())
		{
			GProcessedField fld = (GProcessedField)en.nextElement();
			Rectangle rc = fld.getBounds();
			if(fld.getFormField().getType()==GConst.TM_TABLE)
			{
				int x = rc.x + rc.width;
				int y = rc.y;
				int width = m_rcBounds.width - x - balancer.getGroupRightMargin();
				int height = rc.height;
				//Ya no se incrementan las tablas un 25% por la cara
				/*Rectangle rcRest = new Rectangle(x, y, width, height);
				if (!isOverlapping(rcRest))
				{
					if(((int)(0.25 * rc.width))<rcRest.width)
						rcRest.width = (int)(0.25 * rc.width);
					rc.width += rcRest.width;
					fld.setBounds(rc);
					Rectangle rcCmp = fld.getComponentBounds();
					Rectangle rcCompSec = fld.getComponentSecundarioBounds();
					rcCmp.width += rcRest.width;
					rcCompSec.x +=rcRest.width;
					fld.setComponentBounds(rcCmp);
					fld.setComponentSecundarioBounds(rcCompSec);
					fld.fineTune(balancer);
				}*/
			}
			if(rc.x+rc.width>rightMostPoint)
				rightMostPoint = rc.x+rc.width;
		}
		int gapAtRight = m_rcBounds.width - rightMostPoint - balancer.getGroupRightMargin();
		int alignment = balancer.getAlignment();
		int iColumnCount = getColumnCount();
		if(alignment!=GConst.ALIGN_JUSTIFY||(alignment==GConst.ALIGN_JUSTIFY && iColumnCount==1))
		{
			int increment = 0;
			switch(alignment)
			{
			case GConst.ALIGN_LEFT:
				break;
			case GConst.ALIGN_CENTER:
			case GConst.ALIGN_JUSTIFY:
				increment = (int)(gapAtRight/2);
				break;
			case GConst.ALIGN_RIGHT:
				increment = gapAtRight;
				break;
			}
			if (increment != 0)
			{
				en = m_vProcessedFieldList.elements();
				while (en.hasMoreElements())
				{
					GProcessedField fld = (GProcessedField) en.nextElement();
					Rectangle rc = fld.getBounds();
					
						rc.x += increment;
					fld.setBounds(rc);
				}
			}
		}
		else //if alignment==GConst.ALIGN_JUSTIFY
		{
			//if column count 1, is already considered in abobe if condition
			int increment = gapAtRight/(iColumnCount-1);
			if(increment!=0)
			{
				int elCount = m_vProcessedFieldList.size();
				for(int i = 0;i<elCount; i++)
				{
					GProcessedField fld = (GProcessedField) m_vProcessedFieldList.elementAt(i);
					if(fld.getColumn()==0){
						continue;
					}
					Rectangle rc = fld.getBounds();
						rc.x += increment*fld.getColumn();

					fld.setBounds(rc);
				}
			}
		}

		//tab improvement if more than one fields are there in the same row of one colunm
		en = m_vColumnList.elements();
		while(en.hasMoreElements())
		{
			GGrpColumn col = (GGrpColumn)en.nextElement();
			col.fineTune(balancer);
		}
	}

	/*
    public void printElements(String str)
    {
        Enumeration en = m_vProcessedFieldList.elements();
        while(en.hasMoreElements())
        {
            GProcessedField fld = (GProcessedField)en.nextElement();
            System.out.println(str + " "+fld);
        }
    }*/
}
