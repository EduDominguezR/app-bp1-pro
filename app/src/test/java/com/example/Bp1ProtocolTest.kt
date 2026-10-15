package com.example

import com.example.protocol.AckResult
import com.example.protocol.AncMode
import com.example.protocol.Bp1Protocol
import com.example.protocol.HardwareEqPreset
import com.example.protocol.MAGIC_AA
import com.example.protocol.MAGIC_BA
import com.example.protocol.OP_ANC_CONTROL
import com.example.protocol.OP_BATTERY_QUERY
import com.example.protocol.OP_EQ_PRESET
import com.example.protocol.OP_GAME_MODE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Bp1ProtocolTest {

    @Test
    fun testEncodeAncCommand() {
        val packetOff = Bp1Protocol.encodeAncCommand(AncMode.OFF, 0x80)
        assertEquals(4, packetOff.size)
        assertEquals(MAGIC_BA, packetOff[0])
        assertEquals(OP_ANC_CONTROL, packetOff[1])
        assertEquals(0x00.toByte(), packetOff[2])
        assertEquals(0x80.toByte(), packetOff[3])

        val packetAnc = Bp1Protocol.encodeAncCommand(AncMode.ANC_ON, 0xFF)
        assertEquals(0x01.toByte(), packetAnc[2])
        assertEquals(0xFF.toByte(), packetAnc[3])

        // Verify bounds clamping (0x10 to 0xFF)
        val packetClamped = Bp1Protocol.encodeAncCommand(AncMode.TRANSPARENCY, 0x02)
        assertEquals(0x10.toByte(), packetClamped[3])
    }

    @Test
    fun testEncodeGameModeCommand() {
        val enablePacket = Bp1Protocol.encodeGameModeCommand(true)
        assertEquals(3, enablePacket.size)
        assertEquals(MAGIC_BA, enablePacket[0])
        assertEquals(OP_GAME_MODE, enablePacket[1])
        assertEquals(0x01.toByte(), enablePacket[2])

        val disablePacket = Bp1Protocol.encodeGameModeCommand(false)
        assertEquals(0x00.toByte(), disablePacket[2])
    }

    @Test
    fun testEncodeBatteryQuery() {
        val query = Bp1Protocol.encodeBatteryQuery()
        assertEquals(2, query.size)
        assertEquals(MAGIC_AA, query[0])
        assertEquals(OP_BATTERY_QUERY, query[1])
    }

    @Test
    fun testEncodeHardwareEq() {
        val eqPacket = Bp1Protocol.encodeHardwareEqPreset(HardwareEqPreset.BASS_BOOST)
        assertEquals(3, eqPacket.size)
        assertEquals(MAGIC_BA, eqPacket[0])
        assertEquals(OP_EQ_PRESET, eqPacket[1])
        assertEquals(0x01.toByte(), eqPacket[2])
    }

    @Test
    fun testParseBatteryResponse() {
        // AA 27 <L=85%> <R=90%> <Case=80% + Charging bit 0x80>
        val payload = byteArrayOf(
            MAGIC_AA,
            OP_BATTERY_QUERY,
            85.toByte(),
            90.toByte(),
            (80 or 0x80).toByte()
        )

        val info = Bp1Protocol.parseBatteryResponse(payload)
        assertNotNull(info)
        assertEquals(85, info!!.leftPercent)
        assertEquals(90, info.rightPercent)
        assertEquals(80, info.casePercent)
        assertFalse(info.isLeftCharging)
        assertFalse(info.isRightCharging)
        assertTrue(info.isCaseCharging)
        assertTrue(info.isAvailable)
    }

    @Test
    fun testParseAckDiscrimination() {
        // Specific ACK echoing BA 34
        val specificAckPayload = byteArrayOf(MAGIC_BA, OP_ANC_CONTROL, 0x01)
        val result1 = Bp1Protocol.parseAck(specificAckPayload, OP_ANC_CONTROL)
        assertTrue(result1 is AckResult.SpecificAck)
        assertEquals(OP_ANC_CONTROL, (result1 as AckResult.SpecificAck).opcode)

        // Generic 2-byte ACK (e.g. AA 01 or 00)
        val genericAckPayload = byteArrayOf(MAGIC_AA, 0x01)
        val result2 = Bp1Protocol.parseAck(genericAckPayload, OP_ANC_CONTROL)
        assertTrue(result2 is AckResult.GenericAck)
    }
}
