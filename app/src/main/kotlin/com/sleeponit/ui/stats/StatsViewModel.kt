package com.sleeponit.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeponit.data.repository.PurchaseRepository
import com.sleeponit.data.repository.UserPreferencesRepository
import com.sleeponit.domain.model.Decision
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StatsData(
    val totalSaved: Double,
    val totalSpent: Double,
    val boughtCount: Int,
    val skippedCount: Int,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    repository: PurchaseRepository,
    prefs: UserPreferencesRepository
) : ViewModel() {

    val currencyCode = prefs.currencyCode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), "USD"
    )

    val stats = repository.purchases.map { purchases ->
        val bought = purchases.filter { it.decision == Decision.BUY }
        val skipped = purchases.filter { it.decision == Decision.SKIP }
        StatsData(
            totalSaved = skipped.mapNotNull { it.price }.sum(),
            totalSpent = bought.mapNotNull { it.price }.sum(),
            boughtCount = bought.size,
            skippedCount = skipped.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
