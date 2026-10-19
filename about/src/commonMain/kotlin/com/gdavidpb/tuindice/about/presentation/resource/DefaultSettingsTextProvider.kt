package com.gdavidpb.tuindice.about.presentation.resource

import org.jetbrains.compose.resources.getString
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.settings_assistant_consent_failed
import tuindice.about.generated.resources.settings_assistant_conversations_delete_failed
import tuindice.about.generated.resources.settings_assistant_conversations_deleted
import tuindice.about.generated.resources.settings_share_message
import tuindice.about.generated.resources.settings_share_subject
import tuindice.about.generated.resources.about_version_unavailable
import tuindice.about.generated.resources.label_privacy_policy
import tuindice.about.generated.resources.label_support
import tuindice.about.generated.resources.label_terms_and_conditions

class DefaultSettingsTextProvider : SettingsTextProvider {
	override suspend fun versionUnavailable(): String = getString(Res.string.about_version_unavailable)

	override suspend fun termsAndConditionsLabel(): String = getString(Res.string.label_terms_and_conditions)

	override suspend fun privacyPolicyLabel(): String = getString(Res.string.label_privacy_policy)

	override suspend fun supportLabel(): String = getString(Res.string.label_support)

	override suspend fun shareSubject(): String = getString(Res.string.settings_share_subject)

	override suspend fun shareMessage(): String = getString(Res.string.settings_share_message)

	override suspend fun assistantConsentFailed(): String = getString(Res.string.settings_assistant_consent_failed)

	override suspend fun assistantConversationsDeleted(): String =
		getString(Res.string.settings_assistant_conversations_deleted)

	override suspend fun assistantConversationsDeleteFailed(): String =
		getString(Res.string.settings_assistant_conversations_delete_failed)
}
