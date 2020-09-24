package uk.nhs.nhsx.covid19.android.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import uk.nhs.nhsx.covid19.android.app.appComponent
import uk.nhs.nhsx.covid19.android.app.notifications.NotificationProvider
import uk.nhs.nhsx.covid19.android.app.status.ResumeContactTracingNotificationTimeProvider
import javax.inject.Inject

class ExposureNotificationReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationProvider: NotificationProvider

    @Inject
    lateinit var resumeContactTracingNotificationTimeProvider: ResumeContactTracingNotificationTimeProvider

    override fun onReceive(context: Context, intent: Intent) {
        context.appComponent.inject(this)
        notificationProvider.showExposureNotificationReminder()
        resumeContactTracingNotificationTimeProvider.value = null
    }
}
