package com.kgjr.safecircle.ui.utils

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun getTintedBitmapDescriptor(context: Context, drawableResId: Int, tintColor: Int): BitmapDescriptor {
    val drawable = ContextCompat.getDrawable(context, drawableResId)!!
    val wrappedDrawable = DrawableCompat.wrap(drawable)
    DrawableCompat.setTint(wrappedDrawable, tintColor)
    wrappedDrawable.setBounds(0, 0, wrappedDrawable.intrinsicWidth, wrappedDrawable.intrinsicHeight)

    val bitmap = createBitmap(wrappedDrawable.intrinsicWidth, wrappedDrawable.intrinsicHeight)
    val canvas = Canvas(bitmap)
    wrappedDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
