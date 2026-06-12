package com.gdavidpb.tuindice.base.ui.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

object TuIndiceDarkTheme {
	val Local: ProvidableCompositionLocal<Boolean?> = compositionLocalOf { null }

	@Composable
	fun isDark(): Boolean = Local.current ?: isSystemInDarkTheme()
}
