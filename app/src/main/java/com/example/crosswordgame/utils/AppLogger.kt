package com.example.crosswordgame.utils

import android.util.Log

/**
 * AppLogger: Centralized logging for consistent debugging.
 * Simple wrapper for production safety.
 */
object AppLogger {
    private const val GLOBAL_TAG = "CrosswordGame"
    private const val DEBUG = true // Toggle this for production releases

    fun d(tag: String, message: String) {
        if (DEBUG) Log.d("$GLOBAL_TAG:$tag", message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (DEBUG) Log.e("$GLOBAL_TAG:$tag", message, throwable)
    }

    fun i(tag: String, message: String) {
        if (DEBUG) Log.i("$GLOBAL_TAG:$tag", message)
    }
}
