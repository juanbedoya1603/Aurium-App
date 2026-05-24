package com.proyecto.aurium.data.datasource

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseUserDataSource {

    private val database = FirebaseDatabase.getInstance().getReference("users")

    suspend fun getUserByPhoneNumber(phoneNumber: String): DataSnapshot {
        return database.orderByChild("phoneNumber").equalTo(phoneNumber).get().await()
    }

    fun generateUserId(): String {
        return database.push().key ?: java.util.UUID.randomUUID().toString()
    }

    suspend fun saveUser(userId: String, userData: Map<String, Any>) {
        database.child(userId).setValue(userData).await()
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>) {
        database.child(userId).updateChildren(updates).await()
    }
}