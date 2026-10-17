package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TopAppBarOverflowMenuViewUiTest {
	@Test
	fun when_overflowButtonIsTapped_then_theChosenActionIsDispatched() = runTuIndiceUiTest {
		var selectedAction: TopBarAction? = null

		setTuIndiceTestContent {
			TopAppBarActionsView(
				actions = emptyList(),
				overflowActions = listOf(TopBarAction.SignOutAction, TopBarAction.ChangePensumAction),
				onAction = { action -> selectedAction = action },
				actionContentDescription = { action -> action.action },
				actionIconContent = { action -> Text(action.action) }
			)
		}

		assertNodeVisible(BaseUiTags.TopAppBarOverflowButton)
		onNodeWithTag(BaseUiTags.TopAppBarOverflowButton).performClick()

		val itemTag = BaseUiTags.topBarOverflowItem(TopBarAction.ChangePensumAction)
		assertNodeVisible(itemTag)
		onNodeWithTag(itemTag).performClick()

		assertEquals(TopBarAction.ChangePensumAction, selectedAction)
	}

	@Test
	fun when_thereAreNoOverflowActions_then_theOverflowButtonIsNotDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TopAppBarActionsView(
				actions = listOf(TopBarAction.SignOutAction),
				overflowActions = emptyList(),
				onAction = {},
				actionContentDescription = { action -> action.action },
				actionIconContent = { action -> Text(action.action) }
			)
		}

		assertNodeVisible(BaseUiTags.topBarActionButton(TopBarAction.SignOutAction))
		assertNodeHidden(BaseUiTags.TopAppBarOverflowButton)
	}

	@Test
	fun when_anOverflowActionHasNoDescription_then_itsItemIsNotDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TopAppBarActionsView(
				actions = emptyList(),
				overflowActions = listOf(TopBarAction.SignOutAction, TopBarAction.ChangePensumAction),
				onAction = {},
				actionContentDescription = { action ->
					if (action == TopBarAction.SignOutAction) null else action.action
				},
				actionIconContent = { action -> Text(action.action) }
			)
		}

		assertNodeVisible(BaseUiTags.TopAppBarOverflowButton)
		onNodeWithTag(BaseUiTags.TopAppBarOverflowButton).performClick()

		// The item with a description shows that the menu is open, so the missing one is missing for its own reason.
		assertNodeVisible(BaseUiTags.topBarOverflowItem(TopBarAction.ChangePensumAction))
		assertNodeHidden(BaseUiTags.topBarOverflowItem(TopBarAction.SignOutAction))
	}
}
