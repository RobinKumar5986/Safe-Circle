package com.kgjr.safecircle.models

data class UserLastLocation(
    val activity: String? = null,
    val address: String? = null,
    val battery: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timeStamp: Long? = null
)
