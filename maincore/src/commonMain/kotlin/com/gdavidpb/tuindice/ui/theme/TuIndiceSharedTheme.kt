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
	onPrimary = Color(0xFF3F2E00),
	primaryContainer = Color(0xFF5B4300),
	onPrimaryContainer = Color(0xFFFFDF9E),
	secondary = Color(0xFFD8C4A0),
	onSecondary = Color(0xFF3A2F15),
	secondaryContainer = Color(0xFF52452A),
	onSecondaryContainer = Color(0xFFF5E0BB),
	tertiary = Color(0xFF5FD4FD),
	onTertiary = Color(0xFF003544),
	tertiaryContainer = Color(0xFF004D62),
	onTertiaryContainer = Color(0xFFB9EAFF),
	error = Color(0xFFFFB4AB),
	errorContainer = Color(0xFF93000A),
	onError = Color(0xFF690005),
	onErrorContainer = Color(0xFFFFDAD6),
	background = Color(0xFF1E1B16),
	onBackground = Color(0xFFE9E1D8),
	surface = Color(0xFF1E1B16),
	onSurface = Color(0xFFE9E1D8),
	surfaceVariant = Color(0xFF4D4639),
	onSurfaceVariant = Color(0xFFD0C5B4),
	outline = Color(0xFF998F80),
	inverseOnSurface = Color(0xFF1E1B16),
	inverseSurface = Color(0xFFE9E1D8),
	inversePrimary = Color(0xFF785900),
	surfaceTint = Color(0xFFFABD00),
	outlineVariant = Color(0xFF4D4639),
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
