package org.sonar.java.model;

import org.eclipse.jdt.core.dom.IVariableBinding;
import org.sonar.java.resolve.JavaSymbol;
import org.sonar.plugins.java.api.semantic.Symbol;

import javax.annotation.Nullable;

public class JInternals {

  /**
   * Replacement for {@link JavaSymbol.VariableJavaSymbol#constantValue()}
   */
  @Nullable
  public static Object constantValue(Symbol.VariableSymbol symbol) {
    if (symbol instanceof JVariableSymbol) {
      return ((IVariableBinding) ((JVariableSymbol) symbol).binding).getConstantValue();
    }
    return ((JavaSymbol.VariableJavaSymbol) symbol).constantValue().orElse(null);
  }

}
