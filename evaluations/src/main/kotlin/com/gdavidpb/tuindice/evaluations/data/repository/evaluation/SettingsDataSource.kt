package com.gdavidpb.tuindice.evaluations.data.repository.evaluation

interface SettingsDataSource {
	suspend fun isGetEvaluationsOnCooldown(): Boolean
	suspend fun setGetEvaluationsOnCooldown()
}