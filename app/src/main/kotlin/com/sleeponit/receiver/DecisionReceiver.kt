package com.sleeponit.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sleeponit.data.repository.PurchaseRepository
import com.sleeponit.domain.model.Decision
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DecisionReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: PurchaseRepository

    override fun onReceive(context: Context, intent: Intent) {
        val purchaseId = intent.getLongExtra(EXTRA_PURCHASE_ID, -1L).takeIf { it != -1L } ?: return
        val decision = intent.getStringExtra(EXTRA_DECISION)?.let { Decision.valueOf(it) } ?: return

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(purchaseId.toInt())

        val result = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                repository.recordDecision(purchaseId, decision)
            } finally {
                result.finish()
            }
        }
    }

    companion object {
        const val EXTRA_PURCHASE_ID = "purchase_id"
        const val EXTRA_DECISION = "decision"
    }
}
