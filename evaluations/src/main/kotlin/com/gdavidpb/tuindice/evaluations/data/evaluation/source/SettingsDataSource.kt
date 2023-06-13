package com.gdavidpb.tuindice.evaluations.data.evaluation.source

interface SettingsDataSource {
	suspend fun isGetEvaluationsOnCooldown(): Boolean
	suspend fun setGetEvaluationsOnCooldown()
}