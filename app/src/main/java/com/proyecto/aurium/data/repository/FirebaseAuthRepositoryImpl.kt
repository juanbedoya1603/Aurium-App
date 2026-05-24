package com.proyecto.aurium.data.repository

import android.util.Base64
import com.proyecto.aurium.R
import com.proyecto.aurium.data.datasource.FirebaseUserDataSource
import com.proyecto.aurium.domain.model.User
import com.proyecto.aurium.domain.repository.AuthRepository
import java.security.MessageDigest
import java.security.SecureRandom
import java.net.UnknownHostException
import java.net.ConnectException

class FirebaseAuthRepositoryImpl(
    private val dataSource: FirebaseUserDataSource = FirebaseUserDataSource()
) : AuthRepository {

    override suspend fun login(phoneNumber: String, pin: String): Pair<Boolean, Int> {
        return try {
            val dataSnapshot = dataSource.getUser(phoneNumber)

            if (dataSnapshot.exists()) {
                val blockedUntil = dataSnapshot.child("blockedUntil").value as? Long ?: 0L
                if (System.currentTimeMillis() < blockedUntil) {
                    return Pair(false, R.string.error_account_blocked)
                }

                val dbPin = dataSnapshot.child("pin").value.toString()
                val dbSalt = dataSnapshot.child("salt").value.toString()
                val hashedInputPin = hashPin(pin, dbSalt)

                if (dbPin == hashedInputPin) {
                    val updates = mapOf<String, Any>(
                        "failedAttempts" to 0,
                        "blockedUntil" to 0L
                    )
                    dataSource.updateUser(phoneNumber, updates)
                    Pair(true, 0)
                } else {
                    val currentAttempts = (dataSnapshot.child("failedAttempts").value as? Long ?: 0L) + 1
                    val updates = mutableMapOf<String, Any>("failedAttempts" to currentAttempts)

                    val errorMessage = if (currentAttempts >= 3) {
                        updates["blockedUntil"] = System.currentTimeMillis() + (15 * 60 * 1000)
                        updates["failedAttempts"] = 0
                        R.string.error_account_blocked
                    } else {
                        R.string.error_login_failed
                    }

                    dataSource.updateUser(phoneNumber, updates)
                    Pair(false, errorMessage)
                }
            } else {
                Pair(false, R.string.error_login_failed)
            }
        } catch (e: UnknownHostException) {
            Pair(false, R.string.error_no_internet)
        } catch (e: ConnectException) {
            Pair(false, R.string.error_no_internet)
        } catch (e: Exception) {
            if (e.message?.contains("network", ignoreCase = true) == true ||
                e.cause is UnknownHostException || e.cause is ConnectException) {
                Pair(false, R.string.error_no_internet)
            } else {
                Pair(false, R.string.error_login_failed)
            }
        }
    }

    override suspend fun register(user: User): Pair<Boolean, Int> {
        return try {
            val existingUser = dataSource.getUser(user.phoneNumber)
            if (existingUser.exists()) {
                return Pair(false, R.string.error_user_exists)
            }

            val salt = generateSalt()
            val hashedPin = hashPin(user.pin, salt)

            val userData = mapOf<String, Any>(
                "fullName" to user.fullName,
                "documentNumber" to user.documentNumber,
                "email" to user.email,
                "pin" to hashedPin,
                "salt" to salt,
                "failedAttempts" to 0,
                "blockedUntil" to 0L
            )

            dataSource.saveUser(user.phoneNumber, userData)
            Pair(true, R.string.register_success_message)

        } catch (e: UnknownHostException) {
            Pair(false, R.string.error_no_internet)
        } catch (e: ConnectException) {
            Pair(false, R.string.error_no_internet)
        } catch (e: Exception) {
            if (e.message?.contains("network", ignoreCase = true) == true ||
                e.cause is UnknownHostException || e.cause is ConnectException) {
                Pair(false, R.string.error_no_internet)
            } else {
                Pair(false, R.string.error_register_failed)
            }
        }
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return Base64.encodeToString(saltBytes, Base64.NO_WRAP)
    }

    private fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray())
        val bytes = md.digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}