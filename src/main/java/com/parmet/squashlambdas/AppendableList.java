package com.parmet.squashlambdas;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppendableList<T> implements Appendable2<T> {
  private final List<T> delegate = new ArrayList<>();

  public AppendableList() {}

  public AppendableList(List<T> src) {
    delegate.addAll(src);
  }

  @Override
  public AppendableList<T> append(T toAppend) {
    delegate.add(toAppend);
    return this;
  }

  @Override
  public ImmutableList<T> toList() {
    return ImmutableList.copyOf(delegate);
  }

  @Override
  public Iterator<T> iterator() {
    return delegate.iterator();
  }
}
