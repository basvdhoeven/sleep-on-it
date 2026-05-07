package com.sleeponit.ui.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleeponit.domain.model.Decision
import com.sleeponit.domain.model.Purchase
import com.sleeponit.domain.model.currencySymbol
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PurchaseListScreen(
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onEditClick: (Purchase) -> Unit,
    onStatsClick: () -> Unit,
    viewModel: PurchaseListViewModel = hiltViewModel()
) {
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val currencyCode by viewModel.currencyCode.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val totalSaved by viewModel.totalSaved.collectAsStateWithLifecycle()

    val now = System.currentTimeMillis()
    var pendingDelete by remember { mutableStateOf<Purchase?>(null) }
    var pendingSnooze by remember { mutableStateOf<Purchase?>(null) }

    val readyToDecide = purchases.filter { it.decision == null && it.sleepUntil <= now }
    val sleeping = purchases.filter { it.decision == null && it.sleepUntil > now }
    val decided = purchases.filter { it.decision != null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sleep On It \uD83D\uDE34") },
                actions = {
                    IconButton(onClick = onStatsClick) {
                        Icon(Icons.Filled.BarChart, contentDescription = "Statistics")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add purchase")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            stickyHeader {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SortOrder.entries.forEach { order ->
                        FilterChip(
                            selected = sortOrder == order,
                            onClick = { viewModel.setSortOrder(order) },
                            label = { Text(order.label) }
                        )
                    }
                }
            }
            if (readyToDecide.isNotEmpty()) {
                item { SectionHeader("Ready to Decide 🔔") }
                items(readyToDecide, key = { it.id }) { purchase ->
                    SwipeToDeleteCard(onDelete = { pendingDelete = purchase }) {
                        PurchaseCard(
                            purchase = purchase,
                            currencyCode = currencyCode,
                            onBuy = { viewModel.decide(purchase, Decision.BUY) },
                            onSkip = { viewModel.decide(purchase, Decision.SKIP) },
                            onSnooze = { pendingSnooze = purchase },
                            onEdit = { onEditClick(purchase) },
                            onDelete = { pendingDelete = purchase }
                        )
                    }
                }
            }
            if (sleeping.isNotEmpty()) {
                item { SectionHeader("Sleeping 💤") }
                items(sleeping, key = { it.id }) { purchase ->
                    SwipeToDeleteCard(onDelete = { pendingDelete = purchase }) {
                        PurchaseCard(
                            purchase = purchase,
                            currencyCode = currencyCode,
                            onEdit = { onEditClick(purchase) },
                            onDelete = { pendingDelete = purchase }
                        )
                    }
                }
            }
            if (decided.isNotEmpty()) {
                item { SectionHeader("Decided") }
                items(decided, key = { it.id }) { purchase ->
                    SwipeToDeleteCard(onDelete = { pendingDelete = purchase }) {
                        PurchaseCard(
                            purchase = purchase,
                            currencyCode = currencyCode,
                            onEdit = { onEditClick(purchase) },
                            onDelete = { pendingDelete = purchase }
                        )
                    }
                }
            }
        }
    }


    pendingDelete?.let { purchase ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete \"${purchase.name}\"?") },
            text = { Text("This will permanently remove it from your list.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(purchase)
                    pendingDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    pendingSnooze?.let { purchase ->
        AlertDialog(
            onDismissRequest = { pendingSnooze = null },
            title = { Text("Snooze reminder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        "1 day" to TimeUnit.DAYS.toMillis(1),
                        "3 days" to TimeUnit.DAYS.toMillis(3),
                        "1 week" to TimeUnit.DAYS.toMillis(7)
                    ).forEach { (label, millis) ->
                        TextButton(
                            onClick = {
                                viewModel.snooze(purchase, millis)
                                pendingSnooze = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(label) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { pendingSnooze = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteCard(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onDelete()
            false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.errorContainer else Color.Transparent,
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier.fillMaxSize().background(color).padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        content()
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun PurchaseCard(
    purchase: Purchase,
    currencyCode: String,
    onBuy: (() -> Unit)? = null,
    onSkip: (() -> Unit)? = null,
    onSnooze: (() -> Unit)? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val now = System.currentTimeMillis()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        purchase.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    purchase.price?.let {
                        Text(formatPrice(it, currencyCode), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                StatusChip(purchase = purchase, now = now)
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
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
                if (onSnooze != null) {
                    TextButton(
                        onClick = onSnooze,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Snooze reminder")
                    }
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
    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private fun formatPrice(amount: Double, currencyCode: String): String {
    val symbol = currencySymbol(currencyCode)
    val nf = NumberFormat.getNumberInstance().apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }
    return "$symbol${nf.format(amount)}"
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
