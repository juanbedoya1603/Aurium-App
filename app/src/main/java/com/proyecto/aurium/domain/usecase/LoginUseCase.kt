package com.proyecto.aurium.domain.usecase

import com.proyecto.aurium.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(phoneNumber: String, pin: String): Pair<Boolean, Int> {
        return repository.login(phoneNumber, pin)
    }
}