package com.gdavidpb.tuindice.evaluations.data.repository


interface SettingsDataRepository {
	suspend fun isGetEvaluationsOnCooldown(): Boolean
	suspend fun setGetEvaluationsOnCooldown()
}