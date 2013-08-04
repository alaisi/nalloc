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
 * Pointer to a memory address.
 *
 * @author Antti Laisi
 */
public interface Pointer<T> extends AutoCloseable {

	/**
	 * @return Struct referenced by this pointer.
	 */
	T deref();

	/**
	 * @return Memory address pointed to.
	 */
	long address();

	/**
	 * Sets memory address.
	 *
	 * @param New address
	 */
	void address(long address);

	/**
	 * Frees memory that is pointed to by this pointer.
	 */
	void free();

	/**
	 * @return Clone of this pointer
	 */
	Pointer<T> clone();

	/**
	 * AutoCloseable support, calls free().
	 */
	@Override
	void close();

}
