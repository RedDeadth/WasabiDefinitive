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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
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

    val currentUser = FirebaseAuth.getInstance().currentUser

    Scaffold(
        topBar = {
            // Barra superior con el título
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFD7E36))

                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "Escudo",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black
                )
                Text(
                    text = "GuardianBox",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold, // Título en negrita
                        color = Color.Black,
                        fontSize = 24.sp,
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        bottomBar = {
            // Barra inferior con el nombre del usuario y el botón de cerrar sesión
            if (currentUser != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEFEFE)) // Color de fondo gris claro para la barra inferior
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Persona",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Black
                    )
                    Text(
                        text = "${currentUser.displayName ?: currentUser.email}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                    // Aquí puedes poner un botón de cerrar sesión (aún no funcional)
                    Button(onClick = { /* Lógica de cerrar sesión */ }) {
                        Text(text = "Cerrar sesión")
                    }
                }
            }
        }
    ) { paddingValues ->
        // Contenido principal de la pantalla
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFA25D))
                .padding(16.dp)
                .padding(
                    top = paddingValues.calculateTopPadding(), // Evitar que el contenido se solape con la barra superior
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            // Sección de casilleros reservados
            if (reservedLockers.isNotEmpty()) {
                Text(
                    text = "Mis Casilleros",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold // Título en negrita
                    )
                )
                LazyRow(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reservedLockers) { locker ->
                        LockerItem(locker = locker, index = 0) {
                            navController.navigate("locker_details/${locker.id}")
                        }
                    }
                }
            }

            // Sección de casilleros compartidos
            if (sharedLockers.isNotEmpty()) {
                Text(
                    text = "Casilleros Compatidos",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold // Título en negrita
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

            // Sección de casilleros libres
            if (freeLockers.isNotEmpty()) {
                Text(
                    text = "Casilleros Libres",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold // Título en negrita
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
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold // Título en negrita
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
