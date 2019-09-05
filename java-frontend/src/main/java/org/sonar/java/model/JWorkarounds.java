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
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

import java.lang.reflect.Method;

final class JWorkarounds {
  private JWorkarounds() {
  }

  static IAnnotationBinding[] resolvePackageAnnotations(AST ast, String packageName) {
    // See org.eclipse.jdt.core.dom.PackageBinding#getAnnotations()
    try {
      Method methodGetBindingResolver = ast.getClass()
        .getDeclaredMethod("getBindingResolver");
      methodGetBindingResolver.setAccessible(true);
      Object bindingResolver = methodGetBindingResolver.invoke(ast);

      Method methodLookupEnvironment = bindingResolver.getClass()
        .getDeclaredMethod("lookupEnvironment");
      methodLookupEnvironment.setAccessible(true);
      LookupEnvironment lookupEnvironment = (LookupEnvironment) methodLookupEnvironment.invoke(bindingResolver);

      NameEnvironmentAnswer answer = lookupEnvironment.nameEnvironment.findType(
        TypeConstants.PACKAGE_INFO_NAME,
        CharOperation.splitOn('.', packageName.toCharArray())
      );
      if (answer == null) {
        return new IAnnotationBinding[0];
      }

      IBinaryType type = answer.getBinaryType();
      IBinaryAnnotation[] binaryAnnotations = type.getAnnotations();
      AnnotationBinding[] binaryInstances =
        BinaryTypeBinding.createAnnotations(binaryAnnotations, lookupEnvironment, type.getMissingTypeNames());
      AnnotationBinding[] allInstances =
        AnnotationBinding.addStandardAnnotations(binaryInstances, type.getTagBits(), lookupEnvironment);

      Method methodGetAnnotationInstance = bindingResolver.getClass()
        .getDeclaredMethod("getAnnotationInstance", AnnotationBinding.class);
      methodGetAnnotationInstance.setAccessible(true);

      IAnnotationBinding[] domInstances = new IAnnotationBinding[allInstances.length];
      for (int i = 0; i < allInstances.length; i++) {
        domInstances[i] = (IAnnotationBinding) methodGetAnnotationInstance.invoke(bindingResolver, allInstances[i]);
      }
      return domInstances;

    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

}
