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

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.MethodTree;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

final class JMethodSymbol extends JSymbol implements Symbol.MethodSymbol {

  JMethodSymbol(JFactory factory, IBinding binding) {
    super(factory, binding);
  }

  private IMethodBinding methodBinding() {
    return (IMethodBinding) binding;
  }

  @Override
  public List<Type> parameterTypes() {
    return Arrays.stream(methodBinding().getParameterTypes())
      .map(factory::type)
      .collect(Collectors.toList());
  }

  @Override
  public TypeSymbol returnType() {
    return factory.typeSymbol(methodBinding().getReturnType());
  }

  @Override
  public List<Type> thrownTypes() {
    return Arrays.stream(methodBinding().getExceptionTypes())
      .map(factory::type)
      .collect(Collectors.toList());
  }

  @Nullable
  @Override
  public MethodSymbol overriddenSymbol() {
    return null; // TODO
  }

  @Override
  public String signature() {
    return ""; // TODO
  }

  @Nullable
  @Override
  public MethodTree declaration() {
    return (MethodTree) super.declaration();
  }

}
