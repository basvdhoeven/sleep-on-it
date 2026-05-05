package com.sleeponit.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeponit.data.repository.PurchaseRepository
import com.sleeponit.domain.model.Purchase
import com.sleeponit.domain.model.SleepDuration
import com.sleeponit.worker.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPurchaseViewModel @Inject constructor(
    private val repository: PurchaseRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    fun save(name: String, price: Double?, notes: String, duration: SleepDuration) {
        val now = System.currentTimeMillis()
        val purchase = Purchase(
            name = name,
            price = price,
            notes = notes,
            createdAt = now,
            sleepUntil = now + duration.millis
        )
        viewModelScope.launch {
            val id = repository.addPurchase(purchase)
            reminderScheduler.schedule(id, name, purchase.sleepUntil)
        }
    }
}
