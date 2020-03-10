/*
 * SonarQube Java
 * Copyright (C) 2012-2020 SonarSource SA
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
package org.sonar.java.checks;

import org.junit.Test;
import org.sonar.java.checks.verifier.JavaCheckVerifier;
import org.sonar.java.checks.verifier.MultipleFilesJavaCheckVerifier;
import org.sonar.java.se.checks.ConditionalUnreachableCodeCheck;
import org.sonar.java.se.checks.ParameterNullnessCheck;

import java.util.Arrays;

public class WipTest {

  /**
   * Doesn't raise issue
   * when can't load `package-info`
   */
  @Test
  public void investigation_of_false_negative() {
    MultipleFilesJavaCheckVerifier.verify(Arrays.asList(
      "src/test/files/checks/wip/com/google/common/base/Preconditions.java"
      , "src/test/files/checks/wip/com/google/common/base/package-info.java"
    ), new ConditionalUnreachableCodeCheck());
  }

  /**
   * Raises unexpected issue
   * <code>
   * Annotate the parameter with @javax.annotation.Nullable in method 'get' declaration, or make sure that null can not be passed as argument.
   * </code>
   * when `target/classes` is on classpath
   */
  @Test
  public void investigation_of_false_positive() {
    /// This proves that Nullable annotation is read correctly from classes
//    MultipleFilesJavaCheckVerifier.verify(Arrays.asList(
//      "src/test/files/checks/wip/AbstractMultimap.java"
//    ), new ParameterNullnessCheck());

    /// In this case Multimap.java exists as both input and class file
    MultipleFilesJavaCheckVerifier.verify(Arrays.asList(
      "src/test/files/checks/wip/com/google/common/collect/Multimap.java",
      "src/test/files/checks/wip/com/google/common/collect/AbstractMultimap.java"
    ), new ParameterNullnessCheck());

//    MultipleFilesJavaCheckVerifier.verify(Arrays.asList(
//      "src/test/files/checks/wip/example/AM.java",
//      "src/test/files/checks/wip/example/M.java"
//    ), new ParameterNullnessCheck());

//    MultipleFilesJavaCheckVerifier.verify(Arrays.asList(
//      "/Users/evgeny.mandrikov/projects/sonarsource/sonar-java/its/sources/guava/src/com/google/common/collect/AbstractMultimap.java" // 384
//      , "/Users/evgeny.mandrikov/projects/sonarsource/sonar-java/its/sources/guava/src/com/google/common/collect/Multimap.java" // 694
//      , "/Users/evgeny.mandrikov/projects/sonarsource/sonar-java/its/sources/guava/src/com/google/common/collect/package-info.java"
//    ), new ParameterNullnessCheck());
  }

  // NOTE: MultipleFilesJavaCheckVerifier checks only in last file

}
