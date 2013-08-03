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

import com.github.nalloc.impl.UnsafeNativeHeapAllocator;


/**
 * Memory allocator that allocates structs from native ("C") heap.
 * 
 * @author Antti Laisi
 */
public interface NativeHeapAllocator {

	/**
	 * The malloc() function allocates size bytes and returns a pointer to the allocated
	 * memory. The memory is not initialized.
	 * 
	 * See <a href="http://pubs.opengroup.org/onlinepubs/009695399/functions/malloc.html">malloc</a>.
	 * 
	 * Size is determined by struct size. Calling this method allocates always 2 objects (total 32 bytes)
	 * from JVM heap.
	 * 
	 * @param structType Class annotated with &#064;Struct
	 * @return Pointer to struct instance
	 */
	<T> Pointer<T> malloc(Class<T> structType);

	/**
	 * The calloc() function allocates memory for an array of nmemb elements of size bytes 
	 * each and returns a pointer to the allocated memory. The memory is set to zero.
	 * 
	 * See <a href="http://pubs.opengroup.org/onlinepubs/009695399/functions/calloc.html">calloc</a>.
	 * 
	 * Calling this method always allocates 2 objects (total 56 bytes) from JVM heap. Java execution
	 * time is O(1).
	 * 
	 * @param nmemb Size of array
	 * @param structType Class annotated with &#064;Struct
	 * @return Pointer to struct array
	 */
	<T> Array<T> calloc(long nmemb, Class<T> structType);

	/**
	 * The realloc() function changes the size of the memory block pointed to by ptr to size bytes.
	 * The contents will be unchanged in the range from the start of  the  region up to  the 
	 * minimum of the old and new sizes.  If the new size is larger than the old size, the added memory
	 * will not be initialized.
	 * 
	 * See <a href="http://pubs.opengroup.org/onlinepubs/009695399/functions/realloc.html">realloc</a>.
	 * 
	 * Calling this method never allocates from JVM heap. Java execution time is O(1).
	 * 
	 * @param pointer Pointer to struct array
	 * @param nmemb New size of array
	 * @return Pointer to struct array of size nmemb
	 */
	<T> Array<T> realloc(Array<T> pointer, long nmemb);

	public class Factory {
		/**
		 * Creates a new {@link NativeHeapAllocator} that can allocate structs listed in structTypes.
		 * This is an expensive operation and callers should store the returned allocator.
		 * 
		 * @param structTypes Struct classes that the returned allocator can instantiate
		 * @return New allocator instance
		 */
		public static NativeHeapAllocator create(Class<?>... structTypes) {
			return new UnsafeNativeHeapAllocator(structTypes);
		}
	}
}
