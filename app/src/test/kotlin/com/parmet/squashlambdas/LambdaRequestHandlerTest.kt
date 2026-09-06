package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.notify.OperatorNotifier
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Test

class LambdaRequestHandlerTest {
    private val notifier = mockk<OperatorNotifier>(relaxed = true)

    @Test
    fun `initialize before processing`() {
        val actions = mutableListOf<String>()
        val handler = TestHandler(notifier, actions)

        handler.handleRequest("input", mockk())

        assertThat(actions).containsExactly("initialize", "process input").inOrder()
    }

    @Test
    fun `publish processing failures`() {
        val failure = IllegalStateException("failure")
        val handler = TestHandler(notifier, mutableListOf(), failure)

        handler.handleRequest("input", mockk())

        coVerify(exactly = 1) { notifier.publishFailure(failure) }
        assertThat(RequestContext.context).isEmpty()
    }

    @Test
    fun `scheduled handlers expose concrete input types`() {
        listOf(MakeReservationHandler::class.java, MonitorSlotsHandler::class.java).forEach { handlerClass ->
            val method = handlerClass.getDeclaredMethod(
                "handleRequest",
                ScheduledEvent::class.java,
                Context::class.java
            )

            assertThat(method.declaringClass).isEqualTo(handlerClass)
        }
    }
}

private class TestHandler(
    override val notifier: OperatorNotifier,
    private val actions: MutableList<String>,
    private val failure: Exception? = null
) : LambdaRequestHandler(),
    RequestHandler<String, Any> {
    override fun initialize() {
        actions += "initialize"
    }

    override fun handleRequest(input: String, context: Context) {
        handle(input) {
            failure?.let { throw it }
            actions += "process $input"
        }
    }
}
