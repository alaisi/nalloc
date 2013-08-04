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

import com.github.nalloc.Array;

/**
 * {@link Array} containing fixed-sized structs.
 *
 * @author Antti Laisi
 */
@SuppressWarnings("restriction")
public class HeapArray<T> implements Array<T> {

	protected final NativeStruct struct;
	private final long msize;

	protected long size;
	private long address;

	/**
	 * @param address Pointer address
	 * @param size Size of array
	 * @param struct Struct instance
	 */
	public HeapArray(final long address, final long size, final NativeStruct struct) {
		this.address = address;
		this.size = size;
		this.struct = struct;
		this.msize = struct.getSize();
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(final long index) {
		struct.address = address + index * msize;
		return (T) struct;
	}

	@Override
	public T clear(final long index) {
		UNSAFE.setMemory(address + index * msize, msize, (byte) 0);
		return get(index);
	}

	@Override
	public long size() {
		return size;
	}

	@Override
	public T deref() {
		return get(0);
	}

	@Override
	public long address() {
		return address;
	}

	@Override
	public void address(final long address) {
		this.address = address;
	}

	@Override
	public void free() {
		UNSAFE.freeMemory(address);
	}

	@Override
	public String toString() {
		return String.format("0x%X", address);
	}

	@Override
	public Array<T> clone() {
		return new HeapArray<>(address, size, struct.clone());
	}

	@Override
	public void close() {
		free();
	}
}
