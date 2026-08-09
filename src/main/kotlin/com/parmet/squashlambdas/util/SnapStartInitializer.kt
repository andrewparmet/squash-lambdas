package com.parmet.squashlambdas.util

import org.crac.Context
import org.crac.Core
import org.crac.Resource

internal class SnapStartInitializer(
    initialize: () -> Unit
) : Resource {
    private val initialization = lazy(initializer = initialize)

    init {
        Core.getGlobalContext().register(this)
    }

    fun initialize() =
        initialization.value

    override fun beforeCheckpoint(context: Context<out Resource>) {
        initialize()
    }

    override fun afterRestore(context: Context<out Resource>) =
        Unit
}
