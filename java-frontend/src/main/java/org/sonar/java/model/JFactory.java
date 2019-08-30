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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.sonar.java.resolve.Symbols;
import org.sonar.plugins.java.api.semantic.Symbol;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

final class JFactory {

  private final AST ast;

  private final Map<ITypeBinding, JType> types = new HashMap<>();

  JFactory(AST ast) {
    this.ast = ast;
  }

  JType type(ITypeBinding binding) {
    return types.computeIfAbsent(binding, k -> new JType(this, binding));
  }

  Symbol.TypeSymbol typeSymbol(ITypeBinding binding) {
    // FIXME implement
    return Symbols.unknownSymbol;
  }

  @Nullable
  ITypeBinding resolveTypeBinding(String name) {
    ITypeBinding typeBinding = ast.resolveWellKnownType(name);
    if (typeBinding != null) {
      return typeBinding;
    }

    // BindingResolver bindingResolver = ast.getBindingResolver();
    // ReferenceBinding referenceBinding = bindingResolver
    //   .lookupEnvironment()
    //   .getType(CharOperation.splitOn('.', fqn.toCharArray()));
    // return bindingResolver.getTypeBinding(referenceBinding);
    try {
      Method methodGetBindingResolver = ast.getClass()
        .getDeclaredMethod("getBindingResolver");
      methodGetBindingResolver.setAccessible(true);
      Object bindingResolver = methodGetBindingResolver.invoke(ast);

      Method methodLookupEnvironment = bindingResolver.getClass()
        .getDeclaredMethod("lookupEnvironment");
      methodLookupEnvironment.setAccessible(true);
      LookupEnvironment lookupEnvironment = (LookupEnvironment) methodLookupEnvironment.invoke(bindingResolver);

      ReferenceBinding referenceBinding = lookupEnvironment.getType(
        CharOperation.splitOn('.', name.toCharArray())
      );

      Method methodGetTypeBinding = bindingResolver.getClass()
        .getDeclaredMethod("getTypeBinding", TypeBinding.class);
      methodGetTypeBinding.setAccessible(true);
      return (ITypeBinding) methodGetTypeBinding.invoke(bindingResolver, referenceBinding);

    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }

}
