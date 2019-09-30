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

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.Converter;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.CancelableProblemFactory;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JCompiler {

  private final String version;
  private final Compiler compiler;
  private final Converter converter;

  public JCompiler(String version, List<File> classpath, boolean resolveBindings) {
    this.version = version;
    Map<String, String> compilerOptions = new HashMap<>();
    compilerOptions.put(JavaCore.COMPILER_COMPLIANCE, version);
    compilerOptions.put(JavaCore.COMPILER_SOURCE, version);
    compilerOptions.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, "enabled");
    compilerOptions.put(CompilerOptions.OPTION_Store_Annotations, "enabled");
    converter = new Converter(compilerOptions, resolveBindings);
    compiler = new Compiler(
      new NameEnvironment(classpath(classpath)),
      ERROR_HANDLING_POLICY,
      new CompilerOptions(compilerOptions, false),
      null,
      new CancelableProblemFactory(null)
    );
  }

  public CompilationUnitTree process(String unitName, String source) {
    CompilationUnit sourceUnit = new CompilationUnit(source.toCharArray(), unitName, "UTF-8");
    CompilationResult unitResult = new CompilationResult(sourceUnit, 0, 0, 100);
    CompilationUnitDeclaration parsedUnit = compiler.parser.dietParse(sourceUnit, unitResult);
    compiler.lookupEnvironment.buildTypeBindings(parsedUnit, null);
    compiler.lookupEnvironment.completeTypeBindings();
    compiler.process(parsedUnit, 0);
    return JParser.convert(version, unitName, source, converter.convert(parsedUnit, unitResult));
  }

  private static class NameEnvironment extends FileSystem {
    NameEnvironment(Classpath[] paths) {
      super(paths, null, false);
    }
  }

  private static FileSystem.Classpath[] classpath(List<File> classpath) {
    List<FileSystem.Classpath> result = new ArrayList<>();
    org.eclipse.jdt.internal.compiler.util.Util.collectRunningVMBootclasspath(result);
    for (File file : classpath) {
      result.add(
        FileSystem.getClasspath(file.getAbsolutePath(), null, null)
      );
    }
    return result.toArray(new FileSystem.Classpath[0]);
  }

  private static final IErrorHandlingPolicy ERROR_HANDLING_POLICY = new IErrorHandlingPolicy() {
    @Override
    public boolean proceedOnErrors() {
      return false;
    }

    @Override
    public boolean stopOnFirstError() {
      return false;
    }

    @Override
    public boolean ignoreAllErrors() {
      return false;
    }
  };

}
