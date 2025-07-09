package com.kgjr.safecircle.ui.layouts

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceCheckInScreen(
    onBackPress: () -> Unit,
) {
    val places = remember {
        listOf(
            "Add your Home",
            "Add your School",
            "Add your Gym",
            "Add your Grocery Store"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Places") },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                PlaceAddNewItem { onAddPlace("New Place") }

                LazyColumn {
                    items(places) { place ->
                        PlaceItem(name = place, onClick = { onAddPlace(place) })
                    }
                }
            }
        }
    )
}
