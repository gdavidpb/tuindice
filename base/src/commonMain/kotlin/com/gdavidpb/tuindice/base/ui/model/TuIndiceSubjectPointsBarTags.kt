package com.gdavidpb.tuindice.base.ui.model

// The test tags of a subject's points bar: the part it has earned, and the part evaluated and not earned yet, as a
// screen test names them. The tag of the bar itself goes on the modifier of the bar.
data class TuIndiceSubjectPointsBarTags(
	val earned: String? = null,
	val evaluated: String? = null
)
