package com.tlh.demo1

import android.content.Context

class HeartRateDataSourceImpl(context: Context) : HeartRateDataSource {

    private val heartRateData = mutableListOf<HeartRateData>()

    override suspend fun insertHeartRateData(heartRateData: HeartRateData) {
        heartRateData.add(heartRateData)

    }

    override suspend fun getAllHeartRateData(): List<HeartRateData> {
        return heartRateData.toList()
    }
}