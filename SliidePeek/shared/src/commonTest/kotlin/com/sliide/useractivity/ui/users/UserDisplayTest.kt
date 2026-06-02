package com.sliide.useractivity.ui.users

import kotlin.test.Test
import kotlin.test.assertEquals

class UserDisplayTest {

    @Test
    fun initials_takes_first_letters_of_first_two_words() {
        assertEquals("PN", userInitials("Priya Nair"))
        assertEquals("AS", userInitials("aarav sharma"))
    }

    @Test
    fun initials_uses_a_single_word_first_letter() {
        assertEquals("M", userInitials("Maya"))
    }

    @Test
    fun initials_ignores_extra_whitespace_and_third_word() {
        assertEquals("MR", userInitials("  Maya   Reed  Smith "))
    }

    @Test
    fun initials_fall_back_to_question_mark_when_blank() {
        assertEquals("?", userInitials("   "))
        assertEquals("?", userInitials(""))
    }

    @Test
    fun short_relative_time_compacts_common_phrases() {
        assertEquals("now", shortRelativeTime("just now"))
        assertEquals("5min", shortRelativeTime("5 min ago"))
        assertEquals("2h", shortRelativeTime("2 h ago"))
        assertEquals("1d", shortRelativeTime("1 d ago"))
    }
}
