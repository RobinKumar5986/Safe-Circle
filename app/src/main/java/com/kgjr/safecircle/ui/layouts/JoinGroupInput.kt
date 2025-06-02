package com.kgjr.safecircle.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kgjr.safecircle.theme.primaryVariant

@Composable
fun JoinGroupInput(
    boxWidth: Dp = 32.dp,
    boxHeight: Dp = 40.dp,
    length: Int,
    value: String,
    onValueChange: (String) -> Unit
) {
    val spaceBetweenBoxes = 12.dp
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            val upperCased = newValue.uppercase()
            if (upperCased.length <= length) {
                onValueChange(upperCased)
            }
        },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Ascii),
        keyboardActions = KeyboardActions.Default,
        modifier = Modifier.focusRequester(focusRequester),
        decorationBox = {
            Row(
                modifier = Modifier
                    .size(width = (boxWidth + spaceBetweenBoxes) * length, height = boxHeight),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(length) { index ->
                    val isCurrent = index == value.length
                    val boxColor = if (isCurrent) {
                        primaryVariant.copy(alpha = 0.2f)
                    } else {
                        Color.Gray.copy(alpha = 0.2f)
                    }
                    Box(
                        modifier = Modifier
                            .size(boxWidth, boxHeight)
                            .clip(RoundedCornerShape(8.dp))
                            .background(boxColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = value.getOrNull(index)?.toString() ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                    }
                    if (index == 2) {
                        Text(
                            text = "-",
                            fontSize = 24.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    )
}
