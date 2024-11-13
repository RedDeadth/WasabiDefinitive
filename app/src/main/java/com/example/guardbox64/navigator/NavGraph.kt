package com.example.guardbox64.navigator

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.guardbox64.ui.screens.RegisterScreen
import com.example.guardbox64.ui.screens.ConfirmationScreen
import com.example.guardbox64.ui.screens.LockerDetailsScreen
import com.example.guardbox64.ui.screens.LockerListScreen
import com.example.guardbox64.ui.screens.LoginScreen
import com.example.guardbox64.ui.screens.PaymentScreen // Asegúrate de tener PaymentScreen importado
import com.example.guardbox64.ui.viewmodel.LockerViewModel
import com.example.guardbox64.ui.viewmodel.AuthViewModel

@Composable
fun NavGraph(navController: NavHostController, startDestination: String) {
    val lockerViewModel: LockerViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController, authViewModel) }
        composable("register") { RegisterScreen(navController) }
        composable("locker_list") {
            LockerListScreen(
                lockerViewModel = lockerViewModel,
                navController = navController,
                onAddLockerClick = { /* lógica de agregar */ }
            )
        }
        composable("locker_details/{lockerId}") { backStackEntry ->
            val lockerId = backStackEntry.arguments?.getString("lockerId")
            lockerId?.let {
                LockerDetailsScreen(navController, lockerId)
            }
        }
        composable("reservation_confirmation/{lockerId}") {
            ConfirmationScreen(navController) // Ajusta esto según la definición correcta de la función
        }
        composable("payment/{lockerId}") { backStackEntry ->
            val lockerId = backStackEntry.arguments?.getString("lockerId")
            lockerId?.let {
                PaymentScreen(navController, lockerId)
            }
        }
    }
}
