package com.parmet.squashlambdas;

import com.google.common.collect.ImmutableList;

public interface Appendable2<T> extends Iterable<T> {
  Appendable2<T> append(T toAppend);

  default Appendable2<T> appendAll(Appendable2<T> toAppends) {
    for (T toAppend : toAppends) {
      append(toAppend);
    }
    return this;
  }

  ImmutableList<T> toList();
}
