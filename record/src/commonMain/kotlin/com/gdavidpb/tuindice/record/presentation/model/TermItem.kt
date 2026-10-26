package com.gdavidpb.tuindice.record.presentation.model

data class TermItem(
	val termId: String,
	val periodYear: Int,
	val termOrder: Int,
	val shortNameText: String,
	val kind: TermItemKind,
	// The values the term's metrics animate toward. The views do the animating, so the mapper only says where they end.
	val gradeTarget: Float,
	val gradeSumTarget: Float,
	val creditsTarget: Int,
	// The older term's values, set only where this term shows its changes: it has attempts and an older term exists.
	val previousGradeTarget: Float?,
	val previousGradeSumTarget: Float?,
	val previousCreditsTarget: Int?,
	val isCurrent: Boolean,
	val canDelete: Boolean,
	val canEdit: Boolean,
	val attempts: List<AttemptItem>,
	// The notice this term's page carries: only the current term has one, and only when it speaks
	// of that term.
	val notice: RecordNotice? = null,
	// The attempt a jump brought in, when this term lists it: its page takes it once.
	val arrivalAttemptId: String? = null
)
