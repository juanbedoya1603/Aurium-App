package com.proyecto.aurium.data.repository

import com.proyecto.aurium.data.datasource.FirebaseUserDataSource
import com.proyecto.aurium.data.session.UserSession
import com.proyecto.aurium.domain.model.User
import com.proyecto.aurium.domain.repository.UserRepository

/**
 * Implementación Firebase de UserRepository.
 * Al obtener el usuario, llena UserSession para que TransactionsViewModel
 * pueda leer userId, phoneNumber y balanceBtc sin hacer otra consulta.
 */
class FirebaseUserRepositoryImpl(
    private val dataSource: FirebaseUserDataSource = FirebaseUserDataSource()
) : UserRepository {

    override suspend fun getUserByPhone(phoneNumber: String): User? {
        return try {
            val snapshot = dataSource.getUserByPhoneNumber(phoneNumber)

            if (snapshot.exists() && snapshot.hasChildren()) {
                val userSnapshot = snapshot.children.first()

                val balanceBtc = when (val raw = userSnapshot.child("balanceBtc").value) {
                    is Double -> raw
                    is Long   -> raw.toDouble()
                    is String -> raw.toDoubleOrNull() ?: 0.0
                    else      -> 0.0
                }

                val user = User(
                    fullName       = userSnapshot.child("fullName").value?.toString() ?: "",
                    documentNumber = userSnapshot.child("documentNumber").value?.toString() ?: "",
                    email          = userSnapshot.child("email").value?.toString() ?: "",
                    phoneNumber    = userSnapshot.child("phoneNumber").value?.toString() ?: "",
                    pin            = userSnapshot.child("pin").value?.toString() ?: "",
                    balanceBtc     = balanceBtc
                )

                // Llenar UserSession para que TransactionsViewModel lo use directamente
                UserSession.userId      = userSnapshot.key
                UserSession.phoneNumber = user.phoneNumber
                UserSession.fullName    = user.fullName
                UserSession.balanceBtc  = balanceBtc

                user
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}