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

import com.google.common.base.Strings;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.sonar.java.resolve.Symbols;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Type;

import java.util.Objects;

final class JType implements Type, Type.ArrayType {

  private final JFactory factory;
  private final ITypeBinding typeBinding;

  JType(JFactory factory, ITypeBinding typeBinding) {
    this.factory = Objects.requireNonNull(factory);
    this.typeBinding = Objects.requireNonNull(typeBinding);
  }

  @Override
  public boolean is(String fullyQualifiedName) {
    return fullyQualifiedName.equals(fullyQualifiedName());
  }

  @Override
  public boolean isSubtypeOf(String fullyQualifiedName) {
    return typeBinding.isSubTypeCompatible(
      factory.resolveTypeBinding(fullyQualifiedName)
    );
  }

  @Override
  public boolean isSubtypeOf(Type type) {
    return typeBinding.isSubTypeCompatible(
      ((JType) type).typeBinding
    );
  }

  @Override
  public boolean isArray() {
    return typeBinding.isArray();
  }

  @Override
  public boolean isClass() {
    return typeBinding.isClass()
      || typeBinding.isInterface()
      || typeBinding.isEnum();
  }

  @Override
  public boolean isVoid() {
    return "V".equals(typeBinding.getBinaryName());
  }

  @Override
  public boolean isPrimitive() {
    return typeBinding.isPrimitive()
      && !isVoid();
  }

  @Override
  public boolean isPrimitive(Primitives primitive) {
    // TODO suboptimal
    return primitive.name().toLowerCase().equals(typeBinding.getName());
  }

  @Override
  public boolean isUnknown() {
    return false; // TODO typeBinding.isRecovered() ?
  }

  @Override
  public boolean isNumerical() {
    switch (typeBinding.getBinaryName()) {
      default:
        return false;
      case "B": // byte
      case "C": // char
      case "S": // short
      case "I": // int
      case "J": // long
      case "F": // float
      case "D": // double
        return true;
    }
  }

  /**
   * @see #is(String)
   */
  @Override
  public String fullyQualifiedName() {
    return qualifiedName(typeBinding);
  }
  private static String qualifiedName(ITypeBinding typeBinding) {
    if (typeBinding.isRecovered()) {
      return Symbols.unknownType.fullyQualifiedName();
    } else if (typeBinding.isNullType() || typeBinding.isPrimitive()) {
      return typeBinding.getName();
    } else if (typeBinding.isArray()) {
      return qualifiedName(typeBinding.getElementType())
        + Strings.repeat("[]", typeBinding.getDimensions());
    } else {
      return typeBinding.getErasure().getBinaryName();
    }
  }

  @Override
  public String name() {
    return typeBinding.getName();
  }

  @Override
  public Symbol.TypeSymbol symbol() {
    return factory.typeSymbol(typeBinding);
  }

  @Override
  public Type erasure() {
    return this;
  }

  @Override
  public Type elementType() {
    return factory.type(typeBinding.getElementType());
  }

}
