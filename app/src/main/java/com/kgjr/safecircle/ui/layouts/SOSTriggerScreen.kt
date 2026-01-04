package com.kgjr.safecircle.ui.layouts

import android.content.Intent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.kgjr.safecircle.theme.SoftDarkRed
import com.kgjr.safecircle.theme.baseThemeColor
import com.kgjr.safecircle.ui.utils.detectCountryCode
import kotlinx.coroutines.delay

@Composable
fun SOSTriggerScreen(onClick: () -> Unit) {
    val context = LocalContext.current

    val countryCode = remember { detectCountryCode(context) }
    val regionCode = when (countryCode) {
        in listOf(
            "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR", "DE",
            "GR", "HU", "IE", "IT", "LV", "LT", "LU", "MT", "NL", "PL", "PT",
            "RO", "SK", "SI", "ES", "SE"
        ) -> "EU"
        else -> countryCode
    }
    val emergencyNumbers = when (regionCode) {
        "IN" -> listOf(
            "Police" to "100",
            "Ambulance" to "102",
            "Fire" to "101",
            "Women Helpline" to "1091"
        )
        "US" -> listOf(
            "Police" to "911",
            "Ambulance" to "911",
            "Fire" to "911"
        )
        "EU" -> listOf(
            "Police" to "112",
            "Ambulance" to "112",
            "Fire" to "112"
        )
        "RU" -> listOf(
            "Police" to "102",
            "Ambulance" to "103",
            "Fire" to "101"
        )
        else -> listOf(
            "Police" to "100",
            "Ambulance" to "102",
            "Fire" to "101"
        )
    }


    var isButtonEnabled by remember { mutableStateOf(true) }
    var timeLeft by remember { mutableIntStateOf(0) }

    // Animate fill and pulse
    val progress = if (isButtonEnabled) 1f else timeLeft / 20f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "progressAnim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    // Countdown logic
    LaunchedEffect(isButtonEnabled) {
        if (!isButtonEnabled) {
            timeLeft = 20
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            isButtonEnabled = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = "Call SOS",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            color = baseThemeColor,
            textAlign = TextAlign.Center
        )

        // Subtitle / Info text
        Text(
            text = "Press the SOS button below to alert your emergency contacts.\n" +
                    "Please use only in case of real emergency.\n " +
                    "Press the text to call the emergency services."
            ,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.Gray,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 🚓 Emergency numbers
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            emergencyNumbers.forEach { (label, number) ->
                EmergencyNumberItem(label, number, context)
            }
        }

        Spacer(modifier = Modifier.height(50.dp))

        // Circular SOS Button
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color.Red.copy(alpha = if (isButtonEnabled) 0.9f else pulseAlpha),
                            baseThemeColor.copy(alpha = animatedProgress)
                        )
                    )
                )
                .clickable(enabled = isButtonEnabled) {
                    isButtonEnabled = false
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            if (isButtonEnabled) {
                Text(
                    text = "SOS",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 38.sp
                    )
                )
            } else {
                Text(
                    text = "${timeLeft}s",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 34.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

/**
 * Small reusable composable for each emergency number item.
 */
@Composable
fun EmergencyNumberItem(label: String, number: String, context: android.content.Context) {
    Text(
        text = "$label - $number",
        style = MaterialTheme.typography.bodyLarge.copy(
            color = SoftDarkRed,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_DIAL, "tel:$number".toUri())
                context.startActivity(intent)
            }
    )
}
