package com.kgjr.safecircle.models

data class StayPoint(
    val location: ArchiveLocationData,
    val startTime: Long,
    val endTime: Long
)