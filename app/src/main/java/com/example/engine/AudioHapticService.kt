package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class AudioHapticService(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var soundEnabled: Boolean = true
    var hapticsEnabled: Boolean = true

    fun playTap() {
        if (!soundEnabled) return
        scope.launch {
            playTone(frequency = 550f, durationMs = 45, envelope = Envelope.FADE_OUT)
        }
    }

    fun playSequentialTone(step: Int, totalSteps: Int = 10) {
        if (!soundEnabled) return
        scope.launch {
            val scale = floatArrayOf(
                329.63f, // E4
                392.00f, // G4
                440.00f, // A4
                523.25f, // C5
                587.33f, // D5
                659.25f, // E5
                783.99f, // G5
                880.00f, // A5
                1046.50f, // C6
                1174.66f, // D6
                1318.51f  // E6
            )
            val freq = scale.getOrElse(step % scale.size) { 523.25f }
            playTone(freq, 110, Envelope.FADE_OUT)
        }
    }


    fun playTargetDisplay() {
        if (!soundEnabled) return
        scope.launch {
            playTone(
                frequency = 880f,
                durationMs = 120,
                envelope = Envelope.FADE_OUT
            )
        }
    }

    fun playRecallPrompt() {
        if (!soundEnabled) return
        scope.launch {
            playTone(659.25f, 70, Envelope.FADE_OUT)
            playTone(880.00f, 130, Envelope.FADE_OUT)
        }
    }

    fun playCorrect() {
        if (!soundEnabled) return
        scope.launch {
            // Crisp arpeggio C5 (523Hz), E5 (659Hz), G5 (784Hz)
            playTone(523f, 60, Envelope.FADE_OUT)
            playTone(659f, 60, Envelope.FADE_OUT)
            playTone(784f, 120, Envelope.FADE_OUT)
        }
    }

    fun playWrong() {
        if (!soundEnabled) return
        scope.launch {
            playTone(220f, 80, Envelope.SAWTOOTH_LOW)
            playTone(160f, 140, Envelope.SAWTOOTH_LOW)
        }
    }

    fun playLevelUp() {
        if (!soundEnabled) return
        scope.launch {
            playTone(523f, 70, Envelope.FADE_OUT)
            playTone(659f, 70, Envelope.FADE_OUT)
            playTone(784f, 70, Envelope.FADE_OUT)
            playTone(1046f, 200, Envelope.FADE_OUT)
        }
    }

    fun playCombo(combo: Int) {
        if (!soundEnabled) return
        scope.launch {
            val baseFreq = (600f + (combo.coerceAtMost(15) * 50f))
            playTone(baseFreq, 50, Envelope.FADE_OUT)
            playTone(baseFreq * 1.25f, 90, Envelope.FADE_OUT)
        }
    }

    fun vibrateTap() {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(15)
            }
        } catch (_: Exception) {}
    }

    fun vibrateSuccess() {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 40, 50), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 30, 40, 50), -1)
            }
        } catch (_: Exception) {}
    }

    fun vibrateWrong() {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(180)
            }
        } catch (_: Exception) {}
    }

    private enum class Envelope {
        FADE_OUT,
        SAWTOOTH_LOW
    }

    private fun playTone(frequency: Float, durationMs: Int, envelope: Envelope) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt().coerceAtLeast(100)
        val audioData = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val wave = when (envelope) {
                Envelope.FADE_OUT -> {
                    val fade = 1.0f - progress
                    (sin(2.0 * Math.PI * i * frequency / sampleRate) * fade * 0.8).toFloat()
                }
                Envelope.SAWTOOTH_LOW -> {
                    val angle = (i * frequency / sampleRate) % 1.0
                    val saw = (2.0 * angle - 1.0).toFloat()
                    val fade = 1.0f - progress * 0.5f
                    saw * fade * 0.6f
                }
            }
            audioData[i] = (wave * Short.MAX_VALUE).toInt().toShort()
        }

        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(audioData.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(audioData, 0, audioData.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong())
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {}
    }
}
