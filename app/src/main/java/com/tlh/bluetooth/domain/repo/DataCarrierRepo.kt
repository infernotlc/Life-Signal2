package com.tlh.bluetooth.domain.repo

import com.tlh.bluetooth.data.entities.EventInterestOutput

interface DataCarrierRepo {

    fun pickArrayOfInterest(
        thresholdEnding: Double = 15.0,
        beginMax: Double = 0.3,
        endMax: Double = 0.7
    ): EventInterestOutput?

    fun isFallOfPhone(
        eventOfInterest: EventInterestOutput
    ): Boolean

    fun control10Seconds()
}