package com.gdavidpb.tuindice.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.gdavidpb.tuindice.base.ui.style.TuIndiceDarkTheme
import com.gdavidpb.tuindice.base.ui.style.TuIndiceShapes

@Composable
fun TuIndiceTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	dynamicColor: Boolean = false,
	content: @Composable () -> Unit
) {
	val colorScheme = when {
		dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
			val context = LocalContext.current
			if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
		}

		darkTheme -> TuIndiceColorScheme.dark
		else -> TuIndiceColorScheme.light
	}

	// Mirrors TuIndiceSharedTheme so both hosts resolve the same shapes and
	// dark-theme local; only the dynamic-color branch is Android-specific.
	CompositionLocalProvider(TuIndiceDarkTheme.Local provides darkTheme) {
		MaterialTheme(
			colorScheme = colorScheme,
			typography = TuIndiceTypography,
			shapes = TuIndiceShapes,
			content = content
		)
	}
}
