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
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.github.nalloc.Array;
import com.github.nalloc.NativeHeapAllocator;
import com.github.nalloc.Pointer;
import com.github.nalloc.Struct;
import com.github.nalloc.Struct.Field;
import com.github.nalloc.Struct.Type;

/**
 * Unit tests for {@link UnsafeNativeHeapAllocator}.
 *
 * @author Antti Laisi
 */
public class UnsafeNativeHeapAllocatorTest {

	final NativeHeapAllocator allocator = NativeHeapAllocator.Factory.create(MyStruct.class);

	@Test
	public void shouldMallocSingleStruct() {
		try(Pointer<MyStruct> ptr = allocator.malloc(MyStruct.class)) {
			MyStruct my = ptr.deref();
			assertNotNull(my);
		}
	}

	@Test
	public void shouldCallocArray() {
		try(Array<MyStruct> array = allocator.calloc(3, MyStruct.class)) {
			assertEquals(array.deref(), array.get(0));
			assertEquals(3, array.size());
			array.get(2).name("name");
			assertEquals("name", array.get(2).name());
		}
	}

	@Test
	public void shouldResizeArrayOnRealloc() {
		try(Array<MyStruct> array = allocator.calloc(1, MyStruct.class)) {
			assertEquals(1, array.size());
			allocator.realloc(array, 3);
			assertEquals(3, array.size());
			array.get(2).name("name");
			assertEquals("name", array.get(2).name());
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldFailCallocZero() {
		allocator.calloc(0, MyStruct.class);
	}

	@Struct(c=true, value={
		@Field(name="name",type=Type.STRING,len=10) })
	static interface MyStruct {
		String name();
		void name(String value);
	}
}
