package com.proyecto.aurium.data.repository

import com.proyecto.aurium.R
import com.proyecto.aurium.data.datasource.FirebaseUserDataSource
import com.proyecto.aurium.domain.model.User
import com.proyecto.aurium.domain.repository.AuthRepository
import java.security.MessageDigest

class FirebaseAuthRepositoryImpl(
    private val dataSource: FirebaseUserDataSource = FirebaseUserDataSource()
) : AuthRepository {

    override suspend fun login(phoneNumber: String, pin: String): Pair<Boolean, Int> {
        return try {
            val dataSnapshot = dataSource.getUser(phoneNumber)

            if (dataSnapshot.exists()) {
                val dbPin = dataSnapshot.child("pin").value.toString()
                val hashedInputPin = hashPin(pin)

                if (dbPin == hashedInputPin) {
                    Pair(true, 0)
                } else {
                    Pair(false, R.string.error_login_failed)
                }
            } else {
                Pair(false, R.string.error_login_failed)
            }
        } catch (e: Exception) {
            Pair(false, R.string.error_login_failed)
        }
    }

    override suspend fun register(user: User): Pair<Boolean, Int> {
        return try {
            val hashedPin = hashPin(user.pin)
            val userData = mapOf(
                "fullName" to user.fullName,
                "documentNumber" to user.documentNumber,
                "email" to user.email,
                "pin" to hashedPin
            )

            dataSource.saveUser(user.phoneNumber, userData)
            Pair(true, R.string.register_success_message)

        } catch (e: Exception) {
            Pair(false, R.string.error_register_failed)
        }
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}