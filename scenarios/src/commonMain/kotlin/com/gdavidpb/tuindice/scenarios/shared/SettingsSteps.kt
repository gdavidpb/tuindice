package com.gdavidpb.tuindice.scenarios.shared

import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.about.ui.SettingsUiTags
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.group
import com.gdavidpb.tuindice.scenariokit.dsl.scrollUntilVisible
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.ui.MaincoreUiTags

/** From Summary's top bar, the only one with the student's avatar: taps it and waits for the Settings screen. */
fun StepBuilder.openSettings() {
	group("openSettings") {
		tap(MaincoreUiTags.TuIndiceTopBarSettingsButton)
		waitVisible(SettingsUiTags.ContentContainer, Within.Wait)
	}
}

/** From Settings: the pushed screen's back button, which returns to Summary. */
fun StepBuilder.closeSettings() {
	group("closeSettings") {
		tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	}
}

/** From Settings: scrolls to «Acerca de TuIndice», opens it and waits for its content. */
fun StepBuilder.openAboutFromSettings() {
	group("openAboutFromSettings") {
		scrollUntilVisible(SettingsUiTags.OpenAbout, Scroll.ContentDown, Within.Action)
		tap(SettingsUiTags.OpenAbout)
		waitVisible(AboutUiTags.ContentContainer, Within.Wait)
	}
}

/**
 * From Summary: Settings, then «Cerrar sesión» at the end of the list, and the sign-out sheet, which opens
 * with its message.
 */
fun StepBuilder.openSignOutFromSettings() {
	group("openSignOutFromSettings") {
		tap(MaincoreUiTags.TuIndiceTopBarSettingsButton)
		waitVisible(SettingsUiTags.ContentContainer, Within.Wait)
		scrollUntilVisible(SettingsUiTags.SignOut, Scroll.ContentDown, Within.Action)
		tap(SettingsUiTags.SignOut)
		waitVisible(AuthUiTags.SignOutMessageText, Within.Action)
	}
}
