package com.gdavidpb.tuindice.auth.presentation.resource

import com.gdavidpb.tuindice.base.presentation.mapper.commonNetworkUnavailableMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonServiceUnavailableMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonTimeoutMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonUnexpectedErrorMessage
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.label_retry
import tuindice.auth.generated.resources.title_privacy_policy
import tuindice.auth.generated.resources.title_terms_and_conditions

class DefaultSignInSharedTextProvider : SignInSharedTextProvider {
	override suspend fun retryLabel(): String = getString(Res.string.label_retry)

	override suspend fun termsAndConditionsTitle(): String = getString(Res.string.title_terms_and_conditions)

	override suspend fun privacyPolicyTitle(): String = getString(Res.string.title_privacy_policy)

	override suspend fun serviceUnavailable(): String = commonServiceUnavailableMessage()

	override suspend fun networkUnavailable(): String = commonNetworkUnavailableMessage()

	override suspend fun timeout(): String = commonTimeoutMessage()

	override suspend fun unexpectedError(): String = commonUnexpectedErrorMessage()
}
