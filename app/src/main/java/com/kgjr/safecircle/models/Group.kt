package com.kgjr.safecircle.models

data class Group(
    val name: String? = null,
    val createdOn: Long? = null,
    val admin: Map<String, GroupUser>? = null,
    val users: Map<String, GroupUser>? = null,
)