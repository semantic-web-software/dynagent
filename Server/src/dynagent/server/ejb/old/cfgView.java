/*package dynagent.ejb;


public class cfgView extends Object{

	static int V_InternalEditPadd=2;
	static int H_InternalEditPadd=1;
	int CellPadd=2;


	public static int tabbV=40;
	public static int tabbH=12;

	public static int anchoBordeWin=5;
	int longMinimoCampoTexto=35;
	int longMinimoCampoTextoTabla=15;
	int longMinimoExtensionCheck=15;
	final static int anchoMinimoCampoBool=21;
	int longMinimoCampoFecha=10;
       	int longMinimoCampoFechaHora=20;
	int longMinimoCampoNumerico=8;
	int anchoMinimoCampoSalida=110;
	double bondadOptimaMinima=0.99;
	double bondadMinima=0.6;
	double esteticaMinDeFlow=0.3;
	int anchoScrollBar=20;
	public static long umbralArea=120000; //por debajo de este area minima predomina la relación de aspecto

	static double minRelAspectSoloFlow=12;

	public static int altoEtiquetaGrupo=20;
	public static int anchoBordeGrupo=4;

	public static double aspectRelation_improvIncrement=1;
	public final static int ladoBotonTabla=24;
	int grosorBordeForm=10;
	public static int Hgap=2;
	public static int Vgap=2;
	public static int GridVgap=0;
	int pesoDePrioridad=20;
	int pasosItera=10;
	int anchoMedioLabel=50;
	double umbralIncAnchoLabel=0.7;
	double margenIncAreaFrenteAspecto=0.2;
	double relAspecto=2;
	double umbralDeMejoraDeCompactacionArea=0.38;//si la compactacion no mejora este valor los items acaban en modo
						//flow que es mas eficiente, y perdemos estática
	double umbralSaltoColumn=0.7;
	double incrementoPorcentualDeIteracion=0.2;
	int altoMedioDeCampo=20;
	double umbralSaltoColumnOpcional=0.3;// esto controla, que si al contruir los paneles posibles,
						// el resto relativo al ancho actual, no cabe en el siguiente ancho
						// si lo que llevo tiene un alto superior a este umbral, creo una nueva pag
	public Object clone(){
		cfgView cfgClone= new cfgView();
		cfgClone.relAspecto=relAspecto;
		cfgClone.tabbH=tabbH;
		cfgClone.tabbV=tabbV;
		cfgClone.umbralSaltoColumn=umbralSaltoColumn;
		cfgClone.umbralSaltoColumnOpcional=umbralSaltoColumnOpcional;
		cfgClone.anchoMedioLabel=anchoMedioLabel;
		cfgClone.umbralIncAnchoLabel=umbralIncAnchoLabel;
		cfgClone.umbralDeMejoraDeCompactacionArea= umbralDeMejoraDeCompactacionArea;
		cfgClone.incrementoPorcentualDeIteracion=incrementoPorcentualDeIteracion;
		cfgClone.altoMedioDeCampo=altoMedioDeCampo;
		cfgClone.aspectRelation_improvIncrement=aspectRelation_improvIncrement;
		//cfgClone.anchoMinimoCampoBool=anchoMinimoCampoBool;
		cfgClone.pesoDePrioridad=pesoDePrioridad;
		cfgClone.longMinimoExtensionCheck=longMinimoExtensionCheck;
		cfgClone.bondadOptimaMinima=bondadOptimaMinima;
		cfgClone.bondadMinima=bondadMinima;

		cfgClone.minRelAspectSoloFlow=minRelAspectSoloFlow;
		cfgClone.Hgap=Hgap;
		cfgClone.longMinimoCampoTexto=longMinimoCampoTexto;
		cfgClone.longMinimoCampoFecha=longMinimoCampoFecha;
		cfgClone.longMinimoCampoNumerico=longMinimoCampoNumerico;
		cfgClone.anchoMinimoCampoSalida=anchoMinimoCampoSalida;
		cfgClone.margenIncAreaFrenteAspecto=margenIncAreaFrenteAspecto;

		cfgClone.V_InternalEditPadd=V_InternalEditPadd;
		cfgClone.H_InternalEditPadd=H_InternalEditPadd;
		cfgClone.CellPadd=CellPadd;
		cfgClone.pasosItera=pasosItera;
		cfgClone.esteticaMinDeFlow=esteticaMinDeFlow;

		cfgClone.altoEtiquetaGrupo=altoEtiquetaGrupo;
		cfgClone.anchoBordeGrupo=anchoBordeGrupo;

		//cfgClone.ladoBotonTabla=ladoBotonTabla;
		cfgClone.grosorBordeForm=grosorBordeForm;

		return cfgClone;
	}
}
*/