package com.gdavidpb.tuindice.domain.repository

interface SettingsRepository {
    fun isReviewSuggested(value: Int): Boolean

    fun storeTopicSubscription(topic: String)
    fun isSubscribedToTopic(topic: String): Boolean

    fun getLastScreen(): Int
    fun setLastScreen(screen: Int)

    fun clear()
}