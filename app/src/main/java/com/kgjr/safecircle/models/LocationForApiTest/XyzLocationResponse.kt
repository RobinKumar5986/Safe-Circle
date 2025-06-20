package com.kgjr.safecircle.models.LocationForApiTest

import com.google.gson.annotations.SerializedName

data class XyzLocationResponse(
    @SerializedName("statename") val statename: Any?,
    @SerializedName("standard") val standard: Standard?,
    @SerializedName("distance") val distance: String?,
    @SerializedName("elevation") val elevation: String?,
    @SerializedName("osmtags") val osmtags: OsmTags?,
    @SerializedName("state") val state: String?,
    @SerializedName("latt") val latt: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("prov") val prov: String?,
    @SerializedName("geocode") val geocode: String?,
    @SerializedName("geonumber") val geonumber: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("stnumber") val stnumber: String?,
    @SerializedName("staddress") val staddress: String?,
    @SerializedName("inlatt") val inlatt: String?,
    @SerializedName("alt") val alt: Alt?,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("region") val region: String?,
    @SerializedName("postal") val postal: String?,
    @SerializedName("longt") val longt: String?,
    @SerializedName("remaining_credits") val remainingCredits: Any?,
    @SerializedName("confidence") val confidence: String?,
    @SerializedName("class") val classType: Any?,
    @SerializedName("inlongt") val inlongt: String?,
    @SerializedName("adminareas") val adminareas: AdminAreas?,
    @SerializedName("altgeocode") val altgeocode: String?
)

data class Standard(
    @SerializedName("addresst") val addressT: String?,
    @SerializedName("staddress") val staddress: String?,
    @SerializedName("stnumber") val stnumber: String?,
    @SerializedName("statename") val statename: Any?,
    @SerializedName("inlatt") val inlatt: String?,
    @SerializedName("distance") val distance: String?,
    @SerializedName("region") val region: String?,
    @SerializedName("postal") val postal: String?,
    @SerializedName("prov") val prov: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("countryname") val countryname: String?,
    @SerializedName("confidence") val confidence: String?,
    @SerializedName("class") val classType: Any?,
    @SerializedName("inlongt") val inlongt: String?
)

data class OsmTags(
    @SerializedName("name_kn") val nameKn: String?,
    @SerializedName("boundary") val boundary: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("alt_name") val altName: String?,
    @SerializedName("admin_level") val adminLevel: String?
)

data class Alt(
    @SerializedName("loc") val loc: List<AltLocation>?
)

data class AltLocation(
    @SerializedName("staddress") val staddress: String?,
    @SerializedName("stnumber") val stnumber: String?,
    @SerializedName("postal") val postal: String?,
    @SerializedName("latt") val latt: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("prov") val prov: String?,
    @SerializedName("longt") val longt: String?,
    @SerializedName("dist") val dist: String?,
    @SerializedName("class") val classType: Any?
)

data class AdminAreas(
    @SerializedName("admin10") val admin10: AdminLevel?,
    @SerializedName("admin9") val admin9: AdminLevel?,
    @SerializedName("admin8") val admin8: AdminLevel8?,
    @SerializedName("admin6") val admin6: AdminLevel?,
    @SerializedName("admin5") val admin5: AdminLevel5?
)

data class AdminLevel(
    @SerializedName("name_kn") val nameKn: String?,
    @SerializedName("level") val level: String?,
    @SerializedName("boundary") val boundary: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("alt_name") val altName: String?,
    @SerializedName("admin_level") val adminLevel: String?
)

data class AdminLevel8(
    @SerializedName("wikipedia") val wikipedia: String?,
    @SerializedName("operator_wikidata") val operatorWikidata: String?,
    @SerializedName("operator") val operator: String?,
    @SerializedName("name_kn") val nameKn: String?,
    @SerializedName("boundary") val boundary: String?,
    @SerializedName("name_en") val nameEn: String?,
    @SerializedName("old_name") val oldName: String?,
    @SerializedName("name_cs") val nameCs: String?,
    @SerializedName("wikidata") val wikidata: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("name_or") val nameOr: String?,
    @SerializedName("admin_level") val adminLevel: String?,
    @SerializedName("level") val level: String?,
    @SerializedName("name_ar") val nameAr: String?,
    @SerializedName("name_pa") val namePa: String?,
    @SerializedName("name_ur") val nameUr: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("name_hi") val nameHi: String?
)

data class AdminLevel5(
    @SerializedName("wikipedia") val wikipedia: String?,
    @SerializedName("wikidata") val wikidata: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("admin_level") val adminLevel: String?,
    @SerializedName("level") val level: String?,
    @SerializedName("name_kn") val nameKn: String?,
    @SerializedName("boundary") val boundary: String?,
    @SerializedName("alt_name") val altName: String?,
    @SerializedName("type") val type: String?
)
