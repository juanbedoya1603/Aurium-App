package com.proyecto.aurium.domain.repository

import com.proyecto.aurium.domain.model.User

interface AuthRepository {
    suspend fun login(phoneNumber: String, pin: String): Pair<Boolean, Int>
    suspend fun register(user: User): Pair<Boolean, Int>
}