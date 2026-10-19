package com.gdavidpb.tuindice.auth.presentation.mapper

import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.label_retry
import tuindice.auth.generated.resources.snack_password_updated
import tuindice.auth.generated.resources.title_privacy_policy
import tuindice.auth.generated.resources.title_terms_and_conditions

internal suspend fun labelRetry(): String {
	return getString(Res.string.label_retry)
}

internal suspend fun snackPasswordUpdated(): String {
	return getString(Res.string.snack_password_updated)
}

internal suspend fun titlePrivacyPolicy(): String {
	return getString(Res.string.title_privacy_policy)
}

internal suspend fun titleTermsAndConditions(): String {
	return getString(Res.string.title_terms_and_conditions)
}
