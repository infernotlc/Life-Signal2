package com.tlh.bluetooth.presentation.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tlh.bluetooth.common.DataCarrier
import com.tlh.bluetooth.data.entities.EventInterestOutput
import com.tlh.bluetooth.domain.repo.DataCarrierRepo
import com.tlh.bluetooth.domain.repo.ParametersRepo
import kotlin.math.abs

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val parametersRepoImpl: ParametersRepo,
    private val dataCarrierRepoImpl: DataCarrierRepo,
) : ViewModel() {
    private var lastDetection: Long = 0
    private var detection: Boolean = false
    private var parametersRealLife: MutableList<DoubleArray> = mutableListOf()
    var information = MutableLiveData<String>()

    fun calculateAcgParameters(
        eventHolder: EventInterest
        Output
    ): DoubleArray? {

        return parametersRepoImpl.calculateAcgParameters(eventHolder)
    }

    fun fallAlgorithm(magnitude: Double): DoubleArray? {
        dataCarrierRepoImpl.control10Seconds()

        /*   binding.startTime.text = DataCarrier.temp.first().time.toString()
           binding.endTime.text = DataCarrier.temp.last().time.toString()*/

        val g = 9.81
        //milisaniyeleri saniyeye çevir.
        if (magnitude > 3 * g && !detection) {
            lastDetection = System.currentTimeMillis().div(1000)
            detection = true
            information.value = "Magnitude sağlandı."
        }

        if (detection && abs(
                System.currentTimeMillis().div(1000) - lastDetection
            ).toDouble() > 0.75 && magnitude > 3 * g
        ) {
            detection = true
            lastDetection = System.currentTimeMillis().div(1000)
        }
        if (detection && abs(System.currentTimeMillis().div(1000) - lastDetection) >= 5) {
            //  binding.magnitude.text = magnitude.toString()

            information.value = "5 saniye geçti."


            val magnitude10 = DataCarrier.temp.map {
                it.magnitude
            }
            val time10 = DataCarrier.temp.map {
                it.time
            }
            val acgxyz10 = DataCarrier.temp.map {
                it.values
            }
            val indexBegin = 0
            val eventOfInterest = dataCarrierRepoImpl.pickArrayOfInterest()


            if (magnitude10.subList(eventOfInterest!!.end, magnitude10.size).isEmpty()) {
                detection = false
            }

            val activity = magnitude10.subList(eventOfInterest.end, magnitude10.size)
                .sum() / magnitude10.subList(eventOfInterest.end, magnitude10.size).size

            if (activity < 11 && !dataCarrierRepoImpl.isFallOfPhone(eventOfInterest)) {
                try {
                    information.value = "activity e girdiii"

                    val result = calculateAcgParameters(eventOfInterest)

                    result?.let {
                        information.value = "parametreler sağlandı $it"
                        parametersRealLife.add(it)
                    }
                } catch (e: Exception) {
                    Log.i("changeInAngle",e.message.toString())
                    e.printStackTrace()
                }
            }
            detection = false

        }

        if (parametersRealLife.size < 2) {
            return if (parametersRealLife.isEmpty()) {
                null
            } else {
                parametersRealLife[0]
            }
        }

        // getting the matrix of paramters, which can be passed to model at once
        return vstack(parametersRealLife)
    }


    private fun vstack(arrays: List<DoubleArray>): DoubleArray {
        val totalRows = arrays.sumOf { it.size }
        val result = DoubleArray(totalRows)
        var index = 0
        for (array in arrays) {
            for (element in array) {
                result[index++] = element
            }
        }
        return result
    }

}
