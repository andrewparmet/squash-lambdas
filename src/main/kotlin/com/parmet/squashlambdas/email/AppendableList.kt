package com.parmet.squashlambdas.email

class AppendableList<T>() : Appendable<T>, Iterable<T> {
    private val delegate: MutableList<T> = mutableListOf()

    constructor(src: List<T>): this() {
        delegate.addAll(src)
    }

    override fun append(toAppend: T) = apply { delegate.add(toAppend) }

    override fun iterator() = delegate.iterator()
}
