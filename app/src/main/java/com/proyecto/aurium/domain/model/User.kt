package com.proyecto.aurium.domain.model

data class User(
    val fullName: String,
    val documentNumber: String,
    val email: String,
    val phoneNumber: String,
    val pin: String
)