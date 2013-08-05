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
	 * Mmap is called with flags MAP_SHARED. The mapping length is nmemb times struct size.
	 * Calling this method allocates at least 3 objects from JVM heap. At least one object is
	 * eligible for GC. Java execution time is O(1).
	 *
	 * @param file File to mmap
	 * @param nmemb Amount of structs the mapping has space for
	 * @param structType Class annotated with &#064;Struct
	 */
	<T> Array<T> mmap(final File file, final long nmemb, final Class<T> structType) throws IOException;

	/**
	 * mmap() creates a new mapping in the virtual address space of the calling process.
	 *
	 * See <a href="http://pubs.opengroup.org/onlinepubs/009695399/functions/mmap.html">mmap</a>.
	 *
	 * Mmap is called with flags MAP_PRIVATE | MAP_ANONYMOUS. The length of the array is calculated from
	 * buffer capacity and struct size. The buffer must be created with {@link ByteBuffer#allocateDirect(int)}.
	 * Calling this method always allocates 2 objects (total 64 bytes) from JVM heap.
	 *
	 * @param buffer Byffer to mmap, must be direct and in native order
	 * @param structType Class annotated with &#064;Struct
	 */
	<T> Array<T> mmap(final ByteBuffer buffer, final Class<T> structType);

	/**
	 * mmap() creates a new mapping in the virtual address space of the calling process.
	 *
	 * See <a href="http://pubs.opengroup.org/onlinepubs/009695399/functions/mmap.html">mmap</a>.
	 *
	 * Mmap is called with flags MAP_PRIVATE | MAP_ANONYMOUS. The mapping length is nmemb times struct size.
	 * Calling this method allocates at least 3 objects from JVM heap. Java execution time is O(1).
	 *
	 * @param nmemb Amount of structs the mapping has space for
	 * structType Class annotated with &#064;Struct
	 */
	<T> Array<T> mmap(final long nmemb, final Class<T> structType);

	/**
	 * Casts the {@link Array} of structs to {@link ByteBuffer}.
	 *
	 * Calling this method never allocates from JVM heap.
	 *
	 * @param structs Mmapped array
	 */
	ByteBuffer toBytes(final Array<?> structs);

	public class Factory {
		/**
		 * Creates a new {@link MmapAllocator} that can allocate structs listed in structTypes.
		 * This is an expensive operation and callers should store the returned allocator.
		 *
		 * @param structTypes Struct classes that the returned allocator can instantiate
		 * @return New allocator instance
		 */
		public static MmapAllocator create(final Class<?>... structTypes) {
			return new DirectBufferMmapAllocator(structTypes);
		}
	}
}
