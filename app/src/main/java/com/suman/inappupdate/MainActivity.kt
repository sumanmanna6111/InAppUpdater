package com.suman.inappupdate

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    companion object {
        const val PERMISSION_REQUEST_STORAGE = 0
    }

    lateinit var downloadController: DownloadController

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn = findViewById<Button>(R.id.btn_update)
        val url = findViewById<EditText>(R.id.editTextText)

        btn.setOnClickListener {
            val apkUrl = url.text.toString()
            downloadController = DownloadController(this, apkUrl)
            checkStoragePermission()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadController.enqueueDownload()
            } else {
                Toast.makeText(this, "denied permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkStoragePermission() {
        if (checkSelfPermissionCompat(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            downloadController.enqueueDownload()
        } else {
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        if (shouldShowRequestPermissionRationaleCompat(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(this.currentFocus!!, "need per", Snackbar.LENGTH_SHORT).show()
        } else {
            requestPermissionsCompat(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_STORAGE)
        }
    }


    fun checkSelfPermissionCompat(permission: String) = ActivityCompat.checkSelfPermission(this, permission)

    fun shouldShowRequestPermissionRationaleCompat(permission: String) = ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

    fun requestPermissionsCompat(permissionsArray: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(this, permissionsArray, requestCode)
    }

}