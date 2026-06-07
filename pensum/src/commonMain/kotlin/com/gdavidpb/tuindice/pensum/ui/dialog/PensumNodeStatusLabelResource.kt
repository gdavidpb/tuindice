package com.gdavidpb.tuindice.pensum.ui.dialog

import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusDisplay
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import org.jetbrains.compose.resources.StringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_canvas_legend_approved
import tuindice.pensum.generated.resources.pensum_canvas_legend_available
import tuindice.pensum.generated.resources.pensum_canvas_legend_blocked
import tuindice.pensum.generated.resources.pensum_canvas_legend_current

internal fun PensumNodeStatusDisplay.labelResource(): StringResource {
	return when {
		type == PensumNodeStatusType.APPROVED -> Res.string.pensum_canvas_legend_approved
		type == PensumNodeStatusType.CURRENT -> Res.string.pensum_canvas_legend_current
		type == PensumNodeStatusType.BLOCKED -> Res.string.pensum_canvas_legend_blocked
		else -> Res.string.pensum_canvas_legend_available
	}
}
