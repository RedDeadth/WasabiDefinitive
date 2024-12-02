package com.example.guardbox64.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
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

    // Estado para controlar la expansión/collapse
    val expandedReserved = remember { mutableStateOf(true) } // Siempre desplegado
    val expandedShared = remember { mutableStateOf(true) } // Siempre desplegado
    val expandedFree = remember { mutableStateOf(false) }
    val expandedOccupied = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFD7E36)) // Color de fondo del TopBar
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "Escudo",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black // Color del icono de escudo
                )
                Text(
                    text = "GuardianBox",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 24.sp,
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        bottomBar = {
            if (currentUser != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFD7E36)) // Color de fondo del BottomBar, igual al TopBar
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Persona",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Black // Color del icono de la persona
                    )
                    Text(
                        text = "${currentUser.displayName ?: currentUser.email}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red) // Fondo rojo del botón
                    ) {
                        Text(text = "Cerrar sesión", color = Color.White) // Texto en blanco
                    }
                }
            }
        }
    ) { paddingValues ->
    Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFA25D))
                .padding(16.dp)
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()

                )
        ) {
            // Sección de casilleros reservados
            if (reservedLockers.isNotEmpty()) {
                ExpandableSection(
                    sectionTitle = "Mis Casilleros",
                    isExpanded = expandedReserved.value,
                    onToggle = { expandedReserved.value = !expandedReserved.value }
                ) {
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
            }

            // Sección de casilleros compartidos
            if (sharedLockers.isNotEmpty()) {
                ExpandableSection(
                    sectionTitle = "Casilleros Compartidos",
                    isExpanded = expandedShared.value,
                    onToggle = { expandedShared.value = !expandedShared.value }
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(sharedLockers) { index, locker ->
                            LockerItem(locker = locker, index = index) {
                                navController.navigate("locker_details/${locker.id}")
                            }
                        }
                    }
                }
            }

            // Sección de casilleros libres
            if (freeLockers.isNotEmpty()) {
                ExpandableSection(
                    sectionTitle = "Casilleros Libres",
                    isExpanded = expandedFree.value,
                    onToggle = { expandedFree.value = !expandedFree.value }
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
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
            }

            // Sección de casilleros ocupados
            if (occupiedLockers.isNotEmpty()) {
                ExpandableSection(
                    sectionTitle = "Casilleros Ocupados",
                    isExpanded = expandedOccupied.value,
                    onToggle = { expandedOccupied.value = !expandedOccupied.value }
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(occupiedLockers) { index, locker ->
                            LockerItem(locker = locker, index = index) {
                                navController.navigate("locker_details/${locker.id}")
                            }
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
fun ExpandableSection(
    sectionTitle: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = "Expandir sección",
                tint = Color.Black // Aquí defines el color del icono
            )
        }
        AnimatedVisibility(visible = isExpanded) {
            content()
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
