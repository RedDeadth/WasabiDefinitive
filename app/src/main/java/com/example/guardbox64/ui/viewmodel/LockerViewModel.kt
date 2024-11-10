package com.example.guardbox64.ui.viewmodel

import android.os.Looper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.example.guardbox64.model.Locker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import android.os.Handler
import androidx.compose.runtime.remember
import com.google.firebase.auth.FirebaseAuth

class LockerViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("lockers")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _lockers = MutableLiveData<List<Locker>>(emptyList())
    val lockers: LiveData<List<Locker>> = _lockers

    init {
        // Observar el estado de autenticación de Firebase
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                loadLockers()
            }
        }
    }

    init {
        // Observar el estado de autenticación de Firebase
        auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                loadLockers()
            }
        }
    }
    fun reserveLocker(
        lockerId: String,
        userId: String,
        reservationEndTime: Long?,  // Cambiar Long a Long?
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {


        val lockerRef = database.child(lockerId)
        val updates = mapOf<String, Any?>(  // Cambiar Any a Any?
            "occupied" to true,
            "userId" to userId,
            "reservationEndTime" to reservationEndTime // Ahora puede ser null
        )

        // Actualiza los campos ocupados y userId en una sola llamada
        lockerRef.updateChildren(updates)
            .addOnSuccessListener {
                if (reservationEndTime != null) {  // Solo iniciar el temporizador si reservationEndTime no es null
                    startReservationTimer(lockerId, reservationEndTime)  // Agrega esto
                }
                onSuccess() // Notificar éxito
            }
            .addOnFailureListener { e ->
                onFailure("Error reservando el casillero: ${e.message}")
            }
    }


    fun loadLockers() {
        val user = auth.currentUser
        if (user != null) {
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lockerList = mutableListOf<Locker>()
                    val lockerIds = mutableSetOf<String>()
                    val currentTime = System.currentTimeMillis()

                    for (lockerSnapshot in snapshot.children) {
                        try {
                            val locker = lockerSnapshot.getValue(Locker::class.java)
                                ?.copy(id = lockerSnapshot.key ?: "")
                            if (locker != null && locker.id !in lockerIds) {
                                // Verifica si la reserva ha expirado
                                if (locker.reservationEndTime != null) {
                                    val reservationEndTimeString = locker.reservationEndTime.toString()
                                    val reservationEndTime = reservationEndTimeString.toLongOrNull()
                                    if (reservationEndTime != null && reservationEndTime <= currentTime) {
                                        // Si la reserva ha expirado, actualiza el casillero a no ocupado
                                        synchronized(this) {
                                            val lockerRef = database.child(locker.id)
                                            val updates = mapOf(
                                                "occupied" to false,
                                                "userId" to "",
                                                "reservationEndTime" to null
                                            )
                                            lockerRef.updateChildren(updates)
                                                .addOnSuccessListener {
                                                    Log.d("Firebase", "Casillero ${locker.id} actualizado correctamente.")
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("Firebase", "Error al actualizar casillero ${locker.id}: ${e.message}")
                                                }
                                        }
                                    }
                                }

                                // Agregar el casillero a la lista
                                lockerList.add(locker)
                                lockerIds.add(locker.id)
                            }
                        } catch (e: Exception) {
                            // Manejo de excepciones durante la deserialización
                            Log.e("LockerViewModel", "Error deserializing Locker: $e")
                        }
                    }

                    // Actualizar el LiveData con los casilleros cargados (incluyendo los expirados)
                    _lockers.value = lockerList
                    Log.d("Firebase", "Casilleros cargados correctamente.")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al cargar casilleros: ${error.message}")
                    _lockers.value = emptyList() // Retornar lista vacía en caso de error
                }
            })
        } else {
            Log.e("Firebase", "Usuario no autenticado")
        }
    }
    fun updateLockerOpenState(lockerId: String, isOpen: Boolean) {
        val lockerRef = database.child(lockerId)
        lockerRef.child("open").setValue(isOpen)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    android.util.Log.d("Firebase", "Estado de apertura actualizado a: $isOpen")
                    // Aquí podrías notificar a través de LiveData si es necesario
                } else {
                    android.util.Log.e("Firebase", "Error al actualizar estado de apertura: ${task.exception?.message}")
                    // Maneja el error de forma adecuada, como notificar al usuario
                }
            }
    }
    private fun startReservationTimer(lockerId: String, reservationEndTime: Long) {
        val currentTime = System.currentTimeMillis()

        if (currentTime < reservationEndTime) {
            val delayInMillis = reservationEndTime - currentTime
            Handler(Looper.getMainLooper()).postDelayed({
                expireReservation(lockerId)  // Expirar la reserva cuando el tiempo finaliza
            }, delayInMillis)
        }
    }
    private fun expireReservation(lockerId: String) {
        val lockerRef = database.child(lockerId)
        val updates = mapOf<String, Any>(
            "occupied" to false,
            "userId" to "",


        )

        lockerRef.updateChildren(updates)
            .addOnSuccessListener {
                android.util.Log.d("Firebase", "Reserva del casillero expirada correctamente.")
            }
            .addOnFailureListener { e:Exception ->
                android.util.Log.e("Firebase", "Error al expirar reserva del casillero: ${e.message}")
            }
    }
    fun endReservation(lockerId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val lockerRef = database.child(lockerId)
        val updates = mapOf<String, Any>(
            "occupied" to false,
            "userId" to "",

        )


        lockerRef.updateChildren(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Error desconocido") }
    }
    fun shareLockerAccess(
        lockerId: String,
        newEmail: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            onFailure("Por favor ingresa un correo electrónico válido")
            return
        }

        Log.d("LockerViewModel", "Iniciando verificación en Auth para: $newEmail")

        // Primero intentamos buscar el usuario por email en Auth
        auth.fetchSignInMethodsForEmail(newEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    Log.d("LockerViewModel", "Métodos de inicio de sesión encontrados: $signInMethods")

                    // Verificar si el usuario actual tiene permisos para compartir
                    val currentUser = auth.currentUser
                    if (currentUser == null) {
                        onFailure("Debes iniciar sesión para compartir acceso")
                        return@addOnCompleteListener
                    }

                    // Procedemos a actualizar el locker incluso si no encontramos métodos de inicio de sesión
                    // Esto permite compartir con usuarios que podrían registrarse más tarde
                    database.child("lockers")
                        .child(lockerId)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            try {
                                val currentLocker = snapshot.getValue(Locker::class.java)
                                Log.d("LockerViewModel", "Locker actual: $currentLocker")

                                if (currentLocker == null) {
                                    onFailure("No se pudo encontrar el casillero")
                                    return@addOnSuccessListener
                                }

                                // Verificar si el usuario actual es el dueño del locker
                                if (currentLocker.userId != currentUser.uid) {
                                    onFailure("No tienes permisos para compartir este casillero")
                                    return@addOnSuccessListener
                                }

                                // Verificar si el correo ya existe
                                val updatedEmails = currentLocker.sharedWithEmails.toMutableList()
                                if (updatedEmails.contains(newEmail)) {
                                    onFailure("Este correo ya tiene acceso al casillero")
                                    return@addOnSuccessListener
                                }

                                // Añadir nuevo correo
                                updatedEmails.add(newEmail)

                                // Actualizar en Firebase
                                database.child("lockers")
                                    .child(lockerId)
                                    .child("sharedWithEmails")
                                    .setValue(updatedEmails)
                                    .addOnSuccessListener {
                                        Log.d("LockerViewModel", "Acceso compartido exitosamente con: $newEmail")
                                        onSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("LockerViewModel", "Error actualizando sharedWithEmails", e)
                                        onFailure("Error al actualizar la lista de accesos: ${e.message}")
                                    }

                            } catch (e: Exception) {
                                Log.e("LockerViewModel", "Error procesando datos del casillero", e)
                                onFailure("Error procesando datos del casillero: ${e.message}")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("LockerViewModel", "Error obteniendo datos del casillero", e)
                            onFailure("Error al obtener información del casillero: ${e.message}")
                        }
                } else {
                    Log.e("LockerViewModel", "Error verificando en Auth", task.exception)
                    onFailure("Error al verificar el correo electrónico: ${task.exception?.message}")
                }
            }
    }
    private fun updateLockerSharedAccess(
        lockerId: String,
        newEmail: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        Log.d("LockerViewModel", "Iniciando updateLockerSharedAccess para email: $newEmail")

        database.child(lockerId).get()
            .addOnSuccessListener { snapshot ->
                try {
                    val currentLocker = snapshot.getValue(Locker::class.java)
                    Log.d("LockerViewModel", "Locker actual: $currentLocker")
                    Log.d("LockerViewModel", "Emails compartidos actuales: ${currentLocker?.sharedWithEmails}")

                    if (currentLocker == null) {
                        onFailure("No se pudo encontrar el casillero")
                        return@addOnSuccessListener
                    }

                    // Crear nueva lista de correos
                    val updatedEmails = currentLocker.sharedWithEmails.toMutableList()

                    // Verificar si el correo ya existe
                    if (updatedEmails.contains(newEmail)) {
                        Log.d("LockerViewModel", "Email ya existe en la lista: $newEmail")
                        onFailure("Este correo ya tiene acceso al casillero")
                        return@addOnSuccessListener
                    }

                    // Añadir nuevo correo
                    updatedEmails.add(newEmail)
                    Log.d("LockerViewModel", "Nueva lista de emails: $updatedEmails")

                    // Actualizar en Firebase
                    database.child(lockerId)
                        .child("sharedWithEmails")
                        .setValue(updatedEmails)
                        .addOnSuccessListener {
                            Log.d("LockerViewModel", "Acceso compartido exitosamente con: $newEmail")
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.e("LockerViewModel", "Error actualizando sharedWithEmails", e)
                            onFailure("Error al actualizar la lista de accesos: ${e.message}")
                        }

                } catch (e: Exception) {
                    Log.e("LockerViewModel", "Error procesando datos del casillero", e)
                    onFailure("Error procesando datos del casillero: ${e.message}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("LockerViewModel", "Error obteniendo datos del casillero", e)
                onFailure("Error al obtener información del casillero: ${e.message}")
            }
    }

    fun removeSharedAccess(
        lockerId: String,
        emailToRemove: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        database.child(lockerId).get().addOnSuccessListener { snapshot ->
            val currentLocker = snapshot.getValue(Locker::class.java)
            val updatedEmails = (currentLocker?.sharedWithEmails ?: emptyList()).toMutableList()

            if (updatedEmails.remove(emailToRemove)) {
                database.child(lockerId)
                    .child("sharedWithEmails")
                    .setValue(updatedEmails)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Error desconocido al remover el acceso")
                    }
            } else {
                onFailure("El correo no se encontraba en la lista de accesos compartidos")
            }
        }.addOnFailureListener { e ->
            onFailure(e.message ?: "Error al obtener información del casillero")
        }
    }

}


