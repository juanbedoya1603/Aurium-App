package com.proyecto.aurium.data.datasource

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseUserDataSource {

    private val database = FirebaseDatabase.getInstance().getReference("users")

    suspend fun getUser(phoneNumber: String): DataSnapshot {
        return database.child(phoneNumber).get().await()
    }

    suspend fun saveUser(phoneNumber: String, userData: Map<String, Any>) {
        database.child(phoneNumber).setValue(userData).await()
    }

    suspend fun updateUser(phoneNumber: String, updates: Map<String, Any>) {
        database.child(phoneNumber).updateChildren(updates).await()
    }
}