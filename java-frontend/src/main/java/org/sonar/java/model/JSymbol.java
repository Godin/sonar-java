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

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.sonar.java.resolve.Symbols;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.SymbolMetadata;
import org.sonar.plugins.java.api.semantic.Type;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class JSymbol implements Symbol {

  protected final JSema sema;
  protected final IBinding binding;

  JSymbol(JSema sema, IBinding binding) {
    this.sema = Objects.requireNonNull(sema);
    this.binding = Objects.requireNonNull(binding);
  }

  @Override
  public final boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public final int hashCode() {
    return super.hashCode();
  }

  @Override
  public final String toString() {
    switch (binding.getKind()) {
      case IBinding.VARIABLE:
        return owner().name() + "#" + name();
      case IBinding.METHOD:
        return owner().name() + "#" + name() + "()";
      case IBinding.TYPE:
        return name();
      default:
        throw new UnsupportedOperationException("kind: " + binding.getKind());
    }
  }

  @Override
  public final String name() {
    if (binding.getKind() == IBinding.METHOD && ((IMethodBinding) binding).isConstructor()) {
      return "<init>";
    }
    if (binding.getKind() == IBinding.TYPE) {
      return ((ITypeBinding) binding).getErasure().getName();
    }
    return binding.getName();
  }

  /**
   * @see #enclosingClass()
   */
  @Override
  public final Symbol owner() {
    switch (binding.getKind()) {
      case IBinding.TYPE: {
        ITypeBinding typeBinding = (ITypeBinding) binding;
        IMethodBinding declaringMethod = typeBinding.getDeclaringMethod();
        if (declaringMethod != null) { // local type
          return sema.methodSymbol(declaringMethod);
        }
        ITypeBinding declaringClass = typeBinding.getDeclaringClass();
        if (declaringClass != null) { // member type
          return sema.typeSymbol(declaringClass);
        }
        // top-level type
        return new JSymbol(sema, typeBinding.getPackage()) {
        };
      }
      case IBinding.METHOD: {
        IMethodBinding b = (IMethodBinding) binding;
        if (b.getDeclaringClass().isAnonymous() && b.isConstructor()) {
          // TODO
          // see constructor in FileHandlingCheck: new FileReader("") {}
          // and method of interface in HostnameVerifierImplementationCheck
          return sema.typeSymbol(b.getDeclaringClass().getSuperclass());
        }
        return sema.typeSymbol(b.getDeclaringClass());
      }
      case IBinding.VARIABLE: {
        IVariableBinding variableBinding = (IVariableBinding) binding;
        IMethodBinding declaringMethod = variableBinding.getDeclaringMethod();
        if (declaringMethod != null) { // local variable
          return sema.methodSymbol(declaringMethod);
        }
        ITypeBinding declaringClass = variableBinding.getDeclaringClass();
        if (declaringClass != null) { // field
          return sema.typeSymbol(declaringClass);
        }
        // variable declaration in a static or instance initializer
        // or local variable declaration in recovered method
        // or array.length
        // See HiddenFieldCheck
        Tree t = declaration();
        if (t == null) {
          return Symbols.unknownSymbol;
        }
        while (true) {
          t = t.parent();
          switch (t.kind()) {
            case INITIALIZER:
            case STATIC_INITIALIZER:
              return ((ClassTree) t.parent()).symbol();
            case METHOD:
            case CONSTRUCTOR:
              // local variable
              return ((MethodTree) t).symbol();
          }
        }
      }
      default:
        throw new IllegalStateException("Kind: " + binding.getKind());
    }
  }

  @Override
  public final Type type() {
    switch (binding.getKind()) {
      case IBinding.TYPE:
        return sema.type((ITypeBinding) binding);
      case IBinding.VARIABLE:
        return sema.type(((IVariableBinding) binding).getType());
      case IBinding.PACKAGE:
      case IBinding.METHOD:
//        // TODO METHOD in StandardCharsetsConstantsCheck , RedundantTypeCastCheck and MethodIdenticalImplementationsCheck , PACKAGE in InnerClassTooManyLinesCheck
        return null;
      default:
        throw new IllegalStateException("Kind: " + binding.getKind());
    }
  }

  @Override
  public final boolean isVariableSymbol() {
    return binding.getKind() == IBinding.VARIABLE;
  }

  @Override
  public final boolean isTypeSymbol() {
    if (binding.isRecovered()) {
      // TODO see SAMAnnotatedCheck
      return false;
    }
    return binding.getKind() == IBinding.TYPE;
  }

  @Override
  public final boolean isMethodSymbol() {
    return binding.getKind() == IBinding.METHOD;
  }

  @Override
  public final boolean isPackageSymbol() {
    return binding.getKind() == IBinding.PACKAGE;
  }

  @Override
  public final boolean isStatic() {
    return Modifier.isStatic(binding.getModifiers());
  }

  @Override
  public final boolean isFinal() {
    return Modifier.isFinal(binding.getModifiers());
  }

  @Override
  public final boolean isEnum() {
    switch (binding.getKind()) {
      case IBinding.TYPE:
        return ((ITypeBinding) binding).isEnum();
      case IBinding.VARIABLE:
        return ((IVariableBinding) binding).isEnumConstant();
      default:
        return false;
    }
  }

  @Override
  public final boolean isInterface() {
    return binding.getKind() == IBinding.TYPE
      && ((ITypeBinding) binding).isInterface();
  }

  @Override
  public final boolean isAbstract() {
    return Modifier.isAbstract(binding.getModifiers());
  }

  @Override
  public final boolean isPublic() {
    return Modifier.isPublic(binding.getModifiers());
  }

  @Override
  public final boolean isPrivate() {
    return Modifier.isPrivate(binding.getModifiers());
  }

  @Override
  public final boolean isProtected() {
    return Modifier.isProtected(binding.getModifiers());
  }

  @Override
  public final boolean isPackageVisibility() {
    return !isPublic() && !isPrivate() && !isProtected();
  }

  @Override
  public final boolean isDeprecated() {
    return binding.isDeprecated();
  }

  @Override
  public final boolean isVolatile() {
    return Modifier.isVolatile(binding.getModifiers());
  }

  @Override
  public final boolean isUnknown() {
    return binding.isRecovered();
  }

  @Override
  public final SymbolMetadata metadata() {
    IAnnotationBinding[] annotations;
    if (binding.getKind() == IBinding.PACKAGE) {
      annotations = JWorkarounds.resolvePackageAnnotations(sema.ast, binding.getName());
    } else {
      annotations = binding.getAnnotations();
    }
    return new JSymbolMetadata() {
      @Override
      public List<AnnotationInstance> annotations() {
        return Arrays.stream(annotations)
          .map(sema::annotation)
          .collect(Collectors.toList());
      }
    };
  }

  /**
   * @see #owner()
   */
  @Nullable
  @Override
  public final TypeSymbol enclosingClass() {
    switch (binding.getKind()) {
      case IBinding.PACKAGE:
        return null;
      case IBinding.TYPE: {
        ITypeBinding typeBinding = (ITypeBinding) binding;
        ITypeBinding declaringClass = typeBinding.getDeclaringClass();
        if (declaringClass != null) { // nested (member or local) type
          return sema.typeSymbol(declaringClass);
        }
        // top-level type
        return (TypeSymbol) this;
      }
      case IBinding.METHOD: {
        ITypeBinding declaringClass = ((IMethodBinding) binding).getDeclaringClass();
        return sema.typeSymbol(declaringClass);
      }
      case IBinding.VARIABLE: {
        IVariableBinding variableBinding = (IVariableBinding) this.binding;
        ITypeBinding declaringClass = variableBinding.getDeclaringClass();
        if (declaringClass != null) { // field
          return sema.typeSymbol(declaringClass);
        }
        IMethodBinding declaringMethod = variableBinding.getDeclaringMethod();
        if (declaringMethod != null) { // local variable
          return sema.typeSymbol(declaringMethod.getDeclaringClass());
        }
        // FIXME
        // variable declaration in a static or instance initializer
        // or local variable declaration in recovered method
        // or array.length
        return Symbols.unknownSymbol;
      }
      default:
        throw new IllegalStateException("Kind: " + binding.getKind());
    }
  }

  @Override
  public final List<IdentifierTree> usages() {
    List<IdentifierTree> usages = sema.usages.get(binding);
    return usages == null ? Collections.emptyList() : usages;
  }

  @Nullable
  @Override
  public Tree declaration() {
    return sema.declarations.get(binding);
  }

}
