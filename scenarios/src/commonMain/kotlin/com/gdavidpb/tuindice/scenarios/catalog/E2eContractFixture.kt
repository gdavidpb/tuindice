package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.scenariokit.contract.DriverContractFixture
import com.gdavidpb.tuindice.scenarios.fixture.Start

/**
 * The sign-in screen of a clean launch: a visible container, a disabled button and
 * the USB-ID field, which masks digits as `NN-NNNNN`.
 */
object E2eContractFixture {
	const val ABSENT_TAG = "driver_contract_never_present"

	val fixture = DriverContractFixture(
		start = Start.Clean().toLaunchSpec(),
		presentTag = AuthUiTags.SignInIdleContainer,
		absentTag = ABSENT_TAG,
		disabledTag = AuthUiTags.SignInButton,
		textFieldTag = AuthUiTags.UsbIdTextField,
		textSample = "1234567",
		expectedText = "12-34567"
	)
}
