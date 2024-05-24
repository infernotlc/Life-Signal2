package com.tlh.bluetooth.presentation.home

import android.annotation.SuppressLint
import android.content.Context

import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorPrivacyManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.Fragment
import com.tlh.bluetooth.R
import com.tlh.bluetooth.common.DataCarrier

import com.tlh.bluetooth.data.entities.SensorOutput
import kotlin.math.pow
import kotlin.math.sqrt

@AndroidEntryPoint
class HomeFragment : Fragment(), SensorEventListener {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometerSensor: Sensor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(SensorPrivacyManager.Sensors.ACG.sensor)?.let {
            accelerometerSensor = it
        }
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.information.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
    }


    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == SensorPrivacyManager.Sensors.ACG.sensor) {

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val magnitude = sqrt(x.pow(2.0f).toDouble() + y.pow(2.0f) + z.pow(2.0f).toDouble())

            val sensorOutput = SensorOutput(
                System.currentTimeMillis().div(1000),
                mutableListOf(x, y, z),
                magnitude,
                event.accuracy,
                event.sensor.type
            )

            DataCarrier.temp.add(sensorOutput)

            val result = viewModel.fallAlgorithm(magnitude)

            result?.let {
                binding.fall.text = "Fall Detected"
            }


        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

