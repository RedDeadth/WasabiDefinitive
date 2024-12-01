package com.example.guardbox64.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun PaymentScreen(
    navController: NavController,
    lockerId: String? = null // Para poder pasar el ID del casillero si es necesario
) {
    var hours by remember { mutableStateOf("") }
    var totalCost by remember { mutableStateOf(0.0) }
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Formulario de Pago", fontSize = 24.sp, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = hours,
            onValueChange = {
                hours = it
                totalCost = it.toDoubleOrNull()?.times(1.0) ?: 0.0
            },
            label = { Text("Ingrese las horas de reserva") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Costo Total: S/ $totalCost", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showDialog = true },
            enabled = totalCost > 0
        ) {
            Text(text = "Confirmar Pago")
        }
    }

    // Ventana de confirmación
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(text = "Confirmación de Reserva")
            },
            text = {
                Text("¿Está seguro de que desea reservar el casillero por $hours horas con un costo total de S/ $totalCost?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        navController.navigate("locker_details/$lockerId")
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentScreenPreview() {
    PaymentScreen(navController = rememberNavController())
}
