package com.kgjr.safecircle.ui.layouts

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kgjr.safecircle.R
import com.kgjr.safecircle.theme.baseThemeColor
import com.kgjr.safecircle.theme.googleButtonCole
import com.kgjr.safecircle.ui.utils.Auth.google_sign_in.SignInState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun SignUpView(
    signInState: SignInState,
    nav: () -> Unit,
    onSignInClick: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(signInState.signInErrorMessage) {
        signInState.signInErrorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(signInState.isSignInSuccessFull) {
        if (signInState.isSignInSuccessFull == true) {
            nav()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseThemeColor)
            .padding(20.dp)
            .padding(bottom = 60.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.main_app_logo_empty_background),
                contentDescription = "App Logo",
                modifier = Modifier
                    .padding(top =  100.dp)
                    .size(200.dp)
                    .padding(bottom = 16.dp)
            )

            // App Slogan
            Text(
                text = "Safe Circle Keeps you Safe",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Made with Love
            Text(
                text = "Made with Love",
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            )

        }

        // Google Sign-Up Button
        Button(
            onClick = {
                onSignInClick()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(56.dp)
                .shadow(8.dp, shape = RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = googleButtonCole)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google_symbol),
                    contentDescription = "Google Icon",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = "Sign Up",
                    color = Color.White
                )
            }
        }
    }
}
