package com.sleeponit.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleeponit.domain.model.Decision
import com.sleeponit.domain.model.Purchase
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseListScreen(
    onAddClick: () -> Unit,
    viewModel: PurchaseListViewModel = hiltViewModel()
) {
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val now = System.currentTimeMillis()

    val readyToDecide = purchases.filter { it.decision == null && it.sleepUntil <= now }
    val sleeping = purchases.filter { it.decision == null && it.sleepUntil > now }
    val decided = purchases.filter { it.decision != null }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sleep On It") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add purchase")
            }
        }
    ) { padding ->
        if (purchases.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No purchases yet.\nTap + to add one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (readyToDecide.isNotEmpty()) {
                    item { SectionHeader("Ready to Decide 🔔") }
                    items(readyToDecide, key = { it.id }) { purchase ->
                        PurchaseCard(
                            purchase = purchase,
                            onBuy = { viewModel.decide(purchase, Decision.BUY) },
                            onSkip = { viewModel.decide(purchase, Decision.SKIP) },
                            onDelete = { viewModel.delete(purchase) }
                        )
                    }
                }
                if (sleeping.isNotEmpty()) {
                    item { SectionHeader("Sleeping 💤") }
                    items(sleeping, key = { it.id }) { purchase ->
                        PurchaseCard(purchase = purchase, onDelete = { viewModel.delete(purchase) })
                    }
                }
                if (decided.isNotEmpty()) {
                    item { SectionHeader("Decided") }
                    items(decided, key = { it.id }) { purchase ->
                        PurchaseCard(purchase = purchase, onDelete = { viewModel.delete(purchase) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun PurchaseCard(
    purchase: Purchase,
    onBuy: (() -> Unit)? = null,
    onSkip: (() -> Unit)? = null,
    onDelete: () -> Unit
) {
    val now = System.currentTimeMillis()
    val currency = NumberFormat.getCurrencyInstance()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(purchase.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    purchase.price?.let {
                        Text(currency.format(it), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                StatusChip(purchase = purchase, now = now)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
            if (purchase.notes.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    purchase.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onBuy != null && onSkip != null) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onBuy, modifier = Modifier.weight(1f)) { Text("Buy it") }
                    OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f)) { Text("Skip it") }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(purchase: Purchase, now: Long) {
    val (label, color) = when {
        purchase.decision == Decision.BUY -> "Bought" to MaterialTheme.colorScheme.primaryContainer
        purchase.decision == Decision.SKIP -> "Skipped" to MaterialTheme.colorScheme.secondaryContainer
        purchase.sleepUntil <= now -> "Decide!" to MaterialTheme.colorScheme.errorContainer
        else -> formatTimeLeft(purchase.sleepUntil - now) to MaterialTheme.colorScheme.surfaceVariant
    }
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private fun formatTimeLeft(millis: Long): String {
    val days = TimeUnit.MILLISECONDS.toDays(millis)
    val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h left"
        else -> "< 1h left"
    }
}
