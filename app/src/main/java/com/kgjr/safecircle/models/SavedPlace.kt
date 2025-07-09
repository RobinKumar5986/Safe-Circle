package com.kgjr.safecircle.models

data class SavedPlace(
    val id: String = "",
    val placeName: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val radiusInFeet: Int = 0
)
