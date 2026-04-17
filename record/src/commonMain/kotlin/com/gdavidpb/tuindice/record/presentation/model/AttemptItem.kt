package com.gdavidpb.tuindice.record.presentation.model

import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.domain.model.GradingMode

data class AttemptItem(
	val attemptId: String,
	val subjectCode: String,
	val grade: Int,
	val gradingMode: GradingMode = GradingMode.NUMERIC,
	val outcome: AttemptOutcome? = null,
	val badge: AttemptBadge = AttemptBadge.NONE,
	val codeText: String,
	val nameText: String,
	val gradeText: String,
	val creditsText: String,
	val codeColor: Color,
	val codeContainerColor: Color,
	val isReadOnly: Boolean
)
