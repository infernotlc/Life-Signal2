package com.tlh.demo1

class HeartRateRepository(private val heartRateDataSource: HeartRateDataSource) {

    suspend fun getAllHeartRateData(): List<HeartRateData> {
        return heartRateDataSource.getAllHeartRateData()
    }

    suspend fun addHeartRateData(heartRateData: HeartRateData) {
        heartRateDataSource.insertHeartRateData(heartRateData)
    }
}