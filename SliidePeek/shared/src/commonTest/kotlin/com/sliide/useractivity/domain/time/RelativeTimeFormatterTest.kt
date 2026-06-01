package com.sliide.useractivity.domain.time

import kotlin.test.Test
import kotlin.test.assertEquals

class RelativeTimeFormatterTest {
    private val formatter = RelativeTimeFormatter()
    private val now = 1_000_000L

    @Test
    fun `formats future and under a minute as just now`() {
        assertEquals("just now", formatter.format(now + 1_000, now))
        assertEquals("just now", formatter.format(now - 59_000, now))
    }

    @Test
    fun `formats minutes with short feed copy`() {
        assertEquals("1 min ago", formatter.format(now - 60_000, now))
        assertEquals("5 min ago", formatter.format(now - 5 * 60_000, now))
    }

    @Test
    fun `formats hours with short feed copy`() {
        assertEquals("1 h ago", formatter.format(now - 3_600_000, now))
        assertEquals("3 h ago", formatter.format(now - 3 * 3_600_000, now))
    }

    @Test
    fun `formats days weeks and long tail with short feed copy`() {
        assertEquals("1 d ago", formatter.format(now - 86_400_000, now))
        assertEquals("3 d ago", formatter.format(now - 3 * 86_400_000, now))
        assertEquals("1 w ago", formatter.format(now - 7 * 86_400_000, now))
        assertEquals("30+ d ago", formatter.format(now - 31L * 86_400_000, now))
    }
}
