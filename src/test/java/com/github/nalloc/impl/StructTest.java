/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nalloc.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.github.nalloc.Array;
import com.github.nalloc.NativeHeapAllocator;
import com.github.nalloc.Pointer;
import com.github.nalloc.Struct;
import com.github.nalloc.Struct.Field;
import com.github.nalloc.Struct.Type;

/**
 * Unit tests for {@link Struct}.
 *
 * @author Antti Laisi
 */
public class StructTest {

	@Test
	public void shouldGetAndSetSimpleTypes() {
		try(Pointer<SimpleTypes> ptr = struct(SimpleTypes.class)) {
			SimpleTypes simple = ptr.deref();

			simple.lnumber(Long.MAX_VALUE);
			simple.inumber(Integer.MIN_VALUE);
			simple.c('\u20AC');
			simple.b(Byte.MAX_VALUE);

			assertEquals(Long.MAX_VALUE, simple.lnumber());
			assertEquals(Integer.MIN_VALUE, simple.inumber());
			assertEquals('\u20AC', simple.c());
			assertEquals(Byte.MAX_VALUE, simple.b());
			assertEquals(15, simple.getSize());
		}
	}

	@Test
	public void shouldGetAndSetArrayTypes() {
		try(Pointer<ArrayTypes> ptr = struct(ArrayTypes.class)) {
			ArrayTypes arrays = ptr.deref();

			arrays.barray(new byte[]{'1','2'});
			arrays.carray(new char[]{'1','2','3'});
			arrays.iarray(new int[]{1,2,3,4});
			arrays.larray(new long[]{1,2,3,4,5});
			arrays.string("123456");

			assertTrue(Arrays.equals(new byte[]{'1','2'}, arrays.barray()));
			assertTrue(Arrays.equals(new char[]{'1','2','3'}, arrays.carray()));
			assertTrue(Arrays.equals(new int[]{1,2,3,4}, arrays.iarray()));
			assertTrue(Arrays.equals(new long[]{1,2,3,4,5}, arrays.larray()));
			assertEquals("123456", arrays.string());
			assertEquals(76, arrays.getSize());
		}
	}

	@Test
	public void shouldGetAndSetCCompatible() {
		try(Pointer<CCompatible> ptr = struct(CCompatible.class)) {
			CCompatible compatible = ptr.deref();

			compatible.l(Long.MIN_VALUE);
			compatible.c('$');
			compatible.carray(new char[]{'x','a','_'});
			compatible.string("123");
			compatible.string2("123-too-long");

			assertEquals(Long.MIN_VALUE, compatible.l());
			assertEquals('$', compatible.c());
			assertTrue(Arrays.equals(new char[]{'x','a'}, compatible.carray()));
			assertEquals("123", compatible.string());
			assertEquals("123", compatible.string2());
			assertEquals(24, compatible.getSize());
		}
	}

	@Test
	public void shouldGetNestedStructs() {
		try(Pointer<WithNestedStruct> ptr = struct(WithNestedStruct.class)) {
			WithNestedStruct nested = ptr.deref();
			nested.compatible().string("12");

			assertEquals("12", nested.compatible().string());
			assertEquals(nested.compatible().getSize(), nested.getSize());
		}
	}

	@Test
	public void shouldGetNestedStructArrays() {
		try(Pointer<WithNestedArray> ptr = struct(WithNestedArray.class)) {
			WithNestedArray array = ptr.deref();
			array.array().get(1).compatible().string("AB");

			assertEquals("AB", array.array().get(1).compatible().string());
			assertEquals(2 * array.array().get(0).getSize(), array.getSize());
		}
	}

	<T> Pointer<T> struct(final Class<T> structType) {
		return NativeHeapAllocator.Factory.create(structType).malloc(structType);
	}

	// types

	@Struct({
		@Field(name="lnumber", type=Type.LONG),
		@Field(name="inumber", type=Type.INT),
		@Field(name="c", type=Type.CHAR),
		@Field(name="b", type=Type.BYTE) })
	static interface SimpleTypes {
		long lnumber();
		void lnumber(final long value);
		int inumber();
		void inumber(final int value);
		char c();
		void c(final char value);
		byte b();
		void b(final byte value);
		long getSize();
	}

	@Struct({
		@Field(name="barray", type=Type.BYTE, len=2),
		@Field(name="carray", type=Type.CHAR, len=3),
		@Field(name="iarray", type=Type.INT, len=4),
		@Field(name="larray", type=Type.LONG, len=5),
		@Field(name="string", type=Type.STRING, len=6) })
	static interface ArrayTypes {
		byte[] barray();
		void barray(final byte[] value);
		char[] carray();
		void carray(final char[] value);
		int[] iarray();
		void iarray(final int[] value);
		long[] larray();
		void larray(final long[] value);
		String string();
		void string(final String value);
		long getSize();
	}

	@Struct(c=true, pad=8, value={
		@Field(name="l", type=Type.LONG),
		@Field(name="c", type=Type.CHAR),
		@Field(name="carray", type=Type.CHAR, len=2),
		@Field(name="string", type=Type.STRING, len=4),
		@Field(name="string2", type=Type.STRING, len=4)})
	static interface CCompatible {
		long l();
		void l(final long value);
		char c();
		void c(final char value);
		char[] carray();
		void carray(final char[] value);
		String string();
		void string(final String value);
		String string2();
		void string2(final String value);
		long getSize();
	}

	@Struct(c=true, pad=8, value={
		@Field(name="compatible", type=Type.STRUCT, struct=CCompatible.class) })
	static interface WithNestedStruct {
		CCompatible compatible();
		long getSize();
	}

	@Struct(c=true, pad=8, value={
		@Field(name="array", type=Type.STRUCT, struct=WithNestedStruct.class, len=2) })
	static interface WithNestedArray {
		Array<WithNestedStruct> array();
		long getSize();
	}

}
