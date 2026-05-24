package com.proyecto.aurium.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.aurium.R
import com.proyecto.aurium.data.repository.FirebaseAuthRepositoryImpl
import com.proyecto.aurium.domain.usecase.LoginUseCase
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase = LoginUseCase(FirebaseAuthRepositoryImpl())
) : ViewModel() {

    fun login(phoneNumber: String, pin: String, onResult: (Boolean, Int) -> Unit) {
        if (phoneNumber.isBlank() || pin.isBlank()) {
            onResult(false, R.string.error_login_failed)
            return
        }

        viewModelScope.launch {
            val result = loginUseCase(phoneNumber, pin)
            onResult(result.first, result.second)
        }
    }
}