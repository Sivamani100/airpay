package com.arkiolabs.airpay.platform

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.media.AudioManager
import android.net.Uri
import android.util.Log

/**
 * Sound Manager for transaction events in Airpay.
 * Supports custom audio files chosen by the user in Settings as well as pleasant native fallback tones.
 */
object SoundManager {

    private const val TAG = "SoundManager"

    fun playSuccess(context: Context, customUriString: String?) {
        playSound(
            context = context,
            customUriString = customUriString,
            fallbackToneType = ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,
            fallbackRingtoneType = RingtoneManager.TYPE_NOTIFICATION
        )
    }

    fun playFailure(context: Context, customUriString: String?) {
        // User requested: "for now default don't add any kind of sounds at all okay make sure that clearly"
        // If no custom user-selected sound is configured in Settings, stay completely silent on failure.
        if (customUriString.isNullOrBlank()) {
            return
        }
        playSound(
            context = context,
            customUriString = customUriString,
            fallbackToneType = -1,
            fallbackRingtoneType = -1
        )
    }

    fun playPreview(context: Context, uriString: String?, onSuccess: () -> Unit, onError: () -> Unit) {
        if (uriString.isNullOrBlank()) {
            playTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD)
            onSuccess()
            return
        }
        try {
            val uri = Uri.parse(uriString)
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .build()
                )
                setDataSource(context, uri)
                prepare()
                setOnCompletionListener { mp ->
                    mp.release()
                }
                start()
            }
            onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to preview sound: ${e.message}", e)
            onError()
        }
    }

    private fun playSound(
        context: Context,
        customUriString: String?,
        fallbackToneType: Int,
        fallbackRingtoneType: Int
    ) {
        if (!customUriString.isNullOrBlank()) {
            try {
                val uri = Uri.parse(customUriString)
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context, uri)
                    prepare()
                    setOnCompletionListener { mp ->
                        mp.release()
                    }
                    start()
                }
                return
            } catch (e: Exception) {
                Log.w(TAG, "Custom sound playback failed, falling back to system tone: ${e.message}")
            }
        }

        // Fallback: System Notification / ToneGenerator
        if (fallbackToneType == -1 && fallbackRingtoneType == -1) {
            return
        }
        try {
            val defaultUri = RingtoneManager.getDefaultUri(fallbackRingtoneType)
            val ringtone = RingtoneManager.getRingtone(context, defaultUri)
            if (ringtone != null) {
                ringtone.play()
                return
            }
        } catch (e: Exception) {
            Log.w(TAG, "Ringtone fallback failed: ${e.message}")
        }

        playTone(fallbackToneType)
    }

    private fun playTone(toneType: Int) {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
            toneGen.startTone(toneType, 300)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    toneGen.release()
                } catch (_: Exception) {}
            }, 500)
        } catch (e: Exception) {
            Log.e(TAG, "ToneGenerator failed: ${e.message}")
        }
    }
}
