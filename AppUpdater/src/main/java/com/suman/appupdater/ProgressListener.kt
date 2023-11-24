package com.suman.appupdater

interface ProgressListener {
    fun onStarted()
    fun onProgress(progress: Int)
    fun onFinished()
}