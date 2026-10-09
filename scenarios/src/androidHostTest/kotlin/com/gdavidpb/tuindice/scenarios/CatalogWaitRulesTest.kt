package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.onPlatform
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.system
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.tapAtScreen
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.shared.ShareSheet
import com.gdavidpb.tuindice.scenarios.shared.Within
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The rules about waits for something to be gone (`CatalogWaitRules`): each on the catalog, where nothing breaks it,
 * and on a scenario made to break it, so a rule that stops detecting anything fails here.
 */
class CatalogWaitRulesTest {
	private val clean = LaunchSpec(emptyMap())

	// YE-1: a system element that goes was seen before.

	@Test
	fun everySystemElementTheCatalogWaitsToBeGoneWasSeenBeforeOnItsPlatform() {
		val offenders = CatalogWaitRules.systemElementsGoneWithoutHavingBeenSeen(E2eCatalog.all)

		assertTrue(offenders.isEmpty(), "waits for a system element to be gone that was never seen: $offenders")
	}

	@Test
	fun aSystemElementThatGoesWithoutHavingBeenSeenIsCaughtOnTheBranchOfItsPlatform() {
		// The shape `about-platform-edge-triggers` had: the sheet is closed and gone, never seen.
		val neverSeen = scenario("x-never", "x", clean) {
			onPlatform(Platform.Ios) {
				tapAtScreen(ShareSheet.DISMISS_X, ShareSheet.DISMISS_Y)
				waitGone(system("Sheet"), Within.Action)
			}
		}
		val seenOnTheOtherPlatform = scenario("x-other", "x", clean) {
			onPlatform(Platform.Android) { waitVisible(system("Sheet"), Within.Action) }
			onPlatform(Platform.Ios) { waitGone(system("Sheet"), Within.Action) }
		}
		val seenOutsideAnyBranch = scenario("x-all", "x", clean) {
			waitVisible(system("Sheet"), Within.Action)
			onPlatform(Platform.Ios) { waitGone(system("Sheet"), Within.Action) }
		}
		val seenInTheBranch = scenario("x-branch", "x", clean) {
			onPlatform(Platform.Ios) {
				waitVisible(system("Sheet"), Within.Action)
				waitGone(system("Sheet"), Within.Action)
			}
		}
		val tapped = scenario("x-tap", "x", clean) {
			tap(system("Button"))
			waitGone(system("Button"), Within.Action)
		}
		val ofTheApp = scenario("x-app", "x", clean) { waitGone("a_tag", Within.Action) }

		assertEquals(
			listOf("x-never (Ios): system:Sheet", "x-other (Ios): system:Sheet"),
			CatalogWaitRules.systemElementsGoneWithoutHavingBeenSeen(
				listOf(neverSeen, seenOnTheOtherPlatform, seenOutsideAnyBranch, seenInTheBranch, tapped, ofTheApp)
			)
		)
	}

	// YE-2: a snackbar that is not there is read right after its cause, in the window of the probe.

	@Test
	fun everySnackbarAbsenceTheCatalogAssertsIsReadInTheProbeWindowRightAfterItsCause() {
		val offenders = CatalogWaitRules.snackbarAbsencesNotReadRightAfterTheirCause(E2eCatalog.all)

		assertTrue(offenders.isEmpty(), "snackbar absences that a snackbar already shown would outlast: $offenders")
	}

	@Test
	fun aSnackbarAbsenceThatWaitsLongerOrComesAfterOtherStepsIsCaught() {
		// The shape of `auth-login-invalid` before: the absence last, after waits, with the time of an assertion.
		val atTheEnd = scenario("x-end", "x", clean) {
			waitVisible("rejection", Within.Action)
			waitVisible("field", Within.Assert)
			waitGone(BaseUiTags.SnackbarContainer, Within.Assert)
		}
		val probeAfterATap = scenario("x-tap", "x", clean) {
			tap("button")
			waitGone(BaseUiTags.SnackbarContainer, Within.Probe)
		}
		val rightAfterItsCause = scenario("x-ok", "x", clean) {
			waitVisible("rejection", Within.Action)
			waitGone(BaseUiTags.SnackbarContainer, Within.Probe)
		}
		val afterHavingSeenOne = scenario("x-seen", "x", clean) {
			waitVisible(BaseUiTags.SnackbarContainer, Within.Wait)
			tap(BaseUiTags.SnackbarActionButton)
			waitGone(BaseUiTags.SnackbarContainer, Within.Action)
		}

		assertEquals(
			listOf("x-end: tag:${BaseUiTags.SnackbarContainer}", "x-tap: tag:${BaseUiTags.SnackbarContainer}"),
			CatalogWaitRules.snackbarAbsencesNotReadRightAfterTheirCause(
				listOf(atTheEnd, probeAfterATap, rightAfterItsCause, afterHavingSeenOne)
			)
		)
	}
}
