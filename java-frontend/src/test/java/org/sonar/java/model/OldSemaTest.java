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

import org.junit.jupiter.api.Test;
import org.sonar.java.bytecode.loader.SquidClassLoader;
import org.sonar.java.resolve.SemanticModel;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.VariableTree;

import java.util.Collections;

@Deprecated
class OldSemaTest {

  @Test
  void old() {
    CompilationUnitTree t = JParser.parse("12", "File.java", "class I { int[] f; }", true, Collections.emptyList());
    SemanticModel.createFor(t, new SquidClassLoader(Collections.emptyList()));
    ClassTree c = (ClassTree) t.types().get(0);
    VariableTree f = (VariableTree) c.members().get(0);
    Symbol symbol = f.symbol();
    System.out.println(
      "! " + symbol.isVariableSymbol()
    );
    System.out.println(
      "! " + symbol.type()
    );
  }


}
