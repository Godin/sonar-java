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
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.ClassTree;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

final class JTypeSymbol extends JSymbol implements Symbol.TypeSymbol {

  JTypeSymbol(JFactory factory, IBinding binding) {
    super(factory, binding);
  }

  private ITypeBinding typeBinding() {
    return (ITypeBinding) binding;
  }

  @Nullable
  @Override
  public Type superClass() {
    ITypeBinding superclassTypeBinding = typeBinding().getSuperclass();
    if (superclassTypeBinding == null) {
      return null; // TODO or unknown???
    }
    return factory.type(superclassTypeBinding);
  }

  @Override
  public List<Type> interfaces() {
    return Arrays.stream(typeBinding().getInterfaces())
      .map(factory::type)
      .collect(Collectors.toList());
  }

  @Override
  public Collection<Symbol> memberSymbols() {
    Collection<Symbol> members = new ArrayList<>();
    for (ITypeBinding b : typeBinding().getDeclaredTypes()) {
      members.add(factory.typeSymbol(b));
    }
    for (IVariableBinding b : typeBinding().getDeclaredFields()) {
      members.add(factory.variableSymbol(b));
    }
    for (IMethodBinding b : typeBinding().getDeclaredMethods()) {
      members.add(factory.methodSymbol(b));
    }
    return members;
  }

  @Override
  public Collection<Symbol> lookupSymbols(String name) {
    return memberSymbols().stream()
      .filter(member -> name.equals(member.name()))
      .collect(Collectors.toList());
  }

  @Nullable
  @Override
  public ClassTree declaration() {
    return (ClassTree) super.declaration();
  }

}
