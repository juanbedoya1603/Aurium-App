package com.proyecto.aurium.domain.model
data class Transaction(
    val id: String = "",
    val type: String = "",
    val amountBtc: Double = 0.0,
    val timestamp: Long = 0L,
    val counterpartName: String = "",
    val counterpartPhone: String = ""
)