package com.suman.inappupdate

import android.annotation.SuppressLint
import android.content.Intent
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
import com.suman.appupdater.CustomUpdater
import com.suman.appupdater.DownloadController
import com.suman.appupdater.ProgressListener

class MainActivity : AppCompatActivity() {


    private lateinit var download :DownloadController
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn = findViewById<Button>(R.id.btn_update)
        val url = findViewById<EditText>(R.id.editTextText)

        download = DownloadController(this)
        btn.setOnClickListener {
            download.enqueueDownload(url.text.toString(), object : ProgressListener{
                override fun onStarted() {
                }
                override fun onProgress(progress: Int) {
                    btn.text = "Updating $progress%"
                }

                override fun onFinished() {
                }
            })

        }
    }
}