package org.sonar.java.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.sonar.plugins.java.api.tree.ClassTree
import org.sonar.plugins.java.api.tree.MethodTree
import org.sonar.plugins.java.api.tree.ReturnStatementTree

class ExampleKotlinTest {

  @Test
  fun test() {
    val e = (((JParser
      .parse("12", "File.java", "class C { Object m() { return null; } }", true, emptyList())
      .types()[0] as ClassTree)
      .members()[0] as MethodTree)
      .block()!!
      .body()[0] as ReturnStatementTree)
      .expression() as AbstractTypedTree
    val typeBinding = e.typeBinding!!
    assertFalse(typeBinding.isNullType)
  }

}
