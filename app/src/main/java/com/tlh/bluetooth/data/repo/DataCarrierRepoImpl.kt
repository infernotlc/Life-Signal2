package com.tlh.bluetooth.data.repo

import com.tlh.bluetooth.common.DataCarrier
import com.tlh.bluetooth.data.entities.EventInterestOutput

class DataCarrierRepoImpl : DataCarrierRepo {

    override fun control10Seconds() {
        var last = DataCarrier.temp.last().time
        var first = DataCarrier.temp.first().time

        while (last - first > 10) {
            DataCarrier.temp.removeAt(0)
            last = DataCarrier.temp.last().time
            first = DataCarrier.temp.first().time
        }

    }

    override fun isFallOfPhone(
        eventOfInterest: EventInterestOutput
    ): Boolean {
        val timeSeconds = DataCarrier.temp.map {
            it.time
        }

        val magnitude = DataCarrier.temp.map {
            it.magnitude
        }
        val indexes = (eventOfInterest.begin until eventOfInterest.maxPeakIndex)
            .filter { magnitude[it] < 0.5 }
            .toIntArray()

        if (indexes.size in setOf(0, 1, 2)) {
            return false
        }

        val differences = indexes.mapIndexed { index, value ->
            if (index == 0) value else value - indexes[index - 1]
        }.toIntArray()

        val tempParts = mutableListOf<IntRange>()
        var temp = mutableListOf<Int>()

        differences.forEachIndexed { i, numSamples ->
            if (temp.isEmpty()) {
                temp.add(i)
            } else {
                if (numSamples in 1..3) {
                    temp.add(i)
                } else {
                    tempParts.add(temp.indices)
                    temp = mutableListOf(i)
                }
            }
        }

        tempParts.add(temp.indices)

        tempParts.forEach { part ->
            val totalSecondsBelow2g = timeSeconds.zip(indexes.slice(part)) { time, index ->
                if (index < timeSeconds.size - 1) timeSeconds[index + 1] - time else 0L
            }.sum()

            if (totalSecondsBelow2g > 0.05) {
                return true
            }
        }

        return false
    }


    override fun pickArrayOfInterest(
        thresholdEnding: Double,
        beginMax: Double,
        endMax: Double
    ): EventInterestOutput? {

        val peak = DataCarrier.temp.maxBy {
            it.magnitude
        }
        val timeSeconds = DataCarrier.temp.map {
            it.time
        }

        val magnitudeVector = DataCarrier.temp.map {
            it.magnitude
        }

        val maxPeakIndex = DataCarrier.temp.indexOf(peak)
        val maxPeakTime = peak.time
        var begin = DataCarrier.temp.first().time
        var end = DataCarrier.temp.last().time

        var freeFallEnd: Int? = null

        for (i in maxPeakIndex downTo 0) {
            // Max başlangıç zamanı kontrolü
            if (abs(maxPeakTime - timeSeconds[i]) > beginMax) {
                begin = i.toLong()
                break
            }
            // 9g'nin altında serbest düşüş tespiti - başlangıcı arama
            if (magnitudeVector[i] <= 9) {
                var indexFreeFall = i
                var counter = 0
                freeFallEnd = i
                while (true) {
                    // 9.25g'nin üzerindeki örnekleri kontrol etme
                    if (magnitudeVector[indexFreeFall] > 9.25) {
                        counter++
                        if (counter >= 5) {
                            begin = indexFreeFall.toLong()
                            for (ii in 0 until 5) {
                                if (magnitudeVector[indexFreeFall + ii] < 20) {
                                    begin = (indexFreeFall + ii).toLong()
                                    break
                                }
                            }
                            break
                        }
                        indexFreeFall--
                        continue
                    } else {
                        counter = 0
                    }
                    // Maksimum başlangıç zamanı ve serbest düşüş için maksimum zamanı kontrol etme
                    if (abs(maxPeakTime - timeSeconds[indexFreeFall]) > beginMax) {
                        begin = indexFreeFall.toLong()
                        break
                    }
                    // Etkinlik ölçümden önce başladı
                    if (indexFreeFall == 0) {
                        begin = 0
                        break
                    }
                    indexFreeFall--
                }
                break
            }
        }
        // Olayın bitişini kontrol etme
        var topBorder = 0
        for (i in maxPeakIndex until timeSeconds.size) {
            if (abs(maxPeakTime - timeSeconds[i]) > endMax) {
                topBorder = i
                break
            }
        }
        for (i in topBorder downTo maxPeakIndex) {
            // Eşik değerinden yüksek genlikte ilk örneği arama
            if (magnitudeVector[i] > thresholdEnding) {
                end = i.toLong()
                break
            }
        }
        // Yanlış uygulama kontrolü
        if (begin > end) {
            return null
        }
        if (begin < 0) {
            begin = 0
        }
        return EventInterestOutput(
            DataCarrier.temp.subList(begin.toInt(), end.toInt()),
            begin.toInt(),
            end.toInt(),
            maxPeakIndex,
            freeFallEnd
        )
    }
}