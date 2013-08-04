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

import java.util.HashMap;
import java.util.Map;

import com.github.nalloc.Array;
import com.github.nalloc.NativeHeapAllocator;
import com.github.nalloc.Pointer;

/**
 * {@link NativeHeapAllocator} implementation using sun.misc.Unsafe.
 *
 * @author Antti Laisi
 */
@SuppressWarnings("restriction")
public class UnsafeNativeHeapAllocator implements NativeHeapAllocator {

	private final Map<Class<?>, Class<? extends NativeStruct>> implementations = new HashMap<>();

	public UnsafeNativeHeapAllocator(final Class<?>... structTypes) {
		StructClassGenerator generator = new StructClassGenerator(structTypes);
		for(Class<?> struct : structTypes) {
			implementations.put(struct, generator.generate(struct));
		}
	}

	@Override
	public <T> Pointer<T> malloc(final Class<T> structType) {
		NativeStruct struct = NativeStruct.create(implementations.get(structType));
		struct.address = UNSAFE.allocateMemory(struct.getSize());
		return new HeapPointer<T>(struct);
	}

	@Override
	public <T> Array<T> calloc(final long nmemb, final Class<T> structType) {
		NativeStruct struct = NativeStruct.create(implementations.get(structType));
		long address = UNSAFE.allocateMemory(nmemb * struct.getSize());
		UNSAFE.setMemory(address, nmemb * struct.getSize(), (byte) 0);
		return new HeapArray<T>(address, nmemb, struct);
	}

	@Override
	public <T> Array<T> realloc(final Array<T> pointer, final long nmemb) {
		NativeStruct struct = (NativeStruct) pointer.deref();
		struct.address = UNSAFE.reallocateMemory(struct.address, nmemb * struct.getSize());
		return pointer;
	}

}
