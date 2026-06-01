package com.sliide.useractivity.domain.time

class RelativeTimeFormatter {
    fun format(thenMillis: Long, nowMillis: Long): String {
        val elapsedSeconds = ((nowMillis - thenMillis) / 1_000).coerceAtLeast(0)
        return when {
            elapsedSeconds < 60 -> "just now"
            elapsedSeconds < 3_600 -> short(elapsedSeconds / 60, "min")
            elapsedSeconds < 86_400 -> short(elapsedSeconds / 3_600, "h")
            elapsedSeconds < 604_800 -> short(elapsedSeconds / 86_400, "d")
            elapsedSeconds < 2_592_000 -> short(elapsedSeconds / 604_800, "w")
            else -> "30+ d ago"
        }
    }

    private fun short(value: Long, unit: String): String = "$value $unit ago"
}
