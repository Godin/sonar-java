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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.java.api.semantic.Type;

import java.io.Serializable;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class JTypeSymbolTest {

  @Test
  void superClass() {
    Type javaLangObject = typeSymbol("java.lang.Object").type();
    assertAll(
      () ->
        assertThat(typeSymbol("java.lang.Object").superClass())
          .as("for java.lang.Object")
          .isNull(),
      () ->
        assertThat(typeSymbol("java.lang.String").superClass())
          .as("for classes")
          .isSameAs(javaLangObject),
      () ->
        assertThat(typeSymbol("java.util.Map").superClass())
          .as("for interfaces")
          .isSameAs(javaLangObject),
      () ->
        assertThat(typeSymbol("int").superClass())
          .as("for primitives")
          .isNull(),
      () ->
        assertThat(typeSymbol("int[]").superClass())
          .as("for arrays")
          .isSameAs(javaLangObject)
    );
  }

  static abstract class Target implements Serializable {
  }

  @Test
  void interfaces() {
    assertThat(typeSymbol("java.lang.Object").interfaces())
      .isEmpty();
    assertThat(typeSymbol(Target.class.getName()).interfaces())
      .containsOnly(type("java.io.Serializable"));
  }

  @Test
  void memberSymbols() {
    assertThat(typeSymbol("java.lang.Object").memberSymbols())
      .isNotEmpty();
  }

  @Test
  void lookupSymbols() {
    assertThat(typeSymbol("java.lang.Object").lookupSymbols("hashCode"))
      .hasSize(1);
  }

  private JTypeSymbol typeSymbol(String name) {
    return sema.typeSymbol(Objects.requireNonNull(sema.resolveType(name)));
  }

  private JType type(String name) {
    return sema.type(Objects.requireNonNull(sema.resolveType(name)));
  }

  private JSema sema;

  @BeforeEach
  void setup() {
    ASTParser astParser = ASTParser.newParser(AST.JLS12);
    astParser.setEnvironment(
      new String[]{"target/test-classes"},
      new String[]{},
      new String[]{},
      true
    );
    astParser.setResolveBindings(true);
    astParser.setUnitName("File.java");
    astParser.setSource("".toCharArray());
    AST ast = astParser.createAST(null).getAST();
    sema = new JSema(ast);
  }

}
