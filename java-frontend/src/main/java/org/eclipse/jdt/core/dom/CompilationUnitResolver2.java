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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @see ASTParser#createASTs(String[], String[], String[], FileASTRequestor, IProgressMonitor)
 */
public class CompilationUnitResolver2 extends CompilationUnitResolver {

  private CompilationUnitResolver2(
    INameEnvironment environment,
    IErrorHandlingPolicy policy,
    CompilerOptions compilerOptions,
    ICompilerRequestor requestor,
    IProblemFactory problemFactory,
    IProgressMonitor monitor,
    boolean fromJavaProject
  ) {
    super(environment, policy, compilerOptions, requestor, problemFactory, monitor, fromJavaProject);
  }

  public static void resolve(
    String[] sourceUnits,
    FileASTRequestor requestor,
    int apiLevel,
    Map options,
    List<File> classpath,
    int flags,
    IProgressMonitor monitor
  ) {
    ArrayList<FileSystem.Classpath> cp = new ArrayList<>();
    org.eclipse.jdt.internal.compiler.util.Util.collectRunningVMBootclasspath(cp);
    Main main = new Main(new PrintWriter(System.out), new PrintWriter(System.err), false, null, null);
    for (File file : classpath) {
      main.processPathEntries(Main.DEFAULT_SIZE_CLASSPATH, cp, file.getAbsolutePath(), null, false, false);
    }

    CompilationUnitResolver.resolve(
      sourceUnits,
      null,
      new String[0],
      requestor,
      apiLevel,
      options,
      cp,
      flags,
      monitor
    );
  }

}
