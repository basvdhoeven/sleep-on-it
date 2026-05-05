package com.sleeponit.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeponit.data.repository.PurchaseRepository
import com.sleeponit.data.repository.UserPreferencesRepository
import com.sleeponit.domain.model.Decision
import com.sleeponit.domain.model.Purchase
import com.sleeponit.worker.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder(val label: String) {
    DATE_ADDED("Date added"),
    NAME("Name"),
    PRICE("Price")
}

@HiltViewModel
class PurchaseListViewModel @Inject constructor(
    private val repository: PurchaseRepository,
    private val reminderScheduler: ReminderScheduler,
    prefs: UserPreferencesRepository
) : ViewModel() {

    val currencyCode = prefs.currencyCode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), "USD"
    )

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_ADDED)
    val sortOrder = _sortOrder

    val purchases = combine(repository.purchases, _sortOrder) { list, order ->
        when (order) {
            SortOrder.DATE_ADDED -> list.sortedByDescending { it.createdAt }
            SortOrder.NAME -> list.sortedBy { it.name.lowercase() }
            SortOrder.PRICE -> list.sortedByDescending { it.price ?: 0.0 }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }

    fun decide(purchase: Purchase, decision: Decision) = viewModelScope.launch {
        repository.recordDecision(purchase.id, decision)
        reminderScheduler.cancel(purchase.id)
    }

    fun delete(purchase: Purchase) = viewModelScope.launch {
        repository.deletePurchase(purchase)
        reminderScheduler.cancel(purchase.id)
    }

    fun snooze(purchase: Purchase, additionalMillis: Long) = viewModelScope.launch {
        repository.snoozePurchase(purchase.id, additionalMillis)
        val newSleepUntil = purchase.sleepUntil + additionalMillis
        reminderScheduler.cancel(purchase.id)
        reminderScheduler.schedule(purchase.id, purchase.name, newSleepUntil)
    }
}
