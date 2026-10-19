package com.gdavidpb.tuindice.about.presentation.mapper

import org.jetbrains.compose.resources.getString
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.about_assistant_consent_failed
import tuindice.about.generated.resources.about_assistant_conversations_delete_failed
import tuindice.about.generated.resources.about_assistant_conversations_deleted
import tuindice.about.generated.resources.about_share_message
import tuindice.about.generated.resources.about_share_subject
import tuindice.about.generated.resources.about_version_unavailable
import tuindice.about.generated.resources.label_privacy_policy
import tuindice.about.generated.resources.label_support
import tuindice.about.generated.resources.label_terms_and_conditions

internal suspend fun aboutAssistantConsentFailed(): String {
	return getString(Res.string.about_assistant_consent_failed)
}

internal suspend fun aboutAssistantConversationsDeleteFailed(): String {
	return getString(Res.string.about_assistant_conversations_delete_failed)
}

internal suspend fun aboutAssistantConversationsDeleted(): String {
	return getString(Res.string.about_assistant_conversations_deleted)
}

internal suspend fun aboutShareMessage(): String {
	return getString(Res.string.about_share_message)
}

internal suspend fun aboutShareSubject(): String {
	return getString(Res.string.about_share_subject)
}

internal suspend fun aboutVersionUnavailable(): String {
	return getString(Res.string.about_version_unavailable)
}

internal suspend fun labelPrivacyPolicy(): String {
	return getString(Res.string.label_privacy_policy)
}

internal suspend fun labelSupport(): String {
	return getString(Res.string.label_support)
}

internal suspend fun labelTermsAndConditions(): String {
	return getString(Res.string.label_terms_and_conditions)
}
