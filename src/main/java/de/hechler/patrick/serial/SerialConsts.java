package de.hechler.patrick.serial;


class SerialConsts {
	
	static final int MIN_IDENTY            = 0;
	static final int NULL                  = 0;
	static final int PRIMITIVE             = 4;
	static final int PRIMITIVE_BOOLEAN     = 5;
	static final int PRIMITIVE_INT         = 6;
	static final int PRIMITIVE_LONG        = 7;
	static final int PRIMITIVE_BYTE        = 8;
	static final int PRIMITIVE_SHORT       = 9;
	static final int PRIMITIVE_DOUBLE      = 10;
	static final int PRIMITIVE_FLOAT       = 11;
	static final int PRIMITIVE_CHAR        = 12;
	static final int ARRAY                 = 13;
	static final int NON_PRIMITIVE_BOOLEAN = 14;
	static final int NON_PRIMITIVE_INT     = 15;
	static final int NON_PRIMITIVE_LONG    = 16;
	static final int NON_PRIMITIVE_BYTE    = 17;
	static final int NON_PRIMITIVE_SHORT   = 18;
	static final int NON_PRIMITIVE_DOUBLE  = 19;
	static final int NON_PRIMITIVE_FLOAT   = 20;
	static final int NON_PRIMITIVE_CHAR    = 21;
	static final int STRING                = 22;
	static final int OBJECT                = 23;
	static final int BIG_INTEGER           = 24;
	static final int BIG_DECIMAL           = 25;
	static final int MAX_IDENTY            = 25;
	
	private SerialConsts() {}
	
	//@formatter:off
	static String identyToString(int identy) {
		String ret;
		switch (identy) {
			case NULL:                  ret = "NULL"; break;
			case PRIMITIVE:             ret = "PRIMITIVE"; break;
			case PRIMITIVE_BOOLEAN:     ret = "PRIMITIVE_BOOLEAN"; break;
			case PRIMITIVE_INT:         ret = "PRIMITIVE_INT"; break;
			case PRIMITIVE_LONG:        ret = "PRIMITIVE_LONG"; break;
			case PRIMITIVE_BYTE:        ret = "PRIMITIVE_BYTE"; break;
			case PRIMITIVE_SHORT:       ret = "PRIMITIVE_SHORT"; break;
			case PRIMITIVE_DOUBLE:      ret = "PRIMITIVE_DOUBLE"; break;
			case PRIMITIVE_FLOAT:       ret = "PRIMITIVE_FLOAT"; break;
			case PRIMITIVE_CHAR:        ret = "PRIMITIVE_CHAR"; break;
			case ARRAY:                 ret = "ARRAY"; break;
			case NON_PRIMITIVE_BOOLEAN: ret = "NON_PRIMITIVE_BOOLEAN"; break;
			case NON_PRIMITIVE_INT:     ret = "NON_PRIMITIVE_INT"; break;
			case NON_PRIMITIVE_LONG:    ret = "NON_PRIMITIVE_LONG"; break;
			case NON_PRIMITIVE_BYTE:    ret = "NON_PRIMITIVE_BYTE"; break;
			case NON_PRIMITIVE_SHORT:   ret = "NON_PRIMITIVE_SHORT"; break;
			case NON_PRIMITIVE_DOUBLE:  ret = "NON_PRIMITIVE_DOUBLE"; break;
			case NON_PRIMITIVE_FLOAT:   ret = "NON_PRIMITIVE_FLOAT"; break;
			case NON_PRIMITIVE_CHAR:    ret = "NON_PRIMITIVE_CHAR"; break;
			case STRING:                ret = "STRING"; break;
			case BIG_INTEGER:           ret = "BIG_INTEGER"; break;
			case BIG_DECIMAL:           ret = "BIG_DECIMAL"; break;
			case OBJECT:                ret = "OBJECT"; break;
			default: throw new IllegalAccessError("this is no identyfier: " + identy + " minIdenty=" + MIN_IDENTY + " maxIdenty=" + MAX_IDENTY);
		}
		return ret;
	}
	//@formatter:on
	
	static String identyToStringOrElse(int identy, String elseStr) {
		if (identy < MIN_IDENTY) {
			return elseStr;
		} else if (identy > MAX_IDENTY) {
			return elseStr;
		} else {
			return identyToString(identy);
		}
	}
	
	
}
