package com.kgjr.safecircle.models

import com.google.gson.annotations.SerializedName

/**
 * Represents the 'address' object nested within the Nominatim API response.
 * Fields are nullable as they might not always be present for every location.
 */
data class Address(
    @SerializedName("village")
    val village: String?,
    @SerializedName("county")
    val county: String?,
    @SerializedName("state")
    val state: String?,
    @SerializedName("ISO3166-2-lvl4")
    val iso31662Lvl4: String?,
    @SerializedName("country")
    val country: String?,
    @SerializedName("country_code")
    val countryCode: String?,
    @SerializedName("road")
    val road: String?,
    @SerializedName("postcode")
    val postcode: String?,
    @SerializedName("city")
    val city: String?,
    @SerializedName("suburb")
    val suburb: String?,
    @SerializedName("neighbourhood")
    val neighbourhood: String?
)