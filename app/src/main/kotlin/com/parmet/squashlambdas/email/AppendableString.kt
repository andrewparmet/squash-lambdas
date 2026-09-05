package com.parmet.squashlambdas.email

internal class AppendableString() : Appendable<StringBuilder> {
    private val delegate = StringBuilder()

    constructor(src: CharSequence) : this() {
        delegate.append(src)
    }

    override fun append(toAppend: StringBuilder) =
        apply { delegate.append(toAppend) }

    override fun iterator() =
        listOf(delegate).iterator()

    override fun toString() =
        delegate.toString()
}
