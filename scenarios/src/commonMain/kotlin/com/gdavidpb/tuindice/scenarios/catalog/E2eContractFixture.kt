package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.scenariokit.contract.DriverContractFixture
import com.gdavidpb.tuindice.scenarios.fixture.Start

/**
 * The sign-in screen of a clean launch: a visible container, a disabled button, the USB-ID field, which masks digits
 * as `NN-NNNNN`, and the password field, which is secure.
 */
object E2eContractFixture {
	const val ABSENT_TAG = "driver_contract_never_present"

	val fixture = DriverContractFixture(
		start = Start.Clean().toLaunchSpec(),
		presentTag = AuthUiTags.SignInIdleContainer,
		absentTag = ABSENT_TAG,
		disabledTag = AuthUiTags.SignInButton,
		textFieldTag = AuthUiTags.UsbIdTextField,
		secureFieldTag = AuthUiTags.PasswordTextField,
		// 25 characters: the iOS driver empties a field by tapping at 75 % of its width, and 25 bullets still end before it.
		secureSample = "abcdefghijklmnopqrstuvwxy",
		// Not the placeholder of the field ("12-34567"), which a driver that read it instead of the text would match.
		textSample = "7654321",
		expectedText = "76-54321"
	)
}
