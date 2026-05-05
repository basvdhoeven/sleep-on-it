package com.sleeponit.worker

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun schedule(purchaseId: Long, name: String, sleepUntil: Long) {
        val delay = sleepUntil - System.currentTimeMillis()
        if (delay <= 0) return

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    ReminderWorker.KEY_ID to purchaseId,
                    ReminderWorker.KEY_NAME to name
                )
            )
            .addTag(tagFor(purchaseId))
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancel(purchaseId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag(tagFor(purchaseId))
    }

    private fun tagFor(id: Long) = "reminder_$id"
}
