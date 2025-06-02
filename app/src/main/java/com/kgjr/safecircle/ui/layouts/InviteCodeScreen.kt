package com.kgjr.safecircle.ui.layouts

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kgjr.safecircle.theme.primaryVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteCodeScreen(code: String, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val formattedCode = if (code.length == 6) {
        code.substring(0, 3) + "-" + code.substring(3, 6)
    } else {
        code
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
                            Text(
                                text = "Join a Circle",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { onBack() }) {
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
            Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(paddingValues)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Invite members to the Family Circle",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B0A2A),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Share your code out loud or send it in a message",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(primaryVariant.copy(alpha = 0.1f))
                            .padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = formattedCode,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(40.dp))
                                    .background(primaryVariant)
                                    .clickable {
                                        val shareIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Join Safe Circle with the Group Code ---> *$code*"
                                            )
                                            type = "text/plain"
                                        }
                                        context.startActivity(
                                            Intent.createChooser(
                                                shareIntent,
                                                "Share Invite Code"
                                            )
                                        )
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Send Code",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun InviteCodeScreenPreview() {
    // You might want to wrap in your app theme if you have one
    Surface {
        InviteCodeScreen(code = "ABC123")
    }
}
