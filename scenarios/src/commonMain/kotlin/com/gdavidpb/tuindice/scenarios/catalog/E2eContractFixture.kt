package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.scenariokit.contract.DriverContractFixture
import com.gdavidpb.tuindice.scenarios.fixture.Start

/** The sign-in screen of a clean launch: a visible container, a disabled button and a text field. */
object E2eContractFixture {
	const val ABSENT_TAG = "driver_contract_never_present"

	val fixture = DriverContractFixture(
		start = Start.Clean().toLaunchSpec(),
		presentTag = AuthUiTags.SignInIdleContainer,
		absentTag = ABSENT_TAG,
		disabledTag = AuthUiTags.SignInButton,
		textFieldTag = AuthUiTags.UsbIdTextField
	)
}
