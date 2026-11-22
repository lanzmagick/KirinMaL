package com.axiel7.moelist.ui.composables.media

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KirinProgressColorsTest {
    @Test
    fun knownTotalUsesSixPercentageStages() {
        assertEquals(Color(0xFFFF526B), kirinProgressStageColor(1, 10, true))
        assertEquals(Color(0xFFFF9D35), kirinProgressStageColor(2, 10, true))
        assertEquals(Color(0xFFFFD54F), kirinProgressStageColor(4, 10, true))
        assertEquals(Color(0xFF39DB8A), kirinProgressStageColor(6, 10, true))
        assertEquals(Color(0xFF00CFFF), kirinProgressStageColor(8, 10, true))
        assertEquals(Color(0xFFB65CFF), kirinProgressStageColor(10, 10, true))
    }

    @Test
    fun unknownTotalUsesCountMilestones() {
        assertEquals(Color(0xFFFF526B), kirinProgressStageColor(1, null, true))
        assertEquals(Color(0xFFFF9D35), kirinProgressStageColor(5, null, true))
        assertEquals(Color(0xFFFFD54F), kirinProgressStageColor(10, null, true))
        assertEquals(Color(0xFF39DB8A), kirinProgressStageColor(25, null, true))
        assertEquals(Color(0xFF00CFFF), kirinProgressStageColor(50, null, true))
        assertEquals(Color(0xFFB65CFF), kirinProgressStageColor(100, null, true))
    }

    @Test
    fun disabledOrEmptyProgressKeepsThemeColor() {
        assertNull(kirinProgressStageColor(0, 12, true))
        assertNull(kirinProgressStageColor(0, null, true))
        assertNull(kirinProgressStageColor(9, 12, false))
        assertNull(kirinProgressStageColor(9, null, false))
    }
}
