package com.parmet.squashlambdas.util;

import static com.google.common.base.Throwables.throwIfUnchecked;

import com.google.mu.function.CheckedSupplier;

public class Utils {
  public static <R, T extends Throwable> R get(CheckedSupplier<R, T> sup) {
    try {
      return sup.get();
    } catch (Throwable ex) {
      throwIfUnchecked(ex);
      throw new RuntimeException(ex);
    }
  }

  public static <T extends Throwable> void run(CheckedRunnable<T> run) {
    get(() -> {
      run.run();
      return null;
    });
  }

  public interface CheckedRunnable<T extends Throwable> {
    void run() throws T;
  }
}
