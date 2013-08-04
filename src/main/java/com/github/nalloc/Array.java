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
	 * @return struct at index.
	 */
	T get(long index);

	/**
	 * Sets memory to 0 at index.
	 */
	T clear(long index);

	/**
	 * Returns the amount of structs in this memory region.
	 */
	long size();

	/**
	 * @return Clone of this array pointer.
	 */
	@Override
	Array<T> clone();
}
