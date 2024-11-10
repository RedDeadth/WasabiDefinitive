package com.example.guardbox64.model

data class Locker(
    val id: String = "",
    val name: String = "",
    val occupied: Boolean = false,
    val open: Boolean = false,
    val userId: String = "",
    val reservationEndTime: Long? = null,
    val description: String = "",
    val location: String = "",
    val price: Double = 0.0,  // Cambia de String a Double si es un número
    val blocked: Boolean = false,  // Añadir este campo para que coincida con la base de datos
    val sharedWithEmails: List<String> = emptyList(),
)
