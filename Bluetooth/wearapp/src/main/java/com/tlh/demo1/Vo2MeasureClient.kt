package com.tlh.demo1

import android.content.Context
import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.awaitWithException
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Timer
import java.util.TimerTask

class Vo2MeasureClient(context: Context) {
    private val healthServicesClient = HealthServices.getClient(context)
    private val measureClient = healthServicesClient.measureClient

    suspend fun hasVo2RateCapability(): Boolean {
        val capabilities = measureClient.getCapabilitiesAsync().awaitWithException()
        return (DataType.VO2_MAX in capabilities.supportedDataTypesMeasure)
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun vo2RateMeasureFlow(): Flow<String> = callbackFlow {
        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(
                dataType: DeltaDataType<*, *>,
                availability: Availability
            ) {
                if (availability is DataTypeAvailability) {
                    GlobalScope.launch {
                        if (hasVo2RateCapability()) {
                            startVo2RateMeasurement()
                        } else {
                            Log.d(TAG, "Vo2 rate data is not available.")
                        }
                    }
                }
            }

            override fun onDataReceived(data: DataPointContainer) {
                val vo2RateBpm = data.getData(DataType.VO2_MAX)
                if (vo2RateBpm.isNotEmpty()) {
                    val lastVo2Rate = vo2RateBpm.last().value
                    Log.d(TAG, "onDataReceived: Current Vo2 rate data is -- $lastVo2Rate")
                    val success = trySend("Current Vo2 rate data is -- $lastVo2Rate").isSuccess
                    if (!success) {
                        Log.e(TAG, "Error sending vo2 rate data to flow.")
                    }
                } else {
                    Log.d(TAG, "onDataReceived: vo2 rate data is empty.")
                }
            }
        }
        measureClient.registerMeasureCallback(DataType.VO2_MAX, callback)
        awaitClose {
            runBlocking {
                measureClient.unregisterMeasureCallbackAsync(
                    DataType.VO2_MAX,
                    callback
                ).await()
            }
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun startVo2RateMeasurement() {

        val flow = vo2RateMeasureFlow()

        GlobalScope.launch {
            flow.launchIn(this)
        }
    }


    companion object {
        private const val TAG = "Vo2Max"

    }
}
