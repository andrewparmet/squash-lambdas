package com.parmet.squashlambdas.email;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import java.util.List;

class AppendableList<T> implements Appendable2<T> {
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
  public UnmodifiableIterator<T> iterator() {
    return Iterators.unmodifiableIterator(delegate.iterator());
  }
}
