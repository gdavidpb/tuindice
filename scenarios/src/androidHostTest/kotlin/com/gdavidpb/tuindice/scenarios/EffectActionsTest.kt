package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The actions whose effect is a request the app sends to the backend. A scenario that covers one of them waits for that
 * request, answered with the status its mock declares, so the scenario fails when the action does nothing: a scenario
 * that only checks the screen after the tap passes whatever the action sends.
 */
class EffectActionsTest {
	private data class Effect(val action: String, val method: String, val path: String, val status: Int)

	private val effects = listOf(
		Effect("assistant.AssistantChat.RateMessage", "POST", "/assistant/v1/feedback", status = 204),
		Effect("assistant.AssistantChat.ReportMessage", "POST", "/assistant/v1/reports", status = 204),
		Effect(
			action = "summary.TermReport.Rate",
			method = "POST",
			path = "/assistant/v1/insights/01c705af6984b336fc2aed20576ec543:1/feedback",
			status = 204
		),
		Effect("about.Settings.SetAssistantConsent", "PUT", "/assistant/v1/consent", status = 200)
	)

	@Test
	fun everyScenarioCoveringAnEffectActionWaitsForItsRequest() {
		val offenders = effects.flatMap { effect ->
			val covering = E2eCatalog.all.filter { effect.action in it.covers }

			assertTrue(covering.isNotEmpty(), "no scenario covers ${effect.action}")

			covering.filterNot { scenario ->
				scenario.steps.flattened().any { step ->
					step is Step.ExpectRequest &&
						step.method == effect.method &&
						step.path == effect.path &&
						step.status == effect.status
				}
			}.map { scenario ->
				"${scenario.id} covers ${effect.action} without waiting for " +
					"${effect.method} ${effect.path} answered ${effect.status}"
			}
		}

		assertTrue(offenders.isEmpty(), "covered actions whose effect no step checks: $offenders")
	}
}
