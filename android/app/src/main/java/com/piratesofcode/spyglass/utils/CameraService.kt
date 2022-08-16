package com.piratesofcode.spyglass.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.piratesofcode.spyglass.utils.Constants.TAG
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs


private const val RATIO_4_3_VALUE = 4.0 / 3.0
private const val RATIO_16_9_VALUE = 16.0 / 9.0

@SuppressLint("UnsafeOptInUsageError")
class CameraService(
    private val activity: AppCompatActivity,
    private val context: Context,
    private val viewFinder: PreviewView,
    private val capture: MaterialButton,
    private val onCaptureClick: () -> Unit,
    private val onImageAnalyzed: (result: String?) -> Unit,
    private val documentOnScreen: (state: Boolean) -> Unit
) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var cameraControl: CameraControl? = null
    var flashMode = ImageCapture.FLASH_MODE_OFF

    private val documentOnScreenAnalysis by lazy {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(cameraExecutor) { image ->
                    val mediaImage = image.image
                    mediaImage?.let {
                        val inputImage =
                            InputImage.fromMediaImage(it, image.imageInfo.rotationDegrees)
                        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                        labeler.process(inputImage)
                            .addOnSuccessListener { labels ->
                                documentOnScreen(
                                    labels.any { label ->
                                        label.index == 273 && label.confidence > 0.70
                                    }
                                )
                                image.close()
                            }
                            .addOnFailureListener { e ->
                                Log.e("SPYGLASS", "${e.message}")
                                image.close()
                            }
                    }
                }
            }
    }

    fun start() {
        capture.setOnClickListener {
            onCaptureClick()
            takePhoto()
        }
        startCamera()
    }

    fun stop() {
        cameraExecutor.shutdown()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                lensFacing = when {
                    hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                    hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                    else -> throw IllegalStateException("Camera unavailable.")
                }
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(context)
        )
    }

    @SuppressLint("UnsafeOptInUsageError", "ClickableViewAccessibility")
    fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera Failure.")
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val screenAspectRatio = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = activity.windowManager.currentWindowMetrics.bounds
            aspectRatio(metrics.width(), metrics.height())
        } else {
            val metrics = context.resources.displayMetrics
            aspectRatio(metrics.widthPixels, metrics.heightPixels)
        }
        val rotation = viewFinder.display.rotation

        preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(viewFinder.display.rotation)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .setFlashMode(flashMode)
            .build()

        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                activity,
                cameraSelector,
                preview,
                documentOnScreenAnalysis,
                imageCapture
            )
            cameraControl = camera.cameraControl
            preview?.setSurfaceProvider(viewFinder.surfaceProvider)
        } catch (e: Exception) {
            Log.e(TAG, "Camera binding failed. ${e.message}")
        }

        viewFinder.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> return@setOnTouchListener true
                MotionEvent.ACTION_UP -> {
                    val factory = viewFinder.meteringPointFactory
                    val point = factory.createPoint(motionEvent.x, motionEvent.y)
                    val action = FocusMeteringAction.Builder(point).build()
                    cameraControl?.startFocusAndMetering(action)
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = width.coerceAtLeast(height).toDouble() / width.coerceAtMost(height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val mediaImage = image.image
                    if (mediaImage != null) {
                        try {
                            analyzeImage(
                                InputImage.fromMediaImage(
                                    mediaImage,
                                    image.imageInfo.rotationDegrees
                                )
                            )
                        } catch (e: IllegalArgumentException) {
                            analyzeImage(
                                InputImage.fromBitmap(
                                    imageProxyToBitmap(mediaImage),
                                    image.imageInfo.rotationDegrees
                                )
                            )
                        }
                    }
                    image.close()
                }
            })
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun imageProxyToBitmap(image: Image): Bitmap {
        val buffer = image.planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun analyzeImage(image: InputImage) {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(image)
            .addOnSuccessListener { result ->
                onImageAnalyzed(result.text)
            }
    }

    private fun hasBackCamera() =
        cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false

    private fun hasFrontCamera() =
        cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
}