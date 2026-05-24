package com.proyecto.aurium.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.aurium.R
import com.proyecto.aurium.data.repository.FirebaseAuthRepositoryImpl
import com.proyecto.aurium.domain.model.User
import com.proyecto.aurium.domain.usecase.RegisterUseCase
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase = RegisterUseCase(FirebaseAuthRepositoryImpl())
) : ViewModel() {

    fun register(
        fullName: String,
        documentNumber: String,
        email: String,
        phoneNumber: String,
        pin: String,
        confirmPin: String,
        onResult: (Boolean, Int) -> Unit
    ) {
        if (fullName.isBlank() || documentNumber.isBlank() || email.isBlank() ||
            phoneNumber.isBlank() || pin.isBlank()) {
            onResult(false, R.string.error_register_failed)
            return
        }

        if (documentNumber.length !in 8..10) {
            onResult(false, R.string.error_invalid_document)
            return
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        if (!email.matches(emailRegex)) {
            onResult(false, R.string.error_invalid_email)
            return
        }

        if (phoneNumber.length != 10) {
            onResult(false, R.string.error_invalid_phone)
            return
        }

        if (pin.length != 4) {
            onResult(false, R.string.error_register_failed)
            return
        }

        if (pin != confirmPin) {
            onResult(false, R.string.error_passwords_match)
            return
        }

        val user = User(
            fullName = fullName,
            documentNumber = documentNumber,
            email = email,
            phoneNumber = phoneNumber,
            pin = pin
        )

        viewModelScope.launch {
            val result = registerUseCase(user)
            onResult(result.first, result.second)
        }
    }
}