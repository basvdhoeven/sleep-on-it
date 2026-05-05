package com.sleeponit.domain.model

data class AppCurrency(val code: String, val symbol: String, val name: String)

val CURRENCIES = listOf(
    AppCurrency("USD", "$",   "US Dollar"),
    AppCurrency("EUR", "€",   "Euro"),
    AppCurrency("GBP", "£",   "British Pound"),
    AppCurrency("JPY", "¥",   "Japanese Yen"),
    AppCurrency("CAD", "CA$", "Canadian Dollar"),
    AppCurrency("AUD", "A$",  "Australian Dollar"),
    AppCurrency("CHF", "Fr",  "Swiss Franc"),
    AppCurrency("CNY", "¥",   "Chinese Yuan"),
    AppCurrency("INR", "₹",   "Indian Rupee"),
    AppCurrency("SEK", "kr",  "Swedish Krona"),
    AppCurrency("NOK", "kr",  "Norwegian Krone"),
    AppCurrency("DKK", "kr",  "Danish Krone"),
    AppCurrency("BRL", "R$",  "Brazilian Real"),
    AppCurrency("MXN", "MX$", "Mexican Peso"),
    AppCurrency("SGD", "S$",  "Singapore Dollar"),
    AppCurrency("HKD", "HK$", "Hong Kong Dollar"),
    AppCurrency("NZD", "NZ$", "New Zealand Dollar"),
    AppCurrency("KRW", "₩",   "South Korean Won"),
    AppCurrency("ZAR", "R",   "South African Rand"),
    AppCurrency("TRY", "₺",   "Turkish Lira"),
    AppCurrency("PLN", "zł",  "Polish Zloty"),
    AppCurrency("CZK", "Kč",  "Czech Koruna"),
)

fun currencySymbol(code: String): String = CURRENCIES.find { it.code == code }?.symbol ?: code
