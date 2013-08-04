package com.github.nalloc.impl;

import static sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET;
import static sun.misc.Unsafe.ARRAY_CHAR_BASE_OFFSET;
import static sun.misc.Unsafe.ARRAY_INT_BASE_OFFSET;
import static sun.misc.Unsafe.ARRAY_LONG_BASE_OFFSET;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * Utility methods for direct memory access.
 */
@SuppressWarnings("restriction")
public enum PointerArithmetics {

	INSTANCE;

	static final Unsafe UNSAFE;
	private static final long STRING_VALUE_OFFSET;

	static {
		try {
			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			UNSAFE = (Unsafe) theUnsafe.get(null);
			STRING_VALUE_OFFSET = UNSAFE.objectFieldOffset(String.class.getDeclaredField("value"))
					+ ARRAY_CHAR_BASE_OFFSET
					+ 12; // TODO: is this constant?
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public final long getLong(final long address) {
		return UNSAFE.getLong(address);
	}
	public final int getInt(final long address) {
		return UNSAFE.getInt(address);
	}
	public final char getChar(final long address) {
		return UNSAFE.getChar(address);
	}
	public final byte getByte(final long address) {
		return UNSAFE.getByte(address);
	}

	public final byte[] getBytes(final long address, final long len) {
		byte[] bytes = new byte[(int) len];
		UNSAFE.copyMemory(null, address, bytes, ARRAY_BYTE_BASE_OFFSET, len);
		return bytes;
	}
	public final char[] getChars(final long address, final long len) {
		char[] chars = new char[(int) len];
		UNSAFE.copyMemory(null, address, chars, ARRAY_CHAR_BASE_OFFSET, len << 1);
		return chars;
	}
	public final int[] getInts(final long address, final long len) {
		int[] ints = new int[(int) len];
		UNSAFE.copyMemory(null, address, ints, ARRAY_INT_BASE_OFFSET, len << 2);
		return ints;
	}
	public final long[] getLongs(final long address, final long len) {
		long[] longs = new long[(int) len];
		UNSAFE.copyMemory(null, address, longs, ARRAY_LONG_BASE_OFFSET, len << 3);
		return longs;
	}
	public final String getString(final long address, final long len) {
		char[] chars = new char[(int) len];
		UNSAFE.copyMemory(null, address, chars, ARRAY_CHAR_BASE_OFFSET, len << 1);
		return new String(chars);
	}

	public final void setByte(final long address, final byte value) {
		UNSAFE.putByte(address, value);
	}
	public final void setChar(final long address, final char value) {
		UNSAFE.putChar(address, value);
	}
	public final void setInt(final long address, final int value) {
		UNSAFE.putInt(address, value);
	}
	public final void setLong(final long address, final long value) {
		UNSAFE.putLong(address, value);
	}

	public final void setBytes(final long address, final byte[] bytes, final long len) {
		UNSAFE.copyMemory(bytes, ARRAY_BYTE_BASE_OFFSET, null, address, len);
	}
	public final void setChars(final long address, final char[] chars, final long len) {
		UNSAFE.copyMemory(chars, ARRAY_CHAR_BASE_OFFSET, null, address, len << 1);
	}
	public final void setInts(final long address, final int[] ints, final long len) {
		UNSAFE.copyMemory(ints, ARRAY_INT_BASE_OFFSET, null, address, len << 2);
	}
	public final void setLongs(final long address, final long[] longs, final long len) {
		UNSAFE.copyMemory(longs, ARRAY_LONG_BASE_OFFSET, null, address, len << 3);
	}
	public final void setString(final long address, final String string, final long len) {
		UNSAFE.copyMemory(string, STRING_VALUE_OFFSET, null, address, len << 1);
	}

	// C

	public final char getAnsiCChar(final long address) {
		return (char) UNSAFE.getByte(address);
	}
	public final char[] getAnsiCChars(final long address, final long len) {
		char[] chars = new char[(int) len];
		for(int i = 0; i < len; i++) {
			chars[i] = (char) UNSAFE.getByte(address + i);
		}
		return chars;
	}
	public final String getAnsiCString(final long address, final long len) {
		char[] chars = new char[(int) len];
		int i = 0;
		for(; i < len-1; i++) {
			byte b = UNSAFE.getByte(address + i);
			if(b == 0) {
				break;
			}
			chars[i] = (char) b;
		}
		return new String(chars, 0, i);
	}

	public final void setAnsiCChar(final long address, final char value) {
		UNSAFE.putByte(address, (byte) value);
	}
	public final void setAnsiCChars(final long address, final char[] chars, final long len) {
		for(int i = 0; i < len && i < chars.length; i++) {
			UNSAFE.putByte(address + i, (byte) chars[i]);
		}
	}
	public final void setAnsiCString(final long address, final String string, final long len) {
		int i = 0;
		for(; i < len-1 && i < string.length(); i++) {
			UNSAFE.putByte(address + i, (byte) string.charAt(i));
		}
		UNSAFE.putByte(address + i, (byte) 0);
	}

}
