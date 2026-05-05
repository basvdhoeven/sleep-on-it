package com.sleeponit.data.repository

import com.sleeponit.data.local.PurchaseDao
import com.sleeponit.data.local.PurchaseEntity
import com.sleeponit.domain.model.Decision
import com.sleeponit.domain.model.Purchase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseRepository @Inject constructor(private val dao: PurchaseDao) {

    val purchases: Flow<List<Purchase>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun addPurchase(purchase: Purchase): Long = dao.insert(purchase.toEntity())

    suspend fun recordDecision(id: Long, decision: Decision) {
        val entity = dao.getById(id) ?: return
        dao.update(entity.copy(decision = decision.name))
    }

    suspend fun undecide(id: Long) {
        val entity = dao.getById(id) ?: return
        dao.update(entity.copy(decision = null))
    }

    suspend fun updatePurchase(purchase: Purchase) = dao.update(purchase.toEntity())

    suspend fun deletePurchase(purchase: Purchase) = dao.delete(purchase.toEntity())

    suspend fun snoozePurchase(id: Long, additionalMillis: Long) {
        val entity = dao.getById(id) ?: return
        dao.update(entity.copy(sleepUntil = entity.sleepUntil + additionalMillis))
    }

    private fun PurchaseEntity.toDomain() = Purchase(
        id = id, name = name, price = price, notes = notes,
        createdAt = createdAt, sleepUntil = sleepUntil,
        decision = decision?.let { Decision.valueOf(it) }
    )

    private fun Purchase.toEntity() = PurchaseEntity(
        id = id, name = name, price = price, notes = notes,
        createdAt = createdAt, sleepUntil = sleepUntil,
        decision = decision?.name
    )
}
