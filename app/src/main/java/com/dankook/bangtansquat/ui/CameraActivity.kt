package com.dankook.bangtansquat.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.dankook.bangtansquat.R
import com.dankook.bangtansquat.utils.toByteArray
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_camera.*
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private val cameraExecutor by lazy {
        Executors.newSingleThreadExecutor()
    }

    private val cameraProviderFuture by lazy {
        ProcessCameraProvider.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        startCamera()
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        super.onDestroy()
    }

    private fun startCamera() {
        cameraProviderFuture.addListener(
            CameraProviderListener(),
            ContextCompat.getMainExecutor(this)
        )
    }


    inner class CameraProviderListener : Runnable {

        private val preview by lazy {
            Preview.Builder().build()
        }

        private val analyzer by lazy {
            ImageAnalysis.Builder().build()
        }

        override fun run() {
            // init preview
            preview.setSurfaceProvider(
                viewFinder.createSurfaceProvider()
            )

            // init analyzer
            analyzer.setAnalyzer(cameraExecutor, SampleAnalyzer {
                Logger.d("Average luminosity: $it")
            })

            // bind use case to lifecycle
            with(cameraProviderFuture.get()) {
                unbindAll()
                bindToLifecycle(
                    this@CameraActivity,
                    // 전면 카메라 사용
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    analyzer
                )
            }
        }
    }


    private class SampleAnalyzer(
        private val listener: (Double) -> Unit
    ) : ImageAnalysis.Analyzer {

        override fun analyze(image: ImageProxy) {
            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma)

            image.close()
        }
    }
}
