package com.kgjr.safecircle.models

data class User(
    val createdOn: Long = 0L,
    val email: String = "",
    val name: String = "",
    val profileImage: String = "",
    val groups: Map<String, String> = emptyMap()
)
