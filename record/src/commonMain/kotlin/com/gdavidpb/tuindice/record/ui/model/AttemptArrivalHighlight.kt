package com.gdavidpb.tuindice.record.ui.model

import androidx.compose.runtime.Stable

/**
 * The arrival highlight a card plays: [trigger] starts it (each arrival is a new object, so a repeated jump plays
 * it again), and [onFinished] runs when it ends, so whoever brought the arrival can stop asking for it.
 */
@Stable
class AttemptArrivalHighlight(
	val trigger: Any,
	val onFinished: () -> Unit
)
