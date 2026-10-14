package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCareerItem
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryNavDependencies
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.testing.ControllablePensumProgressRepository
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleEvaluationsRepository
import com.gdavidpb.tuindice.summary.testing.HUB_THURSDAY
import com.gdavidpb.tuindice.summary.testing.createObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.testing.createSummaryViewModelWithHub
import com.gdavidpb.tuindice.summary.testing.hubAttempt
import com.gdavidpb.tuindice.summary.testing.hubDate
import com.gdavidpb.tuindice.summary.testing.hubEvaluation
import com.gdavidpb.tuindice.summary.testing.hubMeeting
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.testing.hubTerm
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

/** What the route does with the lines of the hub: each one asks the host for the screen that owns it. */
@OptIn(ExperimentalTestApi::class)
class SummaryRouteHubUiTest {
	@Test
	fun when_theClassOfTheWeekIsTapped_then_routeOpensTheSchedule() = runTuIndiceUiTest {
		var scheduleOpens = 0
		val viewModel = createHubViewModel()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToProfilePictureSettingsDialog = { _, _ -> },
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = {},
				navigation = SummaryNavDependencies(onOpenSchedule = { scheduleOpens++ }),
				viewModel = viewModel
			)
		}

		awaitHub(viewModel)

		onNodeWithTag(SummaryUiTags.WeekClassRow).performScrollTo().performClick()
		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.WeekSection)).performScrollTo().performClick()

		waitUntil(timeoutMillis = 2_000) { scheduleOpens == 2 }
	}

	@Test
	fun when_anEvaluationOfTheWeekAndAnUngradedPointAreTapped_then_routeAsksForTheirWeeks() = runTuIndiceUiTest {
		val weeks = mutableListOf<LocalDate>()
		val viewModel = createHubViewModel()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToProfilePictureSettingsDialog = { _, _ -> },
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = {},
				navigation = SummaryNavDependencies(onNavigateToEvaluationsWeek = { weekStart, _ -> weeks += weekStart }),
				viewModel = viewModel
			)
		}

		awaitHub(viewModel)

		onNodeWithTag(SummaryUiTags.weekEvaluationRow("friday")).performScrollTo().performClick()
		onNodeWithTag(SummaryUiTags.attentionItem("ungraded")).performScrollTo().performClick()

		waitUntil(timeoutMillis = 2_000) { weeks.size == 2 }

		assertEquals(listOf(hubDate(12), hubDate(5)), weeks)
	}

	@Test
	fun when_theTermAndCareerLinksAndAnEnrollmentErrorAreTapped_then_routeOpensRecordAndPensum() = runTuIndiceUiTest {
		var recordOpens = 0
		var pensumOpens = 0
		val viewModel = createHubViewModel()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToProfilePictureSettingsDialog = { _, _ -> },
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = {},
				navigation = SummaryNavDependencies(
					onNavigateToRecord = { recordOpens++ },
					onNavigateToPensum = { pensumOpens++ }
				),
				viewModel = viewModel
			)
		}

		awaitHub(viewModel)

		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.TermSection)).performScrollTo().performClick()
		onNodeWithTag(SummaryUiTags.attentionItem("enrollment-CI5311")).performScrollTo().performClick()
		onNodeWithTag(SummaryUiTags.CareerInviteCard).performScrollTo().performClick()
		onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.CareerSection)).performScrollTo().performClick()

		waitUntil(timeoutMillis = 2_000) { recordOpens == 2 && pensumOpens == 2 }
	}

	@Test
	fun when_theHubIsNotNavigatedByTheHost_then_tappingALineDoesNothing() = runTuIndiceUiTest {
		val viewModel = createHubViewModel()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToProfilePictureSettingsDialog = { _, _ -> },
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		awaitHub(viewModel)

		onNodeWithTag(SummaryUiTags.WeekClassRow).performScrollTo().performClick()

		assertNodeVisible(SummaryUiTags.ContentContainer)
	}

	@Test
	fun when_thePensumHasProgress_then_theCareerSaysItsPercentOnceInTheTextAndInTheDescription() = runTuIndiceUiTest {
		val viewModel = createHubViewModel(
			pensumProgress = PensumCreditsProgress(approvedCredits = 156, totalCredits = 240)
		)

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToProfilePictureSettingsDialog = { _, _ -> },
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			(viewModel.state.value as? Summary.State.Content)?.hub?.career is SummaryCareerItem.Progress
		}

		// Through the real mapper and the real resources: the percent sign is written once.
		onNodeWithText("65 %").performScrollTo().assertIsDisplayed()
		onAllNodesWithText("65 %%").assertCountEquals(0)
		onNodeWithTag(SummaryUiTags.CareerProgressBar)
			.assertContentDescriptionEquals("Avance del pensum: 156 de 240 créditos, 65 %")
	}

	// A Thursday at ten with a class in progress, an evaluation on Friday, one that went by without a
	// grade, and a subject the university refused part of the enrollment of.
	private fun createHubViewModel(pensumProgress: PensumCreditsProgress? = null): SummaryViewModel {
		val record = hubRecord(
			hubTerm(
				attempts = listOf(
					hubAttempt(
						code = "CI5437",
						schedule = listOf(hubMeeting(day = HUB_THURSDAY, start = 3, end = 4))
					),
					hubAttempt(code = "CI5311", enrollmentErrors = listOf("CHOQUE DE HORARIO"))
				)
			)
		)

		return createSummaryViewModelWithHub(
			hub = createObserveSummaryHubUseCase(
				record = ControllableVisibleAcademicRecordRepository(record),
				pensumProgress = ControllablePensumProgressRepository(pensumProgress),
				evaluations = ControllableVisibleEvaluationsRepository(
					initialEvaluations = listOf(
						hubEvaluation(id = "friday", code = "CI5437", date = hubDate(16)),
						hubEvaluation(id = "late", code = "CI5437", date = hubDate(8))
					)
				)
			)
		)
	}

	private fun ComposeUiTest.awaitHub(viewModel: SummaryViewModel) {
		waitUntil(timeoutMillis = 2_000) {
			(viewModel.state.value as? Summary.State.Content)?.hub?.week != null
		}
	}
}
