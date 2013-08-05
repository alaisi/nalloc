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

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.github.nalloc.Array;
import com.github.nalloc.MmapAllocator;

/**
 * Unit tests for {@link MmapArray}.
 */
public class MmapArrayTest {

	final MmapAllocator allocator = MmapAllocator.Factory.create(Val.class);

	@Test
	public void shouldCloneArray() throws IOException {
		File file = File.createTempFile(getClass().getSimpleName(), ".map");
		try(Array<Val> array = allocator.mmap(file, 5, Val.class)) {
			Array<Val> clone = array.clone();
			Val v1 = array.get(0);
			v1.val(123);
			Val v2 = clone.get(1);
			v2.val(456);

			assertEquals(array.address(), clone.address());
			assertEquals(array.size(), clone.size());
			assertTrue(v1.val() != v2.val());
			assertEquals(v1.val(), clone.get(0).val());
		}
		file.delete();
	}

}
