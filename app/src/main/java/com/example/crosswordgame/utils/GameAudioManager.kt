package com.example.crosswordgame.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import com.example.crosswordgame.R
import java.util.*

/**
 * Professional AudioManager: Uses SoundPool for low-latency feedback and TTS for education.
 */
class GameAudioManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var soundPool: SoundPool? = null
    private var successId: Int = -1
    private var errorId: Int = -1
    
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build()

        successId = soundPool?.load(context, R.raw.success, 1) ?: -1
        errorId = soundPool?.load(context, R.raw.error, 1) ?: -1
        
        tts = TextToSpeech(context, this)
    }

    fun playSuccess() {
        if (successId != -1) soundPool?.play(successId, 1f, 1f, 1, 0, 1f)
        triggerHaptic(HapticType.SUCCESS)
    }

    fun playError() {
        if (errorId != -1) soundPool?.play(errorId, 1f, 1f, 1, 0, 1f)
        triggerHaptic(HapticType.ERROR)
    }

    fun playPronunciation(word: String) {
        if (ttsReady) {
            tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word_id")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                ttsReady = true
            }
        }
    }

    private fun triggerHaptic(type: HapticType) {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    HapticType.SUCCESS -> VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 40, 40, 40), -1)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                when (type) {
                    HapticType.SUCCESS -> vibrator.vibrate(40)
                    HapticType.ERROR -> vibrator.vibrate(40)
                }
            }
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        tts?.stop()
        tts?.shutdown()
    }

    enum class HapticType { SUCCESS, ERROR }
}
