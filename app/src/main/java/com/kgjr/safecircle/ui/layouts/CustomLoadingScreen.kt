package com.kgjr.safecircle.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kgjr.safecircle.R

@Composable
fun CustomLoadingScreen(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White
) {
    var compositionSpec by remember { mutableStateOf(LottieCompositionSpec.RawRes(R.raw.loading_anim)) }
    val composition by rememberLottieComposition(spec = compositionSpec)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            modifier = Modifier.size(300.dp),
            composition = composition,
            iterations = LottieConstants.IterateForever,
        )
    }
}
