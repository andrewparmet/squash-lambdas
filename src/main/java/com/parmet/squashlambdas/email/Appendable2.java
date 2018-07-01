package com.parmet.squashlambdas.email;

interface Appendable2<T> extends Iterable<T> {
  Appendable2<T> append(T toAppend);

  default Appendable2<T> appendAll(Appendable2<T> toAppends) {
    for (T toAppend : toAppends) {
      append(toAppend);
    }
    return this;
  }
}
