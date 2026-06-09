package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal val ScreenBackground = Color(0xFF101112)
internal val PanelBackground = Color(0xFF171819)
internal val PanelBorder = Color(0xFF343638)
internal val FloatingPanelBackground = Color.Black
internal val Approved = Color(0xFF8FE38C)
internal val Current = Color(0xFFFFC400)
internal val Available = Color(0xFF8A8F94)
internal val CanvasNeutral = Available
internal val Selected = Color(0xFFF7F7F7)
internal val TextPrimary = Color(0xFFF7F7F7)
internal val TextSecondary = Color(0xFF9C9EA3)
internal val PensumElementCornerRadius = 8.dp
internal val PensumElementShape = RoundedCornerShape(PensumElementCornerRadius)

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

@Composable
internal fun pensumGraphColors(): PensumGraphColors {
	val colorScheme = MaterialTheme.colorScheme

	return if (isSystemInDarkTheme()) {
		DarkPensumGraphColors
	} else {
		PensumGraphColors(
			isDark = false,
			screenBackground = colorScheme.background,
			canvasBackground = colorScheme.background,
			canvasTermBand = colorScheme.surfaceVariant.copy(alpha = 0.18f),
			panelBackground = colorScheme.surface,
			panelBorder = colorScheme.outlineVariant,
			floatingPanelBackground = colorScheme.surface,
			approved = Color(0xFF2E7D32),
			current = Color(0xFF8C6700),
			available = colorScheme.outline,
			blocked = colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
			canvasNeutral = colorScheme.outline,
			selected = colorScheme.onSurface,
			textPrimary = colorScheme.onSurface,
			textSecondary = colorScheme.onSurfaceVariant,
			nodeContainer = colorScheme.surface,
			controlsBackground = colorScheme.surface.copy(alpha = 0.92f),
			minimapBackground = colorScheme.surface.copy(alpha = 0.92f)
		)
	}
}

private val DarkPensumGraphColors = PensumGraphColors(
	isDark = true,
	screenBackground = ScreenBackground,
	canvasBackground = Color(0xFF141516),
	canvasTermBand = PanelBackground.copy(alpha = 0.42f),
	panelBackground = PanelBackground,
	panelBorder = PanelBorder,
	floatingPanelBackground = FloatingPanelBackground,
	approved = Approved,
	current = Current,
	available = Available,
	blocked = CanvasNeutral.copy(alpha = 0.72f),
	canvasNeutral = CanvasNeutral,
	selected = Selected,
	textPrimary = TextPrimary,
	textSecondary = TextSecondary,
	nodeContainer = PanelBackground,
	controlsBackground = Color.Black.copy(alpha = 0.68f),
	minimapBackground = Color.Black.copy(alpha = 0.62f)
)

internal const val MinCanvasZoom = 0.18f
internal const val MinCanvasFitZoom = 0.08f
internal const val MaxCanvasZoom = 2.25f
internal const val InitialCanvasZoom = 0.74f
internal const val NodeFocusMinZoom = 0.92f
internal const val ProgressFocusMaxZoom = 1.18f
internal const val TermFocusMaxZoom = 1.18f
internal const val DoubleTapCanvasZoom = 1.45f
internal const val DoubleTapZoomOutThreshold = 1.05f
internal const val ZoomControlStepCount = 8
internal const val CanvasFitScaleTolerance = 0.002f
internal const val ZoomAnimationMillis = 220
internal const val CanvasOverlayAnimationMillis = 180
internal const val CanvasManualGestureIdleMillis = 320
internal const val CanvasSnapDelayMillis = 260
internal const val SummaryAnimationMillis = 700
internal val InitialCanvasOffset = 18.dp
internal val CanvasPanMargin = 36.dp
internal val CanvasFitPadding = 24.dp
internal val CanvasFocusPadding = 64.dp
internal val CanvasSnapDistance = 26.dp
internal val CanvasSnapViewportInset = 18.dp
internal val CanvasBottomOverlayPadding = 88.dp
internal val MinimapWidth = 156.dp
internal val MinimapHeight = 104.dp
internal val StickyTermHeaderHeight = 38.dp
internal const val StickyTermHeaderShortMinZoom = 0.34f
internal const val StickyTermHeaderFullMinZoom = 0.50f
internal val StickyTermFullLabelMinWidth = 112.dp
internal val StickyTermShortLabelMinWidth = 40.dp
internal val ZoomControlButtonHeight = 48.dp
internal val CanvasFitStateTolerance = 1.dp
internal val EdgeEndpointGap = 0.dp
internal val EdgeCornerRadius = 14.dp
internal val EdgeRerouteSpacing = 32.dp
internal val DisconnectedEdgeDashLength = 14.dp
internal val DisconnectedEdgeDashGap = 10.dp
internal val ArrowHeadLength = 12.dp
