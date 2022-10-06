package com.gdavidpb.tuindice.base.domain.repository

interface SettingsRepository {
	fun isReviewSuggested(value: Int): Boolean

	fun saveSubscriptionTopic(topic: String)
	fun isSubscribedToTopic(topic: String): Boolean

	fun getLastScreen(): Int
	fun setLastScreen(screen: Int)

	fun clear()
}