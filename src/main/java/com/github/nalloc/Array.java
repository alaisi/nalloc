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

/**
 * Pointer to a continuous memory region.
 *
 * @author Antti Laisi
 */
public interface Array<T> extends Pointer<T> {

	/**
	 * Accessor for array at index. To prevent JVM heap allocations the returned struct is always the
	 * same object for an array instance and only the struct pointer address is modified by this method.
	 * Use {@link Array#clone()} if storing the returned object is required.
	 *
	 * @return Struct addressed to index.
	 */
	T get(final long index);

	/**
	 * Sets memory to 0 at index.
	 */
	T clear(final long index);

	/**
	 * Returns the amount of structs in this memory region.
	 */
	long size();

	/**
	 * Returns a shallow clone of an array. Cloning is a cheap operation, only the pointer is cloned and
	 * not the array content.
	 *
	 * @return Clone of this array pointer.
	 */
	@Override
	Array<T> clone();
}
