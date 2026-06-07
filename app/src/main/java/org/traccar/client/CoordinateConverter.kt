/*
 * Copyright 2024 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.client

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object CoordinateConverter {

    private const val PI = Math.PI
    private const val A = 6378245.0
    private const val EE = 0.00669342162296594323

    /**
     * Converts from WGS-84 to GCJ-02 coordinate system used by Chinese maps (AMap etc).
     * Coordinates outside China are returned unchanged.
     */
    fun toGcj02(lat: Double, lon: Double): Pair<Double, Double> {
        if (!isInChina(lat, lon)) {
            return Pair(lat, lon)
        }
        val dLat = transformLatitude(lon - 105.0, lat - 35.0)
        val dLon = transformLongitude(lon - 105.0, lat - 35.0)
        val radLat = lat / 180.0 * PI
        var magic = sin(radLat)
        magic = 1 - EE * magic * magic
        val sqrtMagic = sqrt(magic)
        val finalLat = lat + (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI)
        val finalLon = lon + (dLon * 180.0) / (A / sqrtMagic * cos(radLat) * PI)
        return Pair(finalLat, finalLon)
    }

    private fun isInChina(lat: Double, lon: Double): Boolean {
        return lon >= 72.004 && lon <= 137.8347 && lat >= 0.8293 && lat <= 55.8271
    }

    private fun transformLatitude(x: Double, y: Double): Double {
        var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * sqrt(abs(x))
        ret += (20.0 * sin(6.0 * x * PI) + 20.0 * sin(2.0 * x * PI)) * 2.0 / 3.0
        ret += (20.0 * sin(y * PI) + 40.0 * sin(y / 3.0 * PI)) * 2.0 / 3.0
        ret += (160.0 * sin(y / 12.0 * PI) + 320.0 * sin(y * PI / 30.0)) * 2.0 / 3.0
        return ret
    }

    private fun transformLongitude(x: Double, y: Double): Double {
        var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * sqrt(abs(x))
        ret += (20.0 * sin(6.0 * x * PI) + 20.0 * sin(2.0 * x * PI)) * 2.0 / 3.0
        ret += (20.0 * sin(x * PI) + 40.0 * sin(x / 3.0 * PI)) * 2.0 / 3.0
        ret += (150.0 * sin(x / 12.0 * PI) + 300.0 * sin(x / 30.0 * PI)) * 2.0 / 3.0
        return ret
    }
}
