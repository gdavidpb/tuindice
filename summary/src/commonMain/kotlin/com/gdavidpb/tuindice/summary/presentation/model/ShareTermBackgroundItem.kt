package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable

/** A background of the card in the picker: the [artwork] itself, and whether it is the one [selected]. */
@Immutable
data class ShareTermBackgroundItem(
	val artwork: ShareTermArtwork,
	val selected: Boolean
)
