package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCareerItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.testing.WithLargeLetter
import com.gdavidpb.tuindice.summary.testing.assertTextIsWhole
import com.gdavidpb.tuindice.summary.testing.summaryCareerProgressItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryCareerViewUiTest {
	@Test
	fun when_thereIsProgress_then_itShowsCreditsShareAndABarThatSaysIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryCareerView(career = summaryCareerProgressItem(), actions = SummaryHubActions())
		}

		assertNodeVisible(SummaryUiTags.CareerCard)
		onNodeWithText("Carrera").assertIsDisplayed()
		onNodeWithText("Pensum").assertIsDisplayed()
		onNodeWithText("156 de 240 créditos").assertIsDisplayed()
		onNodeWithText("65 %").assertIsDisplayed()
		// The card is one button, so the bar is read from the tree that keeps each node apart.
		onNodeWithTag(SummaryUiTags.CareerProgressBar, useUnmergedTree = true)
			.assertContentDescriptionEquals("Avance del pensum: 156 de 240 créditos, 65 %")
		assertEquals(
			0.65f,
			onNodeWithTag(SummaryUiTags.CareerProgressBar, useUnmergedTree = true)
				.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current
		)
		onNodeWithTag(SummaryUiTags.CareerInviteCard).assertDoesNotExist()
	}

	@Test
	fun when_thereIsProgress_then_itsBarIsTheSharedHeight() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryCareerView(career = summaryCareerProgressItem(), actions = SummaryHubActions())
		}

		onNodeWithTag(SummaryUiTags.CareerProgressBar, useUnmergedTree = true)
			.assertHeightIsEqualTo(TuIndiceComponentSizes.ProgressBarHeight)
	}

	@Test
	fun when_theProgressCardIsTapped_then_itAsksForPensum() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryCareerView(
				career = summaryCareerProgressItem(),
				actions = SummaryHubActions(onTargetClick = { target -> targets += target })
			)
		}

		onNodeWithTag(SummaryUiTags.CareerCard).performClick()

		assertEquals(listOf<SummaryHubTarget>(SummaryHubTarget.Pensum), targets)
	}

	@Test
	fun when_thereIsNoPensumInTheCache_then_theCardInvitesToOpenIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryCareerView(career = SummaryCareerItem.Invite, actions = SummaryHubActions())
		}

		assertNodeVisible(SummaryUiTags.CareerInviteCard)
		onNodeWithText("Mira cuánto llevas").assertIsDisplayed()
		onNodeWithText("Abre tu pensum para calcular tu avance.").assertIsDisplayed()
		onNodeWithTag(SummaryUiTags.CareerCard).assertDoesNotExist()
		onNodeWithTag(SummaryUiTags.CareerProgressBar).assertDoesNotExist()
	}

	@Test
	fun when_theInvitationAndTheLinkAreTapped_then_bothAskForThePensum() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryCareerView(
				career = SummaryCareerItem.Invite,
				actions = SummaryHubActions(onTargetClick = { target -> targets += target })
			)
		}

		onNodeWithTag(SummaryUiTags.CareerInviteCard).assertHasClickAction()
		onNodeWithTag(SummaryUiTags.CareerInviteCard).performClick()
		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.CareerSection)).performClick()

		assertEquals(listOf<SummaryHubTarget>(SummaryHubTarget.Pensum, SummaryHubTarget.Pensum), targets)
	}

	@Test
	fun when_theLinkIsTappedWithProgress_then_itAsksForThePensum() = runTuIndiceUiTest {
		val targets = mutableListOf<SummaryHubTarget>()

		setTuIndiceTestContent {
			SummaryCareerView(
				career = summaryCareerProgressItem(),
				actions = SummaryHubActions(onTargetClick = { target -> targets += target })
			)
		}

		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.CareerSection)).performClick()

		assertEquals(listOf<SummaryHubTarget>(SummaryHubTarget.Pensum), targets)
	}

	@Test
	fun when_theLetterIsLargeOnANarrowScreen_then_nothingIsCutInEitherCard() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				SummaryCareerView(career = summaryCareerProgressItem(), actions = SummaryHubActions())
			}
		}

		assertTextIsWhole("156 de 240 créditos")
		assertTextIsWhole("65 %", singleLine = true)
	}

	@Test
	fun when_theLetterIsLargeOnANarrowScreen_then_theInvitationIsWhole() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WithLargeLetter {
				SummaryCareerView(career = SummaryCareerItem.Invite, actions = SummaryHubActions())
			}
		}

		assertTextIsWhole("Mira cuánto llevas")
		assertTextIsWhole("Abre tu pensum para calcular tu avance.")
	}
}
