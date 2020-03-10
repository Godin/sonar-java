package example;

//import org.checkerframework.checker.nullness.qual.Nullable;
import javax.annotation.Nullable;

public interface M<K, V> {

  void g(@Nullable K key);

}
