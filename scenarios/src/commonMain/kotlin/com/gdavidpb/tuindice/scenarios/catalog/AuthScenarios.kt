package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.scenarios.shared.signInThroughUi

val authLoginCancel = scenario("auth-login-cancel", "auth", Start.Clean().toLaunchSpec()) {
	covers("auth.SignIn.ClickCancelSignIn")
	account(E2eAccounts.LoginCancel.id)
	signsIn()

	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	signInThroughUi(E2eAccounts.LoginCancel)
	waitVisible(AuthUiTags.SignInCancelButton, Within.Action)
	tap(AuthUiTags.SignInCancelButton)
	waitVisible(AuthUiTags.SignInButton, Within.Action)
	waitGone(AuthUiTags.SignInLoggingInContainer, Within.Assert)
	waitVisible(AuthUiTags.UsbIdTextField, Within.Assert)
}
