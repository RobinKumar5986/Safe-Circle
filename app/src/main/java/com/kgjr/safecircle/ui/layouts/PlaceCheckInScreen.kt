package com.kgjr.safecircle.ui.layouts

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.R
import com.kgjr.safecircle.models.Place
import com.kgjr.safecircle.models.SavedPlace
import com.kgjr.safecircle.theme.baseThemeColor
import com.kgjr.safecircle.ui.utils.SharedPreferenceManager
import com.kgjr.safecircle.ui.viewmodels.GroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceCheckInScreen(
    onBackPress: () -> Unit,
) {
    val context  = LocalContext.current
    val viewModel: GroupViewModel = viewModel()
    val userId = MainApplication.getGoogleAuthUiClient().getSignedInUser()?.userId
    val places = remember {
        listOf(
            Place("Home", R.drawable.house),
            Place("School", R.drawable.school),
            Place("Office", R.drawable.briefcase),
            Place("Gym", R.drawable.gym),
            Place("Grocery Store", R.drawable.grocery_store)
        )
    }
    var isAddPlaceScreen by remember { mutableStateOf(false) }

    var placeType by remember { mutableStateOf<String?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val sharedPrefManager = remember { SharedPreferenceManager(context) }
    val radius = remember { mutableStateOf<Int?>(null) }
    val placeId = remember { mutableStateOf<String?>(null) }

    var savedPlaces by remember { mutableStateOf<List<SavedPlace>>(emptyList()) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var placeToDelete by remember { mutableStateOf<SavedPlace?>(null) }

    LaunchedEffect(error) {
        if(error != null){
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }
    if(!isLoading) {
        LaunchedEffect(Unit) {
            savedPlaces = sharedPrefManager.getPlaceCheckins()
        }
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = !isAddPlaceScreen,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                Scaffold(
                    topBar = {
                        Surface(
                            tonalElevation = 4.dp,
                            shadowElevation = 4.dp
                        ) {
                            TopAppBar(
                                title = { Text("Places", color = Color.Black) },
                                navigationIcon = {
                                    IconButton(onClick = onBackPress) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color.Black
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.White
                                )
                            )
                        }

                    },
                    content = { paddingValues ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .background(Color.White)
                        ) {
                            item {
                                PlaceAddNewItem { selectedPlace ->
                                    radius.value = null
                                    placeId.value = null
                                    placeType = selectedPlace
                                    isAddPlaceScreen = true
                                }
                            }

                            item {
                                Divider(thickness = 1.dp, color = Color.LightGray)
                            }

                            if (savedPlaces.isNotEmpty()) {
                                items(savedPlaces) { savedPlace ->
                                    SavedPlaceItem(
                                        savedPlace = savedPlace,
                                        onClick = { selectedPlaceData ->
                                            radius.value = selectedPlaceData.radiusInFeet
                                            placeId.value = selectedPlaceData.id
                                            placeType = selectedPlaceData.placeName
                                            isAddPlaceScreen = true
                                        },
                                        onRemoveClick = {
                                            placeToDelete = it
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }

                            items(places) { place ->
                                PlaceItem(place = place, onClick = { selectedPlace ->
                                    radius.value = null
                                    placeId.value = null
                                    placeType = selectedPlace
                                    isAddPlaceScreen = true
                                })
                            }
                        }

                    }
                )
            }

            AnimatedVisibility(
                visible = isAddPlaceScreen,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            ) {
                AddPlaceScreen(
                    placeType = placeType,
                    radius = radius.value,
                    placeId = placeId.value,
                    onBackPress = {
                        isAddPlaceScreen = false
                    },
                    onSavePlace = { placeData , isUpdate , placeId ->
                        userId?.let {
                            if(isUpdate){
                                placeId?.let {
                                    viewModel.updatePlaceCheckingIn(
                                        placeId = placeId,
                                        placeData,
                                        userId = userId,
                                        context
                                    )
                                }
                            }else{
                                viewModel.savePlace(placeData = placeData, userId = it, context)
                            }

                        }
                        isAddPlaceScreen = false
                    }
                )
            }
        }
    }else{
        CustomLottieAnimationView()
    }
    if (showDeleteDialog && placeToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                placeToDelete = null
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(baseThemeColor)
                        .clickable {
                            placeToDelete?.let { place ->
                                userId?.let { id ->
                                    viewModel.deletePlaceCheckingIn(place.id, id, context)
                                }
                            }
                            showDeleteDialog = false
                            placeToDelete = null
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text("Remove", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray.copy(alpha = 0.2f))
                        .clickable {
                            showDeleteDialog = false
                            placeToDelete = null
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text("Cancel", color = Color.Black, fontWeight = FontWeight.Medium)
                }
            },
            title = {
                Text(
                    text = "Remove Place?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove \"${placeToDelete?.placeName}\"?",
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

}

@Composable
fun PlaceAddNewItem(onClick: (String?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(null) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = baseThemeColor.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Place",
                tint = baseThemeColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text("Add a new Place", fontSize = 18.sp, fontWeight = FontWeight.Medium ,  color = Color.Black)
    }

}

@Composable
fun PlaceItem(place: Place, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(place.type) }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(baseThemeColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = place.iconRes),
                contentDescription = "${place.type} Icon",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(baseThemeColor)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Text(
            text = "Add your ${place.type}",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

//        Icon(
//            imageVector = Icons.Filled.Close,
//            contentDescription = "Remove",
//            tint = Color.Gray
//        )
    }

    Divider(thickness = 1.dp, color = Color.LightGray)
}

@Composable
fun SavedPlaceItem(
    savedPlace: SavedPlace,
    onClick: (SavedPlace) -> Unit,
    onRemoveClick: (SavedPlace) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick(savedPlace) }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(baseThemeColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = "Saved Place Icon",
                    tint = baseThemeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = savedPlace.placeName,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { onRemoveClick(savedPlace) }
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = Color.Gray
                )
            }
        }

        Divider(thickness = 1.dp, color = Color.LightGray)
    }
}
