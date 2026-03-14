package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.summary.testing.summaryContentState
import com.gdavidpb.tuindice.summary.testing.summaryItemsFor
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryContentViewUiTest {
	@Test
	fun when_editProfilePictureTapped_then_invokesEditCallback() = runTuIndiceUiTest {
		var editClicks = 0
		val contentState = summaryContentState()
		val items = summaryItemsFor(contentState)

		setTuIndiceTestContent {
			SummaryContentView(
				state = contentState,
				summaryItems = items,
				onEditProfilePictureClick = { editClicks++ }
			)
		}

		assertNodeVisible(SummaryUiTags.ContentContainer)
		assertNodeVisible(SummaryUiTags.GradeText)
		assertNodeVisible(SummaryUiTags.NameText)
		assertNodeVisible(SummaryUiTags.CareerText)
		assertNodeVisible(SummaryUiTags.StatusRow)
		assertNodeVisible(SummaryUiTags.ItemsList)
		assertNodeVisible(SummaryUiTags.statusCard(0))
		assertNodeVisible(SummaryUiTags.statusCard(1))

		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).performClick()

		assertEquals(1, editClicks)
	}

	@Test
	fun when_summaryHasNoItems_then_rendersHeaderSectionWithoutStatusCards() = runTuIndiceUiTest {
		val contentState = summaryContentState(
			isUpdated = false
		)

		setTuIndiceTestContent {
			SummaryContentView(
				state = contentState,
				summaryItems = emptyList(),
				onEditProfilePictureClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.ContentContainer)
		assertNodeVisible(SummaryUiTags.NameText)
		assertNodeHidden(SummaryUiTags.statusCard(0))
	}

	@Test
	fun when_summaryIsOutdated_then_displaysStatusIconAndText() = runTuIndiceUiTest {
		val contentState = summaryContentState(
			isUpdated = false
		)

		setTuIndiceTestContent {
			SummaryContentView(
				state = contentState,
				summaryItems = summaryItemsFor(contentState),
				onEditProfilePictureClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.StatusRow)
		assertNodeVisible(SummaryUiTags.StatusIcon)
		assertNodeVisible(SummaryUiTags.StatusText)
	}
}
