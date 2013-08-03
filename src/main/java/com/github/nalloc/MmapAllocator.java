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

package com.github.nalloc;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.github.nalloc.impl.DirectBufferMmapAllocator;

/**
 * Memory allocator that allocates objects from memory mapped files.
 *
 * @author Antti Laisi
 */
public interface MmapAllocator {

	/**
	 * mmap() creates a new mapping in the virtual address space of the calling process.
	 * 
	 * See <a href="http://pubs.opengroup.org/onlinepubs/009695399/functions/mmap.html">mmap</a>.
	 * 
	 * The mapping length is nmemb times struct size. Calling this method allocates at least 3 objects 
	 * from JVM heap. At least one object is eligible for GC. Java execution time is O(1).
	 */
	<T> Array<T> mmap(File file, long nmemb, Class<T> structType) throws IOException;

	/**
	 * Casts the buffer to an {@link Array} of type structType. The length of the array is calculated from
	 * buffer capacity and struct size. The buffer must be created with {@link ByteBuffer#allocateDirect(int)}.
	 * 
	 * Calling this method always allocates 2 objects (total 64 bytes) from JVM heap.
	 */
	<T> Array<T> cast(ByteBuffer buffer, Class<T> structType);

	/**
	 * Casts the {@link Array} of structs to {@link ByteBuffer}.
	 * 
	 * Calling this method never allocates from JVM heap.
	 */
	ByteBuffer cast(Array<?> structs);

	public class Factory {
		/**
		 * Creates a new {@link MmapAllocator} that can allocate structs listed in structTypes.
		 * This is an expensive operation and callers should store the returned allocator.
		 */
		public static MmapAllocator create(Class<?>... structTypes) {
			return new DirectBufferMmapAllocator(structTypes);
		}
	}
}
