package com.example.crosswordgame.core.manager

import android.util.Log
import kotlin.system.measureTimeMillis

class PerformanceOptimizer {
    
    /**
     * Measure the execution time of a block of code and log it if it exceeds a threshold.
     */
    inline fun <T> profile(tag: String, threshold: Long = 16, block: () -> T): T {
        var result: T
        val time = measureTimeMillis {
            result = block()
        }
        if (time > threshold) {
            Log.w("Performance", "[$tag] took ${time}ms, which exceeds ${threshold}ms threshold!")
        }
        return result
    }

    /**
     * Clear memory caches if needed.
     */
    fun optimizeMemory() {
        System.gc() // Just a hint, but useful for manual optimization triggers
        Log.i("Performance", "Memory optimization triggered.")
    }
}
