package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.data.model.AmbientSoundType
import kotlinx.coroutines.*
import java.util.Random
import kotlin.math.sin

class AmbientSoundEngine {
    private var audioTrack: AudioTrack? = null
    private var soundJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Volatile
    var currentSound: AmbientSoundType = AmbientSoundType.NONE
        private set

    @Volatile
    var volume: Float = 0.7f
        set(value) {
            field = value.coerceIn(0f, 1f)
            audioTrack?.setVolume(field)
        }

    fun playSound(soundType: AmbientSoundType) {
        if (currentSound == soundType && audioTrack != null) return
        stopSound()
        if (soundType == AmbientSoundType.NONE) return

        currentSound = soundType
        soundJob = scope.launch {
            generateAndStreamAudio(soundType)
        }
    }

    fun stopSound() {
        currentSound = AmbientSoundType.NONE
        soundJob?.cancel()
        soundJob = null
        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
        } catch (_: Exception) {}
        audioTrack = null
    }

    private suspend fun generateAndStreamAudio(soundType: AmbientSoundType) {
        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack = track
        track.setVolume(volume)
        track.play()

        val buffer = ShortArray(bufferSize)
        val random = Random()
        var phase = 0.0
        var lowPassFilter = 0.0
        var cycleCounter = 0

        try {
            while (currentCoroutineContext().isActive && currentSound == soundType) {
                for (i in buffer.indices) {
                    val rawSample = when (soundType) {
                        AmbientSoundType.RAIN -> {
                            // Pink/brown filtered noise with occasional rain drops
                            val noise = (random.nextGaussian() * 0.4)
                            lowPassFilter += (noise - lowPassFilter) * 0.25
                            val drop = if (random.nextInt(3000) == 0) (random.nextDouble() * 0.8) else 0.0
                            (lowPassFilter * 0.8 + drop).coerceIn(-1.0, 1.0)
                        }
                        AmbientSoundType.DEEP_FOREST -> {
                            // Gentle wind rustle with subtle bird chirps
                            cycleCounter++
                            val windSway = sin(cycleCounter * 0.0003) * 0.3 + 0.7
                            val rustle = (random.nextGaussian() * 0.25) * windSway
                            lowPassFilter += (rustle - lowPassFilter) * 0.15
                            val chirp = if (cycleCounter % 18000 in 0..1200) {
                                sin(cycleCounter * 0.4) * 0.15
                            } else 0.0
                            (lowPassFilter + chirp).coerceIn(-1.0, 1.0)
                        }
                        AmbientSoundType.OCEAN_WAVES -> {
                            // Periodic swell of filtered noise
                            cycleCounter++
                            val wavePeriod = (sin(cycleCounter * 0.0002) + 1.0) * 0.5
                            val noise = random.nextGaussian() * wavePeriod * 0.5
                            lowPassFilter += (noise - lowPassFilter) * 0.08
                            lowPassFilter.coerceIn(-1.0, 1.0)
                        }
                        AmbientSoundType.CAMPFIRE -> {
                            // Soft rumble with crackle pops
                            val rumble = random.nextGaussian() * 0.15
                            lowPassFilter += (rumble - lowPassFilter) * 0.05
                            val crackle = if (random.nextInt(1200) == 0) (random.nextDouble() * 0.9 - 0.45) else 0.0
                            (lowPassFilter + crackle).coerceIn(-1.0, 1.0)
                        }
                        AmbientSoundType.NIGHT_CRICKETS -> {
                            // High rhythmic chirps with silent pauses
                            cycleCounter++
                            phase += 0.25
                            val chirpEnvelope = if ((cycleCounter / 3000) % 2 == 0) sin(cycleCounter * 0.05) else 0.0
                            val cricket = sin(phase * 4.5) * chirpEnvelope * 0.25
                            val backgroundNoise = (random.nextGaussian() * 0.02)
                            (cricket + backgroundNoise).coerceIn(-1.0, 1.0)
                        }
                        AmbientSoundType.WHITE_NOISE -> {
                            // Steady smooth white/brown noise
                            val noise = random.nextGaussian() * 0.2
                            lowPassFilter += (noise - lowPassFilter) * 0.3
                            lowPassFilter.coerceIn(-1.0, 1.0)
                        }
                        AmbientSoundType.NONE -> 0.0
                    }

                    // Convert to 16-bit PCM
                    buffer[i] = (rawSample * 32767.0 * 0.45).toInt().toShort()
                }

                track.write(buffer, 0, buffer.size)
            }
        } catch (_: Exception) {
        } finally {
            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }
}
