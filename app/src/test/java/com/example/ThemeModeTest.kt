package com.example

import com.example.data.AppThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun testAppThemeModeParsing() {
        assertEquals(AppThemeMode.DARK, AppThemeMode.fromString("DARK"))
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromString("LIGHT"))
        assertEquals(AppThemeMode.LIQUID_GLASS, AppThemeMode.fromString("LIQUID_GLASS"))
        assertEquals(AppThemeMode.LIQUID_GLASS, AppThemeMode.fromString("liquid_glass"))
        assertEquals(AppThemeMode.DARK, AppThemeMode.fromString(null))
        assertEquals(AppThemeMode.DARK, AppThemeMode.fromString("UNKNOWN_VALUE"))
    }
}
