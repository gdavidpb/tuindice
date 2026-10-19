package com.gdavidpb.tuindice.auth.presentation.mapper

import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.presentation.resource.SignInSharedTextProvider
import com.gdavidpb.tuindice.auth.presentation.resource.SignInTextProvider

internal suspend fun SignInUseCaseError?.toErrorMessage(
	textProvider: SignInTextProvider,
	sharedTextProvider: SignInSharedTextProvider,
	identifierMode: SignInIdentifierMode,
	supportEmail: String
): String {
	return when (this) {
		is SignInUseCaseError.InvalidCredentials ->
			when (identifierMode) {
				SignInIdentifierMode.UsbId ->
					textProvider.invalidUsbIdCredentials()

				SignInIdentifierMode.UsbEmail ->
					textProvider.invalidUsbEmailCredentials()
			}

		// Terminal outcomes the user cannot solve alone: both point at support.
		is SignInUseCaseError.AccountDisabled ->
			textProvider.accountDisabled(supportEmail)

		is SignInUseCaseError.Untrusted ->
			textProvider.untrusted(supportEmail)

		is SignInUseCaseError.AuthenticationFailed ->
			textProvider.authenticationFailed()

		is SignInUseCaseError.NoConnection ->
			if (isNetworkAvailable) {
				sharedTextProvider.serviceUnavailable()
			} else {
				sharedTextProvider.networkUnavailable()
			}

		is SignInUseCaseError.Timeout ->
			sharedTextProvider.timeout()

		// Fault belongs to the university's upstream, not TuIndice — say so, otherwise a
		// new user with no other context reads the generic message as our app being broken.
		is SignInUseCaseError.Unavailable ->
			textProvider.usbUnavailable()

		is SignInUseCaseError.TooManyRequests ->
			textProvider.tooManyRequests()

		else ->
			sharedTextProvider.unexpectedError()
	}
}
