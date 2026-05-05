package com.sleeponit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Double?,
    val notes: String,
    val createdAt: Long,
    val sleepUntil: Long,
    val decision: String? // "BUY" or "SKIP", null while pending
)
