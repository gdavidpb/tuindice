package com.gdavidpb.tuindice.record.presentation.model

import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.base.domain.model.subject.GradingMode
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus

data class AttemptItem(
	val attemptId: String,
	val termId: String,
	val grade: Int,
	val gradingMode: GradingMode = GradingMode.NUMERIC,
	val status: SubjectStatus? = null,
	val codeText: String,
	val nameText: String,
	val gradeText: String,
	val creditsText: String,
	val codeColor: Color,
	val codeContainerColor: Color,
	val isReadOnly: Boolean
)
