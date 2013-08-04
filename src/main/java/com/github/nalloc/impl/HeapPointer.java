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

import static com.github.nalloc.impl.PointerArithmetics.UNSAFE;

import com.github.nalloc.Pointer;

/**
 * {@link Pointer} to a single struct.
 *
 * @author Antti Laisi
 */
@SuppressWarnings("restriction")
final class HeapPointer<T> implements Pointer<T> {

	private final NativeStruct struct;

	HeapPointer(final NativeStruct struct) {
		this.struct = struct;
	}

	@Override
	@SuppressWarnings("unchecked")
	public final T deref() {
		return (T) struct;
	}

	@Override
	public long address() {
		return struct.address;
	}

	@Override
	public void address(final long address) {
		struct.address = address;
	}

	@Override
	public final void free() {
		UNSAFE.freeMemory(struct.address);
	}

	@Override
	public final String toString() {
		return struct.toString();
	}

	@Override
	public final Pointer<T> clone() {
		return new HeapPointer<>(struct.clone());
	}

	@Override
	public void close() {
		free();
	}
}
