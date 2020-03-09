package com.google.common.base;

//@javax.annotation.ParametersAreNonnullByDefault
public class Preconditions {

  public static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

}
