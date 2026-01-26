package com.iterio.app.service.timer

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.iterio.app.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * タイマー効果音を管理するクラス
 */
@Singleton
class SoundEffectManager @Inject constructor() {

    private var soundPool: SoundPool? = null
    private var tickSoundId: Int = 0
    private var completeSoundId: Int = 0
    private var soundPoolLoaded: Boolean = false

    fun initialize(context: Context) {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build()
                .apply {
                    setOnLoadCompleteListener { _, _, status ->
                        if (status == 0) {
                            soundPoolLoaded = true
                        }
                    }
                }

            tickSoundId = soundPool?.load(context, R.raw.countdown_tick, 1) ?: 0
            completeSoundId = soundPool?.load(context, R.raw.timer_complete, 1) ?: 0
        } catch (e: Exception) {
            soundPoolLoaded = false
        }
    }

    fun playTickSound() {
        if (soundPoolLoaded && tickSoundId != 0) {
            soundPool?.play(tickSoundId, 0.7f, 0.7f, 1, 0, 1f)
        }
    }

    fun playCompleteSound() {
        if (soundPoolLoaded && completeSoundId != 0) {
            soundPool?.play(completeSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        try {
            soundPool?.release()
        } finally {
            soundPool = null
            soundPoolLoaded = false
        }
    }
}
