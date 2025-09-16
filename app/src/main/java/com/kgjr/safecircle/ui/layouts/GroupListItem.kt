package com.kgjr.safecircle.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kgjr.safecircle.R

@Composable
fun GroupListItem(
    id: String,
    name: String,
    expandedIds: Set<String>,
    onExpandChange: (String) -> Unit
) {
    val letterColors = mapOf(
        'A' to Color(0xFFE57373), 'B' to Color(0xFF64B5F6), 'C' to Color(0xFF81C784),
        'D' to Color(0xFFFFB74D), 'E' to Color(0xFFBA68C8), 'F' to Color(0xFF4DB6AC),
        'G' to Color(0xFF9575CD), 'H' to Color(0xFF7986CB), 'I' to Color(0xFFA1887F),
        'J' to Color(0xFF90A4AE), 'K' to Color(0xFFDCE775), 'L' to Color(0xFFFF8A65),
        'M' to Color(0xFF4DD0E1), 'N' to Color(0xFF64DD17), 'O' to Color(0xFFFFD600),
        'P' to Color(0xFF00ACC1), 'Q' to Color(0xFF00897B), 'R' to Color(0xFFD81B60),
        'S' to Color(0xFF5E35B1), 'T' to Color(0xFF3949AB), 'U' to Color(0xFF00838F),
        'V' to Color(0xFF6D4C41), 'W' to Color(0xFF039BE5), 'X' to Color(0xFFC0CA33),
        'Y' to Color(0xFFF57F17), 'Z' to Color(0xFF1B5E20)
    )

    val firstChar = name.firstOrNull()?.uppercaseChar() ?: 'A'
    val bgColor = letterColors[firstChar] ?: Color.Gray
    val isExpanded = expandedIds.contains(id)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 2.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(bgColor.copy(alpha = 0.15f))
                    .border(1.dp, bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = firstChar.toString(),
                    color = bgColor,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { onExpandChange(id) }) {
                Icon(
                    painter = painterResource(
                        id = if (isExpanded) R.drawable.baseline_arrow_drop_up
                        else R.drawable.outline_arrow_drop_down
                    ),
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

//            if (isExpanded) {
//                Surface(
//                    tonalElevation = 0.dp,
//                    shadowElevation = 4.dp,
//                    color = Color.Transparent,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(2.dp)
//                ) {}
//            }

    }
}
