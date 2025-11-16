package com.kgjr.safecircle.ui.layouts

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kgjr.safecircle.theme.DarkText
import com.kgjr.safecircle.theme.LightGrayBackground
import com.kgjr.safecircle.theme.primaryVariant
import com.kgjr.safecircle.ui.layouts.customAlerts.CreateGroupAlert


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameCircleScreen(
    createNewCircle: (String) -> Unit,
    onBackPress: () -> Unit
) {
    var circleNameInput by remember { mutableStateOf("") }
    var currentCircleName by remember { mutableStateOf("") }
    var isConfirmGroupName by remember { mutableStateOf(false) }

    val predefinedSuggestions = remember {
        listOf(
            "Family",
            "Friends",
            "Extended family",
            "Special someones",
            "Carpool",
            "Siblings",
            "Field trip group",
            "Vacation group",
            "Babysitter"
        )
    }

    val currentSuggestions = remember(circleNameInput, predefinedSuggestions) {
        val list = mutableListOf<String>()
        if (circleNameInput.isNotBlank() && predefinedSuggestions.none { it.equals(circleNameInput, ignoreCase = true) }) {
            list.add(circleNameInput)
        }
        list.addAll(predefinedSuggestions.filter {
            it.contains(circleNameInput, ignoreCase = true) || circleNameInput.isBlank()
        })
        list.distinctBy { it.lowercase() }
    }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = circleNameInput,
                                onValueChange = { circleNameInput = it },
                                placeholder = { Text("Name your Circle", color = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White,
                                    cursorColor = Color.Black,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    unfocusedPlaceholderColor = Color.Gray,
                                    focusedPlaceholderColor = Color.Gray
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            onBackPress()
                        }) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                Text(
                    text = "Choose the name for your new Circle",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LightGrayBackground)
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUGGESTIONS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier
                            .padding(start = 16.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(currentSuggestions) { suggestion ->
                        SuggestionItem(suggestion = suggestion, onSelect = { circleName ->
                            currentCircleName = circleName
                            isConfirmGroupName = true
                        })
                    }
                }
            }
        }
    )
    if (isConfirmGroupName) {
        CreateGroupAlert(
            groupName = currentCircleName,
            onConfirm = {
                isConfirmGroupName = false
                if (!currentCircleName.isEmpty()) {
                    createNewCircle(currentCircleName)
                }
            },
            onCancel = {
                isConfirmGroupName = false
                currentCircleName = ""
            }
        )
    }
}
@Composable
fun SuggestionItem(suggestion: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(suggestion) }
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(primaryVariant.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add",
                tint = primaryVariant,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Text(
            text = suggestion,
            color = DarkText,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 20.dp),
        thickness = 1.dp,
        color = LightGrayBackground
    )
}
