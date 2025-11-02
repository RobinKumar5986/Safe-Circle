package com.kgjr.safecircle.ui.layouts


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.R
import com.kgjr.safecircle.theme.baseThemeColor
import com.kgjr.safecircle.theme.blackPurple
import com.kgjr.safecircle.theme.contactButtonBackground
import com.kgjr.safecircle.theme.contactButtonTextColor
import com.kgjr.safecircle.ui.layouts.customAlerts.GeneralNotificationSetupAlert
import com.kgjr.safecircle.ui.viewmodels.NotificationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSetupScreen(
    onBackPress: () -> Unit,
) {
    val viewModel: NotificationViewModel = viewModel()
    val groupList by viewModel.groupList.collectAsState()
    var expandedIds by remember { mutableStateOf(setOf<String>()) }
    val uiScope = rememberCoroutineScope()
    val availableGroups by viewModel.availableGroups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isError by viewModel.error.collectAsState()
    var selectedUserIds by remember { mutableStateOf(setOf<String>()) }
    val adminId = MainApplication.getGoogleAuthUiClient().getSignedInUser()?.userId
    val context = LocalContext.current
    var isConfirmNotification by remember { mutableStateOf(false) }
    var showCompletionAnimation by remember { mutableStateOf(false) }
    var showLoadingAnimation by remember { mutableStateOf(false) }
    val sharedPreferenceManager = MainApplication.getSharedPreferenceManager()

    if(showLoadingAnimation){
        CustomLottieAnimationView()
    }else if(showCompletionAnimation){
        CustomLottieAnimationView(animationResId = R.raw.check_mark, iterations = 1)
        LaunchedEffect(Unit) {
            delay(1500)
            showLoadingAnimation = false
            showCompletionAnimation = false
            onBackPress()
        }
    }else {
        Scaffold(
            topBar = {
                Column {
                    Surface(
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp
                    ) {
                        TopAppBar(
                            title = { Text("Notification Setup", color = Color.Black) },
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
                    LinearProgressIndicator(
                        progress = { selectedUserIds.size / 4f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = baseThemeColor,
                        trackColor = baseThemeColor.copy(alpha = 0.2f),
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                    )

                }
            },
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.White)
                ) {
                    if (groupList.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(bottom = 250.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "You are not part of any group till now",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Create or Join a Group Today",
                                color = blackPurple,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 80.dp),
                            contentPadding = PaddingValues(0.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(groupList) { group ->
                                GroupListItem(
                                    id = group.id,
                                    name = group.name,
                                    expandedIds = expandedIds,
                                    onExpandChange = { id ->
                                        expandedIds = if (expandedIds.contains(id)) {
                                            expandedIds - id
                                        } else {
                                            expandedIds + id
                                        }
                                        uiScope.launch {
                                            if (!availableGroups.containsKey(id) && expandedIds.contains(
                                                    id
                                                )
                                            ) {
                                                viewModel.getGroupUsers(groupId = id)
                                            }
                                        }
                                    }
                                )

                                if (expandedIds.contains(group.id)) {
                                    Column(
                                        modifier = Modifier.padding(
                                            start = 66.dp,
                                            top = 4.dp,
                                            bottom = 6.dp
                                        )
                                    ) {
                                        when {
                                            isLoading.contains(group.id) -> {
                                                Box(
                                                    modifier = Modifier.size(48.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(42.dp)
                                                    )
                                                }
                                            }

                                            isError.contains(group.id) -> {
                                                Text(
                                                    text = "Something went wrong",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontSize = 14.sp
                                                    ),
                                                    color = Color.Red
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Button(
                                                    onClick = { viewModel.getGroupUsers(group.id) }
                                                ) {
                                                    Text(text = "Reload")
                                                }
                                            }

                                            else -> {
                                                val usersMap =
                                                    availableGroups[group.id]?.users ?: emptyMap()

                                                usersMap.forEach { (userId, groupUser) ->
                                                    if (adminId != userId) {
                                                        UserItemForGeneralNotification(
                                                            userId = userId,
                                                            name = groupUser.name ?: "-",
                                                            imageUrl = groupUser.profileImageUrl
                                                                ?: "",
                                                            isChecked = selectedUserIds.contains(
                                                                userId
                                                            ),
                                                            onCheckedChange = { id, checked ->
                                                                if (selectedUserIds.size >= 4) {
                                                                    Toast.makeText(
                                                                        context,
                                                                        "You have already added the max people",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                                selectedUserIds = if (checked) {
                                                                    if (selectedUserIds.size < 4) selectedUserIds + id else selectedUserIds
                                                                } else {
                                                                    selectedUserIds - id
                                                                }

                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (group.id != groupList.last().id) {
                                    Divider(
                                        color = Color.LightGray,
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = {
                                isConfirmNotification = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = contactButtonBackground,
                                contentColor = contactButtonTextColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Save", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        )
    }
    if(isConfirmNotification){
        GeneralNotificationSetupAlert(onCancel = {
            isConfirmNotification = false
        }, onConfirm = {
            isConfirmNotification = false
            sharedPreferenceManager.saveUserIdsForNotification(selectedUserIds)
            sharedPreferenceManager.saveNotificationSetupCompleted(true)
            showLoadingAnimation = true
            viewModel.getAllFcmTokenForTheUsers(selectedUserIds, onComplete = {
                showLoadingAnimation = false
                showCompletionAnimation = true
            })
        })
    }
}
