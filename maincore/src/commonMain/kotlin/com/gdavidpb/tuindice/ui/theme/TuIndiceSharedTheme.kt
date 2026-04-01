package com.gdavidpb.tuindice.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val lightColorScheme = lightColorScheme(
	primary = Color(0xFFFABD00),
	onPrimary = Color(0xFFFFFFFF),
	primaryContainer = Color(0xFFFFDF9E),
	onPrimaryContainer = Color(0xFF5B4300),
	secondary = Color(0xFF6B5D3F),
	onSecondary = Color(0xFFFFFFFF),
	secondaryContainer = Color(0xFFF5E0BB),
	onSecondaryContainer = Color(0xFF241A04),
	tertiary = Color(0xFF006781),
	onTertiary = Color(0xFFFFFFFF),
	tertiaryContainer = Color(0xFFB9EAFF),
	onTertiaryContainer = Color(0xFF001F29),
	error = Color(0xFFBA1A1A),
	errorContainer = Color(0xFFFFDAD6),
	onError = Color(0xFFFFFFFF),
	onErrorContainer = Color(0xFF410002),
	background = Color(0xFFFFFBFF),
	onBackground = Color(0xFF1E1B16),
	surface = Color(0xFFFFFBFF),
	onSurface = Color(0xFF1E1B16),
	surfaceVariant = Color(0xFFEDE1CF),
	onSurfaceVariant = Color(0xFF4D4639),
	outline = Color(0xFF7F7667),
	inverseOnSurface = Color(0xFFF7EFE7),
	inverseSurface = Color(0xFF33302A),
	inversePrimary = Color(0xFFFABD00),
	surfaceTint = Color(0xFF785900),
	outlineVariant = Color(0xFFD0C5B4),
	scrim = Color(0xFF000000),
)

private val darkColorScheme = darkColorScheme(
	primary = Color(0xFFFABD00),
	onPrimary = Color(0xFF231A00),
	primaryContainer = Color(0xFF3B2C00),
	onPrimaryContainer = Color(0xFFFFF1BF),
	secondary = Color(0xFFD8B44A),
	onSecondary = Color(0xFF131311),
	secondaryContainer = Color(0xFF23211D),
	onSecondaryContainer = Color(0xFFF4F1E8),
	tertiary = Color(0xFFE8E3D7),
	onTertiary = Color(0xFF171613),
	tertiaryContainer = Color(0xFF2A2722),
	onTertiaryContainer = Color(0xFFF4F1E8),
	error = Color(0xFFFFB4AB),
	errorContainer = Color(0xFF93000A),
	onError = Color(0xFF690005),
	onErrorContainer = Color(0xFFFFDAD6),
	background = Color(0xFF0B0B0A),
	onBackground = Color(0xFFF4F1E8),
	surface = Color(0xFF131311),
	onSurface = Color(0xFFF4F1E8),
	surfaceVariant = Color(0xFF1D1D1A),
	onSurfaceVariant = Color(0xFFC9C3B6),
	outline = Color(0xFF6F695D),
	inverseOnSurface = Color(0xFF131311),
	inverseSurface = Color(0xFFF4F1E8),
	inversePrimary = Color(0xFF8C6700),
	surfaceTint = Color(0xFFFABD00),
	outlineVariant = Color(0xFF34312B),
	scrim = Color(0xFF000000),
)

@Composable
fun TuIndiceSharedTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	val colorScheme = if (darkTheme) darkColorScheme else lightColorScheme

	MaterialTheme(
		colorScheme = colorScheme,
		typography = Typography(),
		content = content
	)
}
