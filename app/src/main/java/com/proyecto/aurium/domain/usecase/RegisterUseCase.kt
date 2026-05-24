package com.proyecto.aurium.domain.usecase

import com.proyecto.aurium.domain.model.User
import com.proyecto.aurium.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(user: User): Pair<Boolean, Int> {
        return repository.register(user)
    }
}