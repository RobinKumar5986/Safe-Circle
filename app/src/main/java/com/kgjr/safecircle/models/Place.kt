package com.kgjr.safecircle.models

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

data class Place(
    val type: String,
    @DrawableRes val iconRes: Int
)
