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

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import com.github.nalloc.Struct;
import com.github.nalloc.Struct.Field;
import com.github.nalloc.Struct.Type;

/**
 * Generates implementing classes for struct interfaces.
 * 
 * @author Antti Laisi
 */
final class StructClassGenerator {

	private final ClassPool classes;

	/**
	 * Creates a new generator.
	 *
	 * @param definitions Struct interfaces
	 */
	StructClassGenerator(final Class<?>... definitions) {
		ClassPool.doPruning = true;
		classes = new ClassPool(false);
		classes.appendClassPath(new ClassClassPath(definitions[0]));
	}

	@SuppressWarnings("unchecked")
	final Class<? extends NativeStruct> generate(final Class<?> definitionClass) {
		String className = definitionClass.getName() + "$GenStruct";

		try {
			return (Class<? extends NativeStruct>) Class.forName(className, false, definitionClass.getClassLoader());
		} catch(ClassNotFoundException e) { 
			/* proceed with generating implementing class */
		}

		Struct struct = definitionClass.getAnnotation(Struct.class);
		try {
			return generate(className, definitionClass, struct);
		} catch (NotFoundException | CannotCompileException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private Class<? extends NativeStruct> generate(final String className, final Class<?> definitionClass, final Struct struct) 
			throws NotFoundException, CannotCompileException {

		CtClass definition = classes.get(definitionClass.getName());
		definition.freeze();

		CtClass generated = classes.makeClass(className, 
				classes.get(NativeStruct.class.getName()));
		generated.addInterface(definition);

		long offset = 0;
		for(Field field : struct.value()) {
			generateFieldAccessors(generated, struct, field, offset, definition);
			offset += byteLength(struct, field);
		}

		generateGetSize(generated, struct, offset);

		return generated.toClass(definitionClass.getClassLoader(), definitionClass.getProtectionDomain());
	}

	/*
	 * Creates getSize() method that returns byte length of struct.
	 * 
	 *   public class Example$GenStruct {
	 *     public final long getSize() { 
	 *       return 123L;
	 *     }
	 *   }
	 */
	private void generateGetSize(final CtClass generated, final Struct struct, final long finalOffset) 
			throws CannotCompileException {

		long size = struct.pad() == 1 ? finalOffset : finalOffset + (struct.pad() - finalOffset % struct.pad());
		generated.addMethod(CtNewMethod.make(String.format(
				"public final long getSize(){ return %dL; }", size), 
				generated));
	}

	private void generateFieldAccessors(final CtClass generated, final Struct struct, final Field field, final long offset,
			final CtClass definition) throws CannotCompileException, NotFoundException {

		if(field.type() == Type.STRUCT && field.len() == 1) {
			generateStructAccessor(generated, struct, field, offset);
		} else if(field.type() == Type.STRUCT) {
			generateStructArrayAccessor(generated, struct, field, offset);
		} else {
			generateSimpleFieldAccessor(generated, struct, field, offset, definition);
		}
	}

	/*
	 * Creates struct field and getter for a single nested struct.
	 *
	 *   public class Example$GenStruct {
	 *     Nested$GenStruct _message;
	 *     public final Nested$GenStruct message() {
	 *       _message.setAddress(super.address + 123L);
	 *       return _message;
	 *     }
	 *   }
	 */
	private void generateStructAccessor(final CtClass generated, final Struct struct, final Field field, final long offset)
			throws CannotCompileException, NotFoundException {

		Class<?> nestedStruct = generate(field.struct());

		generated.addField(new CtField(classes.get(nestedStruct.getName()), "_" + field.name(), generated), 
				"new " + nestedStruct.getName() + "();");

		generated.addMethod(CtNewMethod.make(String.format(
				"public final %s %s(){ _%s.setAddress(super.address + %dL); return _%s; }", 
				field.struct().getName(), field.name(), field.name(), offset, field.name()
			), generated));
	}

	/*
	 * Creates struct field and getter for a nested struct array.
	 *
	 *   public class Example$GenStruct {
	 *     Array _messages;
	 *     public final Array messages() {
	 *       _messages.address(super.address + 123L);
	 *       return _messages;
	 *     }
	 *   }
	 */
	private void generateStructArrayAccessor(final CtClass generated, final Struct struct, final Field field, final long offset)
			throws CannotCompileException, NotFoundException {
		
		Class<?> nestedStruct = generate(field.struct());

		generated.addField(new CtField(classes.get(HeapArray.class.getName()), "_" + field.name(), generated),
				String.format("new %s(0L, %dL, %s.create(%s.class))",
						HeapArray.class.getName(), field.len(), NativeStruct.class.getName(), nestedStruct.getName()));
		
		generated.addMethod(CtNewMethod.make(String.format(
				"public final %s %s(){ _%s.address(super.address + %dL); return _%s; }", 
				HeapArray.class.getName(), field.name(), field.name(), offset, field.name()), 
			generated));
	}

	/*
	 * Creates getter and setter for a primitive type or String.
	 * 
	 *   public class Example$GenStruct {
	 *     public final byte message() {
	 *       return POINTERS.getByte(super.address + 123L);
	 *     }
	 *     public final void message(byte value) {
	 *       return POINTERS.setByte(super.address + 123L, value);
	 *     }
	 *   }
	 */
	private void generateSimpleFieldAccessor(final CtClass generated, final Struct struct, final Field field, final long offset, 
			final CtClass definition) throws CannotCompileException, NotFoundException {

		String fieldClass = typeToClassName(field, definition);

		generated.addMethod(CtNewMethod.make(String.format(
				"public final %s %s(){ %s }", 
				fieldClass, field.name(), implementGet(struct, field, offset)
			), generated));

		generated.addMethod(CtNewMethod.make(String.format(
				"public final void %s(%s o){ %s }", 
				field.name(), fieldClass, implementSet(struct, field, offset)
			), generated));
		
	}

	private String implementGet(final Struct struct, final Field field, final long offset) {
		if(field.type() == Type.BYTE) {
			return implementGetByte(field, offset);
		}
		if(field.type() == Type.CHAR) {
			return implementGetChar(struct, field, offset);
		}
		if(field.type() == Type.INT) {
			return implementGetInt(field, offset);
		}
		if(field.type() == Type.LONG) {
			return implementGetLong(field, offset);
		}
		if(field.type() == Type.STRING) {
			return implementGetString(struct, field, offset);
		}
		throw new IllegalStateException();
	}

	private String implementGetByte(final Field field, final long offset) {
		if(field.len() == 1) {
			return String.format("return POINTERS.getByte(super.address + %dL);", offset);
		}
		return String.format("return POINTERS.getBytes(super.address + %dL, %dL);", offset, field.len());
	}

	private String implementGetChar(Struct struct, Field field, long offset) {
		if(struct.c() && field.len() == 1) {
			return String.format("return POINTERS.getAnsiCChar(super.address + %dL);", offset);
		}
		if(struct.c()) {
			return String.format("return POINTERS.getAnsiCChars(super.address + %dL, %dL);", offset, field.len());
		}
		if(field.len() == 1) {
			return String.format("return POINTERS.getChar(super.address + %dL);", offset);
		}
		return String.format("return POINTERS.getChars(super.address + %dL, %dL);", offset, field.len());
	}

	private String implementGetInt(final Field field, final long offset) {
		if(field.len() == 1) {
			return String.format("return POINTERS.getInt(super.address + %dL);", offset);
		}
		return String.format("return POINTERS.getInts(super.address + %dL, %dL);", offset, field.len());
	}

	private String implementGetLong(final Field field, final long offset) {
		if(field.len() == 1) {
			return String.format("return POINTERS.getLong(super.address + %dL);", offset);
		}
		return String.format("return POINTERS.getLongs(super.address + %dL, %dL);", offset, field.len());
	}

	private String implementGetString(Struct struct, Field field, long offset) {
		if(struct.c()) {
			return String.format("return POINTERS.getAnsiCString(super.address + %dL, %dL);", offset, field.len());
		}
		return String.format("return POINTERS.getString(super.address + %dL, %dL);", offset, field.len());
	}

	private String implementSet(final Struct struct, final Field field, final long offset) {
		if(field.type() == Type.BYTE) {
			return implementSetByte(field, offset);
		}
		if(field.type() == Type.CHAR) {
			return implementSetChar(struct, field, offset);
		}
		if(field.type() == Type.INT) {
			return implementSetInt(field, offset);
		}
		if(field.type() == Type.LONG) {
			return implementSetLong(field, offset);
		}
		if(field.type() == Type.STRING) {
			return implementSetString(struct, field, offset);
		}
		throw new IllegalStateException();
	}

	private String implementSetByte(final Field field, final long offset) {
		if(field.len() == 1) {
			return String.format("return POINTERS.setByte(super.address + %dL, $1);", offset);
		}
		return String.format("return POINTERS.setBytes(super.address + %dL, $1, %dL);", offset, field.len());
	}

	private String implementSetChar(final Struct struct, final Field field, final long offset) {
		if(struct.c() && field.len() == 1) {
			return String.format("return POINTERS.setAnsiCChar(super.address + %dL, $1);", offset);
		}
		if(struct.c()) {
			return String.format("return POINTERS.setAnsiCChars(super.address + %dL, $1, %dL);", offset, field.len());
		}
		if(field.len() == 1) {
			return String.format("return POINTERS.setChar(super.address + %dL, $1);", offset);
		}
		return String.format("return POINTERS.setChars(super.address + %dL, $1, %dL);", offset, field.len());
	}

	private String implementSetInt(final Field field, final long offset) {
		if(field.len() == 1) {
			return String.format("return POINTERS.setInt(super.address + %dL, $1);", offset);
		}
		return String.format("return POINTERS.setInts(super.address + %dL, $1, %dL);", offset, field.len());
	}

	private String implementSetLong(final Field field, final long offset) {
		if(field.len() == 1) {
			return String.format("return POINTERS.setLong(super.address + %dL, $1);", offset);
		}
		return String.format("return POINTERS.setLongs(super.address + %dL, $1, %dL);", offset, field.len());
	}

	private String implementSetString(final Struct struct, final Field field, final long offset) {
		if(struct.c()) {
			return String.format("return POINTERS.setAnsiCString(super.address + %dL, $1, %dL);", offset, field.len());
		}
		return String.format("return POINTERS.setString(super.address + %dL, $1, %dL);", offset, field.len());
	}

	private String typeToClassName(Field field, CtClass definition) {
		Type type = field.type(); 
		if(type == Type.BYTE) {
			return "byte" + (field.len() == 1 ? "" : "[]");
		}
		if(type == Type.CHAR) {
			return "char" + (field.len() == 1 ? "" : "[]");
		}
		if(type == Type.INT) {
			return "int" + (field.len() == 1 ? "" : "[]");
		}
		if(type == Type.LONG) {
			return "long" + (field.len() == 1 ? "" : "[]");
		}
		if(type == Type.STRING) {
			return String.class.getName();
		}
		throw new IllegalStateException();
	}

	private long typeByteLength(Struct struct, Field field) {
		switch (field.type()) {
		case BYTE:
			return 1;
		case STRING:
		case CHAR:
			return struct.c() ? 1 : 2;
		case INT:
			return 4;
		case LONG:
			return 8;
		case STRUCT:
			return NativeStruct.create(generate(field.struct())).getSize();
		default:
			throw new IllegalStateException();
		}
	}

	private long byteLength(Struct struct, Field field) {
		return field.len() * typeByteLength(struct, field);
	}

}
