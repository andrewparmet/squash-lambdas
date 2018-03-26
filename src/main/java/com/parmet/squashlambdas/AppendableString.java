package com.parmet.squashlambdas;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;

public class AppendableString implements Appendable2<StringBuilder> {

  private final StringBuilder delegate = new StringBuilder();

  public AppendableString() {}

  public AppendableString(StringBuilder src) {
    append(src);
  }

  @Override
  public Appendable2<StringBuilder> append(StringBuilder toAppend) {
    delegate.append(toAppend.toString());
    return this;
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public Iterator<StringBuilder> iterator() {
    return ImmutableList.of(delegate).iterator();
  }

  @Override
  public ImmutableList<StringBuilder> toList() {
    return ImmutableList.of(delegate);
  }
}
