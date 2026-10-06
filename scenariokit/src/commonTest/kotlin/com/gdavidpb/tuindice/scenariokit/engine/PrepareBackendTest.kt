package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.MockState
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Step
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrepareBackendTest {
	private val seeded = LaunchSpec(
		arguments = mapOf("TUINDICE_E2E_MAIN_SECTION" to "summary"),
		mockStates = listOf(MockState("login-token-lifecycle", "TokensIssued"))
	)

	@Test
	fun resetPaths_listTheFourResetsInOrder() {
		assertEquals(
			listOf(
				"POST /__admin/scenarios/reset",
				"DELETE /__admin/requests",
				"POST /evaluations/v3/reset",
				"POST /record/v5/reset"
			),
			BackendEngine.resetPaths.map { "${it.method} ${it.path}" }
		)
	}

	@Test
	fun run_resetsTheBackendAndSetsMockStatesBeforeLaunching() {
		val fake = FakeDriver()
		fake.backend.states["login-token-lifecycle"] = "Started"

		assertPassed(fake.run(scenarioOf(start = seeded)))

		assertEquals(
			listOf(
				"POST /__admin/scenarios/reset",
				"DELETE /__admin/requests",
				"POST /evaluations/v3/reset",
				"POST /record/v5/reset",
				"PUT /__admin/scenarios/login-token-lifecycle/state"
			),
			fake.backend.calls.take(5)
		)
		assertEquals("TokensIssued", fake.backend.states["login-token-lifecycle"])
		assertEquals("Bearer e2e-harness-reset", fake.backend.authorizations["POST /evaluations/v3/reset"])
		assertEquals("Bearer e2e-harness-reset", fake.backend.authorizations["POST /record/v5/reset"])
		val launchedAt = fake.calls.indexOf("launch")
		val lastStateCall = fake.calls.indexOfLast { it.startsWith("http(") && "state" in it }
		assertTrue(launchedAt > lastStateCall)
		assertEquals(listOf(seeded), fake.launches)
	}

	@Test
	fun run_whenAResetIsRejected_failsAsBackendUnavailableWithoutLaunching() {
		val fake = FakeDriver()
		fake.backend.failingPath = "/record/v5/reset"

		val outcome = fake.run(scenarioOf(Step.Tap(Query.Tag("any"))))

		val failure = assertFailed(outcome, FailureKind.BACKEND_UNAVAILABLE, stepIndex = -1)
		assertContains(failure.message, "POST /record/v5/reset answered 503")
		assertTrue(fake.launches.isEmpty())
		assertTrue(outcome.steps.isEmpty())
		assertTrue(fake.captured.isEmpty())
	}

	@Test
	fun run_whenAMockStateCannotBeSet_failsAsBackendUnavailable() {
		val fake = FakeDriver()
		fake.backend.failingPath = "/__admin/scenarios/login-token-lifecycle/state"

		val failure = assertFailed(fake.run(scenarioOf(start = seeded)), FailureKind.BACKEND_UNAVAILABLE)

		assertContains(failure.message, "PUT /__admin/scenarios/login-token-lifecycle/state")
		assertTrue(fake.launches.isEmpty())
	}

	@Test
	fun run_whenWireMockIsDown_failsAsBackendUnavailable() {
		val fake = FakeDriver().apply { backend.down = true }

		val failure = assertFailed(fake.run(scenarioOf()), FailureKind.BACKEND_UNAVAILABLE)

		assertContains(failure.message, "answered -1")
	}

	@Test
	fun run_whenTheAppDoesNotLaunch_failsAsAppNotRunningBeforeTheFirstStep() {
		val fake = FakeDriver().apply { launchResults = ArrayDeque(listOf(false)) }

		val outcome = fake.run(scenarioOf(Step.Back()))

		assertFailed(outcome, FailureKind.APP_NOT_RUNNING, stepIndex = -1)
		assertEquals(listOf("test-scenario" to -1), fake.captured)
		assertTrue(outcome.steps.isEmpty())
	}

	@Test
	fun launch_neverWipesState() {
		val fake = FakeDriver()

		assertPassed(fake.run(scenarioOf(Step.Relaunch(emptyMap()))))

		assertTrue("terminate" !in fake.calls)
		assertEquals(2, fake.launches.size)
	}
}
