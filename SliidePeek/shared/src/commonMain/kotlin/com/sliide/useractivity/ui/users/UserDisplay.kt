package com.sliide.useractivity.ui.users

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
