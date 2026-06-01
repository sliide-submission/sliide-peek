package com.sliide.useractivity.ui.shell

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class AppLayoutTest {
    @Test
    fun `width below threshold is compact`() {
        assertEquals(AppLayoutClass.Compact, appLayoutClassForWidth(699.dp))
    }

    @Test
    fun `width at threshold is expanded`() {
        assertEquals(AppLayoutClass.Expanded, appLayoutClassForWidth(700.dp))
    }

    @Test
    fun `master pane uses narrow width for constrained tablet`() {
        assertEquals(260.dp, masterPaneWidthFor(760.dp))
    }

    @Test
    fun `master pane uses full width for wide tablet`() {
        assertEquals(320.dp, masterPaneWidthFor(920.dp))
    }
}
