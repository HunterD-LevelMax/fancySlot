package com.slotmachineapp.fancyslotmachine.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SlotColorScheme = darkColorScheme(
    primary = SlotPrimary,
    background = SlotBackground,
    surface = SlotSurface,
    onSurface = SlotOnSurface
)

@Composable
fun FancySlotMachineTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SlotColorScheme,
        typography = Typography,
        content = content
    )
}
