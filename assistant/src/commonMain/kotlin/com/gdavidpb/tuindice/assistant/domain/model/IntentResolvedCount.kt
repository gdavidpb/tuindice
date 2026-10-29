package com.gdavidpb.tuindice.assistant.domain.model

/**
 * The rows of an intent that adds evaluations: [applied] of the [selected] rows the student marked were applied. The
 * receipt of the intent reads it, so it says what was added.
 */
data class IntentResolvedCount(val applied: Int, val selected: Int)
