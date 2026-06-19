package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TopAppBarActionsViewUiTest {
	@Test
	fun when_configHasActions_then_rendersButtonsAndDispatchesAction() = runTuIndiceUiTest {
		var selectedAction: TopBarAction? = null

		setTuIndiceTestContent {
			TopAppBarActionsView(
				topBarConfig = TopBarConfig.Summary,
				onAction = { action -> selectedAction = action }
			) { action ->
				Text(action.action)
			}
		}

		val signOutTag = BaseUiTags.topBarActionButton(TopBarAction.SignOutAction)
		assertNodeVisible(BaseUiTags.TopAppBarActionsContainer)
		assertNodeVisible(signOutTag)

		onNodeWithTag(signOutTag).performClick()
		assertEquals(TopBarAction.SignOutAction, selectedAction)
	}

	@Test
	fun when_configIsNull_then_hidesActionsContainer() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TopAppBarActionsView(
				topBarConfig = null,
				onAction = {}
			) { action ->
				Text(action.action)
			}
		}

		assertNodeHidden(BaseUiTags.TopAppBarActionsContainer)
	}

	@Test
	fun when_recordConfigActionTapped_then_dispatchesRecordTermSelectionAction() = runTuIndiceUiTest {
		var selectedAction: TopBarAction? = null

		setTuIndiceTestContent {
			TopAppBarActionsView(
				topBarConfig = TopBarConfig.Record,
				onAction = { action -> selectedAction = action }
			) { action ->
				Text(action.action)
			}
		}

		val actionTag = BaseUiTags.topBarActionButton(TopBarAction.RecordTermSelectionAction)
		assertNodeVisible(actionTag)

		onNodeWithTag(actionTag).performClick()
		assertEquals(TopBarAction.RecordTermSelectionAction, selectedAction)
	}

	@Test
	fun when_recordConfigAddsEnrollmentProof_then_dispatchesEnrollmentProofAction() = runTuIndiceUiTest {
		var selectedAction: TopBarAction? = null
		val topBarConfig = mutableStateOf<TopBarConfig?>(TopBarConfig.Record)

		setTuIndiceTestContent {
			TopAppBarActionsView(
				topBarConfig = topBarConfig.value,
				onAction = { action -> selectedAction = action }
			) { action ->
				Text(action.action)
			}
		}

		topBarConfig.value = TopBarConfig.RecordWithEnrollmentProof
		waitForIdle()

		val enrollmentProofTag = BaseUiTags.topBarActionButton(TopBarAction.FetchEnrollmentProofAction)
		assertNodeVisible(BaseUiTags.topBarActionButton(TopBarAction.RecordTermSelectionAction))
		assertNodeVisible(enrollmentProofTag)

		onNodeWithTag(enrollmentProofTag).performClick()
		assertEquals(TopBarAction.FetchEnrollmentProofAction, selectedAction)
	}

	@Test
	fun when_pensumConfigHasActions_then_rendersSearchOnly() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TopAppBarActionsView(
				topBarConfig = TopBarConfig.Pensum,
				onAction = {}
			) { action ->
				Text(action.action)
			}
		}

		assertNodeVisible(BaseUiTags.topBarActionButton(TopBarAction.SearchPensumAction))
		assertNodeHidden(BaseUiTags.topBarActionButton(TopBarAction.ChangePensumAction))
	}
}
