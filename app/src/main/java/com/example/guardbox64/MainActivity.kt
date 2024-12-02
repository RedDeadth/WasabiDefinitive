package com.example.guardbox64

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.guardbox64.ui.viewmodel.LockerViewModel
import com.google.firebase.FirebaseApp
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.guardbox64.ui.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.guardbox64.navigator.NavGraph
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.example.guardbox64.ui.theme.AppTheme // Asegúrate de importar tu tema aquí

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var navController: NavHostController
    private lateinit var lockerViewModel: LockerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa Firebase
        FirebaseApp.initializeApp(this)
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        lockerViewModel = ViewModelProvider(this).get(LockerViewModel::class.java)

        // Configuración de Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Debes usar tu client_id de OAuth 2.0
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val userId = authViewModel.loadSession(this) // Cargar la sesión
        val startDestination = if (userId != null) "locker_list" else "login"

        // Aplicar el tema globalmente
        setContent {
            AppTheme {
                navController = rememberNavController() // Crear el controlador de navegación
                NavGraph(navController = navController, startDestination = startDestination)
            }
        }

        // Observar el estado de autenticación
        authViewModel.authState.observe(this, Observer { user ->
            if (user != null) {
                // El usuario ha iniciado sesión, navega a la pantalla de casilleros
                navController.navigate("locker_list") {
                    popUpTo("login") { inclusive = true }
                }
                // Cargar los casilleros después de iniciar sesión
                lockerViewModel.loadLockers()
            } else {
                // El usuario ha cerrado sesión, navega a la pantalla de inicio de sesión
                navController.navigate("login") {
                    popUpTo("locker_list") { inclusive = true }
                }
            }
        })
    }

    fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Manejar el resultado del flujo de Google Sign-In
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            authViewModel.handleGoogleSignInResult(
                data,
                this,
                onSuccess = {
                    // Navegación manejada por el observador de authState
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                },
                onFailure = { errorMessage ->
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    companion object {
        const val RC_SIGN_IN = 9001
    }
}