package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.TextEntryMode
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.SIGN_IN_GROUP
import com.gdavidpb.tuindice.scenarios.shared.openCurrentEnrollmentProof
import com.gdavidpb.tuindice.scenarios.shared.signInThroughUi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** The step sequences the shared helpers expand to, which every translated scenario relies on. */
class SharedStepsTest {
	private val start = Start.Clean().toLaunchSpec()

	@Test
	fun signInThroughUiTypesTheCredentialAndWaitsForTheBootstrapRequest() {
		val group = groupOf { signInThroughUi(E2eAccounts.Canonical) }
		val steps = group.steps
		val next = steps.cursor()
		val tapUsbId = next()
		val typeUsbId = next()
		val tapPassword = next()
		val typePassword = next()
		val tapDismiss = next()
		val enabled = next()
		val tapSignIn = next()
		val request = next()

		assertEquals(SIGN_IN_GROUP, group.name)
		assertEquals(SIGN_IN_STEPS, steps.size)
		assertEquals(Query.Tag(AuthUiTags.UsbIdTextField), assertIs<Step.Tap>(tapUsbId).q)
		assertIs<Step.EnterText>(typeUsbId).also {
			assertEquals(Query.Tag(AuthUiTags.UsbIdTextField), it.q)
			assertEquals("1111111", it.text)
			assertEquals("11-11111", it.expect)
			assertEquals(false, it.secure)
			assertEquals(TextEntryMode.Keys, it.mode)
		}
		assertEquals(Query.Tag(AuthUiTags.PasswordTextField), assertIs<Step.Tap>(tapPassword).q)
		assertIs<Step.EnterText>(typePassword).also {
			assertEquals(Query.Tag(AuthUiTags.PasswordTextField), it.q)
			assertEquals("123456", it.text)
			assertEquals(true, it.secure)
		}
		assertEquals(Query.Tag(AuthUiTags.KeyboardDismissArea), assertIs<Step.Tap>(tapDismiss).q)
		assertIs<Step.AssertEnabled>(enabled).also {
			assertEquals(Query.Tag(AuthUiTags.SignInButton), it.q)
			assertEquals(true, it.enabled)
		}
		assertEquals(Query.Tag(AuthUiTags.SignInButton), assertIs<Step.Tap>(tapSignIn).q)
		assertIs<Step.ExpectRequest>(request).also {
			assertEquals("POST", it.method)
			assertEquals("/auth/v2/bootstrap", it.path)
			assertEquals("11-11111:123456", it.basicAuth)
		}
	}

	@Test
	fun signInThroughUiExpectsTheBackendIdentifierOfAnEmailAccount() {
		val (typeUsbId, request) = groupOf { signInThroughUi(E2eAccounts.CanonicalEmail) }.steps.typeAndRequest()

		assertIs<Step.EnterText>(typeUsbId).also {
			assertEquals("mail@usb.ve", it.text)
			assertEquals("mail@usb.ve", it.expect)
		}
		assertEquals("mail:123456", assertIs<Step.ExpectRequest>(request).basicAuth)
	}

	@Test
	fun signInThroughUiCanTypeDigitsInTheEmailMode() {
		val (typeUsbId, request) = groupOf {
			signInThroughUi(E2eAccounts.Canonical, typed = "1111111", shown = "1111111")
		}.steps.typeAndRequest()

		assertIs<Step.EnterText>(typeUsbId).also {
			assertEquals("1111111", it.text)
			assertEquals("1111111", it.expect)
		}
		assertEquals("11-11111:123456", assertIs<Step.ExpectRequest>(request).basicAuth)
	}

	@Test
	fun signInThroughUiOfARejectedAccountStillExpectsItsCredentialAtTheBackend() {
		val (_, request) = groupOf { signInThroughUi(E2eAccounts.Invalid) }.steps.typeAndRequest()

		assertEquals("00-00000:bad-password", assertIs<Step.ExpectRequest>(request).basicAuth)
	}

	@Test
	fun openCurrentEnrollmentProofSelectsTheCurrentTermAndScrollsToTheButton() {
		val steps = groupOf { openCurrentEnrollmentProof() }.steps
		val next = steps.cursor()
		val tapChip = next()
		val waitButton = next()
		val scroll = next()
		val tapButton = next()
		val button = Query.Tag(RecordUiTags.EnrollmentProofButton)

		assertEquals(OPEN_PROOF_STEPS, steps.size)
		assertEquals(Query.Tag(RecordUiTags.termChip(E2eFixtures.CurrentTerm.value)), assertIs<Step.Tap>(tapChip).q)
		assertEquals(button, assertIs<Step.WaitVisible>(waitButton).q)
		assertIs<Step.ScrollUntilVisible>(scroll).also {
			assertEquals(button, it.q)
			assertEquals(Scroll.ContentDown, it.direction)
		}
		assertEquals(button, assertIs<Step.Tap>(tapButton).q)
	}

	/** The sign-in group's second step (typing the identifier) and last one (the bootstrap request). */
	private fun List<Step>.typeAndRequest(): Pair<Step, Step> = this[1] to last()

	private fun List<Step>.cursor(): () -> Step = iterator().let { steps -> { steps.next() } }

	private fun groupOf(block: StepBuilder.() -> Unit): Step.Group =
		assertIs<Step.Group>(scenario("shared-demo", "shared", start, block).steps.single())

	private companion object {
		const val SIGN_IN_STEPS = 8
		const val OPEN_PROOF_STEPS = 4
	}
}
