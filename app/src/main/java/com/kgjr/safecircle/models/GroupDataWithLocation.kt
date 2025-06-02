package com.kgjr.safecircle.models

data class GroupDataWithLocation (
    var id: String,
    var userData: GroupUser,
    var locationData: UserLastLocation
)