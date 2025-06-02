package com.kgjr.safecircle.models

import com.google.gson.annotations.SerializedName

/**
 * Represents the top-level response from the Nominatim reverse geocoding API.
 */
data class NominatimResponse(
    @SerializedName("place_id")
    val placeId: Long,
    @SerializedName("licence")
    val licence: String,
    @SerializedName("osm_type")
    val osmType: String,
    @SerializedName("osm_id")
    val osmId: Long,
    @SerializedName("lat")
    val lat: String,
    @SerializedName("lon")
    val lon: String,
    @SerializedName("class")
    val apiClass: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("place_rank")
    val placeRank: Int,
    @SerializedName("importance")
    val importance: Double,
    @SerializedName("addresstype")
    val addressType: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("address")
    val address: Address,
    @SerializedName("boundingbox")
    val boundingBox: List<String>
)
