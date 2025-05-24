package com.kgjr.safecircle.ui.utils
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

data class PermissionItemData(
    @DrawableRes val painter: Int? = null,
    val icon: ImageVector? = null,
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val onClick: () -> Unit
)
