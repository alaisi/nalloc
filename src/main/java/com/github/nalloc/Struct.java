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

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks an interface as struct that can be allocated off-heap. Struct annotation
 * contains list of fields in the struct. Fields have a type and length. Fields of
 * length 2 or more are represented by arrays. Nested structs are represented as
 * struct interfaces an nested struct arrays as Array.
 *
 * @author Antti Laisi
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Struct {

	/**
	 * List of fields in the struct.
	 * */
	Field[] value();

	/**
	 * C/C++ compatibility.
	 */
	boolean c() default false;

	/**
	 * Pad struct size to multiple of.
	 */
	byte pad() default 1;

	/**
	 * Single field in a struct.
	 */
	@Retention(RUNTIME)
	@Target(ANNOTATION_TYPE)
	public @interface Field {
		/**
		 * Name of the field.
		 * */
		String name();

		/**
		 * Storage type.
		 */
		Type type();

		/**
		 * Length of the field.
		 */
		long len() default 1;

		/**
		 * Nested struct class if type is STRUCT.
		 */
		Class<?> struct() default void.class;
	}

	/**
	 * Type of struct field.
	 */
	public enum Type {
		BYTE,
		CHAR,
		INT,
		LONG,
		STRING,
		STRUCT;
	}
}
