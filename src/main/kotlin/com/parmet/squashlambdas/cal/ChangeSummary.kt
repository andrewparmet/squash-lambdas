package com.parmet.squashlambdas.cal

import com.parmet.squashlambdas.activity.Activity
import com.parmet.squashlambdas.email.EmailData

data class ChangeSummary(
    private val action: Action,
    private val activity: Activity
) {
    fun process(eventManager: EventManager) =
        action.handle(activity, eventManager)

    fun summary() =
        activity.summary()

    companion object {
        fun fromEmail(email: EmailData): ChangeSummary? {
            val action = Action.parseFromSubject(email.body)

            return if (action == Action.None) {
                null
            } else {
                ChangeSummary(action, Activity.fromEmailData(email))
            }
        }
    }
}
