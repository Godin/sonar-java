package example;

//import org.checkerframework.checker.nullness.qual.Nullable;
import javax.annotation.Nullable;

abstract class AM<K, V> implements M<K, V> {

//  public void test(@javax.annotation.Nullable Object o) {
//    x(o); // SHOULD RAISE ISSUE
//  }
//
//  public void x(Object o) {
//  }

  public void put(@Nullable K key) {
    g(key);
  }

}
