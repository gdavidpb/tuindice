package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.runtime.Composable

interface EvaluationItemMappingProvider {
	@Composable
	fun rememberMapping(): EvaluationItemMapping
}
