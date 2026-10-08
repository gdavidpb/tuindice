package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.auth.utils.extension.isUsbEmail
import com.gdavidpb.tuindice.auth.utils.extension.isUsbId
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.AppLogoView
import com.gdavidpb.tuindice.base.ui.view.toBoldMarkerAnnotatedText
import org.jetbrains.compose.resources.stringResource
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.sign_in_service_unavailable

@Composable
fun SignInIdleView(
	state: SignIn.State.Idle,
	onUsbIdChange: (usbId: String) -> Unit,
	onPasswordChange: (password: String) -> Unit,
	onPasswordVisibilityToggle: () -> Unit,
	onIdentifierModeToggle: () -> Unit,
	onUsageDataCollectionEnabledChange: (enabled: Boolean) -> Unit = {},
	onSignInClick: () -> Unit,
	onTermsAndConditionsClick: () -> Unit,
	onPrivacyPolicyClick: () -> Unit,
	termsAndConditionsText: String,
	privacyPolicyText: String,
	policiesText: String,
	usbIdLabelText: String,
	usbEmailLabelText: String,
	usbIdPlaceholderText: String,
	usbEmailPlaceholderText: String,
	useUsbEmailContentDescription: String,
	useUsbIdContentDescription: String,
	passwordLabelText: String,
	usageDataConsentText: String = "",
	signInButtonText: String,
	isWaiting: Boolean
) {
	val focusManager = LocalFocusManager.current
	val passwordFocusRequester = remember { FocusRequester() }
	val isValidIdentifier = when (state.identifierMode) {
		SignInIdentifierMode.UsbId -> state.usbId.isUsbId()
		SignInIdentifierMode.UsbEmail -> state.usbId.isUsbEmail()
	}
	val rejection = state.rejection
	val fixedRejection = rejection.takeIfFixedUnderTheButton()
	val isSignInEnabled = isValidIdentifier && state.password.isNotEmpty() && !state.isServiceUnavailable
	val policyIntroText = policiesText.substringBefore(termsAndConditionsText).trimEnd()
	val policyTextStyle = TextStyle(
		textAlign = TextAlign.Center,
		color = MaterialTheme.colorScheme.onBackground
	) + MaterialTheme.typography.bodyMedium
	val policyLinkStyle = policyTextStyle + SpanStyle(
		color = MaterialTheme.colorScheme.surfaceTint,
		textDecoration = TextDecoration.Underline
	)

	Column(
		modifier = Modifier
			.testTag(AuthUiTags.SignInIdleContainer)
			.fillMaxSize()
			.imePadding(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Box(
			modifier = Modifier
				.testTag(AuthUiTags.KeyboardDismissArea)
				.clickable(
					interactionSource = remember { MutableInteractionSource() },
					indication = null,
					onClick = { focusManager.clearFocus(force = true) }
				)
				.padding(vertical = 32.dp)
		) {
			AppLogoView(contentDescription = null)
		}

		UsbIdTextField(
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					vertical = 8.dp,
					horizontal = 32.dp
				),
			labelText = when (state.identifierMode) {
				SignInIdentifierMode.UsbId -> usbIdLabelText
				SignInIdentifierMode.UsbEmail -> usbEmailLabelText
			},
			placeholderText = when (state.identifierMode) {
				SignInIdentifierMode.UsbId -> usbIdPlaceholderText
				SignInIdentifierMode.UsbEmail -> usbEmailPlaceholderText
			},
			identifierMode = state.identifierMode,
			identifierToggleCount = state.identifierToggleCount,
			toggleContentDescription = when (state.identifierMode) {
				SignInIdentifierMode.UsbId -> useUsbEmailContentDescription
				SignInIdentifierMode.UsbEmail -> useUsbIdContentDescription
			},
			showTogglePulse = state.identifierMode == SignInIdentifierMode.UsbId && state.usbId.isEmpty(),
			isWaiting = isWaiting,
			// Wrong credentials mark both fields; the message is not repeated here, a screen reader gets it.
			isError = rejection is SignIn.Rejection.InvalidCredentials,
			errorDescription = (rejection as? SignIn.Rejection.InvalidCredentials)?.message,
			usbId = state.usbId,
			onUsbIdChange = onUsbIdChange,
			onIdentifierModeToggle = onIdentifierModeToggle,
			keyboardActions = KeyboardActions(
				onNext = { passwordFocusRequester.requestFocus() }
			)
		)

		PasswordTextField(
			modifier = Modifier
				.focusRequester(passwordFocusRequester)
				.fillMaxWidth()
				.padding(horizontal = 32.dp),
			labelText = passwordLabelText,
			password = state.password,
			isPasswordVisible = state.isPasswordVisible,
			isWaiting = isWaiting,
			onPasswordChange = onPasswordChange,
			onPasswordVisibilityToggle = onPasswordVisibilityToggle,
			// Wrong credentials: the only message goes here, once, under the password.
			error = (rejection as? SignIn.Rejection.InvalidCredentials)?.message,
			errorModifier = Modifier
				.testTag(AuthUiTags.SignInRejectedMarker)
				.semantics { liveRegion = LiveRegionMode.Polite },
			imeAction = ImeAction.Done,
			keyboardActions = KeyboardActions(onDone = {
				if (isSignInEnabled) onSignInClick()
			})
		)

		// One toggleable row with a checkbox role: the screen reader announces the
		// consent text and state together instead of a nameless box plus a label.
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.toggleable(
					value = state.usageDataCollectionEnabled,
					role = Role.Checkbox,
					onValueChange = onUsageDataCollectionEnabledChange
				)
				.testTag(AuthUiTags.UsageDataConsentCheckbox)
				.padding(
					top = 12.dp,
					start = 24.dp,
					end = 32.dp
				),
			verticalAlignment = Alignment.CenterVertically
		) {
			Checkbox(
				// A display-only checkbox (the row is the toggle) loses the 48dp
				// minimum footprint, which was also the visual gap to the text.
				modifier = Modifier.minimumInteractiveComponentSize(),
				checked = state.usageDataCollectionEnabled,
				onCheckedChange = null
			)

			Text(
				modifier = Modifier.weight(1f),
				text = usageDataConsentText.toBoldMarkerAnnotatedText(),
				color = MaterialTheme.colorScheme.onBackground,
				style = MaterialTheme.typography.bodyMedium
			)
		}

		Button(
			modifier = Modifier
				.testTag(AuthUiTags.SignInButton)
				.fillMaxWidth()
				.padding(
					vertical = 16.dp,
					horizontal = 32.dp
				),
			enabled = isSignInEnabled,
			onClick = { onSignInClick() }
		) {
			Text(text = signInButtonText)
		}

		// Animated so the form does not jump when the wait starts or ends, and a live region so a
		// screen reader says why the button stopped responding.
		FixedMessage(
			text = stringResource(Res.string.sign_in_service_unavailable).takeIf { state.isServiceUnavailable },
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			tag = AuthUiTags.ServiceUnavailableMessage
		)

		// A disabled account or an unverified device: nothing the person typed is wrong, so the fields
		// stay as they are and the message stays here, in the color of an error, until they edit.
		FixedMessage(
			text = fixedRejection?.message,
			color = MaterialTheme.colorScheme.error,
			tag = AuthUiTags.SignInRejectedMarker
		)

		Column(
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text(
				text = policyIntroText,
				style = policyTextStyle
			)

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.Center,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					modifier = Modifier
						.testTag(AuthUiTags.TermsAndConditionsLink)
						.clickable(onClick = onTermsAndConditionsClick),
					text = termsAndConditionsText,
					style = policyLinkStyle
				)

				Text(
					text = " y ",
					style = policyTextStyle
				)

				Text(
					modifier = Modifier
						.testTag(AuthUiTags.PrivacyPolicyLink)
						.clickable(onClick = onPrivacyPolicyClick),
					text = privacyPolicyText,
					style = policyLinkStyle
				)
			}
		}
	}
}

// [text] is null while there is nothing to say. The message that leaves keeps its own text for as long as it
// takes to go, so the block does not collapse into an empty line first.
@Composable
private fun FixedMessage(
	text: String?,
	color: Color,
	tag: String
) {
	AnimatedContent(
		targetState = text,
		transitionSpec = { fadeIn() togetherWith fadeOut() },
		label = "FixedMessage"
	) { message ->
		if (message != null) {
			Text(
				modifier = Modifier
					.testTag(tag)
					.semantics { liveRegion = LiveRegionMode.Polite }
					.fillMaxWidth()
					.padding(
						start = TuIndiceSpacing.Wide,
						end = TuIndiceSpacing.Wide,
						bottom = TuIndiceSpacing.Screen
					),
				text = message,
				style = MaterialTheme.typography.bodySmall,
				color = color,
				textAlign = TextAlign.Center
			)
		}
	}
}

// A disabled account or an unverified device are shown under the button; wrong credentials are not.
private fun SignIn.Rejection?.takeIfFixedUnderTheButton(): SignIn.Rejection? {
	return when (this) {
		is SignIn.Rejection.AccountDisabled,
		is SignIn.Rejection.Untrusted -> this

		is SignIn.Rejection.InvalidCredentials,
		null -> null
	}
}
