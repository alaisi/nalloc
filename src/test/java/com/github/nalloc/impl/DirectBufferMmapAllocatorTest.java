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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.After;
import org.junit.Test;

import com.github.nalloc.Array;
import com.github.nalloc.MmapAllocator;
import com.github.nalloc.Struct;
import com.github.nalloc.Struct.Field;
import com.github.nalloc.Struct.Type;

/**
 * Unit tests for {@link DirectBufferMmapAllocator}.
 *
 * @author Antti Laisi
 */
public class DirectBufferMmapAllocatorTest {

	final MmapAllocator allocator = MmapAllocator.Factory.create(MyMappedStruct.class);
	File file;

	@Test
	public void shouldMemoryMapFile() throws IOException {
		try(Array<MyMappedStruct> array = allocator.mmap(file(), 10, MyMappedStruct.class)) {
			MyMappedStruct struct = array.get(0);
			struct.id(1);

			assertEquals(1, struct.id());
			assertTrue(file.exists());
			assertEquals(10 * struct.getSize(), file.length());
		}
	}

	@Test
	public void shouldAccessStructAsByteBuffer() throws IOException {
		try(Array<MyMappedStruct> array = allocator.mmap(file(), 1, MyMappedStruct.class)) {
			MyMappedStruct struct = array.get(0);
			ByteBuffer buffer = allocator.cast(array);
			assertNotNull(buffer);

			buffer.putLong(0, 77);
			assertEquals(77, struct.id());
		}
	}

	@Test
	public void shouldAccessByteBufferAsStruct() throws IOException {
		ByteBuffer buffer = ByteBuffer.allocateDirect(128);
		buffer.order(ByteOrder.nativeOrder());
		buffer.putLong(123);

		Array<MyMappedStruct> array = allocator.cast(buffer, MyMappedStruct.class);
		assertEquals(123, array.deref().id());
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldRejectCastingIndirectBuffer() throws IOException {
		allocator.cast(ByteBuffer.allocate(8), MyMappedStruct.class);
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldRejectCastingBufferInWrongOrder() throws IOException {
		allocator.cast(ByteBuffer.allocateDirect(8), MyMappedStruct.class);
	}

	@After
	public void cleanup() {
		if(file != null) {
			file.delete();
		}
	}

	File file() throws IOException {
		return file = File.createTempFile(getClass().getSimpleName(), ".map");
	}

	@Struct({
		@Field(name="id", type=Type.LONG) })
	static interface MyMappedStruct {
		long id();
		void id(final long id);
		long getSize();
	}
}
