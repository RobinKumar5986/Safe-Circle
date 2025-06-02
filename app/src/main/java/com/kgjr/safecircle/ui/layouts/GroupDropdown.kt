package com.kgjr.safecircle.ui.layouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kgjr.safecircle.theme.baseThemeColor

@Composable
fun GroupDropdown(
    currentGroupName: String,
    visible: Boolean,
    onToggle: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = Modifier.zIndex(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
                .padding(horizontal = 100.dp)
                .height(33.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, end = 10.dp)
                    .clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentGroupName,
                    textAlign = TextAlign.Start,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Create Group",
                    tint = baseThemeColor
                )
            }
        }
    }
}
