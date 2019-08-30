/*
 * SonarQube Java
 * Copyright (C) 2012-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.model;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.java.api.semantic.Type;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class JTypeTest {

  @Test
  void is() {
    assertThat(adapt(typeBinding("int").createArrayType(2)).is("int[][]"))
      .isTrue();
  }

  @Test
  void isSubtypeOf() {
    Type type = adapt(typeBinding("java.util.HashMap"));
    assertThat(type.isSubtypeOf("java.util.AbstractMap"))
      .as("class")
      .isTrue();
    assertThat(type.isSubtypeOf("java.util.Map"))
      .as("interface")
      .isTrue();
  }

  @Test
  void isArray() {
    assertThat(adapt(typeBinding("int").createArrayType(1)).isArray())
      .isTrue();
  }

  @Test
  void isClass() {
    assertThat(adapt(typeBinding("java.lang.Object")).isClass())
      .as("for classes")
      .isTrue();
    assertThat(adapt(typeBinding("java.util.Map$Entry")).isClass())
      .as("for interfaces")
      .isTrue();
    assertThat(adapt(typeBinding("java.lang.annotation.RetentionPolicy")).isClass())
      .as("for enums")
      .isTrue();
  }

  @Test
  void isVoid() {
    assertThat(adapt(typeBinding("void")).isVoid())
      .isTrue();
  }

  @Test
  void isPrimitive() {
    assertThat(adapt(typeBinding("int")).isPrimitive())
      .isTrue();
    assertThat(adapt(typeBinding("void")).isPrimitive())
      .isFalse();

    assertThat(adapt(typeBinding("int")).isPrimitive(Type.Primitives.INT))
      .isTrue();
    assertThat(adapt(typeBinding("int")).isPrimitive(Type.Primitives.BYTE))
      .isFalse();
  }

  @Test
  void isNumerical() {
    assertThat(adapt(typeBinding("byte")).isNumerical())
      .isTrue();
    assertThat(adapt(typeBinding("char")).isNumerical())
      .isTrue();
    assertThat(adapt(typeBinding("short")).isNumerical())
      .isTrue();
    assertThat(adapt(typeBinding("int")).isNumerical())
      .isTrue();
    assertThat(adapt(typeBinding("long")).isNumerical())
      .isTrue();
    assertThat(adapt(typeBinding("float")).isNumerical())
      .isTrue();
    assertThat(adapt(typeBinding("double")).isNumerical())
      .isTrue();
  }

  @Test
  void fullyQualifiedName() {
    assertThat(adapt(typeBinding("java.util.Map$Entry")).fullyQualifiedName())
      .isEqualTo("java.util.Map$Entry");
    assertThat(adapt(typeBinding("int")).fullyQualifiedName())
      .isEqualTo("int");
  }

  @Test
  void name() {
    assertThat(adapt(typeBinding("java.util.Map$Entry")).name())
      .isEqualTo("Entry");
    assertThat(adapt(typeBinding("int")).name())
      .isEqualTo("int");
  }

  @Test
  void symbol() {
    Type type = adapt(typeBinding("java.lang.Object"));
    assertThat(type.symbol())
      .isSameAs(factory.typeSymbol(typeBinding("java.lang.Object")));
  }

  @Test
  void erasure() {
    Type type = adapt(typeBinding("java.lang.Object"));
    assertThat(type.erasure())
      .isSameAs(type);
  }

  @Test
  void elementType() {
    Type.ArrayType arrayType = (Type.ArrayType) adapt(typeBinding("int").createArrayType(2));
    assertThat(arrayType.elementType())
      .isSameAs(adapt(typeBinding("int")));
  }

  private static JFactory factory;

  private static Type adapt(ITypeBinding typeBinding) {
    return factory.type(typeBinding);
  }

  private static ITypeBinding typeBinding(String name) {
    return Objects.requireNonNull(factory.resolveTypeBinding(name));
  }

  @BeforeAll
  static void setup() {
    ASTParser astParser = ASTParser.newParser(AST.JLS12);
    astParser.setEnvironment(
      new String[]{},
      new String[]{},
      new String[]{},
      true
    );
    astParser.setResolveBindings(true);
    astParser.setBindingsRecovery(true);
    astParser.setUnitName("Test.java");
    astParser.setSource("".toCharArray());
    factory = new JFactory(astParser.createAST(null).getAST());
  }

}
