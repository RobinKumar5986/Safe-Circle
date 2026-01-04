package com.kgjr.safecircle.ui.utils.helpers

import com.kgjr.safecircle.models.ArchiveLocationData
import kotlin.math.*

object HelperFunctions {

    private const val NOICE_THRESHOLD = 400.0 // Meters
    private const val PREV_NEXT_THRESHOLD = 250.0 // Meters
    /**
     * @author:Robin Kumar
     * @about: This function is been use to remove noice(extream jump in the locations) and sort the Archive Location data.
     **/
    fun filterArchiveLocationData(
        locationData: List<Pair<String, List<ArchiveLocationData>>>
    ): List<List<ArchiveLocationData>> {

        val sortedGroups = locationData
            .sortedByDescending { it.first }
            .map { it.second }

        return sortedGroups.map { group ->
            processLocationGroup(group)
        }
    }

    private fun processLocationGroup(list: List<ArchiveLocationData>): List<ArchiveLocationData> {
        if (list.size < 3) return list

        val result = mutableListOf<ArchiveLocationData>()

        // Always keep the first point
        result.add(list[0])

        for (i in 1 until list.size - 1) {
            val prev = result.last()
            val current = list[i]
            val next = list[i + 1]

            val distancePrevAndCur = getDistance(lat1 = prev.latitude!!, lon1 = prev.longitude!!, lat2 = current.latitude!!, lon2 = current.longitude!!)
            val distanceCurAndNext = getDistance(lat1 = current.latitude!!, lon1 = current.longitude!!, lat2 = next.latitude!!, lon2 = next.longitude!!)
            val distanceNextAndPrev = getDistance(lat1 = next.latitude!!, lon1 = next.longitude!!, lat2 = prev.latitude!!, lon2 = prev.longitude!!)
            //TODO: if the history is not good we need to include the timeStamp and activity also for the check...
            if (distancePrevAndCur > NOICE_THRESHOLD && distanceCurAndNext > NOICE_THRESHOLD && distanceNextAndPrev < PREV_NEXT_THRESHOLD) {
                result.add(normalizeBetween(prev,next,current))
            } else {
                result.add(current)
            }
        }

        result.add(list.last())

        return result
    }

    private fun normalizeBetween(prev: ArchiveLocationData, next: ArchiveLocationData, originalCurrent: ArchiveLocationData): ArchiveLocationData {
        return originalCurrent.copy(
            latitude = (prev.latitude!! + next.latitude!!) / 2,
            longitude = (prev.longitude!! + next.longitude!!) / 2
        )
    }

    private fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}