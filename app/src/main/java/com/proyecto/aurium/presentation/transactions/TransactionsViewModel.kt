package com.proyecto.aurium.presentation.transactions

import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.proyecto.aurium.data.session.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TransactionsViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val usersRef = FirebaseDatabase.getInstance().getReference("users")

    /**
     * Processes the transaction. If isDeposit = true, adds the amount to the current balance.
     * If false, subtracts it. Returns (success, message).
     */
    fun processTransaction(amountStr: String, isDeposit: Boolean, onResult: (Boolean, String) -> Unit) {
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            onResult(false, "Invalid amount. Please enter a value greater than 0.")
            return
        }

        val userId = UserSession.userId
        if (userId.isNullOrEmpty()) {
            onResult(false, "Invalid or expired session.")
            return
        }

        val currentBalance = UserSession.balanceBtc

        if (!isDeposit && amount > currentBalance) {
            onResult(false, "Insufficient balance. Current balance: ₿ ${"%.6f".format(currentBalance)}")
            return
        }

        _isLoading.value = true

        val newBalance = if (isDeposit) {
            currentBalance + amount
        } else {
            currentBalance - amount
        }

        usersRef.child(userId).child("balanceBtc").setValue(newBalance)
            .addOnSuccessListener {
                _isLoading.value = false
                // Update local session as well (though HomeViewModel will do it automatically)
                UserSession.balanceBtc = newBalance
                val txType = if (isDeposit) "Deposit" else "Withdrawal"
                onResult(true, "Successful $txType of ₿ ${"%.6f".format(amount)}")
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                onResult(false, "Error processing transaction: ${exception.message}")
            }
    }

    /**
     * Performs a transfer of BTC from the current user's balance to the destination user's account.
     * Validates amounts, user sessions, recipient phone number, and updates both accounts in Firebase.
     */
    fun transferBtc(amountStr: String, destinationPhone: String, onResult: (Boolean, String) -> Unit) {
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            onResult(false, "Invalid amount. Please enter a value greater than 0.")
            return
        }

        val senderId = UserSession.userId
        val senderPhone = UserSession.phoneNumber
        if (senderId.isNullOrEmpty() || senderPhone.isNullOrEmpty()) {
            onResult(false, "Invalid or expired session.")
            return
        }

        val currentBalance = UserSession.balanceBtc
        if (amount > currentBalance) {
            onResult(false, "Insufficient balance. Current balance: ₿ ${"%.6f".format(currentBalance)}")
            return
        }

        val recipientPhone = destinationPhone.trim()
        if (recipientPhone.isEmpty() || recipientPhone.length != 10 || !recipientPhone.all { it.isDigit() }) {
            onResult(false, "Invalid phone number. It must be exactly 10 digits.")
            return
        }

        if (recipientPhone == senderPhone) {
            onResult(false, "You cannot transfer BTC to your own account.")
            return
        }

        _isLoading.value = true

        // Find the recipient by phone number
        usersRef.orderByChild("phoneNumber").equalTo(recipientPhone).get()
            .addOnSuccessListener { snapshot ->
                val recipientSnapshot = snapshot.children.firstOrNull()
                if (recipientSnapshot == null) {
                    _isLoading.value = false
                    onResult(false, "Recipient phone number not found in our system.")
                    return@addOnSuccessListener
                }

                val recipientId = recipientSnapshot.key
                if (recipientId.isNullOrEmpty()) {
                    _isLoading.value = false
                    onResult(false, "Recipient account is invalid.")
                    return@addOnSuccessListener
                }

                val recipientBalance = when (val raw = recipientSnapshot.child("balanceBtc").value) {
                    is Double -> raw
                    is Long   -> raw.toDouble()
                    is String -> raw.toDoubleOrNull() ?: 0.0
                    else      -> 0.0
                }

                val senderNewBalance = currentBalance - amount
                val recipientNewBalance = recipientBalance + amount

                // Create updates map for atomic operation
                val updates = mapOf(
                    "$senderId/balanceBtc" to senderNewBalance,
                    "$recipientId/balanceBtc" to recipientNewBalance
                )

                usersRef.updateChildren(updates)
                    .addOnSuccessListener {
                        _isLoading.value = false
                        UserSession.balanceBtc = senderNewBalance
                        val recipientName = recipientSnapshot.child("fullName").value?.toString() ?: recipientPhone
                        onResult(true, "Successfully transferred ₿ ${"%.6f".format(amount)} to $recipientName.")
                    }
                    .addOnFailureListener { exception ->
                        _isLoading.value = false
                        onResult(false, "Transaction failed: ${exception.message}")
                    }
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                onResult(false, "Failed to look up recipient: ${exception.message}")
            }
    }
}
