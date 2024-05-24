package com.tlh.demo1

interface HeartRateDataSource {
    suspend fun insertHeartRateData(heartRateData: HeartRateData)
    suspend fun getAllHeartRateData(): List<HeartRateData>
}