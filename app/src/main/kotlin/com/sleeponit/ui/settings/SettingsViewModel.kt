package com.sleeponit.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeponit.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    val currencyCode = prefs.currencyCode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        "USD"
    )

    fun setCurrency(code: String) = viewModelScope.launch {
        prefs.setCurrencyCode(code)
    }
}
