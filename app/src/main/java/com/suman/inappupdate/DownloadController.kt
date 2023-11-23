package com.suman.inappupdate

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.util.Timer
import kotlin.concurrent.schedule


class DownloadController(private val context: Context, private val url: String) {
    companion object {
        private const val FILE_NAME = "DownloadApp.apk"
        private const val FILE_BASE_PATH = "file://"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        private const val PROVIDER_PATH = ".provider"
        private const val APP_INSTALL_PATH = "\"application/vnd.android.package-archive\""
    }

    @SuppressLint("Range")
    fun enqueueDownload() {
        val destination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/$FILE_NAME"
        val uri = Uri.parse("$FILE_BASE_PATH$destination")
        val file = File(destination)
        if (file.exists()) file.delete()
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(downloadUri)
        //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        request.setMimeType(MIME_TYPE)
        request.setTitle("Updating ...")
        request.setDescription("Description")
        request.setRequiresCharging(false)// Set if charging is required to begin the download
        request.setAllowedOverMetered(true)// Set if download is allowed on Mobile network
        request.setAllowedOverRoaming(true);
        request.setDestinationUri(uri)
        showInstallOption(destination, uri)

        val downloadId = downloadManager.enqueue(request)
        Toast.makeText(context, "Downloading...", Toast.LENGTH_LONG).show()

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    when (status) {
                        DownloadManager.STATUS_FAILED -> {}
                        DownloadManager.STATUS_PAUSED -> {}
                        DownloadManager.STATUS_PENDING -> {}
                        DownloadManager.STATUS_RUNNING -> {
                            val total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            if (total >= 0) {
                                val downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                val progress = (downloaded * 100L / total).toInt()
                                Log.d("TAG", "enqueueDownload: $progress")
                            }
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            Log.d("TAG", "enqueueDownload: 100")
                            mainHandler.removeCallbacks(this)
                            return
                        }
                    }
                }
                mainHandler.postDelayed(this, 1000)

            }
        })



    }
    private fun showInstallOption(destination: String, uri: Uri) {
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val contentUri = FileProvider.getUriForFile(context,context.packageName+ PROVIDER_PATH, File(destination))
                    val install = Intent(Intent.ACTION_VIEW)
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    install.data = contentUri
                    context.startActivity(install)
                    context.unregisterReceiver(this)
                } else {
                    val install = Intent(Intent.ACTION_VIEW)
                    install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    install.setDataAndType(uri, APP_INSTALL_PATH)
                    context.startActivity(install)
                    context.unregisterReceiver(this)
                }
            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}