package com.gdavidpb.tuindice.evaluations.data.repository

interface SettingsDataSource {
	suspend fun isGetEvaluationsOnCooldown(): Boolean
	suspend fun setGetEvaluationsOnCooldown()
}