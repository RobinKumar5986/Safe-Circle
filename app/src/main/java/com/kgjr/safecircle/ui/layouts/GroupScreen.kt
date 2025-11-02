package com.kgjr.safecircle.ui.layouts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.maps.android.compose.MapType
import com.kgjr.safecircle.LauncherActivity
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.R
import com.kgjr.safecircle.fcm.FcmManager
import com.kgjr.safecircle.models.SettingButtonType
import com.kgjr.safecircle.theme.baseThemeColor
import com.kgjr.safecircle.theme.sosRed
import com.kgjr.safecircle.ui.layouts.customAlerts.HelpAndSupportDialog
import com.kgjr.safecircle.ui.layouts.customAlerts.LogoutConfirmationDialog
import com.kgjr.safecircle.ui.layouts.customAlerts.NotificationSetupTourAlert
import com.kgjr.safecircle.ui.navigationGraph.subGraphs.HomeIds
import com.kgjr.safecircle.ui.utils.BackgroundApiManagerUtil
import com.kgjr.safecircle.ui.utils.LocationActivityManager
import com.kgjr.safecircle.ui.utils.LocationUtils
import com.kgjr.safecircle.ui.viewmodels.GroupViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    nav: (HomeIds, String) -> Unit,
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
    var isStartNotificationTour by remember { mutableStateOf(false) }
    val userDataWithLocation by viewModel.groupWithLocation.collectAsState()
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
    val checkingInListScrollState = rememberScrollState()
    val sharedPreferenceManager = MainApplication.getSharedPreferenceManager()


    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSettingsSheetVisible by remember { mutableStateOf(false) }
    val showLogoutDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var showHelpAndFeedbackDialog by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        label = "FadeAnimation"
    )
    LaunchedEffect(Unit) {
        val mapTypeId = sharedPreferenceManager.getMapTypeId()
        selectedMapType = LocationUtils.getMapTypeFromId(mapTypeId)
    }
    LaunchedEffect(Unit) {
        if (!MainApplication.getSharedPreferenceManager().getIsPlaceCheckInCalled()){
            adminId?.let {
                viewModel.getAllPlaceCheckins(userId = it,context = context)
            }
        }
    }
    LaunchedEffect(Unit) {
        BackgroundApiManagerUtil.uploadAllPendingData()
    }
    LaunchedEffect(Unit) {
        //Saving FCM Token
        if(!sharedPreferenceManager.getIsFCMTokenSaved()) {
            FcmManager.registerForFcmToken() { fcmToken ->
                if (adminId != null && userName != null) {
                    viewModel.saveFCMTokenForTheDevice(
                        fcmToken = fcmToken,
                        userId = adminId,
                        userName = userName,
                        profileUrl = profileImageUrl ?: ""
                    )
                }
            }
        }
    }
    LaunchedEffect(error) {
        if(error != null){
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        /**
         * @Mark: always initialize the below method because it also creates the notification channel.
         */
        LocationActivityManager.cancelPeriodicNotificationWorker(context)
        LocationActivityManager.initializeNotificationAndWorker(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permission = Manifest.permission.ACTIVITY_RECOGNITION
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                delay(5000) // 5-sec delay
                LocationActivityManager.startActivityRecognition(context)
            } else {
                Log.e("SafeCircle", "ACTIVITY_RECOGNITION permission not granted")
            }
        } else {
            Log.d("SafeCircle", "ACTIVITY_RECOGNITION not required for SDK < 29")
        }
    }
    LaunchedEffect(Unit) {
        println("Looper Data")
        Log.d("SafeCircle", MainApplication.getSharedPreferenceManager().getIsUpdateLocationApiCalledLooper().toString())
        Log.d("SafeCircle", MainApplication.getSharedPreferenceManager().getIsUpdateLocationApiCalled().toString())
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

    if(!sharedPreferenceManager.getNotificationSetupTour() && userDataWithLocation.isNotEmpty()){
        isStartNotificationTour = true
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
                                        createNewCircle = true
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
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ){
//                                AdBannerView(bannerId = MainApplication.HOME_AD_ID)
                                Row(
                                    modifier = Modifier
                                        .horizontalScroll(checkingInListScrollState)
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                    ) {
                                        IconButton(
                                            onClick = {
                                                nav(HomeIds.LOCATION_CHECKING_IN_PLACE,"")
                                            },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.building),
                                                contentDescription = "Add Person",
                                                tint = baseThemeColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                }

                                Divider(thickness = 1.dp, color = Color.LightGray)
                                UserList(
                                    viewModel = viewModel,
                                    onClick = { userId , imageUrl->
                                        MainApplication.imageUrl = imageUrl
                                        nav(HomeIds.LOCATION_HISTORY, userId)
                                    },
                                )
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            currentGroupId?.let {
                                                nav(HomeIds.ADD_TO_CIRCLE, it)
                                            }
                                        }
                                        .padding(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(baseThemeColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.outline_supervisor),
                                            contentDescription = "Add Person",
                                            tint = baseThemeColor,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Add a person",
                                        color = baseThemeColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

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
                            .padding(16.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = (-370).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ---- Check-In Button ----
                        Box(
                            modifier = Modifier
                                .background(Color.White, shape = RoundedCornerShape(50))
                                .clickable {
                                    nav(HomeIds.LOCATION_CHECKING_IN_PLACE,"")
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.baseline_edit_location_24),
                                    contentDescription = "Location",
                                    modifier = Modifier.size(20.dp),
                                    colorFilter = ColorFilter.tint(baseThemeColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Check In", color = baseThemeColor, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // ---- SOS Button ----
                        Box(
                            modifier = Modifier
                                .background(Color.White, shape = RoundedCornerShape(50))
                                .padding(end = 16.dp)
                                .clickable {
                                    if(sharedPreferenceManager.isNotificationSetupCompleted()){
                                        nav(HomeIds.SOS_SCREEN,"")
                                    }else{
                                        Toast.makeText(context,"Complete the notification setup first", Toast.LENGTH_SHORT).show()
                                        nav(HomeIds.NOTIFICATION_SETUP_SCREEN,"")
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.sos),
                                    contentDescription = "SOS Icon",
                                    modifier = Modifier.size(20.dp),
                                    colorFilter = ColorFilter.tint(sosRed)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SOS", color = sosRed, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // ---- Layer Circular Button ----
                        Box(
                            modifier = Modifier
                                .size(35.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable {
                                    scope.launch { isChangeLayer = true }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.layer),
                                contentDescription = "Layer Icon",
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(baseThemeColor)
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
                AnimatedVisibility(
                    visible = alpha == 1f,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier = Modifier.zIndex(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 25.dp, top = 45.dp)
                            .size(35.dp)
                            .clip(CircleShape)
                            .shadow(elevation = 8.dp, shape = CircleShape)
                            .background(Color.White)
                            .clickable {
                                isSettingsSheetVisible = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.setting),
                            contentDescription = "Settings",
                            tint = baseThemeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
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
                            Triple(R.drawable.street, "Road", MapType.TERRAIN),
                            Triple(R.drawable.street, "Street", MapType.NORMAL),
                            Triple(R.drawable.satellite, "Satellite", MapType.HYBRID)
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
                                            val mapTypeId = LocationUtils.getMapTypeId(mapType)
                                            sharedPreferenceManager.saveMapTypeId(mapTypeId)
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
        CustomLottieAnimationView()
    }
    if (showLogoutDialog.value) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog.value = false },
            onConfirmLogout = {
                showLogoutDialog.value = false
                coroutineScope.launch {
                    MainApplication.getGoogleAuthUiClient().signOut()
                    sharedPreferenceManager.clearSharedPreference()
                    val intent = Intent(context, LauncherActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                }
            }
        )
    }
    if(showHelpAndFeedbackDialog){
        HelpAndSupportDialog(onDismiss = { showHelpAndFeedbackDialog = false })
    }

    //@Mark: Setting bottom sheet
    if (isSettingsSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isSettingsSheetVisible = false },
            sheetState = settingsSheetState,
            containerColor = Color.White,
            dragHandle = null
        ) {
            SettingsSheetContent { selectedType ->
                when (selectedType) {
                    SettingButtonType.SHARE_APP -> { val message =
                        "You Should try SafeCircle! It's a location shearing app that I use with my family and friends: https://play.google.com/store/apps/details?id=com.kgjr.safecircle&hl=en"
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                message
                            )
                            type = "text/plain"
                        }
                        context.startActivity(
                            Intent.createChooser(
                                shareIntent,
                                "Share Safe Circle"
                            )
                        )
                    }
                    SettingButtonType.HELP_AND_FEEDBACK -> {
                        showHelpAndFeedbackDialog = true
                    }
                    SettingButtonType.PRIVACY_SECURITY -> {
                        val privacyPolicyUrl = "https://sites.google.com/view/kjjrsafecircle/home"
                        val intent = Intent(Intent.ACTION_VIEW, privacyPolicyUrl.toUri())
                        context.startActivity(intent)
                    }

                    SettingButtonType.LOGOUT -> {
                        showLogoutDialog.value = true
                    }
                    SettingButtonType.NOTIFICATION_SETUP -> {
                        nav(HomeIds.NOTIFICATION_SETUP_SCREEN,"")
                    }
                }
            }
        }
    }


    //@Mark notification setup tour
    if(isStartNotificationTour){
        NotificationSetupTourAlert(onStart = {
            sharedPreferenceManager.setNotificationSetupTour(true)
            nav(HomeIds.NOTIFICATION_SETUP_SCREEN,"")
        }, onCancel = {
//            isStartNotificationTour = false
            //TODO: in future if allow the users to skip the tour
        })

    }

}