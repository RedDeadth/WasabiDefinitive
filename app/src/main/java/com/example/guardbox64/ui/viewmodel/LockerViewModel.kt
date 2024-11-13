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
                                                "reservationEndTime" to null,

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

        // Verificación directa de que el correo existe en Auth
        auth.fetchSignInMethodsForEmail(newEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods

                    // Acceso directo al casillero sin duplicar el nodo `lockers`
                    val updatedEmailsRef = database.child(lockerId).child("sharedWithEmails")
                    updatedEmailsRef.runTransaction(object : Transaction.Handler {
                        override fun doTransaction(mutableData: MutableData): Transaction.Result {
                            val currentEmails = mutableData.getValue<List<String>>() ?: emptyList()
                            if (currentEmails.contains(newEmail)) {
                                onFailure("Este correo ya tiene acceso al casillero")
                                return Transaction.abort()
                            }

                            // Añadir nuevo correo y actualizar en Firebase
                            val updatedEmails = currentEmails.toMutableList().apply { add(newEmail) }
                            mutableData.value = updatedEmails
                            return Transaction.success(mutableData)
                        }

                        override fun onComplete(
                            error: DatabaseError?,
                            committed: Boolean,
                            snapshot: DataSnapshot?
                        ) {
                            if (committed) {
                                onSuccess()
                            } else {
                                onFailure(error?.message ?: "Error desconocido al compartir el acceso")
                            }
                        }
                    })
                } else {
                    onFailure("Error al verificar el correo electrónico: ${task.exception?.message}")
                }
            }
    }


}


