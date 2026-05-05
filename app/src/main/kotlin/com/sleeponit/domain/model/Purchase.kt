package com.sleeponit.domain.model

data class Purchase(
    val id: Long = 0,
    val name: String,
    val price: Double? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val sleepUntil: Long,
    val decision: Decision? = null
)

enum class Decision { BUY, SKIP }

enum class SleepDuration(val label: String, val millis: Long) {
    ONE_DAY("1 day", 24 * 60 * 60_000L),
    THREE_DAYS("3 days", 3 * 24 * 60 * 60_000L),
    ONE_WEEK("1 week", 7 * 24 * 60 * 60_000L),
    TWO_WEEKS("2 weeks", 14 * 24 * 60 * 60_000L),
    ONE_MONTH("1 month", 30L * 24 * 60 * 60_000L),
}
