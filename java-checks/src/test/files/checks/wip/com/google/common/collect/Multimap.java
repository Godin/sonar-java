package com.google.common.collect;

//import org.checkerframework.checker.nullness.qual.Nullable;
import javax.annotation.Nullable;

@javax.annotation.ParametersAreNonnullByDefault
public interface Multimap<K, V> {

  void get(@Nullable K key);

}
