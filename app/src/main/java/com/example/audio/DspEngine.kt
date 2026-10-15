package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.audiofx.Equalizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh

private const val TAG = "DspEngine"

/**
 * Frequency centers for 10-band Graphic Equalizer.
 */
val EQ_FREQUENCIES = listOf(31, 63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)

/**
 * Built-in Software Equalizer Presets.
 */
enum class SoftwarePreset(val displayName: String, val gains: List<Float>) {
    ORIGINAL("Original (Plano)", listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)),
    BASS_BOOST("Bass Boost", listOf(6.5f, 6.0f, 4.5f, 2.0f, 0f, 0f, 0f, 0.5f, 1.0f, 1.5f)),
    EQUILIBRADO("Equilibrado", listOf(2.0f, 1.5f, 0.5f, 0f, 0f, 0.5f, 1.0f, 1.5f, 2.0f, 2.0f)),
    VOCALES("Vocales", listOf(-2.0f, -1.0f, 0f, 1.5f, 3.5f, 4.0f, 3.0f, 1.5f, 0.5f, 0f)),
    POP("Pop", listOf(1.5f, 2.5f, 3.0f, 2.0f, 0f, -0.5f, 1.5f, 2.5f, 3.0f, 3.5f)),
    ROCK("Rock", listOf(4.5f, 3.5f, 2.0f, -0.5f, -1.5f, 0f, 1.5f, 3.0f, 4.0f, 4.5f)),
    ELECTRONICA("Electrónica", listOf(6.0f, 5.0f, 3.0f, 0.5f, -1.0f, 0f, 2.0f, 3.5f, 5.0f, 5.5f)),
    HIP_HOP("Hip-Hop", listOf(7.0f, 6.5f, 4.0f, 1.5f, -0.5f, 0.5f, 1.5f, 2.5f, 3.0f, 3.5f)),
    CLASICA("Clásica", listOf(3.5f, 3.0f, 2.0f, 1.0f, -0.5f, -0.5f, 0.5f, 1.5f, 2.5f, 3.5f)),
    PODCAST("Podcast", listOf(-4.0f, -2.0f, 0f, 1.5f, 3.0f, 3.5f, 2.0f, 0.5f, -1.0f, -2.0f)),
    GAMING("Gaming / Pasos", listOf(2.0f, 1.0f, -1.0f, 0.5f, 2.0f, 3.5f, 4.5f, 4.0f, 2.5f, 1.0f)),
    PERSONALIZADO("Personalizado", listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

    companion object {
        fun fromName(name: String): SoftwarePreset =
            entries.firstOrNull { it.name == name } ?: ORIGINAL
    }
}

/**
 * Direct Form II Transposed Biquad Peaking Filter.
 * H(s) = (s^2 + s*(A/Q) + 1) / (s^2 + s/(A*Q) + 1)
 */
class BiquadPeakingFilter(
    val centerFreq: Float,
    val sampleRate: Float = 44100f,
    val q: Float = 1.414f
) {
    var gainDb: Float = 0f
        set(value) {
            field = value.coerceIn(-12f, 12f)
            computeCoefficients()
        }

    private var b0 = 1.0
    private var b1 = 0.0
    private var b2 = 0.0
    private var a1 = 0.0
    private var a2 = 0.0

    // Direct Form II Transposed delay states
    private var d1 = 0.0
    private var d2 = 0.0

    init {
        computeCoefficients()
    }

    private fun computeCoefficients() {
        val a = 10.0.pow(gainDb / 40.0)
        val w0 = 2.0 * PI * (centerFreq / sampleRate)
        val alpha = sin(w0) / (2.0 * q)
        val cosW0 = cos(w0)

        val a0 = 1.0 + alpha / a
        b0 = (1.0 + alpha * a) / a0
        b1 = (-2.0 * cosW0) / a0
        b2 = (1.0 - alpha * a) / a0
        a1 = (-2.0 * cosW0) / a0
        a2 = (1.0 - alpha / a) / a0
    }

    fun processSample(input: Double): Double {
        val output = b0 * input + d1
        d1 = b1 * input - a1 * output + d2
        d2 = b2 * input - a2 * output
        return output
    }

    fun resetState() {
        d1 = 0.0
        d2 = 0.0
    }

    /**
     * Evaluates magnitude response |H(e^jw)| in dB at frequency f.
     */
    fun evaluateGainAt(f: Double): Double {
        val w = 2.0 * PI * (f / sampleRate)
        val cosW = cos(w)
        val cos2W = cos(2.0 * w)
        val sinW = sin(w)
        val sin2W = sin(2.0 * w)

        val numReal = b0 + b1 * cosW + b2 * cos2W
        val numImag = -(b1 * sinW + b2 * sin2W)
        val denReal = 1.0 + a1 * cosW + a2 * cos2W
        val denImag = -(a1 * sinW + a2 * sin2W)

        val numMag2 = numReal * numReal + numImag * numImag
        val denMag2 = denReal * denReal + denImag * denImag

        val mag2 = if (denMag2 > 1e-12) numMag2 / denMag2 else 1.0
        return 10.0 * (ln(mag2) / ln(10.0))
    }
}

/**
 * High-performance 10-band DSP Engine with anti-clipping limiter,
 * curve evaluation, and test audio player.
 */
class DspEngine {

    private val sampleRate = 44100f
    private val filters = EQ_FREQUENCIES.map { freq ->
        BiquadPeakingFilter(centerFreq = freq.toFloat(), sampleRate = sampleRate, q = 1.414f)
    }

    // State flows
    private val _currentPreset = MutableStateFlow(SoftwarePreset.ORIGINAL)
    val currentPreset: StateFlow<SoftwarePreset> = _currentPreset.asStateFlow()

    private val _bandGains = MutableStateFlow(EQ_FREQUENCIES.map { 0f })
    val bandGains: StateFlow<List<Float>> = _bandGains.asStateFlow()

    private val _preampGainDb = MutableStateFlow(0f)
    val preampGainDb: StateFlow<Float> = _preampGainDb.asStateFlow()

    private val _isLimiterEnabled = MutableStateFlow(true)
    val isLimiterEnabled: StateFlow<Boolean> = _isLimiterEnabled.asStateFlow()

    private val _isAuditionPlaying = MutableStateFlow(false)
    val isAuditionPlaying: StateFlow<Boolean> = _isAuditionPlaying.asStateFlow()

    private val _dspActive = MutableStateFlow(true)
    val dspActive: StateFlow<Boolean> = _dspActive.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var auditionTrack: AudioTrack? = null
    private var auditionJob: Job? = null
    private var systemEqualizer: Equalizer? = null

    init {
        applyPreset(SoftwarePreset.ORIGINAL)
    }

    fun applyPreset(preset: SoftwarePreset) {
        _currentPreset.value = preset
        setBandGains(preset.gains)
    }

    fun setBandGains(gains: List<Float>) {
        val clamped = gains.map { it.coerceIn(-12f, 12f) }
        _bandGains.value = clamped
        for (i in filters.indices) {
            if (i < clamped.size) {
                filters[i].gainDb = clamped[i]
            }
        }
        updateSystemEqualizer()
    }

    fun setBandGain(index: Int, gainDb: Float) {
        if (index !in filters.indices) return
        val current = _bandGains.value.toMutableList()
        current[index] = gainDb.coerceIn(-12f, 12f)
        _bandGains.value = current
        filters[index].gainDb = current[index]
        _currentPreset.value = SoftwarePreset.PERSONALIZADO
        updateSystemEqualizer()
    }

    fun setPreampGain(gainDb: Float) {
        _preampGainDb.value = gainDb.coerceIn(-6f, 6f)
    }

    fun setLimiterEnabled(enabled: Boolean) {
        _isLimiterEnabled.value = enabled
    }

    fun setDspActive(active: Boolean) {
        _dspActive.value = active
        updateSystemEqualizer()
    }

    fun resetToFlat() {
        applyPreset(SoftwarePreset.ORIGINAL)
        _preampGainDb.value = 0f
    }

    /**
     * Computes the combined frequency response curve at 80 points across 20Hz-20kHz.
     * Evaluates total dB gain = preamp + sum(band responses).
     */
    fun computeResponseCurve(pointsCount: Int = 80): List<Pair<Float, Float>> {
        val preamp = if (_dspActive.value) _preampGainDb.value else 0f
        val minFreq = 20.0
        val maxFreq = 20000.0
        val logMin = ln(minFreq)
        val logMax = ln(maxFreq)

        val result = ArrayList<Pair<Float, Float>>(pointsCount)
        for (i in 0 until pointsCount) {
            val fraction = i.toDouble() / (pointsCount - 1)
            val freq = kotlin.math.exp(logMin + fraction * (logMax - logMin))

            var totalDb = preamp.toDouble()
            if (_dspActive.value) {
                for (filter in filters) {
                    totalDb += filter.evaluateGainAt(freq)
                }
            }
            result.add(Pair(freq.toFloat(), totalDb.coerceIn(-18.0, 18.0).toFloat()))
        }
        return result
    }

    /**
     * Attaches to an Android AudioSession (e.g. from a media player).
     */
    fun attachToAudioSession(sessionId: Int) {
        try {
            systemEqualizer?.release()
            systemEqualizer = Equalizer(0, sessionId).apply {
                enabled = _dspActive.value
            }
            updateSystemEqualizer()
            Log.d(TAG, "Attached system equalizer to session $sessionId")
        } catch (e: Exception) {
            Log.w(TAG, "AudioSession attachment not supported on this platform: ${e.message}")
        }
    }

    private fun updateSystemEqualizer() {
        val eq = systemEqualizer ?: return
        try {
            eq.enabled = _dspActive.value
            val numBands = eq.numberOfBands.toInt()
            val gains = _bandGains.value
            for (i in 0 until numBands) {
                if (i < gains.size) {
                    // System equalizer band levels are in millibels (mB = dB * 100)
                    val mB = (gains[i] * 100).toInt().toShort()
                    eq.setBandLevel(i.toShort(), mB)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not update system equalizer: ${e.message}")
        }
    }

    /**
     * Real-time DSP test player: Generates pink-noise harmonic audition stream
     * and filters it through the 10 Biquad filters + Limiter directly to the audio output.
     */
    fun toggleAudition() {
        if (_isAuditionPlaying.value) {
            stopAudition()
        } else {
            startAudition()
        }
    }

    private fun startAudition() {
        stopAudition()
        _isAuditionPlaying.value = true

        auditionJob = scope.launch(Dispatchers.Default) {
            val bufferSize = AudioTrack.getMinBufferSize(
                44100,
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
                        .setSampleRate(44100)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            auditionTrack = track
            track.play()

            val shortBuffer = ShortArray(bufferSize / 2)
            var phase1 = 0.0
            var phase2 = 0.0
            var phase3 = 0.0

            // Pink noise filter state
            var b0N = 0.0; var b1N = 0.0; var b2N = 0.0; var b3N = 0.0; var b4N = 0.0; var b5N = 0.0

            try {
                while (isActive && _isAuditionPlaying.value) {
                    val preampLin = 10.0.pow(_preampGainDb.value / 20.0)
                    val useDsp = _dspActive.value
                    val useLimiter = _isLimiterEnabled.value

                    for (i in shortBuffer.indices) {
                        // Generate rich harmonic musical noise test signal
                        val white = (Math.random() * 2.0 - 1.0)
                        b0N = 0.99886 * b0N + white * 0.0555179
                        b1N = 0.99332 * b1N + white * 0.0750759
                        b2N = 0.96900 * b2N + white * 0.1538520
                        b3N = 0.86650 * b3N + white * 0.3104856
                        b4N = 0.55000 * b4N + white * 0.5329522
                        b5N = -0.7616 * b5N - white * 0.0168980
                        val pink = (b0N + b1N + b2N + b3N + b4N + b5N + white * 0.5362) * 0.05

                        phase1 += 2.0 * PI * 130.81 / 44100.0 // C3
                        phase2 += 2.0 * PI * 261.63 / 44100.0 // C4
                        phase3 += 2.0 * PI * 523.25 / 44100.0 // C5
                        val tones = (sin(phase1) * 0.15 + sin(phase2) * 0.12 + sin(phase3) * 0.08)

                        var sample = (pink * 0.6 + tones * 0.4) * preampLin

                        if (useDsp) {
                            for (filter in filters) {
                                sample = filter.processSample(sample)
                            }
                        }

                        // Anti-clipping limiter (soft saturation)
                        if (useLimiter) {
                            sample = tanh(sample * 1.1)
                        } else {
                            sample = sample.coerceIn(-1.0, 1.0)
                        }

                        shortBuffer[i] = (sample * 30000.0).toInt().coerceIn(-32767, 32767).toShort()
                    }
                    track.write(shortBuffer, 0, shortBuffer.size)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Audition error: ${e.message}")
            } finally {
                try {
                    track.stop()
                    track.release()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    fun stopAudition() {
        _isAuditionPlaying.value = false
        auditionJob?.cancel()
        auditionJob = null
        try {
            auditionTrack?.stop()
            auditionTrack?.release()
        } catch (e: Exception) {
            // Ignore
        }
        auditionTrack = null
        filters.forEach { it.resetState() }
    }

    fun release() {
        stopAudition()
        systemEqualizer?.release()
        systemEqualizer = null
    }
}
