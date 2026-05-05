package com.sleeponit.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeponit.data.repository.PurchaseRepository
import com.sleeponit.data.repository.UserPreferencesRepository
import com.sleeponit.domain.model.Decision
import com.sleeponit.domain.model.Purchase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchaseListViewModel @Inject constructor(
    private val repository: PurchaseRepository,
    prefs: UserPreferencesRepository
) : ViewModel() {

    val currencyCode = prefs.currencyCode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        "USD"
    )

    val purchases = repository.purchases.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun decide(purchase: Purchase, decision: Decision) = viewModelScope.launch {
        repository.recordDecision(purchase.id, decision)
    }

    fun delete(purchase: Purchase) = viewModelScope.launch {
        repository.deletePurchase(purchase)
    }
}
