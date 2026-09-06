package com.parmet.squashlambdas

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.google.common.truth.Truth.assertThat
import com.parmet.squashlambdas.notify.OperatorNotifier
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Test

class ScheduledLambdaRequestHandlerTest {
    private val notifier = mockk<OperatorNotifier>(relaxed = true)

    @Test
    fun `initialize before processing`() {
        val actions = mutableListOf<String>()
        val handler = TestHandler(notifier, actions)

        handler.handleRequest(ScheduledEvent(), mockk())

        assertThat(actions).containsExactly("initialize", "process").inOrder()
    }

    @Test
    fun `publish processing failures`() {
        val failure = IllegalStateException("failure")
        val handler = TestHandler(notifier, mutableListOf(), failure)

        handler.handleRequest(ScheduledEvent(), mockk())

        coVerify(exactly = 1) { notifier.publishFailure(failure) }
        assertThat(RequestContext.context).isEmpty()
    }

    @Test
    fun `scheduled handlers expose concrete input types`() {
        listOf(MakeReservationHandler::class.java, MonitorSlotsHandler::class.java).forEach { handlerClass ->
            val method = handlerClass.getMethod(
                "handleRequest",
                ScheduledEvent::class.java,
                Context::class.java
            )

            assertThat(method.parameterTypes.first()).isEqualTo(ScheduledEvent::class.java)
        }
    }
}

private class TestHandler(
    override val notifier: OperatorNotifier,
    private val actions: MutableList<String>,
    private val failure: Exception? = null
) : ScheduledLambdaRequestHandler() {
    override fun initialize() {
        actions += "initialize"
    }

    override suspend fun process(input: ScheduledEvent) {
        failure?.let { throw it }
        actions += "process"
    }
}
