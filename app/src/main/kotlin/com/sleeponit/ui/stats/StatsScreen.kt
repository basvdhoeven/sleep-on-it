package com.sleeponit.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleeponit.domain.model.currencySymbol
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val currencyCode by viewModel.currencyCode.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (stats == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val s = stats!!
        val symbol = currencySymbol(currencyCode)
        val totalDecided = s.boughtCount + s.skippedCount

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Money saved by skipping",
                value = formatAmount(s.totalSaved, symbol),
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
            StatCard(
                label = "Money spent on buys",
                value = formatAmount(s.totalSpent, symbol),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Bought",
                    value = "${s.boughtCount}",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
                StatCard(
                    label = "Skipped",
                    value = "${s.skippedCount}",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }
            if (totalDecided > 0) {
                StatCard(
                    label = "Buy rate",
                    value = "${s.boughtCount * 100 / totalDecided}%",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatAmount(amount: Double, symbol: String): String {
    val nf = NumberFormat.getNumberInstance().apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "$symbol${nf.format(amount)}"
}
