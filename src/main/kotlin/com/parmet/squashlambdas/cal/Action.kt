package com.parmet.squashlambdas.cal

import com.parmet.squashlambdas.activity.Activity

sealed class Action(
    @Transient
    internal val handle: (Activity, EventManager) -> Unit
) {
    object Create : Action({ activity, manager -> manager.create(activity) })

    object Update : Action({ activity, manager -> manager.update(activity) })

    object Delete : Action({ activity, manager -> manager.delete(activity) })

    object None : Action({ _, _ -> })

    override fun toString() = this::class.java.simpleName

    companion object {
        private val CREATION =
            listOf(
                "A reservation including you has been made",
                "You've joined a reservation",
                "You have been added to the activity"
            )

        private val UPDATING =
            listOf(
                "has cancelled out of reservation",
                "has been removed",
                "has joined your reservation"
            )

        private val DELETION =
            listOf(
                "You've been removed from a reservation",
                "has been successfully cancelled",
                "has cancelled your reservation",
                "A reservation including you has been cancelled",
                "You have successfully cancelled"
            )

        private val NO_ACTION =
            listOf(
                "This is a reminder",
                "Here are your scores recorded",
                "has re-confirmed a reservation made"
            )

        fun parseFromSubject(body: String): Action {
            return when {
                CREATION.containsMatch(body) -> Create
                UPDATING.containsMatch(body) -> Update
                DELETION.containsMatch(body) -> Delete
                NO_ACTION.containsMatch(body) -> None
                else -> throw IllegalArgumentException("Unable to parse action from $body")
            }
        }

        private fun List<String>.containsMatch(body: String) = any { body.contains(it) }
    }
}
