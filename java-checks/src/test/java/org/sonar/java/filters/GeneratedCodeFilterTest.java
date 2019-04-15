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
package org.sonar.java.filters;

import org.junit.Test;
import org.sonar.java.checks.CommentRegularExpressionCheck;
import org.sonar.java.checks.naming.BadClassNameCheck;
import org.sonar.java.checks.naming.BadLocalVariableNameCheck;
import org.sonar.java.checks.naming.BadMethodNameCheck;

@org.junit.Ignore("SymbolMetadata")
public class GeneratedCodeFilterTest {

  @Test
  public void test() {
    CommentRegularExpressionCheck commentRegularExpressionCheck = new CommentRegularExpressionCheck();
    commentRegularExpressionCheck.regularExpression = ".*alpha.*";
    FilterVerifier.verify("src/test/files/filters/GeneratedCodeFilter.java", new GeneratedCodeFilter(),
      // activated rules
      commentRegularExpressionCheck,
      new BadClassNameCheck(),
      new BadMethodNameCheck(),
      new BadLocalVariableNameCheck());
  }
}
