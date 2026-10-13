package com.gdavidpb.tuindice.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object TuIndiceColorScheme {
	// Two-tone rule: the screen, sheets and dialogs sit on `surface`; every card, row or panel on
	// them is `surfaceVariant`, without shadow; a small element inside a card is `surfaceDim`.
	// `secondaryContainer` is only the selected state. Material's container defaults land on the
	// same tones: sheets and dialogs take `surface`, Card takes `surfaceVariant`.
	val light = lightColorScheme(
		primary = Color(0xFFFABD00),
		onPrimary = Color(0xFF231A00),
		primaryContainer = Color(0xFFFFDF9E),
		onPrimaryContainer = Color(0xFF5B4300),
		secondary = Color(0xFF6B5D3F),
		onSecondary = Color(0xFFFFFFFF),
		secondaryContainer = Color(0xFFDDDAD0),
		onSecondaryContainer = Color(0xFF241A04),
		tertiary = Color(0xFF006781),
		onTertiary = Color(0xFFFFFFFF),
		tertiaryContainer = Color(0xFFB9EAFF),
		onTertiaryContainer = Color(0xFF001F29),
		error = Color(0xFFBA1A1A),
		errorContainer = Color(0xFFFFDAD6),
		onError = Color(0xFFFFFFFF),
		onErrorContainer = Color(0xFF410002),
		background = Color(0xFFFBFAF7),
		onBackground = Color(0xFF1E1B16),
		surface = Color(0xFFFBFAF7),
		onSurface = Color(0xFF1E1B16),
		surfaceVariant = Color(0xFFF1F0EB),
		onSurfaceVariant = Color(0xFF54514A),
		outline = Color(0xFF85827A),
		inverseOnSurface = Color(0xFFF3F2EE),
		inverseSurface = Color(0xFF32312D),
		inversePrimary = Color(0xFFFABD00),
		surfaceTint = Color(0xFF785900),
		outlineVariant = Color(0xFFDDDBD3),
		surfaceContainerLowest = Color(0xFFFBFAF7),
		surfaceContainerLow = Color(0xFFFBFAF7),
		surfaceContainer = Color(0xFFF1F0EB),
		surfaceContainerHigh = Color(0xFFFBFAF7),
		surfaceContainerHighest = Color(0xFFF1F0EB),
		surfaceBright = Color(0xFFFBFAF7),
		surfaceDim = Color(0xFFE5E4DD),
		scrim = Color(0xFF000000),
	)

	val dark = darkColorScheme(
		primary = Color(0xFFFABD00),
		onPrimary = Color(0xFF231A00),
		primaryContainer = Color(0xFF3B2C00),
		onPrimaryContainer = Color(0xFFFFF1BF),
		secondary = Color(0xFFD8B44A),
		onSecondary = Color(0xFF131311),
		secondaryContainer = Color(0xFF34322C),
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
		surfaceContainerLowest = Color(0xFF131311),
		surfaceContainerLow = Color(0xFF131311),
		surfaceContainer = Color(0xFF1D1D1A),
		surfaceContainerHigh = Color(0xFF131311),
		surfaceContainerHighest = Color(0xFF1D1D1A),
		surfaceBright = Color(0xFF131311),
		surfaceDim = Color(0xFF2A2A26),
		scrim = Color(0xFF000000),
	)
}
