package com.dankook.bangtansquat.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dankook.bangtansquat.R
import com.dankook.bangtansquat.extensions.plusAssign
import com.tedpark.tedpermission.rx2.TedRx2Permission
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnCamera.setOnClickListener {
            disposables += checkPermission()
        }
    }

    private fun checkPermission() =
        TedRx2Permission.with(this@MainActivity)
            .setPermissions(android.Manifest.permission.CAMERA)
            .request()
            .flatMap {
                if (it.isGranted.not()) Single.error(IllegalStateException())
                else Single.just(it)
            }
            .subscribe({
                startActivity<CameraActivity>()
            }) { toastPermissionDenied() }

    private fun toastPermissionDenied() =
        longToast(R.string.permission_denied)

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }
}