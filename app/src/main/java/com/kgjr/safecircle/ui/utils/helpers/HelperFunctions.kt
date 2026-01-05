package com.kgjr.safecircle.ui.utils.helpers

import com.kgjr.safecircle.models.ArchiveLocationData
import kotlin.math.*

object HelperFunctions {

    private const val NOICE_THRESHOLD = 70.0 // Meters
    private const val PREV_NEXT_THRESHOLD = 250.0 // Meters

    private const val SHARP_ANGLE_THRESHOLD = 30.0
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
//            val distanceNextAndPrev = getDistance(lat1 = next.latitude!!, lon1 = next.longitude!!, lat2 = prev.latitude!!, lon2 = prev.longitude!!)

            val angleAtCurrent = calculateAngleAtCurrentPoint(prev, current, next)

            //TODO: if the history is not good we need to include the timeStamp and activity also for the check...

            if (distancePrevAndCur > NOICE_THRESHOLD && distanceCurAndNext > NOICE_THRESHOLD && angleAtCurrent  < SHARP_ANGLE_THRESHOLD) {
//                result.add(normalizeBetween(prev,next,current))
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

    /**
     * Calculates distance between two coordinates in meters using the Haversine formula.
     **/
    private fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    /**
     * Calculates the interior angle (in degrees) at the 'current' point
     * formed by three geographic points: prev -> current -> next.
     */
    fun calculateAngleAtCurrentPoint(
        prev: ArchiveLocationData,
        current: ArchiveLocationData,
        next: ArchiveLocationData
    ): Double {
        val lat1 = prev.latitude!!
        val lon1 = prev.longitude!!
        val lat2 = current.latitude!!
        val lon2 = current.longitude!!
        val lat3 = next.latitude!!
        val lon3 = next.longitude!!

        // Calculate distances in meters using Haversine
        val a = getDistance(lat1, lon1, lat2, lon2) // prev -> current
        val b = getDistance(lat2, lon2, lat3, lon3) // current -> next
        val c = getDistance(lat1, lon1, lat3, lon3) // prev -> next (opposite side)

        // If points are too close or collinear, avoid division by zero
        if (a <= 0.0 || b <= 0.0) return 180.0

        // Law of cosines: cos(C) = (a² + b² - c²) / (2ab)
        val cosAngle = (a.pow(2) + b.pow(2) - c.pow(2)) / (2 * a * b)

        // Clamp to handle floating-point errors
        val clampedCos = cosAngle.coerceIn(-1.0, 1.0)

        return Math.toDegrees(acos(clampedCos))
    }
}