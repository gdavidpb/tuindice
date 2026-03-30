package com.gdavidpb.tuindice.evaluations.data.source

interface SettingsDataSource {
	suspend fun isGetEvaluationsOnCooldown(): Boolean
	suspend fun setGetEvaluationsOnCooldown()
}