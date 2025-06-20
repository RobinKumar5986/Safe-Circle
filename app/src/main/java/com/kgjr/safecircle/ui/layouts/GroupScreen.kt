package com.kgjr.safecircle.ui.layouts

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.maps.android.compose.MapType
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.R
import com.kgjr.safecircle.theme.baseThemeColor
import com.kgjr.safecircle.ui.navigationGraph.subGraphs.HomeIds
import com.kgjr.safecircle.ui.utils.LocationActivityManager
import com.kgjr.safecircle.ui.viewmodels.GroupViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    nav: (HomeIds,String) -> Unit
) {
    val context  = LocalContext.current
    val viewModel: GroupViewModel = viewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val scaffoldState = rememberBottomSheetScaffoldState()
    var isCreateNewCircleTopSheet by remember { mutableStateOf(false) }
    var targetAlpha by remember { mutableFloatStateOf(1f) }
    var selectedMapType by remember { mutableStateOf(MapType.NORMAL) }
    var isChangeLayer by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var createNewCircle by remember { mutableStateOf(false) }
    var createNewCircleAnimation by remember { mutableStateOf(false) }

    var joinNewCircle by remember { mutableStateOf(false) }
    var joinNewCircleAnimation by remember { mutableStateOf(false) }

    val userName = MainApplication.getGoogleAuthUiClient().getSignedInUser()?.userName
    val eMail = MainApplication.getGoogleAuthUiClient().getSignedInUser()?.email
    val adminId = MainApplication.getGoogleAuthUiClient().getSignedInUser()?.userId
    val profileImageUrl = MainApplication.getGoogleAuthUiClient().getSignedInUser()?.profileUrl

    val userData by viewModel.user.collectAsState()
    val groupList by viewModel.groupList.collectAsState()
    val currentSelectedGroup by viewModel.group.collectAsState()
    val currentGroupId by viewModel.currentGroupId.collectAsState()

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        label = "FadeAnimation"
    )
    LaunchedEffect(error) {
        if(error != null){
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }
    LaunchedEffect(Unit) {
        LocationActivityManager.initializeNotificationAndWorker(context)
        val permission = Manifest.permission.ACTIVITY_RECOGNITION
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            delay(5000)//5 - sec delay
            LocationActivityManager.startActivityRecognition(context)
        } else {
            Log.e("GroupScreen", "ACTIVITY_RECOGNITION permission not granted")
        }
    }

    LaunchedEffect(Unit) {
        adminId?.let {
            if (userData == null) {
                viewModel.loadUserData(userId = adminId)
            }else{
                if(currentGroupId != null){
                    viewModel.loadGroupById(groupId = currentGroupId!!) {}
                }else{
                    viewModel.loadGroupById(groupId = groupList[0].id) {}
                }
            }
        }
    }

    BackHandler(enabled = isCreateNewCircleTopSheet) {
        isCreateNewCircleTopSheet = false
    }
    BackHandler(enabled = joinNewCircle) {
        joinNewCircle = false
        joinNewCircleAnimation = false
    }
    BackHandler(enabled = createNewCircle ) {
        createNewCircleAnimation = false
        createNewCircle = false
    }
    if (!isLoading) {
        if(createNewCircle){
            AnimatedVisibility(
                visible = createNewCircleAnimation,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                NameCircleScreen(onBackPress = {
                    createNewCircleAnimation = false
                    createNewCircle = false
                }, createNewCircle = { groupName ->
                    if (adminId != null && eMail != null) {
                        viewModel.createGroupForUser(
                            groupName = groupName,
                            adminUserId = adminId,
                            eMail = eMail,
                            userName = userName ?: "N/A",
                            userProfileImageUrl = profileImageUrl ?: ""
                        )
                        createNewCircle = false
                    }
                })
            }
        }
        else if(joinNewCircle){
            AnimatedVisibility(
                visible = joinNewCircleAnimation,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                JoinCircleScreen(onSubmitPress = { code ->
                    joinNewCircle = false
                    joinNewCircleAnimation = false
                    viewModel.joinNewCircle(
                        groupId = code,
                        userId = adminId!!,
                        userName = userName ?: "",
                        userEmail = eMail!!,
                        userProfileUrl = profileImageUrl ?: ""
                    )

                }, onBackPress = {
                    joinNewCircle = false
                    joinNewCircleAnimation = false
                })
            }
        }
        else {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetContent = {
                    Box(
                        modifier = Modifier
                            .background(Color.White)
                            .fillMaxSize()
                    ) {
                        if(groupList.isEmpty()) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(top = 30.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = {
                                        isCreateNewCircleTopSheet = false
                                        createNewCircle = true
                                        scope.launch {
                                            delay(10)
                                            createNewCircleAnimation = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = baseThemeColor)
                                ) {
                                    Text("Create a Circle")
                                }

                                Button(
                                    onClick = {
                                        isCreateNewCircleTopSheet = false
                                        joinNewCircle = true
                                        scope.launch {
                                            delay(10)
                                            joinNewCircleAnimation = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = baseThemeColor)
                                ) {
                                    Text("Join a Circle")
                                }
                            }
                        }else{
                            UserList(viewModel, onClick = { userId ->
                                nav(HomeIds.LOCATION_HISTORY,userId)
                            })
                        }
                    }
                },
                sheetContainerColor = Color.White,
                modifier = Modifier.fillMaxSize(),
                sheetTonalElevation = 10.dp,
                sheetPeekHeight = 370.dp
            ) {
                val isDragging by remember {
                    derivedStateOf {
                        scaffoldState.bottomSheetState.currentValue != scaffoldState.bottomSheetState.targetValue
                    }
                }

                LaunchedEffect(isDragging, scaffoldState.bottomSheetState.targetValue) {
                    targetAlpha =
                        if (isDragging || scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded) 0f else 1f
                }

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HomeScreenMapView(selectedMapType = selectedMapType, viewModel = viewModel)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .offset(y = (-370).dp)
                            .padding(end = 16.dp, bottom = 8.dp)
                            .alpha(alpha),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .size(35.dp)
                                .clip(CircleShape)
                                .background(Color.White)

                                .clickable {
                                    scope.launch {
                                        isChangeLayer = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.layer),
                                contentDescription = "Create Group",
                                modifier = Modifier
                                    .size(24.dp)

                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isCreateNewCircleTopSheet) Color.Black.copy(alpha = 0.5f) else Color.Transparent
                    )
                    .then(
                        if (isCreateNewCircleTopSheet) Modifier.clickable(
                            onClick = { isCreateNewCircleTopSheet = false },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) else Modifier
                    )
            ) {
                // Sliding sheet (Create New Circle) - below the header
                AnimatedVisibility(
                    visible = isCreateNewCircleTopSheet,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                    exit = slideOutVertically(
                        targetOffsetY = { -it },
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeOut(animationSpec = tween(durationMillis = 300))
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 450.dp)
                            .shadow(4.dp),
                        color = Color.White,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 25.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = {
                                        currentGroupId?.let {
                                            nav(HomeIds.ADD_TO_CIRCLE, it)
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.add_friend),
                                        contentDescription = "Add Friend",
                                        colorFilter = ColorFilter.tint(baseThemeColor),
                                        modifier = Modifier
                                            .size(28.dp)
                                            .padding(2.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        bottom = 16.dp
                                    )
                            ) {
                                groupList.forEach { item ->
                                    Text(
                                        item.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.loadGroupById(item.id) {
                                                    isCreateNewCircleTopSheet = false
                                                }
                                            }
                                            .padding(16.dp)
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp),
                                        thickness = 0.5.dp,
                                        color = Color.Gray.copy(alpha = 0.5f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            isCreateNewCircleTopSheet = false
                                            createNewCircle = true
                                            scope.launch {
                                                delay(10)
                                                createNewCircleAnimation = true
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = baseThemeColor)
                                    ) {
                                        Text("Create a Circle")
                                    }

                                    Button(
                                        onClick = {
                                            isCreateNewCircleTopSheet = false
                                            joinNewCircle = true
                                            scope.launch {
                                                delay(10)
                                                joinNewCircleAnimation = true
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = baseThemeColor)
                                    ) {
                                        Text("Join a Circle")
                                    }
                                }
                            }
                        }
                    }
                }

                if(!groupList.isEmpty()) {
                    GroupDropdown(
                        currentGroupName = currentSelectedGroup?.name ?: "N/A",
                        visible = alpha == 1f,
                        onToggle = { isCreateNewCircleTopSheet = !isCreateNewCircleTopSheet }
                    )
                }
            }
            if (isChangeLayer) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isChangeLayer = false
                    },
                    sheetState = sheetState,
                    dragHandle = null,
                    containerColor = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        isChangeLayer = false
                                    }
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Map Type",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier
                            .padding(top = 10.dp, start = 20.dp, end = 20.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val options = listOf(
                            Triple(R.drawable.street, "Auto", MapType.NORMAL),
                            Triple(R.drawable.street, "Street", MapType.TERRAIN),
                            Triple(R.drawable.satellite, "Satellite", MapType.SATELLITE)
                        )

                        options.forEach { (imageRes, label, mapType) ->
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = imageRes),
                                    contentDescription = label,
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clickable {
                                            selectedMapType = mapType
                                        }
                                        .border(
                                            width = if (selectedMapType == mapType) 1.dp else 0.dp,
                                            color = if (selectedMapType == mapType) baseThemeColor else Color.Transparent,
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = label, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                }
            }
        }

    } else {
        CustomLoadingScreen()
    }
}