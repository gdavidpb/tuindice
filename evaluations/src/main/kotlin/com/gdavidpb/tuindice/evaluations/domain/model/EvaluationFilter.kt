package com.gdavidpb.tuindice.evaluations.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class EvaluationFilter(
	val name: String,
	val icon: ImageVector,
	val entries: List<EvaluationFilterEntry>
)