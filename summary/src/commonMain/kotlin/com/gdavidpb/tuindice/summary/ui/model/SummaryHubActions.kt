package com.gdavidpb.tuindice.summary.ui.model

import androidx.compose.runtime.Stable
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget

/** What the lines of the hub ask for when they are tapped: the screen they lead to. */
@Stable
class SummaryHubActions(
	val onTargetClick: (target: SummaryHubTarget) -> Unit = {}
)
