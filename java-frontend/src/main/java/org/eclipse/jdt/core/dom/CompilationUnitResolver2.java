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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.core.CancelableProblemFactory;
import org.eclipse.jdt.internal.core.INameEnvironmentWithProgress;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @see ASTParser#createASTs(String[], String[], String[], FileASTRequestor, IProgressMonitor)
 * @see CompilationUnitResolver#resolve(String[], String[], String[], FileASTRequestor, int, Map, List, int, IProgressMonitor)
 */
public class CompilationUnitResolver2 extends CompilationUnitResolver {

  private final IProgressMonitor monitor;

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
    this.monitor = monitor;
  }

  public static void resolve(
    org.eclipse.jdt.internal.compiler.env.ICompilationUnit[] sourceUnits,
    FileASTRequestor requestor,
    Map<String, String> options,
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

//    CompilationUnitResolver.resolve(
//      sourceUnits,
//      null,
//      new String[0],
//      requestor,
//      apiLevel, // AST.JLS12
//      options,
//      cp,
//      flags,
//      monitor
//    );

    INameEnvironmentWithProgress environment = null;
    CancelableProblemFactory problemFactory = null;
    try {
      SubMonitor subMonitor = SubMonitor.convert(monitor, sourceUnits.length * 2);
      environment = new NameEnvironmentWithProgress(cp.toArray(new FileSystem.Classpath[0]), null, subMonitor);
      problemFactory = new CancelableProblemFactory(subMonitor);
      CompilerOptions compilerOptions = getCompilerOptions(options, (flags & ICompilationUnit.ENABLE_STATEMENTS_RECOVERY) != 0);
      compilerOptions.ignoreMethodBodies = (flags & ICompilationUnit.IGNORE_METHOD_BODIES) != 0;
      CompilationUnitResolver2 resolver = new CompilationUnitResolver2(
        environment,
        getHandlingPolicy(),
        compilerOptions,
        getRequestor(),
        problemFactory,
        subMonitor,
        false);
      resolver.resolve(sourceUnits, requestor, options, flags);
    } finally {
      if (environment != null) {
        environment.setMonitor(null); // don't hold a reference to this external object
      }
      if (problemFactory != null) {
        problemFactory.monitor = null; // don't hold a reference to this external object
      }
    }
  }

  public static org.eclipse.jdt.internal.compiler.env.ICompilationUnit[] createSourceUnits(String[] paths) {
    org.eclipse.jdt.internal.compiler.env.ICompilationUnit[] result = new org.eclipse.jdt.internal.compiler.env.ICompilationUnit[paths.length];
    for (int i = 0; i < paths.length; i++) {
      result[i] = new org.eclipse.jdt.internal.compiler.batch.CompilationUnit(null, paths[i], null);
    }
    return result;
  }

  private void resolve(
    org.eclipse.jdt.internal.compiler.env.ICompilationUnit[] sourceUnits,
    FileASTRequestor astRequestor,
    Map<String, String> compilerOptions,
    int flags
  ) {
    astRequestor.compilationUnitResolver = this;
    this.bindingTables = new DefaultBindingResolver.BindingTables();
    CompilationUnitDeclaration unit = null;
    try {
      beginToCompile(sourceUnits, new String[0]);
      for (int i = 0; i < this.totalUnits; i++) {
        unit = this.unitsToProcess[i];
        try {
          process(unit, i);

          char[] fileName = unit.compilationResult.getFileName();
          org.eclipse.jdt.internal.compiler.env.ICompilationUnit source = (org.eclipse.jdt.internal.compiler.env.ICompilationUnit) this.requestedSources.get(fileName);
          CompilationResult compilationResult = unit.compilationResult;
          org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit = compilationResult.compilationUnit;
          char[] contents = sourceUnit.getContents();
          AST ast = AST.newAST(AST.JLS12);
          ast.setFlag(flags | AST.RESOLVED_BINDINGS);
          ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
          ASTConverter converter = new ASTConverter(compilerOptions, true, this.monitor);
          BindingResolver resolver = new DefaultBindingResolver(unit.scope, null, this.bindingTables, (flags & ICompilationUnit.ENABLE_BINDINGS_RECOVERY) != 0, this.fromJavaProject);
          ast.setBindingResolver(resolver);
          converter.setAST(ast);
          CompilationUnit compilationUnit = converter.convert(unit, contents);
          compilationUnit.setTypeRoot(null);
          compilationUnit.setLineEndTable(compilationResult.getLineSeparatorPositions());
          ast.setDefaultNodeFlag(0);
          ast.setOriginalModificationCount(ast.modificationCount());

          astRequestor.acceptAST(new String(source.getFileName()), compilationUnit);
        } finally {
          // cleanup compilation unit result
          unit.cleanUp();
        }
        this.unitsToProcess[i] = null; // release reference to processed unit declaration
        this.requestor.acceptResult(unit.compilationResult.tagAsAccepted());
      }
    } catch (OperationCanceledException e) {
      throw e;
    } catch (org.eclipse.jdt.internal.compiler.problem.AbortCompilation e) {
      this.handleInternalException(e, unit);
    } catch (Error e) {
      this.handleInternalException(e, unit, null);
      throw e; // rethrow
    } catch (RuntimeException e) {
      this.handleInternalException(e, unit, null);
      throw e; // rethrow
    } finally {
      // disconnect ourselves from ast requestor
      astRequestor.compilationUnitResolver = null;
    }
  }

  @Override
  public void process(CompilationUnitDeclaration unit, int i) {
    HashtableOfObject tmp = this.requestedSources;
    this.requestedSources = new HashtableOfObject();
    super.process(unit, i);
    this.requestedSources = tmp;
  }

}
