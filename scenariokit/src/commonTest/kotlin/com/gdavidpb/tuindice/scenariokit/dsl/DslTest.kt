package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Step
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.seconds

class DslTest {
	private val start = LaunchSpec(mapOf("A" to "1"))

	@Test
	fun scenario_collectsStepsAndMetadata() {
		val built = scenario("auth-demo", "auth", start) {
			covers("SignIn.Submit")
			tags("smoke")
			platforms(Platform.Ios)
			account("canonical")
			signsIn()
			timeout(90)
			quarantine("flaky", "2026-12-31")
			tap("button")
			enterText("field", "12-34567", expect = "12-34567", replace = true)
			expectRequest("POST", "/auth/v2/bootstrap", basicAuth = "11-11111:123456")
		}

		assertEquals("auth-demo", built.id)
		assertEquals(start, built.start)
		assertEquals(listOf("SignIn.Submit"), built.covers)
		assertEquals(listOf("smoke"), built.tags)
		assertEquals(listOf(Platform.Ios), built.platforms)
		assertEquals("canonical", built.account)
		assertEquals(true, built.signsIn)
		assertEquals(90, built.timeoutSeconds)
		assertEquals("2026-12-31", built.quarantine?.until)
		assertEquals(listOf("Tap", "EnterText", "ExpectRequest"), built.steps.map { it::class.simpleName })
	}

	@Test
	fun durations_becomeMilliseconds() {
		val built = scenario("a-b", "a", start) { waitVisible("x", 2.seconds) }

		assertEquals(2_000L, (built.steps.single() as Step.WaitVisible).timeoutMs)
	}

	@Test
	fun textHelpers_markSecureFieldsAndReplacement() {
		val built = scenario("a-b", "a", start) {
			enterSecureText("pw", "123456")
			enterText("f", "v", replace = true)
		}

		val (secure, replacing) = built.steps.map { it as Step.EnterText }

		assertEquals(true, secure.secure)
		assertEquals(false, secure.replace)
		assertEquals(false, replacing.secure)
		assertEquals(true, replacing.replace)
	}

	@Test
	fun submitAndMockState_buildTheirOwnSteps() {
		val built = scenario("a-b", "a", start) {
			submitTextEntry()
			mockState("login-token-lifecycle", "Reissued")
		}

		assertEquals(
			listOf("SubmitTextEntry", "SetMockState"),
			built.steps.map { it::class.simpleName }
		)
		assertEquals(Step.SetMockState("login-token-lifecycle", "Reissued", built.steps.last().site), built.steps.last())
		assertEquals("login-token-lifecycle = Reissued", built.steps.last().target)
	}

	@Test
	fun blocks_nestAndKeepTheirSteps() {
		val built = scenario("a-b", "a", start) {
			ifVisible("banner") { tap("dismiss") }
			onPlatform(Platform.Android) { back() }
			group("sign in") {
				tap("a")
				tap("b")
			}
		}

		val ifVisible = built.steps[0]
		val onPlatform = built.steps[1]
		val group = built.steps[2]
		assertIs<Step.IfVisible>(ifVisible)
		assertEquals(Query.Tag("banner"), ifVisible.q)
		assertEquals(1_500L, ifVisible.withinMs)
		assertEquals(1, ifVisible.steps.size)
		assertEquals(Platform.Android, (onPlatform as Step.OnPlatform).platform)
		assertEquals(2, (group as Step.Group).steps.size)
	}

	@Test
	fun conditionalBlocks_acceptAnyQueryKind() {
		val built = scenario("a-b", "a", start) {
			ifVisible(Query.System("Cancel"), within = 2.seconds) { tap(Query.System("Cancel")) }
			ifVisible(Query.Text("Cargando")) { back() }
		}

		val (system, text) = built.steps
		assertIs<Step.IfVisible>(system)
		assertIs<Step.IfVisible>(text)
		assertEquals(Query.System("Cancel"), system.q)
		assertEquals(2_000L, system.withinMs)
		assertEquals(Query.Text("Cargando"), text.q)
		assertEquals(1_500L, text.withinMs)
	}

	@Test
	fun swipeHelpers_pointTheFingerTheRightWay() {
		val built = scenario("a-b", "a", start) {
			swipeScreen(SwipeDirection.Up)
			swipeScreen(SwipeDirection.Right)
			swipeFrom("card", SwipeDirection.Left)
		}

		val (up, right, left) = built.steps.map { it as Step.Swipe }

		assertEquals(true, up.dy < 0.0 && up.dx == 0.0)
		assertEquals(true, right.dx > 0.0 && right.dy == 0.0)
		assertEquals(Query.Tag("card"), left.from)
		assertEquals(true, left.dx < 0.0)
	}

	@Test
	fun queries_buildTheThreeKinds() {
		assertEquals(Query.Tag("a"), tag("a"))
		assertEquals(Query.Text("b", true), text("b", contains = true))
		assertEquals(Query.System("c"), system("c"))
	}
}
