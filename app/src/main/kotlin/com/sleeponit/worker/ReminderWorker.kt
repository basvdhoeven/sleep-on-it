package com.sleeponit.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sleeponit.MainActivity
import com.sleeponit.SleepOnItApp
import com.sleeponit.domain.model.Decision
import com.sleeponit.receiver.DecisionReceiver
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getLong(KEY_ID, -1L)
        val name = inputData.getString(KEY_NAME) ?: return Result.failure()

        val openIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openPendingIntent = PendingIntent.getActivity(
            applicationContext, id.toInt(), openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val buyPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            (id * 2).toInt(),
            Intent(applicationContext, DecisionReceiver::class.java).apply {
                putExtra(DecisionReceiver.EXTRA_PURCHASE_ID, id)
                putExtra(DecisionReceiver.EXTRA_DECISION, Decision.BUY.name)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val skipPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            (id * 2 + 1).toInt(),
            Intent(applicationContext, DecisionReceiver::class.java).apply {
                putExtra(DecisionReceiver.EXTRA_PURCHASE_ID, id)
                putExtra(DecisionReceiver.EXTRA_DECISION, Decision.SKIP.name)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, SleepOnItApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Time to decide!")
            .setContentText("Should you buy \"$name\"?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .addAction(0, "Buy it", buyPendingIntent)
            .addAction(0, "Skip it", skipPendingIntent)
            .build()

        (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(id.toInt(), notification)

        return Result.success()
    }

    companion object {
        const val KEY_ID = "purchase_id"
        const val KEY_NAME = "purchase_name"
    }
}
