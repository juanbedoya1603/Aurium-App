package com.proyecto.aurium.domain.usecase

import com.proyecto.aurium.domain.model.User
import com.proyecto.aurium.domain.repository.UserRepository

class GetUserDataUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(phoneNumber: String): User? {
        return userRepository.getUserByPhone(phoneNumber)
    }
}