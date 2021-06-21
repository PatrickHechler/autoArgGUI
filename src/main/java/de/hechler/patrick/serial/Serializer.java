package de.hechler.patrick.serial;

import static de.hechler.patrick.serial.SerialConsts.ARRAY;
import static de.hechler.patrick.serial.SerialConsts.NON_PRIMITIVE_BOOLEAN;
import static de.hechler.patrick.serial.SerialConsts.NON_PRIMITIVE_BYTE;
import static de.hechler.patrick.serial.SerialConsts.NON_PRIMITIVE_CHAR;
import static de.hechler.patrick.serial.SerialConsts.NON_PRIMITIVE_DOUBLE;
import static de.hechler.patrick.serial.SerialConsts.NON_PRIMITIVE_FLOAT;
import static de.hechler.patrick.serial.SerialConsts.NON_PRIMITIVE_INT;
import static de.hechler.patrick.serial.SerialConsts.NON_PRIMITIVE_LONG;
import static de.hechler.patrick.serial.SerialConsts.NON_PRIMITIVE_SHORT;
import static de.hechler.patrick.serial.SerialConsts.NULL;
import static de.hechler.patrick.serial.SerialConsts.OBJECT;
import static de.hechler.patrick.serial.SerialConsts.PRIMITIVE;
import static de.hechler.patrick.serial.SerialConsts.PRIMITIVE_BOOLEAN;
import static de.hechler.patrick.serial.SerialConsts.PRIMITIVE_BYTE;
import static de.hechler.patrick.serial.SerialConsts.PRIMITIVE_CHAR;
import static de.hechler.patrick.serial.SerialConsts.PRIMITIVE_DOUBLE;
import static de.hechler.patrick.serial.SerialConsts.PRIMITIVE_FLOAT;
import static de.hechler.patrick.serial.SerialConsts.PRIMITIVE_INT;
import static de.hechler.patrick.serial.SerialConsts.PRIMITIVE_LONG;
import static de.hechler.patrick.serial.SerialConsts.PRIMITIVE_SHORT;
import static de.hechler.patrick.serial.SerialConsts.STRING;
import static de.hechler.patrick.serial.SerialConsts.BIG_INTEGER;
import static de.hechler.patrick.serial.SerialConsts.BIG_DECIMAL;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;
import java.math.BigDecimal;

public class Serializer {
	
	private final boolean saveStatic;
	private final boolean saveNonStatic;
	private final boolean saveSuperClassFiedls;
	
	public Serializer(boolean saveStatic, boolean saveNonStatic, boolean saveSuperClassFiedls) {
		this.saveStatic = saveStatic;
		this.saveNonStatic = saveNonStatic;
		this.saveSuperClassFiedls = saveSuperClassFiedls;
	}
	
	public void writeObject(OutputStream out, Object val) throws IOException {
		out.write(OBJECT);
		if (val == null) {
			out.write(NULL);
		} else {
			Class <?> cls = val.getClass();
			if (cls.isArray()) {
				writeArray(out, val);
			} else if (cls == String.class) {
				out.write(STRING);
				writeString(out, (String) val);
			} else if (cls == Boolean.class) {
				out.write(NON_PRIMITIVE_BOOLEAN);
				out.write( ((boolean) (Boolean) val) ? 1 : 0);
			} else if (cls == Integer.class) {
				out.write(NON_PRIMITIVE_INT);
				writeInt(out, (int) (Integer) val);
			} else if (cls == Long.class) {
				out.write(NON_PRIMITIVE_LONG);
				long l = (long) (Long) val;
				writeLong(out, l);
			} else if (cls == Byte.class) {
				out.write(NON_PRIMITIVE_BYTE);
				out.write( ((int) (byte) (Byte) val) & 0xFF);
			} else if (cls == Short.class) {
				out.write(NON_PRIMITIVE_SHORT);
				short s = (short) (Short) val;
				byte[] bytes = new byte[2];
				bytes[0] = (byte) s;
				bytes[1] = (byte) (s >> 8);
				out.write(bytes);
			} else if (cls == Double.class) {
				out.write(NON_PRIMITIVE_DOUBLE);
				long l = Double.doubleToRawLongBits((double) (Double) val);
				writeLong(out, l);
			} else if (cls == Float.class) {
				out.write(NON_PRIMITIVE_FLOAT);
				int i = Float.floatToRawIntBits((float) (Float) val);
				writeInt(out, i);
			} else if (cls == Character.class) {
				out.write(NON_PRIMITIVE_CHAR);
				char s = (char) (Character) val;
				byte[] bytes = new byte[2];
				bytes[0] = (byte) s;
				bytes[1] = (byte) (s >> 8);
				out.write(bytes);
			} else if (cls == BigInteger.class){
				out.write(BIG_INTEGER);
				BigInteger bi = (BigInteger) val;
				byte[] bytes = bi.toByteArray();
				writeInt(out, bytes.length);
				out.write(bytes);
			} else if (cls == BigDecimal.class){
				out.write(BIG_DECIMAL);
				BigDecimal bd = (BigDecimal) val;
				String str = bd.toEngineeringString();
				writeString(out, str);
			} else {
				out.write(OBJECT);
				writeString(out, cls.getName());
				while (true) {
					Field[] fields = cls.getDeclaredFields();
					List <Field> save = new ArrayList <>();
					for (int i = 0; i < fields.length; i ++ ) {
						int mod = fields[i].getModifiers();
						if ( (mod & Modifier.TRANSIENT) != 0) {
							continue;
						}
						if ( !saveStatic && (mod & Modifier.STATIC) != 0) {
							continue;
						}
						if ( !saveNonStatic && (mod & Modifier.STATIC) == 0) {
							continue;
						}
						save.add(fields[i]);
					}
					fields = save.toArray(new Field[save.size()]);
					writeInt(out, fields.length);
					for (int i = 0; i < fields.length; i ++ ) {
						writeString(out, fields[i].getName());
						Class <?> ft = fields[i].getType();
						try {
							boolean flag = fields[i].isAccessible();
							fields[i].setAccessible(true);
							if (ft.isPrimitive()) {
								writePrimitive(out, fields[i].get(val));
							} else {
								writeObject(out, fields[i].get(val));
							}
							fields[i].setAccessible(flag);
						} catch (IllegalAccessException e) {
							throw new InternalError(e);
						}
					}
					if (saveSuperClassFiedls) {
						cls = cls.getSuperclass();
						// if (cls != null) {
						// out.write(NEXT_SUPERCLASS);
						// }
					} else {
						cls = null;
					}
					if (cls == null) {
						out.write(NULL);
						break;
					} else {
						out.write(OBJECT);
					}
				}
			}
		}
	}
	
	private void writeArray(OutputStream out, Object arr) throws IOException {
		if (arr == null) throw new NullPointerException("can't write null array value'");
		Class <?> cls = arr.getClass();
		if ( !cls.isArray()) throw new IllegalArgumentException("can't write non array array! cls: '" + cls + "' val: '" + arr + "'");
		out.write(ARRAY);
		Class <?> comp = cls.getComponentType();
		if (comp.isArray()) {
			out.write(ARRAY);
			int deep = 2;// 1 for itself, 2 for componentType, 3 will come if first ultimateComp is also array
			Class <?> ultimateComp = comp.getComponentType();
			while (ultimateComp.isArray()) {
				ultimateComp = comp.getComponentType();
				deep ++ ;
			}
			writeInt(out, deep);
			if (ultimateComp.isPrimitive()) {
				out.write(PRIMITIVE);
				if (comp == Boolean.TYPE) {
					out.write(PRIMITIVE_BOOLEAN);
				} else if (comp == Byte.TYPE) {
					out.write(PRIMITIVE_BYTE);
				} else if (comp == Integer.TYPE) {
					out.write(PRIMITIVE_INT);
				} else if (comp == Long.TYPE) {
					out.write(PRIMITIVE_LONG);
				} else if (comp == Short.TYPE) {
					out.write(PRIMITIVE_SHORT);
				} else if (comp == Double.TYPE) {
					out.write(PRIMITIVE_DOUBLE);
				} else if (comp == Float.TYPE) {
					out.write(PRIMITIVE_FLOAT);
				} else if (comp == Character.TYPE) {
					out.write(PRIMITIVE_CHAR);
				} else {
					throw new InternalError("unknown primitive type: " + comp);
				}
			} else {
				out.write(OBJECT);
				writeString(out, ultimateComp.getName());
			}
		} else if (comp.isPrimitive()) {
			out.write(PRIMITIVE);
			if (comp == Boolean.TYPE) {
				out.write(PRIMITIVE_BOOLEAN);
			} else if (comp == Byte.TYPE) {
				out.write(PRIMITIVE_BYTE);
			} else if (comp == Integer.TYPE) {
				out.write(PRIMITIVE_INT);
			} else if (comp == Long.TYPE) {
				out.write(PRIMITIVE_LONG);
			} else if (comp == Short.TYPE) {
				out.write(PRIMITIVE_SHORT);
			} else if (comp == Double.TYPE) {
				out.write(PRIMITIVE_DOUBLE);
			} else if (comp == Float.TYPE) {
				out.write(PRIMITIVE_FLOAT);
			} else if (comp == Character.TYPE) {
				out.write(PRIMITIVE_CHAR);
			} else {
				throw new InternalError("unknown primitive type: " + comp);
			}
		} else {
			out.write(OBJECT);
			writeString(out, comp.getName());
		}
		int len = Array.getLength(arr);
		writeInt(out, len);
		for (int i = 0; i < len; i ++ ) {
			Object wt = Array.get(arr, i);
			if (comp.isArray()) {
				writeArray(out, wt);
			} else if (comp.isPrimitive()) {
				writePrimitive(out, wt);
			} else {
				writeObject(out, wt);
			}
		}
	}
	
	private void writePrimitive(OutputStream out, Object val) throws IOException {
		if (val == null) throw new NullPointerException("can't write null primitive value'");
		Class <?> cls = val.getClass();
		out.write(PRIMITIVE);
		if (cls == Boolean.class) {
			out.write(PRIMITIVE_BOOLEAN);
			if ((boolean) (Boolean) val) {
				out.write(1);
			} else {
				out.write(0);
			}
		} else if (cls == Integer.class) {
			out.write(PRIMITIVE_INT);
			writeInt(out, (int) (Integer) val);
		} else if (cls == Long.class) {
			out.write(PRIMITIVE_LONG);
			long l = (long) (Long) val;
			writeLong(out, l);
		} else if (cls == Byte.class) {
			out.write(PRIMITIVE_BYTE);
			out.write( ((int) (byte) (Byte) val) & 0xFF);
		} else if (cls == Short.class) {
			out.write(PRIMITIVE_SHORT);
			short s = (short) (Short) val;
			byte[] bytes = new byte[2];
			bytes[0] = (byte) s;
			bytes[1] = (byte) (s >> 8);
			out.write(bytes);
		} else if (cls == Double.class) {
			out.write(PRIMITIVE_DOUBLE);
			long l = Double.doubleToRawLongBits((double) (Double) val);
			writeLong(out, l);
		} else if (cls == Float.class) {
			out.write(PRIMITIVE_FLOAT);
			int i = Float.floatToRawIntBits((float) (Float) val);
			writeInt(out, i);
		} else if (cls == Character.class) {
			out.write(PRIMITIVE_CHAR);
			char s = (char) (Character) val;
			byte[] bytes = new byte[2];
			bytes[0] = (byte) s;
			bytes[1] = (byte) (s >> 8);
			out.write(bytes);
		} else {
			throw new IllegalArgumentException("this is no primitive type wrapped in an Object: class='" + cls + "' obj='" + val + "'");
		}
	}
	
	private static void writeString(OutputStream out, String write) throws IOException {
		byte[] zw = write.getBytes(StandardCharsets.UTF_8);
		byte[] bytes = new byte[zw.length + 4];
		intToBytes(bytes, 0, zw.length);
		System.arraycopy(zw, 0, bytes, 4, zw.length);
		out.write(bytes);
	}
	
	private void writeLong(OutputStream out, long l) throws IOException {
		writeInt(out, (int) l);
		writeInt(out, (int) (l >> 32));
	}
	
	private static void writeInt(OutputStream out, int val) throws IOException {
		byte[] b = new byte[4];
		intToBytes(b, 0, val);
		out.write(b);
	}
	
	private static void intToBytes(byte[] bytes, int off, int val) {
		bytes[off] = (byte) val;
		bytes[off + 1] = (byte) (val >> 8);
		bytes[off + 2] = (byte) (val >> 16);
		bytes[off + 3] = (byte) (val >> 24);
	}
	
}
