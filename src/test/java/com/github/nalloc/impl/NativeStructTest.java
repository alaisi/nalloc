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

import org.junit.Test;

import com.github.nalloc.Array;
import com.github.nalloc.NativeHeapAllocator;
import com.github.nalloc.Pointer;
import com.github.nalloc.Struct;
import com.github.nalloc.Struct.Field;
import com.github.nalloc.Struct.Type;

/**
 * Unit tests for {@link NativeStruct}.
 *
 * @author Antti Laisi
 */
public class NativeStructTest {

	final NativeHeapAllocator allocator = NativeHeapAllocator.Factory.create(NestedStruct.class);

	@Test
	public void shouldCloneNestedFields() {
		try(Pointer<NestedStruct> ptr = allocator.malloc(NestedStruct.class)) {
			Pointer<NestedStruct> clone = ptr.clone();
			ptr.deref().nested().val(1);
			ptr.deref().array().get(1).val(2);

			assertTrue(ptr.deref() != clone.deref());
			assertEquals(ptr.deref().nested().val(), clone.deref().nested().val());
			assertEquals(ptr.deref().array().get(1).val(), clone.deref().array().get(1).val());
		}
	}

	@Struct({
		@Field(name="nested", type=Type.STRUCT, struct=Val.class),
		@Field(name="array", type=Type.STRUCT, struct=Val.class, len=2) })
	static interface NestedStruct {
		Val nested();
		Array<Val> array();
	}
}
