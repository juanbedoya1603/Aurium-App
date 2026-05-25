package com.proyecto.aurium.domain.repository

import com.proyecto.aurium.domain.model.User

interface UserRepository {
    suspend fun getUserByPhone(phoneNumber: String): User?
}