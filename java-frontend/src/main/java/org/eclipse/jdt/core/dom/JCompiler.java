package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JCompiler extends Compiler {

  private final static String SRC = new File("src/test/wip/").getAbsolutePath();
  private final static String SRC1 = SRC + "/example/A.java";
  private final static String SRC2 = SRC + "/example/B.java";

  public static void main(String[] args) {
    assertTrue(new File(SRC).exists(), SRC);
    assertTrue(new File(SRC1).exists(), SRC1);
    assertTrue(new File(SRC2).exists(), SRC2);

    ASTParser astParser = ASTParser.newParser(AST.JLS13);
    astParser.setCompilerOptions(createSettings());
    astParser.setEnvironment(
      new String[0],
      new String[0],
      null,
      true
    );
    astParser.setResolveBindings(true);
    astParser.setBindingsRecovery(true);

    astParser.createASTs(
      new String[]{
        SRC1,
        SRC2
      },
      null,
      new String[0],
      new FileASTRequestor() {
        @Override
        public void acceptAST(String sourceFilePath, org.eclipse.jdt.core.dom.CompilationUnit ast) {
          if (sourceFilePath.endsWith("A.java")) {
            printA(ast);
//          } else {
//            printB(ast);
          }
        }
      },
      null
    );

//    System.out.println("---------------------");
//    new JCompiler().run();
  }

  private JCompiler(
  ) {
    super(
      createNameEnvironment(),
      createErrorHandlingPolicy(),
      createCompilerOptions(),
      (f) -> {
        System.out.println(f.compiledTypes);
      },
      new DefaultProblemFactory()
    );
  }

  private static CompilerOptions createCompilerOptions() {
    CompilerOptions compilerOptions = new CompilerOptions(
      createSettings()
    );
    compilerOptions.storeAnnotations = true;
    return compilerOptions;
  }

  private void run() {
    DefaultBindingResolver.BindingTables bindingTables = new DefaultBindingResolver.BindingTables();

    beginToCompile(
      new ICompilationUnit[]{
        createUnit(SRC1),
        createUnit(SRC2)
      }
    );

    for (int i = 0; i < this.totalUnits; i++) {
      CompilationUnitDeclaration unitToProcess = this.unitsToProcess[i];
      System.out.println("Processing " + new String(unitToProcess.getFileName()));
      process(unitToProcess, i);
      ASTConverter converter = new ASTConverter(
        createSettings(),
        true,
        null
      );
      AST ast = AST.newAST(AST.JLS8, false);
      ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
      ast.scanner.sourceLevel = ClassFileConstants.JDK1_8;
      ast.scanner.complianceLevel = ClassFileConstants.JDK1_8;
      ast.setFlag(AST.RESOLVED_BINDINGS);
      converter.setAST(ast);
      BindingResolver resolver = new DefaultBindingResolver(
        unitToProcess.scope,
        DefaultWorkingCopyOwner.PRIMARY,
        bindingTables,
        true,
        false
      );
      ast.setBindingResolver(resolver);
      org.eclipse.jdt.core.dom.CompilationUnit converted = converter.convert(
        unitToProcess,
        unitToProcess.compilationResult.compilationUnit.getContents()
      );

      assertTrue(converted.getProblems().length == 0, "problems");

      if (new String(unitToProcess.getFileName()).endsWith("B.java")) {
        printB(converted);
      }
      if (new String(unitToProcess.getFileName()).endsWith("A.java")) {
        printA(converted);
      }
    }
  }

  private static void printA(org.eclipse.jdt.core.dom.CompilationUnit converted) {
    TypeDeclaration typeDeclaration = (TypeDeclaration) converted.types().get(0);
    MethodDeclaration methodDeclaration = (MethodDeclaration) typeDeclaration.bodyDeclarations.get(0);
    ExpressionStatement s = (ExpressionStatement) methodDeclaration.getBody().statements().get(0);
    IMethodBinding methodBinding = ((MethodInvocation) s.getExpression()).resolveMethodBinding();
    System.out.println(methodBinding.isParameterizedMethod());
//    methodBinding = methodBinding.getMethodDeclaration();
    System.out.println(Arrays.toString(methodBinding.getParameterAnnotations(0)));
  }

  private static void printB(org.eclipse.jdt.core.dom.CompilationUnit converted) {
    TypeDeclaration typeDeclaration = (TypeDeclaration) converted.types().get(0);
    MethodDeclaration method = typeDeclaration.getMethods()[0];
    IMethodBinding methodBinding = method.resolveBinding();
    IAnnotationBinding[] parameterAnnotations = methodBinding.getParameterAnnotations(0);
    System.out.println(Arrays.toString(parameterAnnotations));
  }

  private static char[] source(String filename) {
    try {
      return Util.getFileCharContent(new File(filename), "UTF-8");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static ICompilationUnit createUnit(String filename) {
    return new CompilationUnit(
      source(filename),
      filename,
      "UTF-8"
    );
  }

  private static INameEnvironment createNameEnvironment() {
    List<FileSystem.Classpath> cp = new ArrayList<>();
    org.eclipse.jdt.internal.compiler.util.Util.collectRunningVMBootclasspath(cp);
    cp.add(FileSystem.getClasspath(SRC, "UTF-8", null));
    return new FS(cp);
  }

  static class FS extends FileSystem {
    FS(List<Classpath> cp) {
      super(cp.toArray(new Classpath[0]), new String[0], false);
    }
  }

  private static IErrorHandlingPolicy createErrorHandlingPolicy() {
    return new IErrorHandlingPolicy() {
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

  private static Map<String, String> createSettings() {
    HashMap<String, String> settings = new HashMap<>();
    String version = "8";
    settings.put(JavaCore.COMPILER_COMPLIANCE, version);
    settings.put(JavaCore.COMPILER_SOURCE, version);
    return settings;
  }

  private static void assertTrue(boolean actual, String message) {
    if (!actual) {
      throw new AssertionError(message);
    }
  }

}
