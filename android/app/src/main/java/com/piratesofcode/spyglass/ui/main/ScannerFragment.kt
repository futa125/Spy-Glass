package com.piratesofcode.spyglass.ui.main

import android.content.Context.SENSOR_SERVICE
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.piratesofcode.spyglass.R
import com.piratesofcode.spyglass.databinding.FragmentScannerBinding
import com.piratesofcode.spyglass.ui.document.DocumentActivity
import com.piratesofcode.spyglass.utils.CameraService
import com.piratesofcode.spyglass.utils.Constants.REQUIRED_PERMISSIONS
import kotlin.math.sqrt

const val ACCELEROMETER_CHECK_INTERVAL = 25L

class ScannerFragment : Fragment(), SensorEventListener {

    private lateinit var binding: FragmentScannerBinding

    private var cameraService: CameraService? = null
    private var sensorManager: SensorManager? = null

    private var acceleration = 0f
    private var lastAcceleration = 0f
    private var currentAcceleration = 0f

    private val handler = Handler(Looper.getMainLooper())

    private var documentOnScreen = false

    private var counter = 0
    private val runnable = object : Runnable {
        override fun run() {
            when {
                documentOnScreen.not() -> {
                    binding.captureButton.isEnabled = false
                    setIndicatorColors(R.color.error_red)
                }
                acceleration > 0.35 -> {
                    counter = 0
                    binding.captureButton.isEnabled = false
                    setIndicatorColors(R.color.error_red)
                }
                counter >= 19 && documentOnScreen -> {
                    counter = 0
                    binding.captureButton.isEnabled = true
                    setIndicatorColors(R.color.success_green)
                }
                else -> {
                    counter++
                }
            }
            handler.postDelayed(this, ACCELEROMETER_CHECK_INTERVAL)
        }
    }

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value == true }
            if (granted) {
                cameraService?.start()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScannerBinding.inflate(layoutInflater)
        setupCamera()
        setupSensors()

        return binding.root
    }

    private fun setupCamera() {
        binding.apply {
            captureButton.clipToOutline = true
            cameraViewfinder.post {
                cameraService = CameraService(
                    activity = requireActivity() as AppCompatActivity,
                    context = requireContext(),
                    viewFinder = binding.cameraViewfinder,
                    capture = binding.captureButton,
                    onCaptureClick = ::onCaptureClick,
                    onImageAnalyzed = ::onImageAnalyzed,
                    documentOnScreen = ::documentOnScreen
                ).also {
                    if (hasPermissions()) {
                        it.start()
                    } else {
                        permissionsLauncher.launch(REQUIRED_PERMISSIONS)
                    }
                }
            }
            flashModeButton.setOnClickListener {
                when (cameraService?.flashMode) {
                    ImageCapture.FLASH_MODE_OFF -> {
                        cameraService?.flashMode = ImageCapture.FLASH_MODE_ON
                        flashModeButton.icon =
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_flash_on)
                    }
                    ImageCapture.FLASH_MODE_ON -> {
                        cameraService?.flashMode = ImageCapture.FLASH_MODE_AUTO
                        flashModeButton.icon =
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_flash_auto)
                    }
                    ImageCapture.FLASH_MODE_AUTO -> {
                        cameraService?.flashMode = ImageCapture.FLASH_MODE_OFF
                        flashModeButton.icon =
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_flash_off)
                    }
                }
                cameraService?.bindCameraUseCases()
            }
        }
    }

    private fun isSignature(text: String) =
        text matches DocumentActivity.receiptRegex ||
                text matches DocumentActivity.offerRegex ||
                text matches DocumentActivity.internalDocumentRegex

    private fun onImageAnalyzed(result: String?) {
        if (!result.isNullOrBlank()) {
            val firstLine = result.split("\n")[0].trim()

            if (isSignature(firstLine)) {
                DocumentActivity.start(requireContext(), result)
            } else {
                onProcessingError("unknown_document")
            }

        } else {
            onProcessingError("no_text")
        }
    }

    private fun documentOnScreen(state: Boolean) {
        documentOnScreen = state
    }

    private fun onProcessingError(error: String) {
        handler.post(runnable)
        binding.analyzeProgress.visibility = View.GONE
        when (error) {
            "no_document" ->
                Toast.makeText(requireContext(), "No document detected.", Toast.LENGTH_SHORT).show()
            "no_text" ->
                Toast.makeText(requireContext(), "No text detected.", Toast.LENGTH_SHORT).show()
            "unknown_document" ->
                Toast.makeText(requireContext(), "Unknown document type.", Toast.LENGTH_SHORT)
                    .show()
        }
    }

    private fun onCaptureClick() {
        binding.apply {
            handler.removeCallbacksAndMessages(null)
            analyzeProgress.visibility = View.VISIBLE
            setIndicatorColors(R.color.spyglass_gold)
            captureButton.isEnabled = false
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(runnable)
        binding.analyzeProgress.visibility = View.GONE
    }

    private fun setIndicatorColors(colorId: Int) {
        binding.apply {
            root.background =
                ContextCompat.getDrawable(requireContext(), colorId)
            val background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.circle_background
            )
            background?.setTint(
                ContextCompat.getColor(
                    requireContext(),
                    colorId
                )
            )
            buttonIndicator.background = background
        }
    }

    private fun hasPermissions() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        sensorManager?.unregisterListener(this)
        cameraService?.stop()
    }

    private fun setupSensors() {
        val sensorManager = activity?.getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_FASTEST,
            SensorManager.SENSOR_DELAY_FASTEST
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            lastAcceleration = currentAcceleration
            currentAcceleration = sqrt(x * x + y * y + z * z)
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuraccy: Int) {
        return
    }
}
