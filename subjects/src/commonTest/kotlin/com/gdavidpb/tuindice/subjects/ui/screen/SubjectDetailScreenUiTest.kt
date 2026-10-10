package com.gdavidpb.tuindice.subjects.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.testing.globalSubjectSegmentItem
import com.gdavidpb.tuindice.subjects.testing.subjectDetailItem
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SubjectDetailScreenUiTest {
	@Test
	fun when_stateIsIdle_then_rendersNoStateView() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TestSubjectDetailScreen(state = SubjectDetail.State.Idle)
		}

		waitForIdle()

		assertNodeHidden(SubjectsUiTags.Loading)
		assertNodeHidden(SubjectsUiTags.Content)
		assertNodeHidden(SubjectsUiTags.Unavailable)
		assertNodeHidden(SubjectsUiTags.Failed)
	}

	@Test
	fun when_stateIsLoading_then_displaysLoadingTitleAndMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TestSubjectDetailScreen(state = SubjectDetail.State.Loading)
		}

		assertNodeVisible(SubjectsUiTags.Loading)
		onNodeWithTag(SubjectsUiTags.LoadingTitle).assertTextEquals(LOADING_TITLE)
		onNodeWithTag(SubjectsUiTags.LoadingMessage).assertTextEquals(LOADING_MESSAGE)
		assertNodeHidden(SubjectsUiTags.Content)
	}

	@Test
	fun when_stateIsContent_then_displaysDetailAndForwardsTabSelection() = runTuIndiceUiTest {
		val selections = mutableListOf<SubjectSegmentTab>()

		setTuIndiceTestContent {
			TestSubjectDetailScreen(
				state = SubjectDetail.State.Content(
					detail = subjectDetailItem().copy(
						hasSegmentTabs = true,
						globalSegment = globalSubjectSegmentItem()
					)
				),
				onTabSelected = { tab -> selections += tab }
			)
		}

		assertNodeVisible(SubjectsUiTags.Content)
		assertNodeHidden(SubjectsUiTags.Loading)
		onNodeWithText("Calculo I").assertIsDisplayed()
		onNodeWithTag(SubjectsUiTags.CareerTab).assertTextEquals(CAREER_TAB)
		onNodeWithTag(SubjectsUiTags.GlobalTab)
			.assertTextEquals(GLOBAL_TAB)
			.performClick()

		assertEquals(listOf(SubjectSegmentTab.GLOBAL), selections)
	}

	@Test
	fun when_stateIsUnavailable_then_closeActionInvokesDismissRequest() = runTuIndiceUiTest {
		var dismissRequests = 0
		var retryClicks = 0

		setTuIndiceTestContent {
			TestSubjectDetailScreen(
				state = SubjectDetail.State.Unavailable(subjectCode = "MAT404"),
				onRetryClick = { retryClicks++ },
				onDismissRequest = { dismissRequests++ }
			)
		}

		assertNodeVisible(SubjectsUiTags.Unavailable)
		assertNodeVisible(BaseUiTags.EmptyStateAnimation)
		assertNodeHidden(SubjectsUiTags.Retry)
		onNodeWithText(UNAVAILABLE_TITLE).assertIsDisplayed()
		onNodeWithText(UNAVAILABLE_BODY).assertIsDisplayed()
		onNodeWithText(CLOSE_TEXT).performClick()

		assertEquals(1, dismissRequests)
		assertEquals(0, retryClicks)
	}

	@Test
	fun when_stateIsFailed_then_retryActionInvokesRetryCallback() = runTuIndiceUiTest {
		var dismissRequests = 0
		var retryClicks = 0

		setTuIndiceTestContent {
			TestSubjectDetailScreen(
				state = SubjectDetail.State.Failed(subjectCode = "MAT101"),
				onRetryClick = { retryClicks++ },
				onDismissRequest = { dismissRequests++ }
			)
		}

		assertNodeVisible(SubjectsUiTags.Failed)
		assertNodeVisible(BaseUiTags.ErrorStateAnimation)
		onNodeWithText(FAILED_TITLE).assertIsDisplayed()
		onNodeWithText(FAILED_MESSAGE).assertIsDisplayed()
		onNodeWithTag(SubjectsUiTags.Retry)
			.assertTextEquals(RETRY_TEXT)
			.performClick()

		assertEquals(1, retryClicks)
		assertEquals(0, dismissRequests)
	}
}

@Composable
private fun TestSubjectDetailScreen(
	state: SubjectDetail.State,
	onRetryClick: () -> Unit = {},
	onTabSelected: (SubjectSegmentTab) -> Unit = {},
	onDismissRequest: () -> Unit = {}
) {
	SubjectDetailScreen(
		state = state,
		careerTabText = CAREER_TAB,
		globalTabText = GLOBAL_TAB,
		loadingTitle = LOADING_TITLE,
		loadingMessage = LOADING_MESSAGE,
		unavailableTitle = UNAVAILABLE_TITLE,
		unavailableBody = UNAVAILABLE_BODY,
		failedTitle = FAILED_TITLE,
		failedMessage = FAILED_MESSAGE,
		retryText = RETRY_TEXT,
		closeText = CLOSE_TEXT,
		onRetryClick = onRetryClick,
		onTabSelected = onTabSelected,
		onDismissRequest = onDismissRequest
	)
}

private const val CAREER_TAB = "Tu carrera"
private const val GLOBAL_TAB = "General"
private const val LOADING_TITLE = "Calculando estadísticas"
private const val LOADING_MESSAGE = "Estamos cruzando notas de esta materia..."
private const val UNAVAILABLE_TITLE = "Sin datos suficientes"
private const val UNAVAILABLE_BODY = "Cuando haya más historial podrás ver estadísticas."
private const val FAILED_TITLE = "No pudimos calcular estadísticas"
private const val FAILED_MESSAGE = "Intenta de nuevo para consultar los datos de MAT101."
private const val RETRY_TEXT = "Reintentar"
private const val CLOSE_TEXT = "Cerrar"
