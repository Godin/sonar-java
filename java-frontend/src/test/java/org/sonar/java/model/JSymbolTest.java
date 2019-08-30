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
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.fail;

class JSymbolTest {

  @Test
  void wip() {
    fail();
  }

  @Test
  void test() {
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
    JFactory factory = new JFactory(astParser.createAST(null).getAST());

    ITypeBinding typeBinding = Objects.requireNonNull(factory.resolveTypeBinding("java.lang.Object"));
    JTypeSymbol typeSymbol = factory.typeSymbol(typeBinding);
    System.out.println(
    );
  }

  @Test
  void name() {
  }

}
