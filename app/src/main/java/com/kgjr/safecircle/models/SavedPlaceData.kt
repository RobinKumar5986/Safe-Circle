package com.kgjr.safecircle.models

data class SavedPlaceData(
    val placeName: String,
    val lat: Double,
    val lng: Double,
    val radiusInFeet: Int
)
