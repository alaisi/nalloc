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

import java.lang.reflect.Field;

import com.github.nalloc.Pointer;

/**
 * Base class for generated structs. Instances don't hold state, only a memory address.
 *
 * @author Antti Laisi
 */
public abstract class NativeStruct {

	protected static final PointerArithmetics POINTERS = PointerArithmetics.INSTANCE;

	// public only for generated classes
	public static NativeStruct create(Class<? extends NativeStruct> structClass) {
		try {
			return structClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	protected long address;

	/**
	 * @return Length of struct in bytes
	 */
	protected abstract long getSize();

	/**
	 * Sets memory address of the struct.
	 *
	 * @param address New address
	 */
	public final void setAddress(final long address) {
		this.address = address;
	}

	@Override
	public final String toString() {
		return String.format("0x%X", address);
	}

	/**
	 * Creates a deep copy of the instance.
	 */
	@Override
	protected NativeStruct clone() {
		NativeStruct clone = NativeStruct.create(getClass());
		clone.address = this.address;
		for(Field field : getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				Object o = field.get(this);
				// nested structs
				if(o instanceof NativeStruct) {
					field.set(clone, ((NativeStruct) o).clone());
				} else if (o instanceof Pointer<?>) {
					field.set(clone, ((Pointer<?>) o).clone());
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return clone;
	}

}
