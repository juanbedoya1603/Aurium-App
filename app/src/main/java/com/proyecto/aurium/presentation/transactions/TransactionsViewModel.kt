package com.proyecto.aurium.presentation.transactions

import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.proyecto.aurium.data.session.UserSession
import com.proyecto.aurium.domain.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TransactionsViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val usersRef = FirebaseDatabase.getInstance().getReference("users")
    private val transactionsRef = FirebaseDatabase.getInstance().getReference("transactions")

    fun processTransaction(amountStr: String, isDeposit: Boolean, onResult: (Boolean, String) -> Unit) {
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            onResult(false, "Monto inválido. Ingresa un valor mayor que 0.")
            return
        }

        val userId = UserSession.userId
        if (userId.isNullOrEmpty()) {
            onResult(false, "Sesión inválida o expirada.")
            return
        }

        val currentBalance = UserSession.balanceBtc

        if (!isDeposit && amount > currentBalance) {
            onResult(false, "Saldo insuficiente. Saldo actual: BTC ${"%.6f".format(currentBalance)}")
            return
        }

        _isLoading.value = true

        val newBalance = if (isDeposit) currentBalance + amount else currentBalance - amount
        val txType = if (isDeposit) "DEPOSIT" else "WITHDRAW"

        usersRef.child(userId).child("balanceBtc").setValue(newBalance)
            .addOnSuccessListener {
                UserSession.balanceBtc = newBalance
                saveTransaction(
                    userId = userId,
                    type = txType,
                    amountBtc = amount,
                    counterpartName = "",
                    counterpartPhone = ""
                )

                _isLoading.value = false
                val txLabel = if (isDeposit) "Depósito" else "Retiro"
                onResult(true, "$txLabel realizado con éxito por BTC ${"%.6f".format(amount)}")
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                onResult(false, "Error al procesar la transacción: ${exception.message}")
            }
    }

    fun transferBtc(amountStr: String, destinationPhone: String, onResult: (Boolean, String) -> Unit) {
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            onResult(false, "Monto inválido. Ingresa un valor mayor que 0.")
            return
        }

        val senderId = UserSession.userId
        val senderPhone = UserSession.phoneNumber
        val senderName = UserSession.fullName ?: senderPhone ?: "Usuario"
        if (senderId.isNullOrEmpty() || senderPhone.isNullOrEmpty()) {
            onResult(false, "Sesión inválida o expirada.")
            return
        }

        val currentBalance = UserSession.balanceBtc
        if (amount > currentBalance) {
            onResult(false, "Saldo insuficiente. Saldo actual: BTC ${"%.6f".format(currentBalance)}")
            return
        }

        val recipientPhone = destinationPhone.trim()
        if (recipientPhone.isEmpty() || recipientPhone.length != 10 || !recipientPhone.all { it.isDigit() }) {
            onResult(false, "Número de celular inválido. Debe tener exactamente 10 dígitos.")
            return
        }

        if (recipientPhone == senderPhone) {
            onResult(false, "No puedes transferir BTC a tu propia cuenta.")
            return
        }

        _isLoading.value = true

        usersRef.orderByChild("phoneNumber").equalTo(recipientPhone).get()
            .addOnSuccessListener { snapshot ->
                val recipientSnapshot = snapshot.children.firstOrNull()
                if (recipientSnapshot == null) {
                    _isLoading.value = false
                    onResult(false, "El número de celular del destinatario no fue encontrado.")
                    return@addOnSuccessListener
                }

                val recipientId = recipientSnapshot.key
                if (recipientId.isNullOrEmpty()) {
                    _isLoading.value = false
                    onResult(false, "La cuenta del destinatario no es válida.")
                    return@addOnSuccessListener
                }

                val recipientBalance = when (val raw = recipientSnapshot.child("balanceBtc").value) {
                    is Double -> raw
                    is Long   -> raw.toDouble()
                    is String -> raw.toDoubleOrNull() ?: 0.0
                    else      -> 0.0
                }

                val recipientName = recipientSnapshot.child("fullName").value?.toString() ?: recipientPhone
                val senderNewBalance = currentBalance - amount
                val recipientNewBalance = recipientBalance + amount

                val updates = mapOf(
                    "$senderId/balanceBtc" to senderNewBalance,
                    "$recipientId/balanceBtc" to recipientNewBalance
                )

                usersRef.updateChildren(updates)
                    .addOnSuccessListener {
                        UserSession.balanceBtc = senderNewBalance

                        // Transacción del emisor: TRANSFER_SENT
                        saveTransaction(
                            userId = senderId,
                            type = "TRANSFER_SENT",
                            amountBtc = amount,
                            counterpartName = recipientName,
                            counterpartPhone = recipientPhone
                        )

                        // Transacción del receptor: TRANSFER_RECEIVED
                        saveTransaction(
                            userId = recipientId,
                            type = "TRANSFER_RECEIVED",
                            amountBtc = amount,
                            counterpartName = senderName,
                            counterpartPhone = senderPhone
                        )

                        _isLoading.value = false
                        onResult(true, "Transferencia de BTC ${"%.6f".format(amount)} a $recipientName realizada con éxito.")
                    }
                    .addOnFailureListener { exception ->
                        _isLoading.value = false
                        onResult(false, "La transacción falló: ${exception.message}")
                    }
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                onResult(false, "No se pudo buscar el destinatario: ${exception.message}")
            }
    }

    private fun saveTransaction(
        userId: String,
        type: String,
        amountBtc: Double,
        counterpartName: String,
        counterpartPhone: String
    ) {
        val txId = transactionsRef.child(userId).push().key
            ?: java.util.UUID.randomUUID().toString()

        val transaction = Transaction(
            id = txId,
            type = type,
            amountBtc = amountBtc,
            timestamp = System.currentTimeMillis(),
            counterpartName = counterpartName,
            counterpartPhone = counterpartPhone
        )

        val txMap = mapOf(
            "id" to transaction.id,
            "type" to transaction.type,
            "amountBtc" to transaction.amountBtc,
            "timestamp" to transaction.timestamp,
            "counterpartName" to transaction.counterpartName,
            "counterpartPhone" to transaction.counterpartPhone
        )

        transactionsRef.child(userId).child(txId).setValue(txMap)
    }
}