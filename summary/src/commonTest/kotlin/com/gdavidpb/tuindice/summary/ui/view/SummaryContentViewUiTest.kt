package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.testing.summaryContentState
import com.gdavidpb.tuindice.summary.testing.summaryItemsFor
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
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
				syncStatus = SyncStatus.Healthy,
				summaryItems = items,
				onEditProfilePictureClick = { editClicks++ },
				onStatusIconClick = {}
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
	fun when_userRefreshIsRunning_then_profilePictureEditIsDisabled() = runTuIndiceUiTest {
		var editClicks = 0
		val contentState = summaryContentState(isUserRefreshing = true)

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				SummaryContentView(
					state = contentState,
					syncStatus = SyncStatus.Healthy,
					summaryItems = summaryItemsFor(contentState),
					onEditProfilePictureClick = { editClicks++ },
					onStatusIconClick = {}
				)
			}
		}

		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).assertIsNotEnabled()
		assertEquals(0, editClicks)
	}

	@Test
	fun when_summaryHasNoItems_then_rendersHeaderSectionWithoutStatusCards() = runTuIndiceUiTest {
		val contentState = summaryContentState()

		setTuIndiceTestContent {
			SummaryContentView(
				state = contentState,
				syncStatus = SyncStatus.Healthy,
				summaryItems = emptyList(),
				onEditProfilePictureClick = {},
				onStatusIconClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.ContentContainer)
		assertNodeVisible(SummaryUiTags.NameText)
		assertNodeHidden(SummaryUiTags.statusCard(0))
	}

	@Test
	fun when_summaryContentIsVisible_then_displaysStatusIconAndText() = runTuIndiceUiTest {
		val contentState = summaryContentState()

		setTuIndiceTestContent {
			SummaryContentView(
				state = contentState,
				syncStatus = SyncStatus.Healthy,
				summaryItems = summaryItemsFor(contentState),
				onEditProfilePictureClick = {},
				onStatusIconClick = {}
			)
		}

		assertNodeVisible(SummaryUiTags.StatusRow)
		onNodeWithTag(
			SummaryUiTags.StatusIcon,
			useUnmergedTree = true
		).assertIsDisplayed()
		assertNodeVisible(SummaryUiTags.StatusText)
		assertNodeVisible(SummaryUiTags.StatusIconButton)
		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsNotEnabled()
		onNodeWithText(contentState.lastUpdate).assertIsDisplayed()
	}

	@Test
	fun when_syncIsRunning_then_statusIconRemainsVisibleBesideLastUpdate() = runTuIndiceUiTest {
		val contentState = summaryContentState()

		mainClock.autoAdvance = false

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				SummaryContentView(
					state = contentState,
					syncStatus = SyncStatus.Healthy,
					isSyncing = true,
					summaryItems = summaryItemsFor(contentState),
					onEditProfilePictureClick = {},
					onStatusIconClick = {}
				)
			}
		}

		onNodeWithTag(
			SummaryUiTags.StatusIcon,
			useUnmergedTree = true
		).assertIsDisplayed()
		onNodeWithText(contentState.lastUpdate).assertIsDisplayed()
	}

	@Test
	fun when_syncIsRunningWithHealthyStatus_then_statusIconUsesLoadingRotation() {
		assertEquals(
			expected = -180f,
			actual = syncStatusIconRotation(
				isStatusRefreshing = true,
				currentRotation = -180f
			)
		)
	}

	@Test
	fun when_userRefreshIsRunningWithHealthyStatus_then_statusIconUsesLoadingRotation() {
		val contentState = summaryContentState(isUserRefreshing = true)

		assertEquals(
			expected = -180f,
			actual = syncStatusIconRotation(
				isStatusRefreshing = contentState.isUserRefreshing,
				currentRotation = -180f
			)
		)
	}

	@Test
	fun when_syncStatusIsNotHealthyButRefreshing_then_statusIconUsesLoadingRotation() {
		listOf(
			SyncStatus.Failed,
			SyncStatus.Unavailable,
			SyncStatus.OutdatedCredentials
		).forEach { syncStatus ->
			assertEquals(
				expected = Icons.Outlined.Sync,
				actual = syncStatusIcon(
					syncStatus = syncStatus,
					isStatusRefreshing = true
				)
			)
			assertEquals(
				expected = -180f,
				actual = syncStatusIconRotation(
					isStatusRefreshing = true,
					currentRotation = -180f
				)
			)
		}
	}

	@Test
	fun when_statusIsNotRefreshing_then_statusIconDoesNotUseLoadingRotationOrOverrideErrorIcon() {
		assertEquals(
			expected = Icons.Outlined.SyncProblem,
			actual = syncStatusIcon(
				syncStatus = SyncStatus.Failed,
				isStatusRefreshing = false
			)
		)
		assertEquals(
			expected = 0f,
			actual = syncStatusIconRotation(
				isStatusRefreshing = false,
				currentRotation = -180f
			)
		)
	}

	@Test
	fun when_syncHasFailedButRefreshIsRunning_then_statusShowsLoadingAndDetailsAreTemporarilyDisabled() =
		runTuIndiceUiTest {
			val contentState = summaryContentState()
			var statusIconClicks = 0

			setTuIndiceTestContent {
				CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
					SummaryContentView(
						state = contentState,
						syncStatus = SyncStatus.Failed,
						isSyncing = true,
						summaryItems = summaryItemsFor(contentState),
						onEditProfilePictureClick = {},
						onStatusIconClick = { statusIconClicks++ }
					)
				}
			}

			assertNodeVisible(SummaryUiTags.StatusRow)
			assertNodeVisible(SummaryUiTags.StatusIconButton)
			onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsNotEnabled()
			onNodeWithTag(SummaryUiTags.StatusText).assertTextContains(contentState.lastUpdate)
			assertEquals(0, statusIconClicks)
		}

	@Test
	fun when_syncHasFailed_then_statusKeepsLastUpdateAndCanOpenDetails() = runTuIndiceUiTest {
		val contentState = summaryContentState()
		var statusIconClicks = 0

		setTuIndiceTestContent {
			SummaryContentView(
				state = contentState,
				syncStatus = SyncStatus.Failed,
				summaryItems = summaryItemsFor(contentState),
				onEditProfilePictureClick = {},
				onStatusIconClick = { statusIconClicks++ }
			)
		}

		assertNodeVisible(SummaryUiTags.StatusRow)
		assertNodeVisible(SummaryUiTags.StatusIconButton)
		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsEnabled()
		onNodeWithTag(SummaryUiTags.StatusText).assertTextContains(contentState.lastUpdate)
		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()
		assertEquals(1, statusIconClicks)
	}

	@Test
	fun when_syncIsUnavailable_then_statusKeepsLastUpdateAndCanOpenDetails() = runTuIndiceUiTest {
		val contentState = summaryContentState()
		var statusIconClicks = 0

		setTuIndiceTestContent {
			SummaryContentView(
				state = contentState,
				syncStatus = SyncStatus.Unavailable,
				summaryItems = summaryItemsFor(contentState),
				onEditProfilePictureClick = {},
				onStatusIconClick = { statusIconClicks++ }
			)
		}

		assertNodeVisible(SummaryUiTags.StatusRow)
		assertNodeVisible(SummaryUiTags.StatusIconButton)
		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsEnabled()
		onNodeWithTag(SummaryUiTags.StatusText).assertTextContains(contentState.lastUpdate)
		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()
		assertEquals(1, statusIconClicks)
	}

	@Test
	fun when_syncStatusIsOutdatedCredentials_then_statusKeepsLastUpdateAndCanOpenDetails() = runTuIndiceUiTest {
		val contentState = summaryContentState()
		var statusIconClicks = 0

		setTuIndiceTestContent {
			SummaryContentView(
				state = contentState,
				syncStatus = SyncStatus.OutdatedCredentials,
				summaryItems = summaryItemsFor(contentState),
				onEditProfilePictureClick = {},
				onStatusIconClick = { statusIconClicks++ }
			)
		}

		assertNodeVisible(SummaryUiTags.StatusRow)
		assertNodeVisible(SummaryUiTags.StatusIconButton)
		onNodeWithTag(SummaryUiTags.StatusIconButton).assertIsEnabled()
		onNodeWithTag(SummaryUiTags.StatusText).assertTextContains(contentState.lastUpdate)
		onNodeWithTag(SummaryUiTags.StatusIconButton).performClick()
		assertEquals(1, statusIconClicks)
	}
}
