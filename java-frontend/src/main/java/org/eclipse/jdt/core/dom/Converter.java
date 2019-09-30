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
package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;

import java.util.Map;

public final class Converter {

  private final Map<String, String> compilerOptions;
  private final boolean resolveBindings;
  private final DefaultBindingResolver.BindingTables bindingTables = new DefaultBindingResolver.BindingTables();

  public Converter(Map<String, String> compilerOptions, boolean resolveBindings) {
    this.compilerOptions = compilerOptions;
    this.resolveBindings = resolveBindings;
  }

  public CompilationUnit convert(
    CompilationUnitDeclaration compilationUnitDeclaration,
    CompilationResult compilationResult
  ) {
    AST ast = AST.newAST(AST.JLS12);
    if (resolveBindings) {
      ast.setFlag(AST.RESOLVED_BINDINGS);
      ast.setBindingResolver(new DefaultBindingResolver(
        compilationUnitDeclaration.scope,
        null,
        bindingTables,
        true,
        false
      ));
    } else {
      ast.setBindingResolver(new BindingResolver());
    }

    ASTConverter converter = new ASTConverter(compilerOptions, resolveBindings, null);
    converter.setAST(ast);
    org.eclipse.jdt.core.dom.CompilationUnit dom = converter.convert(
      compilationUnitDeclaration,
      compilationResult.compilationUnit.getContents()
    );
    dom.setLineEndTable(
      compilationResult.getLineSeparatorPositions()
    );
    return dom;
  }

}
