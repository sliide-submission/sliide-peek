package com.sliide.useractivity.ui.users

import com.sliide.useractivity.domain.model.UserGender
import com.sliide.useractivity.domain.model.UserStatus

internal val UserStatus.label: String
    get() = when (this) {
        UserStatus.Active -> "Active"
        UserStatus.Inactive -> "Inactive"
        UserStatus.Unknown -> "Unknown"
    }

internal val UserGender.label: String
    get() = when (this) {
        UserGender.Male -> "Male"
        UserGender.Female -> "Female"
        UserGender.Unknown -> "Unknown"
    }

/** Avatar initials: first letters of the first two words, uppercased. Falls back to "?". */
fun userInitials(name: String): String = name.trim()
    .split(Regex("\\s+"))
    .filter { it.isNotBlank() }
    .take(2)
    .joinToString("") { it.first().uppercase() }
    .ifBlank { "?" }

/** Compact relative time for tight (tablet) rows, e.g. "5 min ago" → "5min", "just now" → "now". */
fun shortRelativeTime(value: String): String = value
    .replace("just now", "now")
    .replace(" ago", "")
    .replace(" ", "")
