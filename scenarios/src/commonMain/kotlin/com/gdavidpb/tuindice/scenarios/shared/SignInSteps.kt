package com.gdavidpb.tuindice.scenarios.shared

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.assertEnabled
import com.gdavidpb.tuindice.scenariokit.dsl.enterSecureText
import com.gdavidpb.tuindice.scenariokit.dsl.enterText
import com.gdavidpb.tuindice.scenariokit.dsl.expectRequest
import com.gdavidpb.tuindice.scenariokit.dsl.group
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccount

/**
 * Signs in by typing the credential, up to the bootstrap request reaching the backend. Only the scenarios
 * that exercise sign-in itself use it (`CatalogStartTest` holds the list); every other scenario starts seeded.
 *
 * The identifier is [typed] and the field must read back [shown]; the defaults are the USB-ID mode, where the
 * field masks digits as `NN-NNNNN`. For the email mode, toggle the mode first and pass the text the field shows
 * unmasked (`signInThroughUi(account, typed = "1111111", shown = "1111111")`).
 */
fun StepBuilder.signInThroughUi(
	account: E2eAccount,
	typed: String = account.usbIdDigits,
	shown: String = account.usbIdFormatted
) {
	group(SIGN_IN_GROUP) {
		tap(AuthUiTags.UsbIdTextField)
		enterText(AuthUiTags.UsbIdTextField, typed, expect = shown)
		tap(AuthUiTags.PasswordTextField)
		enterSecureText(AuthUiTags.PasswordTextField, account.password)
		tap(AuthUiTags.KeyboardDismissArea)
		assertEnabled(AuthUiTags.SignInButton, true)
		tap(AuthUiTags.SignInButton)
		expectRequest(
			"POST",
			"/auth/v2/bootstrap",
			basicAuth = "${account.backendIdentifier}:${account.password}"
		)
	}
}
