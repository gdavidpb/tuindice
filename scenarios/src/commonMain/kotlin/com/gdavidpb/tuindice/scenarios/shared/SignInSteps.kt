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
 * that exercise sign-in itself use it; every other scenario starts seeded.
 */
fun StepBuilder.signInThroughUi(account: E2eAccount) {
	group("signInThroughUi") {
		tap(AuthUiTags.UsbIdTextField)
		enterText(AuthUiTags.UsbIdTextField, account.usbIdDigits, expect = account.usbIdFormatted)
		tap(AuthUiTags.PasswordTextField)
		enterSecureText(AuthUiTags.PasswordTextField, account.password)
		tap(AuthUiTags.KeyboardDismissArea)
		assertEnabled(AuthUiTags.SignInButton, true)
		tap(AuthUiTags.SignInButton)
		expectRequest("POST", "/auth/v2/bootstrap", basicAuth = "${account.usbIdFormatted}:${account.password}")
	}
}
