package com.sliide.useractivity.ui.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppNavigatorTest {
    @Test
    fun `starts at users route`() {
        val navigator = AppNavigator()

        assertEquals(AppRoute.Users, navigator.currentRoute)
        assertFalse(navigator.canGoBack)
        assertNull(navigator.selectedUserId)
    }

    @Test
    fun `navigate pushes route and updates selection`() {
        val navigator = AppNavigator()

        navigator.navigate(AppRoute.UserDetail(2))

        assertEquals(AppRoute.UserDetail(2), navigator.currentRoute)
        assertTrue(navigator.canGoBack)
        assertEquals(2, navigator.selectedUserId)
    }

    @Test
    fun `back returns to previous route`() {
        val navigator = AppNavigator()
        navigator.navigate(AppRoute.UserDetail(2))
        navigator.navigate(AppRoute.PostDetail(userId = 2, postId = 101))

        val didGoBack = navigator.goBack()

        assertTrue(didGoBack)
        assertEquals(AppRoute.UserDetail(2), navigator.currentRoute)
        assertEquals(2, navigator.selectedUserId)
    }

    @Test
    fun `root back is no op`() {
        val navigator = AppNavigator()

        assertFalse(navigator.goBack())
        assertEquals(AppRoute.Users, navigator.currentRoute)
    }

    @Test
    fun `reset clears stack and selections`() {
        val navigator = AppNavigator()
        navigator.navigate(AppRoute.PostDetail(userId = 2, postId = 101))

        navigator.resetToUsers()

        assertEquals(AppRoute.Users, navigator.currentRoute)
        assertFalse(navigator.canGoBack)
        assertNull(navigator.selectedUserId)
        assertNull(navigator.selectedPostId)
    }
}
