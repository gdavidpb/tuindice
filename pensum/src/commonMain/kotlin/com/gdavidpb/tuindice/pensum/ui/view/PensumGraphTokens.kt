package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceDarkTheme

private val LightPensumCanvasTermBand = Color(0xFFF4F4F4)
private val LightPensumPanelBorder = Color(0xFFD8D8D8)
private val LightPensumTextSecondary = Color(0xFF5F6368)
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
	val panelBackground = colorScheme.surfaceContainerLow

	return if (TuIndiceDarkTheme.isDark()) {
		PensumGraphColors(
			isDark = true,
			screenBackground = colorScheme.background,
			canvasBackground = colorScheme.background,
			canvasTermBand = colorScheme.surfaceVariant.copy(alpha = 0.42f),
			panelBackground = panelBackground,
			panelBorder = colorScheme.outlineVariant,
			floatingPanelBackground = panelBackground,
			approved = AcademicStatusColors.ApprovedDark,
			current = colorScheme.primary,
			available = AcademicStatusColors.AvailableDark,
			blocked = AcademicStatusColors.BlockedDark,
			canvasNeutral = AcademicStatusColors.AvailableDark,
			selected = colorScheme.onSurface,
			textPrimary = colorScheme.onSurface,
			textSecondary = colorScheme.onSurfaceVariant,
			nodeContainer = panelBackground,
			controlsBackground = panelBackground,
			minimapBackground = panelBackground
		)
	} else {
		PensumGraphColors(
			isDark = false,
			screenBackground = colorScheme.background,
			canvasBackground = colorScheme.background,
			canvasTermBand = LightPensumCanvasTermBand.copy(alpha = 0.72f),
			panelBackground = panelBackground,
			panelBorder = LightPensumPanelBorder,
			floatingPanelBackground = panelBackground,
			approved = AcademicStatusColors.ApprovedLight,
			current = colorScheme.primary,
			available = AcademicStatusColors.AvailableLight,
			blocked = AcademicStatusColors.BlockedLight,
			canvasNeutral = AcademicStatusColors.AvailableLight,
			selected = LightPensumTextSecondary,
			textPrimary = colorScheme.onSurface,
			textSecondary = LightPensumTextSecondary,
			nodeContainer = panelBackground,
			controlsBackground = panelBackground,
			minimapBackground = panelBackground
		)
	}
}

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
