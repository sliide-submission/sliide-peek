package com.sliide.useractivity.domain.time

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

interface AppClock {
    fun nowMillis(): Long
}

class SystemAppClock : AppClock {
    @OptIn(ExperimentalTime::class)
    override fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
