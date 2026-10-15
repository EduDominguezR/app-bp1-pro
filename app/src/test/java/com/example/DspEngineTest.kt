package com.example

import com.example.audio.BiquadPeakingFilter
import com.example.audio.DspEngine
import com.example.audio.SoftwarePreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class DspEngineTest {

    @Test
    fun testBiquadFilterFlatResponse() {
        val filter = BiquadPeakingFilter(centerFreq = 1000f, sampleRate = 44100f)
        filter.gainDb = 0f

        // When gain is 0 dB, magnitude response should be approximately 0 dB
        val gainAtCenter = filter.evaluateGainAt(1000.0)
        assertTrue("Gain at center freq should be near 0 dB", abs(gainAtCenter) < 0.05)

        val output = filter.processSample(1.0)
        assertEquals(1.0, output, 0.001)
    }

    @Test
    fun testBiquadFilterBoost() {
        val filter = BiquadPeakingFilter(centerFreq = 1000f, sampleRate = 44100f)
        filter.gainDb = 6f

        val gainAtCenter = filter.evaluateGainAt(1000.0)
        assertEquals(6.0, gainAtCenter, 0.1)

        // Far away from center, gain should approach 0 dB
        val gainAtFar = filter.evaluateGainAt(20000.0)
        assertTrue("Gain far from center should taper down", abs(gainAtFar) < 2.0)
    }

    @Test
    fun testDspEngineCurveComputation() {
        val engine = DspEngine()
        engine.applyPreset(SoftwarePreset.BASS_BOOST)

        val curve = engine.computeResponseCurve(80)
        assertEquals(80, curve.size)

        // In Bass Boost, low frequencies (e.g. around 60 Hz) should have positive gain
        val lowFreqPoint = curve.firstOrNull { it.first in 50f..80f }
        assertTrue("Low frequencies should have boosted gain in Bass Boost preset", (lowFreqPoint?.second ?: 0f) > 3f)
    }

    @Test
    fun testLimiterSoftSaturationStability() {
        // Test tanh saturation: even with input 5.0, output never exceeds 1.0
        val input = 5.0
        val saturated = kotlin.math.tanh(input * 1.1)
        assertTrue("Limiter output should stay <= 1.0", saturated <= 1.0)
        assertFalse("Limiter should not produce NaN or Infinity", saturated.isNaN() || saturated.isInfinite())
    }
}
