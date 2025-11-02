package com.kgjr.safecircle.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.telephony.TelephonyManager
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import java.util.Locale
import androidx.core.graphics.createBitmap

fun detectCountryCode(context: Context): String {
    val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val simCountry = telephonyManager.simCountryIso?.uppercase()
    if (!simCountry.isNullOrEmpty()) return simCountry

    val networkCountry = telephonyManager.networkCountryIso?.uppercase()
    if (!networkCountry.isNullOrEmpty()) return networkCountry

    return Locale.getDefault().country.uppercase()
}

fun renderComposableToBitmap(context: Context, content: @Composable () -> Unit): Bitmap {
    val density = context.resources.displayMetrics.density
    val width = (320 * density).toInt()
    val height = (100 * density).toInt()

    val composeView = ComposeView(context).apply {
        setContent { content() }
        measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.UNSPECIFIED)
        )
        layout(0, 0, measuredWidth, measuredHeight)
    }

    val bitmap = createBitmap(composeView.measuredWidth, composeView.measuredHeight)
    val canvas = Canvas(bitmap)
    composeView.draw(canvas)
    return bitmap
}
