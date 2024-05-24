package com.tlh.demo1

import android.content.Context
import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.SampleDataPoint
import androidx.health.services.client.unregisterMeasureCallback
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Timer
import java.util.TimerTask


class HeartRateMeasureClient(context: Context) {

    private val measureClient = HealthServices.getClient(context).measureClient
    private val heartRateRepository= HeartRateRepository(HeartRateDataSourceImpl(context))

    suspend fun hasHeartRateCapability(): Boolean {
        val capabilities = measureClient.getCapabilitiesAsync().await()
        return (DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure)
    }

    @ExperimentalCoroutinesApi
    fun heartRateMeasureFlow(): Flow<MeasureMessage> = callbackFlow {
        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(
                dataType: DeltaDataType<*, *>,
                availability: Availability
            ) {
                if (availability is DataTypeAvailability) {
                    GlobalScope.launch {
                        if (hasHeartRateCapability()) {
                            startHeartRateMeasurement()
                        } else {
                            Log.d(TAG, "Vo2 rate data is not available.")
                        }
                    }
                }
            }

            override fun onDataReceived(data: DataPointContainer) {
                val heartRateBpm = data.getData(DataType.HEART_RATE_BPM)
                Log.d(TAG, "💓 Received heart rate: ${heartRateBpm.first().value}")
                trySendBlocking(MeasureMessage.MeasureData(heartRateBpm))
                val heartRateData= HeartRateData(
                    timestamp = System.currentTimeMillis(),
                    heartRate = heartRateBpm.first().value
                )
                GlobalScope.launch {
                    heartRateRepository.addHeartRateData(heartRateData)
                }
            }

        }

        Log.d(TAG, "⌛ Registering for data...")
        measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback)

        awaitClose {
            Log.d(TAG, "👋 Unregistering for data")
            runBlocking {
                measureClient.unregisterMeasureCallback(DataType.HEART_RATE_BPM, callback)
            }
        }
    }


    sealed class MeasureMessage {
        class MeasureAvailability(val availability: DataTypeAvailability) : MeasureMessage()
        class MeasureData(val data: List<SampleDataPoint<Double>>) : MeasureMessage()
    }

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    fun startHeartRateMeasurement() {
        val intervalMillis = 10000 // Ölçüm aralığı: 10 saniye

        val timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                val flow = heartRateMeasureFlow()
                val job = flow.launchIn(GlobalScope)

                GlobalScope.launch {
                    delay(5000)
                    job.cancelAndJoin()
                }
            }
        }
        timer.schedule(task, 0, intervalMillis.toLong())
    }

    companion object {
        const val TAG = "Heart RateMax"
    }
}
