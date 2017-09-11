abstract class A {

  // yield [arg -> true] has two flows
  private void one_yield_two_flows(boolean arg) {  // flow@xproc1 [[order=2]] {{Implies 'arg' has the same value as 'a'.}} flow@xproc2 [[order=2]] {{Implies 'arg' has the same value as 'a'.}}
    if (cond) {
      if (arg) return;  // flow@xproc1 [[order=3]] {{Implies 'arg' is true.}}
    } else {
      if (arg) return;  // flow@xproc2 [[order=3]] {{Implies 'arg' is true.}}
    }
    throw new RuntimeException();
  }

  void test(boolean a) {
    one_yield_two_flows(a); // flow@xproc1 [[order=1]] {{'a' is passed to 'one_yield_two_flows()'.}} flow@xproc1 [[order=4]] {{Implies 'a' is true.}} flow@xproc2 [[order=1]] {{'a' is passed to 'one_yield_two_flows()'.}} flow@xproc2 [[order=4]] {{Implies 'a' is true.}}
    // Noncompliant@+1 [[flows=xproc1,xproc2]]
    if (a) { // flow@xproc1 [[order=5]] {{Expression is always true.}} flow@xproc2 [[order=5]] {{Expression is always true.}}
    }
  }

  // two exceptional yields [arg -> true, cond -> true] [arg -> true, cond -> false]
  // two normal yields [arg -> false, cond -> true] [arg -> false, cond -> false]
  private void two_yields_one_flow_each(boolean arg, boolean cond) throws MyEx { // flow@normal1 [[order=2]] {{Implies 'arg' has the same value as 'a'.}} flow@normal2 [[order=2]] {{Implies 'arg' has the same value as 'a'.}}  flow@ex1 [[order=2]] {{Implies 'arg' has the same value as 'a'.}} flow@ex2 [[order=2]] {{Implies 'arg' has the same value as 'a'.}}
    if (cond) {
      if (arg) throw new MyEx();  // flow@normal1 [[order=3]] {{Implies 'arg' is false.}} flow@ex1 [[order=3]] {{Implies 'arg' is true.}}
    } else {
      if (arg) throw new MyEx();  // flow@normal2 [[order=3]] {{Implies 'arg' is false.}} flow@ex2 [[order=3]] {{Implies 'arg' is true.}}
    }
    return;
  }

  void test_two_yields(boolean a, boolean cond) {
    try {
      two_yields_one_flow_each(a, cond); // flow@normal1 [[order=1]] {{'a' is passed to 'two_yields_one_flow_each()'.}} flow@normal1 [[order=4]] {{Implies 'a' is false.}} flow@normal2 [[order=1]] {{'a' is passed to 'two_yields_one_flow_each()'.}} flow@normal2 [[order=4]] {{Implies 'a' is false.}} flow@ex1 [[order=1]] {{'a' is passed to 'two_yields_one_flow_each()'.}} flow@ex1 [[order=4]] {{Implies 'a' is true.}} flow@ex1 [[order=5]] {{'MyEx' is thrown.}}  flow@ex2 [[order=1]] {{'a' is passed to 'two_yields_one_flow_each()'.}} flow@ex2 [[order=4]] {{Implies 'a' is true.}} flow@ex2 [[order=5]] {{'MyEx' is thrown.}}
    } catch (MyEx e) {
      // Noncompliant@+1 [[flows=ex1,ex2]]
      if (a) { // flow@ex1 [[order=6]] {{Expression is always true.}} flow@ex2 [[order=6]] {{Expression is always true.}}
        return;
      }
    }
    // Noncompliant@+1 [[flows=normal1,normal2]]
    if (a) { // flow@normal1 [[order=5]] {{Expression is always false.}} flow@normal2 [[order=5]] {{Expression is always false.}}

    }
  }

  class MyEx extends Exception {
  }


}
