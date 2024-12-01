package com.example.guardbox64.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.guardbox64.R
import com.example.guardbox64.model.Locker
import com.example.guardbox64.ui.viewmodel.LockerViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun LockerListScreen(
    lockerViewModel: LockerViewModel,
    navController: NavHostController,
) {

    val lockers by lockerViewModel.lockers.observeAsState(emptyList())
    val uniqueLockers = lockers.distinctBy { it.id }

    val reservedLockers = uniqueLockers.filter { it.occupied && it.userId == FirebaseAuth.getInstance().currentUser?.uid }
    val freeLockers = uniqueLockers.filter { !it.occupied }
    val occupiedLockers = uniqueLockers.filter { it.occupied && it.userId != FirebaseAuth.getInstance().currentUser?.uid }

    val sharedLockers = uniqueLockers.filter { locker ->
        locker.sharedWithEmails.contains(FirebaseAuth.getInstance().currentUser?.email)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFA25D)) // Cambia el fondo a naranja
            .padding(16.dp)
    ) {
        // Sección de casilleros reservados
        if (reservedLockers.isNotEmpty()) {
            Text(
                text = "Mis Casilleros",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            )
            LazyRow(
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reservedLockers) { locker ->
                    LockerItem(locker = locker, index = 0) { // Pasar un índice fijo para evitar errores
                        navController.navigate("locker_details/${locker.id}")
                    }
                }
            }
        }

        // Sección de casilleros compartidos
        if (sharedLockers.isNotEmpty()) {
            Text(
                text = "Casilleros Compartidos",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sharedLockers) { locker ->
                    LockerItem(locker = locker, index = 0) { // Pasar un índice fijo para evitar errores
                        navController.navigate("locker_details/${locker.id}")
                    }
                }
            }
        }

        // Sección de casilleros libres
        if (freeLockers.isNotEmpty()) {
            Text(
                text = "Casilleros Libres",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(4), // 4 columnas
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(freeLockers) { index, locker ->
                    LockerItem(locker = locker, index = index) {
                        navController.navigate("locker_details/${locker.id}")
                    }
                }
            }


        }

        // Sección de casilleros ocupados
        if (occupiedLockers.isNotEmpty()) {
            Text(
                text = "Casilleros Ocupados",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            )
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(occupiedLockers) { locker ->
                    LockerItem(locker = locker, index = 0) { // Pasar un índice fijo para evitar errores
                        navController.navigate("locker_details/${locker.id}")
                    }
                }
            }
        }

        if (uniqueLockers.isEmpty()) {
            Text(
                text = "No hay casilleros disponibles.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun LockerItem(locker: Locker, index: Int, onClick: () -> Unit) {
    // Colores alternados entre azul y blanco
    val backgroundColor = if (index % 2 == 0) Color.Blue else Color.White
    val textColor = if (index % 2 == 0) Color.White else Color.Black

    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(color = backgroundColor, shape = MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .fillMaxWidth() // Limitar el tamaño al ancho disponible
            .height(120.dp) // Define una altura fija para las tarjetas
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen del casillero en círculo
            Image(
                painter = painterResource(id = R.drawable.locker),
                contentDescription = "Imagen del Casillero",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ID del casillero
            Text(
                text = "ID: ${locker.id}",
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
