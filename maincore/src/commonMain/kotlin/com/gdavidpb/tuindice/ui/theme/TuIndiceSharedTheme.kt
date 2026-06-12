package com.gdavidpb.tuindice.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.gdavidpb.tuindice.base.ui.style.TuIndiceDarkTheme
import com.gdavidpb.tuindice.base.ui.style.TuIndiceShapes

@Composable
fun TuIndiceSharedTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	val colorScheme = if (darkTheme) TuIndiceColorScheme.dark else TuIndiceColorScheme.light

	CompositionLocalProvider(TuIndiceDarkTheme.Local provides darkTheme) {
		MaterialTheme(
			colorScheme = colorScheme,
			typography = TuIndiceTypography,
			shapes = TuIndiceShapes,
			content = content
		)
	}
}
