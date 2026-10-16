package com.gdavidpb.tuindice.base.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

// The system's "square.and.arrow.up" drawn in its 24-point grid: an open box, with the arrow leaving it upwards through
// the gap in its top edge. The icon is a stroke, so the tint of its caller paints it.
private val shareIosIcon: ImageVector by lazy {
	ImageVector.Builder(
		name = "square.and.arrow.up",
		defaultWidth = 24.dp,
		defaultHeight = 24.dp,
		viewportWidth = 24f,
		viewportHeight = 24f
	)
		.addPath(
			pathData = addPathNodes("M8.5 9H5V20H19V9H15.5"),
			stroke = SolidColor(Color.Black),
			strokeLineWidth = SHARE_STROKE_WIDTH,
			strokeLineCap = StrokeCap.Round,
			strokeLineJoin = StrokeJoin.Round
		)
		.addPath(
			pathData = addPathNodes("M12 15V3.5M8.5 7L12 3.5L15.5 7"),
			stroke = SolidColor(Color.Black),
			strokeLineWidth = SHARE_STROKE_WIDTH,
			strokeLineCap = StrokeCap.Round,
			strokeLineJoin = StrokeJoin.Round
		)
		.build()
}

private const val SHARE_STROKE_WIDTH = 1.75f

actual val ShareIcon: ImageVector
	get() = shareIosIcon
