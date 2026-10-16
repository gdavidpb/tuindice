package com.gdavidpb.tuindice.sampleflow.presentation.route

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextStyle
import com.gdavidpb.tuindice.sampleflow.ui.view.SampleFlowBadge

// Control negativo: route monta pantallas y queda fuera del alcance de las reglas de importación de presentación.
fun sampleFlowRouteControl(bitmap: ImageBitmap, style: TextStyle, badge: SampleFlowBadge): Any =
	Triple(bitmap, style, badge)
