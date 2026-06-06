package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.shape.RoundedCornerShape
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

internal const val MinCanvasZoom = 0.18f
internal const val MinCanvasFitZoom = 0.08f
internal const val MaxCanvasZoom = 2.25f
internal const val InitialCanvasZoom = 0.74f
internal const val NodeFocusMinZoom = 0.92f
internal const val ProgressFocusMaxZoom = 1.18f
internal const val TermFocusMaxZoom = 1.18f
internal const val DoubleTapCanvasZoom = 1.45f
internal const val DoubleTapZoomOutThreshold = 1.05f
internal const val ZoomButtonStep = 0.18f
internal const val CanvasFitScaleTolerance = 0.002f
internal const val ZoomAnimationMillis = 220
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
internal val StickyTermFullLabelMinWidth = 112.dp
internal val StickyTermMinWidth = 72.dp
internal val ZoomControlButtonHeight = 48.dp
internal val CanvasFitStateTolerance = 1.dp
internal val EdgeEndpointGap = 0.dp
internal val EdgeCornerRadius = 14.dp
internal val EdgeRerouteSpacing = 32.dp
internal val DisconnectedEdgeDashLength = 14.dp
internal val DisconnectedEdgeDashGap = 10.dp
internal val ArrowHeadLength = 12.dp
