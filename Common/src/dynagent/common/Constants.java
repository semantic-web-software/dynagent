package dynagent.common;

import java.text.Collator;
import java.util.Locale;






public class  Constants {
	public static  boolean printLog=false;
	public static final int IDTO_THING=0;
	public static final int IDTO_RESULT_BATCH=1;
	public static final int IDTO_APPLICATION=2;
	public static final int IDTO_WAREHOUSE=3;
	public static final int IDTO_FUNCTIONAL_AREA=4;
	public static final int IDTO_UTASK=5;
	public static final int IDTO_UNIDADES=6;
	public static final int IDTO_USERROL=7;
	public static final int IDTO_USER=8;
	public static final int IDTO_ESTADOREALIZACION=9;
	public static final int IDTO_INSTANT=10;
	public static final int IDTO_OWNER=11;
	public static final int IDTO_ENUMERATED=12;
	public static final int IDTO_ACTION_BATCH_PARAMS = 13;
	public static final int IDTO_REPORT=14;
	public static final int IDTO_ACTION=15;
	public static final int IDTO_PARAMS=16;
	public static final int IDTO_CONFIGURATION=17;
	public static final int IDTO_ACTION_PARAMS=18;
	public static final int IDTO_AUX_PARAMS=19;
	public static final int IDTO_IMPORT=20;
	public static final int IDTO_EXPORT=21;
	public static final int IDTO_IMPORTEXPORT_PARAMS=22;
	public static final int IDTO_DATA_TRANSFER=23;
	public static final int IDTO_REPORT_FORMAT=24;
	public static final int IDTO_QUESTION_TASK=25;
	public static final int IDTO_CLASS=26;
	public static final int IDTO_PROPERTY=27;
	public static final int IDTO_DATA_PROPERTY=28;
	public static final int IDTO_OBJECT_PROPERTY=29;
	public static final int IDTO_ACTION_BATCH = 30;
	
		
	public static final String RULERJESS="JESS";
	public static final String RULERJBOSS="JBOSS";
	
	//PUEDE MODIFICARSE HACIENDO UN SET
	//public static  String RULER=RULERJESS;
	public static  String RULER=RULERJBOSS;
	
	//nombres clases especiales
	public static final String CLS_THING="owl:Thing";
	public static final String CLS_FUNCTIONAL_AREA="FUNCTIONAL_AREA";
	public static final String CLS_UTASK="UTASK";
	public static final String CLS_UNIDADES="UNIDADES";
	public static final String CLS_USERROL="USERROL";
	public static final String CLS_USER="USER";
	public static final String CLS_ESTADO_REALIZACION="ESTADO_REALIZACIÓN";
	public static final String CLS_INSTANT="INSTANT";
	public static final String CLS_OWNER="OWNER";
	public static final String CLS_PARAMS="PARAMS";
	public static final String CLS_ENUMERATED="ENUMERATED";
	
	public static final String CLS_REPORT="REPORT";
	public static final String CLS_ACTION="ACTION";
	public static final String CLS_ACTION_BATCH="ACTION_BATCH";
	public static final String CLS_IMPORT="IMPORT";
	public static final String CLS_EXPORT="EXPORT";
	
	public static final String CLS_CONFIGURATION="CONFIGURATION";
	public static final String CLS_ACTION_PARAMS="ACTION_PARAMS";
	public static final String CLS_ACTION_BATCH_PARAMS="ACTION_BATCH_PARAMS";
	public static final String CLS_AUX_PARAMS="AUX_PARAMS";
	public static final String CLS_AUX="AUX";
	
	public static final String CLS_SYSTEM_CLASS="SYSTEM_CLASS";
	public static final String CLS_DEFAULT_VALUE="VALOR_POR_DEFECTO";
	
	public static final String CLS_IMPORTEXPORT_PARAMS="PARAM_EXPORT_IMPORT";
	
	public static final String CLS_DATA_TRANSFER="DATA_TRANSFER";
	public static final String CLS_RULE_CONFIGURATION="RULE_CONFIGURATION";
	
	public static final String CLS_REPORT_FORMAT="REPORT_FORMAT";
	
	public static final String CLS_QUESTION_TASK="QUESTION_TASK";
	
	public static final String CLS_RESULT_BATCH="RESULTADO_BATCH";
	
	public static final String CLS_APPLICATION="APLICACIÓN";
	
	public static final String CLS_WAREHOUSE="ALMACÉN";
	
	public static final String CLS_LOG_EMAIL="LOG_EMAIL";
	
	public static final String CLS_NOTIFICATION = "NOTIFICACIÓN";
	
	public static final String CLS_CONFIG_PARAM = "PARAMETRO_PROCESADO";
	
	public static final String CLS_BOOLEAN_CONFIG_PARAM = "PARAMETRO_VERDADERO";
	
	public static final String CLS_NUMBER_CONFIG_PARAM = "PARAMETRO_NUMERICO";
	
	
	public static final Integer[] LIST_SYSTEM_CLASS={
		IDTO_FUNCTIONAL_AREA,
		IDTO_UTASK,
		IDTO_ESTADOREALIZACION,
		IDTO_REPORT,
		IDTO_ACTION,
		IDTO_PARAMS,
		IDTO_ACTION_PARAMS,
		IDTO_AUX_PARAMS,
		IDTO_IMPORT,
		IDTO_EXPORT,
		IDTO_IMPORTEXPORT_PARAMS,
		IDTO_DATA_TRANSFER,
		IDTO_REPORT_FORMAT,
		IDTO_QUESTION_TASK,
		IDTO_ACTION_BATCH,
		IDTO_ACTION_BATCH_PARAMS};
	
	public static final String[] LIST_SYSTEM_CLASS_NAME={
		CLS_FUNCTIONAL_AREA,
		CLS_UTASK,
		CLS_ESTADO_REALIZACION,
		CLS_REPORT,
		CLS_ACTION,
		CLS_PARAMS,
		CLS_ACTION_PARAMS,
		CLS_AUX_PARAMS,
		CLS_IMPORT,
		CLS_EXPORT,
		CLS_IMPORTEXPORT_PARAMS,
		CLS_DATA_TRANSFER,
		CLS_REPORT_FORMAT,
		CLS_QUESTION_TASK,
		CLS_ACTION_BATCH,
		CLS_ACTION_BATCH_PARAMS};
	
	public static final String relativePathOWLFILES= "file:///E:/DESARROLLO/filesOWL/";
	
	//Tipos de datos (deben de coincidir con los names que asigna Protege a los tipos de datos)
	public static final String DATA_INT="int";
	public static final String DATA_DOUBLE="double";
	public static final String DATA_STRING="string";
	public static final String DATA_MEMO="memo";
	public static final String DATA_BOOLEAN="boolean";
	public static final String DATA_TIME="time";
	public static final String DATA_DATETIME="dateTime";
	public static final String DATA_DATE="date";
	public static final String DATA_FLOAT="float";
	public static final String DATA_UNIT="unit";
	public static final String DATA_IMAGE="image";
	public static final String DATA_FILE="file";
	
	//Identificadores asociados a los tipos de datos:
	public static final int IDTO_INT=7920;
	public static final int IDTO_DOUBLE=7910;
	public static final int IDTO_STRING=7923;
	public static final int IDTO_MEMO=7945;
	public static final int IDTO_BOOLEAN=7929;
	public static final int IDTO_TIME=7905;
	public static final int IDTO_DATETIME=7912;
	public static final int IDTO_DATE=7924;
	public static final int IDTO_UNIT=7925;
	public static final int IDTO_IMAGE=7926;
	public static final int IDTO_FILE=7927;
	
	//////////////////////////////INSTANCIAS DEL MODELO://///////////////////////////////////////////////////////////////////////////////////////////////////
	//Instantes de referencia
	public static final String BEGIN1970="begin1970";
	public static final int IDO_BEGIN1970=10001;
	public static final String BEGINDAY="beginday";
	public static final int IDO_BEGINDAY=10002;
	
	//User Rol System
	public static final String USERROL_SYSTEM="userRol_System";
	public static final int IDO_USERROL_SYSTEM=10003;

	//Estados Realización User Task
	public static final String INDIVIDUAL_REALIZADO="realizado";
	
	
	public static final String INDIVIDUAL_PENDIENTE="pendiente";
	
	
	public static final String INDIVIDUAL_CANCELADO="cancelado";
	
	
	public static final String INDIVIDUAL_PLANIFICADO="planificado";
	
	
	
	public static final String INDIVIDUAL_INFORMADO="informada";
	
	
	public static final String INDIVIDUAL_INICIALIZANDO="inicializando";

	
	public static final String INDIVIDUAL_PREVALIDANDO="prevalidando";

	
	
	public static final String Task_PLANIFICACIÓN="Task_PLANIFICACIÓN";
	

	//////////////////////////////FIN INSTANCIAS DEL MODELO://///////////////////////////////////////////////////////////////////////////////////////////////////

	//LEVELS
	public static final int LEVEL_MODEL=0;
	public static final int LEVEL_FILTER=1;
	public static final int LEVEL_PROTOTYPE=2;
	public static final int LEVEL_INDIVIDUAL=3;
	
	
	//los valores de un boolean se codificaran en tabla como un numero. Estas son las equivalencias
	public static final int ID_BOOLEAN_FALSE=0;
	public static final int ID_BOOLEAN_TRUE=1;
	public static final String BOOLEAN_FALSE="false";
	public static final String BOOLEAN_TRUE="true";
	
	
	
	public static final String RULERVALUE_ANYVALUE="ANYVALUE";
	
	//ACCESSOS	
	public final static int ACCESS_VIEW=1;
	public final static int ACCESS_NEW=2;
	public final static int ACCESS_SET=4;
	 //access 8 y 16 están ocupados por los permisos de la versión antigua Rel y UnRel
	public final static int ACCESS_DEL=32;
	public final static int ACCESS_FIND=64;
    public final static int ACCESS_ABSTRACT=512;
    
    
    
    public final static String ACCESS_VIEW_NAME="VER";
	public final static String ACCESS_NEW_NAME="CREAR";
	public final static String ACCESS_SET_NAME="EDITAR";
	public final static String ACCESS_DEL_NAME="ELIMINAR";
    public final static String ACCESS_ABSTRACT_NAME="ABSTRACT";
    public final static String ACCESS_FIND_NAME="BUSCAR";
    
    
    public final static String pathVersionModelImported="E:\\DESARROLLO\\ONTOLOGIA\\VERSIONESIMPORTADAS\\";
    
    	
//	Rangos de identificadores
    //este rango esta separando las clases de sistema (utask,userrol,unidades,.....) del resto de clases.
    public static final int MIN_ID_NO_SPECIALCLASS=100;
    
    public static final int MIN_ID_INDIVIDUAL=10100;
    //DE 10000 A 10100 LOS DEJAMOS PARA IDOS ARCODEADOS EN CONSTANTS
    
	//minimo idindividual para BBDD
    public static final int MIN_ID_INDIVIDUAL_BDDD=1000;
	public static final int MAX_ID_INDIVIDUAL=1000000;
	public static final int MAX_ID_PROTOTYPE=-1;
	public static final int MAX_ID_FILTER=-1;
	public static final int MAX_ID_CLASS=7900;
	public static final int MIN_ID_CLASS=0;
	
	public static final int MIN_IdPROP_MODEL=100;
	public static final int MIN_IdPROP_SYSTEM=0;
	public static final int MAX_IdPROP_SYSTEM=99;
	
	public static final int MIN_LENGHT_MEMO=50;
	public static final int MAX_LENGHT_TEXT=200;
	
	public static final int TIMEMILLIS=1000;
	
	//Rango de identificadores para las tipos de datos.
	public static final int MIN_ID_DATACLS=7901;
	public static final int MAX_ID_DATACLS=8000;
	//VALORES DEL OPERADOR
	public static final String OP_INTERSECTION="AND";
	public static final String OP_UNION="OR";
	public static final String OP_ONEOF="ONEOF";
	public static final String OP_CARDINALITY="CAR";
	public static final String OP_HASVALUE="HASVAL";
	//Cardinalidad inversa, nos permitirá definir cardinalidades de las inversas de algunas propiedades sin necesidad
	//de definir explicitamente estas propiedades inversas
	//public static final String OP_CARDINALITY_INV="CARD_INV";
	public static final String OP_NEGATION="NOT";
	public static final String OP_QUANTITYDETAIL="QCR";
//	nuevo operadoR para los valores por defecto
	public static final String OP_DEFAULTVALUE="DEFVAL";
	
	public static final int IdPROP_RDN=2;
	public static final String PROP_RDN="rdn";
	
	
	public static final int IdPROP_USUARIO=3;
	public static final String PROP_USUARIO="usuario";
	
	
	public static final String USER_SYSTEM="SYSTEM";
	
	public static final String GLOBAL_URL="GLOBAL";//en menus centralizados (url global) se asigna este origen de replica que solo indica que debe pasar por set Local idos
	
	public static final String USER_OWNERALLACCESS="OWNERALLACCESS";

	
//Propiedades especiales de utask
	public static final int IdPROP_TARGETCLASS=8;
	public static final String PROP_TARGETCLASS="targetClass";
	
	//public static final int IdPROP_CLASS=9;
	//public static final String PROP_CLASS="class";
	
	
	public static final int IdPROP_MYFUNCTIONALAREA=11;
	public static final String PROP_MYFUNCTIONALAREA="myFunctionalArea";
	public static final int IdPROP_USERROL=12;
	public static final String PROP_USERROL="userRol";
	public static final int IdPROP_ESTADOREALIZACION=13;
	public static final String PROP_ESTADOREALIZACION="estadoRealizacion";
	//public static final int IdPROP_OPERATION=14;
	//public static final String PROP_OPERATION="operation";
	
	//PROPIEDADES DE USER
	public static final int IdPROP_OWNER=15;
	public static final String PROP_OWNER="owner";
	public static final int IdPROP_PASSWORD=16;
	public static final String PROP_PASSWORD="password";
	
	public static final String PROP_MAIL="email";
	public static final String PROP_AREA="area";
	
	
	///CONSTANTES DEL MODELO QUE SE USAN EN CONFIGURACION
	//REPLICAS
	public static final String PROP_ALMACÉN="almacén";
	public static final String CLS_MI_EMPRESA="MI_EMPRESA";
	public static final String CLS_ALMACEN="ALMACÉN";
	
	
	//ÍNDICES
	public static final String CLS_INDICE="ÍNDICE";
	public static final String PROP_DOMINIO="dominio";
	//prefijo temporal
	public static final String PROP_MASC_PREFIX_TEMP="máscara_prefijo_temporal";
	public static final String PROP_CAMPO_EN_PREFIJO_TEMP="campo_en_prefijo_temporal";
	public static final String PROP_CONTADOR_AÑO="contador_año";
	public static final String PROP_ULTIMO_PREFIJO_TEMP="último_prefijo_temporal";
	//resto en orden de aparicion
	public static final String PROP_CAMPO_EN_PREFIJO="campo_en_prefijo";
	public static final String PROP_PREFIJO="prefijo";
	public static final String PROP_INICIO_CONTADOR="inicio_contador";
	public static final String PROP_SUFIJO="sufijo";
	public static final String PROP_SUFIJO_GLOBAL="sufijo_global";
	public static final String PROP_MIN_DIGITS="dígitos_mínimos";
	//filtro
	public static final String PROP_CAMPO_FILTRO="campo_filtro";
	public static final String PROP_VALOR_FILTRO="valor_filtro";

	

	//CONFIGURADOR
	public static final String CLS_ALIAS="ALIAS";
	public static final String CLS_ALIAS_PROPERTY="ALIAS_PROPIEDAD";
	public static final String CLS_ALIAS_CLASS="ALIAS_FORMULARIO";
	public static final String CLS_ALIAS_UTASK="ALIAS_ÁMBITO";
	public static final String CLS_ALIAS_GROUP="ALIAS_GRUPO";
	public static final String CLS_CLASS="CLASE";
	public static final String CLS_ACTION_INDIVIDUAL="ACCIÓN";//Constante para la clase que guarda los individuos de acción
	public static final String CLS_REPORT_INDIVIDUAL="INFORME";//Constante para la clase que guarda los individuos de acción
	public static final String CLS_SUBREPORT_INDIVIDUAL="SUBINFORME";
	public static final String CLS_MENU="MENÚ";
	public static final String CLS_PROPERTY="PROPIEDAD";
	public static final String CLS_DATA_PROPERTY="PROPIEDAD_DATO";
	public static final String CLS_OBJECT_PROPERTY="PROPIEDAD_OBJETO";
	public static final String CLS_CARDMED_TABLE="TAMAÑO_TABLA";
	public static final String CLS_CARDMED_FIELD="TAMAÑO_CAMPO";
	public static final String CLS_CARDINALITY="CARDINALIDAD";
	public static final String CLS_ESSENTIALPROP="CAMPO_DESTACADO";
	public static final String CLS_REQUIRED="CAMPO_REQUERIDO";
	public static final String CLS_MASK="MÁSCARA_CAMPO";
	public static final String CLS_COLUMNPROPERTY="COLUMNAS_TABLA";
	public static final String CLS_ORDER="ORDEN_CAMPO";
	public static final String CLS_ORDER_WITH_FILTER="ORDEN_CAMPO_CON_FILTRO";
	public static final String CLS_ORDERPROPERTY="ORDEN_RELATIVO_CAMPOS";
	public static final String CLS_GROUPS="GRUPO_CAMPOS";
	public static final String CLS_ACCESS="PERMISO";
	public static final String CLS_ACCESS_PROPERTY="PERMISO_PROPIEDAD";
	public static final String CLS_ACCESS_CLASS="PERMISO_FORMULARIO";
	public static final String CLS_ACCESS_UTASK="PERMISO_ÁMBITO";
	public static final String CLS_ACCESS_GENERIC="PERMISO_GENÉRICO";
	public static final String CLS_ACCESS_TYPE="TIPO_PERMISO";
	public static final String CLS_ACCESS_ACTION="ACCIÓN_PERMISO";
	public static final String CLS_DATA_PARAM="PARÁMETRO_DATO";
	public static final String CLS_OBJECT_PARAM="PARÁMETRO_OBJETO";
	public static final String CLS_DATATYPE="TIPO_DATO";
	public static final String CLS_PRIORITY="PRIORIDAD";
	public static final String CLS_CLASIFICATION = "CLASIFICATION";
	
	public static final String PROP_PROPERTY="propiedad";
	public static final String PROP_SCOPE="ámbito";
	public static final String PROP_ALIAS="alias";
	
	public static final String PROP_RESERVED_ID="id";
	
	public static final String PROP_SIZE="tamaño";
	public static final String PROP_TABLE="tabla";
	
	public static final String PROP_REGULAR_EXPRESSION="expresión_regular";
	public static final String PROP_LENGTH="longitud";
	
	public static final String PROP_ORDER="orden";
	public static final String PROP_COLUMNS="columnas";
	public static final String PROP_PROPERTY_PATH="ruta_propiedad";
	
	
	public static final String PROP_FIELDS="campos";
	
	public static final String PROP_PRIORITY="prioridad";
	public static final String PROP_ACCESS="permiso";
	public static final String PROP_ACCESS_ACTION="acción_permiso";
	
	public static final String PROP_VALUE="valor";
	
	public static final String PROP_FUNCTIONAL_AREA="área_funcional";
	
	public static final String PROP_REPORT_FILE="archivo";
	public static final String PROP_REPORT_ORIGINAL_FILE="archivo_original";
	public static final String PROP_REPORT_NCOPIES="copias_impresión";
	public static final String PROP_REPORT_GENERATE_EXCEL="importación_excel";
	public static final String PROP_REPORT_PREPRINT="secuencia_preimpresión";
	public static final String PROP_REPORT_POSTPRINT="secuencia_postimpresión";
	public static final String PROP_REPORT_PRINTER="impresora";
	public static final String PROP_REPORT_PRINT_DIALOG="formulario_impresión";
	public static final String PROP_REPORT_PARAM="parámetro_filtrado";
	public static final String PROP_REPORT_SUBREPORT="subinforme";
	public static final String PROP_REPORT_COMMENT="observaciones";
	public static final String PROP_REPORT_EXEC_PRINTSEQUENCE="ejecutar_secuencia_impresión";
	public static final String PROP_PARAM_DATATYPE="tipo_dato";
	public static final String PROP_PARAM_OBJECTTYPE="tipo_objeto";
	public static final String PROP_CARMIN="mínimo_número_valores";
	public static final String PROP_CARMAX="máximo_número_valores";
	public static final String PROP_DEFAULTVALUE="valor_defecto";
	public static final String PROP_NAME="nombre";
	
	//PROPIEDADES PARA USERTASK TIPO FECHA
	
	public static final int IdPROP_ASIGNDATE=17;
	public static final String PROP_ASIGNDATE="fechaAsignacion";
	public static final int IdPROP_TOPDATE=18;
	public static final String PROP_TOPDATE="fechaTope";
	public static final int IdPROP_EJECUTEDATE=19;
	public static final String PROP_EJECUTEDATE="fechaEjecucion";
	
	public static final int IdPROP_BUSINESSCLASS=20;
	public static final String PROP_BUSINESSCLASS="businessClass";
	
	public static final int IdPROP_OBJECTDELETED=21;
	public static final String PROP_OBJECTDELETED="objectDeleted";
	
	public static final int IdPROP_PARAMS=22;
	public static final String PROP_PARAMS="params";
	
	public static final int IdPROP_SOURCECLASS=23;
	public static final String PROP_SOURCECLASS="sourceClass";
	
	public static final int IdPROP_ITERATOR=24;
	public static final String PROP_ITERATOR="iterator";
	
	public static final int IdPROP_CHECKPRINTING=25;
	public static final String PROP_CHECKPRINTING="confirmar_impresión";
	
	public static final int IdPROP_DIRECTIMPRESION=26;
	public static final String PROP_DIRECTIMPRESION="impresión_directa";
	
	public static final int IdPROP_OBJETO=27;
	public static final String PROP_OBJETO="objeto";
	
	public static final int IdPROP_PROPIEDAD=28;
	public static final String PROP_PROPIEDAD="propiedad";
	
	public static final int IdPROP_VALOR=29;
	public static final String PROP_VALOR="valor";
	
	public static final int IdPROP_FILE=30;
	public static final String PROP_FILE="fichero";
	
	public static final int IdPROP_TARGETPROGRAM=31;
	public static final String PROP_TARGETPROGRAM="programa_destino";

	public static final int IdPROP_REPORT_PREVIEW=32;
	public static final String PROP_REPORT_PREVIEW="vista_previa";
	
	public static final int IdPROP_REPORT_FORMAT=33;
	public static final String PROP_REPORT_FORMAT="formato_informe";
	
	public static final int IdPROP_RESULT=34;
	public static final String PROP_RESULT="resultado";
	
	public static final int IdPROP_CONFIRMED_SOURCE=35;
	public static final String PROP_CONFIRMED_SOURCE="confirmedSource";
	
	public static final int IdPROP_CONFIGURED_MODULES=36;
	public static final String PROP_CONFIGURED_MODULES="módulos_configurados";
	
	public static final String PROP_ORDER_NUMBER="número";
	
	public static final String PROP_CLASS="clase";
	public static final String PROP_EVENT="evento";
	public static final String PROP_EVENT_TYPE="tipo_evento";
	public static final String PROP_MESSAGE="mensaje";
	public static final String PROP_SUBJECT="asunto";
	public static final String PROP_REPORT="informe";
	public static final String PROP_MI_EMPRESA="mi_empresa";
	public static final String PROP_PROPERTY_TO_NOTIFICATION_EMAIL="propiedad_hacia_email_notificación";
	public static final String PROP_NOTIFICATION_EMAIL="email_notificaciones";
	public static final String PROP_GLOBAL="global";
	
	// # User Interface
	public static final String USERINTERFACE = "SHELL";
	
	// Constantes para el metodo de busca en jboss 
	
	public static final int SEARCHBYNULL=0;
	public static final int SEARCHBYNOTNULL=1;
	public static final int SEARCHBYCONCRETE=2;
	public static final int SEARCHBYCONCRETEORNULL=3;
	
	//profundidad navegación filtros
	public static final int MAX_DEPTH_FILTERS=1;
	public static final int MAX_DEPTH_SEARCH_FILTERS=3;
	
	// Prioridades de los permisos
	public static final int MAX_ACCESS_PRIORITY=6;
	
	
	//constantes para los grupos de reglas
	public static final String  QUERYGROUP_rules= "query";
	public static final String  RULESGROUP_rules= "rules";
	public static final String  INICIALIZERULESGROUP= "inicializerules";
	public static final String  BDDRULESGROUP= "bddrules";
	public static final String  FINDERRULESGROUP= "finder";
	public static final String  CHECKMODELRULESGROUP= "checkmodel";
	
	public static final String  DELETEDGROUPRULES= "deletedgrouprules";
	public static final String  ADVANCEDCONFIGURATION_RULES= "advancedconfiguration";
	public static final String  REPORTS_RULES= "reports";
	
	//CONSTANTES DE DEPURACIÓN
	public static  boolean DEBUG_RULESINRULEENGINE=false;
	
	//CONSTANTE PARA EL MODO DEBUG
	public static boolean MODO_DEBUG = true;
	
	public static final String RULER_SERVER = "Server";
	public static final String RULER_APPLET = "Applet";
	public static final String RULER_SCHEDULER = "Scheduler";
	
	public static final String INDIVIDUAL_STATE_DELETED="deleted";
	//estado predeleted 	
	public static final String INDIVIDUAL_STATE_READY="ready";
	public static final String INDIVIDUAL_STATE_INIT_LOCK="initLock";
	public static final String INDIVIDUAL_STATE_LOCK="lock";
	public static final String INDIVIDUAL_STATE_END_LOCK="endLock";
	
	//Formatos para reports
	public static final String EXCEL="excel";
	public static final String PDF="pdf";
	public static final String RTF="rtf";
	
	
	public static final String DEFAULT_RDN_CHAR="&";
	
	public static final String folderUserFiles = "userFiles";
	public static final String imagesFolder = "images";
	public static final String smallImage = "small_";
	public static final String unavailableImage = "imagen-no-disponible.jpg";
	
	// Parámetros para el doGet
	public static final String REQUEST_PARAM_TYPE = "TYPE";
	public static final String REQUEST_PARAM_ID = "ID";
	public static final String REQUEST_PARAM_BNS = "BNS";
	public static final String REQUEST_PARAM_USER = "USER";
	public static final String REQUEST_PARAM_SESSION = "SESSION";
	public static final String REQUEST_PARAM_DOWNLOAD = "DOWNLOAD";
	public static final String REQUEST_TYPE_GETREPORT = "GETREPORT";
	public static final String REQUEST_TYPE_GETFILE = "GETFILE";

	//modos de usuario
	public static final String CONFIGURATION_MODE = "configuracion";
	public static final String BUSINESS_MODE = "negocio";
	public static final String HTML5_MODE = "html5";
	
	//rdn de subscripciones
	public static final String BASIC_SUBSCRIPTION = "-2";//Subscripcion instalada en el descargable
	public static final String CUSTOM_SUBSCRIPTION = "-1";//Subscripcion instalada directamente a clientes no online y no descargable
	public static final String DEMO_SUBSCRIPTION = "0";
	public static final String SHAREWARE_SUBSCRIPTION = "1";
	public static final String BASIC_SUBSCRIPTION_6_MONTHS = "2";
	public static final String BASIC_SUBSCRIPTION_12_MONTHS = "3";
	public static final String CUSTOM_SUBSCRIPTION_6_MONTHS= "4";
	public static final String CUSTOM_SUBSCRIPTION_12_MONTHS= "5";
	public static final String TECHNICAL_SERVICE_TICKET = "6";
	
	//tipos de instalaciones
	public static final String ONLINE_TYPE = "online";
	public static final String DOWNLOAD_TYPE = "download";
	
	public static final int FREEWARE_MAX_CONCURRENT_REQUEST = 3;
	
	public static final String CLASSIFICATION_QUESTION_TASK_RDN = "CLASSIFICATION";
	public static final String BUSINESS_QUESTION_TASK_RDN = "BUSINESS";
	public static final String TALLA_COLOR_QUESTION_TASK_RDN = "TALLA_COLOR";
	
	public static final String ADMIN_ROL="administrador";//Usado para entrar en modo configuración
	
	public static Collator languageCollator = Collator.getInstance(new Locale("es","ES","EURO"));
	
	public static boolean isIDTemporal(int id){
		if(id<0)
			return true;
		else
			return false;
	}
	
	/**
	 * Comprueba si un nombre de clase esta arcodeado con un idto fijo en Constants.
	 * Si es así devuleve su identificador, sino devuelve NULL
	 */
	public static Integer getIdConstantClass(String name){
		Integer idto=null;
			if(name.equals(Constants.CLS_THING)){
			 idto=Constants.IDTO_THING;
		 }
		 else if(name.equals(Constants.CLS_UNIDADES)){
			 idto=Constants.IDTO_UNIDADES;
		 }
		 else if(name.equals(Constants.CLS_ACTION_PARAMS)){
			 idto=Constants.IDTO_ACTION_PARAMS;
		 }
		 else if(name.equals(Constants.CLS_CONFIGURATION)){
			 idto=Constants.IDTO_CONFIGURATION;
		 }	
		 
		 else if(name.equals(Constants.CLS_FUNCTIONAL_AREA)){
			 idto=Constants.IDTO_FUNCTIONAL_AREA;
		 }
		 else if(name.equals(Constants.CLS_UTASK)){
			 idto=Constants.IDTO_UTASK;
		 }
		 else if(name.equals(Constants.CLS_USERROL)){
			 idto=Constants.IDTO_USERROL;
		 }
		 else if(name.equals(Constants.CLS_USER)){
			 idto=Constants.IDTO_USER;
		 }
		 else if(name.equals(Constants.CLS_ESTADO_REALIZACION)){
			 idto=Constants.IDTO_ESTADOREALIZACION;
		 }
		 else if(name.equals(Constants.CLS_OWNER)){
			 idto=Constants.IDTO_OWNER;
		 }
		 else if(name.equals(Constants.CLS_INSTANT)){
			 idto=Constants.IDTO_INSTANT;
		 }
		 else if(name.equals(Constants.CLS_ENUMERATED)){
			 idto=Constants.IDTO_ENUMERATED;
		 }
		 else if(name.equals(Constants.CLS_REPORT)){
			 idto=Constants.IDTO_REPORT;
		 }
		 else if(name.equals(Constants.CLS_PARAMS)){
			 idto=Constants.IDTO_PARAMS;
		 }
		 else if(name.equals(Constants.CLS_AUX_PARAMS)){
			 idto=Constants.IDTO_AUX_PARAMS;
		 }
		 else if(name.equals(Constants.CLS_ACTION)){
			 idto=Constants.IDTO_ACTION;
		 }
		 else if(name.equals(Constants.CLS_IMPORT)){
			 idto=Constants.IDTO_IMPORT;
		 }
		 else if(name.equals(Constants.CLS_EXPORT)){
			 idto=Constants.IDTO_EXPORT;
		 }
		 else if(name.equals(Constants.CLS_IMPORTEXPORT_PARAMS)){
			 idto=Constants.IDTO_IMPORTEXPORT_PARAMS;
		 }
		 else if(name.equals(Constants.CLS_DATA_TRANSFER)){
			 idto=Constants.IDTO_DATA_TRANSFER;
		 }
		 else if(name.equals(Constants.CLS_REPORT_FORMAT)){
			 idto=Constants.IDTO_REPORT_FORMAT;
		 }
		 else if(name.equals(Constants.CLS_QUESTION_TASK)){
			 idto=Constants.IDTO_QUESTION_TASK;
		 }
		 else if(name.equals(Constants.CLS_CLASS)){
			 idto=Constants.IDTO_CLASS;
		 }
		 else if(name.equals(Constants.CLS_PROPERTY)){
			 idto=Constants.IDTO_PROPERTY;
		 }
		 else if(name.equals(Constants.CLS_DATA_PROPERTY)){
			 idto=Constants.IDTO_DATA_PROPERTY;
		 }
		 else if(name.equals(Constants.CLS_OBJECT_PROPERTY)){
			 idto=Constants.IDTO_OBJECT_PROPERTY;
		 }
		 else if(name.equals(Constants.CLS_ACTION_BATCH)){
			 idto=Constants.IDTO_ACTION_BATCH;
		 }
		 else if(name.equals(Constants.CLS_ACTION_BATCH_PARAMS)){
			 idto=Constants.IDTO_ACTION_BATCH_PARAMS;
		 }
		 else if(name.equals(Constants.CLS_RESULT_BATCH)){
			 idto=Constants.IDTO_RESULT_BATCH;
		 }
		 else if(name.equals(Constants.CLS_APPLICATION)){
			 idto=Constants.IDTO_APPLICATION;
		 }
		 else if(name.equals(Constants.CLS_WAREHOUSE)){
			 idto=Constants.IDTO_WAREHOUSE;
		 }
		return idto;
	}
	
	public static boolean isDataType(int type) {
		boolean basicType = false;
		if (type==Constants.IDTO_BOOLEAN || type==Constants.IDTO_DATETIME || type==Constants.IDTO_DATE || 
				type==Constants.IDTO_TIME || type==Constants.IDTO_DOUBLE || type==Constants.IDTO_DOUBLE || 
				type==Constants.IDTO_INT || type==Constants.IDTO_MEMO || type==Constants.IDTO_STRING || 
				type==Constants.IDTO_UNIT || type==Constants.IDTO_IMAGE || type==Constants.IDTO_FILE)
			basicType = true;
		return basicType;
	}
	
	public static Integer getIdDatatype(String name){
		Integer idto=null;
		if(name.equalsIgnoreCase(Constants.DATA_BOOLEAN)){
			idto=Constants.IDTO_BOOLEAN;
		}
		else if(name.equalsIgnoreCase(Constants.DATA_DATETIME)){
			idto=Constants.IDTO_DATETIME;
		}
		else if(name.equalsIgnoreCase(Constants.DATA_DATE)){
			idto=Constants.IDTO_DATE;
		}
		else if(name.equalsIgnoreCase(Constants.DATA_TIME)){
			idto=Constants.IDTO_TIME;
		}
		else if(name.equalsIgnoreCase(Constants.DATA_DOUBLE)){
			idto=Constants.IDTO_DOUBLE;
		}
		else if(name.equalsIgnoreCase(Constants.DATA_FLOAT)){
			idto=Constants.IDTO_DOUBLE;
		}
		else if(name.equalsIgnoreCase(Constants.DATA_INT)){
			idto=Constants.IDTO_INT;
		}
		else if(name.equalsIgnoreCase(Constants.DATA_MEMO)){
			idto=Constants.IDTO_MEMO;
		}
		else if(name.equalsIgnoreCase(Constants.DATA_STRING)){
			idto=Constants.IDTO_STRING;
		}
		else if(name.equalsIgnoreCase(Constants.DATA_UNIT)){
			idto=Constants.IDTO_UNIT;
		}
		else if(name.equalsIgnoreCase(Constants.DATA_IMAGE)){
			idto=Constants.IDTO_IMAGE;
		}
		else if(name.equalsIgnoreCase(Constants.DATA_FILE)){
			idto=Constants.IDTO_FILE;
		}
		return idto;
	}
	
	
	public static String getDatatype(int idto){
		String name=null;
		if(idto==Constants.IDTO_BOOLEAN){
			name=Constants.DATA_BOOLEAN;
		}
		else if(idto==Constants.IDTO_DATETIME){
			name=Constants.DATA_DATETIME;
		}
		else if(idto==Constants.IDTO_DATE){
			name=Constants.DATA_DATE;
		}
		else if(idto==Constants.IDTO_TIME){
			name=Constants.DATA_TIME;
			
		}
		else if(idto==Constants.IDTO_DOUBLE){
			name=Constants.DATA_DOUBLE;
			
		}
		else if(idto==Constants.IDTO_INT){
			name=Constants.DATA_INT;
		}
		else if(idto==Constants.IDTO_MEMO){
			name=Constants.DATA_MEMO;
		}
		else if(idto==Constants.IDTO_STRING){
			name=Constants.DATA_STRING;
		}
		else if(idto==Constants.IDTO_UNIT){
			name=Constants.DATA_UNIT;
		}
		else if(idto==Constants.IDTO_IMAGE){
			name=Constants.DATA_IMAGE;
		}
		else if(idto==Constants.IDTO_FILE){
			name=Constants.DATA_FILE;
		}
		return name;
	}
	
	
	public static Integer getIdType(String nametype){
		Integer idto=null;
		idto=Constants.getIdConstantClass(nametype);
		if(idto==null)
			idto=Constants.getIdDatatype(nametype);
		return idto;
	}
	
	
	
	public static Integer getIdConstantIndividual(String name){
		Integer ido=null;
		if(name.equals(Constants.BEGIN1970)){
		 ido=Constants.IDO_BEGIN1970;
		}
		else if(name.equals(Constants.BEGINDAY)){
			 ido=Constants.IDO_BEGINDAY;
		}
		return ido;
	}
	
	
	
		/**
		 * Comprueba si un nombre de clase de propiedad esta arcodeado con un idProp fijo. Si es así devuleve su identificador, sino devuelve NULL
		 * @param name: nombre de la propiedad
		 * @return: Integer: con el identificador de la propiedad (si existe en Constants) o null (si no existe en Constants)
		 */public static Integer getIdConstantProp(String name){
			Integer idProp=null;
			if(name.equals(Constants.PROP_ASIGNDATE)){
			 idProp=Constants.IdPROP_ASIGNDATE;
			}
			
			else if(name.equals(Constants.PROP_EJECUTEDATE)){
				 idProp=Constants.IdPROP_EJECUTEDATE;
			}
			else if(name.equals(Constants.PROP_USUARIO)){
				 idProp=Constants.IdPROP_USUARIO;
			}
			else if(name.equals(Constants.PROP_ESTADOREALIZACION)){
				 idProp=Constants.IdPROP_ESTADOREALIZACION;
			}
		
			else if(name.equals(Constants.PROP_MYFUNCTIONALAREA)){
				 idProp=Constants.IdPROP_MYFUNCTIONALAREA;
			}
		/*	else if(name.equals(Constants.PROP_OPERATION)){
				 idProp=Constants.IdPROP_OPERATION;
			}*/
			else if(name.equals(Constants.PROP_OWNER)){
				 idProp=Constants.IdPROP_OWNER;
			}
			else if(name.equals(Constants.PROP_PASSWORD)){
				 idProp=Constants.IdPROP_PASSWORD;
			}
			else if(name.equals(Constants.PROP_RDN)){
				 idProp=Constants.IdPROP_RDN;
			}
			else if(name.equals(Constants.PROP_TARGETCLASS)){
				 idProp=Constants.IdPROP_TARGETCLASS;
			}
			else if(name.equals(Constants.PROP_TOPDATE)){
				 idProp=Constants.IdPROP_TOPDATE;
			 }else if(name.equals(Constants.PROP_USERROL)){
				 idProp=Constants.IdPROP_USERROL;
			 }else if(name.equals(Constants.PROP_PARAMS)){
				 idProp=Constants.IdPROP_PARAMS;
			 }else if(name.equals(Constants.PROP_SOURCECLASS)){
				 idProp=Constants.IdPROP_SOURCECLASS;
			 }
			/* else if(name.equals(Constants.PROP_USERMODIFY)){
				 idProp=Constants.IdPROP_USERMODIFY;
			 }*/
			 else if(name.equals(Constants.PROP_CHECKPRINTING)){
				 idProp=Constants.IdPROP_CHECKPRINTING;
			 }
			 else if(name.equals(Constants.PROP_DIRECTIMPRESION)){
				 idProp=Constants.IdPROP_DIRECTIMPRESION;
			 }
			 else if(name.equals(Constants.PROP_OBJETO)){
				 idProp=Constants.IdPROP_OBJETO;
			 }
			 else if(name.equals(Constants.PROP_PROPIEDAD)){
				 idProp=Constants.IdPROP_PROPIEDAD;
			 }
			 else if(name.equals(Constants.PROP_VALOR)){
				 idProp=Constants.IdPROP_VALOR;
			 }
			 else if(name.equals(Constants.PROP_FILE)){
				 idProp=Constants.IdPROP_FILE;
			 }
			 else if(name.equals(Constants.PROP_TARGETPROGRAM)){
				 idProp=Constants.IdPROP_TARGETPROGRAM;
			 }
			 else if(name.equals(Constants.PROP_REPORT_PREVIEW)){
				 idProp=Constants.IdPROP_REPORT_PREVIEW;
			 }
			 else if(name.equals(Constants.PROP_REPORT_FORMAT)){
				 idProp=Constants.IdPROP_REPORT_FORMAT;
			 }
			 else if(name.equals(Constants.PROP_ITERATOR)){
				 idProp=Constants.IdPROP_ITERATOR;
			 }
			 else if(name.equals(Constants.PROP_RESULT)){
				 idProp=Constants.IdPROP_RESULT;
			 }
			 else if(name.equals(Constants.PROP_CONFIRMED_SOURCE)){
				 idProp=Constants.IdPROP_CONFIRMED_SOURCE;
			 }
			
				return idProp;	
			}
	
	
	public static boolean isBasicType(String name){
		return name.equals(DATA_BOOLEAN) || name.equals(DATA_DATETIME) || name.equals(DATA_DOUBLE) || name.equals(DATA_INT) || name.equals(DATA_STRING) || name.equals(DATA_TIME);
	}
	
	
	
	public static boolean isBasicClass(String name){
		return name.equals(CLS_ESTADO_REALIZACION) || name.equals(CLS_FUNCTIONAL_AREA) || name.equals(CLS_CONFIGURATION) || name.equals(CLS_INSTANT) 
		|| name.equals(CLS_THING) || name.equals(CLS_UNIDADES) || name.equals(CLS_USERROL) || name.equals(CLS_UTASK) || name.equals(CLS_ACTION_PARAMS)
		||name.equals(CLS_IMPORTEXPORT_PARAMS)||name.equals(CLS_DATA_TRANSFER)||name.equals(CLS_ACTION)||name.equals(CLS_ENUMERATED)||name.equals(CLS_SYSTEM_CLASS)
		||name.equals(CLS_AUX)||name.equals(CLS_AUX_PARAMS)||name.equals(CLS_PARAMS)||name.equals(CLS_RULE_CONFIGURATION)||name.equals(CLS_SYSTEM_CLASS);
	}
	
		
	public  static boolean isPropertyAccess(String nameAccess){
		boolean resultado=false;
		resultado=nameAccess.equals(Constants.ACCESS_SET_NAME)||nameAccess.equals(Constants.ACCESS_VIEW_NAME);
		return resultado;
	}

	public static String getRULER() {
		return RULER;
	}

	public static void setRULER(String ruler) {
		RULER = ruler;
	}

	public static boolean isPrintLog() {
		return printLog;
	}

	public static void setPrintLog(boolean printLog) {
		Constants.printLog = printLog;
	}

	public static void setDEBUG_RULESINRULEENGINE(boolean debug_rulesinruleengine) {
		DEBUG_RULESINRULEENGINE = debug_rulesinruleengine;
	}
	
	
	public static Integer getAccessType(String accesstypename){
		Integer type;
		if(accesstypename.equals(Constants.ACCESS_VIEW_NAME)){
			type=Constants.ACCESS_VIEW;
		}else if (accesstypename.equals(Constants.ACCESS_SET_NAME)){
			type=Constants.ACCESS_SET;
		}else if (accesstypename.equals(Constants.ACCESS_ABSTRACT_NAME)){
			type=Constants.ACCESS_ABSTRACT;
		}else if (accesstypename.equals(Constants.ACCESS_FIND_NAME)){
			type=Constants.ACCESS_FIND;
		}else if (accesstypename.equals(Constants.ACCESS_NEW_NAME)){
			type=Constants.ACCESS_NEW;
		}else if (accesstypename.equals(Constants.ACCESS_DEL_NAME)){
			type=Constants.ACCESS_DEL;
		}else{
			type=null;
		}
		return type;
	}

	public static String getAccessTypeName(Integer accesstype){
		String type;
		if(accesstype.equals(Constants.ACCESS_VIEW)){
			type=Constants.ACCESS_VIEW_NAME;
		}else if (accesstype.equals(Constants.ACCESS_SET)){
			type=Constants.ACCESS_SET_NAME;
		}else if (accesstype.equals(Constants.ACCESS_ABSTRACT)){
			type=Constants.ACCESS_ABSTRACT_NAME;
		}else if (accesstype.equals(Constants.ACCESS_FIND)){
			type=Constants.ACCESS_FIND_NAME;
		}else if (accesstype.equals(Constants.ACCESS_NEW)){
			type=Constants.ACCESS_NEW_NAME;
		}else if (accesstype.equals(Constants.ACCESS_DEL)){
			type=Constants.ACCESS_DEL_NAME;
		}else{
			type=null;
		}
		return type;
	}
	
	public static final Integer numero_digitos_cuenta = 4;
	public static final Integer numero_digitos_detalle_iva_re = 3;
	
	public static final Integer codigo_cuenta_hp_retenciones_practicadas = 4751;
	public static final Integer codigo_cuenta_hp_retenciones_soportadas = 473;
	public static final Integer codigo_cuenta_hp_iva_soportado = 472;
	public static final Integer codigo_cuenta_hp_iva_repercutido = 477;
	
	//Constantes relativas a las properties de contabilidad
	public static final String prop_apuntes = "apuntes";
	public static final String prop_subcuenta = "subcuenta";
	public static final String prop_contrapartida = "contrapartida";
	public static final String prop_codigo_cuenta = "codigo_cuenta";
	public static final String prop_concepto = "concepto";
	public static final String prop_concepto_cobro = "concepto_cobro";
	public static final String prop_concepto_pago  = "concepto_pago";
	public static final String prop_documento_contable = "documento_contable";
	public static final String prop_cuenta_acreedores = "cuenta_acreedores";
	public static final String prop_cuenta_proveedores = "cuenta_proveedores";
	public static final String prop_cuenta_contable = "cuenta_contable";
	public static final String prop_ejercicio = "ejercicio";
	public static final String prop_cuenta_ventas = "cuenta_ventas";
	public static final String prop_cuenta_clientes = "cuenta_clientes";
	public static final String prop_cuenta_re = "cuenta_R.E.";
	public static final String prop_configuracion_cuentas_compras = "configuración_cuentas_compras";
	public static final String prop_configuracion_cuentas_ventas = "configuración_cuentas_ventas";
	public static final String prop_cuenta_retenciones = "cuenta_retenciones";
	public static final String prop_fecha = "fecha";
	public static final String prop_fecha_inicio = "fecha_inicio";
	public static final String prop_fecha_fin = "fecha_fin";
	public static final String prop_asiento = "asiento";
	public static final String prop_importe = "importe";
	public static final String prop_debe = "debe";
	public static final String prop_base = "base";
	public static final String prop_recargo = "recargo";
	public static final String prop_total_iva = "total_iva";
	public static final String prop_retencion = "retención";
	public static final String prop_porcentaje_retencion = "porcentaje_retención";
	public static final String prop_haber = "haber";
	public static final String prop_detalle = "detalle";
	public static final String prop_desglose_iva = "desglose_iva";
	public static final String prop_iva = "iva";
	public static final String prop_cuota_iva = "cuota_iva";
	public static final String prop_porcentaje_iva = "porcentaje_iva";
	public static final String prop_cuota_recargo = "cuota_recargo";
	public static final String prop_porcentaje_recargo = "porcentaje_recargo";
	public static final String prop_cuenta_iva = "cuenta_IVA";
	public static final String prop_cliente = "cliente";
	public static final String prop_proveedor = "proveedor";
	public static final String prop_vencimientos_asignados = "vencimientos_asignados";
	public static final String prop_vencimiento = "vencimiento";
	public static final String prop_factura = "factura";
	public static final String prop_factura_que_rectifica = "factura_que_rectifica";
	public static final String prop_saldo_haber = "saldo_HABER";
	public static final String prop_saldo_debe = "saldo_DEBE";
	public static final String prop_saldo = "saldo";
	public static final String prop_deuda = "deuda";
	public static final String prop_orden = "orden";
	public static final String prop_partidas = "partidas";
	public static final String prop_carpeta = "carpeta";
	public static final String prop_nombre = "nombre";
	public static final String prop_nombre_tienda = "nombre_tienda";
	public static final String prop_nombre_empresa = "nombre_empresa";
	public static final String prop_importe_partida = "importe_partida";
	public static final String prop_base_partida = "base_partida";
	public static final String prop_factor_de_multiplicacion = "factor_de_multiplicación";
	public static final String prop_ordenes = "órdenes";
	public static final String prop_medio_de_pago = "medio_de_pago";
	public static final String prop_mi_empresa = "mi_empresa";
	public static final String prop_descripcion = "descripción";
	public static final String prop_cuenta_caja = "cuenta_caja";
	public static final String prop_cuenta_bancos = "cuenta_bancos";
	public static final String prop_saldo_subcuenta = "saldo_subcuenta";
	public static final String prop_serie = "serie";
	public static final String prop_linea = "línea";
	public static final String prop_importe_asignado = "importe_asignado";
	public static final String prop_concepto_asiento_factura = "concepto_asiento_factura";	
	public static final String prop_digitos_cuentas = "dígitos_cuentas";
	public static final String prop_estado_realizacion = "estadoRealizacion";
	public static final String prop_source_class = "sourceClass";
	public static final String prop_target_class = "targetClass";
	public static final String prop_params = "params";
	public static final String prop_origen = "origen";
	public static final String prop_entregado_metalico = "entregado_metálico";
	public static final String prop_entregado_tarjeta = "entregado_tarjeta";
	public static final String prop_ticket_venta = "ticket_venta";
	public static final String prop_cantidad = "cantidad";
	public static final String prop_producto = "producto";
	public static final String prop_programa_destino = "programa_destino";
	public static final String prop_fichero = "fichero";
	public static final String prop_valores_variables = "valores_variables";
	public static final String prop_actualizar_saldo_subcuenta = "actualizar_saldo_subcuenta";
	public static final String prop_documento = "documento";
	
	//Constantes relativas a las properties de contabilidad
	public static final String className_factura = "FACTURA";
	public static final String className_factura_a_cliente = "FACTURA_A_CLIENTE";
	public static final String className_factura_proveedor = "FACTURA_PROVEEDOR";
	public static final String className_cuenta_contable = "CUENTA_CONTABLE";
	public static final String className_apunte = "APUNTE";
	public static final String className_apunte_iva = "APUNTE_IVA";
	public static final String className_apunte_re = "APUNTE_RE";
	public static final String className_asiento = "ASIENTO";
	public static final String className_documento_contable = "DOCUMENTO_CONTABLE";
	public static final String className_documento_compra = "DOCUMENTO_COMPRA";
	public static final String className_documento_venta = "DOCUMENTO_VENTA";
	public static final String className_configuracion_contabilidad = "CONFIGURACIÓN_TRASPASO_CONTABILIDAD";
	public static final String className_apunte_abono = "APUNTE_ABONO";
	public static final String className_apunte_cargo = "APUNTE_CARGO";
	public static final String className_factura_rectificativa_ventas = "FACTURA_RECTIFICATIVA_VENTAS";
	public static final String className_factura_rectificativa_compras = "FACTURA_RECTIFICATIVA_COMPRAS"; 
	public static final String className_datos_base_pago = "DATOS_BASE_PAGO";
	public static final String className_cobro = "COBRO_FACTURA";
	public static final String className_pago = "PAGO";
	public static final String className_vencimiento = "VENCIMIENTO";
	public static final String className_vencimiento_de_pago = "VENCIMIENTO_DE_PAGO";
	public static final String className_vencimiento_de_cobro = "VENCIMIENTO_DE_COBRO";
	public static final String className_ticket = "TICKET";
	public static final String className_ticket_venta = "TICKET_VENTA";
	public static final String className_ticket_abono = "TICKET_ABONO";
	public static final String className_predefinido = "PREDEFINIDO";
	public static final String className_partida = "PARTIDA";
	public static final String className_partida_al_haber = "PARTIDA_AL_HABER";
	public static final String className_partida_al_debe = "PARTIDA_AL_DEBE";
	public static final String className_partida_iva = "PARTIDA_IVA";
	public static final String className_partida_re= "PARTIDA_RE";
	public static final String className_cuadrar_asiento = "CUADRAR_ASIENTO";
	public static final String className_importe_anterior = "IMPORTE_ANTERIOR";
	public static final String className_importe_fijo = "IMPORTE_FIJO";
	public static final String className_importe_variable = "IMPORTE_VARIABLE";
	public static final String className_porcentaje_anterior = "PORCENTAJE_IMPORTE_ANTERIOR";
	public static final String className_suma_de_ordenes = "SUMA_DE_ORDENES";
	public static final String className_orden = "ORDEN";
	public static final String className_cliente = "CLIENTE";
	public static final String className_proveedor = "PROVEEDOR";
	public static final String className_tipo_iva = "TIPO_IVA";
	
	/**
	 * Sin relleno, la cadena queda igual.
	 */
	public static final int no_fill = 0;
	/**
	 * Alinear a la izquierda y rellenar con espacios a la derecha.
	 */
	public static final int fill_blank = 1;
	/**
	 * Alinear a la derecha y rellenar con ceros a la izquierda.
	 */
	public static final int fill_zeros = 2;
	
	/**
	 * 	Formato para las 5 siguientes constantes:
	 * 		{	Campo	,	(0|1|2)	,	pos_inicio	, 	longitud	, 	valor_por_defecto	}
	 *						---------									-----------------
	 *					ctes. anteriores								solo si necesario
	 *
	 *	Queda pendiente la ampliación a los registros Individuales Opcionales (hay 6)
	 */
	
	public static final String trazaDoc(String clase, String rdn){
		if(clase.contains("FACTURA")) return "Fac:"+rdn;
		if(clase.contains("ALBARÁN")) return "Alb:"+rdn;
		if(clase.contains("TICKET")) return "Tck:"+rdn;
		if(clase.contains("PEDIDO")) return "Ped:"+rdn;
		if(clase.contains("PRESUPUESTO")) return "Pre:"+rdn;
		if(clase.contains("TRASPASO")) return "Tras:"+rdn;
		if(clase.contains("PRODUCCIÓN")) return "Pro:"+rdn;
		return null;
	}
	
	public static final Object[][] CABECERA_PRESENTADOR = {
			{ "A1", 0, 1, 2, "51" }, { "A2", 0, 3, 2, "80" },
			{ "B1", 0, 5, 12 }, { "B2", 0, 17, 6 }, { "B3", 1, 23, 6 },
			{ "C", 1, 29, 40 }, { "D", 1, 69, 20 }, { "E1", 0, 89, 4 },
			{ "E2", 0, 93, 4 }, { "E3", 1, 97, 12 }, { "F", 1, 109, 40 },
			{ "G", 1, 149, 14 }, };

	public static final Object[][] CABECERA_ORDENANTE = {
			{ "A1", 0, 1, 2, "53" }, { "A2", 0, 3, 2, "80" },
			{ "B1", 0, 5, 12 }, { "B2", 0, 17, 6 }, { "B3", 0, 23, 6 },
			{ "C", 1, 29, 40 }, { "D1", 0, 69, 4 }, { "D2", 0, 73, 4 },
			{ "D3", 0, 77, 2 }, { "D4", 0, 79, 10 }, { "E1", 1, 89, 8 },
			{ "E2", 0, 97, 2 }, { "E3", 1, 99, 10 }, { "F", 1, 109, 40 },
			{ "G", 1, 149, 14 }, };

	public static final Object[][] INDIVIDUAL_OBLIGATORIO = {
			{ "A1", 0, 1, 2, "56" }, { "A2", 0, 3, 2, "80" },
			{ "B1", 0, 5, 12 }, { "B2", 1, 17, 12 }, { "C", 1, 29, 40 },
			{ "D1", 0, 69, 4 }, { "D2", 0, 73, 4 }, { "D3", 0, 77, 2 },
			{ "D4", 0, 79, 10 }, { "E", 2, 89, 10 }, { "F1", 2, 99, 6 },
			{ "F2", 1, 105, 10 }, { "G", 1, 115, 40 }, { "H", 1, 153, 8 }, };

	public static final Object[][] TOTAL_ORDENANTE = { { "A1", 0, 1, 2, "58" },
			{ "A2", 0, 3, 2, "80" }, { "B1", 0, 5, 12 }, { "B2", 1, 17, 12 },
			{ "C", 1, 29, 40 }, { "D1", 1, 69, 20 }, { "E1", 2, 89, 10 },
			{ "E2", 1, 99, 6 }, { "F1", 2, 105, 10 }, { "F2", 2, 115, 10 },
			{ "F3", 1, 125, 20 }, { "G", 1, 145, 18 }, };
	
	public static final Object[][] TOTAL_GENERAL = { { "A1", 0, 1, 2, "59" },
		{ "A2", 0, 3, 2, "80" }, { "B1", 0, 5, 12 }, { "B2", 1, 17, 12 },
		{ "C", 1, 29, 40 }, { "D1", 2, 69, 4 }, { "D2", 1, 73, 16 },
		{ "E1", 2, 89, 10 }, { "E2", 1, 99, 6 }, { "F1", 2, 105, 10 }, 
		{ "F2", 2, 115, 10 }, { "F3", 1, 125, 20 }, { "G", 1, 145, 18 }, };

}