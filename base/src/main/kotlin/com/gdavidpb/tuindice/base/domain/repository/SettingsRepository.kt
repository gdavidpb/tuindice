package com.gdavidpb.tuindice.base.domain.repository

interface SettingsRepository {
	fun isReviewSuggested(value: Int): Boolean

	fun getLastScreen(): String
	fun setLastScreen(route: String)

	fun getActiveToken(): String?
	fun setActiveToken(token: String)

	fun clear()
}