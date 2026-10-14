package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground

/** A background of the card in the picker: the [background] itself, and whether it is the one [selected]. */
@Immutable
data class ShareTermBackgroundItem(
	val background: ShareBackground,
	val selected: Boolean
)
