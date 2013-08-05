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

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import java.util.Map;

import com.github.nalloc.Array;
import com.github.nalloc.MmapAllocator;

/**
 * {@link MmapAllocator} implementation using direct {@link ByteBuffer}s.
 *
 * @author Antti Laisi
 */
public class DirectBufferMmapAllocator implements MmapAllocator {

	private final Map<Class<?>, Class<? extends NativeStruct>> implementations = new HashMap<>();

	public DirectBufferMmapAllocator(final Class<?>... structTypes) {
		StructClassGenerator generator = new StructClassGenerator(structTypes);
		for(Class<?> struct : structTypes) {
			implementations.put(struct, generator.generate(struct));
		}
	}

	@Override
	public <T> Array<T> mmap(final File file, final long nmemb, final Class<T> structType) throws IOException {
		FileChannel channel = FileChannel.open(file.toPath(), READ, WRITE, CREATE);
		NativeStruct struct = NativeStruct.create(implementations.get(structType));
		MappedByteBuffer buffer = channel.map(MapMode.READ_WRITE, 0, nmemb * struct.getSize());
		buffer.order(ByteOrder.nativeOrder());
		channel.close();
		return new MmapArray<T>(buffer, nmemb, struct);
	}

	@Override
	public <T> Array<T> cast(final ByteBuffer buffer, final Class<T> structType) {
		if(!buffer.isDirect() || !ByteOrder.nativeOrder().equals(buffer.order())) {
			throw new IllegalArgumentException("Only direct buffers in native byte order can be mapped");
		}
		NativeStruct struct = NativeStruct.create(implementations.get(structType));
		return new MmapArray<T>((MappedByteBuffer) (buffer), buffer.capacity() / struct.getSize(), struct);
	}

	@Override
	public ByteBuffer cast(final Array<?> structs) {
		MmapArray<?> array = (MmapArray<?>) structs;
		return array.buffer;
	}

}
