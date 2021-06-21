package de.hechler.patrick.serial;

import static de.hechler.patrick.serial.SerialConsts.ARRAY;
import static de.hechler.patrick.serial.SerialConsts.BIG_DECIMAL;
import static de.hechler.patrick.serial.SerialConsts.BIG_INTEGER;
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
import static de.hechler.patrick.serial.SerialConsts.identyToStringOrElse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Deserializer {
	
	private final Map <Class <?>, Method> creates;
	
	public Deserializer(Map <Class <?>, Method> creates) {
		this.creates = Collections.unmodifiableMap(new HashMap <>(creates));
	}
	
	// public void writeObject(OutputStream out, Object val) throws IOException {
	public void overwriteObject(InputStream in, Object val) throws IOException {
		assertRead(in, OBJECT);
		assertRead(in, OBJECT);// either OBJECT, PRIMITIVE or ARRAY, but here it has to be OBJECT, because it has to be the object of val
		Class <?> cls;
		{
			String clsName = readString(in);
			try {
				cls = Class.forName(clsName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new NoClassDefFoundError(clsName);
			}
		}
		overwriteObject2(in, val, cls);
	}
	
	private void overwriteObject2(InputStream in, Object val, Class <?> cls) throws IOException, InternalError {
		val.getClass().asSubclass(cls);
		// while (true) {
		// Field[] fields = cls.getDeclaredFields();
		// List<Field> save = new ArrayList<>();
		// for(int i = 0; i < fields.length; i ++) {
		// int mod = fields[i].getModifiers();
		// if (!saveStatic && (mod & Modifier.STATIC) != 0) continue;
		// if (!saveNonStatic && (mod & Modifier.STATIC) == 0) continue;
		// if (!saveFinal && (mod & Modifier.FINAL) != 0) continue;
		// if (!saveNonFinal && (mod & Modifier.FINAL) == 0) continue;
		// save.add(fields[i]);
		// }
		// fields = save.toArray(new Field[save.size()]);
		while (true) {
			// writeInt(out, fields.length);
			int len = readInt(in);
			// for (int i = 0; i < fields.length; i ++) {
			for (int i = 0; i < len; i ++ ) {
				// writeString(out, fields[i].getName());
				Field field;
				{
					String fn = readString(in);
					try {
						field = cls.getDeclaredField(fn);
					} catch (NoSuchFieldException | SecurityException e) {
						throw new InternalError(e);
					}
				}
				
				// Class <?> ft = fields[i].getType();
				// try {
				// boolean flag = fields[i].isAccessible();
				// fields[i].setAccessible(true);
				// if (ft.isPrimitive()) {
				// writePrimitive(out, fields[i].get(val));
				// } else {
				// writeObject(out, fields[i].get(val));
				// }
				boolean wa = field.isAccessible();
				field.setAccessible(true);
				// boolean wf = Dirty.removeFinal(field);
				Object obj = readAny(in);
				try {
					field.set(val, obj);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new InternalError(e);
				}
				// if (wf) {
				// Dirty.addFinal(field);
				// }
				field.setAccessible(wa);
				// fields[i].setAccessible(flag);
				// } catch(IllegalAccessException e) {
				// throw new InternalError(e);
				// }
				// }
			}
			// if (saveSuperClassFiedls) {
			// cls = cls.getSuperclass();
			// if (cls != null) {
			// out.write(NEXT_SUPERCLASS);
			// }
			// } else {
			// cls = null;
			// }
			// if (cls == null) {
			// out.write(NULL);
			// break;
			// }
			// }
			int zw = assertRead(in, NULL, OBJECT);
			if (zw == 0) { // NULL
				break;
			} else {// OBJECT
				cls = cls.getSuperclass();
			}
			
		}
	}
	
	private Object readAny(InputStream in) throws IOException {
		int zw = assertRead(in, OBJECT, ARRAY, PRIMITIVE);
		switch (zw) {
		case 0:
			return readObject0(in);
		case 1:
			return readArray(in);
		case 2:
			return readPrimitive(in);
		default:
			throw new InternalError("illegal index: " + zw);
		}
	}
	
	public Object readObject(InputStream in) throws IOException {
		assertRead(in, OBJECT);
		return readObject0(in);
	}
	
	private Object readObject0(InputStream in) throws IOException, InternalError, NoClassDefFoundError {
		int zw = assertRead(in, NULL, ARRAY, NON_PRIMITIVE_BOOLEAN, NON_PRIMITIVE_INT, NON_PRIMITIVE_LONG,
			NON_PRIMITIVE_BYTE, NON_PRIMITIVE_SHORT, NON_PRIMITIVE_DOUBLE, NON_PRIMITIVE_FLOAT, NON_PRIMITIVE_CHAR,
			STRING, BIG_INTEGER, BIG_DECIMAL, OBJECT);
		switch (zw) {
		case 0: // NULL
			return null;
		case 1: // ARRAY
			return readArray(in);
		case 2: // NON_PRIMITIVE_BOOLEAN
			return (Boolean) (assertRead(in, 0, 1) == 1);
		case 3: // NON_PRIMITIVE_INT
			return (Integer) readInt(in);
		case 4: // NON_PRIMITIVE_LONG
			return (Long) readLong(in);
		case 5: // NON_PRIMITIVE_BYTE
			return (Byte) (byte) in.read();
		case 6: {// NON_PRIMITIVE_SHORT
			byte[] bytes = new byte[2];
			in.read(bytes);
			short s = (short) (bytes[0] & 0xFF);
			s |= (short) ( (bytes[1] & 0xFF) << 8);
			return (Short) s;
		}
		case 7: // NON_PRIMITIVE_DOUBLE
			return (Double) Double.longBitsToDouble(readLong(in));
		case 8: // NON_PRIMITIVE_FLOAT
			return (Float) Float.intBitsToFloat(readInt(in));
		case 9: {// NON_PRIMITIVE_CHAR
			byte[] bytes = new byte[2];
			in.read(bytes);
			char s = (char) (bytes[0] & 0xFF);
			s |= (char) ( (bytes[1] & 0xFF) << 8);
			return (Character) s;
		}
		case 10: // STRING
			return readString(in);
		case 11:// BIG_INTEGER
		{
			byte[] bytes = new byte[readInt(in)];
			in.read(bytes);
			return new BigInteger(bytes);
		}
		case 12:// BIG_DECIMAL
		{
			String str = readString(in);
			return new BigDecimal(str);
		}
		case 13: // OBJECT
			// Class <?> cls;
			// {
			// String clsName = readString(in);
			// try {
			// cls = Class.forName(clsName);
			// } catch (ClassNotFoundException e) {
			// e.printStackTrace();
			// throw new NoClassDefFoundError(clsName);
			// }
			// }
			String clsName = readString(in);
			try {
				Class <?> cls = Class.forName(clsName);
				Object instance;
				if (creates.containsKey(cls)) {
					try {
						Method creator = creates.get(cls);
						instance = creator.invoke(null);
						if (instance == null) {
							throw new NullPointerException("creator returned null. creator: " + creator);
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new InternalError(e);
					}
				} else {
					try {
						Constructor <?> creator = cls.getDeclaredConstructor();
						try {
							boolean flag = creator.isAccessible();
							creator.setAccessible(true);
							instance = creator.newInstance();
							creator.setAccessible(flag);
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
							throw new InternalError(e);
						}
					} catch (NoSuchMethodException | SecurityException e) {
						throw new InternalError(e);
					}
				}
				overwriteObject2(in, instance, cls);
				return instance;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new NoClassDefFoundError(clsName);
			}
		default:
			throw new InternalError("illegal index: " + zw);
		}
	}
	
	private Object readArray(InputStream in) throws IOException {
		int deep = 1;
		Class <?> ultimateComp;
		int zw = assertRead(in, PRIMITIVE, OBJECT, ARRAY);
		switch (zw) {
		case 0:// PRIMITIVE
			zw = assertRead(in, PRIMITIVE_BOOLEAN, PRIMITIVE_BYTE, PRIMITIVE_CHAR, PRIMITIVE_DOUBLE, PRIMITIVE_FLOAT,
				PRIMITIVE_INT, PRIMITIVE_LONG, PRIMITIVE_SHORT);
			switch (zw) {
			case 0:// boolean
				ultimateComp = Boolean.TYPE;
				break;
			case 1:// byte
				ultimateComp = Byte.TYPE;
				break;
			case 2:// char
				ultimateComp = Character.TYPE;
				break;
			case 3:// double
				ultimateComp = Double.TYPE;
				break;
			case 4:// float
				ultimateComp = Float.TYPE;
				break;
			case 5:// int
				ultimateComp = Integer.TYPE;
				break;
			case 6:// long
				ultimateComp = Long.TYPE;
				break;
			case 7:// short
				ultimateComp = Short.TYPE;
				break;
			default:
				throw new InternalError("illegal index: " + zw);
			}
			break;
		case 1:// OBJECT
			try {
				String clsName = readString(in);
				ultimateComp = Class.forName(clsName);
			} catch (ClassNotFoundException e) {
				throw new InternalError(e);
			}
			break;
		case 2: {// ARRAY
			deep = readInt(in);
			zw = assertRead(in, PRIMITIVE, OBJECT);
			switch (zw) {
			case 0:// primitive
				zw = assertRead(in, PRIMITIVE_BOOLEAN, PRIMITIVE_BYTE, PRIMITIVE_CHAR, PRIMITIVE_DOUBLE,
					PRIMITIVE_FLOAT, PRIMITIVE_INT, PRIMITIVE_LONG, PRIMITIVE_SHORT);
				switch (zw) {
				case 0:// boolean
					ultimateComp = Boolean.TYPE;
					break;
				case 1:// byte
					ultimateComp = Byte.TYPE;
					break;
				case 2:// char
					ultimateComp = Character.TYPE;
					break;
				case 3:// double
					ultimateComp = Double.TYPE;
					break;
				case 4:// float
					ultimateComp = Float.TYPE;
					break;
				case 5:// int
					ultimateComp = Integer.TYPE;
					break;
				case 6:// long
					ultimateComp = Long.TYPE;
					break;
				case 7:// short
					ultimateComp = Short.TYPE;
					break;
				default:
					throw new InternalError("illegal index: " + zw);
				}
				break;
			case 1:// non primitive
				try {
					String clsName = readString(in);
					ultimateComp = Class.forName(clsName);
				} catch (ClassNotFoundException e) {
					throw new InternalError(e);
				}
				break;
			default:
				throw new InternalError("illegal index: " + zw);
			}
			break;
		}
		default:
			throw new InternalError("illegal index: " + zw);
		}
		int[] len = new int[deep];
		len[0] = readInt(in);
		Object arr = Array.newInstance(ultimateComp, len);
		for (int i = 0; i < len[0]; i ++ ) {
			Object read;
			if (deep > 1) {
				assertRead(in, ARRAY);
				read = readArray(in);
			} else if (ultimateComp.isPrimitive()) {
				assertRead(in, PRIMITIVE);
				read = readPrimitive(in);
			} else {
				// assertRead(in, OBJECT);
				read = readObject(in);
			}
			Array.set(arr, i, read);
		}
		return arr;
	}
	
	private Object readPrimitive(InputStream in) throws IOException {
		int zw = assertRead(in, PRIMITIVE_BOOLEAN, PRIMITIVE_BYTE, PRIMITIVE_CHAR, PRIMITIVE_DOUBLE, PRIMITIVE_FLOAT,
			PRIMITIVE_INT, PRIMITIVE_LONG, PRIMITIVE_SHORT);
		switch (zw) {
		case 0:// boolean
			return assertRead(in, 0, 1) == 1;
		case 1:// byte
			return (Byte) (byte) in.read();
		case 2: {// char
			byte[] bytes = new byte[2];
			in.read(bytes);
			char c = (char) (bytes[0] & 0xFF);
			c |= (char) ( (bytes[1] & 0xFF) << 8);
			return (Character) c;
		}
		case 3:// double
			return (Double) Double.longBitsToDouble(readLong(in));
		case 4:// float
			return (Float) Float.intBitsToFloat(readInt(in));
		case 5:// int
			return (Integer) readInt(in);
		case 6:// long
			return (Long) readLong(in);
		case 7:// short
			byte[] bytes = new byte[2];
			in.read(bytes);
			short c = (short) (bytes[0] & 0xFF);
			c |= (short) ( (bytes[1] & 0xFF) << 8);
			return (Short) c;
		default:
			throw new InternalError("illegal index: " + zw);
		}
	}
	
	public static int assertRead(InputStream in, int... possibleValues) throws IOException {
		int val = in.read();
		for (int i = 0; i < possibleValues.length; i ++ ) {
			if (val == possibleValues[i])
				return i;
		}
		// "asserted to read '" + Arrays.toString(values) + "' (" +
		// identyToStringOrElse(value, "no identy") + "), but got " + val
		StringBuilder errMsg = new StringBuilder("asserted to read [");
		if (possibleValues.length > 0) {
			errMsg.append(possibleValues[0]);
		}
		for (int i = 1; i < possibleValues.length; i ++ ) {
			errMsg.append(", ").append(possibleValues[i]);
		}
		errMsg.append("] in identys: [");
		if (possibleValues.length > 0) {
			errMsg.append(identyToStringOrElse(possibleValues[0], "no identy"));
		}
		for (int i = 1; i < possibleValues.length; i ++ ) {
			errMsg.append(", ").append(identyToStringOrElse(possibleValues[i], "no identy"));
		}
		errMsg.append("], but got: ").append(val).append(" identy: ").append(identyToStringOrElse(val, "no identy"));
		throw new AssertionError(errMsg.toString());
	}
	
	private static String readString(InputStream in) throws IOException {
		int len = readInt(in);
		byte[] bytes = new byte[len];
		in.read(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}
	
	private static long readLong(InputStream in) throws IOException {
		long val = ((long) readInt(in)) & 0xFFFFFFFFL;
		val |= ( ((long) readInt(in)) & 0xFFFFFFFF) << 32;
		return val;
	}
	
	private static int readInt(InputStream in) throws IOException {
		int ret = 0;
		byte[] bytes = new byte[4];
		in.read(bytes);
		ret = bytes[0] & 0xFF;
		ret |= (bytes[1] & 0xFF) << 8;
		ret |= (bytes[2] & 0xFF) << 16;
		ret |= (bytes[3] & 0xFF) << 24;
		return ret;
	}
	
}
