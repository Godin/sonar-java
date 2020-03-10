package com.google.common.collect;

//import org.checkerframework.checker.nullness.qual.Nullable;
import javax.annotation.Nullable;

@javax.annotation.ParametersAreNonnullByDefault
abstract class AbstractMultimap<K, V> implements Multimap<K, V> {

//  public void test(@javax.annotation.Nullable Object o) {
//    x(o); // SHOULD RAISE ISSUE
//  }
//
//  public void x(Object o) {
//  }

  public void put() {
    get(null);
  }

}
