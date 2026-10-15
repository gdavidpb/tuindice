package com.gdavidpb.tuindice.pensum.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
internal data class PensumGraphColors(
	val isDark: Boolean,
	val screenBackground: Color,
	val canvasBackground: Color,
	val canvasTermBand: Color,
	val panelBackground: Color,
	val panelBorder: Color,
	val floatingPanelBackground: Color,
	val approved: Color,
	val current: Color,
	val available: Color,
	val blocked: Color,
	val canvasNeutral: Color,
	val selected: Color,
	val textPrimary: Color,
	val textSecondary: Color,
	val nodeContainer: Color,
	val controlsBackground: Color,
	val minimapBackground: Color
)
