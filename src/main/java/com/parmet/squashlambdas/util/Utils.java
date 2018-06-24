package com.parmet.squashlambdas.util;

import static com.google.common.base.Throwables.throwIfUnchecked;

import com.google.mu.function.CheckedSupplier;

public class Utils {
  public static <T, E extends Throwable> T get(CheckedSupplier<T, E> sup) {
    try {
      return sup.get();
    } catch (Throwable ex) {
      throwIfUnchecked(ex);
      throw new RuntimeException(ex);
    }
  }
}
