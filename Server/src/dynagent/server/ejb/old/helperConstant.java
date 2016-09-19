package dynagent.ejb.old;
/*package dynagent.ejb;

public class helperConstant {

    //TM no puede superar 100 porque seraan long 3 digitos, y queryData espera de 1 a 2
	public static final int TM_TABLA=0;
	public static final int TM_ENUMERADO= 1;
	public static final int TM_ENTERO=2;
	public static final int TM_REAL=3;
	public static final int TM_TEXTO=4;
	public static final int TM_FECHA=5;
	public static final int TM_MEMO=6;
	public static final int TM_BOOLEANO=7;
	public static final int TM_BOOLEANO_EXT=8;
	public static final int TM_IMAGEN=9;
   	public static final int TM_FECHAHORA=10;
   	
	public static final int TO_BASE=1;
	public static final int TO_USER=2;
	public static final int TO_SCHEDULER=3;
	public static final int TO_PERIODO=4;
	public static final int TO_FRECUENCIA=5;
	public static final int TO_INDICE=6;
	public static final int TO_CUANTITATIVO=7;
	public static final int TO_NULO=8;
	public static final int TO_MEDIBLE=9;
	public static final int TO_METATIPO=10;
	public static final int TO_ROL=11;
	public static final int TO_WINDOW=12;
	public static final int TO_COLOR=13;
	public static final int TO_USER_CFG=14;
	public static final int TO_TASK=15;

	public static final int FILTER_TASK=1;

	public static final int REFLEXIVA=0;

	public static final int DOM_DEFAULT=1;
	public static final int PK_VACIO=0;
	public static final float INFINITO=2147483647;

	public static final int OBJ_TRANSITION_ACTION = 1;
	public static final int TASK_TRANSITION= 2;
	public static final int ASIGNAR_OT= 3;
	public static final int EJECUTAR_OT=4;
	public static final int ASIGNAR_USER=5;
	public static final int IMPRIMIR_ACTION=6;
	public static final int TRANSFORMATION_ACTION=7;
	public static final int DECISION_ACTION=8;
	public static final int NEWTASK_ACTION=9;


	public static final int NEW_OPERATION = 1;
	public static final int SET_OPERATION = 2;
	public static final int DEL_OPERATION = 3;
	public static final int REL_OPERATION = 4;
	public static final int CONFIRM_OPERATION = 5;
	public static final int VIEW_OPERATION = 6;
	public static final int UNREL_OPERATION = 7;
	public static final int GET_OPERATION = 8;

	public static final int F_NEW_PRO = 1; //tipo de fact new pro
	public static final int F_TRAN = 2;
	public static final int	F_OBJTRAN= 3;
	public static final int	F_NEWPRO_EVENT= 4;
	public static final int	F_TRAN_EVENT= 5;
	public static final int	F_OBJTRAN_EVENT= 6;
	public static final int	F_OWNING_EVENT= 7;
	public static final int	F_LOGIN= 8;
	public static final int	F_LOCK= 9;
	public static final int F_ENDPRO_EVENT=10;
	public static final int F_UNLOCK=11;
	public static final int	F_NEWTASK_EVENT= 12;
	public static final int F_NEW_TASK = 13;
	public static final int F_GROW_OW_LEVEL = 14;
	public static final int F_DECREASE_OW_LEVEL = 15;
	public static final int F_CANCEL_PRO = 16;

	public static final int TAPOS_RDN= 2;

	//actualmente se usa para el user
	public static final int TAPOS_NOMBRE= 2;
	public static final int TAPOS_APELLIDOS= 3;
	public static final int TAPOS_ROL= 4;
	public static final int TAPOS_PWD= 5;
	
	public static final int TAPOS_DATE_INI= 6;
	public static final int TAPOS_DATE_END= 7;
	public static final int TAPOS_HORA_INI= 8;
	public static final int TAPOS_HORA_END= 9;
	public static final int TAPOS_DIA_SEMANA= 10;
	public static final int TAPOS_HORA= 11;
	public static final int TAPOS_FECHA_EXE_TASK= 12;
	public static final int TAPOS_METATIPO= 13;
	public static final int TAPOS_RESTO= 14;
	public static final int TAPOS_MAXIMO= 15;
	public static final int TAPOS_ESTADO= 16;
	public static final int TAPOS_DATE_EXPIRA= 17;
	public static final int TAPOS_MEDIDA= 18;
	public static final int TAPOS_INDICE= 19;
	public static final int TAPOS_IMAGEN= 20;
	public static final int TAPOS_ROJO= 21;
	public static final int TAPOS_AZUL= 22;
	public static final int TAPOS_VERDE= 23;
	public static final int TAPOS_TASK_TYPE= 25;
    public static final int TAPOS_OBJECT_FILTER= 27;

	public static final int TAPOS_FILTER_ROOT= 50;//son virtuales, no existen en la bd, es para el calculo
	public static final int TAPOS_IDO= 51;

	public static final int FLOW_POLICY = 1;
	public static final int ACTION_POLICY = 2;
	public static final int MANUAL_POLICY = 3;

	public static final int OWNER_USER=1;
	public static final int OWNER_ROL=2;

	public static final int LOAD_POLICY = 1;
	public static final int RAMDOM_POLICY = 2;

	public static final int OT_CTX= 1;
	public static final int CURR_USER_CTX = 2;
	public static final int THIS_CTX = 3;
	public static final int CTX_TASK = 4;
	public static final int CTX_WINDOWS = 5;

	public static final int CAT_ESTRUCTURAL=1;
	public static final int CAT_SERVICIO=2;
	public static final int CAT_TRANSFORMACION=3;
	public static final int CAT_CONTRACTUAL=4;
	public static final int CAT_ASOCIACION=6;

	public static final int ROL_SERVIDOR=5;
	public static final int ROL_CONTRATO=7;
	public static final int ROL_CONTENEDOR=8;
	public static final int ROL_PARTE=9;
	public static final int ROL_PRODUCTO=11;
	public static final int ROL_MODELO=12;
	public static final int ROL_COLOR_FONDO=13;

	public static final int REL_COMPOSICION=2;
	public static final int REL_PRODUCCION=3;
	public static final int REL_INDIRECTA=4;
	public static final int REL_TRANSITIVA=5;

	public static final int SCOPE_RELATIVO=1;
	public static final int SCOPE_DISTINGUIDO=2;

	public static final int ENUMVAL_ALLWEEKDAY= 8;
	public static final int ENUMVAL_MONTOFRI= 9;
	public static final int ENUMVAL_WEEKEND= 10;

	public static final int USER_ROL_SYSTEM= 1;
    public static final String byPassKey="dyna";

	static public boolean equals(int tm, String valA, String valB)
			throws ParseException {

		if (isNull(tm, valA) && isNull(tm, valB))
			return true;
		if (isNull(tm, valA) && !isNull(tm, valB))
			return false;
		if (!isNull(tm, valA) && isNull(tm, valB))
			return false;

		if ((tm == TM_ENUMERADO || tm == TM_ENTERO || tm == TM_REAL)
				&& (valA.matches(".+[;\\s].+") || valB.matches(".+[;\\s].+"))) {
			if (valA.matches(".+[;\\s].+") != valB.matches(".+[;\\s].+"))
				return false;
			if (valA.indexOf(";") >= 0 && valB.indexOf(";") >= 0) {
				String[] lisA = valA.split(";");
				String[] lisB = valB.split(";");
				if (lisA.length != lisB.length)
					return false;
				else {
					int[] intA = new int[lisA.length];
					int[] intB = new int[lisA.length];
					for (int i = 0; i < lisA.length; i++) {
						intA[i] = Integer.parseInt(lisA[i]);
						intB[i] = Integer.parseInt(lisB[i]);
					}
					Arrays.sort(intA);
					Arrays.sort(intB);
					for (int i = 0; i < intA.length; i++)
						if (intA[i] != intB[i])
							return false;
					return true;
				}
			}
			if (valA.matches(".+\\s.+"))
				return valA.equals(valB);
			else
				throw new ParseException(
						"helperConstant.equals, not match tm,vaA,valB " + tm
								+ "," + valA + "," + valB, 0);
		}
		if (tm == TM_ENUMERADO || tm == TM_ENTERO) {
			int vA = Integer.parseInt((valA.indexOf(".") == -1 ? valA : valA
					.substring(0, valA.indexOf("."))));
			int vB = Integer.parseInt((valB.indexOf(".") == -1 ? valB : valB
					.substring(0, valB.indexOf("."))));
			return vA == vB;
		}
		if (tm == TM_REAL) {
			double vA = Double.parseDouble(valA);
			double vB = Double.parseDouble(valB);
			return vA == vB;
		}
		if (tm == TM_TEXTO || tm == TM_MEMO || tm == TM_IMAGEN)
			return valA.equals(valB);

		if (tm == TM_BOOLEANO) {
			boolean vA = valA.equals("1");
			boolean vB = valB.equals("1");
			return vA == vB;
		}
		if (tm == TM_BOOLEANO_EXT) {
			Boolean vA = parseBooleanValue(TM_BOOLEANO_EXT, valA);
			Boolean vB = parseBooleanValue(TM_BOOLEANO_EXT, valB);
			if (!vA.equals(vB))
				return false;
			String tA = parseBooleanTextValue(TM_BOOLEANO_EXT, valA);
			String tB = parseBooleanTextValue(TM_BOOLEANO_EXT, valB);
			if (tA == null && tB == null)
				return true;
			if (tA == null && tB != null || tA != null && tB == null)
				return false;
			return tA.equals(tB);
		}
		if (tm == TM_FECHA || tm == TM_FECHAHORA)
			return dateUtil.equals(valA, valB);
		return false;
	}

	static public Object parseValue(int tm, String val) throws ParseException {
		if (val == null || val.length() == 0)
			return null;
		if (tm == TM_ENUMERADO) {
			if (val.indexOf(";") == -1)
				return new Integer(val.indexOf(".") == -1 ? val : val
						.substring(0, val.indexOf(".")));
			else {
				String[] lista = val.split(";");
				ArrayList res = new ArrayList();
				for (int i = 0; i < lista.length; i++)
					res.add(new Integer(lista[i]));
				return res;
			}
		}
		if (tm == TM_ENTERO || tm == TM_REAL) {
			if (val.matches(".+[\\s-:].+")) {
				String[] part = val.split("[\\s-:]");
				return new range((tm == TM_ENTERO ? (Object) parseInt(part[0])
						: (Object) parseReal(part[0])),
						(tm == TM_ENTERO ? (Object) parseInt(part[1])
								: (Object) parseReal(part[1])));
			}
		}
		if (tm == TM_ENTERO)
			return new Integer(val.indexOf(".") == -1 ? val : val.substring(0,
					val.indexOf(".")));
		if (tm == TM_REAL)
			return new Double(val);
		if (tm == TM_TEXTO || tm == TM_MEMO || tm == TM_IMAGEN)
			return val;
		if (tm == TM_BOOLEANO)
			return new Boolean((val.toUpperCase().equals("TRUE") || val.equals("1") || val
					.equals("-1")));
		if (tm == TM_BOOLEANO_EXT) {
			try {
				return new extendedValue(parseBooleanValue(tm, val),
						parseBooleanTextValue(tm, val));
			} catch (DataErrorException de) {
				de.printStackTrace();
				throw new ParseException(de.getMessage(), 0);
			}
		}
		if (tm == TM_FECHA || tm == TM_FECHAHORA)
			return dateUtil.parseFecha(val);
		else
			throw new ParseException("TM NO CONOCIDO " + tm, 0);
	}

	static Integer parseInt(String val) {
		return new Integer(val.indexOf(".") == -1 ? val : val.substring(0, val
				.indexOf(".")));
	}

	static Double parseReal(String val) {
		return new Double(val);
	}

	static public String valueToString(int tm, Object val) {
		if (val instanceof range) {
			return valueToString(tm, ((range) val).min) + " "
					+ valueToString(tm, ((range) val).max);
		}
		if (val instanceof Element) {
			try {
				return jdomParser.returnXML((Element) val);
			} catch (JDOMException e) {
				e.printStackTrace();
				return null;
			}
		}
		if (val instanceof String)
			return (String) val;
		if (val == null)
			return null;
		if (tm == TM_ENUMERADO || tm == TM_ENTERO || tm == TM_REAL
				|| tm == TM_TEXTO || tm == TM_MEMO || tm == TM_IMAGEN) {
			if (val instanceof ArrayList)
				return jdomParser.buildMultivalue(((ArrayList) val).toArray());
			else
				return val.toString();
		}
		if (tm == TM_FECHA) {
			java.util.Date fecha = (java.util.Date) val;
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
			return sdf.format(fecha);
		}
		if (tm == TM_FECHAHORA) {
			java.util.Date fecha = (java.util.Date) val;
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
			return sdf.format(fecha);
		}
		if (tm == TM_BOOLEANO || tm == TM_BOOLEANO_EXT) {
			if (val instanceof Boolean)
				return (((Boolean) val).booleanValue() ? "1" : "0");
			if (val instanceof extendedValue) {
				extendedValue ev = (extendedValue) val;
				String res = (((Boolean) ev.getValue()).booleanValue() ? "1"
						: "0");
				if (ev.getComment() != null && ev.getComment().length() > 0)
					res = res + ":" + ev.getComment();
				return res;
			}
		}
		return null;
	}

	static public Boolean parseBooleanValue(int tm, String val)
			throws ParseException {
		if (tm == helperConstant.TM_BOOLEANO)
			return (Boolean) helperConstant.parseValue(tm, val);

		if (tm == helperConstant.TM_BOOLEANO_EXT && val != null
				&& val.length() > 0) {
			if (val.indexOf("#NULLVALUE#") >= 0)
				return null;
			else {
				String[] buff = val.split(":");
				return (Boolean) helperConstant.parseValue(
						helperConstant.TM_BOOLEANO, buff[0]);
			}
		} else
			return null;
	}

	static public String parseBooleanTextValue(int tm, String val)
			throws ParseException {
		if (tm == helperConstant.TM_BOOLEANO)
			return null;

		if (tm == helperConstant.TM_BOOLEANO_EXT && val != null
				&& val.length() > 0) {
			String[] buff = val.split(":");
			if (buff.length > 1)
				return buff[1]!=null && !buff[1].equals("null")?buff[1]:null;
		}
		return null;
	}

	static boolean isNull(int tm, Object val) {
		if (val == null)
			return true;
		if (tm != TM_ENUMERADO)
			return val instanceof String
					&& (((String) val).length() == 0 || ((String) val)
							.equals("#NULLVALUE#"));
		else {
			if (val instanceof String
					&& (((String) val).length() == 0
							|| ((String) val).equals("#NULLVALUE#") || ((String) val)
							.equals("0")))
				return true;
			if (val instanceof Double && ((Double) val).intValue() == 0)
				return true;
			if (val instanceof Integer && ((Integer) val).intValue() == 0)
				return true;
			else
				return false;
		}
	}

	static public boolean equals(int tm, Object valA, Object valB) {
		if (isNull(tm, valA) && isNull(tm, valB))
			return true;
		if (isNull(tm, valA) && !isNull(tm, valB))
			return false;
		if (!isNull(tm, valA) && isNull(tm, valB))
			return false;
		if (valA instanceof ArrayList && !(valB instanceof ArrayList)
				|| !(valA instanceof ArrayList) && valB instanceof ArrayList)
			return false;
		return valA.equals(valB);
	}

	static public boolean equals(Object valA, Object valB) {
		if (valA == null && valB != null)
			return false;
		if (valA != null && valB == null)
			return false;
		if (valA == null && valB == null)
			return true;
		if (valA instanceof ArrayList && !(valB instanceof ArrayList)
				|| !(valA instanceof ArrayList) && valB instanceof ArrayList)
			return false;
		return valA.equals(valB);
	}
}
*/
