package com.parmet.squashlambdas.email;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

class AppendableString implements Appendable2<StringBuilder> {
  private final StringBuilder delegate = new StringBuilder();

  public AppendableString() {}

  public AppendableString(StringBuilder src) {
    append(src);
  }

  @Override
  public final Appendable2<StringBuilder> append(StringBuilder toAppend) {
    delegate.append(toAppend);
    return this;
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public UnmodifiableIterator<StringBuilder> iterator() {
    return ImmutableList.of(delegate).iterator();
  }
}
