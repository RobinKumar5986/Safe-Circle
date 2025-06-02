package com.kgjr.safecircle.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kgjr.safecircle.theme.PurpleGrey40
import com.kgjr.safecircle.theme.baseThemeColor
import com.kgjr.safecircle.theme.primaryVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinCircleScreen(onBackPress: () -> Unit,
                     onSubmitPress: (String) -> Unit) {
    // Invite code input field using OTPTextField
    var code by remember { mutableStateOf("") }
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
                            Text(
                                text = "Join a Circle",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {onBackPress()}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint =  Color.Black
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Enter the invite code title
                Text(
                    text = "Enter the invite code",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))


                JoinGroupInput (
                    length = 6,
                    value = code,
                    onValueChange = { code = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    modifier = Modifier.padding(top = 10.dp),
                    text = "Get the code from the person\nsetting up your family's Circle",
                    fontSize = 20.sp,
                    color = PurpleGrey40.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        onSubmitPress(code)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    enabled = code.length == 6,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryVariant,
                        contentColor = Color.White,
                        disabledContainerColor = if(code.length == 6 ) {primaryVariant}else {PurpleGrey40.copy(alpha = 0.2f)},
                        disabledContentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Submit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun JoinCircleScreenPreview() {
    JoinCircleScreen(onBackPress = {

    }, onSubmitPress = {

    })
}