package com.gdavidpb.tuindice.scenariorunner

import com.gdavidpb.tuindice.scenariokit.codec.CatalogCodec
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariorunner.driver.FailureArtifacts
import com.gdavidpb.tuindice.scenariorunner.driver.UiAutomatorScenarioDriver
import org.junit.Assert.assertTrue
import java.io.File

/** What the driver probes share: the login screen of the contract fixture, its queries and a driver per probe. */
internal class ProbeApp {
	val fixture = CatalogCodec.decode(CatalogAsset.json).contractFixture
	val screen = Query.Tag(fixture.presentTag)
	val usbId = Query.Tag(checkNotNull(fixture.textFieldTag))
	val password = Query.Tag("auth_password_text_field")
	val label = Query.Tag("auth_terms_and_conditions_link")
	val never = Query.Tag(fixture.absentTag)

	/** A driver whose scenario is `probe-<name>`, with the app launched and its login screen showing. */
	fun begin(name: String): UiAutomatorScenarioDriver {
		val driver = UiAutomatorScenarioDriver()
		driver.beginScenario("probe-$name")
		assertTrue(driver.launch(fixture.start))
		assertTrue(driver.waitVisible(screen, LAUNCH_WAIT_MS))

		return driver
	}

	/** The `driver.log` of probe [name], read from disk. */
	fun log(name: String): String = File(FailureArtifacts.directory("probe-$name"), "driver.log").readText()

	companion object {
		private const val LAUNCH_WAIT_MS = 60_000L
		const val PASSWORD_SAMPLE = "abcdefghijklmnopqrstuvwxyz0123"
		const val SHORT_SAMPLE = "abc"
		const val SAMPLE_LENGTH = 30
		const val SAMPLE_EVENTS = 60
	}
}
