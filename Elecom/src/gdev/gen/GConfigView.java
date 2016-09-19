package gdev.gen;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import miniCalendar.JDateTime;

/**
 * Esta clase contiene las constantes que necesito en la interfaz, como por ejemplo los márgenes, 
 * la separación entre campos, ancho predefinido de un tipo de campo, etc.
 * @author Dynagent
 *
 */
public class GConfigView
{
    /** Separación vertical entre el texto y el componente que lo contiene (GEdit)*/
    final public static int V_InternalEditPadd=2;
    /** Separación horizontal entre el texto y el componente que lo contiene (GEdit)*/
    final public static int H_InternalEditPadd=1;
    final public static int IncrementScrollVertical=10;
    /** Separación horizontal entre dos grupos del mismo panel*/
    final public static int PanelHGap=10;
    /** Separación vertical entre dos grupos en el mismo panel*/
    final public static int PanelVGap=0;
    /** Margen izquierdo del panel*/
    final public static int PanelLeftMargin=10;
    /** Margen superior del panel*/
    final public static int PanelTopMargin=10;
    /** Margen derecho del panel*/
    final public static int PanelRightMargin=10;
    /** Margen inferior del panel*/
    final public static int PanelBottomMargin=10;
    
    /** Margen superior de la primera fila del grupo con respecto al borde superior del grupo*/
    final public static int GroupTopMargin=20;
    /** Margen inferior de la última fila del grupo con respecto al borde inferior del grupo*/
    final public static int GroupBottomMargin=10;
    /** Margen izquierdo de la primera columna con respecto al borde izquierdo del grupo*/
    final public static int GroupLeftMargin=10;
    /** Margen derecho de la última columna con respecto al borde derecho del grupo */
    final public static int GroupRightMargin=10;
    /** Separación horizontal entre campos de un mismo grupo*/
    final public static int GroupHGap=8;
    /** Separación vertical entre filas (entre campos de un mismo grupo)*/
    final public static int GroupVGap=2;
    
    final public static int HCellPad=4;
    final public static int VCellPad=10;
    
    
    //Se usan en el paquete gdev.gfld para dar tamaño a los elementos
    //Dan tamaño predefinidos a los componentes, sin tener en cuenta la etiqueta.
    /** Ancho mínimo predefinido (en ancho de carácteres) un campo de texto*/
    final public static int minimumLengthText=35;
    /** Ancho mínimo predefinido (en ancho de carácteres) un campo memo*/
    final public static int minimumLengthMemoByRow=15;
    /** Ancho mínimo predefinido de una tabla que contiene texto*/
    final public static int minimumLengthTextTable=20;
    /** Ancho mínimo predefinido del comentario de un CheckBox*/
    final public static int minimumLengthCheckExtension=15;
    /** Ancho mínimo predefinido de un campo Fecha*/
    final public static int minimumLengthDate=10;
    /** Ancho mínimo predefinido de un campo Fecha-Horas*/
    final public static int minimumLengthDateHour=20;
    /** Ancho mínimo predefinido de un campo numérico*/
    final public static int minimumLengthNumericField=8;
    /** Ancho mínimo predefinido de un CheckBox*/
    final public static int minimumWidthBoolField=16;
    /** Altura predefinida de una fila de una tabla.*/
    //final public static double heightRowTable=17; 
    /** Ancho predefinido de un botón*/
    final public static int buttonWidth=26;
    /** Alto predefinido de un botón*/
    final public static int buttonHeight=26;
    /** Ancho predefinido de una barra de desplazamiento (vertical)*/
    final public static int widthScrollBar=20;
    /** Ancho predefinido del textField de las horas*/
    final public static int hourWidth = JDateTime.widht;
    
//  Añadidos
    public static final int grosorBordeForm=10;
    public static final int longMinimoCampoFechaHora=20;
    public static final int cellPadd=2;
    
    public static final Color colorBackgroundRequired=new Color(255,255,231);//new Color(253,253,225);
    
    public static final Border borderSelected=BorderFactory.createLineBorder(Color.RED, 1);
    
    public static final float multiplySizeHighlightedFont=(float)1.6;
    
    public static final int horizontalMarginCell=5;
    
    public static final Dimension minimumSizeImage=new Dimension(150,150);
    
    public static final int widthImageButton=12;
    
    public static final double reductionSizeCheck=0.1;
    
    public static final double reductionSizeImageCheck=0.1;
    
	public static int smallImageHeight=40;
	
	public static final Color colorBackgroundPermanent=new Color(190,220,245);//new Color(150,210,250);
	
	public static final Integer limitFinderResults=100;
	
	public static boolean beepOnTables=false;
	
	public static boolean enterFocusOnAcceptButton=true;
	
	public static int redondeoDecimales=4;//Este valor es cambiado si existe un parametro de configuracion en la aplicacion

}
