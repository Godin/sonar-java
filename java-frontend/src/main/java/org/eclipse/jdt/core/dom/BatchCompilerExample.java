package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.core.compiler.batch.BatchCompiler;

import java.io.PrintWriter;

public class BatchCompilerExample {

  public static void main(String[] args) {
    BatchCompiler.compile(new String[]{
      "--release", "8",
      "/tmp/j/Example.java",
      "-d", "/tmp/j/classes"
    }, new PrintWriter(System.out), new PrintWriter(System.err), null);
  }

}
