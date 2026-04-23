package org.eclipse.jdt.core.dom;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.core.dom.ICompilationUnitResolver;

import java.util.List;
import java.util.Map;

public class CompilationUnitResolverDiscovery {

  public static String JAVA_HOME;
  public static String VERSION;

  static ICompilationUnitResolver getInstance() {
    return new ICompilationUnitResolver() {

      @Override
      public void resolve(String[] sourceFilePaths, String[] encodings, String[] bindingKeys,
                          FileASTRequestor requestor, int apiLevel, Map<String, String> compilerOptions, List<FileSystem.Classpath> classpath,
                          int flags, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void parse(ICompilationUnit[] compilationUnits, ASTRequestor requestor, int apiLevel,
                        Map<String, String> compilerOptions, int flags, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void parse(String[] sourceFilePaths, String[] encodings, FileASTRequestor requestor, int apiLevel,
                        Map<String, String> compilerOptions, int flags, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void resolve(ICompilationUnit[] compilationUnits, String[] bindingKeys, ASTRequestor requestor,
                          int apiLevel, Map<String, String> compilerOptions, IJavaProject project,
                          WorkingCopyOwner workingCopyOwner, int flags, IProgressMonitor monitor) {
        throw new UnsupportedOperationException();
      }

      @Override
      public CompilationUnit toCompilationUnit(org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit, final boolean initialNeedsToResolveBinding, IJavaProject project, List<FileSystem.Classpath> classpaths, int focalPosition,
                                               int apiLevel, Map<String, String> compilerOptions, WorkingCopyOwner parsedUnitWorkingCopyOwner, WorkingCopyOwner typeRootWorkingCopyOwner, int flags, IProgressMonitor monitor) {

        classpaths = List.of(
          FileSystem.getOlderSystemRelease(
            JAVA_HOME,
            VERSION,
            null
          ));

        return CompilationUnitResolver.toCompilationUnit(sourceUnit, initialNeedsToResolveBinding, project,
          classpaths, focalPosition == -1 ? null : new NodeSearcher(focalPosition), apiLevel, compilerOptions, parsedUnitWorkingCopyOwner, typeRootWorkingCopyOwner, flags, monitor);
      }

    };
  }
}
