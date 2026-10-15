package com.example.protocol

/**
 * Protocol specification for Baseus Bass BP1 Pro (EP10 Pro).
 * Based on reverse-engineered BLE GATT protocol analysis from community research.
 *
 * Implements strict packet framing, validation, and honest confirmation tracking.
 */

// Magic prefix bytes
const val MAGIC_BA: Byte = 0xBA.toByte()
const val MAGIC_AA: Byte = 0xAA.toByte()

// Documented Opcodes
const val OP_ANC_CONTROL: Byte = 0x34.toByte()
const val OP_GAME_MODE: Byte = 0x24.toByte()
const val OP_BATTERY_QUERY: Byte = 0x27.toByte()
const val OP_EQ_PRESET: Byte = 0x42.toByte()

/**
 * ANC Modes supported by Baseus Bass BP1 Pro firmware.
 */
enum class AncMode(val code: Byte, val label: String) {
    OFF(0x00.toByte(), "Desactivado"),
    ANC_ON(0x01.toByte(), "Cancelación Activa"),
    TRANSPARENCY(0x02.toByte(), "Transparencia");

    companion object {
        fun fromCode(code: Byte): AncMode = entries.firstOrNull { it.code == code } ?: OFF
    }
}

/**
 * Hardware EQ presets loaded inside the earbud DSP.
 */
enum class HardwareEqPreset(val code: Byte, val label: String) {
    BALANCED(0x00.toByte(), "Equilibrado (Hardware)"),
    BASS_BOOST(0x01.toByte(), "Bass Boost (Hardware)"),
    VOICE(0x02.toByte(), "Vocales (Hardware)"),
    CLEAR(0x03.toByte(), "Clear / Pop (Hardware)");

    companion object {
        fun fromCode(code: Byte): HardwareEqPreset = entries.firstOrNull { it.code == code } ?: BALANCED
    }
}

/**
 * Command verification status.
 * Crucial rule: Never simulate confirmed hardware state when the earbud firmware sends a generic ACK.
 */
enum class CommandStatus(val label: String) {
    IDLE("En reposo"),
    ENVIANDO("Enviando..."),
    CONFIRMADO("Confirmado por hardware"),
    ENVIADO_SIN_CONFIRMACION("Enviado (ACK genérico no discriminado)"),
    NO_COMPATIBLE("Función no confirmada en este firmware"),
    ERROR("Error de comunicación")
}

/**
 * Real battery readings reported by earbud sensors.
 */
data class BatteryInfo(
    val leftPercent: Int = -1,
    val rightPercent: Int = -1,
    val casePercent: Int = -1,
    val isLeftCharging: Boolean = false,
    val isRightCharging: Boolean = false,
    val isCaseCharging: Boolean = false,
    val isAvailable: Boolean = false
) {
    val isLeftInCase: Boolean get() = leftPercent == 0 && isLeftCharging
    val isRightInCase: Boolean get() = rightPercent == 0 && isRightCharging
}

/**
 * Ack validation result.
 */
sealed class AckResult {
    data class SpecificAck(val opcode: Byte, val matched: Boolean) : AckResult()
    data class GenericAck(val raw: ByteArray) : AckResult()
    data class Invalid(val reason: String) : AckResult()
}

object Bp1Protocol {

    /**
     * Encodes ANC control command: BA 34 <mode> <level>
     * Documented level range: 0x10 to 0xFF.
     */
    fun encodeAncCommand(mode: AncMode, level: Int = 0x80): ByteArray {
        val safeLevel = level.coerceIn(0x10, 0xFF).toByte()
        return byteArrayOf(MAGIC_BA, OP_ANC_CONTROL, mode.code, safeLevel)
    }

    /**
     * Encodes Low Latency / Game Mode command: BA 24 <01|00>
     */
    fun encodeGameModeCommand(enabled: Boolean): ByteArray {
        val flag: Byte = if (enabled) 0x01 else 0x00
        return byteArrayOf(MAGIC_BA, OP_GAME_MODE, flag)
    }

    /**
     * Encodes Battery query command: AA 27
     */
    fun encodeBatteryQuery(): ByteArray {
        return byteArrayOf(MAGIC_AA, OP_BATTERY_QUERY)
    }

    /**
     * Encodes Hardware EQ preset: BA 42 <preset>
     */
    fun encodeHardwareEqPreset(preset: HardwareEqPreset): ByteArray {
        return byteArrayOf(MAGIC_BA, OP_EQ_PRESET, preset.code)
    }

    /**
     * Parses battery response packet.
     * Earbuds report battery percentage (0-100%) and charging flags.
     */
    fun parseBatteryResponse(payload: ByteArray): BatteryInfo? {
        if (payload.size < 3) return null

        // Check if header matches AA 27 or notification payload
        val offset = if (payload[0] == MAGIC_AA && payload[1] == OP_BATTERY_QUERY) 2 else 0
        if (payload.size < offset + 3) return null

        val left = payload[offset].toInt() and 0xFF
        val right = payload[offset + 1].toInt() and 0xFF
        val caseVal = payload[offset + 2].toInt() and 0xFF

        // High bit in certain Baseus firmware represents charging status
        val leftPercent = left and 0x7F
        val isLeftCharging = (left and 0x80) != 0
        val rightPercent = right and 0x7F
        val isRightCharging = (right and 0x80) != 0
        val casePercent = caseVal and 0x7F
        val isCaseCharging = (caseVal and 0x80) != 0

        val validLeft = if (leftPercent in 0..100) leftPercent else -1
        val validRight = if (rightPercent in 0..100) rightPercent else -1
        val validCase = if (casePercent in 0..100) casePercent else -1

        val isAvailable = validLeft >= 0 || validRight >= 0 || validCase >= 0

        return BatteryInfo(
            leftPercent = validLeft,
            rightPercent = validRight,
            casePercent = validCase,
            isLeftCharging = isLeftCharging,
            isRightCharging = isRightCharging,
            isCaseCharging = isCaseCharging,
            isAvailable = isAvailable
        )
    }

    /**
     * Parses ANC acknowledgment or notification.
     * Determines whether the response is a specific mode echo or generic ACK.
     */
    fun parseAck(payload: ByteArray, expectedOpcode: Byte): AckResult {
        if (payload.isEmpty()) return AckResult.Invalid("Payload vacío")

        if (payload.size >= 3 && payload[0] == MAGIC_BA && payload[1] == expectedOpcode) {
            return AckResult.SpecificAck(opcode = expectedOpcode, matched = true)
        }

        // Generic 2-byte or single byte ACK (e.g. AA 01 or 0x00)
        if (payload.size <= 2) {
            return AckResult.GenericAck(raw = payload)
        }

        return AckResult.Invalid("Respuesta no esperada: ${payload.joinToString(" ") { "%02X".format(it) }}")
    }

    /**
     * Converts a byte array to hex string for technical diagnostic logs.
     */
    fun toHexString(bytes: ByteArray): String {
        return bytes.joinToString(separator = " ") { "%02X".format(it) }
    }
}
