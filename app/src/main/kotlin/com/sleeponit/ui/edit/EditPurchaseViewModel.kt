package com.sleeponit.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeponit.data.repository.PurchaseRepository
import com.sleeponit.data.repository.UserPreferencesRepository
import com.sleeponit.domain.model.Decision
import com.sleeponit.domain.model.currencySymbol
import com.sleeponit.worker.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditPurchaseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PurchaseRepository,
    private val reminderScheduler: ReminderScheduler,
    prefs: UserPreferencesRepository
) : ViewModel() {

    private val purchaseId: Long = checkNotNull(savedStateHandle["purchaseId"])

    val purchase = repository.purchases
        .map { list -> list.find { it.id == purchaseId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val currencySymbol = prefs.currencyCode
        .map { currencySymbol(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "$")

    fun update(name: String, price: Double?, notes: String, sleepUntil: Long) {
        viewModelScope.launch {
            val current = purchase.value ?: return@launch
            repository.updatePurchase(current.copy(name = name, price = price, notes = notes, sleepUntil = sleepUntil))
            reminderScheduler.cancel(purchaseId)
            if (sleepUntil > System.currentTimeMillis() && current.decision == null) {
                reminderScheduler.schedule(purchaseId, name, sleepUntil)
            }
        }
    }

    fun decide(decision: Decision) = viewModelScope.launch {
        repository.recordDecision(purchaseId, decision)
        reminderScheduler.cancel(purchaseId)
    }

    fun undecide() = viewModelScope.launch {
        val current = purchase.value ?: return@launch
        repository.undecide(purchaseId)
        if (current.sleepUntil > System.currentTimeMillis()) {
            reminderScheduler.schedule(purchaseId, current.name, current.sleepUntil)
        }
    }

    fun delete() = viewModelScope.launch {
        val current = purchase.value ?: return@launch
        repository.deletePurchase(current)
        reminderScheduler.cancel(purchaseId)
    }
}
