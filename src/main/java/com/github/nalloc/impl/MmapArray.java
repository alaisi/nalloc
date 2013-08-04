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

import java.nio.MappedByteBuffer;

import com.github.nalloc.Array;

import sun.nio.ch.DirectBuffer;

/**
 * {@link Array} containing fixed-sized structs in a memory mapped buffer.
 *
 * @author Antti Laisi
 */
@SuppressWarnings("restriction")
final class MmapArray<T> extends HeapArray<T> {

	final MappedByteBuffer buffer;

	MmapArray(final MappedByteBuffer buffer, final long size, final NativeStruct struct) {
		super(((DirectBuffer) buffer).address(), size, struct);
		this.buffer = buffer;
	}

	@Override
	public void free() {
		((DirectBuffer) buffer).cleaner().clean();
	}

	@Override
	public Array<T> clone() {
		return new MmapArray<>(buffer, size, struct.clone());
	}
}
