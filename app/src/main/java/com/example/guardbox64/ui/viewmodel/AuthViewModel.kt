package com.example.guardbox64.ui.viewmodel

import android.util.Log
import com.google.firebase.auth.AuthResult
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<FirebaseUser?>()
    val authState: LiveData<FirebaseUser?> = _authState

    fun register(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error durante el registro", e)
                val errorMessage = when (e) {
                    is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil"
                    is FirebaseAuthInvalidCredentialsException -> "El email no es válido"
                    is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con este email"
                    else -> "Error: ${e.message}"
                }
                onFailure(errorMessage)
            }
        }
    }
    fun login(
        email: String,
        password: String,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = auth.currentUser
                saveSession(auth.currentUser?.uid ?: "", context)
                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error durante el inicio de sesión", e)
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Credenciales inválidas"
                    is FirebaseAuthWeakPasswordException -> "La contraseña es incorrecta"
                    else -> "Error: ${e.message}"
                }
                onFailure(errorMessage)
            }
        }
    }
    fun loadSession(context: Context): String? {
        val sharedPref = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("user_id", null)
    }
    private fun signInWithGoogle(
        idToken: String,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        viewModelScope.launch {
            try {
                val result = auth.signInWithCredential(credential).await()
                _authState.value = result.user
                saveSession(result.user?.uid ?: "", context)
                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error durante el inicio de sesión con Google", e)
                onFailure("Error: ${e.message}")
            }
        }
    }
    fun handleGoogleSignInResult(
        data: Intent?,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                signInWithGoogle(idToken, context, onSuccess, onFailure)
            } else {
                onFailure("No se pudo obtener el token de Google")
            }
        } catch (e: ApiException) {
            Log.e("AuthViewModel", "Google sign in failed", e)
            onFailure("Error en el inicio de sesión de Google: ${e.message}")
        }
    }


}
private fun saveSession(userId: String, context: Context) {
    val sharedPref = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString("user_id", userId)
        apply()
    }
}