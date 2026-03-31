package com.gdavidpb.tuindice.evaluations.data.contract


interface SettingsDataSource {
	suspend fun isGetEvaluationsOnCooldown(): Boolean
	suspend fun setGetEvaluationsOnCooldown()
}