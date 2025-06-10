package com.kgjr.safecircle.ui.layouts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kgjr.safecircle.theme.orangeColor

@Composable
fun CustomMapMarker(
    painter: Painter,
    modifier: Modifier = Modifier,
    backgroundColor: Color = orangeColor,
    iconResId: Int? = null
) {
    Column(
        modifier = modifier.wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .padding(top = 18.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .background(backgroundColor)
                    .border(4.dp, Color.White, RoundedCornerShape(27.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painter,
                    contentDescription = "Marker Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(27.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            iconResId?.let { resId ->
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Overlay Icon",
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color.Gray, CircleShape)
                        .padding(4.dp)
                )
            }
        }

        Canvas(modifier = Modifier.size(8.dp)) {
            val path = Path().apply {
                moveTo(size.width / 2, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(path, color = Color.White)
            drawPath(path, color = Color.White, style = Stroke(width = 4f))
        }
    }
}